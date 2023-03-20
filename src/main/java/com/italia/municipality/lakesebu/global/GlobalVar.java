package com.italia.municipality.lakesebu.global;

import java.io.File;

import com.italia.municipality.lakesebu.enm.AppConf;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 04/09/2020
 *
 */
public class GlobalVar {
	
	public static final String PRIMARY_DRIVE=System.getenv("SystemDrive");
	public static final double YEARLY = 12.0;
	public static final double CEDULA_DIVEDEND = 1000.00;
	public static final String APP_NAME =  AppConf.APP_CONFIG_FOLDER_NAME.getValue();// "webtis";
	public static final String SEP = File.separator;
	public static final String APP_CONF_DIR = "C:"+SEP+APP_NAME+SEP+"conf"+SEP;
	public static final String APP_DATABASE_CONF = "C:"+SEP+APP_NAME+SEP+"conf"+SEP+"dbconf.max";
	public static final String LOG_FOLDER = "C:"+SEP+APP_NAME+SEP+"log"+SEP;
	public static final String REPORT_FOLDER = "C:"+SEP+APP_NAME+SEP+"reports"+SEP;
	public static final boolean LOG_ENABLE = true;
	public static final String LICENSE_DATA_FILE = "C:"+SEP+APP_NAME+SEP+"conf"+SEP+"dbconf.max";
	public static final String LICENSE_FILE_NAME= "C:"+SEP+APP_NAME+SEP+"conf"+SEP+"data.max";
	public static final String LICENSE_EXP = "";
	public static final String UPLOADED_EXCEL_PATH_FOLDER = "C:"+SEP+APP_NAME+SEP+"upload"+SEP;
	public static final String BARANGAY_CODE = "MUN ";
	public static final String BARANGAY="Poblaction";
	public static final String MUNICIPALITY="Lake Sebu";
	public static final String PROVINCE="South Cotabato";
	public static final String OWNER_IMG_FOLDER="C:"+SEP+APP_NAME+SEP+"license"+SEP+"img"+SEP;
	public static final String OWNER_IMG_PATH="C:"+SEP+APP_NAME+SEP+"license"+SEP;
	public static final String WATER_RENTAL_CERTIFICATE_RPT="water_rental_certificate";
	public static final String TRANSPO="transpo_clearance";
	public static final String WATER_RENTAL_PAYMENT_HISTORY_RPT="water_rental_ors";
	public static final String WATER_RENTAL_PAYMENT_BILLING_RPT="water_rental_bill_statement";
	public static final String MTO_OR_CEDULA_SIGNATORY="FERDINAND L. LOPEZ";
	//public static final String CEDULA_LQ310_PRINT="cedula_LQ310";
	//public static final String CEDULA_FX2175_PRINT="cedula_FX2175";
	//public static final String OR51_LQ310_PRINT="OR51_LQ310";
	//public static final String OR51_FX2175_PRINT="OR51_FX2175";
	public static final String QRCODE_CITIZEN = "citizen-qrcode";
	public static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
	public static final String COUNTER_REPORT_FILE = "counter_report.tis";
	public static final String COLLECTOR_MODE_FILE="collector-mode.tis";
	public static final String UPLOAD_XML = "C:"+SEP+APP_NAME+SEP+"upload-xml"+SEP;
	public static final String COMMIT_XML = "C:"+SEP+APP_NAME+SEP+"commit-xml"+SEP;
	public static final String COMMIT_XML_ERROR = COMMIT_XML + "error" + SEP;
	public static final String COMMIT_XML_EXIST = COMMIT_XML+"exist"+SEP;
	public static final String COMMIT_XML_UNPROCESSED = COMMIT_XML+"unprocessed"+SEP;
	public static final String DOWNLOADED_DATA_FOLDER = "C:" + SEP + APP_NAME + SEP + "downloaded-data" + SEP ; //data from server
	public static final String COMPLETED_DATA_FOLDER = DOWNLOADED_DATA_FOLDER + "completed-data" + SEP ; //data from server
	public static final String EMPLOYEE_IMAGE_PATH = PRIMARY_DRIVE + SEP + APP_NAME + SEP + "employee-img" + SEP;
	public static final String EMPLOYEE_IMAGE_PATH_SIG = EMPLOYEE_IMAGE_PATH + "signature" + SEP;
	public static final String EMPLOYEE_IMAGE_PATH_PHOTO = EMPLOYEE_IMAGE_PATH + "photo" + SEP;
	public static final String EMPLOYEE_IMAGE_PATH_QRCODE = EMPLOYEE_IMAGE_PATH + "qrcode" + SEP;
	public static final String EMPLOYEE_PAYROLL = "payroll";
	public static final String EMPLOYEE_ID = "id";
	public static final String EMPLOYEE_ID_FOLDER = EMPLOYEE_IMAGE_PATH + "ID" + SEP; 
	public static final String CASH_DISBURSEMENT_NAME = "cash-disbursement";
	public static final String CHECK_ISSUED_REPORT_SHORT_NAME = "check-issued-report-short";
	public static final String CHECK_ISSUED_REPORT_LONG_NAME = "check-issued-report-long";
	public static final String CHECK_ISSUED_REPORT_LONG_EXTENDED_NAME = "check-issued-report-long-extended";
	public static final String COLLECTION_DEPOSIT_REPORT_SHORT_NAME = "collection-deposit-report-short";
	public static final String COLLECTION_DEPOSIT_REPORT_LONG_NAME = "collection-deposit-report-long";
	public static final String COLLECTION_DEPOSIT_REPORT_LONG_EXTENDED_NAME = "collection-deposit-report-long-extended";
	public static final String BUSINESS_MAP_IMG = PRIMARY_DRIVE + SEP + APP_NAME + SEP + "license" + SEP + "map" +SEP;
	public static final String RIS_SLIP_RPT = "ris";
	public static final String WATER_RENTAL_REG = "water_rental_reg";
	public static final String CHECK_XML_FOLDER=PRIMARY_DRIVE + SEP + APP_NAME + SEP + "check_xml" + SEP;
	public static final String CHECK_XML=CHECK_XML_FOLDER+SEP+"xml"+SEP;
	public static final String CHECK_XML_ERROR=CHECK_XML_FOLDER+SEP+"error"+SEP;
	public static final String CHECK_XML_UNPROCCESSED=CHECK_XML_FOLDER+SEP+"unproccessed"+SEP;
}
