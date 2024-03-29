package com.italia.municipality.lakesebu.bean;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.primefaces.PrimeFaces;
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
import com.italia.municipality.lakesebu.controller.ORNameList;
import com.italia.municipality.lakesebu.controller.TaxAccountGroup;
import com.italia.municipality.lakesebu.database.WebTISDatabaseConnect;
import com.italia.municipality.lakesebu.enm.FormType;
import com.italia.municipality.lakesebu.enm.FundType;
import com.italia.municipality.lakesebu.enm.GraphColor;
import com.italia.municipality.lakesebu.enm.GraphColorWithBorder;
import com.italia.municipality.lakesebu.enm.Months;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.OpenTableAccess;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;
import lombok.Data;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 11/08/2021
 *
 */
@Named
@RequestScoped
@Data
public class GraphIssuanceBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1435756756423L;
	
	private LineChartModel lineModel;
	private List years;
	private List selectedYear;
	private List collectors;
	private List selectedCollector;
	private Map<Integer, Collector> collectorMap;
	private List forms;
	private int selectedForm;
	private BarChartModel barModel2;
	
	private List accountYears;
	private List accountSelectedYear;
	private List accounts;
	private List accountSelected;
	private Map<String,String> mapAccounts;
	private BarChartModel barModel3;
	
	private List cashYears;
	private List cashYearSelected;
	private int fundId;
	private List funds;
	private BarChartModel barModel4;
	
	private Map<Long, TaxAccountGroup> groupMapData;
	private boolean includeMonths;
	
	@PostConstruct
	public void init() {
		
		System.out.println("running graph issuannce.......");
		
		collectorMap = new LinkedHashMap<Integer, Collector>();
		
		loadDummyData();
		selectedYear = new ArrayList<>();
		accountSelectedYear = new ArrayList<>();
		accountYears = new ArrayList<>();
		years = new ArrayList<>();
		cashYears = new ArrayList<>();
		for(int year=2019; year<=DateUtils.getCurrentYear(); year++) {
			years.add(new SelectItem(year, year+""));
			accountYears.add(new SelectItem(year, year+""));
			cashYears.add(new SelectItem(year, year+""));
		}
		selectedCollector = new ArrayList<>();
		collectors = new ArrayList<>();
		for(Collector col : Collector.retrieve(" AND cl.isid!=0 AND cl.isid!=25 ORDER BY cl.departmentid, cl.collectorname", new String[0])) {
			collectors.add(new SelectItem(col.getId(), col.getName()));
			collectorMap.put(col.getId(), col);
		}
		
		selectedForm = 0;
		forms = new ArrayList<>();
		forms.add(new SelectItem(0, "All Forms"));
		for(FormType f : FormType.values()) {
			forms.add(new SelectItem(f.getId(), f.getDescription()));
		}
		accounts = new ArrayList<>();
		mapAccounts = new LinkedHashMap<String, String>();
		groupMapData = new LinkedHashMap<Long, TaxAccountGroup>();
		for(TaxAccountGroup acc : TaxAccountGroup.retrieve(" ORDER BY st.accname ", new String[0])) {
			accounts.add(new SelectItem(acc.getId(), acc.getName()));
			mapAccounts.put(acc.getName(), acc.getName());
			groupMapData.put(acc.getId(), acc);
		}
		
		funds = new ArrayList<>();
		fundId = FundType.GENERAL_FUND.getId();
		for(FundType type : FundType.values()) {
			funds.add(new SelectItem(type.getId(), type.getName()));
		}
		
		dummyBarModel2();
		dummyBarModel3();
		dummyBarModel4();
	}
	
	public void dummyBarModel2() {
        barModel2 = new BarChartModel();
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
	
	public void dummyBarModel4() {
        barModel4 = new BarChartModel();
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
        barModel4.setData(data);

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
        
        barModel4.setOptions(options);
        //barModel2.setExtender("addValueOnGraph");
    }
	
	public void loadCurrentBar() {
        barModel2 = new BarChartModel();
        ChartData data = new ChartData();
        
        BarChartDataSet barDataSet = new BarChartDataSet();
        barDataSet.setLabel(DateUtils.getCurrentYear()+" Collections");
        barDataSet.setBackgroundColor("rgba(255, 99, 132, 0.2)");
        barDataSet.setBorderColor("rgb(255, 99, 132)");
        barDataSet.setBorderWidth(1);
        List<Number> values = new ArrayList<>();
        Map<Integer, Double> mapData = CollectionInfo.retrieveYear(DateUtils.getCurrentYear());
        List<String> labels = new ArrayList<>();
        
        for(int key : mapData.keySet()) {
        	labels.add(DateUtils.getMonthName(key));
        	values.add(mapData.get(key));
        }
        
       
        
        barDataSet.setData(values);
        data.addChartDataSet(barDataSet);

        
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
        title.setText("Year Collection Data");
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
	
	public void loadDynamicBar() {
		System.out.println("loadDynamicBar.......................");
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
		   
	    	
		    color++;
		    
		     List<Number> values = new ArrayList<>();
		     double totalAmount = 0d;
	        for(int month=1; month<=12; month++) {
	        	if(mapData!=null && mapData.get(year).containsKey(month)) {
	        		values.add(mapData.get(year).get(month));
	        		amounts.put(month, mapData.get(year).get(month));
	        		totalAmount += mapData.get(year).get(month);
	        	}else {
	        		values.add(0);
	        		amounts.put(month, 0.0);
	        	}
		    }
	        
	        barDataSet.setLabel(year+"=" + Currency.formatAmount(totalAmount));
		    barDataSet.setBackgroundColor(GraphColorWithBorder.valueId(color));
		    barDataSet.setBorderColor(GraphColor.valueId(color));
		    barDataSet.setBorderWidth(1);
	        
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
	    Map<Integer, Map<Integer, Map<Integer, Double>>> mapData = CollectionInfo.graph(getSelectedYear(),getSelectedCollector(),getSelectedForm());
	    
	    boolean isMorethanOneYearSelected=false;
	    if(getSelectedYear()!=null && getSelectedYear().size()>1) {
	    	isMorethanOneYearSelected=true;
	    }
	    
	    for(int year : mapData.keySet()) {
	    	
	    	//double totalAmount = 0d;
	        for(int col : mapData.get(year).keySet()) {
	        	
	        	BarChartDataSet barDataSet = new BarChartDataSet();
	        	List<Number> values = new ArrayList<>();
	        	double totalAmount = 0d;
	 	        for(int month=1; month<=12; month++) {
		        	if(mapData!=null && mapData.get(year).containsKey(col)) {
		        		if(mapData.get(year).get(col).containsKey(month)) {
		        			values.add(mapData.get(year).get(col).get(month));
		        			totalAmount += mapData.get(year).get(col).get(month);
		        		}else {
		        			values.add(0);
		        		}
		        	}else {
		        		values.add(0);
		        	}
			    }
	 	        
	 	        barDataSet.setData(values);
	 	       barDataSet.setLabel(getCollectorMap().get(col).getName() +  (isMorethanOneYearSelected==true?  "("+ year +")=" + Currency.formatAmount(totalAmount) : "="+Currency.formatAmount(totalAmount)));
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
        title.setText("Year Collection Data" + (isMorethanOneYearSelected==true? "" : " " +  getSelectedYear().toString()) + " " + (getSelectedForm()==0? "" : FormType.nameId(getSelectedForm())));
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
	
	public void loadGraph() {
		
		loadCurrentData();
		loadCurrentBar();
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('dlgGraph').show(1000);");
	}
	
	
	public void loadCurrentData() {
		
		
		lineModel = new LineChartModel();
       ChartData data = new ChartData();
       List<String> labels = new ArrayList<>();
        LineChartDataSet dataSet = new LineChartDataSet();
        List<Object> values = new ArrayList<>();
        
        Map<Integer, Double> mapData = CollectionInfo.retrieveYear(DateUtils.getCurrentYear());
        
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
	
	
	
	public void selectedGraph() {
		
		if(getSelectedYear()!=null && getSelectedYear().size()>0) {
			if(getSelectedCollector().size()>0) {
				loadDynamic2();
				loadDynamicBar2();
			}else {
				loadDynamic();
				loadDynamicBar();
			}
			
		}
	}
	
	public void loadDynamic() {
		lineModel = new LineChartModel();
		ChartData data = new ChartData();
	    int color = 1;
	    Map<Integer, Map<Integer, Double>> mapData = CollectionInfo.graph(getSelectedYear());
	    boolean isMorethanOneYearSelected=false;
	    if(getSelectedYear()!=null && getSelectedYear().size()>1) {
	    	isMorethanOneYearSelected=true;
	    }
	    Map<Integer, Double> amounts = new LinkedHashMap<>();
	    for(int year : mapData.keySet()) {
	    	
	    	
	        LineChartDataSet dataSet = new LineChartDataSet();
	        List<Object> values = new ArrayList<>();
	        double totalAmount = 0d;
	        for(int month=1; month<=12; month++) {
	        	if(mapData!=null && mapData.get(year).containsKey(month)) {
	        		values.add(mapData.get(year).get(month));
	        		amounts.put(month, mapData.get(year).get(month));
	        		totalAmount += mapData.get(year).get(month);
	        	}else {
	        		values.add(0);
	        		amounts.put(month, 0.0);
	        	}
		    }
	        
	        dataSet.setData(values);
	        dataSet.setFill(false);
	        dataSet.setLabel(year+"="+Currency.formatAmount(totalAmount));
	        dataSet.setBorderColor(GraphColor.valueId(color++));
	        
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
        title.setText("Year Collection Data"+ (isMorethanOneYearSelected==true? "" : " " +  getSelectedYear().toString()));
        options.setTitle(title);

        lineModel.setOptions(options);
        //lineModel.setExtender("addValueOnGraph");
        
	}
	
	public void loadDynamic2() {
		lineModel = new LineChartModel();
		ChartData data = new ChartData();
	    int color = 1;
	    Map<Integer, Map<Integer, Map<Integer, Double>>> mapData = CollectionInfo.graph(getSelectedYear(),getSelectedCollector(),getSelectedForm());
	    
	    boolean isMorethanOneYearSelected=false;
	    if(getSelectedYear()!=null && getSelectedYear().size()>1) {
	    	isMorethanOneYearSelected=true;
	    }
	    //Map<Integer, Double> mapTotal = new LinkedHashMap<Integer, Double>();
	    for(int year : mapData.keySet()) {
	    	
	        //double totalAmount = 0d;
	        for(int col : mapData.get(year).keySet()) {
	        	
	        	 LineChartDataSet dataSet = new LineChartDataSet();
	 	         List<Object> values = new ArrayList<>();
	 	        double totalAmount = 0d;
	 	        for(int month=1; month<=12; month++) {
		        	if(mapData!=null && mapData.get(year).containsKey(col)) {
		        		if(mapData.get(year).get(col).containsKey(month)) {
		        			values.add(mapData.get(year).get(col).get(month));
		        			totalAmount += mapData.get(year).get(col).get(month);
		        		}else {
		        			values.add(0);
		        		}
		        	}else {
		        		values.add(0);
		        	}
			    }
	 	        
	 	        dataSet.setData(values);
		        dataSet.setFill(false);
		        dataSet.setLabel(getCollectorMap().get(col).getName() +  (isMorethanOneYearSelected==true?  "("+ year +")="+ Currency.formatAmount(totalAmount) : "=" + Currency.formatAmount(totalAmount)));
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
        title.setText("Year Collection Data" + (isMorethanOneYearSelected==true? "" : " " +  getSelectedYear().toString()) + " " + (getSelectedForm()==0? "" : FormType.nameId(getSelectedForm())));
        options.setTitle(title);

        lineModel.setOptions(options);
        
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
        dataSet.setLabel("Please select Data");
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
	
	public void onChange(TabChangeEvent event) {
		if("Line Graph".equalsIgnoreCase(event.getTab().getTitle())) {
			
			
			if(getSelectedYear()!=null && getSelectedYear().size()>0) {
				if(getSelectedCollector().size()>0) {
					loadDynamic2();
				}else {
					loadDynamic();
				}
				
			}
			
		}else if("Bar Graph".equalsIgnoreCase(event.getTab().getTitle())){
			
			if(getSelectedYear()!=null && getSelectedYear().size()>0) {
				if(getSelectedCollector().size()>0) {
					loadDynamicBar2();
				}else {
					loadDynamicBar();
				}
				
			}
			
		}	
	}
	
	
	public void reportAcc() {
		
		if(isIncludeMonths()) {
			reportAccDetailed();
		}else {
		
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
		
	}
	
	private void accountGrpah(Map<Integer, Map<String, Double>> mapData) {
		System.out.println("accountGrpah.............................");
		barModel3 = new BarChartModel();
	    ChartData data = new ChartData();
	    int color = 1;
	    
	    boolean isMorethanOneYearSelected=false;
	    if(getAccountSelectedYear()!=null && getAccountSelectedYear().size()>1) {
	    	isMorethanOneYearSelected=true;
	    }
	    Map<String, Double> amounts = new LinkedHashMap<>();
	    double amount = 0d;
		for(int year : mapData.keySet()) {
			for(String name : mapData.get(year).keySet()) {

				BarChartDataSet barDataSet = new BarChartDataSet();
	        	List<Number> values = new ArrayList<>();
	        	
	        	//System.out.println("year: " + year + "\tname: " + name + "\tamount: " + mapData.get(year).get(name));
	        	
	        	for(String nem : getMapAccounts().values()) {
	        		if(name.equalsIgnoreCase(nem)) {
	        			amount += mapData.get(year).get(nem);
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
	    title.setText("Year Collection Data" + (isMorethanOneYearSelected==true? "" : " " +  getAccountSelectedYear().toString()) + " = " + Currency.formatAmount(amount));
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

@SuppressWarnings("unchecked")
public void reportAccDetailed() {
		
		
		Map<Long, Map<Integer, Map<Integer, Double>>> mapData = new LinkedHashMap<Long, Map<Integer, Map<Integer, Double>>>();
		if(getAccountSelected()!=null && getAccountSelected().size()==0) {
			for(ORNameList aa : ORNameList.getTopTax(Integer.valueOf(getAccountSelectedYear().get(0)+""), 5)) {
				getAccountSelected().add(aa.getGroupId().getId());
			}
		}
	
		if(getAccountSelectedYear()!=null) {
			mapData = ORNameList.retrieveCollection(Integer.valueOf(getAccountSelectedYear().get(0)+""), getAccountSelected().size()>0? true : false,getAccountSelected());
		}
		
		accountGrpahDetailed(mapData);
		
		
	}
	
private void accountGrpahDetailed(Map<Long, Map<Integer, Map<Integer, Double>>> mapData) {
	System.out.println("accountGrpah.............................");
	barModel3 = new BarChartModel();
    ChartData data = new ChartData();
    int color = 1;
    
    boolean isMorethanOneYearSelected=false;
    if(getAccountSelectedYear()!=null && getAccountSelectedYear().size()>1) {
    	isMorethanOneYearSelected=true;
    }
    
    int year = Integer.valueOf(getAccountSelectedYear().get(0)+"");
    
    List<String> labels = new ArrayList<>();
    
    double grandTotal = 0d;
	
	
		for(long group : mapData.keySet()) {
			
			
			BarChartDataSet barDataSet = new BarChartDataSet();
			List<Number> values = new ArrayList<>();
			double total = 0d;
			for(int mm=1; mm<=12; mm++) {
				double amountTax = 0d;
				if(mapData.get(group).get(year).containsKey(mm)) {
					amountTax = mapData.get(group).get(year).get(mm);
					values.add(amountTax);
					total += amountTax;
				}else {
					values.add(amountTax);
				}
			}
			grandTotal += total;
			barDataSet.setData(values);
			barDataSet.setLabel(getGroupMapData().get(group).getName() + "=" + Currency.formatAmount(total));
		    barDataSet.setBackgroundColor(GraphColorWithBorder.valueId(color));
		    barDataSet.setBorderColor(GraphColor.valueId(color));
		    barDataSet.setBorderWidth(1);
		    data.addChartDataSet(barDataSet);
			color++;
		}
	
	//month labels
	for(int m=1; m<=12; m++) {	
		labels.add(DateUtils.getMonthName(m));	
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
    title.setText("Year Collection Data" + (isMorethanOneYearSelected==true? "" : " " +  getAccountSelectedYear().toString()) + " = " + Currency.formatAmount(grandTotal));
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

	public void loadCashDeposit() {
		String sql = "SELECT DATE_FORMAT(alldatetrans,'%Y') as year,DATE_FORMAT(alldatetrans,'%m') as month, amount as total FROM rcdallcontroller WHERE isactiveall=1 AND fundtype=? ";//AND (alldatetrans>=? AND alldatetrans<=?)";
		String[] params = new String[1];
		params[0] = getFundId()+"";
		int nowYear = DateUtils.getCurrentYear();
		if(getCashYearSelected()!=null) {
			if(getCashYearSelected().size()>1) {
				sql += " AND (";
				int i=1;
				for(Object s : getCashYearSelected()) {
					if(i==1) {
						sql += "(alldatetrans>='"+ s.toString() +"-01-01' AND alldatetrans<='"+ s.toString() +"-12-31')";
					}else {
						sql += " OR (alldatetrans>='"+ s.toString() +"-01-01' AND alldatetrans<='"+ s.toString() +"-12-31')";
					}
					i++;
				}
				sql += " ) ";
			}else {
				if(getCashYearSelected().size()==1) {
					sql += " AND (alldatetrans>='"+ Integer.valueOf(getCashYearSelected().get(0).toString())+"-01-01' AND alldatetrans<='"+ Integer.valueOf(getCashYearSelected().get(0).toString()) +"-12-31')";
				}else {
					sql += " AND (alldatetrans>='"+ nowYear +"-01-01' AND alldatetrans<='"+ nowYear +"-12-31')";
				}
			}
		}else {
			sql += " AND (alldatetrans>='"+ nowYear +"-01-01' AND alldatetrans<='"+ nowYear +"-12-31')";
		}
		sql += " ORDER BY alldatetrans";
		//sql += " GROUP BY DATE_FORMAT(alldatetrans,'%m')";
		
		ResultSet rs = OpenTableAccess.query(sql, params, new WebTISDatabaseConnect());
		Map<Integer, Map<Integer, Double>> yearData = new LinkedHashMap<Integer, Map<Integer, Double>>();
		Map<Integer, Double> monthData = new LinkedHashMap<Integer, Double>();
		try {
			while(rs.next()) {
				int month = rs.getInt("month");
				int year = rs.getInt("year");
				double total = rs.getDouble("total");
				if(yearData!=null) {
					if(yearData.containsKey(year)) {
						if(yearData.get(year).containsKey(month)) {
							double amnt = yearData.get(year).get(month);
							total += amnt;
							yearData.get(year).put(month, total);
						}else {
							yearData.get(year).put(month, total);
						}
					}else {
						monthData = new LinkedHashMap<Integer, Double>();
						monthData.put(month, total);
						yearData.put(year, monthData);
					}
				}else {
					monthData.put(month, total);
					yearData.put(year, monthData);
				}
			}
			rs.close();
			Map<Integer, Map<Integer,Double>> sortedData = new TreeMap<Integer, Map<Integer, Double>>(yearData);
			collectionGrpah(sortedData);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void collectionGrpah(Map<Integer, Map<Integer, Double>> mapData) {
		int nowYear = DateUtils.getCurrentYear();
		barModel4 = new BarChartModel();
	    ChartData data = new ChartData();
	    int color = 1;
	    
	    boolean isMorethanOneYearSelected=false;
	    if(getCashYearSelected()!=null && getCashYearSelected().size()>1) {
	    	isMorethanOneYearSelected=true;
	    }
	    Map<Integer, Double> amounts = new LinkedHashMap<>();
		for(int year : mapData.keySet()) {
			BarChartDataSet barDataSet = new BarChartDataSet();
        	List<Number> values = new ArrayList<>();
        	List<String> labels = new ArrayList<>();
        	double grandTotal = 0d;
			for(int month : mapData.get(year).keySet()) {
				String name = Months.getMonthName(month);
				double total = mapData.get(year).get(month);
				values.add(total);
				labels.add(name);
				grandTotal += total;
			}
			barDataSet.setData(values);
 	        //barDataSet.setLabel((isMorethanOneYearSelected==true?  "("+ year +")" : ""));
			barDataSet.setLabel(year+" = " + Currency.formatAmount(grandTotal));
		    barDataSet.setBackgroundColor(GraphColorWithBorder.valueId(color));
		    barDataSet.setBorderColor(GraphColor.valueId(color));
		    barDataSet.setBorderWidth(1);
	        data.addChartDataSet(barDataSet);
	        data.setLabels(labels);
        	color++;
		}
		
		
	    barModel4.setData(data);
	    
	    
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
	    title.setText("Cash Collection Data" + (isMorethanOneYearSelected==true? "" : " " +  (getCashYearSelected()==null? nowYear+"" : getCashYearSelected().toString())));
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
	    
	    barModel4.setOptions(options);
	}
	
}
