package se.sundsvall.seabloader.batchimport.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "ddcold_index") // TODO: Remove after completion of Stralfors invoices import

/**
 * Model to map xml structure:
 * 
 * <code>
 *  <?xml version="1.0" encoding="UTF-8"?>
 *  <ddcold_index>
 *    <header>
 *      <doctype>Documents</doctype>
 *    </header>
 *    <body>
 *      <file name="1379140_1391851.pdf" pages="2">
 *        <document>
 *          <page number="">
 *            <index name="customer_number">123456</index>
 *            <index name="Name">Efternamn Förnamn Mellannamn</index>
 *            <index name="Invoice_type">Faktura</index>
 *            <index name="invoice_date">2020-06-02 00:00:00.000</index>
 *            <index name="invoice_number">123456789</index>
 *            <index name="facility_adress">1234567890</index>
 *            <index name="facility_number">Stora gatan 1</index>
 *            <index name="property_id">NULL</index>
 *            <index name="amount">1 234,00</index>
 *          </page>
 *        </document>
 *      </file>
 *    </body>
 *  </ddcold_index>
 * </code>
 * 
 * to its java equivalent object
 */
public class XmlRoot {
	private Body body;

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}
}
