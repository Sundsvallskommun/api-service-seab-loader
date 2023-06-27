package se.sundsvall.seabloader.batchimport.service;

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;

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
import org.springframework.scheduling.annotation.Async;
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
import se.sundsvall.seabloader.integration.db.InvoiceRepository;
import se.sundsvall.seabloader.integration.db.model.InvoiceEntity;

@Service
public class BatchImportService { // TODO: Remove after completion of Stralfors invoices import

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchImportService.class);
	private static final String MAIL_PREFIX = "%s records have been processed, of which %s records were either present from previous runs, were missing crucial data or had an unprocessable status. Therefore %s records have been imported. ";
	private static final String FAILED_RECORD_ROW = "%n%nRecord for pdf file %s";
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

	@Async
	public void execute(String pathToScan) {
		RequestId.init();

		final var directory = new File(pathToScan);
		final var filter = XmlFilenameFilter.create();

		if (directory.isDirectory() && directory.listFiles(filter).length > 0) {
			processDescriptorFiles(directory.listFiles(filter));
		} else {
			notificationService.sendNotification(
				String.format("Import for path %s", pathToScan),
				"No records have been processed as directory does not exist or does not contain any descriptor files.");
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

		// Remove pdf and set to imported for each saved item in batch and clear entity manager to use minimum memory footprint
		stralforsFiles.stream().forEach(StralforsFile::setImported);
		entityManager.clear();
	}

	private StralforsFile addPdfData(String directory, StralforsFile stralforsFile) {
		try {
			// Read and add pdf as base64 to file object
			final var inFileBytes = Files.readAllBytes(Paths.get(directory + File.separator + stralforsFile.getName()));
			stralforsFile.setPdf(new String(Base64.encodeBase64(inFileBytes), defaultCharset()));
			return stralforsFile;
		} catch (IOException e) {
			LOGGER.error("Exception when adding pdf data to record for pdf {}", stralforsFile.getName(), e);
			stralforsFile.setFailed();
			return null;
		}
	}

	private InvoiceEntity toInvoiceEntity(StralforsFile stralforsFile) {
		try {
			return StralforsMapper.toInvoiceEntity(stralforsFile);
		} catch (JsonProcessingException e) {
			LOGGER.error("Exception when mapping stralfors record for pdf {} to an invoice entity", stralforsFile.getName(), e);
			stralforsFile.setFailed();
			return null;
		}
	}

	private void sendNotification(File descriptorFile, final XmlRoot xmlRoot) {
		final var total = xmlRoot.getBody().getFiles().size();
		final var imported = xmlRoot.getBody().getFiles().stream()
			.filter(StralforsFile::isImported)
			.count();
		final var failedRecords = xmlRoot.getBody().getFiles().stream()
			.filter(StralforsFile::isFailed)
			.toList();

		if (failedRecords.isEmpty()) {
			notificationService.sendNotification(
				format("Successful import for path %s", descriptorFile.getParent()),
				format(MAIL_PREFIX, total, total - imported, imported));
		} else {
			final var message = new StringBuilder().append(format(MAIL_PREFIX, total, total - imported, imported));
			message.append(format("%s of the records resulted in error. The following records threw exceptions:%n", failedRecords.size()));
			failedRecords.forEach(entry -> message.append(format(FAILED_RECORD_ROW, entry.getName())));
			message.append(format("%n%n%nRequestId for operation is %s", RequestId.get()));

			notificationService.sendNotification(
				format("Import for path %s resulted in errors", descriptorFile.getParent()),
				message.toString());
		}
	}
}
