package se.sundsvall.seabloader.batchimport.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class Entry { // TODO: Remove after completion of Stralfors invoices import

	@JacksonXmlProperty(localName = "name", isAttribute = true)
	private String key;

	@JacksonXmlText(value = true)
	private String value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = Optional.ofNullable(key).map(String::toLowerCase).orElse(key);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		Optional.ofNullable(value)
			.filter(s -> !StringUtils.equalsIgnoreCase("NULL", s))
			.ifPresentOrElse(s -> this.value = s, () -> this.value = null);
	}
}
