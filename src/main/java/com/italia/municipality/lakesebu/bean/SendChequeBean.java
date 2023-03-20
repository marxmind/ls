package com.italia.municipality.lakesebu.bean;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import com.italia.municipality.lakesebu.controller.ChequeXML;
import com.italia.municipality.lakesebu.controller.Reports;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.utils.Application;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;

@Named("sendchk")
@ViewScoped
@Data
public class SendChequeBean implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 433564647574561L;
	private List<Reports> data;
	
	public void loadFiles() {
		File folder = new File(GlobalVar.CHECK_XML);
		File[] listOfFiles = folder.listFiles();
		data = new ArrayList<>();
		for (File file : listOfFiles) {
			if(file.isFile()) {
				Reports r = new Reports();
				r.setF1(file.getName().replace(".xml", ""));
				data.add(r);
			}
		}
		
		PrimeFaces pf = PrimeFaces.current();
		pf.executeInitScript("PF('dlgCheques').show()");
	}
	
	public void upload() {
		boolean isprocessed = false;
		if(ChequeXML.checkingConnection()) {
			ChequeXML.retrieveXMLforServerSaving();
			loadFiles();
			ChequeXML.activateSession(false);
			isprocessed = true;
		}else {
			System.out.println("Server is down...");
			Application.addMessage(1, "Connection Failure", "Server is not accessible");
		}
		if(isprocessed && (data==null || data.size()==0)) {
			PrimeFaces pf = PrimeFaces.current();
			pf.executeInitScript("PF('dlgCheques').hide()");
			Application.addMessage(1, "Success", "Data has successfully sync to server");
		}
	}

}
