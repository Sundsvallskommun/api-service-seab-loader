package se.sundsvall.seabloader.batchimport.service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Optional;

public class XmlFilenameFilter implements FilenameFilter { // TODO: Remove after completion of Stralfors invoices import

	public static XmlFilenameFilter create() {
		return new XmlFilenameFilter();
	}

	@Override
	public boolean accept(File dir, String name) {
		return Optional.ofNullable(name).orElse("").toLowerCase().endsWith(".xml");
	}
}
