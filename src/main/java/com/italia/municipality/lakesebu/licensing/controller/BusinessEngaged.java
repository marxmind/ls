package com.italia.municipality.lakesebu.licensing.controller;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.italia.municipality.lakesebu.enm.AppConf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author mark italia
 * @since 12/02/2017
 * @version 1.0
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class BusinessEngaged {
	
	private int id;
	private String name;
	
	private static final String BUSINESS_FILE = AppConf.PRIMARY_DRIVE.getValue() +  AppConf.SEPERATOR.getValue() + 
			AppConf.APP_CONFIG_FOLDER_NAME.getValue() + AppConf.SEPERATOR.getValue() +
			AppConf.APP_CONFIG_SETTING_FOLDER.getValue() + AppConf.SEPERATOR.getValue() +
			"BusinessEngaged.xml";
	
	public static List<BusinessEngaged> readBusinessEngagedXML(){
    	List<BusinessEngaged> lines = Collections.synchronizedList(new ArrayList<BusinessEngaged>());
    	try {
            File fXmlFile = new File(BUSINESS_FILE);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile); 
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize(); 
            NodeList nList = doc.getElementsByTagName("line");
            //System.out.println("----------------------------");

                for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
                    //System.out.println("\nCurrent Element :" + nNode.getNodeName());
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            
            Element eElement = (Element) nNode; 
            	
            BusinessEngaged line = new BusinessEngaged();
            	line.setId(Integer.valueOf(eElement.getAttribute("id")));
            	line.setName(eElement.getElementsByTagName("name").item(0).getTextContent());
            	
            	lines.add(line);
                }
            }
           } catch (Exception e) {
            e.printStackTrace();
           }
    	return lines;
    }
	
	public static BusinessEngaged businessName(int id){
    	BusinessEngaged line = new BusinessEngaged();
    	try {
            File fXmlFile = new File(BUSINESS_FILE);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile); 
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize(); 
            NodeList nList = doc.getElementsByTagName("line");
            //System.out.println("----------------------------");

                for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
                    //System.out.println("\nCurrent Element :" + nNode.getNodeName());
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            
            	Element eElement = (Element) nNode; 
            	int idLine = Integer.valueOf(eElement.getAttribute("id"));
            	if(id==idLine){
	            	line.setId(idLine);
	            	line.setName(eElement.getElementsByTagName("name").item(0).getTextContent());
	            	break;
            	}
                }
            }
           } catch (Exception e) {
            e.printStackTrace();
           }
    	return line;
    }
}
