package se.sundsvall.seabloader.service.mapper.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.seabloader.service.mapper.model.InvoiceType.CONSOLIDATED_INVOICE;
import static se.sundsvall.seabloader.service.mapper.model.InvoiceType.CREDIT_INVOICE;
import static se.sundsvall.seabloader.service.mapper.model.InvoiceType.DIRECT_DEBIT;
import static se.sundsvall.seabloader.service.mapper.model.InvoiceType.FINAL_INVOICE;
import static se.sundsvall.seabloader.service.mapper.model.InvoiceType.INVOICE;
import static se.sundsvall.seabloader.service.mapper.model.InvoiceType.REMINDER;
import static se.sundsvall.seabloader.service.mapper.model.InvoiceType.SELF_INVOICE;

import org.junit.jupiter.api.Test;

class InvoiceTypeTest {

	@Test
	void values() {
		assertThat(InvoiceType.values()).containsExactly(INVOICE, CREDIT_INVOICE, DIRECT_DEBIT, SELF_INVOICE, REMINDER, CONSOLIDATED_INVOICE, FINAL_INVOICE);
	}

	@Test
	void fromValue() {
		assertThat(InvoiceType.fromValue("Faktura")).isEqualTo(INVOICE);
		assertThat(InvoiceType.fromValue("Kreditfaktura")).isEqualTo(CREDIT_INVOICE);
		assertThat(InvoiceType.fromValue("Autogiro")).isEqualTo(DIRECT_DEBIT);
		assertThat(InvoiceType.fromValue("Självfaktura")).isEqualTo(SELF_INVOICE);
		assertThat(InvoiceType.fromValue("Påminnelse")).isEqualTo(REMINDER);
		assertThat(InvoiceType.fromValue("Samlingsfaktura")).isEqualTo(CONSOLIDATED_INVOICE);
		assertThat(InvoiceType.fromValue("Slutfaktura")).isEqualTo(FINAL_INVOICE);
	}
}
