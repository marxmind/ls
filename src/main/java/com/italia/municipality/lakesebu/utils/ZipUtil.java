package com.italia.municipality.lakesebu.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 04/12/2023
 *
 */
public class ZipUtil {

	public static void zipFileSingle(String fullPathAndFileName, String destination, String zipName) {
		   FileOutputStream fos=null;
			ZipOutputStream zipOut=null;
			try {
				
				fos = new FileOutputStream(destination+zipName);
				zipOut = new ZipOutputStream(fos);
				File fileToZip = new File(fullPathAndFileName);
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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   }
	   
	   public static void zipMultipleFile(List<String> srcFiles,String destination, String zipName) {
		   try {
		   final FileOutputStream fos = new FileOutputStream(Paths.get(destination).getParent().toAbsolutePath() + zipName + ".zip");
	       ZipOutputStream zipOut = new ZipOutputStream(fos);

	       for (String srcFile : srcFiles) {
	           File fileToZip = new File(srcFile);
	           FileInputStream fis = new FileInputStream(fileToZip);
	           ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
	           zipOut.putNextEntry(zipEntry);

	           byte[] bytes = new byte[1024];
	           int length;
	           while((length = fis.read(bytes)) >= 0) {
	               zipOut.write(bytes, 0, length);
	           }
	           fis.close();
	       }

	       zipOut.close();
	       fos.close();
		   }catch(Exception e) {}
	   }
	    
	   public static void zipDirectory(String sourceFileFolder, String zipDestination,String zipName) {
		   System.out.println("directory: " + sourceFileFolder);
		   System.out.println("zip destination: " + zipDestination);
		   System.out.println("zip name: " + zipName);
		   //make directory first
		   File fileDir = new File(zipDestination);
		   fileDir.mkdir();
		   try {
		   
	       FileOutputStream fos = new FileOutputStream(zipDestination + zipName +".zip");
	       ZipOutputStream zipOut = new ZipOutputStream(fos);

	       File fileToZip = new File(sourceFileFolder);
	       //zipFile(fileToZip, fileToZip.getName(), zipOut,"sql");
	       zipFile(fileToZip, zipName, zipOut,"sql");
	       zipOut.close();
	       fos.close();
	       
		   }catch(Exception e) {e.printStackTrace();}
	   }
	   
	   /**
	    * 
	    * @param fileToZip
	    * @param fileName
	    * @param zipOut
	    * @param fileExt default all or provide specific
	    * @throws IOException
	    */
	   private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut,String fileExt) throws IOException {
	       if (fileToZip.isHidden()) {
	           return;
	       }
	       if (fileToZip.isDirectory()) {
	           if (fileName.endsWith("/")) {
	               zipOut.putNextEntry(new ZipEntry(fileName));
	               zipOut.closeEntry();
	           } else {
	               zipOut.putNextEntry(new ZipEntry(fileName + "/"));
	               zipOut.closeEntry();
	           }
	           File[] children = fileToZip.listFiles();
	           for (File childFile : children) {
	        	   String ext = "all"; 
	        	   try{ext = childFile.getName().split("\\.")[1];}catch(ArrayIndexOutOfBoundsException e) {ext = "err"; }
	        	   System.out.println("File: "+childFile.getName());
	        	   if(ext.equalsIgnoreCase("sql")) {
	        		   zipFile(childFile, fileName + "/" + childFile.getName(), zipOut,ext);
	        	   }else if(ext.equalsIgnoreCase("all")) {
	        		   zipFile(childFile, fileName + "/" + childFile.getName(), zipOut,"all");
	        	   }
	           }
	           return;
	       }
	       FileInputStream fis = new FileInputStream(fileToZip);
	       ZipEntry zipEntry = new ZipEntry(fileName);
	       zipOut.putNextEntry(zipEntry);
	       byte[] bytes = new byte[1024];
	       int length;
	       while ((length = fis.read(bytes)) >= 0) {
	           zipOut.write(bytes, 0, length);
	       }
	       fis.close();
	   }
	
}
