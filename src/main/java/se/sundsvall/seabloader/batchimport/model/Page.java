package se.sundsvall.seabloader.batchimport.model;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Page { // TODO: Remove after completion of Stralfors invoices import

	@JacksonXmlProperty(isAttribute = true)
	private String number;

	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "index")
	private List<Entry> informationEntries;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public List<Entry> getInformationEntries() {
		return informationEntries;
	}

	public void setInformationEntries(List<Entry> informationEntries) {
		this.informationEntries = informationEntries;
	}

}
