package se.sundsvall.seabloader.batchimport.model;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Body { // TODO: Remove after completion of Stralfors invoices import

	@JacksonXmlProperty(localName = "file")
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<StralforsFile> files;

	public List<StralforsFile> getFiles() {
		return files;
	}

	public void setFiles(List<StralforsFile> files) {
		this.files = files;
	}
}
