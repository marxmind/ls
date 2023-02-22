package com.italia.municipality.lakesebu.enm;

/**
 * 
 * @author Mark Italia
 * @since 08-11-2018
 * @version 1.0
 *
 */
public enum FormType {
	
	CTC_INDIVIDUAL(1, "C.T.C","Form 0016"),
	CTC_CORPORATION(2, "C & C1","Form 0017"),
	AF_51(3, "AF-51","Form 51"),
	AF_52(4, "AF-52","Form 52"),
	AF_53(5, "AF-53","Form 53"),
	AF_54(6, "AF-54","Form 54"),
	AF_56(7, "AF-56","Form 56"),
	AF_56_I(8, "AF-56(I)","Form 56(I)"),
	CT_2(9,"CT 2.00","Form 55(A) Cash Ticket 2"),
	CT_5(10,"CT 5.00","Form 55(A) Cash Ticket 5"),
	AF_51_C(11, "AF-51 C","Form 51-C"),
	AF_57(12,"AF-57","Form 57"),
	AF_58(13,"AF-58","Form 58"),
	AI(14,"AI","Auxiliary Invoice"),
	BP(15,"BP","Bike Plate"),
	DID(16,"DID","Drivers I.D."),
	DRSAG(17,"D.R. SAG","Delivery Receipt of SAG"),
	DVS(18,"DVS","Delivery Van Sticker"),
	GF103(19,"GF 103","Collectors Cash Book"),
	GF106(20,"GF 106","Dishonored Check"),
	HC(21,"H.C.","Health Cert. None/Food Handler"),
	MS(22,"M.S.","Motorela Sticker"),
	PCP(23,"P.C.P.","Push Cart Plate"),
	PPT(24,"P.P.T.","Pay Parking Ticket"),
	PUVS(25,"P.U.V.S","Public Utility Vehicle Sticker"),
	RTA(26,"RTA","Road & Traffic Administration"),
	TCT(27,"T.C.T","Traffic Citation Ticket"),
	TWM(28,"T.W.M.","Tags of Weight and Measure");
	
	private int id;
	private String name;
	private String description;
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	private FormType(int id, String name,String description){
		this.id = id;
		this.name = name;
		this.description = description;	
	}
	
	public static String nameId(int id){
		
		for(FormType type : FormType.values()){
			if(id==type.getId()){
				return type.getName();
			}
		}
		
		return FormType.CTC_INDIVIDUAL.getName();
	}
	
	public static String nameDescriptionId(int id){
		
		for(FormType type : FormType.values()){
			if(id==type.getId()){
				return type.getDescription();
			}
		}
		
		return FormType.CTC_INDIVIDUAL.getDescription();
	}
	
	public static int idName(String name){
		
		for(FormType type : FormType.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type.getId();
			}
		}
		
		return FormType.CTC_INDIVIDUAL.getId();
	}
	
	public static FormType val(String name) {
		
		for(FormType type : FormType.values()){
			if(name.equalsIgnoreCase(type.getName())){
				return type;
			}
		}
		
		return null;
	}
	
	public static FormType val(int id) {
		
		for(FormType type : FormType.values()){
			if(id==type.getId()){
				return type;
			}
		}
		
		return null;
	}
	
}
