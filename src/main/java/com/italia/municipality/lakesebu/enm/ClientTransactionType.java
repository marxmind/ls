package com.italia.municipality.lakesebu.enm;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 09/27/2022
 *
 */
public enum ClientTransactionType {
	
	CEDULA(0, "Individual Cedula"),
	BUSINESS_NEW(1, "New Business"),
	BUSINESS_RENEWAL(2, "Business Renewal"),
	QUARTERLY_BUSINESS_PAYMENT(3, "Business Quarterly Payment"),
	FISHCAGE_NEW(4, "Fish Cage Water Rental (New)"),
	FISHCAGE_RENEW(5, "Fish Cage Renewal"),
	TRANSPORT(6, "Transport Product"),
	TRAFFICVIOLATION_MAJOR(7, "Major Traffic Violation"),
	POLICE_CLEARANCE_LOCAL(8, "Police Clearance Local"),
	POLICE_CLEARANCE_ABROAD(9, "Police Clearance Abroad"),
	SWINE_TRANSPORT(10, "Swine Transportation"),
	COW_TRANSPORT(11, "Cow Transportation"),
	CARABAO_TRANSPORT(12, "Carabao Transportation"),
	OTHER_ANIMAL_TRANSPORT(13, "Other Animal Transportation"),
	BAMBOO_TRANSPORT(14, "Bamboo Transport"),
	RATTAN_TRANSPORT(15, "Rattan Transportation"),
	WOOD_TRANSPORT(16, "Wood Transportation"),
	LUMBER_TRANSPORT(17, "Lumber Transportation"),
	ABACA_TRANSPORT(18, "Lumber Transportation"),
	FURNITURE_TRANSPORT(19, "Furniture Transportation"),
	POULTRY_TRANSPORT(20, "Poultry Product Transportation"),
	OUTDOOR_ACTIVITIES(21, "Outdoor Activities"),
	RETIREMENT_BUSINESS(22, "Request for Retirement of Business"),
	LAND_TAX_CLEARANCE(23, "Land Tax Clearance"),
	OTHER_DOCUMENTS(24, "Other Documents Purposes"),
	TRAFFICVIOLATION_MINOR(25, "Minor Traffic Violation"),
	
	BIRTH_REG(26,"Birth Registration"),
	BIRTH_CERTIFIED_TRUE_COPY(27, "Certified True Copy of Live Birth"),
	BIRTH_LIVE_CERTIFICATION(28,"Certification Fee of Live Birth"),
	BIRTH_MACHINE_FORM1A(29,"Machine Copy - Live Birth Form 1A "),
	BIRTH_FILING_FEE_9255(30,"R.A. 9255 Live Birth"),
	BIRTH_CERTIFICATION(31,"Birth Certification"),
	BIRTH_MACHINE_COPY_LIVE(32,"Machine Copy (Live Birth)"),
	BIRTH_DELAYED_REG(33,"Delayed Registration of Birth (11-30 years)"),
	BIRTH_MACHINE_COPY(34,"Machine Copy of Birth"),
	
	DEATH_CERTIFICATE(35,"Death Certification"),
	DEATH_MACHINE(36, "Machine Copy of Death"),
	DEATH_DELAYED_REG(37,"Delayed Registration of Death"),
	
	CADAVER_TRANSFER(38, "Transfer of Cadaver"),
	CADAVER_EXHUMATION(39,"Exhumation of Cadaver"),
	
	MARIAGE_REGISTRATION(40, "Registration Fee (Marriage)"),
	MARRIAGE_APPLICATION(41, "Application for Marriage License"),
	MARRIAGE_CERTIFIED_MACHINE_COPY(42, "Machine Copy (Marriage)"),
	MARRIAGE_TRUE_COPY(43,"True Copy (Marriage)"),
	MARRIAGE_NON_RESIDENT_APPLICATION(44,"Non-Resident (Marriage)"),
	MARRIAGE_FORIEGNER_APPLICATION(45,"Foreigner (Marriage)"),
	MARRIAGE_LICENSSE(46,"License Fee (Marriage)"),
	MARRIAGE_PRE_COUNSELING(47,"Pre-Marriage Counseling"),
	
	COE(48,"COE Certificate of Employment"),
	
	TAX_DECLARATION(49, "Tax Declaration"),
	
	OTHER(50, "Others");
	
	private int id;
	private String name;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private ClientTransactionType(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	public static String nameId(int id){
		
		for(ClientTransactionType type : ClientTransactionType.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		
		return ClientTransactionType.CEDULA.getName();
	}
	
	public static int idName(String name){
		
		for(ClientTransactionType type : ClientTransactionType.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		
		return ClientTransactionType.CEDULA.getId();
	}
	
}
