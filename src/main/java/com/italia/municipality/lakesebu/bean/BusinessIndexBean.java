package com.italia.municipality.lakesebu.bean;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import com.italia.municipality.lakesebu.controller.BusinessIndex;
import com.italia.municipality.lakesebu.controller.BusinessIndexTrans;
import com.italia.municipality.lakesebu.controller.BusinessMapping;
import com.italia.municipality.lakesebu.controller.Livelihood;
import com.italia.municipality.lakesebu.controller.ORNameList;
import com.italia.municipality.lakesebu.enm.BusinessCategory;
import com.italia.municipality.lakesebu.enm.BusinessQtrType;
import com.italia.municipality.lakesebu.enm.BusinessType;
import com.italia.municipality.lakesebu.global.GlobalVar;
import com.italia.municipality.lakesebu.licensing.controller.Barangay;
import com.italia.municipality.lakesebu.licensing.controller.BusinessCustomer;
import com.italia.municipality.lakesebu.licensing.controller.DocumentFormatter;
import com.italia.municipality.lakesebu.licensing.controller.Municipality;
import com.italia.municipality.lakesebu.licensing.controller.Province;
import com.italia.municipality.lakesebu.licensing.controller.Regional;
import com.italia.municipality.lakesebu.licensing.controller.Words;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.CheckInternetConnection;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.SendSMS;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;
@Named("bnIndex")
@ViewScoped
@Data
public class BusinessIndexBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private BusinessIndex businessIndex;
	private List status;
	private String searchParam;
	private List<BusinessIndex> business;
	private List<BusinessIndexTrans> trans;
	private BusinessIndexTrans tranData;
	private String orData;
	private List quarters;
	
	private List barangay;
	private List municipal;
	private List provincial;
	private List regional;
	
	private int searchTypeId;
	private List searchTypes;
	
	private int year;
	private List years;
	private int qtrId;
	private List quarterSearch;
	private List<BusinessIndexTrans> businesTrans;
	private Date dateFrom;
	private Date dateTo;
	private boolean includeDate;
	private String searchName;
	private List typesOf;
	
	private List bars1;
	private int barId1;
	private List bars2;
	private int barId2;
	private Map<Integer, Barangay> mapBarangayData;
	private int typeOfSearchId;
	private List typeOfSearch;
	private List<BusinessMapping> maps;
	private String searchBusinessMap;
	private BusinessMapping selectedMapData;
	
	//mapping
	private MapModel allBusiness;
	private String centerMap;
	private Marker marker;
	private Map<String, BusinessMapping> mapBiz;
	
	private List essentials;
	private List categories;
	
	private int mappingId;
	private List mappings;
	
	@PostConstruct
	public void init() {
		
		mappingId = 0; //default
		mappings = new ArrayList<>();
		mappings.add(new SelectItem(0, "All"));
		mappings.add(new SelectItem(1, "Mapping Area"));
		mappings.add(new SelectItem(2, "No Mapping Area"));
		
		typeOfSearchId = 3;//All
		typesOf = new ArrayList<>();
		typeOfSearch = new ArrayList<>();
		typeOfSearch.add(new SelectItem(3,"All"));
		for(BusinessType type : BusinessType.values()){
			typesOf.add(new SelectItem(type.getId(),type.getName()));
			typeOfSearch.add(new SelectItem(type.getId(),type.getName()));
		}
		
		essentials = new ArrayList<>();
		essentials.add(new SelectItem(0, "Non-Essential"));
		essentials.add(new SelectItem(1, "Essential"));
		
		categories = new ArrayList<>();
		for(BusinessCategory cat : BusinessCategory.values()) {
			categories.add(new SelectItem(cat.getId(), cat.getName()));
		}
		
		mapBarangayData = new LinkedHashMap<Integer, Barangay>();
		barangay = new ArrayList<>();
		bars1 = new ArrayList<>();
		bars2 = new ArrayList<>();
		barId1=0;
		barId2=0;
		municipal = new ArrayList<>();
		provincial = new ArrayList<>();
		regional = new ArrayList<>();
		
		for(Regional rg : Regional.retrieve("SELECT * FROM regional WHERE isactivereg=1", new String[0])) {
			regional.add(new SelectItem(rg.getId(),rg.getName()));
		}
		for(Province rg : Province.retrieve("", new String[0])) {
			provincial.add(new SelectItem(rg.getId(),rg.getName()));
		}
		for(Municipality rg : Municipality.retrieve("", new String[0])) {
			municipal.add(new SelectItem(rg.getId(),rg.getName()));
		}
		bars1.add(new SelectItem(0,"All"));
		bars2.add(new SelectItem(0,"All"));
		for(Barangay rg : Barangay.retrieve(" AND mun.munid=1", new String[0])) {
			barangay.add(new SelectItem(rg.getId(),rg.getName()));
			bars1.add(new SelectItem(rg.getId(),rg.getName()));
			bars2.add(new SelectItem(rg.getId(),rg.getName()));
			mapBarangayData.put(rg.getId(), rg);
		}
		
		
		searchTypeId = 0;
		searchTypes = new ArrayList<>();
		searchTypes.add(new SelectItem(0,"Business"));
		searchTypes.add(new SelectItem(1,"Business Transaction"));
		
		businesTrans = new ArrayList<>();
		
		businessIndex = BusinessIndex.builder()
				.dateTmp(new Date())
				.statusId(0)
				.isActive(1)
				.regional(Regional.builder().id(12).build())
				.provincial(Province.builder().id(1).build())
				.municipal(Municipality.builder().id(1).build())
				.barangay(Barangay.builder().id(1).build())
				.purok("Ilang-Ilang")
				.map(BusinessMapping.builder().id(0).build())
				.build();
		
		status = new ArrayList<>();
		status.add(new SelectItem(0, "Active"));
		status.add(new SelectItem(1, "Retired"));
		
		quarters = new ArrayList<>();
		quarterSearch = new ArrayList<>();
		quarterSearch.add(new SelectItem(0, "All"));
		for(BusinessQtrType q : BusinessQtrType.values()) {
			quarters.add(new SelectItem(q.getId(), q.getName()));
			quarterSearch.add(new SelectItem(q.getId(), q.getName()));
		}
		
		years = new ArrayList<>();
		year = DateUtils.getCurrentYear();
		years.add(new SelectItem(0, "All"));
		for(int year=2020; year<=DateUtils.getCurrentYear(); year++) {
			years.add(new SelectItem(year, year+""));
		}
		dateFrom = DateUtils.getDateToday();
		dateTo = dateFrom;
		initBiz("init");
		
	}
	
	public void updateSearchParam() {
		PrimeFaces pf = PrimeFaces.current();
		if(getSearchTypeId()==0) {
			pf.executeScript("showMain()");
		}else {
			pf.executeScript("showSub()");
		}
		
	}
	
	private void initBiz(String type) {
		
		
		business = new ArrayList<BusinessIndex>();
		String sql = "";
		if(getSearchParam()!=null && !getSearchParam().isEmpty()) {
			sql = " AND  (bn.bnname like '%"+ getSearchParam() +"%' OR bn.bnowner like '%"+ getSearchParam() +"%' )";
		}
		if(getBarId1()>0) {
			sql += " AND bgy.bgid=" + getBarId1();
		}
		
		if(getMappingId()==1) {//mapping area
			sql += " AND bn.bzid>0 ";
		}else if(getMappingId()==2) {//no mapping area
			sql += " AND bn.bzid=0 ";
		}
		
		if("init".equalsIgnoreCase(type)) {
			sql += " LIMIT 10";
		}
		
		business = BusinessIndex.retrieve(sql, new String[0]);
		if(business!=null && business.size()==1) {
			clickItem(business.get(0));
		}else {
			Collections.reverse(business);
		}
	}
	
	public void loadBusiness() {
		initBiz("search");
	}
	
	public void loadBusinessTrans() {
		//if(getYear()>0 && getQtrId()>0) {
			businesTrans = new ArrayList<BusinessIndexTrans>();
			String sql = "";
			String[] params = new String[0];
			String[] ch = {" "," ",","};
			if(getSearchName()!=null && !getSearchName().isEmpty()) {
				List<String> str = new ArrayList<String>();
				//identify space character
				boolean isFound = false;
				for(String c : ch) {
					if(getSearchName().contains(c)) {
						str.add(c);
						isFound = true;
					}
				}
				if(isFound) {
					//replace found space character
					String tmp = getSearchName();
					for(String c : str) {
						tmp = tmp.replace(c, " ");
						System.out.println("tmp: " + tmp);
					}
					//split the word using space character
					String[] prm = tmp.split(" ");
					sql += " AND ( ";
					int cnt = 1;
					for(String p : prm) {
						if(cnt==1) {
							sql += " bn.bnowner like '%"+ p +"%' OR bn.bnname like '%"+ p +"%' OR bnt.ornumber like '%"+ p +"%'";
					    }else {
							sql += " OR bn.bnowner like '%"+ p +"%' OR bn.bnname like '%"+ p +"%' OR bnt.ornumber like '%"+ p +"%'";
						}
						cnt++;
					}
					sql +=" )";
				}else {
					sql += " AND (bn.bnowner like '%"+ getSearchName() +"%' OR bn.bnname like '%"+ getSearchName() +"%' OR bnt.ornumber like '%"+ getSearchName() +"%') ";
				}
			}
			//if not equal to 3
			if(getTypeOfSearchId()!=3) {
				sql += " AND bnt.bntype=" + getTypeOfSearchId();
			}
			
			if(getYear()>0) {
				sql += " AND bnt.bnyear="+getYear();
			}
			
			if(getQtrId()>0) {
				sql += " AND bnt.qtrype=" + getQtrId();
			}
			
			if(isIncludeDate()) {
				params = new String[2];
				params[0] = DateUtils.convertDate(getDateFrom(), "yyyy-MM-dd");
				params[1] = DateUtils.convertDate(getDateTo(), "yyyy-MM-dd");
				sql = " AND (bnt.bntdate>=? AND bnt.bntdate<=?)";
			}
			
			if(getBarId2()>0) {
				sql += " AND bn.bgid=" + getBarId2();
			}
			
			sql += " ORDER BY bn.bnowner";
			String address = "";
			for(BusinessIndexTrans b : BusinessIndexTrans.retrieve(sql, params)) {
				int id = b.getBusinessIndex().getBarangay().getId();
				address = getMapBarangayData().get(id).getName();
				double amount = ORNameList.retrieveORNumberTotalAmount(b.getOrnumber());
				b.setAmountPaid(amount);
				b.getBusinessIndex().setAddress(address);
				businesTrans.add(b);
			}
		//}
	}
	
	public void clickItem(BusinessIndex index) {
		index.setDateTmp(DateUtils.convertDateString(index.getDateTrans(), "yyyy-MM-dd"));
		businessIndex = index;
	}
	
	public void delete(BusinessIndex index) {
		index.delete();
		business.remove(index);
		Application.addMessage(1, "Success", "Successfully deleted");
	}
	
	public void clear() {
		businessIndex = BusinessIndex.builder()
				.dateTmp(new Date())
				.statusId(0)
				.isActive(1)
				.regional(Regional.builder().id(12).build())
				.provincial(Province.builder().id(1).build())
				.municipal(Municipality.builder().id(1).build())
				.barangay(Barangay.builder().id(1).build())
				.purok("Ilang-Ilang")
				.map(BusinessMapping.builder().id(0).build())
				.build();
	}
	
	public void save() {
		businessIndex.setDateTrans(DateUtils.convertDate(businessIndex.getDateTmp(), "yyyy-MM-dd"));
		businessIndex.save();
		loadBusiness();
		Application.addMessage(1, "Success", "Successfully saved");
		clear();
	}
	
	public List<String> autoOwnerName(String query){
		return BusinessCustomer.names(query);
	}
	
	public List<String> autoNatureOfBusiness(String query){
		System.out.println("business query: " + query);
		String sql = "SELECT DISTINCT natureofbusiness FROM businessindex WHERE  natureofbusiness like '%" + query + "%' LIMIT 20";
		return BusinessIndex.retrieveNatureOfBusiness(sql, new String[0]);
	}
	
	public List<String> autoBusinessName(String query){
		System.out.println("business query: " + query);
		String sql = "SELECT DISTINCT livename FROM livelihood WHERE  livename like '%" + query + "%' LIMIT 20";
		return Livelihood.retrieveBusinessName(sql, new String[0]);
	}
	
	public void addDetails(BusinessIndex index) {
		businessIndex = index;
		tranData = BusinessIndexTrans.builder()
				.year(DateUtils.getCurrentYear())
				.dateTrans(DateUtils.getCurrentDateYYYYMMDD())
				.businessIndex(index)
				.isActive(1)
				.build();
		trans = BusinessIndexTrans.retrieve(" AND bn.bnid=" + businessIndex.getId(), new String[0]);
		trans.add(tranData);
		
		
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('dlgAddDetails').show(1000)");
	}
	public void deleteDtls(BusinessIndexTrans tran) {
		if(tran.getId()>0) {
			tran.delete();
			trans.remove(tran);
			Application.addMessage(1, "Success", "Successfully deleted");
		}else {
			Application.addMessage(2, "Success", "Item cannot be deleted");
		}
	}
	
	public void saveTrans(BusinessIndexTrans tran) {
		boolean isOk = true;
		if(BusinessQtrType.ANNUAL.getId()==tran.getQtrPayment()
				|| BusinessQtrType.FIRST_QTR.getId()==tran.getQtrPayment()
				|| BusinessQtrType.FIRST_SEMI_ANNUAL.getId()==tran.getQtrPayment()
				|| BusinessQtrType.FIRST_TO_THIRD.getId()==tran.getQtrPayment()) {
			if(tran.getBasicTax()==0) {//required basic tax
				isOk = false;
				Application.addMessage(3, "Error", "Basix tax is required");
			}
		}
		
		if(isOk) {
			tran.save();
			trans = BusinessIndexTrans.retrieve(" AND bn.bnid=" + businessIndex.getId(), new String[0]);
			tranData = BusinessIndexTrans.builder()
					.year(DateUtils.getCurrentYear())
					.dateTrans(DateUtils.getCurrentDateYYYYMMDD())
					.businessIndex(tran.getBusinessIndex())
					.isActive(1)
					.build();
			trans.add(tranData);
			Application.addMessage(1, "Success", "Successfully saved");
		}
	}
	
	public void onCellEdit(CellEditEvent event) {}
	
	public void findORNumber(BusinessIndexTrans tran) {
		String sql = " AND orl.ornumber='"+ tran.getOrnumber() +"'";
		String text="";
		
		text += "<p>Business Name: "+tran.getBusinessIndex().getBusinessName()+"</p>";
		text += "<p>Owner: "+tran.getBusinessIndex().getOwner()+"</p>";
		text += "<br/>";
		text += "<pi>Official Receipt No.: "+ tran.getOrnumber() +"</pi>";
		text += "<br/>";
		text += "<pi>Payment Details</pi>";
		text += "<br/>";
		text += "<ul>";
		double amount = 0d;
		String date="";
		String payor="";
		int count = 1;
		for(ORNameList on : ORNameList.retrieve(sql, new String[0])) {
			text += "<li>" + on.getPaymentName().getName() + "\t" + Currency.formatAmount(on.getAmount()) + "</li>";
			amount += on.getAmount();
			if(count==1) {
				date = on.getOrList().getDateTrans();
				payor=on.getCustomer().getFullname();
			}
			count++;
		}
		text += "</ul>";
		text += "<br/>";
		text += "<bold>Total: "+ Currency.formatAmount(amount) +"</bold>";
		text += "<br/>";
		text += "<br/>";
		text += "<pi>This receipt is paid by: "+ payor +" on " + DateUtils.convertDateToMonthDayYear(date) + "</pi>";
		
		setOrData(text);
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('dlgORDtls').show(1000)");
	}
	public void closeDetails() {
		clear();
		loadBusiness();
	}
	public void loadMap() {
		maps = new ArrayList<BusinessMapping>();
		String sql = "";
		if(getSearchBusinessMap()!=null && !getSearchBusinessMap().isEmpty()) {
			sql = " AND (name like '%"+ getSearchBusinessMap() +"%' OR owner like '%"+ getSearchBusinessMap() +"%')";
		}
		sql += " ORDER BY owner";
		maps= BusinessMapping.retrieve(sql, new String[0]);
	}
	public void selectedMap(BusinessMapping map) {
		businessIndex.setMap(map);
		System.out.println("map>>>> " + getBusinessIndex().getMap().getId());
	}
	
	public void showStore(BusinessIndex index) {
		BusinessMapping bz = BusinessMapping.mapData(index.getMap().getId());
		copyPhoto(bz.getPictureOfBusiness());
		copyPhoto(bz.getPictureOfOwner());
		setSelectedMapData(bz);
	}

	public static String copyPhoto(String photoId){
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		
        String driveImage =  GlobalVar.BUSINESS_MAP_IMG + photoId;
        String contextImageLoc = File.separator + "resources" + File.separator + "images" + File.separator + "businessmap" + File.separator;
         System.out.println("copying file to this path : " + contextImageLoc);
            String pathToSave = externalContext.getRealPath("") + contextImageLoc;
            File file = new File(driveImage);
            try{
    			Files.copy(file.toPath(), (new File(pathToSave + file.getName())).toPath(),
    			        StandardCopyOption.REPLACE_EXISTING);
    			}catch(IOException e){}
            
            return driveImage;
	}
	public void openSelectedBusinessInMap(BusinessIndex index) {
		BusinessMapping bz = BusinessMapping.mapData(index.getMap().getId());
		setCenterMap(bz.getLatitude()+"," + bz.getLongtitude());
		allBusiness = new DefaultMapModel();
		String noPermit = "https://maps.google.com/mapfiles/ms/micons/red-dot.png";
		String withPermit = "https://maps.google.com/mapfiles/ms/micons/green-dot.png";
		mapBiz = new LinkedHashMap<String, BusinessMapping>();
		String name = "";
		String markerColor = withPermit;
		if(bz.getName()!=null && !bz.getName().isEmpty()) {name =  bz.getName().toUpperCase();}else {name =  bz.getOwner().toUpperCase();}
		if("no".equalsIgnoreCase(bz.getHasPermit())){
			markerColor = noPermit;
		}
		mapBiz.put(name, bz);
		allBusiness.addOverlay(new Marker(new LatLng(Double.valueOf(bz.getLatitude()), Double.valueOf(bz.getLongtitude())), name, bz.getPictureOfBusiness(),markerColor));
	}
	public void onMarkerSelect(OverlaySelectEvent event) {
		
        marker = (Marker) event.getOverlay();
        copyPhoto(marker.getData()+"");
        setSelectedMapData(getMapBiz().get(marker.getTitle()));   
    }
	
	public void sendCustomerSMS(BusinessIndex rs) {
		if(rs!=null && rs.getId()>0 && !rs.getContanctNo().isEmpty()) {
			if(CheckInternetConnection.isInternetPresent("https://semaphore.co/")) {
				System.out.println("The site is accessible...");
				String contactNo = rs.getContanctNo();
				contactNo = contactNo.replace("+63", "");
				int len = contactNo.length();
				if(len==11) {
					String api_key = Words.getTagName("sms-api-key");
					String post_url_msg = Words.getTagName("sms-post-url-msg");
					String user_agent = Words.getTagName("sms-user-agent");
					String msg = GlobalVar.smsMSG.replace("<recepient>", rs.getOwner().split(",")[0]);
					
					System.out.println("sending info "+ msg + " number: " + contactNo);
					String[] response = SendSMS.sendSMS(api_key, contactNo, msg,post_url_msg, user_agent);
					
					//String[] response2 = SendSMS.sendSMS(api_key, contactNo, msg,post_url_msg, user_agent);
					//String[] response = {"SUCCESS"};
					if("SUCCESS".equalsIgnoreCase(response[0])) {
						Application.addMessage(1, "Success", "You have successfully sent the message to " + rs.getOwner());
					}else {
						Application.addMessage(3, "Error", "Sending sms was not successfully. Please try again.");
					}
				}else {
					Application.addMessage(3, "Error", "Please check the contact number of the client. It seems that is not correct.");
				}
			}else {
				System.out.println("Not accessible at this moment....");
				Application.addMessage(3, "Error", "Provider is not available or the internet is not present at this moment. Please re-send the notification to the user once online.");
			}
		}else {
			Application.addMessage(3, "Error", "Please click the client first");
		}
	}
}
