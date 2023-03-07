package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.PrimeFaces;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;
import com.italia.municipality.lakesebu.controller.CollectionInfo;
import com.italia.municipality.lakesebu.controller.Collector;
import com.italia.municipality.lakesebu.controller.ORListing;
import com.italia.municipality.lakesebu.controller.ORNameList;
import com.italia.municipality.lakesebu.enm.GraphColor;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 11/08/2021
 *
 */
@Named
@ViewScoped
public class GraphCollectionBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1543654658785L;
	
	@Setter @Getter private LineChartModel lineModel;
	@Setter @Getter private List years;
	@Setter @Getter private List selectedYear;
	@Setter @Getter private List collectors;
	@Setter @Getter private List selectedCollector;
	@Setter @Getter private Map<Integer, Collector> collectorMap;
	
	@PostConstruct
	public void init() {
		
		collectorMap = new LinkedHashMap<Integer, Collector>();
		
		loadDummyData();
		selectedYear = new ArrayList<>();
		years = new ArrayList<>();
		for(int year=2019; year<=DateUtils.getCurrentYear(); year++) {
			years.add(new SelectItem(year, year+""));
		}
		
		selectedCollector = new ArrayList<>();
		collectors = new ArrayList<>();
		for(Collector col : Collector.retrieve(" ORDER BY cl.collectorname", new String[0])) {
			collectors.add(new SelectItem(col.getId(), col.getName()));
			collectorMap.put(col.getId(), col);
		}
	}
	
	public void loadGraph() {
		
		loadCurrentData();
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('dlgGraph').show(1000);");
	}
	
	
	public void loadDynamic() {
		lineModel = new LineChartModel();
		ChartData data = new ChartData();
	    int color = 1;
	    Map<Integer, Map<Integer, Double>> mapData = ORNameList.graph(getSelectedYear());
	    
	    for(int year : mapData.keySet()) {
	    	
	    	
	        LineChartDataSet dataSet = new LineChartDataSet();
	        List<Object> values = new ArrayList<>();
			
	        for(int month=1; month<=12; month++) {
	        	if(mapData!=null && mapData.get(year).containsKey(month)) {
	        		values.add(mapData.get(year).get(month));
	        	}else {
	        		values.add(0);
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
	    	labels.add(DateUtils.getMonthName(month));
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
				loadDynamic2();
			}else {
				loadDynamic();
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
	 	        
	 	        dataSet.setData(values);
		        dataSet.setFill(false);
		        dataSet.setLabel(getCollectorMap().get(col).getName() +  (isMorethanOneYearSelected==true?  "("+ year +")" : ""));
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
	
}