package com.italia.municipality.lakesebu.controller;


import com.italia.municipality.lakesebu.enm.ClientTransactionType;
import com.italia.municipality.lakesebu.licensing.controller.Words;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class LSPortalScanme {
	
	private String transaction;
	private String firstName;
	private String middleName;
	private String lastName;
	private String fullName;
	private String currentAddress;
	private String birthday;
	private String birthPlace;
	private String gender;
	private String weight;
	private String height;
	private String work;
	private String citizenship;
	private String salary;
	private String civilStatus;
	
	public static LSPortalScanme convertData(String val) {
		LSPortalScanme ls = new LSPortalScanme();
		
		try {
		String[] data = val.split(":");
		int index=1;//zero is reserve for portal
		ls.setTransaction(data[index++]);
		ls.setFirstName(data[index++]);
		ls.setMiddleName(data[index++]);
		ls.setLastName(data[index++]);
		ls.setCurrentAddress(data[index++]);
		ls.setBirthday(data[index++]);
		ls.setBirthPlace(data[index++]);
		ls.setGender(data[index++]);
		ls.setWeight(data[index++]);
		ls.setHeight(data[index++]);
		ls.setWork(data[index++]);
		ls.setCitizenship(data[index++]);
		ls.setSalary(data[index++]);
		ls.setFullName(ls.getLastName()+", " + ls.getFirstName() + " " + ls.getMiddleName());
		
		//update profession
		String idProf=mapProfession(ls);
		ls.setWork(idProf);
		
		}catch(Exception e) { return null;}
		return ls;
	}
	
	private static String mapProfession(LSPortalScanme ls) {
		String idProf="25";//others or none
		String[] types = Words.getTagName("profession-list").split(",");
		for(String prof : types) {
			String[] vals = prof.split("-");
			if(ls.getWork().equalsIgnoreCase(vals[1])) {
				idProf=vals[0];
			}	
		}
		return idProf;
	}
	
	public static Client convertToClient(LSPortalScanme ls) {
		Client client = new Client();
		
		client.setTransType(ClientTransactionType.idName(ls.getTransaction()));
		client.setFirstName(ls.getFirstName());
		client.setMiddleName(ls.getMiddleName());
		client.setLastName(ls.getLastName());
		client.setGender(ls.getGender().equalsIgnoreCase("MALE")? 1 : 2);
		client.setBirthday(ls.getBirthday());
		client.setBirthPlace(ls.getBirthPlace());
		client.setAddress(ls.getCurrentAddress());
		client.setCivilStatus(1);
		client.setWeight(ls.getWeight());
		client.setHeight(ls.getHeight());
		client.setProfession(mapProfession(ls));
		client.setNationality(ls.getCitizenship());
		try{client.setMonthlySalary(Double.valueOf(ls.getSalary().replace(",", "")));}catch(Exception e){client.setMonthlySalary(0);}
		
		return client;
	}
	
}
