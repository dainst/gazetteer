package org.dainst.gazetteer.helpers;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;

public class TempFolderService {
		
	@Value("${tempDirectoryPath}")
	private String tempDirectoryPath;

	public File createFolder() throws Exception {
		
		DateFormat dateFormat = new SimpleDateFormat("MMddyyyyHHmmssSSS");
		
		File tempFolder = new File(tempDirectoryPath);
		if (!tempFolder.exists() && !tempFolder.mkdir()) {
			throw new Exception("Failed to create directory " + tempFolder.getAbsolutePath());
		}
		
		File folder;
		do {
			folder = new File(tempDirectoryPath + File.separator + dateFormat.format(new Date()));
		} while (folder.exists());
						
		if (!folder.mkdir()) {
			throw new Exception("Failed to create directory " + folder.getAbsolutePath());
		}
		
		return folder;
	}
}
