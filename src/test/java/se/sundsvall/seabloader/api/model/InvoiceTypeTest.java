package se.sundsvall.seabloader.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.seabloader.api.model.InvoiceType.CONSOLIDATED_INVOICE;
import static se.sundsvall.seabloader.api.model.InvoiceType.CREDIT_INVOICE;
import static se.sundsvall.seabloader.api.model.InvoiceType.DIRECT_DEBIT;
import static se.sundsvall.seabloader.api.model.InvoiceType.INVOICE;
import static se.sundsvall.seabloader.api.model.InvoiceType.REMINDER;
import static se.sundsvall.seabloader.api.model.InvoiceType.SELF_INVOICE;

class InvoiceTypeTest {

	@Test
	void values() {
		assertThat(InvoiceType.values()).containsExactly(INVOICE, CREDIT_INVOICE, DIRECT_DEBIT, SELF_INVOICE, REMINDER, CONSOLIDATED_INVOICE);
	}

	@Test
	void fromValue() {
		assertThat(InvoiceType.fromValue("Faktura")).isEqualTo(INVOICE);
		assertThat(InvoiceType.fromValue("Kreditfaktura")).isEqualTo(CREDIT_INVOICE);
		assertThat(InvoiceType.fromValue("Autogiro")).isEqualTo(DIRECT_DEBIT);
		assertThat(InvoiceType.fromValue("Självfaktura")).isEqualTo(SELF_INVOICE);
		assertThat(InvoiceType.fromValue("Påminnelse")).isEqualTo(REMINDER);
		assertThat(InvoiceType.fromValue("Samlingsfaktura")).isEqualTo(CONSOLIDATED_INVOICE);
	}
}
