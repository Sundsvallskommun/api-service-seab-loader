package se.sundsvall.seabloader.api.model;

public enum InvoiceType {
	INVOICE("Faktura"),
	CREDIT_INVOICE("Kreditfaktura"),
	DIRECT_DEBIT("Autogiro"),
	SELF_INVOICE("Självfaktura"),
	REMINDER("Påminnelse"),
	CONSOLIDATED_INVOICE("Samlingsfaktura");

	private final String value;

	InvoiceType (final String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static InvoiceType fromValue(final String value) {
		for (InvoiceType invoiceType : InvoiceType.values()) {
			if (invoiceType.value.equals(value)) {
				return invoiceType;
			}
		}
		throw new IllegalArgumentException("Unexpected value '" + value + "'");
	}
}
