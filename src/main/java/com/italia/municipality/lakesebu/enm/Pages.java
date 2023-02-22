package com.italia.municipality.lakesebu.enm;

/**
 * 
 * @author mark italia
 * @since 09/2/2019
 * @version 1.0
 *
 */
public enum Pages {

	CollectionForm(1,"collectionform"),
	IssuedForm(2,"issuedform"),
	Market(3,"market"),
	Dtr(4,"dtr"),
	LandTax(5,"form56"),
	CheckWriting(6,"welcome"),
	CashInBank(7,"funds"),
	CashInTreasury(8,"funds2"),
	VoucherLoging(9,"voucher"),
	Mooe(10,"moereport"),
	Graph(11,"graphgen"),
	StockRecording(12,"stocks"),
	CollectorRecording(13,"logform"),
	ReportGraph(14,"reportsgraph"),
	Orlisting(15,"orlisting"),
	UploadRcd(16,"uploadrcd"),
	VoucherExpense(17,"vr"),
	WaterBilling(18,"waterbill"),
	LandTaxCertification(19,"history");
	
	
	private int id;
	private String name;
	
	public static String moduleName(int id){
		
		for(Pages m : Pages.values()){
			if(id==m.getId()){
				return m.getName();
			}
		}
		return Pages.CollectionForm.getName();
	}
	
	public static int moduleId(String name){
		
		for(Pages m : Pages.values()){
			if(name.equalsIgnoreCase(m.getName())){
				return m.getId();
			}
		}
		return Pages.CollectionForm.getId();
	}
	
	public static Pages selected(int id){
		for(Pages m : Pages.values()){
			if(id==m.getId()){
				return m;
			}
		}
		return Pages.CollectionForm;
	}
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	private Pages(int id, String name){
		this.id = id;
		this.name = name;
	}
}
