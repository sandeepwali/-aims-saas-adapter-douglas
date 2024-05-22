package com.solumesl.aims.saas.dou.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tomcat.util.codec.binary.Base64;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class DouglasUtils {
	
	public static boolean checkFileReady(File articleFile) {
		Path fullPath =  Path.of(articleFile.getAbsolutePath());
		try
		{
			long size1 = 1L;
			log.info(fullPath.toAbsolutePath() + " Size: " + size1 + " Byte");
			long size2 = 0L;

			while(size1 != size2)
			{
				size1 = Files.size(fullPath);
				TimeUnit.MILLISECONDS.sleep(1000);
				size2 = Files.size(fullPath);
				log.info(fullPath.toAbsolutePath() + " =>Upload Size: " + size1 + " Byte versus Size:" + size2 + " Byte");
			}
		} catch (NumberFormatException | IOException | InterruptedException e)
		{
			log.error("checkFileReady {}",e.getMessage());
			return false;
		}
		return true;
	}
	
	public static String zipFile(String fileName) throws IOException {
		String zipFileName = fileName.replace(".dat", ".zip");
		FileOutputStream fos = new FileOutputStream(zipFileName);
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		File fileToZip = new File(fileName);
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		zipOut.close();
		fis.close();
		fos.close();
		return zipFileName;
	}
	
	public static  String convertZipFileToBaseEncodeString(String fileName) {
		File originalFile = new File(fileName);
		String encodedBase64 = null;
		try {
			try (FileInputStream fileInputStreamReader = new FileInputStream(originalFile)) {
				byte[] bytes = new byte[(int) originalFile.length()];
				fileInputStreamReader.read(bytes);
				encodedBase64 = new String(Base64.encodeBase64(bytes));
			}
		} catch (FileNotFoundException e) {
		log.error("{}",e);
		} catch (IOException e) {
			log.error("{}",e);
		}
		return encodedBase64;

	}
}
