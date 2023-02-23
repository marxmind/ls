package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.event.CellEditEvent;

import com.italia.municipality.lakesebu.controller.Barangay;
import com.italia.municipality.lakesebu.controller.ILandType;
import com.italia.municipality.lakesebu.controller.ITaxPayor;
import com.italia.municipality.lakesebu.controller.LandPayor;
import com.italia.municipality.lakesebu.controller.LandType;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.controller.TaxPayor;
import com.italia.municipality.lakesebu.enm.AppConf;

import jakarta.annotation.PostConstruct;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;

/**
 * 
 * @author mark
 * @since 11/17/2016
 * @version 1.0
 *
 */
@Named
@ViewScoped
@Data
public class PayorBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6546486797095481L;

	private List barangayList;
	private int barangayId;
	private String searchParam;
	private List<ITaxPayor> payors;
	private ITaxPayor payorTableData;
	private List barangayListSearch;
	private int barangayIdSearch;
	
	private List<LandPayor> lands;
	private LandPayor landTableData;
	private List barangayListLand;
	private int barangayIdLand;
	
	private List landTypes;
	private int landTypeId;
	private ITaxPayor tmpPayorData;
	private List landStatus;
	private int landStatusId;
	
	private int imrovedId;
	private List improves;
	
	@PostConstruct
	public void init(){
		
		landStatus = new ArrayList<>();
		landStatus.add(new SelectItem(1, "ACTIVE"));
		landStatus.add(new SelectItem(2, "RETIRED"));
		
		barangayListSearch = new ArrayList<>();
		for(Barangay bar : Barangay.retrieve("SELECT * FROM barangay", new String[0])){
			barangayListSearch.add(new SelectItem(bar.getId(), bar.getName()));
		}
		
		barangayListLand = new ArrayList<>();
		for(Barangay bar : Barangay.retrieve("SELECT * FROM barangay", new String[0])){
			barangayListLand.add(new SelectItem(bar.getId(), bar.getName()));
		}
		
		landTypes = new ArrayList<>();
		for(ILandType land : LandType.retrieve("SELECT * FROM landtype", new String[0])){
			landTypes.add(new SelectItem(land.getId(), land.getLandType()));
		}
		
		improves = new ArrayList<>();
		improves.add(new SelectItem(0, "Origin"));
		improves.add(new SelectItem(1, "Improvement"));
		
		String dbTax = ReadConfig.value(AppConf.DB_NAME_TAX);
		String dbWeb = ReadConfig.value(AppConf.DB_NAME_WEBTIS);
		String sql = "SELECT * FROM "+ dbTax +".taxpayor p, "+ dbTax +".barangay b, "+ dbWeb +".userdtls u  WHERE p.payisactive=1 AND p.bgid = b.bgid AND p.userdtlsid = u.userdtlsid order by p.payorid desc limit 10";
		String[] params = new String[0];
		
		
		/**
		 * check if contain td no
		 */
		if(getSearchParam()!=null && getSearchParam().contains("-")){
			String sqlTd = "SELECT * FROM  " + dbTax + ".payorland l, "+ dbTax +".landtype t, "+ 
		dbTax +".taxpayor p, "+ dbTax +".barangay b, "+ dbWeb +".userdtls u  "
				+ "WHERE l.payorid = p.payorid AND l.landid = t.landid AND l.isactiveland=1 AND "
				+ "l.bgid = b.bgid AND l.userdtlsid = u.userdtlsid AND (l.landtd=? OR l.lotno=?) AND l.landstatus=1";
			params = new String[2];
			params[0] = getSearchParam();
			params[1] = getSearchParam();
			List<LandPayor> pays = LandPayor.retrieve(sqlTd, params);
			if(pays.size()>0){
				setSearchParam(pays.get(0).getPayor().getFullName());
			}
			params = new String[0];
		}
		
		/**
		 * both has a value
		 */
		if((getSearchParam()!=null && !getSearchParam().isEmpty()) && getBarangayIdSearch()!=0){
			params = new String[1];
			
			sql = "SELECT * FROM "+ dbTax +".taxpayor p, "+ dbTax +".barangay b, "+ dbWeb +".userdtls u  "
					+ "WHERE p.payisactive=1 AND p.bgid = b.bgid AND p.userdtlsid = u.userdtlsid AND p.payorname like '%" + getSearchParam().replaceAll("--", "") + "%' AND p.bgid=? "
					+ "order by p.payorid desc limit 100";
			params[0] = getBarangayIdSearch()+"";
			
		/**
		 * only input text has a value	
		 */
		}else if((getSearchParam()!=null && !getSearchParam().isEmpty()) && getBarangayIdSearch()==0){
		
			sql = "SELECT * FROM "+ dbTax +".taxpayor p, "+ dbTax +".barangay b, "+ dbWeb +".userdtls u  "
					+ "WHERE p.payisactive=1 AND p.bgid = b.bgid AND p.userdtlsid = u.userdtlsid AND p.payorname like '%" + getSearchParam().replaceAll("--", "") + "%'  "
					+ "order by p.payorid desc limit 100";
			
		/**
		 * Only barangay has a value	
		 */
		}else if((getSearchParam()==null || getSearchParam().isEmpty()) && getBarangayIdSearch()!=0){
			params = new String[1];
			sql = "SELECT * FROM "+ dbTax +".taxpayor p, "+ dbTax +".barangay b, "+ dbWeb +".userdtls u  "
					+ "WHERE p.payisactive=1 AND p.bgid = b.bgid AND p.userdtlsid = u.userdtlsid AND  p.bgid=? "
					+ "order by p.payorid desc limit 100";
			params[0] = getBarangayIdSearch()+"";
		}
		
		payors = new ArrayList<ITaxPayor>();
		payors = TaxPayor.retrieve(sql, params);
		//Collections.reverse(payors);
	}
	
	 public void onCellEdit(CellEditEvent event) {
		 try{
	        Object oldValue = event.getOldValue();
	        Object newValue = event.getNewValue();
	        
	        System.out.println("old Value: " + oldValue);
	        System.out.println("new Value: " + newValue);
	        
	        int index = event.getRowIndex();
	        
	        if(getBarangayId()!=0){
	        	String[] params = new String[1];
	        	params[0] = getBarangayId()+"";
		        Barangay barangay = Barangay.retrieve("SELECT * FROM barangay WHERE bgid=?", params).get(0);
		        payors.get(index).setBarangay(barangay);
		        setBarangayId(0);
	        }
	        
	        //saving after edit
	        try{
	        	saveCellEdit(payors.get(index));
	        }catch(Exception e){}
	        
		 }catch(Exception e){}  
	 }       
	
	 public void onCellEditLand(CellEditEvent event) {
		 try{
	        Object oldValue = event.getOldValue();
	        Object newValue = event.getNewValue();
	        
	        System.out.println("old Value: " + oldValue);
	        System.out.println("new Value: " + newValue);
	        
	        int index = event.getRowIndex();
	        System.out.println("index : " + index);
	        String[] params = new String[1];
	        if(getBarangayIdLand()!=0){
	        	params[0] = getBarangayIdLand()+"";
		        Barangay barangay = Barangay.retrieve("SELECT * FROM barangay WHERE bgid=?", params).get(0);
		        lands.get(index).setBarangay(barangay);
		        setBarangayId(0);
	        }
	        if(getLandTypeId()!=0){
		        params[0] = getLandTypeId()+"";
		        ILandType land = LandType.retrieve("SELECT * FROM landtype WHERE landid=?", params).get(0);
				lands.get(index).setLandType(land);
				setLandTypeId(0);
	        }
	        
	        if(getLandStatusId()!=0){
	        	lands.get(index).setStatus(getLandStatusId());
	        	setLandStatusId(0);
	        }
	        
	        if(getImrovedId()!=lands.get(index).getIsImprovment()) {
	        	lands.get(index).setIsImprovment(getImrovedId());
	        	setImrovedId(0);
	        }
	        
	        //saving after edit
	        try{
	        	saveCellEditLand(lands.get(index));
	        }catch(Exception e){}
	        
		 }catch(Exception e){}  
	 }    
	 
	 public void addNew(){
			System.out.println("add new");
			ITaxPayor pay = new TaxPayor();
			if(payors!=null && payors.size()>0){
				System.out.println("added create default value");
				pay = createDefaultPayor();
				pay = TaxPayor.save(pay);
				//payors.add(pay);
			}else{
				System.out.println("create new default value");
				payors = new ArrayList<>();
				pay = createDefaultPayor();
				pay = TaxPayor.save(pay);
				//payors.add(pay);
			}
			setSearchParam(null);
			setBarangayIdSearch(0);
			init();
		} 
	 
	 public void copyPasteCell(){
			if(getPayorTableData()!=null){
				ITaxPayor payor = new TaxPayor();
				payor.setFullName(getPayorTableData().getFullName());
				payor.setAddress(getPayorTableData().getAddress());
				payor.setBarangay(getPayorTableData().getBarangay());
				payor.setUserDtls(Login.getUserLogin().getUserDtls());
				payor.setIsactive(1);
				payor.save();
				setSearchParam(null);
				setBarangayIdSearch(0);
				init();
			}
		} 
	 
	private ITaxPayor createDefaultPayor(){
		ITaxPayor payor = new TaxPayor();
		payor.setFullName("Last Name, First Name");
		payor.setAddress("Poblacion, Lake Sebu, South Cotabato");
		Barangay barangay = new Barangay();
		barangay.setId(1);
		barangay.setName("Poblacion");
		payor.setBarangay(barangay);
		payor.setUserDtls(Login.getUserLogin().getUserDtls());
		payor.setIsactive(1);
		return payor;
	}
	
	public void deleteCell(){
		if(getPayorTableData()!=null){
			getPayorTableData().delete();
			setPayorTableData(null);
			init();
		}
	}
	
	public void saveCellEdit(ITaxPayor pay){
		setPayorTableData(pay);
		save();
	}
	
	public void save(){
		if(getPayorTableData()!=null){
			ITaxPayor payor = getPayorTableData();
			payor.setUserDtls(Login.getUserLogin().getUserDtls());
			payor.save();
			setPayorTableData(null);
			init();
		}
	}
	
	public List getBarangayList() {
		barangayList = new ArrayList<>();
		for(Barangay bar : Barangay.retrieve("SELECT * FROM barangay", new String[0])){
			barangayList.add(new SelectItem(bar.getId(), bar.getName()));
		}
		
		return barangayList;
	}
	
	public void openLand(ITaxPayor pay){
		if(pay!=null){
			System.out.println("openLand not null");
		}else{
			System.out.println("openLand null");
		}
		//setPayorTableData(pay);
		setTmpPayorData(pay);
		payorLandClick();
	}
	
	public void payorLandClick(){
		lands = new ArrayList<LandPayor>();
		if(getTmpPayorData()!=null){
			System.out.println("getPayorTableData() is not null");
			String dbTax = ReadConfig.value(AppConf.DB_NAME_TAX);
			String dbWeb = ReadConfig.value(AppConf.DB_NAME_WEBTIS);
			String sql = "SELECT * FROM  " + dbTax + ".payorland l, "+ dbTax +".landtype t, "+ dbTax +".taxpayor p, "+ dbTax +".barangay b, "+ dbWeb +".userdtls u  WHERE l.payorid = p.payorid AND l.landid = t.landid AND l.isactiveland=1 AND l.bgid = b.bgid AND l.userdtlsid = u.userdtlsid AND l.payorid=? ORDER BY l.payorlandid DESC";
			String[] params = new String[1];
			params[0] = getTmpPayorData().getId()+"";
			lands = LandPayor.retrieve(sql, params);
			System.out.println("getPayorTableData() SQL: "+ sql);
			
		}
	}
	
	public void addNewLand(){
		if(getTmpPayorData()!=null){
			
		System.out.println("Add new " + getTmpPayorData().getFullName());	
			
		LandPayor pay = new LandPayor();
		pay.setTaxDeclarionNo("TD NO");
		pay.setLotNo("LOT NO");
		pay.setAddress(getTmpPayorData().getBarangay().getName() + ", Lake Sebu, South Cotabato");
		pay.setRemarks("N/A");
		pay.setLandValue(0.00);
		pay.setIsActive(1);
		pay.setStatus(1);
		pay.setBarangay(getTmpPayorData().getBarangay());
		pay.setPayor(getTmpPayorData());
		pay.setIsImprovment(getImrovedId());
		
		ILandType land = new LandType();
		land.setId(1);
		land.setLandType("Agricultural");
		pay.setLandType(land);
		pay.setUserDtls(Login.getUserLogin().getUserDtls());
		pay.save();
		payorLandClick();
		}
	}
	
	public void copyPasteCellLand(){
		if(getLandTableData()!=null){
			
			System.out.println("copy new " + getTmpPayorData().getFullName());
			
			LandPayor pay = new LandPayor();
			pay.setTaxDeclarionNo(getLandTableData().getTaxDeclarionNo());
			pay.setLotNo(getLandTableData().getLotNo());
			pay.setAddress(getLandTableData().getAddress());
			pay.setRemarks(getLandTableData().getRemarks());
			pay.setLandValue(getLandTableData().getLandValue());
			pay.setIsActive(1);
			pay.setStatus(1);
			pay.setBarangay(getLandTableData().getBarangay());
			pay.setPayor(getTmpPayorData());
			pay.setLandType(getLandTableData().getLandType());
			pay.setUserDtls(Login.getUserLogin().getUserDtls());
			pay.setIsImprovment(getLandTableData().getIsImprovment());
			pay.save();
			setLandTableData(null);
			payorLandClick();
		}
	}
	
	public void saveCellEditLand(LandPayor pay){
		setLandTableData(pay);
		saveLand();
	}
	
	public void saveLand(){
		if(getLandTableData()!=null){
			System.out.println("saving land....");
			LandPayor pay = getLandTableData();
			pay.setUserDtls(Login.getUserLogin().getUserDtls());
			pay.save();
			setLandTableData(null);
			payorLandClick();
		}
	}
	
	public void deleteCellLand(){
		if(getLandTableData()!=null){
			getLandTableData().delete();
			setLandTableData(null);
			payorLandClick();
		}
	}
}
