package se.sundsvall.seabloader.batchimport.util;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.RegExUtils.removeAll;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import se.sundsvall.seabloader.batchimport.model.Entry;
import se.sundsvall.seabloader.batchimport.model.StralforsFile;

public class ImportUtility { // TODO: Remove after completion of Stralfors invoices import

	public static final String ENTRY_KEY_INVOICE_NUMBER = "invoice_number";
	public static final String ENTRY_KEY_INVOICE_TYPE = "invoice_type";
	public static final String ENTRY_KEY_CUSTOMER_NBR = "customer_number";
	private static final String INVOICE_TYPE_REMINDER = "påminnelse";

	private ImportUtility() {}

	public static String extractLowestInvoiceNumber(StralforsFile stralforsFile) {
		final var value = ofNullable(getEntryValue(stralforsFile, ENTRY_KEY_INVOICE_NUMBER)).orElse("");
		return List.of(value.split(";")).stream()
			.sorted()
			.findFirst()
			.map(invoiceNumber -> removeAll(invoiceNumber, "'"))
			.orElse(value);
	}

	public static boolean isProcessable(StralforsFile stralforsFile) {
		final var invoiceNumber = extractLowestInvoiceNumber(stralforsFile);

		final var isReminder = Stream.of(ofNullable(getEntryValue(stralforsFile, ENTRY_KEY_INVOICE_TYPE)).orElse(""))
			.map(StringUtils::trim)
			.anyMatch(INVOICE_TYPE_REMINDER::equalsIgnoreCase);

		final var isInvoiceNumberPresent = Stream.of(ofNullable(invoiceNumber).orElse(""))
			.anyMatch(StringUtils::isNotBlank);

		return !isReminder && isInvoiceNumberPresent;
	}

	public static String getEntryValue(StralforsFile stralforsFile, String key) {
		return stralforsFile.getDocument().getPage().getInformationEntries().stream()
			.filter(e -> key.equalsIgnoreCase(e.getKey()))
			.findAny()
			.map(Entry::getValue)
			.map(StringUtils::trim)
			.orElse(null);
	}
}
