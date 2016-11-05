package org.dainst.gazetteer.helpers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipArchiveBuilder {
	
	static final int BUFFER = 2048;
	
	public static File buildZipArchiveFromFolder(File inputFolder, File outputFolder) throws Exception {
		
		String outputFilePath = outputFolder.getAbsolutePath() + File.separator + inputFolder.getName() + ".zip";
				
		FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath);
		ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
		zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
		
		byte data[] = new byte[BUFFER];
		
		try {
		
			String fileNames[] = inputFolder.list();
			for (String fileName : fileNames) {
				FileInputStream fileInputStream = new FileInputStream(inputFolder.getAbsolutePath() + File.separator + fileName);
				BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream, BUFFER);
				
				try {
					ZipEntry entry = new ZipEntry(inputFolder.getName() + File.separator + fileName);			
					zipOutputStream.putNextEntry(entry);
					
					int count;
		            while((count = bufferedInputStream.read(data, 0, BUFFER)) != -1) {
		               zipOutputStream.write(data, 0, count);
		            }
	
				} catch (Exception e) {
					throw e;
				} finally {
					bufferedInputStream.close();
				}
			}
		} catch(Exception e) {
			throw e;
		} finally {
			zipOutputStream.close();
		}
		
		return new File(outputFilePath);
	}
	
}
