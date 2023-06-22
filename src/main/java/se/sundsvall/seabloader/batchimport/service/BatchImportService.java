package se.sundsvall.seabloader.batchimport.service;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import jakarta.persistence.EntityManager;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.seabloader.batchimport.mapper.StralforsMapper;
import se.sundsvall.seabloader.batchimport.model.StralforsFile;
import se.sundsvall.seabloader.batchimport.model.XmlRoot;
import se.sundsvall.seabloader.batchimport.util.ImportUtility;
import se.sundsvall.seabloader.batchimport.util.NotificationService;
import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;

@Service
public class BatchImportService { // TODO: Remove after completion of Stralfors invoices import

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchImportService.class);
	private static final String ENTRY_ROW = "%n%nEntry for pdf file %s";
	private static final int BATCH_SIZE = 100;

	@Autowired
	private InvoiceRepository repository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private NotificationService notificationService;

	private ObjectMapper objectMapper;

	public BatchImportService() {
		objectMapper = new XmlMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	public void execute(String pathToScan) {
		RequestId.init();

		final var directory = new File(pathToScan);
		final var filter = XmlFilenameFilter.create();

		if (directory.isDirectory() && directory.listFiles(filter).length > 0) {
			processDescriptorFiles(directory.listFiles(filter));
		}
	}

	private void processDescriptorFiles(File[] descriptorFiles) {
		List.of(descriptorFiles).stream().forEach(descriptorFile -> {
			try {
				processDescriptorFile(descriptorFile);
			} catch (Exception e) {
				LOGGER.error("Execption when processing descriptor file {}", descriptorFile.getPath(), e);
			}
		});
	}

	private void processDescriptorFile(File descriptorFile) throws IOException {
		final var xmlRoot = objectMapper.readValue(FileUtils.readFileToString(descriptorFile, StandardCharsets.UTF_8), XmlRoot.class);
		final var batches = ListUtils.partition(xmlRoot.getBody().getFiles(), BATCH_SIZE);
		final var currentBatch = new AtomicInteger();

		batches.forEach(batch -> {
			LOGGER.info("Processing batch {} of {}", currentBatch.addAndGet(1), batches.size());
			processEntriesInBatch(descriptorFile.getParent(), batch);
		});

		sendNotification(descriptorFile, xmlRoot);
	}

	private void processEntriesInBatch(String directory, List<StralforsFile> stralforsFiles) {
		final List<InvoiceEntity> entitiesToSave = stralforsFiles.stream()
			.filter(ImportUtility::isProcessable)
			.filter(stralforsFile -> !repository.existsByInvoiceId(ImportUtility.extractLowestInvoiceNumber(stralforsFile)))
			.map(stralforsFile -> addPdfData(directory, stralforsFile))
			.filter(Objects::nonNull)
			.map(this::toInvoiceEntity)
			.filter(Objects::nonNull)
			.toList();

		// Save as entity in DB
		repository.saveAllAndFlush(entitiesToSave);

		// Remove pdf from file items in batch and clear entity manager to use minimum memory footprint
		stralforsFiles.stream().forEach(StralforsFile::removePdf);
		entityManager.clear();
	}

	private StralforsFile addPdfData(String directory, StralforsFile stralforsFile) {
		try {
			// Read and add pdf as base64 to file object
			final var inFileBytes = Files.readAllBytes(Paths.get(directory + File.separator + stralforsFile.getName()));
			stralforsFile.setPdf(new String(Base64.encodeBase64(inFileBytes)));
			return stralforsFile;
		} catch (IOException e) {
			LOGGER.error("Exception when adding pdf data to entry for pdf {}", stralforsFile.getName(), e);
			stralforsFile.setFailed(true);
			return null;
		}
	}

	private InvoiceEntity toInvoiceEntity(StralforsFile stralforsFile) {
		try {
			return StralforsMapper.toInvoiceEntity(stralforsFile);
		} catch (JsonProcessingException e) {
			LOGGER.error("Exception when mapping entry for pdf {} to an invoice entity", stralforsFile.getName(), e);
			stralforsFile.setFailed(true);
			return null;
		}
	}

	private void sendNotification(File descriptorFile, final XmlRoot xmlRoot) {
		final var failedEntries = xmlRoot.getBody().getFiles().stream()
			.filter(StralforsFile::isFailed)
			.toList();

		if (failedEntries.isEmpty()) {
			notificationService.sendNotification(
				String.format("Successful import for path %s", descriptorFile.getParent()),
				String.format("%s entries have been processed.", xmlRoot.getBody().getFiles().size()));
		} else {
			final var message = new StringBuilder().append(format("%s of %s entries resulted in error. The following entries threw exceptions:%n", failedEntries.size(), xmlRoot.getBody().getFiles().size()));
			failedEntries.forEach(entry -> message.append(String.format(ENTRY_ROW, entry.getName())));
			message.append(format("%n%n%nRequestId for operation is %s", RequestId.get()));

			notificationService.sendNotification(
				String.format("Import for path %s resulted in errors", descriptorFile.getParent()),
				message.toString());
		}
	}
}
