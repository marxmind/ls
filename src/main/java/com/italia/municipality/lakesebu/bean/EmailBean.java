package com.italia.municipality.lakesebu.bean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.event.TabChangeEvent;

import com.italia.municipality.lakesebu.controller.Email;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.UserDtls;
import com.italia.municipality.lakesebu.enm.AppConf;
import com.italia.municipality.lakesebu.enm.EmailType;
import com.italia.municipality.lakesebu.utils.Application;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.ToString;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 04/30/2018
 *
 */
@Named
@ViewScoped
@Data
@ToString
public class EmailBean implements Serializable{
	
private static final long serialVersionUID = 1678900989767L;
	
	private boolean displayMsg;
	private String totalMsg;
	private String total;
	private String styleButton;
	
	private List<Email> mails;
	
	private String[] bLabels;
	private boolean[] buttons = new boolean[10];
	private boolean editorToolbar;
	
	private Email emailSelected;
	private String contentMsg;
	
	private List selectedSendUsers;
	private List sendUsers;
	private String title;
	
	private boolean sendToEnable = true;
	private boolean titleEnable = true;
	private boolean contentEmailEnable = true;
	
	private String tabLanded;
	
	private String attachment;
	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
	
	private final String EMAIL_FOLDER = AppConf.PRIMARY_DRIVE.getValue() + File.separator + 
			AppConf.APP_CONFIG_FOLDER_NAME.getValue() + File.separator + "email" + File.separator;
	
	private final String EMAIL_TMP_FOLDER = EMAIL_FOLDER + "tmpemail" + File.separator;
	
	public UserDtls getUser() {
		return Login.getUserLogin().getUserDtls();
	}
	
	@PostConstruct
	public void init() {
		tabLanded = EmailType.INBOX.getName();
		
		sendUsers = new ArrayList<>();

		for(UserDtls u : UserDtls.retrieve("SELECT * FROM userdtls WHERE isactive=1", new String[0])) {
			sendUsers.add(new SelectItem(u.getUserdtlsid(), u.getFirstname() + " " + u.getLastname()));
		}
		
		load(EmailType.INBOX.getName());
		//loadCountEmailNote();//transferred to menuBean
		if(mails!=null && mails.size()>0) {
			buttonReset(false, true, true, false, true, false, false, true, true, false);
			Email e = mails.get(0);
			e.setReadDate(DateUtils.getCurrentDateYYYYMMDD());
			e.setIsOpen(1);
			e.setType(2);//read
			e.save();
			readMail(e);
			setEmailSelected(e);
			
		}else {
			buttonReset(false, true, true, true, true, true, true, true, true, true);
			setEmailSelected(null);
			clearFields();
		}
		setTabLanded(EmailType.INBOX.getName());
	}
	
	/**
	 * 
	 * @param newDoc
	 * @param edit
	 * @param cancel
	 * @param delete
	 * @param draft
	 * @param reply
	 * @param replyAll
	 * @param send
	 * @param recall
	 * @param print
	 */
	private void buttonReset(boolean newDoc, boolean edit, boolean cancel, boolean delete, boolean draft, boolean reply, boolean replyAll, boolean send, boolean recall, boolean print) {
		
		boolean[] button = {newDoc,edit,cancel,delete,draft,reply,replyAll,send,recall,print};
		
		for(int i=0; i<=9; i++) {
			buttons[i] = button[i];
		}
	}
	
	public void clearFields() {
		setContentMsg(null);
		//setEmailSelected(null);
		setSelectedSendUsers(null);
		setTitle(null);
	}
	
	public void clickEmail(Email e, String tab) {
		
		
		
		if(EmailType.INBOX.getName().equalsIgnoreCase(tab)) {
			
			buttonReset(false, true, true, false, true, false, false, true, true, false);
			setEmailSelected(e);
			
			e.setReadDate(DateUtils.getCurrentDateYYYYMMDD());
			e.setIsOpen(1);
			e.setType(2);//read
			e.save();
			
			readMail(e);
			//emailToUpdate(e);
			//setTitle(e.getTitle());
			//String text = Email.readEmail(e.getContendId());
			//setContentMsg(text);
			
			loadCountEmailNote();
			
			
		}else if(EmailType.OUTBOX.getName().equalsIgnoreCase(tab)) {
			
			buttonReset(false, true, true, false, true, false, false, true, true, false);
			setEmailSelected(e);
			
			readMail(e);
			/*emailToUpdate(e);
			setTitle(e.getTitle());
			
			String text = Email.readEmail(e.getContendId());
			setContentMsg(text);*/
			
		}else if(EmailType.SEND.getName().equalsIgnoreCase(tab)) {
			
			buttonReset(false, true, true, false, true, true, true, true, false, false);
			setEmailSelected(e);
			
			readMail(e);
			/*emailToUpdate(e);
			setTitle(e.getTitle());
			
			String text = Email.readEmail(e.getContendId());
			setContentMsg(text);*/
			
		}else if(EmailType.DRAFT.getName().equalsIgnoreCase(tab)) {
			
			buttonReset(false, false, true, false, true, true, true, false, true, true);
			setEmailSelected(e);
			
			readMailDraft(e);
			/*emailToUpdateRecall(e);
			setTitle(e.getTitle());
			
			String text = Email.readEmail(e.getContendId());
			setContentMsg(text);*/
			
		}else if(EmailType.DELETED.getName().equalsIgnoreCase(tab)) {
			
			buttonReset(false, true, true, true, true, true, true, true, true, false);
			setEmailSelected(e);
			
			readMail(e);
			/*emailToUpdate(e);
			setTitle(e.getTitle());
			
			String text = Email.readEmail(e.getContendId());
			setContentMsg(text);*/
			
		}
		
	}
	
	public void emailToUpdateOne(Email eReply) {
		
		List recepients = new ArrayList<>();
		UserDtls fromUser = UserDtls.getUser(eReply.getFromEmail());
		recepients.add(fromUser.getUserdtlsid());
		setSelectedSendUsers(recepients);
		
	}
	
	public void emailToUpdate(Email eReply) {
		
		
		
		String logUserId = getUser().getUserdtlsid()+"";
		boolean isMore = false;
		try {
			String[] em = eReply.getToEmail().split(":");
			isMore=true;
	    }catch(Exception ex) {}
		
		
		
		List recepients = new ArrayList<>();
		
		String sendToMailNames = "";
		if(isMore) {
			int cnt = 1;
			boolean isMany = false;
			for(String userId : eReply.getToEmail().split(":")) {
				
				if(!logUserId.equalsIgnoreCase(userId)) {
					UserDtls fromUser = UserDtls.getUser(userId);
					if(cnt>1) {
						sendToMailNames += ", " + fromUser.getFirstname() + " " + fromUser.getLastname();
						recepients.add(fromUser.getUserdtlsid());
					}else {
						sendToMailNames = fromUser.getFirstname() + " " + fromUser.getLastname();
						recepients.add(fromUser.getUserdtlsid());
					}
					cnt++;
					isMany = true;
				}
				
			}
			if(sendToMailNames.isEmpty()) {
				UserDtls fromUser = UserDtls.getUser(eReply.getFromEmail());
				sendToMailNames = fromUser.getFirstname() + " " + fromUser.getLastname();
				recepients.add(fromUser.getUserdtlsid());
			}else {
				UserDtls fromUser = UserDtls.getUser(eReply.getFromEmail());
				if(isMany) {
					sendToMailNames += ", "+fromUser.getFirstname() + " " + fromUser.getLastname();
					recepients.add(fromUser.getUserdtlsid());
				}else {
					sendToMailNames = fromUser.getFirstname() + " " + fromUser.getLastname();
					recepients.add(fromUser.getUserdtlsid());
				}
			}
		}else {
			UserDtls fromUser = UserDtls.getUser(eReply.getFromEmail());
			sendToMailNames = fromUser.getFirstname() + " " + fromUser.getLastname();
			recepients.add(fromUser.getUserdtlsid());
		}
		
		
		setSelectedSendUsers(recepients);
		
		
	}
	
	public void emailToUpdateRecall(Email eReply) {
		
		
		
		String logUserId = getUser().getUserdtlsid()+"";
		boolean isMore = false;
		try {
			String[] em = eReply.getToEmail().split(":");
			isMore=true;
	    }catch(Exception ex) {}
		
		
		List recepients = new ArrayList<>();
		
		if(isMore) {
			int cnt = 1;
			
			for(String userId : eReply.getToEmail().split(":")) {
				
				if(!logUserId.equalsIgnoreCase(userId)) {
					UserDtls fromUser = UserDtls.getUser(userId);
					if(cnt>1) {
						recepients.add(fromUser.getUserdtlsid());
					}else {
						recepients.add(fromUser.getUserdtlsid());
					}
					cnt++;
					
				}
				
			}
			
		}else {
			UserDtls toUser = UserDtls.getUser(eReply.getToEmail());
			recepients.add(toUser.getUserdtlsid());
		}
		
		
		setSelectedSendUsers(recepients);
		
		
	}
	
	private void enableContent(boolean action) {
		setSendToEnable(action);
		setTitleEnable(action);
		setContentEmailEnable(action);
	}
	
	/*private void tabNotes() {
		
		if(EmailType.INBOX.getName().equals(getTabLanded())) {
			
		}else if(EmailType.OUTBOX.getName().equals(getTabLanded())) {
			
		}else if(EmailType.SEND.getName().equals(getTabLanded())) {
				
		}else if(EmailType.DRAFT.getName().equals(getTabLanded())) {
			
		}else if(EmailType.DELETED.getName().equals(getTabLanded())) {
		
		}		
	}*/
	
	public void clickButton(String buttonName) {
		
		if("New".equalsIgnoreCase(buttonName)) {
			
			clearFields();
			buttonReset(false, true, false, true, false, true, true, false, true, true);
			enableContent(false);
			
			String newMsg = "<p><strong>Hi,</strong></p><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>Best regards,<br/><p><strong>"+getUser().getFirstname()+" "+getUser().getLastname()+"</strong></p>";
			newMsg += "<p><strong>"+ getUser().getJob().getJobname() +"</strong></p>";
			newMsg += "<br/><br/>";
			setContentMsg(newMsg);
			setEditorToolbar(true);
			
		}else if("Edit".equalsIgnoreCase(buttonName)) {
			
			buttonReset(true, true, false, false, false, true, true, false, true, true);
			enableContent(false);
			setEditorToolbar(true);
			
			//load(getTabLanded());
			
			if(mails!=null && mails.size()>0){
				//Email em = mails.get(0);
				//setEmailSelected(em);
				//readMail(getEmailSelected());
				//readMailDraft(getEmailSelected());
				emailToUpdateRecall(getEmailSelected());
			}else{
				setEmailSelected(null);
				clearFields();
				buttonReset(false, true, true, true, true, true, true, true, true, true);
			}
			
		}else if("Cancel".equalsIgnoreCase(buttonName)) {
			
			
			//buttonReset(false, true, true, true, true, true, true, true, true, true);
			buttonReset(false, true, true, false, true, false, false, true, true, false);
			enableContent(true);
			setEditorToolbar(false);
			
			if(mails!=null && mails.size()>0){
				Email em = mails.get(0);
				setEmailSelected(em);
				readMail(em);
			}else{
				setEmailSelected(null);
				clearFields();
				buttonReset(false, true, true, true, true, true, true, true, true, true);
			}
				
			
		}else if("Delete".equalsIgnoreCase(buttonName)) {
			
			if(getEmailSelected()!=null) {
				
			
			//buttonReset(false, true, true, true, true, true, true, true, true, true);
			buttonReset(false, true, true, false, true, false, false, true, true, false);	
			enableContent(true);
			setEditorToolbar(false);
			
			Email el = getEmailSelected();
			el.setType(5);
			el.setIsDeleted(1);
			el.save();
			
			load(getTabLanded());
			
			if(mails!=null && mails.size()>0){
				Email em = mails.get(0);
				setEmailSelected(em);
				readMail(em);
			}else{
				setEmailSelected(null);
				clearFields();
				buttonReset(false, true, true, true, true, true, true, true, true, true);
			}
			
			Application.addMessage(1, "Success", "Email has been successfully deleted.");
			
			}else {
				Application.addMessage(3, "Error", "No email has been selected for deletion.");
			}
			
		}else if("Draft".equalsIgnoreCase(buttonName)) {
			
			
			
			boolean isOk = true;
			
			if(getSelectedSendUsers()==null || getSelectedSendUsers().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide recepient");
				isOk = false;
			}
			
			if(getTitle()==null || getTitle().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide Title of email");
				isOk = false;
			}
			
			if(isOk) {
				
				buttonReset(false, false, false, false, true, true, true, true, true, true);
				enableContent(true);
				setEditorToolbar(false);
				
				saveAsDraft();
				
				load(getTabLanded());
				
				if(mails!=null && mails.size()>0){
					Email em = mails.get(0);
					setEmailSelected(em);
					readMail(em);
				}else{
					setEmailSelected(null);
					clearFields();
					buttonReset(false, true, true, true, true, true, true, true, true, true);
				}
			}
		}else if("Reply".equalsIgnoreCase(buttonName)) {
			
			buttonReset(true, true, false, true, false, true, true, false, true, true);
			enableContent(false);
			setEditorToolbar(true);
			
			Email em = getEmailSelected();
			emailToUpdateOne(em);
			setTitle("RE: "+em.getTitle());
			String text = Email.readEmail(em.getContendId());
			String newMsg = "<p><strong>Hi "+ em.getFromNames() +",</strong></p><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>Best regards,<br/><p><strong>"+getUser().getFirstname()+" "+getUser().getLastname()+"</strong></p>";
			newMsg += "<p><strong>"+ getUser().getJob().getJobname() +"</strong></p>";
			newMsg += "<br/><br/>";
			newMsg +="<p>To: <strong>" + em.getToNames() + "</strong><p>";
			newMsg +="<p>From: <strong>" + em.getFromNames() + "</strong><p>";
			newMsg +="<p>Sent: <strong>" + em.getTimestamp() + "</strong><p>";
			newMsg +="<p>Title: <strong>" + em.getTitle() + "</strong><p>";
			newMsg +="<br/></br>";
			newMsg +=text;
			setContentMsg(newMsg);
			
		}else if("ReplyAll".equalsIgnoreCase(buttonName)) {
			
			buttonReset(true, true, false, true, false, true, true, false, true, true);
			enableContent(false);
			setEditorToolbar(true);
			
			Email em = getEmailSelected();
			emailToUpdate(em);
			setTitle("RE: "+em.getTitle());
			String text = Email.readEmail(em.getContendId());
			String newMsg = "<p><strong>Hi "+ em.getFromNames() +",</strong></p><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>Best regards,<br/><p><strong>"+getUser().getFirstname()+" "+getUser().getLastname()+"</strong></p>";
			newMsg += "<p><strong>"+ getUser().getJob().getJobname() +"</strong></p>";
			newMsg += "<br/><br/>";
			newMsg +="<p>To: <strong>" + em.getToNames() + "</strong><p>";
			newMsg +="<p>From: <strong>" + em.getFromNames() + "</strong><p>";
			newMsg +="<p>Sent: <strong>" + em.getTimestamp() + "</strong><p>";
			newMsg +="<p>Title: <strong>" + em.getTitle() + "</strong><p>";
			newMsg +="<br/></br>";
			newMsg +=text;
			setContentMsg(newMsg);
			
		}else if("Send".equalsIgnoreCase(buttonName)) {
			
			boolean isOk = true;
			
			if(getSelectedSendUsers()==null || getSelectedSendUsers().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide recepient");
				isOk = false;
			}
			
			if(getTitle()==null || getTitle().isEmpty()) {
				Application.addMessage(3, "Error", "Please provide Title of email");
				isOk = false;
			}
			
			if(isOk) {
				send();
				//buttonReset(false, true, true, true, true, true, true, true, true, true);
				buttonReset(false, true, true, false, true, false, false, true, true, false);
				Application.addMessage(1, "Success", "Email has been successfully send.");
				setEditorToolbar(false);
				
				load(getTabLanded());
				
				if(mails!=null && mails.size()>0){
					Email em = mails.get(0);
					setEmailSelected(em);
					readMail(em);
				}else{
					setEmailSelected(null);
					clearFields();
					buttonReset(false, true, true, true, true, true, true, true, true, true);
				}
				
			}
			
		}else if("Recall".equalsIgnoreCase(buttonName)) {
			
			buttonReset(true, false, false, false, false, true, true, false, true, true);
			enableContent(false);
			setEditorToolbar(true);
			
			recallEmail();
			
		}else if("Print".equalsIgnoreCase(buttonName)) {
			
			buttonReset(false, true, true, true, true, true, true, true, true, true);
			enableContent(true);
			setEditorToolbar(false);
			
		}
		
	}
	
	private void send() {
		
		int cnt = 1;
		String sendToMail = "";
		try {
			for(Object obj : getSelectedSendUsers()) {
				long userId = (Long)obj;
				if(cnt>1) {
					sendToMail += ":" + userId;
				}else {
					sendToMail = userId+"";
				}
				cnt++;
			}
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("catch email sendToMail=" + sendToMail);
			//only for draft send without edit
			boolean isMoreDraft = false;
			if(getEmailSelected()!=null) {
				
				try {
					String[] em = getEmailSelected().getToEmail().split(":");
					isMoreDraft=true;
			    }catch(Exception ex) {}
			}
			if(isMoreDraft) {
				for(String userId : getEmailSelected().getToEmail().split(":")) {
					
					if(cnt>1) {
						sendToMail += ":" + userId;
					}else {
						sendToMail = userId;
					}
					cnt++;
				}
			}else {
				sendToMail = getEmailSelected().getToEmail();
			}
		}
		
		System.out.println("catch email no catch =" + sendToMail);
		
		boolean isMore = false;
		try {
			String[] em = sendToMail.split(":");
			isMore=true;
	    }catch(Exception ex) {}
		
		if(isMore) {
			for(String sendTo : sendToMail.split(":")) {
				Email e = new Email();
				e.setSendDate(DateUtils.getCurrentDateYYYYMMDD());
				e.setTitle(getTitle());
				
				e.setType(EmailType.INBOX.getId());
				e.setIsOpen(0);
				e.setIsDeleted(0);
				e.setIsActive(1);
				
				e.setToEmail(sendToMail);
				e.setPersonCopy(Long.valueOf(sendTo));
				e.setFromEmail(getUser().getUserdtlsid()+"");
				
				
				String fileName = DateUtils.getCurrentDateMMDDYYYYTIMEPlain() + "-" + sendTo;
				Email.emailSavePath(getContentMsg(), fileName);
				e.setContendId(fileName);
				e.save();
			}
			
			//sender copy
			Email e = new Email();
			if(getEmailSelected()!=null) {
				e = getEmailSelected();
				Email.deleteEmail(e.getContendId()+".tis");
				Email.deleteEmail(e.getContendId()+".html");
				Email.deleteEmailWeb(e.getContendId()+".html");
			}
			e.setSendDate(DateUtils.getCurrentDateYYYYMMDD());
			e.setTitle(getTitle());
			
			e.setType(EmailType.SEND.getId());
			e.setIsOpen(0);
			e.setIsDeleted(0);
			e.setIsActive(1);
			
			e.setToEmail(sendToMail);
			e.setPersonCopy(getUser().getUserdtlsid());
			e.setFromEmail(getUser().getUserdtlsid()+"");
			
			
			String fileName = DateUtils.getCurrentDateMMDDYYYYTIMEPlain() + "-" + getUser().getUserdtlsid();
			Email.emailSavePath(getContentMsg(), fileName);
			e.setContendId(fileName);
			e.save();
		}else {
			//receiver copy
			Email e = new Email();
			e.setSendDate(DateUtils.getCurrentDateYYYYMMDD());
			e.setTitle(getTitle());
			
			e.setType(EmailType.INBOX.getId());
			e.setIsOpen(0);
			e.setIsDeleted(0);
			e.setIsActive(1);
			
			e.setToEmail(sendToMail);
			e.setPersonCopy(Long.valueOf(sendToMail));
			e.setFromEmail(getUser().getUserdtlsid()+"");
			
			
			String fileName = DateUtils.getCurrentDateMMDDYYYYTIMEPlain() + "-" + sendToMail;
			Email.emailSavePath(getContentMsg(), fileName);
			e.setContendId(fileName);
			e.save();
			
			//sender copy
			e = new Email();
			if(getEmailSelected()!=null) {
				e = getEmailSelected();
				Email.deleteEmail(e.getContendId()+".tis");
				Email.deleteEmail(e.getContendId()+".html");
				Email.deleteEmailWeb(e.getContendId()+".html");
			}
			e.setSendDate(DateUtils.getCurrentDateYYYYMMDD());
			e.setTitle(getTitle());
			
			e.setType(EmailType.SEND.getId());
			e.setIsOpen(0);
			e.setIsDeleted(0);
			e.setIsActive(1);
			
			e.setToEmail(sendToMail);
			e.setPersonCopy(getUser().getUserdtlsid());
			e.setFromEmail(getUser().getUserdtlsid()+"");
			
			
			fileName = DateUtils.getCurrentDateMMDDYYYYTIMEPlain() + "-" + getUser().getUserdtlsid();
			Email.emailSavePath(getContentMsg(), fileName);
			e.setContendId(fileName);
			e.save();
		}
	
		clearFields();
		
	}
	
	public void saveAsDraft() {
		int cnt = 1;
		String sendToMail = "";
		for(Object obj : getSelectedSendUsers()) {
			long userId = (Long)obj;
			if(cnt>1) {
				sendToMail += ":" + userId;
			}else {
				sendToMail = userId+"";
			}
			cnt++;
		}
		
		boolean isMore = false;
		try {
			String[] em = sendToMail.split(":");
			isMore=true;
	    }catch(Exception ex) {}
		
		//sender copy
		if(getEmailSelected()!=null) {
			Email e = getEmailSelected();
			e.setSendDate(null);
			e.setTitle(getTitle());
			e.setToEmail(sendToMail);
			e.setType(EmailType.DRAFT.getId());
			
			String fileName = e.getContendId();
			Email.emailSavePath(getContentMsg(), fileName);
			e.setContendId(fileName);
			e.save();
		}else {
			Email e = new Email();
			e.setSendDate(null);
			e.setTitle(getTitle());
			
			e.setType(EmailType.DRAFT.getId());
			e.setIsOpen(0);
			e.setIsDeleted(0);
			e.setIsActive(1);
			
			e.setToEmail(sendToMail);
			e.setPersonCopy(getUser().getUserdtlsid());
			e.setFromEmail(getUser().getUserdtlsid()+"");
			
			
			String fileName = DateUtils.getCurrentDateMMDDYYYYTIMEPlain() + "-" + getUser().getUserdtlsid();
			Email.emailSavePath(getContentMsg(), fileName);
			e.setContendId(fileName);
			e.save();
		}
		clearFields();
		Application.addMessage(1, "Success", "Email has been successfully save as draft");
	}
	
	public void loadCountEmailNote() {
		String sql = " AND msgtype=1 AND isopen=0 AND isdeleted=0 AND personcpy=?";
		String[] params = new String[1];
		params[0] = getUser().getUserdtlsid()+"";
		int count = Email.countNewEmail(sql, params);
		if(count>0) {
			displayMsg = true;
			setDisplayMsg(true);
			setTotalMsg(count+" messages.");
			setTotal(count+"");
			setStyleButton("color:red;");
		}else {
			displayMsg = false;
			setStyleButton("color:black;");
			setTotalMsg(count+" message.");
			setTotal(count+"");
		}
	}
	
	public void recallEmail() {
		Email em = getEmailSelected();
		//long userID = getUser().getUserdtlsid();
		String sql = " AND msgtoempid=? AND msgfromempid=? AND msgsenddate=? AND msgtitle=?";
		String[] params = new String[4];
		params[0] = em.getToEmail();
		params[1] = em.getFromEmail();
		params[2] = em.getSendDate();
		params[3] = em.getTitle();
		
		for(Email e : Email.retrieve(sql, params)) {
			if(e.getReadDate()==null) {
				e.delete();
			}else {
				UserDtls fromUser = UserDtls.getUser(e.getPersonCopy()+"");
				Application.addMessage(3, "Error",  "Failed to recalling email from " + fromUser.getFirstname() + " " + fromUser.getLastname());
			}
		}
		
		emailToUpdateRecall(em);
		setTitle(em.getTitle());
		String text = Email.readEmail(em.getContendId());
		setContentMsg(text);
		
		load(getTabLanded());
	}
	
	private void readMail(Email em) {
		try {
		//Email e = mails.get(0);
		//setEmailSelected(e);
		emailToUpdate(em);
		setTitle(em.getTitle());
		String text = Email.readEmail(em.getContendId());
		setContentMsg(text);
		copyFileToWebPath(em);
		}catch(Exception e) {}
	}
	
	private void readMailDraft(Email em) {
		try {
		emailToUpdateRecall(em);
		setTitle(em.getTitle());
		String text = Email.readEmail(em.getContendId());
		setContentMsg(text);
		}catch(Exception e) {}
	}
	
	public void onChange(TabChangeEvent event) {
		
		enableContent(true);
		
		
		if("Inbox".equalsIgnoreCase(event.getTab().getTitle())) {
			load(EmailType.INBOX.getName());
			
			buttonReset(false, true, true, false, true, false, false, true, true, false);
			
			if(mails!=null && mails.size()>0){
				Email e = mails.get(0);
				e.setReadDate(DateUtils.getCurrentDateYYYYMMDD());
				e.setIsOpen(1);
				e.setType(2);//read
				e.save();
				setEmailSelected(e);
				readMail(e);
			}else{
				setEmailSelected(null);
				clearFields();
				buttonReset(false, true, true, true, true, true, true, true, true, true);
			}
			
			setTabLanded(EmailType.INBOX.getName());
			
		}else if("Outbox".equalsIgnoreCase(event.getTab().getTitle())) {
			
			setTabLanded(EmailType.OUTBOX.getName());
			
			load(EmailType.OUTBOX.getName());
			buttonReset(false, true, true, false, true, false, false, true, true, false);
			if(mails!=null && mails.size()>0){
				Email e = mails.get(0);
				setEmailSelected(e);
				readMail(e);
			}else{
				setEmailSelected(null);
				clearFields();
				buttonReset(false, true, true, true, true, true, true, true, true, true);
			}
		}else if("Sent".equalsIgnoreCase(event.getTab().getTitle())) {
			
			setTabLanded(EmailType.SEND.getName());
			
			load(EmailType.SEND.getName());
			buttonReset(false, true, true, false, true, true, true, true, false, false);
			if(mails!=null && mails.size()>0){
				Email e = mails.get(0);
				setEmailSelected(e);
				readMail(e);
			}else{
				setEmailSelected(null);
				clearFields();
				buttonReset(false, true, true, true, true, true, true, true, true, true);
			}
		}else if("Draft".equalsIgnoreCase(event.getTab().getTitle())) {
			
			setTabLanded(EmailType.DRAFT.getName());
			
			load(EmailType.DRAFT.getName());
			buttonReset(false, false, true, false, true, true, true, false, true, true);
			if(mails!=null && mails.size()>0){
				Email e = mails.get(0);
				setEmailSelected(e);
				readMailDraft(e);
			}else{
				setEmailSelected(null);
				clearFields();
				buttonReset(false, true, true, true, true, true, true, true, true, true);
			}
		}else if("Trash".equalsIgnoreCase(event.getTab().getTitle())) {
			
			setTabLanded(EmailType.DELETED.getName());
			
			load(EmailType.DELETED.getName());
			buttonReset(false, true, true, true, true, true, true, true, true, false);
			if(mails!=null && mails.size()>0){
				Email e = mails.get(0);
				setEmailSelected(e);
				readMail(e);
			}else{
				setEmailSelected(null);
				clearFields();
				buttonReset(false, true, true, true, true, true, true, true, true, true);
			}
		}
	}
	
	public void load(String tab) {
		
		if(EmailType.INBOX.getName().equalsIgnoreCase(tab)) {
			
			String sql = " AND msgtype=1 AND isopen=0 AND isdeleted=0 AND personcpy=? ORDER BY timestampmsg DESC";
			String[] params = new String[1];
			params[0] = getUser().getUserdtlsid()+"";
			loadEmails(sql,params);
			
		}else if(EmailType.OUTBOX.getName().equalsIgnoreCase(tab)) {
			
			String sql = " AND msgtype=2 AND isopen=1 AND isdeleted=0 AND personcpy=? ORDER BY timestampmsg DESC";
			String[] params = new String[1];
			params[0] = getUser().getUserdtlsid()+"";
			loadEmails(sql,params);
			
		}else if(EmailType.SEND.getName().equalsIgnoreCase(tab)) {
			
			String sql = " AND msgtype=3 AND personcpy=? ORDER BY timestampmsg DESC";
			String[] params = new String[1];
			params[0] = getUser().getUserdtlsid()+"";
			loadComposeEmails(sql,params);
			
		}else if(EmailType.DRAFT.getName().equalsIgnoreCase(tab)) {
			
			String sql = " AND msgtype=4 AND personcpy=? ORDER BY timestampmsg DESC";
			String[] params = new String[1];
			params[0] = getUser().getUserdtlsid()+"";
			loadComposeEmails(sql,params);
			
		}else if(EmailType.DELETED.getName().equalsIgnoreCase(tab)) {
			
			String sql = " AND msgtype=5 AND isdeleted=1 AND personcpy=? ORDER BY timestampmsg DESC LIMIT 100";
			String[] params = new String[1];
			params[0] = getUser().getUserdtlsid()+"";
			loadEmails(sql,params);
			
		}
		
		
	}
	
	
	
	private void loadEmails(String sql, String[] params) {
		mails = new ArrayList<Email>();
		
		UserDtls logUser = getUser();
		for(Email e : Email.retrieve(sql, params)) {
			
			boolean isMore = false;
			try {
				String[] em = e.getToEmail().split(":");
				isMore=true;
		    }catch(Exception ex) {}
			
			if(isMore) {
				String[] to = e.getToEmail().split(":");
				String names = "";
				int cntname = 1;
				for(String v : to) {
						UserDtls fromUser = UserDtls.getUser(v);
						if(cntname>1) {
							names += ", " + fromUser.getFirstname() + " " + fromUser.getLastname();
						}else {
							names += fromUser.getFirstname() + " " + fromUser.getLastname();
						}
						cntname++;
				}
				
				if("0".equalsIgnoreCase(e.getFromEmail())) {
					e.setFromNames("System Generated Email [DO NOT REPLY]");
				}else {
					UserDtls fromUser = UserDtls.getUser(e.getFromEmail());
					e.setFromNames(fromUser.getFirstname() + " " + fromUser.getLastname());
				}
				
				e.setToNames(names);
				
				e = conCutInfo(e);
				
				mails.add(e);
			}else {
					if("0".equalsIgnoreCase(e.getFromEmail())) {
						e.setFromNames("System Generated Email [DO NOT REPLY]");
					}else {
						UserDtls fromUser = UserDtls.getUser(e.getFromEmail());
						e.setFromNames(fromUser.getFirstname() + " " + fromUser.getLastname());
					}
					
					e.setToNames(logUser.getFirstname() + " " + logUser.getLastname());
					
					e = conCutInfo(e);
					
					mails.add(e);
			}
			
		}
		
		//Collections.reverse(mails);
		
	}
	
	private void loadComposeEmails(String sql, String[] params) {
		mails = new ArrayList<Email>();
		UserDtls logUser = getUser();
		for(Email e : Email.retrieve(sql, params)) {
					
			boolean isMore = false;
			try {
				String[] em = e.getToEmail().split(":");
				isMore=true;
		    }catch(Exception ex) {}
			
					String toEmail = "";
					if(isMore) {
						String[] to = e.getToEmail().split(":");
						int cnt = 1;
						for(String v : to) {
							if(cnt>1) {
								UserDtls toUser = UserDtls.getUser(v);
								toEmail += ", "+ toUser.getFirstname() + " " + toUser.getLastname();
							}else {
								UserDtls toUser = UserDtls.getUser(v);
								toEmail = toUser.getFirstname() + " " + toUser.getLastname();
							}
							cnt++;
						}
						e.setToNames(toEmail);
					}else {
						UserDtls fromUser = UserDtls.getUser(e.getToEmail());
						e.setToNames(fromUser.getFirstname() + " " + fromUser.getLastname());
					}
					
					e.setFromNames(logUser.getFirstname() + " " + logUser.getLastname());
					e = conCutInfo(e);
					mails.add(e);
				
			
			
		}
		//Collections.reverse(mails);
	}
	
	private Email conCutInfo(Email email){
		
		int titleCount = email.getTitle().length();
		int toNamesCount = email.getToNames().length();
		int fromNamesCount = email.getFromNames().length();
		if(titleCount>20) {
			email.setTitleCut(email.getTitle().substring(0,19) + "...");
		}else {
			email.setTitleCut(email.getTitle());
		}
		if(toNamesCount>15) {
			email.setToNamesCut(email.getToNames().substring(0,14) + "...");
		}else {
			email.setToNamesCut(email.getToNames());
		}
		if(fromNamesCount>15) {
			email.setFromNamesCut(email.getFromNames().substring(0,14) + "...");
		}else {
			email.setFromNamesCut(email.getFromNames());
		}
		
		return email;
	}
	
	private void copyFileToWebPath(Email em){
		
		
		
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		
		String driveFile =  EMAIL_FOLDER +  em.getContendId() + ".html";
		File emailFile = new File(driveFile);
		String contextFileLoc = File.separator + "attachment" + File.separator;
        String pathToSave = externalContext.getRealPath("") + contextFileLoc;
        setAttachment(pathToSave);
        
		if(emailFile.exists()) {
			copyFile(driveFile, pathToSave);
		}
		
		File files =new File(EMAIL_TMP_FOLDER);
		for(File f : files.listFiles()) {
			copyFile(EMAIL_TMP_FOLDER + f.getName(), pathToSave);
			f.delete();
		}
	}
	
	private void copyFile(String fileLocation, String fileNewLocation) {
		 File file = new File(fileLocation);
         try{
 			Files.copy(file.toPath(), (new File(fileNewLocation + file.getName())).toPath(),
 			        StandardCopyOption.REPLACE_EXISTING);
 			}catch(IOException e){}
	}
	
	public void loadAttachment() {
		
		try{
			
			
  		 File file = new File(EMAIL_FOLDER, getAttachment() + ".html");
		 FacesContext faces = FacesContext.getCurrentInstance();
		 ExternalContext context = faces.getExternalContext();
		 HttpServletResponse response = (HttpServletResponse)context.getResponse();
			
	     BufferedInputStream input = null;
	     BufferedOutputStream output = null;
	     
	     try{
	    	 
	    	 // Open file.
	            input = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);

	            // Init servlet response.
	            response.reset();
	            response.setHeader("Content-Type", "application/pdf");
	            response.setHeader("Content-Length", String.valueOf(file.length()));
	            response.setHeader("Content-Disposition", "inline; filename=\"" + getAttachment() + ".html" + "\"");
	            output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);

	            // Write file contents to response.
	            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
	            int length;
	            while ((length = input.read(buffer)) > 0) {
	                output.write(buffer, 0, length);
	            }

	            // Finalize task.
	            output.flush();
	    	 
	     }finally{
	    	// Gently close streams.
	            close(output);
	            close(input);
	     }
	     
	     // Inform JSF that it doesn't need to handle response.
	        // This is very important, otherwise you will get the following exception in the logs:
	        // java.lang.IllegalStateException: Cannot forward after response has been committed.
	        faces.responseComplete();
	        
		}catch(Exception ioe){
			ioe.printStackTrace();
		}
		
	}
	
	private void close(Closeable resource) {
	    if (resource != null) {
	        try {
	            resource.close();
	        } catch (IOException e) {
	            // Do your thing with the exception. Print it, log it or mail it. It may be useful to 
	            // know that this will generally only be thrown when the client aborted the download.
	            e.printStackTrace();
	        }
	    }
	}
	public String[] getbLabels() {
		
		bLabels = new String[10];
		int i=0;
		bLabels[i++] = "New";
		bLabels[i++] = "Edit";
		bLabels[i++] = "Cancel";
		bLabels[i++] = "Delete";
		bLabels[i++] = "Save as Draft";
		bLabels[i++] = "Reply";
		bLabels[i++] = "Reply All";
		bLabels[i++] = "Send";
		bLabels[i++] = "Recall";
		bLabels[i++] = "Print";
		
		return bLabels;
	}
}
