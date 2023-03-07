package com.italia.municipality.lakesebu.licensing.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.PrimeFaces;
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
import com.italia.municipality.lakesebu.enm.GraphColor;
import com.italia.municipality.lakesebu.licensing.controller.BusinessPermit;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 1/20/2022
 *
 */
@Named
@ViewScoped
@Data
public class BusinessGraphBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 434354657641L;
	private LineChartModel lineModel;
	private List years;
	private List selectedYear;
	private List types;
	private List selectedType;
	private List memos;
	private List selectedMemo;
	private BarChartModel barModel2;
	
	@PostConstruct
	public void init() {
		loadDummyData();
		dummyBarModel2();
	}
	
	public void loadGraph() {
		
		selectedYear = new ArrayList<>();
		selectedYear.add(DateUtils.getCurrentYear());
		years = new ArrayList<>();
		for(int year=2020; year<=DateUtils.getCurrentYear(); year++) {
			years.add(new SelectItem(year, year+""));
		}
		
		selectedType = new ArrayList<>();
		types = new ArrayList<>();
		//types.add(new SelectItem("ALL", "All"));
		types.add(new SelectItem("NEW", "NEW"));
		types.add(new SelectItem("RENEW", "RENEW"));
		types.add(new SelectItem("ADDITIONAL", "ADDITIONAL"));
		
		selectedMemo = new ArrayList<>();
		memos = new ArrayList<>();
		//memos.add(new SelectItem("ALL", "All"));
		memos.add(new SelectItem("ANNUALLY", "ANNUALLY"));
		memos.add(new SelectItem("SEMI-ANNUAL", "SEMI-ANNUAL"));
		memos.add(new SelectItem("QUARTERLY", "QUARTERLY"));
		
		//loadCurrentData();
		loadDummyData();
		dummyBarModel2();
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('dlgGraph').show(1000);");
	}
	
	public void showGraph() {
		//createLineGraph();
		loadG();
	}
	
	public void createLineGraph() {
		Map<String, Map<String, Map<String, Double>>> years = new LinkedHashMap<String, Map<String, Map<String, Double>>>();
		Map<String, Map<String, Double>> types = new LinkedHashMap<String, Map<String, Double>>();
		Map<String, Double> memo = new LinkedHashMap<String, Double>();
		//List<BusinessPermit> permits = new ArrayList<BusinessPermit>();
		
		String sql = "";
		if(getSelectedYear()!=null && getSelectedYear().size()>0) {
			
			int count = 1;
			sql = " AND (";
			for(Object year : getSelectedYear()) {
				if(count==1) {
					sql += " bz.year='"+ year +"' ";
				}else {
					sql += " OR bz.year='"+ year +"' ";
				}
				count++;
			}
			sql +=") ";
			
		
			if(getSelectedType()!=null && getSelectedType().size()>0) {
				sql += " AND (";
				count = 1;
				for(Object o : getSelectedType()) {
					
					if(count==1) {
						sql += " bz.typeof='"+ o +"'";
					}else {
						sql += " OR bz.typeof='"+ o +"'";
					}
					
					count++;
				}
				sql +=") ";
			}
			
			if(getSelectedMemo()!=null && getSelectedMemo().size()>0) {
				sql += " AND (";
				count = 1;
				for(Object o : getSelectedMemo()) {
					
					if(count==1) {
						sql += " bz.memotype='"+ o +"'";
					}else {
						sql += " OR bz.memotype='"+ o +"'";
					}
					
					count++;
				}
				sql +=") ";
			}
			
			System.out.println("For maping SQL: " + sql);
			
			for(BusinessPermit p : BusinessPermit.retrieve(sql, new String[0])) {
				String year = p.getYear();
				String type = p.getType();
				String mem = p.getMemoType();
				
				if(years!=null) {
					if(years.containsKey(year) ) {
						types = new LinkedHashMap<String, Map<String, Double>>();
						memo = new LinkedHashMap<String, Double>();
						
						
						if(years.get(year).containsKey(type)) {
							
							if(years.get(year).get(type).containsKey(mem)) {
								double num = years.get(year).get(type).get(mem) + 1;
								years.get(year).get(type).put(mem,num);
							}else {
								years.get(year).get(type).put(mem, 1.0);
							}
							
						}else {
							memo = new LinkedHashMap<String, Double>();;
							memo.put(mem, 1.0);
							years.get(year).put(type, memo);
						}
						
					}else {
						types = new LinkedHashMap<String, Map<String, Double>>();
						memo = new LinkedHashMap<String, Double>();
						memo.put(mem, 1.0);
						types.put(type, memo);
						years.put(year, types);
					}
				}else {
					memo.put(mem, 1.0);
					types.put(type, memo);
					years.put(year, types);
				}
				
			}
			
			//loadLine(years);
		
		}
	}

	
private void loadG() {
	
	String sql = "";
	if(getSelectedYear()!=null && getSelectedYear().size()>0) {
		
		int count = 1;
		
		for(Object year : getSelectedYear()) {
			if(count==1) {
				sql += " year='"+ year +"' ";
			}else {
				sql += " OR year='"+ year +"' ";
			}
			count++;
		}
		
	
		Map<String, Map<String, Double>> mapData = BusinessPermit.getPerType(sql);
		loadLine(mapData);
	}
}
	
 private void loadLine(Map<String, Map<String, Double>> mapData) {
	lineModel = new LineChartModel();
	ChartData data = new ChartData();
    int color = 1;
    
    
    
    for(String year : mapData.keySet()) {
    	
        
    	//String[] types = {"NEW","RENEW","ADDITIONAL"};
    	String[] memos = {"ANNUALLY","SEMI-ANNUAL","QUARTERLY"};
        for(String type : mapData.get(year).keySet()) {
        	
        	LineChartDataSet dataSet = new LineChartDataSet();
		 	List<Object> values = new ArrayList<>();
		 	
		 	for(Object y : getSelectedYear()) {
		 	
			 	if(year.equalsIgnoreCase(y+"")) {	
			 		values.add(mapData.get(year).get(type));
			 	}else {
			 		values.add(0);
			 	}
		 	
		 	}
       
		 	
 	        dataSet.setData(values);
	        dataSet.setFill(false);
	        dataSet.setLabel(type + "("+year+")");
	        dataSet.setBorderColor(GraphColor.valueId(color++));
	        data.addChartDataSet(dataSet);
	        
        	
        }
        
    }
    //String[] types = {"NEW","RENEW","ADDITIONAL"};
    List<String> labels = new ArrayList<>();
    for(Object t : getSelectedYear()) {
    	labels.add(t.toString());
    }
    data.setLabels(labels);
    lineModel.setData(data);
    
    boolean isMorethanOneYearSelected = false; 
    if(getSelectedYear()!=null) {
    	isMorethanOneYearSelected = getSelectedYear().size()>1? true : false;
    }
    
    LineChartOptions options = new LineChartOptions();
    Title title = new Title();
    title.setDisplay(true);
    title.setText("Year Collection Data" + (isMorethanOneYearSelected==true? "" : " " +  getSelectedYear().toString()));
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
        title.setText("Year Business Data");
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
}