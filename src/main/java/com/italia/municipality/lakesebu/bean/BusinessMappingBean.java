package com.italia.municipality.lakesebu.bean;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.optionconfig.animation.Animation;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.optionconfig.legend.LegendLabel;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.optionconfig.tooltip.Tooltip;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

import com.italia.municipality.lakesebu.controller.BusinessMapping;
import com.italia.municipality.lakesebu.global.GlobalVar;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named("mapping")
@ViewScoped
public class BusinessMappingBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1348798989656L;
	@Setter @Getter private List<BusinessMapping> maps;
	@Setter @Getter private List<BusinessMapping> mapsTemp;
	@Setter @Getter private String searchParam;
	@Setter @Getter private BarChartModel barangayModel;
	@Setter @Getter private BarChartModel permitModel;
	@Setter @Getter private BarChartModel rentedModel;
	
	@Setter @Getter private BarChartModel stackedBarModel;
	@Setter @Getter private List<BusinessMapping> dataInfos;
	
	@Setter @Getter private MapModel allBusiness;
	@Setter @Getter private String centerMap;
	private Marker marker;
	@Setter @Getter private BusinessMapping selectedMapData;
	@Setter @Getter Map<String, BusinessMapping> mapBiz;
	
	@PostConstruct
	private void init() {
		mapBiz = new LinkedHashMap<String, BusinessMapping>();
		maps = BusinessMapping.retrieve(" ORDER BY owner", new String[0]);
		mapsTemp = maps;
		barangay();
		permit();
		rented();
		barangayGraph();
		openMap();
	}
	
	public void load() {
		maps = new ArrayList<BusinessMapping>();
	}
	
	public void businessList() {
		if(getMapsTemp()!=null && getMapsTemp().size()>0) {
			maps = new ArrayList<BusinessMapping>();
			int count = 1;
			if(getSearchParam().contains(":")) {
				System.out.println("Pasok....");
				int cnt = 1;
				for(String val : getSearchParam().split(":")) {
					System.out.println("Pasok.... sa = " + val);
					if(cnt==1) {
						System.out.println("Pasok.... sa count 1 = " + val);
						for(BusinessMapping bm : getMapsTemp()) {
							if(bm.getName().toLowerCase().contains(val.toLowerCase()) ||
									bm.getOwner().toLowerCase().contains(val.toLowerCase()) ||
									bm.getLineOfBusiness().toLowerCase().contains(val.toLowerCase()) || 
									bm.getBarangay().toLowerCase().contains(val.toLowerCase()) ||
									bm.getSitios().toLowerCase().contains(val.toLowerCase())) {
								bm.setNumber(count++);
								maps.add(bm);
								System.out.println("count 1 = " + bm.getName());
							}
						}
					}	
					if(cnt==2) {
						count = 1;//reset count
						System.out.println("Pasok.... sa count 2 = " + val);
							List<BusinessMapping> mapData = new ArrayList<BusinessMapping>();
							for(BusinessMapping bm : maps) {
								if(bm.getName().toLowerCase().contains(val.toLowerCase()) ||
										bm.getOwner().toLowerCase().contains(val.toLowerCase()) ||
										bm.getLineOfBusiness().toLowerCase().contains(val.toLowerCase()) || 
										bm.getBarangay().toLowerCase().contains(val.toLowerCase()) ||
										bm.getSitios().toLowerCase().contains(val.toLowerCase())) {
									bm.setNumber(count++);
									mapData.add(bm);
									System.out.println("count 2 = " + bm.getName());
								}
							}
							maps = new ArrayList<BusinessMapping>();
							maps = mapData;
					}
					
					if(cnt==3) {//look for permit
						count = 1;//reset count
						System.out.println("Pasok.... sa count 3 = " + val);
							List<BusinessMapping> mapData = new ArrayList<BusinessMapping>();
							for(BusinessMapping bm : maps) {
								if(bm.getHasPermit().toLowerCase().contains(val.toLowerCase())) {
									bm.setNumber(count++);
									mapData.add(bm);
									System.out.println("count 3 = " + bm.getName());
								}
							}
							maps = new ArrayList<BusinessMapping>();
							maps = mapData;
					}
					
					if(cnt==4) {//look for rented
						count = 1;//reset count
						System.out.println("Pasok.... sa count 4 = " + val);
							List<BusinessMapping> mapData = new ArrayList<BusinessMapping>();
							for(BusinessMapping bm : maps) {
								if(bm.getRented().toLowerCase().contains(val.toLowerCase())) {
									bm.setNumber(count++);
									mapData.add(bm);
									System.out.println("count 4 = " + bm.getName());
								}
							}
							maps = new ArrayList<BusinessMapping>();
							maps = mapData;
					}
					
					cnt++;
				}
				
			}else {
				for(BusinessMapping bm : getMapsTemp()) {
					if(bm.getName().toLowerCase().contains(getSearchParam().toLowerCase()) ||
							bm.getOwner().toLowerCase().contains(getSearchParam().toLowerCase()) ||
							bm.getLineOfBusiness().toLowerCase().contains(getSearchParam().toLowerCase()) || 
							bm.getHasPermit().toLowerCase().contains(getSearchParam().toLowerCase()) ||
							bm.getBarangay().toLowerCase().contains(getSearchParam().toLowerCase()) ||
							bm.getSitios().toLowerCase().contains(getSearchParam().toLowerCase())) {
						bm.setNumber(count++);
						maps.add(bm);
					}
				}
			}
		}
		if(getSearchParam()==null || getSearchParam().isBlank()) {
			maps = mapsTemp;
		}
	}
	
	public void barangayGraph() {
		
		Map<String, Map<String, Integer>> bargs = new LinkedHashMap<String, Map<String, Integer>>();
		Map<String, Integer> permits = new LinkedHashMap<String, Integer>();
		
		for(BusinessMapping bz : getMapsTemp()) {
			String barangay = bz.getBarangay();
			String permit = bz.getHasPermit();
			if(bargs!=null && bargs.size()>0) {
				if(bargs.containsKey(barangay)) {
					if(bargs.get(barangay).containsKey(permit)) {
						int count = bargs.get(barangay).get(permit) + 1;
						bargs.get(barangay).put(permit, count);
					}else {
						bargs.get(barangay).put(permit, 1);
					}
				}else {
					permits = new LinkedHashMap<String, Integer>();
					permits.put(permit, 1);
					bargs.put(barangay, permits);
				}
			}else {
				permits.put(permit, 1);
				bargs.put(barangay, permits);
			}
		}
		
		stackedBarModel = new BarChartModel();
        ChartData data = new ChartData();
        
		Map<String, Map<String, Integer>> sortedData = new TreeMap<String, Map<String, Integer>>(bargs);
		
		BarChartDataSet barDataSetBar = new BarChartDataSet();
		barDataSetBar.setLabel("Barangay");
		barDataSetBar.setBackgroundColor("rgb(75, 192, 192)");
		List<Number> dataValBar = new ArrayList<>();
		
		BarChartDataSet barDataSetYes = new BarChartDataSet();
		barDataSetYes.setLabel("With Permit");
		barDataSetYes.setBackgroundColor("rgb(54, 162, 235)");
        List<Number> dataValYes = new ArrayList<>();
		
        BarChartDataSet barDataSetNo = new BarChartDataSet();
        barDataSetNo.setLabel("No Permit");
        barDataSetNo.setBackgroundColor("rgb(255, 99, 132)");
        List<Number> dataValNo = new ArrayList<>();
        
        List<String> labels = new ArrayList<>();
        dataInfos = new ArrayList<BusinessMapping>();
		for(String bar : sortedData.keySet()) {
			
			int totalBar = 0;
			int hasPermit = 0;
			int noPermit = 0;
			for(String key : sortedData.get(bar).keySet()) {
				if("Yes".equalsIgnoreCase(key)) {
					totalBar += sortedData.get(bar).get(key);
					hasPermit = sortedData.get(bar).get(key);
				}else if("No".equalsIgnoreCase(key)) {
					totalBar += sortedData.get(bar).get(key);
					noPermit = sortedData.get(bar).get(key);
				}
			}
			
			dataValBar.add(totalBar);
			dataValYes.add(hasPermit);
			dataValNo.add(noPermit);
			labels.add(bar);
			
			dataInfos.add(
        			BusinessMapping.builder().barangay(bar).count(totalBar).countWithPermit(hasPermit).countNoPermit(noPermit).build()
        			);
			
		}
		barDataSetBar.setData(dataValBar);
		barDataSetYes.setData(dataValYes);
		barDataSetNo.setData(dataValNo);
		
		data.addChartDataSet(barDataSetBar);
        data.addChartDataSet(barDataSetYes);
        data.addChartDataSet(barDataSetNo);
        
        data.setLabels(labels);
        stackedBarModel.setData(data);
        
      //Options
        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setStacked(true);
        linearAxes.setOffset(true);
        cScales.addXAxesData(linearAxes);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Business Permit Graph");
        options.setTitle(title);

        Tooltip tooltip = new Tooltip();
        tooltip.setMode("index");
        tooltip.setIntersect(false);
        options.setTooltip(tooltip);

        stackedBarModel.setOptions(options);
        
	}
	
	public void barangay() {
		barangayModel = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet barDataSet = new BarChartDataSet();
        
        List<String> labels = new ArrayList<>();
        List<String> borderColor = new ArrayList<>();
        List<String> bgColor = new ArrayList<>();
        List<Number> values = new ArrayList<>();
        
        Map<String, Integer> mapBarangay = new LinkedHashMap<String, Integer>();
        int countTotal = 0;
        if(getMapsTemp()!=null && getMapsTemp().size()>0) {
        	for(BusinessMapping bm : getMapsTemp()) {
        		if(mapBarangay!=null) {
        			if(mapBarangay.containsKey(bm.getBarangay())) {
        				int count = mapBarangay.get(bm.getBarangay()) + 1;
        				mapBarangay.put(bm.getBarangay(), count);
        			}else {
        				mapBarangay.put(bm.getBarangay(), 1);
        			}
        		}else {
        			mapBarangay.put(bm.getBarangay(), 1);
        		}
        		countTotal++;
        	}
        }
        
        Map<String, Integer> barangaySorted = new TreeMap<String, Integer>(mapBarangay);
        for(String bar : barangaySorted.keySet()) {
        	values.add(barangaySorted.get(bar));
        	labels.add(bar);
        	//bgColor.add("rgba(255, 99, 132, 0.2)");
        	bgColor.add("rgba(75, 192, 192, 0.2)");
        	//borderColor.add("rgb(255, 99, 132)");
        	borderColor.add("rgb(75, 192, 192)");
        	
        }
        
        barDataSet.setLabel("Barangay");
        barDataSet.setData(values);
        barDataSet.setBackgroundColor(bgColor);
        barDataSet.setBorderColor(borderColor);
        barDataSet.setBorderWidth(1);
        data.addChartDataSet(barDataSet);
        data.setLabels(labels);
        barangayModel.setData(data);
        
      //Options
        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setOffset(true);
        //linearAxes.setBeginAtZero(true);
        CartesianLinearTicks ticks = new CartesianLinearTicks();
        linearAxes.setTicks(ticks);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Graph Report for Barangay total of " + countTotal);
        options.setTitle(title);

        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("top");
        LegendLabel legendLabels = new LegendLabel();
        legendLabels.setFontStyle("italic");
        legendLabels.setFontColor("#2980B9");
        legendLabels.setFontSize(24);
        legend.setLabels(legendLabels);
        options.setLegend(legend);

        // disable animation
        Animation animation = new Animation();
        animation.setDuration(0);
        options.setAnimation(animation);

        barangayModel.setOptions(options);
	}
	
	public void permit() {
		permitModel = new BarChartModel();
        ChartData data = new ChartData();

       
        
        Map<String, Integer> mapPermit = new LinkedHashMap<String, Integer>();
        int countTotal = 0;
        if(getMapsTemp()!=null && getMapsTemp().size()>0) {
        	for(BusinessMapping bm : getMapsTemp()) {
        		if(mapPermit!=null) {
        			if(mapPermit.containsKey(bm.getHasPermit())) {
        				int count = mapPermit.get(bm.getHasPermit()) + 1;
        				mapPermit.put(bm.getHasPermit(), count);
        			}else {
        				mapPermit.put(bm.getHasPermit(), 1);
        			}
        		}else {
        			mapPermit.put(bm.getHasPermit(), 1);
        		}
        		countTotal++;
        	}
        }
        
        BarChartDataSet barDataSetYes = new BarChartDataSet();
        
        List<String> labelsYes = new ArrayList<>();
        List<String> borderColorYes = new ArrayList<>();
        List<String> bgColorYes = new ArrayList<>();
        List<Number> valuesYes = new ArrayList<>();
        
        valuesYes.add(mapPermit.get("Yes"));
        labelsYes.add("Has Permit");
        bgColorYes.add("rgba(75, 192, 192, 0.2)");
        borderColorYes.add("rgb(75, 192, 192)");
        
        barDataSetYes.setLabel("Has Permit ("+ mapPermit.get("Yes") + ")");
        barDataSetYes.setData(valuesYes);
        barDataSetYes.setBackgroundColor(bgColorYes);
        barDataSetYes.setBorderColor(borderColorYes);
        barDataSetYes.setBorderWidth(1);
        data.addChartDataSet(barDataSetYes);
        data.setLabels(labelsYes);
        
        
        BarChartDataSet barDataSetNo = new BarChartDataSet();
        
        List<String> labelsNo = new ArrayList<>();
        List<String> borderColorNo = new ArrayList<>();
        List<String> bgColorNo = new ArrayList<>();
        List<Number> valuesNo = new ArrayList<>();
        
        valuesNo.add(mapPermit.get("No"));
        labelsNo.add("Has Permit");
        bgColorNo.add("rgba(255, 99, 132, 0.2)");
        borderColorNo.add("rgb(255, 99, 132)");
        
        barDataSetNo.setLabel("No Permit ("+ mapPermit.get("No") +")");
        barDataSetNo.setData(valuesNo);
        barDataSetNo.setBackgroundColor(bgColorNo);
        barDataSetNo.setBorderColor(borderColorNo);
        barDataSetNo.setBorderWidth(1);
        data.addChartDataSet(barDataSetNo);
        data.setLabels(labelsNo);
        
      
        permitModel.setData(data);
        
      //Options
        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setOffset(true);
        //linearAxes.setBeginAtZero(true);
        CartesianLinearTicks ticks = new CartesianLinearTicks();
        linearAxes.setTicks(ticks);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Graph Report for Business with Permit and No Permit total of " + countTotal);
        options.setTitle(title);

        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("top");
        LegendLabel legendLabels = new LegendLabel();
        legendLabels.setFontStyle("italic");
        legendLabels.setFontColor("#2980B9");
        legendLabels.setFontSize(24);
        legend.setLabels(legendLabels);
        options.setLegend(legend);

        // disable animation
        Animation animation = new Animation();
        animation.setDuration(0);
        options.setAnimation(animation);

        permitModel.setOptions(options);
	}
	
	public void rented() {
		rentedModel = new BarChartModel();
        ChartData data = new ChartData();

       
        
        Map<String, Integer> mapRented = new LinkedHashMap<String, Integer>();
        int countTotal = 0;
        if(getMapsTemp()!=null && getMapsTemp().size()>0) {
        	for(BusinessMapping bm : getMapsTemp()) {
        		if(mapRented!=null) {
        			if(mapRented.containsKey(bm.getRented())) {
        				int count = mapRented.get(bm.getRented()) + 1;
        				mapRented.put(bm.getRented(), count);
        			}else {
        				mapRented.put(bm.getRented(), 1);
        			}
        		}else {
        			mapRented.put(bm.getRented(), 1);
        		}
        		countTotal++;
        	}
        }
        
        BarChartDataSet barDataSetYes = new BarChartDataSet();
        
        List<String> labelsYes = new ArrayList<>();
        List<String> borderColorYes = new ArrayList<>();
        List<String> bgColorYes = new ArrayList<>();
        List<Number> valuesYes = new ArrayList<>();
        
        valuesYes.add(mapRented.get("Yes"));
        labelsYes.add("Renting");
        bgColorYes.add("rgba(75, 192, 192, 0.2)");
        borderColorYes.add("rgb(75, 192, 192)");
        
        barDataSetYes.setLabel("Renting ("+ mapRented.get("Yes") + ")");
        barDataSetYes.setData(valuesYes);
        barDataSetYes.setBackgroundColor(bgColorYes);
        barDataSetYes.setBorderColor(borderColorYes);
        barDataSetYes.setBorderWidth(1);
        data.addChartDataSet(barDataSetYes);
        data.setLabels(labelsYes);
        
        
        BarChartDataSet barDataSetNo = new BarChartDataSet();
        
        List<String> labelsNo = new ArrayList<>();
        List<String> borderColorNo = new ArrayList<>();
        List<String> bgColorNo = new ArrayList<>();
        List<Number> valuesNo = new ArrayList<>();
        
        valuesNo.add(mapRented.get("No"));
        labelsNo.add("Not Renting");
        bgColorNo.add("rgba(255, 99, 132, 0.2)");
        borderColorNo.add("rgb(255, 99, 132)");
        
        barDataSetNo.setLabel("Not Renting ("+ mapRented.get("No") +")");
        barDataSetNo.setData(valuesNo);
        barDataSetNo.setBackgroundColor(bgColorNo);
        barDataSetNo.setBorderColor(borderColorNo);
        barDataSetNo.setBorderWidth(1);
        data.addChartDataSet(barDataSetNo);
        data.setLabels(labelsNo);
        
      
        rentedModel.setData(data);
        
      //Options
        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setOffset(true);
        //linearAxes.setBeginAtZero(true);
        CartesianLinearTicks ticks = new CartesianLinearTicks();
        linearAxes.setTicks(ticks);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Graph Report for Business who Renting and Not Renting total of " + countTotal);
        options.setTitle(title);

        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("top");
        LegendLabel legendLabels = new LegendLabel();
        legendLabels.setFontStyle("italic");
        legendLabels.setFontColor("#2980B9");
        legendLabels.setFontSize(24);
        legend.setLabels(legendLabels);
        options.setLegend(legend);

        // disable animation
        Animation animation = new Animation();
        animation.setDuration(0);
        options.setAnimation(animation);

        rentedModel.setOptions(options);
	}
	
	public void onTabChange(TabChangeEvent event) {
		
		if("Business List".equalsIgnoreCase(event.getTab().getTitle())) {
			businessList();
		}else if("Barangay".equalsIgnoreCase(event.getTab().getTitle())) {
			barangay();
		}else if("Permit".equalsIgnoreCase(event.getTab().getTitle())) {
			permit();
		}else if("Rented".equalsIgnoreCase(event.getTab().getTitle())) {
			rented();
		}else if("Combine Graph".equalsIgnoreCase(event.getTab().getTitle())) {
			barangayGraph();
		}
		
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
	
	public void openMap() {
		allBusiness = new DefaultMapModel();
		if(getMaps()!=null && getMaps().size()<=100) {
			setCenterMap(getMaps().get(0).getLatitude()+", "+getMaps().get(0).getLongtitude());
		}else {
			setCenterMap("6.2238228, 124.7116173");
		}
		
		String noPermit = "https://maps.google.com/mapfiles/ms/micons/red-dot.png";
		String withPermit = "https://maps.google.com/mapfiles/ms/micons/green-dot.png";
		mapBiz = new LinkedHashMap<String, BusinessMapping>();
		for(BusinessMapping bz : getMaps()) {
			String name = "";
			String markerColor = withPermit;
			if(bz.getName()!=null && !bz.getName().isEmpty()) {name =  bz.getName().toUpperCase();}else {name =  bz.getOwner().toUpperCase();}
			if("no".equalsIgnoreCase(bz.getHasPermit())){
				markerColor = noPermit;
			}
			mapBiz.put(name, bz);
			allBusiness.addOverlay(new Marker(new LatLng(Double.valueOf(bz.getLatitude()), Double.valueOf(bz.getLongtitude())), name, bz.getPictureOfBusiness(),markerColor));
		}
	}
	
	public void onMarkerSelect(OverlaySelectEvent event) {
		
        marker = (Marker) event.getOverlay();
        copyPhoto(marker.getData()+"");
        setSelectedMapData(getMapBiz().get(marker.getTitle()));
        
        System.out.println("Name:" + getSelectedMapData().getName() + " owner: " + getSelectedMapData().getOwner());
        
    }
	
	public void openSelectedBusinessInMap(BusinessMapping bz) {
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
	
	public void showStore(BusinessMapping bz) {
		mapBiz = new LinkedHashMap<String, BusinessMapping>();
		String name = "";
		if(bz.getName()!=null && !bz.getName().isEmpty()) {name =  bz.getName().toUpperCase();}else {name =  bz.getOwner().toUpperCase();}
		mapBiz.put(name, bz);
		System.out.println("Business Picture: " + bz.getPictureOfBusiness());
		copyPhoto(bz.getPictureOfBusiness());
		copyPhoto(bz.getPictureOfOwner());
		setSelectedMapData(bz);
	}

    public Marker getMarker() {
        return marker;
    }
	
}
