package com.italia.municipality.lakesebu.bean;

import java.io.File;
import java.io.Serializable;
import com.italia.municipality.lakesebu.controller.Reports;
import com.italia.municipality.lakesebu.controller.SyncData;
import com.italia.municipality.lakesebu.database.Conf;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.OrlistingXML;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named
@ViewScoped
public class SyncBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3245658451L;
	@Setter @Getter private String notify;
	@Setter @Getter private boolean success;
	
	@Deprecated
	public void syncDataFromServer() {
		File folder = new File(GlobalVar.COMMIT_XML);
		File[] listOfFiles = folder.listFiles();
		
		if(listOfFiles!=null && listOfFiles.length>0) {
			boolean hasFiles = false;
			for(File f : listOfFiles) {
				if(f.isFile()) {
					hasFiles = true;
				}
			}
			if(hasFiles) {
				//upload local data and get server data
				loadUploadFiles();
			}
			
		}else {
			//get server data
			fetchDataFromServer();
		}
	}
	@Deprecated
	public void loadUploadFiles() {
		File folder = new File(GlobalVar.COMMIT_XML);
		File[] listOfFiles = folder.listFiles();
		boolean hasFile = false;
		for (File file : listOfFiles) {
			if(file.isFile()) {
				Reports r = new Reports();
				r.setF1(file.getName().replace(".xml", ""	));
				hasFile = true;
			}
		}
		if(hasFile) {
			
				boolean isprocessed = false;
				if(OrlistingXML.checkingConnection()) {
					OrlistingXML.retrieveXMLforServerSaving();
					OrlistingXML.activateSession(false);
					isprocessed = true;
				}else {
					System.out.println("Server is down...");
					Application.addMessage(1, "Connection Failure", "Server is not accessible");
				}
				if(isprocessed) {
					fetchDataFromServer();//get data from server
					Application.addMessage(1, "Success", "Data has successfully sync to server");
				}
				hasFile = false;
		}
	}
	@Deprecated
	public void fetchDataFromServer() {
		String val = "";
		if(SyncData.checkingConnection()) {
			val += "connecting to server.....";
			SyncData.downloadDataFromServer();
			val += "\nCompleted dowloading data...";
			Application.addMessage(1, "Success", "Successfuly fetch data from server...");
			setSuccess(true);
		}else {
			val += "Failed to connect to server...";
			Application.addMessage(1, "Failed", "Unable to connect to server...");
			setSuccess(false);
		}
		setNotify(val);
		
	}
	
	public void uploadData() {
		
		File folder = new File(GlobalVar.DOWNLOADED_DATA_FOLDER);
		File[] listOfFiles = folder.listFiles();
		Conf conf = Conf.getInstance();
		String WEBTIS = conf.getDatabaseMain();
		String TAXATION = conf.getDatabaseLand();
		String CHEQUE = conf.getDatabaseBank();
		String CASHBOOK = conf.getDatabaseCashBook();
		StringBuilder sb = new StringBuilder();
		boolean ispresentweb = false;
		boolean ispresenttax = false;
		boolean ispresentcheck = false;
		boolean ispresentcash = false;
		for (File file : listOfFiles) {
		    if (file.isFile()) {
		    	
		    	if(file.getName().equalsIgnoreCase(WEBTIS+".sql")) {
		    		ispresentweb = true;
		    		//System.out.println(file.getName() + " is present...");
		    		sb.append(file.getName() + " is present.....\n");
		    	}
		    	if(file.getName().equalsIgnoreCase(TAXATION+".sql")) {
		    		ispresenttax = true;
		    		//System.out.println(file.getName() + " is present...");
		    		sb.append(file.getName() + " is present.....\n");
		    	}
		    	if(file.getName().equalsIgnoreCase(CHEQUE+".sql")) {
		    		ispresentcheck = true;
		    		//System.out.println(file.getName() + " is present...");
		    		sb.append(file.getName() + " is present.....\n");
		    	}
		    	if(file.getName().equalsIgnoreCase(CASHBOOK+".sql")) {
		    		ispresentcash = true;
		    		//System.out.println(file.getName() + " is present...");
		    		sb.append(file.getName() + " is present.....\n");
		    	}
		    	
		    }
		} 
		
		if(ispresentweb && ispresenttax && ispresentcheck && ispresentcash) {
			System.out.println("updating data...");
			SyncData.loadServerData();
			System.out.println("successfully updating data...");
			Application.addMessage(1, "Success", "Data has been successfully updated");
			sb.append("updated successfully.....");
			setSuccess(false);
		}else {
			System.out.println("file is not present... failed to update data...");
			Application.addMessage(1, "Error", "Data has been failed to update");
			sb.append("failure to update.....\nreason files are not present or not yet fetch from server...\nplease click Fetch Data From Server.");
			setSuccess(true);
		}
		setNotify(sb.toString());
		
	}
	

}
