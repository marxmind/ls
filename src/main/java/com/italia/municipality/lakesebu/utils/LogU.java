package com.italia.municipality.lakesebu.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;

import com.italia.municipality.lakesebu.controller.ReadConfig;
import com.italia.municipality.lakesebu.enm.AppConf;

/**
 * 
 * @author mark italia
 * @since 11/18/2016
 * @version 1.0
 *
 */
public class LogU {
	
	public static void add(Timestamp action){
		try{add(action+"");}catch(Exception e){add("null");}
	}
	
	public static void add(Double action){
		try{add(action+"");}catch(Exception e){add("null");}
	}
	
	public static void add(BigDecimal action){
		try{add(action+"");}catch(Exception e){add("null");}
	}
	
	public static void add(long action){
		try{add(action+"");}catch(Exception e){add("null");}
	}
	
	public static void add(int action){
		try{add(action+"");}catch(Exception e){add("null");}
	}
	
	public static void add(String action){
		
		if("yes".equalsIgnoreCase(ReadConfig.value(AppConf.APP_LOG_INCLUDE))){
		try{
			
		String FILE_LOG_NAME = "systemlog";
		String FILE_LOG_TMP_NAME = "tmpsystemlog";
		String EXT = ".log";
		
		
        String logpath = ReadConfig.value(AppConf.APP_LOG_PATH);
        
        String finalFile = logpath + FILE_LOG_NAME + "-" + DateUtils.getCurrentDateMMDDYYYYPlain() + EXT;
        String tmpFileName = logpath + FILE_LOG_TMP_NAME + "-" + DateUtils.getCurrentDateMMDDYYYYPlain() + EXT;
        
        File originalFile = new File(finalFile);
        
        //check log directory
        File logdirectory = new File(ReadConfig.value(AppConf.APP_LOG_PATH));
        if(!logdirectory.isDirectory()){
        	logdirectory.mkdir();
        }
        
        if(!originalFile.exists()){
        	originalFile.createNewFile();
        }
        
        BufferedReader br = new BufferedReader(new FileReader(originalFile));
        
        // Construct the new file that will later be renamed to the original
        // filename.
        File tempFile = new File(tmpFileName);
        PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
        
        
        String line = null;
        // Read from the original file and write to the new
        while ((line = br.readLine()) != null) {
            pw.println(line);
        }
        //added new data into file
        /*int i=1;
        for(String action : actions){
        	pw.println(DateUtils.getCurrentDateMMDDYYYYTIME() + "["+ i++ +"] " + action);
        }*/
        pw.println(DateUtils.getCurrentDateMMDDYYYYTIME() + action);	
        pw.flush();
        pw.close();
        br.close();

        // Delete the original file
        if (!originalFile.delete()) {
            System.out.println("Could not delete file");
            return;
        }

        // Rename the new file to the filename the original file had.
        if (!tempFile.renameTo(originalFile))
            System.out.println("Could not rename file");
		
		}catch(Exception e){e.getMessage();}
		}
	}
	
}
