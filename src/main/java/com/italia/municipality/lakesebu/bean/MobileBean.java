package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.optionconfig.legend.LegendLabel;
import org.primefaces.model.charts.optionconfig.title.Title;
import com.italia.municipality.lakesebu.controller.CollectionInfo;
import com.italia.municipality.lakesebu.controller.Collector;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.ORNameList;
import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.controller.TaxAccountGroup;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.enm.GraphColor;
import com.italia.municipality.lakesebu.enm.GraphColorWithBorder;
import com.italia.municipality.lakesebu.enm.Pages;
import com.italia.municipality.lakesebu.security.ClientInfo;
import com.italia.municipality.lakesebu.security.Module;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.LogU;
import com.italia.municipality.lakesebu.utils.OpenTableAccess;
import com.italia.municipality.lakesebu.utils.Whitelist;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import lombok.Data;


@Named
@ViewScoped
@Data
public class MobileBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 124354565657L;
	
	
	private String userName;
	private String password;
	private int moduleId;
	private List modules;
	private boolean enableData;
	
	private LineChartModel lineModel;
	private List years;
	private List selectedYear;
	private List collectors;
	private List selectedCollector;
	private Map<Integer, Collector> collectorMap;
	private BarChartModel barModel2;
	private String tabName="line";
	
	private List accountYears;
	private List accountSelectedYear;
	private List accounts;
	private List accountSelected;
	private Map<String,String> mapAccounts;
	private BarChartModel barModel3;
	
	@PostConstruct
	public void init() {
		setEnableData(true);
		String val = ReadConfig.value(AppConf.SERVER_LOCAL);
        HttpSession session = SessionBean.getSession();
		session.setAttribute("server-local", val);
		session.setAttribute("theme", "saga");
		System.out.println("assigning local for mobile: " + val);
		
		System.out.println("init mobile...");
		loadOther();
		
		graphInit();
		
	}
	
	
	private void graphInit() {
		collectorMap = new LinkedHashMap<Integer, Collector>();
		accountSelectedYear = new ArrayList<>();
		accountYears = new ArrayList<>();
				
		loadDummyData();
		dummyBarModel2();
		dummyBarModel3();
		selectedYear = new ArrayList<>();
		years = new ArrayList<>();
		for(int year=2019; year<=DateUtils.getCurrentYear(); year++) {
			years.add(new SelectItem(year, year+""));
			accountYears.add(new SelectItem(year, year+""));
		}
		
		selectedCollector = new ArrayList<>();
		collectors = new ArrayList<>();
		for(Collector col : Collector.retrieve(" ORDER BY cl.collectorname", new String[0])) {
			collectors.add(new SelectItem(col.getId(), col.getName()));
			collectorMap.put(col.getId(), col);
		}
		
		;
		accounts = new ArrayList<>();
		mapAccounts = new LinkedHashMap<String, String>();
		for(TaxAccountGroup acc : TaxAccountGroup.retrieve(" ORDER BY st.accname ", new String[0])) {
			accounts.add(new SelectItem(acc.getId(), acc.getName()));
			mapAccounts.put(acc.getName(), acc.getName());
		}
	}
	
	private void loadOther() {
		modules = new ArrayList<>();
		modules.add(new SelectItem(0, "Select Module"));
		for(Module m : Module.values()) {
			modules.add(new SelectItem(m.getId(), m.getName()));
			System.out.println("module:" + m.getName());
		}
	}
	
	public String validateUserNamePassword(){
		
		String sql = "SELECT * FROM login WHERE username=? and password=?";
		String[] params = new String[2];
		         params[0] = Whitelist.remove(getUserName());
		         params[1] = Whitelist.remove(getPassword());
		Login in = null;
		try{in = Login.retrieve(sql, params).get(0);}catch(Exception e){return "login";}
		
		String result="mobile";
		LogU.add("Guest with username : " + getUserName() + " and password ******** is trying to log in the system.");
		System.out.println("validating .... " + getPassword() + " - " + password);
		if(in!=null){
			String val = ReadConfig.value(AppConf.SERVER_LOCAL);
	        HttpSession session = SessionBean.getSession();
	        session.setAttribute("username", getUserName());
			session.setAttribute("userid", in.getLogid());
			session.setAttribute("server-local", val);
			
			System.out.println("correct username and password....");
			
			setEnableData(false);
			
			LogU.add("The user has been successfully login to the application with the username : " + userName + " and password " + password);
			
			logUserIn(in);
			
		}else{
			FacesContext.getCurrentInstance().addMessage(
					null,new FacesMessage(
							FacesMessage.SEVERITY_WARN, 
							"Incorrect username and password", 
							"Please enter correct username and password"
							)
					);

			LogU.add("The user was not successfully login to the application with the username : " + userName + " and password " + password);
			setUserName("");
			setPassword("");
			result= "mobile";
			setEnableData(true);
		}
		
		return result;
	}
	
private void logUserIn(Login in){
	if(in==null) in = new Login();
	ClientInfo cinfo = new ClientInfo();
	in.setLogintime(DateUtils.getCurrentDateMMDDYYYYTIME());
	in.setIsOnline(1);
	in.setClientip(cinfo.getClientIP());
	in.setClientbrowser(cinfo.getBrowserName());
	in.save();
}

private String assignModule() {
	
	switch(getModuleId()) {
		case 1 : return Pages.CheckWriting.getName();
		case 2 : return Pages.LandTax.getName();
		case 4 : return Pages.VoucherLoging.getName();
		case 5 : return Pages.Mooe.getName();
		case 6 : return Pages.CashInBank.getName();
		case 7 : return Pages.CashInTreasury.getName();
		case 8 : return Pages.Dtr.getName();
		case 9 : return Pages.Market.getName();
		case 10 : return Pages.StockRecording.getName();
		case 11 : return Pages.IssuedForm.getName();
		case 12 : return Pages.CollectorRecording.getName();
		case 13 : return Pages.ReportGraph.getName();
		case 14 : return Pages.Orlisting.getName();
		case 15 : return Pages.LandTax.getName();
		case 16 : return Pages.UploadRcd.getName();
		case 17 : return Pages.VoucherExpense.getName();
		case 18 : return Pages.WaterBilling.getName();
		case 19 : return Pages.LandTaxCertification.getName();
	}
	
	
	return "main";
}

public void loadDynamic() {
	lineModel = new LineChartModel();
	ChartData data = new ChartData();
    int color = 1;
    Map<Integer, Map<Integer, Double>> mapData = ORNameList.graph(getSelectedYear());
    Map<Integer, Double> amounts = new LinkedHashMap<>();
    for(int year : mapData.keySet()) {
    	
    	
        LineChartDataSet dataSet = new LineChartDataSet();
        List<Object> values = new ArrayList<>();
		
        for(int month=1; month<=12; month++) {
        	if(mapData!=null && mapData.get(year).containsKey(month)) {
        		values.add(mapData.get(year).get(month));
        		amounts.put(month, mapData.get(year).get(month));
        	}else {
        		values.add(0);
        		amounts.put(month, 0.0);
        	}
	    }
        
        dataSet.setData(values);
        dataSet.setFill(false);
        dataSet.setLabel(year+"");
        dataSet.setBorderColor(GraphColor.color(color++));
        data.addChartDataSet(dataSet);
        
        
    }
    List<String> labels = new ArrayList<>();
    for(int month=1; month<=12; month++) {
    	if(getSelectedYear()!=null && getSelectedYear().size()==1) {
	    	double amount = 0d;
	  	   try {amount = amounts.get(month);}catch(Exception e){}
	     	labels.add(DateUtils.getMonthName(month) + (amount>0? "("+ Currency.formatAmount(amount) +")" : ""));
    	}else {
    		labels.add(DateUtils.getMonthName(month));
    	}
    }
    data.setLabels(labels);
    lineModel.setData(data);
    
    LineChartOptions options = new LineChartOptions();
    Title title = new Title();
    title.setDisplay(true);
    title.setText("Year Collection Data");
    options.setTitle(title);

    lineModel.setOptions(options);
    
}

public void selectedGraph() {
	if(getSelectedYear()!=null && getSelectedYear().size()>0) {
		if(getSelectedCollector()!=null && getSelectedCollector().size()>0) {
			if("line".equalsIgnoreCase(getTabName())) {
				loadDynamic2();
			}else {
				loadDynamicBar2();
			}
		}else {
			if("line".equalsIgnoreCase(getTabName())) {
				loadDynamic();
			}else {
				loadBarYear();
			}
		}
		
	}
}

public void loadDynamic2() {
	lineModel = new LineChartModel();
	ChartData data = new ChartData();
    int color = 1;
    Map<Integer, Map<Integer, Map<Integer, Double>>> mapData = CollectionInfo.graph(getSelectedYear(),getSelectedCollector(),0);
    
    boolean isMorethanOneYearSelected=false;
    if(getSelectedYear()!=null && getSelectedYear().size()>1) {
    	isMorethanOneYearSelected=true;
    }
    
    for(int year : mapData.keySet()) {
    	
        
        for(int col : mapData.get(year).keySet()) {
        	
        	 LineChartDataSet dataSet = new LineChartDataSet();
 	         List<Object> values = new ArrayList<>();
        	
 	        for(int month=1; month<=12; month++) {
	        	if(mapData!=null && mapData.get(year).containsKey(col)) {
	        		if(mapData.get(year).get(col).containsKey(month)) {
	        			values.add(mapData.get(year).get(col).get(month));
	        		}else {
	        			values.add(0);
	        		}
	        	}else {
	        		values.add(0);
	        	}
		    }
 	        
 	        String name = getCollectorMap().get(col).getName();
 	        
 	        if(name.contains("-")) {
 	        	name = name.replace("F.L Lopez-", "");
 	        }
 	        
 	        dataSet.setData(values);
	        dataSet.setFill(false);
	        dataSet.setLabel(name +  (isMorethanOneYearSelected==true?  "("+ year +")" : ""));
	        dataSet.setBorderColor(GraphColor.valueId(color++));
	        data.addChartDataSet(dataSet);
        	
        }
        
    }
    List<String> labels = new ArrayList<>();
    for(int month=1; month<=12; month++) {
    	labels.add(DateUtils.getMonthName(month));
    }
    data.setLabels(labels);
    lineModel.setData(data);
    
    LineChartOptions options = new LineChartOptions();
    Title title = new Title();
    title.setDisplay(true);
    title.setText("Year Collection Data" + (isMorethanOneYearSelected==true? "" : " " +  getSelectedYear().toString()));
    options.setTitle(title);

    lineModel.setOptions(options);
    
}

public void loadCurrentData() {
	
	
		lineModel = new LineChartModel();
       ChartData data = new ChartData();
       List<String> labels = new ArrayList<>();
        LineChartDataSet dataSet = new LineChartDataSet();
        List<Object> values = new ArrayList<>();
        
        Map<Integer, Double> mapData = ORNameList.retrieveYear(DateUtils.getCurrentYear());
        
        for(int key : mapData.keySet()) {
        	labels.add(DateUtils.getMonthName(key));
        	values.add(mapData.get(key));
        }
        
        
        dataSet.setData(values);
        dataSet.setFill(false);
        dataSet.setLabel(DateUtils.getCurrentYear()+" Collections");
        dataSet.setBorderColor("rgb(75, 192, 192)");
        //dataSet.setTension(0.1);
        data.addChartDataSet(dataSet);

        
        
        data.setLabels(labels);

        //Options
        LineChartOptions options = new LineChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Year Collection Data");
        options.setTitle(title);

        lineModel.setOptions(options);
        lineModel.setData(data);
	
	
}

public void loadDummyData() {
	
        lineModel = new LineChartModel();
       ChartData data = new ChartData();

        LineChartDataSet dataSet = new LineChartDataSet();
        List<Object> values = new ArrayList<>();
        values.add(65);
        values.add(59);
        values.add(80);
        values.add(81);
        values.add(56);
        values.add(55);
        values.add(40);
        dataSet.setData(values);
        dataSet.setFill(false);
        dataSet.setLabel("My First Dataset");
        dataSet.setBorderColor("rgb(75, 192, 192)");
        //dataSet.setTension(0.1);
        data.addChartDataSet(dataSet);

        List<String> labels = new ArrayList<>();
        
        for(int i=1; i<=12; i++) {
        	labels.add(DateUtils.getMonthName(i));
        }
        
        data.setLabels(labels);

        //Options
        LineChartOptions options = new LineChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Year Collection Data");
        options.setTitle(title);

        lineModel.setOptions(options);
        lineModel.setData(data);
    
}

public void dummyBarModel2() {
    barModel2 = new BarChartModel();
    ChartData data = new ChartData();

    BarChartDataSet barDataSet = new BarChartDataSet();
    barDataSet.setLabel("My First Dataset");
    barDataSet.setBackgroundColor("rgba(255, 99, 132, 0.2)");
    barDataSet.setBorderColor("rgb(255, 99, 132)");
    barDataSet.setBorderWidth(1);
    List<Number> values = new ArrayList<>();
    values.add(65);
    values.add(59);
    values.add(80);
    values.add(81);
    values.add(56);
    values.add(55);
    values.add(40);
    barDataSet.setData(values);

    BarChartDataSet barDataSet2 = new BarChartDataSet();
    barDataSet2.setLabel("My Second Dataset");
    barDataSet2.setBackgroundColor("rgba(255, 159, 64, 0.2)");
    barDataSet2.setBorderColor("rgb(255, 159, 64)");
    barDataSet2.setBorderWidth(1);
    List<Number> values2 = new ArrayList<>();
    values2.add(85);
    values2.add(69);
    values2.add(20);
    values2.add(51);
    values2.add(76);
    values2.add(75);
    values2.add(10);
    barDataSet2.setData(values2);
    

    data.addChartDataSet(barDataSet);
    data.addChartDataSet(barDataSet2);

    List<String> labels = new ArrayList<>();
    labels.add("January");
    labels.add("February");
    labels.add("March");
    labels.add("April");
    labels.add("May");
    labels.add("June");
    labels.add("July");
    data.setLabels(labels);
    barModel2.setData(data);

    //Options
    BarChartOptions options = new BarChartOptions();
    CartesianScales cScales = new CartesianScales();
    CartesianLinearAxes linearAxes = new CartesianLinearAxes();
    linearAxes.setOffset(true);
    CartesianLinearTicks ticks = new CartesianLinearTicks();
    //ticks.setBeginAtZero(true);
    linearAxes.setTicks(ticks);
    cScales.addYAxesData(linearAxes);
    options.setScales(cScales);

    Title title = new Title();
    title.setDisplay(true);
    title.setText("Bar Chart");
    options.setTitle(title);
    
    
    Legend legend = new Legend();
    legend.setDisplay(true);
    legend.setPosition("bottom");
    LegendLabel legendLabels = new LegendLabel();
    legendLabels.setFontStyle("bold");
    legendLabels.setFontColor("#2980B9");
    legendLabels.setFontSize(12);
    legend.setLabels(legendLabels);
    options.setLegend(legend);
    
	/*
	 * Tooltip tooltip = new Tooltip(); tooltip.setEnabled(true);
	 * tooltip.setMode("index"); tooltip.setIntersect(false);
	 * options.setTooltip(tooltip);
	 */
    
    barModel2.setOptions(options);
    //barModel2.setExtender("addValueOnGraph");
}

public void loadBarYear() {
	barModel2 = new BarChartModel();
    ChartData data = new ChartData();
       
   int color = 1;
   Map<Integer, Map<Integer, Double>> mapData = CollectionInfo.graph(getSelectedYear());
   boolean isMorethanOneYearSelected=false;
   if(getSelectedYear()!=null && getSelectedYear().size()>1) {
   	isMorethanOneYearSelected=true;
   }
   Map<Integer, Double> amounts = new LinkedHashMap<>();
   for(int year : mapData.keySet()) {
   	
   	
   		BarChartDataSet barDataSet = new BarChartDataSet();
	    barDataSet.setLabel(year+"");
	    barDataSet.setBackgroundColor(GraphColorWithBorder.valueId(color));
	    barDataSet.setBorderColor(GraphColor.valueId(color));
	    barDataSet.setBorderWidth(1);
   	
	    color++;
	    
	     List<Number> values = new ArrayList<>();
		
       for(int month=1; month<=12; month++) {
       	if(mapData!=null && mapData.get(year).containsKey(month)) {
       		values.add(mapData.get(year).get(month));
       		amounts.put(month, mapData.get(year).get(month));
       	}else {
       		values.add(0);
       		amounts.put(month, 0.0);
       	}
	    }
       
       barDataSet.setData(values);
       
       
       
       data.addChartDataSet(barDataSet);
       
       
   }
   List<String> labels = new ArrayList<>();
   for(int month=1; month<=12; month++) {
	   if(getSelectedYear()!=null && getSelectedYear().size()==1) {
		   double amount = 0d;
		   try {amount = amounts.get(month);}catch(Exception e){}
	   		labels.add(DateUtils.getMonthName(month) + (amount>0? "("+ Currency.formatAmount(amount) +")" : ""));
	   }else {
		   labels.add(DateUtils.getMonthName(month));
	   }
   }
   data.setLabels(labels);
   barModel2.setData(data);
   
   
 //Options
   BarChartOptions options = new BarChartOptions();
   CartesianScales cScales = new CartesianScales();
   CartesianLinearAxes linearAxes = new CartesianLinearAxes();
   linearAxes.setOffset(true);
   CartesianLinearTicks ticks = new CartesianLinearTicks();
   //ticks.setBeginAtZero(true);
   linearAxes.setTicks(ticks);
   cScales.addYAxesData(linearAxes);
   options.setScales(cScales);

   Title title = new Title();
   title.setDisplay(true);
   title.setText("Year Collection Data"+ (isMorethanOneYearSelected==true? "" : " " +  getSelectedYear().toString()));
   options.setTitle(title);
   
   
   Legend legend = new Legend();
   legend.setDisplay(true);
   legend.setPosition("bottom");
   LegendLabel legendLabels = new LegendLabel();
   legendLabels.setFontStyle("bold");
   legendLabels.setFontColor("#2980B9");
   legendLabels.setFontSize(12);
   legend.setLabels(legendLabels);
   options.setLegend(legend);
   
	/*
	 * Tooltip tooltip = new Tooltip(); tooltip.setEnabled(true);
	 * tooltip.setMode("index"); tooltip.setIntersect(false);
	 * options.setTooltip(tooltip);
	 */
   
   barModel2.setOptions(options);
    
}

public void loadDynamicBar2() {
	barModel2 = new BarChartModel();
    ChartData data = new ChartData();
    int color = 1;
    Map<Integer, Map<Integer, Map<Integer, Double>>> mapData = CollectionInfo.graph(getSelectedYear(),getSelectedCollector(),0);
    
    boolean isMorethanOneYearSelected=false;
    if(getSelectedYear()!=null && getSelectedYear().size()>1) {
    	isMorethanOneYearSelected=true;
    }
    
    for(int year : mapData.keySet()) {
    	
        
        for(int col : mapData.get(year).keySet()) {
        	
        	BarChartDataSet barDataSet = new BarChartDataSet();
        	List<Number> values = new ArrayList<>();
        	
 	        for(int month=1; month<=12; month++) {
	        	if(mapData!=null && mapData.get(year).containsKey(col)) {
	        		if(mapData.get(year).get(col).containsKey(month)) {
	        			values.add(mapData.get(year).get(col).get(month));
	        		}else {
	        			values.add(0);
	        		}
	        	}else {
	        		values.add(0);
	        	}
		    }
 	        
 	        String name = getCollectorMap().get(col).getName();
 	        if(name.contains("-")) {
 	        	name = name.replace("F.L Lopez-", "");
 	        }
 	        
 	        barDataSet.setData(values);
 	       barDataSet.setLabel(name +  (isMorethanOneYearSelected==true?  "("+ year +")" : ""));
		    barDataSet.setBackgroundColor(GraphColorWithBorder.valueId(color));
		    barDataSet.setBorderColor(GraphColor.valueId(color));
		    barDataSet.setBorderWidth(1);
	        data.addChartDataSet(barDataSet);
        	color++;
        }
        
    }
    List<String> labels = new ArrayList<>();
    for(int month=1; month<=12; month++) {
    	labels.add(DateUtils.getMonthName(month));
    }
    data.setLabels(labels);
    barModel2.setData(data);
    
    
    BarChartOptions options = new BarChartOptions();
    CartesianScales cScales = new CartesianScales();
    CartesianLinearAxes linearAxes = new CartesianLinearAxes();
    linearAxes.setOffset(true);
    CartesianLinearTicks ticks = new CartesianLinearTicks();
    //ticks.setBeginAtZero(true);
    linearAxes.setTicks(ticks);
    cScales.addYAxesData(linearAxes);
    options.setScales(cScales);

    Title title = new Title();
    title.setDisplay(true);
    title.setText("Year Collection Data" + (isMorethanOneYearSelected==true? "" : " " +  getSelectedYear().toString()));
    options.setTitle(title);
    
    
    Legend legend = new Legend();
    legend.setDisplay(true);
    legend.setPosition("bottom");
    LegendLabel legendLabels = new LegendLabel();
    legendLabels.setFontStyle("bold");
    legendLabels.setFontColor("#2980B9");
    legendLabels.setFontSize(12);
    legend.setLabels(legendLabels);
    options.setLegend(legend);
    
	/*
	 * Tooltip tooltip = new Tooltip(); tooltip.setEnabled(true);
	 * tooltip.setMode("index"); tooltip.setIntersect(false);
	 * options.setTooltip(tooltip);
	 */
    
    barModel2.setOptions(options);
    
}

public void onChange(TabChangeEvent event) {
	if("Line".equalsIgnoreCase(event.getTab().getTitle())) {
		
		if(getSelectedCollector()!=null && getSelectedCollector().size()>0) {
			loadDynamic2();
		}else {
			loadDynamic();
		}
		setTabName("line");
	}else if("Bar".equalsIgnoreCase(event.getTab().getTitle())){
		
		if(getSelectedCollector()!=null && getSelectedCollector().size()>0) {
			loadDynamicBar2();
		}else {
			loadBarYear();
		}
		setTabName("bar");
	}	
	
}

public void dummyBarModel3() {
    barModel3 = new BarChartModel();
    ChartData data = new ChartData();

    BarChartDataSet barDataSet = new BarChartDataSet();
    barDataSet.setLabel("Year 1");
    barDataSet.setBackgroundColor("rgba(255, 99, 132, 0.2)");
    barDataSet.setBorderColor("rgb(255, 99, 132)");
    barDataSet.setBorderWidth(1);
    List<Number> values = new ArrayList<>();
    values.add(65);
    values.add(59);
    values.add(80);
    values.add(81);
    values.add(56);
    values.add(55);
    values.add(40);
    barDataSet.setData(values);

    BarChartDataSet barDataSet2 = new BarChartDataSet();
    barDataSet2.setLabel("Year 2");
    barDataSet2.setBackgroundColor("rgba(255, 159, 64, 0.2)");
    barDataSet2.setBorderColor("rgb(255, 159, 64)");
    barDataSet2.setBorderWidth(1);
    List<Number> values2 = new ArrayList<>();
    values2.add(85);
    values2.add(69);
    values2.add(20);
    values2.add(51);
    values2.add(76);
    values2.add(75);
    values2.add(10);
    barDataSet2.setData(values2);
    

    data.addChartDataSet(barDataSet);
    data.addChartDataSet(barDataSet2);

    List<String> labels = new ArrayList<>();
    labels.add("January");
    labels.add("February");
    labels.add("March");
    labels.add("April");
    labels.add("May");
    labels.add("June");
    labels.add("July");
    data.setLabels(labels);
    barModel3.setData(data);

    //Options
    BarChartOptions options = new BarChartOptions();
    CartesianScales cScales = new CartesianScales();
    CartesianLinearAxes linearAxes = new CartesianLinearAxes();
    linearAxes.setOffset(true);
    CartesianLinearTicks ticks = new CartesianLinearTicks();
    //ticks.setBeginAtZero(true);
    linearAxes.setTicks(ticks);
    cScales.addYAxesData(linearAxes);
    options.setScales(cScales);

    Title title = new Title();
    title.setDisplay(true);
    title.setText("Bar Chart");
    options.setTitle(title);
    
    
    Legend legend = new Legend();
    legend.setDisplay(true);
    legend.setPosition("bottom");
    LegendLabel legendLabels = new LegendLabel();
    legendLabels.setFontStyle("bold");
    legendLabels.setFontColor("#2980B9");
    legendLabels.setFontSize(12);
    legend.setLabels(legendLabels);
    options.setLegend(legend);
    
	/*
	 * Tooltip tooltip = new Tooltip(); tooltip.setEnabled(true);
	 * tooltip.setMode("index"); tooltip.setIntersect(false);
	 * options.setTooltip(tooltip);
	 */
    
    barModel3.setOptions(options);
    //barModel2.setExtender("addValueOnGraph");
}

public void reportAcc() {
	
	String sql = "SELECT YEAR(o.timestampol) as year, aa.accname,sum(o.olamount) as amount FROM taxaccntgroup  aa, paymentname pp, ornamelist o "
			+ "WHERE aa.accisactive=1 AND pp.isactivepy=1 AND o.isactiveol=1 AND aa.accid=pp.accntgrpid AND pp.pyid=o.pyid ";
			
	if(getAccountSelectedYear()!=null) {
		if(getAccountSelectedYear().size()>1) {
			sql += " AND (";
			int i=1;
			for(Object s : getAccountSelectedYear()) {
				if(i==1) {
					sql += "(o.timestampol>='"+ s +"-01-01 00:00:00' AND o.timestampol<='"+ s +"-12-31 23:59:59')";
				}else {
					sql += " OR (o.timestampol>='"+ s +"-01-01 00:00:00' AND o.timestampol<='"+ s +"-12-31 23:59:59')";
				}
				i++;
			}
			sql += " ) ";
		}else {
			sql += " AND (";
			for(Object s : getAccountSelectedYear()) {
				sql += "o.timestampol>='"+ s +"-01-01 00:00:00' AND o.timestampol<='"+ s +"-12-31 23:59:59'";
			}
			sql += " ) ";
		}
	}
	
	if(getAccountSelected()!=null && getAccountSelected().size()>0) {
		
		sql += " AND (";
		int c = 1;
		for(Object s : getAccountSelected()) {
			if(c==1) {
				sql += "aa.accid="+s.toString();
			}else {
				sql += " OR aa.accid="+s.toString();
			}
			c++;
		}
		sql += " )";
	}
	
	
	sql += " GROUP BY pp.accntgrpid ORDER BY amount DESC";
	
	System.out.println("account selected:" + getAccountSelected());
	
	if(getAccountSelected()!=null && getAccountSelected().size()==0) {
		sql += " LIMIT 10";
		setMapAccounts(new LinkedHashMap<String, String>());
	}else if(getAccountSelected()!=null && getAccountSelected().size()>0) {
		setMapAccounts(new LinkedHashMap<String, String>());
	}
	
	System.out.println("Check SQL: " + sql);
	ResultSet rs = OpenTableAccess.query(sql, new String[0], new WebTISDatabaseConnect());
	
	Map<Integer, Map<String, Double>> mapData = new LinkedHashMap<Integer, Map<String, Double>>();
	Map<String, Double> data = new LinkedHashMap<String, Double>();
	
	try {
		while(rs.next()) {
			
			int year = rs.getInt("year");
			String name = rs.getString("accname");
			double amount = rs.getDouble("amount");
			
			mapAccounts.put(name, name);//use for label
			
			if(mapData!=null) {
				if(mapData.containsKey(year)) {
						data = new LinkedHashMap<String, Double>();
						System.out.println("3rd="+name);
						mapData.get(year).put(name, amount);
				}else {
					data = new LinkedHashMap<String, Double>();
					data.put(name, amount);
					mapData.put(year, data);
					System.out.println("2nd="+name);
				}
			}else {
				data.put(name, amount);
				mapData.put(year, data);
				System.out.println("1st="+name);
			}
		}
		rs.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	accountGrpah(mapData);
	
	
}

private void accountGrpah(Map<Integer, Map<String, Double>> mapData) {
	barModel3 = new BarChartModel();
    ChartData data = new ChartData();
    int color = 1;
    
    boolean isMorethanOneYearSelected=false;
    if(getAccountSelectedYear()!=null && getAccountSelectedYear().size()>1) {
    	isMorethanOneYearSelected=true;
    }
    Map<String, Double> amounts = new LinkedHashMap<>();
	for(int year : mapData.keySet()) {
		for(String name : mapData.get(year).keySet()) {

			BarChartDataSet barDataSet = new BarChartDataSet();
        	List<Number> values = new ArrayList<>();
        	
        	//System.out.println("year: " + year + "\tname: " + name + "\tamount: " + mapData.get(year).get(name));
        	
        	for(String nem : getMapAccounts().values()) {
        		if(name.equalsIgnoreCase(nem)) {
        			values.add(mapData.get(year).get(nem));
        			amounts.put(nem, mapData.get(year).get(nem));
        		}else {
        			values.add(0);
        			//amounts.put(nem,0.0);
        		}
        	}
        	 
 	        barDataSet.setData(values);
 	        barDataSet.setLabel(name +  (isMorethanOneYearSelected==true?  "("+ year +")" : ""));
		    barDataSet.setBackgroundColor(GraphColorWithBorder.valueId(color));
		    barDataSet.setBorderColor(GraphColor.valueId(color));
		    barDataSet.setBorderWidth(1);
	        data.addChartDataSet(barDataSet);
        	color++;
			
		}
	}
	
	List<String> labels = new ArrayList<>();
	/*
	 * for(int month=1; month<=12; month++) {
	 * labels.add(DateUtils.getMonthName(month)); }
	 */
	System.out.println("amount size: " + amounts.size());
	
	if(getAccountSelectedYear()!=null && getAccountSelectedYear().size()==1) {
		if(getAccountSelected()!=null && getAccountSelected().size()>0) {
			for(String n : amounts.keySet()) {
				labels.add(n + "("+ Currency.formatAmount(amounts.get(n)) +")");
			}
		}else {
			for(String name : getMapAccounts().values()) {
    			labels.add(name);
    }
		}
	}else {	
	
	    for(String name : getMapAccounts().values()) {
	    			labels.add(name);
	    }
    
	}
    
    data.setLabels(labels);
    barModel3.setData(data);
    
    
    BarChartOptions options = new BarChartOptions();
    CartesianScales cScales = new CartesianScales();
    CartesianLinearAxes linearAxes = new CartesianLinearAxes();
    linearAxes.setOffset(true);
    CartesianLinearTicks ticks = new CartesianLinearTicks();
    //ticks.setBeginAtZero(true);
    linearAxes.setTicks(ticks);
    cScales.addYAxesData(linearAxes);
    options.setScales(cScales);

    Title title = new Title();
    title.setDisplay(true);
    title.setText("Year Collection Data" + (isMorethanOneYearSelected==true? "" : " " +  getAccountSelectedYear().toString()));
    options.setTitle(title);
    
    
    Legend legend = new Legend();
    legend.setDisplay(true);
    legend.setPosition("bottom");
    LegendLabel legendLabels = new LegendLabel();
    legendLabels.setFontStyle("bold");
    legendLabels.setFontColor("#2980B9");
    legendLabels.setFontSize(12);
    legend.setLabels(legendLabels);
    options.setLegend(legend);
    
    barModel3.setOptions(options);
}

}