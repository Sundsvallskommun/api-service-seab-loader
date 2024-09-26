package se.sundsvall.seabloader.service.mapper.model;

import java.util.Arrays;

public enum InvoiceType {

	INVOICE("Faktura"),
	CREDIT_INVOICE("Kreditfaktura"),
	DIRECT_DEBIT("Autogiro"),
	SELF_INVOICE("Självfaktura"),
	REMINDER("Påminnelse"),
	CONSOLIDATED_INVOICE("Samlingsfaktura"),
	FINAL_INVOICE("Slutfaktura");

	private final String value;

	InvoiceType(final String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static InvoiceType fromValue(final String value) {
		return Arrays.stream(values())
			.filter(enumObj -> enumObj.value.equals(value))
			.findFirst().orElseThrow(() -> new IllegalArgumentException("Illegal enum value: %s".formatted(value)));
	}
}
