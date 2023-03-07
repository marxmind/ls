package com.italia.municipality.lakesebu.reports;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import com.italia.municipality.lakesebu.bean.CalendarBean;
import com.italia.municipality.lakesebu.controller.CalendarDate;
import com.italia.municipality.lakesebu.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class CalendarDays {
	
	private int month;
	private int year;
	
	private int id;
	private String dayName;
	private String date;
	private int day;
	private String monthName;
	private CalendarDate monday;
	private CalendarDate tuesday;
	private CalendarDate wednesday;
	private CalendarDate thursday;
	private CalendarDate friday;
	private CalendarDate saturday;
	private CalendarDate sunday;
	
	public static void main(String[] args) {
		
		
		for(Week w : displayMonthSelected(2, 2022)) {
			System.out.println(
					w.getSunday() + " " +
					w.getMonday() + " " +
					w.getTuesday() + " " +
					w.getWednesday() + " " +
					w.getThursday() + " " +
					w.getFriday() + " " +
					w.getSaturday()
					);
		}
	}
	
	
	
	public static List<Week> displayCurrentMonth() {
		int month = DateUtils.getCurrentMonth();
		int year = DateUtils.getCurrentYear();
		int monthStartWeek = DateUtils.monthStartWeek(month, year);
		int lastDay = DateUtils.getLastDay(month, year, Locale.TAIWAN);
		return monthDisplayDate(monthStartWeek, lastDay, month, year, new int[0]);
	}
	
	public static List<Week> displayMonthSelected(int month, int year) {
		int monthStartWeek = DateUtils.monthStartWeek(month, year);
		int lastDay = DateUtils.getLastDay(month, year, Locale.TAIWAN);
		return monthDisplayDate(monthStartWeek, lastDay, month, year, new int[0]);
	}
	
	public static List<Week> displayMonthSelected(int month, int year, int[] holidays) {
		int monthStartWeek = DateUtils.monthStartWeek(month, year);
		int lastDay = DateUtils.getLastDay(month, year, Locale.TAIWAN);
		return monthDisplayDate(monthStartWeek, lastDay, month, year, holidays);
	}
	
	/**
	 * 
	 * @param weekStartDate [sunday=1,monday=2,tuesday=3,wednesday=4,thursday=5,friday=6,saturday=7]
	 * @param endDay = end of the month date ex february end date is 28
	 */
	private static List<Week> monthDisplayDate(int weekStartDate, int endDay, int month, int year, int[] holidays) {
		int dayCount = 1;
		List<Week> weeks = new ArrayList<Week>();
		for(int d=1; d<=endDay; d++) {
			for(int week=1; week<=6; week++ ) {
				Week wk = new Week();
					for(int day=1; day<=7; day++) {
						
						if(dayCount<weekStartDate) {
							d=1;//retain 1
							week(wk, day, 0,month, year, holidays);
						}else {
						
							if(d>endDay) {
								week(wk, day, 0, month, year, holidays);
							}else {
								week(wk, day, d, month, year, holidays);
							}
							d++;
						}
						dayCount++;
					}
					weeks.add(wk);
			}
		}	
		return weeks;
	}
	
	private static Week week(Week w, int week, int day,int month, int year, int[] holidays) {
		if(day==0) return w;
		String col = "";
		w.setMonth(month);
		w.setYear(year);
		
		boolean isFound = false;
		if(holidays.length>0) {
			for(int d : holidays) {
				if(day==d) {
					col="color: green; font-weight: bold; font-size: 20px";
					isFound = true;
				}
			}
		}
		
		//override color if the date is today
		if(month==DateUtils.getCurrentMonth() && year==DateUtils.getCurrentYear() && day==DateUtils.getCurrentDay()) {
			col="color: orange; font-weight: bold; font-size: 20px";
			//if date found in holiday
			//change yellow color
			if(isFound) {
				col="color: yellow; font-weight: bold; font-size: 20px";
			}
		}
		
		switch(week) {
			case 1: w.setSunday(day); w.setId(day); w.setSundayStyle("color: red; font-weight: bold; font-size: 20px"); break;
			case 2: w.setMonday(day); w.setId(day); w.setMondayStyle(col); break;
			case 3: w.setTuesday(day); w.setId(day); w.setTuesdayStyle(col); break;
			case 4: w.setWednesday(day); w.setId(day); w.setWednesdayStyle(col); break;
			case 5: w.setThursday(day); w.setId(day); w.setThursdayStyle(col); break;
			case 6: w.setFriday(day); w.setId(day); w.setFridayStyle(col); break;
			case 7: w.setSaturday(day); w.setId(day); w.setSaturdayStyle(col); break;
		}
		return w;
	}
	
	public static List<CalendarDays> initCalendar(int startMonth, int endMonth, int yearStart, int yearEnd){
		if(yearStart>yearEnd) {yearEnd=yearStart;}//if year start is greater than year end then change the year end value to year start value
		if(startMonth>endMonth) {endMonth=startMonth;}//if start month value is greater than end month value then replace end month value with the start month value
		List<CalendarDays> days = new ArrayList<CalendarDays>();
		int count = 1;
			for(int year=yearStart; year<=yearEnd; year++) {
				if(yearStart<yearEnd) {endMonth=12;}
				if(count>1) {startMonth=1; endMonth=12;}
				while(startMonth<=endMonth) {
					days.addAll(initCalendar(startMonth, year));
					startMonth++;
				}
				count++;
			}
		return days;
	}
	
	public static List<CalendarDays> initCalendar(int month, int year) {
		List<CalendarDays> days = new ArrayList<CalendarDays>();
		String monthName = DateUtils.getMonthName(month);
		java.util.Calendar cal = new GregorianCalendar(year, month-1, 1);
		String mnth = (month<10? "0"+month : month)+"";
		do {
			
			int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
			 int dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH);
			 String dy = (dayOfMonth<10? "0"+dayOfMonth : dayOfMonth)+"";
			 String val = year+"-"+mnth+"-"+dy;
			 com.italia.municipality.lakesebu.controller.CalendarDate cy = com.italia.municipality.lakesebu.controller.CalendarDate.builder()
					 .isActive(1)
					 .type(1)
					 .dateVal(val).build();
			 
			 CalendarDays day = new CalendarDays();
			 	day.setMonth(month);
			 	day.setYear(year);
			 	day.setDate(val);
			 	day.setDay(dayOfMonth);
			 	day.setMonthName(monthName);
				 switch(dayOfWeek) {
					 case 1 : day.setSunday(cy); day.setDayName("Sunday"); break;
					 case 2 : day.setMonday(cy); day.setDayName("Monday"); break;
					 case 3 : day.setTuesday(cy); day.setDayName("Tuesday"); break;
					 case 4 : day.setWednesday(cy); day.setDayName("Wednesday"); break;
					 case 5 : day.setThursday(cy); day.setDayName("Thursday"); break;
					 case 6 : day.setFriday(cy); day.setDayName("Friday"); break;
					 case 7 : day.setSaturday(cy); day.setDayName("Saturday"); break;
				 }
			  days.add(day);
			 cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
		}  while (cal.get(java.util.Calendar.MONTH) == month-1);
		
		return days;
	}
}
