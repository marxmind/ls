package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.optionconfig.legend.LegendLabel;
import org.primefaces.model.charts.optionconfig.title.Title;

import com.italia.municipality.lakesebu.controller.Chequedtls;
import com.italia.municipality.lakesebu.enm.GraphColor;
import com.italia.municipality.lakesebu.enm.GraphColorWithBorder;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named
@RequestScoped
public class CheckGraphBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 13537786454545L;
	@Setter @Getter private BarChartModel barModel;
	@Setter @Getter private List years;
	@Setter @Getter private List selectedYear;
	
	@PostConstruct
	public void init() {
		dummyBarModel();
	}
	
	public void loadGraph() {
		System.out.println("loading graph default value");
		dummyBarModel();
		selectedYear = new ArrayList<>();
		years = new ArrayList<>();
		for(int year=2021; year<=DateUtils.getCurrentYear(); year++) {
			years.add(new SelectItem(year, year+""));
		}
	}
	
	public void dummyBarModel() {
        barModel = new BarChartModel();
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
        barModel.setData(data);

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
        //legendLabels.setFontStyle("bold");
        legendLabels.setFontColor("#2980B9");
        legendLabels.setFontSize(12);
        legend.setLabels(legendLabels);
        options.setLegend(legend);
        
		/*
		 * Tooltip tooltip = new Tooltip(); tooltip.setEnabled(true);
		 * tooltip.setMode("index"); tooltip.setIntersect(false);
		 * options.setTooltip(tooltip);
		 */
        
        barModel.setOptions(options);
    }
	
	public void loadBar() {
		System.out.println("Selected year: " + getSelectedYear().toString());
		barModel = new BarChartModel();
	    ChartData data = new ChartData();
	    int color = 1;
	    boolean isMorethanOneYearSelected=false;
	    if(getSelectedYear()!=null && getSelectedYear().size()>1) {
	    	isMorethanOneYearSelected=true;
	    }
	    Map<Integer,Map<Integer, Double>> mapYear = new LinkedHashMap<Integer, Map<Integer, Double>>();
	    Map<Integer, Double> mapMonth = new LinkedHashMap<Integer, Double>();
	    
	    List<String> labels = new ArrayList<>();
	    BarChartDataSet barDataSet = new BarChartDataSet();
    	List<Number> values = new ArrayList<>();
	    
	    for(Object o : getSelectedYear()) {
	    	int year = Integer.valueOf(o.toString());
	    	Map<Integer, Double> years= Chequedtls.getByMonth(year);
	    	for(int month : years.keySet()) {
	    		values.add(years.get(month));
	    		barDataSet.setLabel(DateUtils.getMonthName(month));
	    	}
	    	barDataSet.setData(values);
		    barDataSet.setBackgroundColor(GraphColorWithBorder.valueId(color));
		    barDataSet.setBorderColor(GraphColor.valueId(color));
		    barDataSet.setBorderWidth(1);
	        data.addChartDataSet(barDataSet);
        	color++;
	    }
	 
	    
	    
	}

}
