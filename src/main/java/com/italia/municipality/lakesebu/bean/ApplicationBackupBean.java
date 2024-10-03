package com.italia.municipality.lakesebu.bean;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import com.italia.municipality.lakesebu.controller.SyncData;
import com.italia.municipality.lakesebu.database.Conf;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.ZipUtil;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

/**
 * 
 * @author Mark Italia
 * @since 04/12/2023
 * @version 1.0
 *
 */
@Named("backup")
@RequestScoped
public class ApplicationBackupBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 13564669584754L;
	
	private StreamedContent file;
	
	public static void main(String[] args) {
		ApplicationBackupBean s = new ApplicationBackupBean();
		s.zippingFile();
	}
	
    public StreamedContent getFile() {
    	
    	String fileName = GlobalVar.BACKUP_ZIP_FILE_NAME + ".zip";
        file = DefaultStreamedContent.builder()
                .name(fileName)
                .contentType("application/x-zip")
                .stream(() -> FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/backup/download/" + GlobalVar.BACKUP_ZIP_FILE_NAME + ".zip"))
                .build();
        return file;
    }
    
public StreamedContent getFileApk() {
    	
    	String fileName = GlobalVar.APP_RELEAE_FILE_NAME + ".apk";
        file = DefaultStreamedContent.builder()
                .name(fileName)
                .contentType("application/x-zip")
                .stream(() -> FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/apps/" + GlobalVar.APP_RELEAE_FILE_NAME + ".apk"))
                .build();
        return file;
    }
	
    
    
   public void zippingFile() {
	   if(fetchDataFromServer()) {
			ZipUtil.zipDirectory(GlobalVar.DOWNLOADED_DATA_FOLDER,GlobalVar.BACKUP_DATA_FOLDER, GlobalVar.BACKUP_ZIP_FILE_NAME);
			copyToAppFolder();
	   }else {
		   Application.addMessage(1, "Failed", "Unable to backup...");
	   }
   }
   
   private void copyToAppFolder() {
	   
	    File fileImg = new File(GlobalVar.BACKUP_DATA_FOLDER + GlobalVar.BACKUP_ZIP_FILE_NAME + ".zip");
		
	    ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		String contextImageLoc = "resources" + File.separator + "backup" + File.separator + "download" + File.separator;
		String pathToSave = externalContext.getRealPath("") + contextImageLoc;
		//System.out.println("Path zip: " + pathToSave);
		 try{
         	
 			Files.copy(fileImg.toPath(), (new File(pathToSave + fileImg.getName())).toPath(),
 			        StandardCopyOption.REPLACE_EXISTING);
 			//System.out.println("writing images....." + pathToSave);
 			}catch(IOException e){}
   }
   
   public void handleFileUpload(FileUploadEvent event) {
       
	    ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		String contextImageLoc = "resources" + File.separator + "backup" + File.separator + "upload" + File.separator;
		String pathToSave = externalContext.getRealPath("") + contextImageLoc;
		String fileName="data_backup.zip";
		File fileImg = new File(pathToSave+fileName);
	    try {
			boolean isSuccess=copyInputStreamToFile(event.getFile().getInputStream(), fileImg);
			//System.out.println("writing zip file....." + pathToSave);
			
			if(isSuccess) {
				boolean isCompleted = unzipFile(pathToSave+fileName, new File(externalContext.getRealPath("") + "resources" + File.separator + "backup" + File.separator + "upload"));
				if(isCompleted) {
					//ready for update database data
					updateDatabaseData();
					PrimeFaces pf = PrimeFaces.current();
					pf.executeScript("PF('dlgUpload').hide(1000)");
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
   }
   
   
   private static boolean copyInputStreamToFile(InputStream inputStream, File file) {
        int DEFAULT_BUFFER_SIZE = 8192;    
       try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
           int read;
           byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
           while ((read = inputStream.read(bytes)) != -1) {
               outputStream.write(bytes, 0, read);
           }
           
           Application.addMessage(1,"Success", "Successfully saved");
           return true;
       }catch(IOException eio) {
    	   eio.printStackTrace();
    	   Application.addMessage(3,"Error", "File was not successfully uploaded");
       }
       return false;
   }
   
   public  static boolean unzipFile(String zipFileLocation, File destinationUnzip) {
	   boolean isSuccess=false;
       try {
	       System.out.println("unzipping file: " + zipFileLocation);
    	   byte[] buffer = new byte[1024];
	       ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFileLocation));
	       ZipEntry zipEntry = zis.getNextEntry();
	       while (zipEntry != null) {
	    	   File newFile = newFile(destinationUnzip, zipEntry);
	    	    if (zipEntry.isDirectory()) {
	    	        if (!newFile.isDirectory() && !newFile.mkdirs()) {
	    	            throw new IOException("Failed to create directory " + newFile);
	    	        }
	    	    } else {
	    	        // fix for Windows-created archives
	    	        File parent = newFile.getParentFile();
	    	        if (!parent.isDirectory() && !parent.mkdirs()) {
	    	            throw new IOException("Failed to create directory " + parent);
	    	        }

	    	        // write file content
	    	        FileOutputStream fos = new FileOutputStream(newFile);
	    	        int len;
	    	        while ((len = zis.read(buffer)) > 0) {
	    	            fos.write(buffer, 0, len);
	    	        }
	    	        System.out.println("Successfully unzip file");
	    	        fos.close();
	    	        isSuccess=true;
	    	    }
	    	    zipEntry = zis.getNextEntry();
	       }

      
		zis.closeEntry();
		zis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isSuccess=false;
		}
       return isSuccess;
   }
   
   public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
	    File destFile = new File(destinationDir, zipEntry.getName());

	    String destDirPath = destinationDir.getCanonicalPath();
	    String destFilePath = destFile.getCanonicalPath();

	    if (!destFilePath.startsWith(destDirPath + File.separator)) {
	        throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
	    }

	    return destFile;
	}
   
	public boolean fetchDataFromServer() {
		String val = "";
		if(SyncData.checkingConnection()) {
			val += "connecting to server.....";
			SyncData.downloadDataFromServer();
			val += "\nCompleted dowloading data...";
			Application.addMessage(1, "Success", "Successfuly fetch data from server...");
			return true;
		}else {
			val += "Failed to connect to server...";
			Application.addMessage(1, "Failed", "Unable to connect to server...");
			return false;
		}
	}
	
	/**
	 * this is function use for updating data in database
	 */
	private static void updateDatabaseData() {
		
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		String contextImageLoc = "resources" + File.separator + "backup" + File.separator + "upload" + File.separator + "data_backup" + File.separator;
		String downloadPath = externalContext.getRealPath("") + contextImageLoc;
			
		File folder = new File(downloadPath);
		File[] listOfFiles = folder.listFiles();
		Conf conf = Conf.getInstance();
		String WEBTIS = conf.getDatabaseMain();
		String TAXATION = conf.getDatabaseLand();
		String CHEQUE = conf.getDatabaseBank();
		String CASHBOOK = conf.getDatabaseCashBook();
		
		boolean ispresentweb = false;
		boolean ispresenttax = false;
		boolean ispresentcheck = false;
		boolean ispresentcash = false;
		for (File file : listOfFiles) {
		    if (file.isFile()) {
		    	
		    	if(file.getName().equalsIgnoreCase(WEBTIS+".sql")) {
		    		ispresentweb = true;
		    		System.out.println(file.getName() + " is present...");
		    	}
		    	if(file.getName().equalsIgnoreCase(TAXATION+".sql")) {
		    		ispresenttax = true;
		    		System.out.println(file.getName() + " is present...");
		    	}
		    	if(file.getName().equalsIgnoreCase(CHEQUE+".sql")) {
		    		ispresentcheck = true;
		    		System.out.println(file.getName() + " is present...");
		    	}
		    	if(file.getName().equalsIgnoreCase(CASHBOOK+".sql")) {
		    		ispresentcash = true;
		    		System.out.println(file.getName() + " is present...");
		    	}
		    	
		    }
		} 
		
		if(ispresentweb && ispresenttax && ispresentcheck && ispresentcash) {
			System.out.println("updating data...");
			SyncData.loadData(downloadPath, "", false);
			System.out.println("successfully updating data...");
			Application.addMessage(1, "Success", "Data has been successfully updated");
		}else {
			System.out.println("file is not present... failed to update data...");
			Application.addMessage(1, "Error", "Data has been failed to update");
		}
	}
	
	private static boolean copyFileUsingStream(String source, String dest) {
		File fileOrg = new File(source);
		File fileCopy = new File(dest);
		return copyFileUsingStream(fileOrg, fileCopy);
	}
	
	private static boolean copyFileUsingStream(File source, File dest) {
		File file = new File(GlobalVar.COMPLETED_DATA_FOLDER);
		file.mkdir();
		System.out.println("copying file to new location....");
	    InputStream is = null;
	    OutputStream os = null;
	    boolean iscopied = false;
	    try {
		    try {
		        is = new FileInputStream(source);
		        os = new FileOutputStream(dest);
		        byte[] buffer = new byte[1024];
		        int length;
		        while ((length = is.read(buffer)) > 0) {
		            os.write(buffer, 0, length);
		        }
		    } finally {
		        is.close();
		        os.close();
		        iscopied = true;
		        System.out.println("successfully copied the file to new location....");
		    }
	    
	    }catch(Exception e) {
	    	e.printStackTrace();
	    }
	    
	    return iscopied;
	}
	
	private static boolean deletingFile(String fileCompletePathName) {
		File file = new File(fileCompletePathName);
		if(file.exists()) {
			file.delete();
			return true;
		}
		return false;
	}
}
