package se.sundsvall.seabloader.batchimport.model;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class StralforsFile { // TODO: Remove after completion of Stralfors invoices import

	@JacksonXmlProperty(localName = "name", isAttribute = true)
	private String fileName;

	@JacksonXmlProperty(isAttribute = true)
	private String pages;

	private Document document;

	private String pdf;

	private boolean failed;

	private boolean imported;

	public String getName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public String getPages() {
		return pages;
	}

	public void setPages(String pages) {
		this.pages = pages;
	}

	public String getPdf() {
		return pdf;
	}

	public void setPdf(String pdf) {
		this.pdf = pdf;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed() {
		this.failed = true;
	}

	public boolean isImported() {
		return imported;
	}

	public void setImported() {
		if (nonNull(this.pdf)) {
			this.pdf = null;
			this.imported = true;
		}
	}
}