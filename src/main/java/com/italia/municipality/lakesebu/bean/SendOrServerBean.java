package com.italia.municipality.lakesebu.bean;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.PrimeFaces;
import com.italia.municipality.lakesebu.controller.Reports;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.OrlistingXML;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named
@ViewScoped
public class SendOrServerBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 647657687453441L;
	
	@Setter @Getter private List<Reports> data;
	
	public void loadFiles() {
		File folder = new File(GlobalVar.COMMIT_XML);
		File[] listOfFiles = folder.listFiles();
		data = new ArrayList<>();
		for (File file : listOfFiles) {
			if(file.isFile()) {
				Reports r = new Reports();
				r.setF1(file.getName().replace(".xml", ""	));
				data.add(r);
			}
		}
		
		PrimeFaces pf = PrimeFaces.current();
		pf.executeInitScript("PF('dlgSendOr').show()");
	}
	
	public void upload() {
		boolean isprocessed = false;
		if(OrlistingXML.checkingConnection()) {
			OrlistingXML.retrieveXMLforServerSaving();
			loadFiles();
			OrlistingXML.activateSession(false);
			isprocessed = true;
		}else {
			System.out.println("Server is down...");
			Application.addMessage(1, "Connection Failure", "Server is not accessible");
		}
		if(isprocessed && (data==null || data.size()==0)) {
			PrimeFaces pf = PrimeFaces.current();
			pf.executeInitScript("PF('dlgSendOr').hide()");
			Application.addMessage(1, "Success", "Data has successfully sync to server");
		}
	}

}
