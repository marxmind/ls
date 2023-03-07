package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.PrimeFaces;
import com.italia.municipality.lakesebu.controller.CalendarDate;
import com.italia.municipality.lakesebu.enm.CalendarType;
import com.italia.municipality.lakesebu.reports.CalendarDays;
import com.italia.municipality.lakesebu.reports.Week;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.DateUtils;
import jakarta.annotation.PostConstruct;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named
@ViewScoped
public class CalendarBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3234647985861L;
	
	@Setter @Getter private int monthId;
	@Setter @Getter private List months;
	
	@Setter @Getter private int yearId;
	@Setter @Getter private List years;
	
	@Setter @Getter private String dateVal;
	@Setter @Getter private String remarks;
	
	@Setter @Getter private List<Week> calendars;
	@Setter @Getter private List<CalendarDate> calendarRecords;
	@Setter @Getter private CalendarDate calendarData;
	
	@Setter @Getter private List types;
	@Setter @Getter private int type;
	
	public CalendarBean() {
		
		
		types = new ArrayList<>();
		//types.add(new SelectItem(0, "Select Type"));
		for(CalendarType t : CalendarType.values()) {
			types.add(new SelectItem(t.getId(), t.getName()));
		}
		
		months = new ArrayList<>();
		int month = 1;
		while(month<=12) {
			months.add(new SelectItem(month, DateUtils.getMonthName(month)));
			month++;
		}
		monthId = DateUtils.getCurrentMonth();
		years = new ArrayList<>();
		yearId=2022;
		while(yearId<=DateUtils.getCurrentYear()+2) {
			years.add(new SelectItem(yearId,yearId+""));
			yearId++;
		}
		yearId = DateUtils.getCurrentYear();
	}
	
	@PostConstruct
	public void init() {
		new CalendarBean();
		updateCalendar();
	}
	
	public void updateCalendar() {
		
		String month = getMonthId()<10? "0"+getMonthId(): getMonthId()+"";
		String year = getYearId()+"";
		String sql = " AND datetype>=2 AND (caldate>=? AND caldate<=?) ORDER BY caldate";
		String[] params = new String[2];
		params[0] = year + "-" + month + "-01";
		params[1] = year + "-" + month + "-31";
		List<CalendarDate> days = CalendarDate.retrieve(sql,params);
		int size = days.size();
		int[] nums=new int[size];
		int i=0;
		for(CalendarDate d : days) {
			int num = Integer.valueOf(d.getDateVal().split("-")[2]);
			nums[i++] = num;
		}
		
		
		calendars = CalendarDays.displayMonthSelected(getMonthId(), getYearId(),nums);
		//Week.showOutput(calendars);
	}
	
	public void updateNextCalendar() {
		int prevMonth = getMonthId();
		if(getMonthId()<12) {
			setMonthId(getMonthId()+1);
		}
		if(prevMonth>getMonthId()) {
			setYearId(getYearId()+1);
		}
		calendars = CalendarDays.displayMonthSelected(getMonthId(), getYearId(), new int[0]);
	}
	public void updatePrevCalendar() {
		int prevMonth = getMonthId();
		if(getMonthId()<12) {
			setMonthId(getMonthId()-1);
		}
		if(prevMonth>getMonthId()) {
			setYearId(getYearId()-1);
		}
		calendars = CalendarDays.displayMonthSelected(getMonthId(), getYearId());
	}
	
	
	public void clickDate(int day) {
		String val = getYearId() + "-" + (getMonthId()<10? "0"+getMonthId(): getMonthId()) + "-" + (day<10? "0"+day:day);
		System.out.println("click date:"+val);
		
		setDateVal(val);
		loadRecord(val);
		
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("showRecord()");
		
	}
	
	public void loadRecord(String valDate) {
		calendarRecords = CalendarDate.retrieve(" AND caldate='"+ valDate +"'",new String[0]);
	}
	
	public void saveRecord() {
		CalendarDate cal = getCalendarData();
		if(cal==null) {cal=new CalendarDate();}
		boolean isOk = true;
		if(getRemarks().isEmpty()) {
			isOk = false;
			Application.addMessage(3, "Error", "Please add remarks");
		}
		if(getType()==0) {
			isOk = false;
			Application.addMessage(3, "Error", "Please select type");
		}
		if(isOk) {
			
			cal.setIsActive(1);
			cal.setDateVal(getDateVal());
			cal.setRemarks(getRemarks());
			cal.setType(getType());
			cal = CalendarDate.save(cal);
			loadRecord(cal.getDateVal());
			setCalendarData(null);
		}
	}
	public void clickRecord(CalendarDate cal) {
		setCalendarData(cal);
		setDateVal(cal.getDateVal());
		setRemarks(cal.getRemarks());
		setType(cal.getType());
	}
	public void deleteRecord(CalendarDate cal) {
		cal.delete();
		loadRecord(cal.getDateVal());
	}
	
	
}
