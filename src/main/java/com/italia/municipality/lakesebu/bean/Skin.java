package com.italia.municipality.lakesebu.bean;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Properties;

import com.italia.municipality.lakesebu.enm.AppConf;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author mark italia
 * @since 11/11/2017
 * @version 1.0
 *
 */
@Named
@ViewScoped
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class Skin implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 548658541L;
	private String label;
	private String header;
	private String title;
	private String placeholder;
	private String textInput;
	private String table;
	private String panel;
	private String tabs;
	private String button;
	private String link;
	private String dialog;
	private String pagelayout;
	private String copyrightLabel;
	private String calendarSched;
	private String homeDirImgaes;
	private String homeImgaeProfile;
	private String msg;
	private String toolbar;
	private String pageTitle;
	private String menu;
	private String grid;
	private String select;
	private String calendar;
	private String chart;
	private String checkbox;
	private String searchButton;
	private String emailButton;
	private String sendToLabel;
	private String emailMenu;
	private String accordionPanelMain;
	
	private static final String PROPERTY_FILE = AppConf.PRIMARY_DRIVE.getValue() + AppConf.SEPERATOR.getValue() +
												AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() +	
												AppConf.APP_CONFIG_SETTING_FOLDER.getValue() + AppConf.SEPERATOR.getValue() + "skin.properties";
	
	@PostConstruct
	public void init(){
		new Skin();
		Properties prop = new Properties();
		try{
			prop.load(new FileInputStream(PROPERTY_FILE));
			setLabel(prop.getProperty("label"));
			setHeader(prop.getProperty("header"));
			setTitle(prop.getProperty("title"));
			setPlaceholder(prop.getProperty("placeholder"));
			setTextInput(prop.getProperty("textInput"));
			setTable(prop.getProperty("table"));
			setPanel(prop.getProperty("panel"));
			setTabs(prop.getProperty("tabs"));
			setButton(prop.getProperty("button"));
			setLink(prop.getProperty("link"));
			setDialog(prop.getProperty("dialog"));
			setPagelayout(prop.getProperty("pagelayout"));
			setCopyrightLabel(prop.getProperty("copyrightLabel"));
			setCalendarSched(prop.getProperty("calendarSched"));
			setHomeDirImgaes(prop.getProperty("homeDirImgaes"));
			setHomeImgaeProfile(prop.getProperty("homeImgaeProfile"));
			setMsg(prop.getProperty("msg"));
			setToolbar(prop.getProperty("toolbar"));
			setPageTitle(prop.getProperty("pageTitle"));
			setMenu(prop.getProperty("menu"));
			setGrid(prop.getProperty("grid"));
			setSelect(prop.getProperty("select"));
			setCalendar(prop.getProperty("calendar"));
			setChart(prop.getProperty("chart"));
			setCheckbox(prop.getProperty("checkbox"));
			setSearchButton(prop.getProperty("searchButton"));
			setEmailButton(prop.getProperty("emailButton"));
			setSendToLabel(prop.getProperty("sendToLabel"));
			setEmailMenu(prop.getProperty("emailMenu"));
			setAccordionPanelMain(prop.getProperty("accordionPanelMain"));
		}catch(Exception e){e.printStackTrace();}
	}
}
