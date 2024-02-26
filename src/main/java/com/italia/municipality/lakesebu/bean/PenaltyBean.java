package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.primefaces.event.CellEditEvent;

import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.PenaltyTable;
import com.italia.municipality.lakesebu.utils.DateUtils;
import com.italia.municipality.lakesebu.utils.Numbers;

import jakarta.annotation.PostConstruct;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;

/**
 * 
 * @author mark
 * @since 12/01/2016
 * @version 1.0
 *
 */
@Named
@ViewScoped
@Data
public class PenaltyBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 643475686586581L;
	private List<PenaltyTable> pens = Collections.synchronizedList(new ArrayList<>());
	private PenaltyTable penaltyTableData;
	private int idFromYear;
	private List fromYear;
	private int idToYear;
	private List toYear;
	private double cntYear;
	private double totalJan;
	private double totalFeb;
	private double totalMar;
	private double totalApr;
	private double totalMay;
	private double totalJun;
	private double totalJul;
	private double totalAug;
	private double totalSep;
	private double totalOct;
	private double totalNov;
	private double totalDec;
	
	@PostConstruct
	public void init(){
		
		toYear = new ArrayList<>();
		for(int i=1985; i<=2030; i++){
			toYear.add(new SelectItem(i,i+""));
		}
		
		fromYear = new ArrayList<>();
		for(int i=1985; i<=DateUtils.getCurrentYear(); i++){
			fromYear.add(new SelectItem(i,i+""));
		}
		
		if(idFromYear==0){
			idFromYear = DateUtils.getCurrentYear() - 10;
		}
		
		if(idToYear==0){
			idToYear = DateUtils.getCurrentYear();
		}
		
		cntYear=0;
		totalJan=0;
		totalFeb=0;
		totalMar=0;
		totalApr=0;
		totalMay=0;
		totalJun=0;
		totalJul=0;
		totalAug=0;
		totalSep=0;
		totalOct=0;
		totalNov=0;
		totalDec=0;
		
		String sql = "SELECT * FROM landtaxpenaltiescal WHERE isactiverate=1 AND (year>=? AND year<=?) ORDER BY year";
		String[] params = new String[2];
		params[0] = getIdFromYear()+"";
		params[1] = getIdToYear()+"";
		pens = Collections.synchronizedList(new ArrayList<>());
		cntYear=0;
		for(PenaltyTable pen : PenaltyTable.retrieve(sql, params)){
			cntYear++;
			totalJan += pen.getJanuary();
			totalFeb += pen.getFebruary();
			totalMar += pen.getMarch();
			totalApr += pen.getApril();
			totalMay += pen.getMay();
			totalJun += pen.getJune();
			totalJul += pen.getJuly();
			totalAug += pen.getAugust();
			totalSep += pen.getSeptember();
			totalOct += pen.getOctober();
			totalNov += pen.getNovember();
			totalDec += pen.getDecember();
			pens.add(pen);
		}
		totalJan = totalJan/cntYear;
		totalFeb = totalFeb/cntYear;
		totalMar = totalMar/cntYear; 
		totalApr = totalApr/cntYear;
		totalMay = totalMay/cntYear;
		totalJun = totalJun/cntYear;
		totalJul = totalJul/cntYear;
		totalAug = totalAug/cntYear;
		totalSep = totalSep/cntYear;
		totalOct = totalOct/cntYear;
		totalNov = totalNov/cntYear;
		totalDec = totalDec/cntYear;
		
		totalJan = Numbers.formatDouble(totalJan);
		totalFeb = Numbers.formatDouble(totalFeb);
		totalMar = Numbers.formatDouble(totalMar);
		totalApr = Numbers.formatDouble(totalApr);
		totalMay = Numbers.formatDouble(totalMay);
		totalJun = Numbers.formatDouble(totalJun);
		totalJul = Numbers.formatDouble(totalJul);
		totalAug = Numbers.formatDouble(totalAug);
		totalSep = Numbers.formatDouble(totalSep);
		totalOct = Numbers.formatDouble(totalOct);
		totalNov = Numbers.formatDouble(totalNov);
		totalDec = Numbers.formatDouble(totalDec);
		Collections.reverse(pens);
	}
	
	 public void onCellEdit(CellEditEvent event) {
	        Object oldValue = event.getOldValue();
	        Object newValue = event.getNewValue();
	        System.out.println("Old Value   "+ event.getOldValue()); 
	        System.out.println("New Value   "+ event.getNewValue());
	 }       

	public void save(){
		if(getPenaltyTableData()!=null){
			PenaltyTable pen = getPenaltyTableData();
			pen.setIsActive(1);
			pen.setUserDtls(Login.getUserLogin().getUserDtls());
			pen.save();
		}
		setPenaltyTableData(null);
		init();
	}
	
	public void addNew(){
		System.out.println("add new");
		PenaltyTable pen = new PenaltyTable();
		if(pens!=null && pens.size()>0){
			System.out.println("added create default value");
			pen = createDefaultPenalty();
			pen = PenaltyTable.save(pen);
			pens.add(pen);
		}else{
			System.out.println("create new default value");
			pens = Collections.synchronizedList(new ArrayList<>());
			pen = createDefaultPenalty();
			pen = PenaltyTable.save(pen);
			pens.add(pen);
		}
		init();
	}
	
	public void copyPasteCell(){
		if(getPenaltyTableData()!=null){
			int cnt = pens.size() + 1;
			PenaltyTable p = getPenaltyTableData();
			PenaltyTable pen = new PenaltyTable();
			pen.setYear(DateUtils.getCurrentYear());
			pen.setJanuary(p.getJanuary());
			pen.setFebruary(p.getFebruary());
			pen.setMarch(p.getMarch());
			pen.setApril(p.getApril());
			pen.setMay(p.getMay());
			pen.setJune(p.getJune());
			pen.setJuly(p.getJuly());
			pen.setAugust(p.getAugust());
			pen.setSeptember(p.getSeptember());
			pen.setOctober(p.getOctober());
			pen.setNovember(p.getNovember());
			pen.setDecember(p.getDecember());
			pen.setIsActive(1);
			pen.setUserDtls(Login.getUserLogin().getUserDtls());
			pen = PenaltyTable.save(pen);
			pen.setCnt(cnt);
			pens.add(pen);
			init();
		}
	}
	
	public void deleteCell(){
		if(pens.size()>0){
			pens.remove(getPenaltyTableData());
			getPenaltyTableData().delete();
		}
	}
	
	public PenaltyTable createDefaultPenalty(){
		PenaltyTable pen = new PenaltyTable();
		pen.setYear(DateUtils.getCurrentYear());
		pen.setJanuary(0.0);
		pen.setFebruary(0.0);
		pen.setMarch(0.0);
		pen.setApril(0.08);
		pen.setMay(0.10);
		pen.setJune(0.12);
		pen.setJuly(0.14);
		pen.setAugust(0.16);
		pen.setSeptember(0.18);
		pen.setOctober(0.20);
		pen.setNovember(0.22);
		pen.setDecember(0.24);
		pen.setIsActive(1);
		pen.setUserDtls(Login.getUserLogin().getUserDtls());
		return pen;
	}
	
}
