package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.italia.municipality.lakesebu.security.License;

import jakarta.annotation.PostConstruct;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;

@Named
@ViewScoped
@Data
public class ExpiredBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 14567899874564556L;
	private List modules;
	private int moduleId;
	private String activationCode;
	private List<License> exes;
	private String message;
	@PostConstruct
	public void init() {
		exes = new ArrayList<>();
		String sql = "SELECT * FROM license";
		exes = License.retrieve(sql, new String[0]);
		
		modules = new ArrayList<>();
		
		for(com.italia.municipality.lakesebu.security.Module m : com.italia.municipality.lakesebu.security.Module.values()){
			modules.add(new SelectItem(m.getId(), m.getName()));
		}
		Random rand = new Random();
        int max=26,min=0;
		String[] msg = {
				"This is an RGE (resume generating event).",
				"This is a CLO (career limiting opportunity).",
				"This is an ID-10-T error.",
				"This is a PICNIC error. (Problem in chair, not in computer)",
				"It is ready to test",
				"This is a PEBCAK error – (Problems Existing Between Chair and Keyboard)",
				"On a scale of 1 to 10 how hard can you click?",
				"Please do the needful.",
				"No good deeds goes unpunished.",
				"Revert at your earliest convenience.",
				"There is no need to SHOUT.",
				"Have you tried turning it off and back on?",
				"Is it plugged in?",
				"If it’s not in the ticket, it didn’t happen.",
				"We’ve stepped in a pile of should.",
				"A raised hand is not a ticket",
				"No appointment = no work done.",
				"Your backup is only as good as your restore",
				"Please reboot.",
				"Self resolving ticket.",
				"If it’s worth doing, it’s worth automating.",
				"Lack of proper planning does not constitute an emergency.",
				"Please login when you’re sober.",
				"Prior planning prevents poor performance.",
				"The button on the side, is it glowing?",
				"I’ve got 99 problems but a switch ain’t one.",
				"That should be relatively straight forward."
			};
		int num = rand.nextInt(max - min + 1) + min;
		setMessage(msg[num]);
	}
	public String activate(){
		
		boolean isActivated = false;
		
		isActivated = License.activateLicenseCode(com.italia.municipality.lakesebu.security.Module.selected(getModuleId()), getActivationCode());
		if(isActivated){
			return "portal.xhtml";
		}
			
		
		return "expired.xhtml";
	}
}
