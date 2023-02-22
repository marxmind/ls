package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;

import com.italia.municipality.lakesebu.controller.Email;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.UserDtls;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Named
@ViewScoped
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class NotiBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1456546578756L;
	private boolean displayMsg;
	private String totalMsg;
	private String total;
	private String styleButton;
	
	public UserDtls getUser() {
		return Login.getUserLogin().getUserDtls();
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
}
