package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.event.CellEditEvent;

import com.italia.municipality.lakesebu.controller.BankAccounts;
import com.italia.municipality.lakesebu.controller.Budget;
import com.italia.municipality.lakesebu.controller.Chequedtls;
import com.italia.municipality.lakesebu.controller.IBudget;
import com.italia.municipality.lakesebu.controller.IBudgetType;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.UserAccessLevel;
import com.italia.municipality.lakesebu.enm.BudgetType;
import com.italia.municipality.lakesebu.utils.Currency;
import com.italia.municipality.lakesebu.utils.DateUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import lombok.Data;

@Named
@ViewScoped
@Data
public class BudgetBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1657568543265647L;

	private List<IBudget> budgets;
	private IBudget budgetData;
	private BigDecimal grandTotal;
	private String total;
	private double grandTotalUsed;
	private String totalUsed;
	private double grandTotalRem;
	private String totalRem;
	private String monthToday;
	
	@PostConstruct
	public void init(){
		loadBudget(BudgetType.MONTHLY);
		updateTotal();
	}

	
	private void loadBudget(BudgetType budgetType){
		String sql = "SELECT * FROM budget b, budgettype t, tbl_bankaccounts a WHERE b.bank_id = a.bank_id AND b.budtypeid=t.budtypeid AND t.budtypeid=? AND budisactive=1";
		String[] params = new String[1];
		params[0] = BudgetType.MONTHLY.getId()+"";
		List<IBudget> tmpbudgets = new ArrayList<IBudget>();
		tmpbudgets = Budget.retrieve(sql, params);
		if(tmpbudgets.size()<=0){
			//create budget based on the bank account table
			for(BankAccounts accounts : BankAccounts.retrieve("SELECT * FROM tbl_bankaccounts", new String[0])){
				tmpbudgets.add(createNewBudget(accounts, budgetType));
			}
		}
		budgets=new ArrayList<IBudget>();
		for(IBudget bud : tmpbudgets){
			budgets.add(computeAmount(bud));
		}
		
	}
	/**
	 * Compute used and remaining amount for IRA
	 * @param bud
	 * @return
	 */
	private IBudget computeAmount(IBudget bud){
		String fieldName = "amount";
		String sql = "SELECT sum(cheque_amount) as "+ fieldName +" FROM tbl_chequedtls WHERE  accnt_no=? AND (date_disbursement>=? and date_disbursement<=?) and isactive=1 and chkstatus=1";
		String dateFrom = "";
		String dateTo = "";
		int month = DateUtils.getCurrentMonth(); 
		int year = DateUtils.getCurrentYear();
		
		int dateToday = Integer.valueOf(DateUtils.getCurrentDateYYYYMMDD().split("-")[2]);
		if(bud.getCycleDate()>dateToday){
			month -=1;
		}
		
		dateFrom = year + "-" + (month<10? "0"+month : month) + "-" + (bud.getCycleDate()<10? "0" + bud.getCycleDate() : bud.getCycleDate());
		
		if(month==12){
			year +=1;
			month=1; 
			
			dateTo = year + "-" + (month<10? "0"+month : month) + "-" + (bud.getCycleDate()<10? "0" + bud.getCycleDate() : bud.getCycleDate());
			
		}else{
			month +=1; 
			int cycleDate = bud.getCycleDate() - 1;
			if(cycleDate==0){
				cycleDate=31;
				month -=1;
			}
			dateTo = year + "-" + (month<10? "0"+month : month) + "-" + (cycleDate<10? "0" + cycleDate : cycleDate);
		}
		
		
		String[] params = new String[3];
		params[0] = bud.getAccounts().getBankId()+"";
		params[1] =  dateFrom; //DateUtils.getFirstDayOfTheMonth("yyyy-MM-dd", DateUtils.getCurrentDateYYYYMMDD(), Locale.TAIWAN);
		params[2] = dateTo;//DateUtils.getLastDayOfTheMonth("yyyy-MM-dd", DateUtils.getCurrentDateYYYYMMDD(), Locale.TAIWAN);
		BigDecimal disamnt = new BigDecimal("0.0");
		try{disamnt = Chequedtls.sum(sql, params, fieldName);}catch(Exception e){}
		double tmpdisamnt = 0;
		try{tmpdisamnt=Double.valueOf(disamnt+"");}catch(Exception e){}
		bud.setUsedAmount(Currency.formatAmount(tmpdisamnt+""));
		double budamnt = Double.valueOf(bud.getAmount()+"");
		double total = budamnt - tmpdisamnt;
		total = total<0? 0 : total;
		bud.setRemainingAmount(Currency.formatAmount(total+""));
		bud.setBudgetAmount(Currency.formatAmount(bud.getAmount()+""));
		return bud;
	}
	
	public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        int index = event.getRowIndex();
        
        try{
        	
        	BigDecimal addAmount = budgets.get(index).getAddedAmount();
        	if(addAmount==null) {addAmount = new BigDecimal("0.00");}
        	budgets.get(index).setAddedAmountTmp(Currency.formatAmount(addAmount));
        	
        	double limitAmount = 0d;
        	try{limitAmount = budgets.get(index).getLimitAmount();}catch(NullPointerException e){}
        	budgets.get(index).setLimitAmountTmp(Currency.formatAmount(limitAmount));
        	
        	onChangeUpdate(budgets.get(index), index);
        }catch(Exception e){e.getMessage();}
        
	}
	
	public void limitCheck(IBudget bud){
		
		String sql = "SELECT * FROM useraccesslevel WHERE useraccesslevelid=?";
		String[] params = new String[1];
		Login in = Login.getUserLogin();
		params[0] = in.getAccessLevel().getUseraccesslevelid()+"";
		UserAccessLevel access = UserAccessLevel.retrieve(sql, params).get(0);
		
		System.out.println("Currenct Access : " + access.getLevel() + " Name " + access.getName());
		int id = Integer.valueOf(bud.getId()+"");
		if(access.getLevel()==1 || access.getLevel()==2){
			budgets.get(id-1).setIsActivated(bud.getIsActivated());
			bud.save();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Activation has been successfully changed.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}else{
			budgets.get(id-1).setIsActivated(bud.getIsActivated()==true? false : true);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "You dont have a right to change the activation. Please ask the administrator to change the activation", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
		
	}
	
	private void onChangeUpdate(IBudget budget, int index){
		System.out.println("onChange....");
		System.out.println("new Budget: " + budget.getLimitAmount());
		//try{
			boolean isOk = false;
			IBudget bud = Budget.retrieveBudget("SELECT * FROM budget WHERE bank_id=" + budget.getAccounts().getBankId()+ " AND budtypeid="+budget.getBudgetType().getId(), new String[0]).get(0);
			System.out.println("old Budget: " + bud.getLimitAmount());
			
			String sql = "SELECT * FROM useraccesslevel WHERE useraccesslevelid=?";
			String[] params = new String[1];
			Login in = Login.getUserLogin();
			params[0] = in.getAccessLevel().getUseraccesslevelid()+"";
			UserAccessLevel access = UserAccessLevel.retrieve(sql, params).get(0);
			
			if(budget.getLimitAmount()==bud.getLimitAmount()){
				isOk = true;
			}else{
				
				System.out.println("Currenct Access : " + access.getLevel() + " Name " + access.getName());
				
				if(access.getLevel()==1 || access.getLevel()==2){
					isOk = true;
					System.out.println("level 1 or 2 = " + access.getLevel());
				}else{
					System.out.println("not level 1 or 2 = " + access.getLevel());	
					isOk = false;
					
		        	budgets.get(index).setLimitAmount(bud.getLimitAmount());
		        	budgets.get(index).setLimitAmountTmp(Currency.formatAmount(bud.getLimitAmount()));
					
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "You dont have a right to update the limit amount. Please ask the administrator to change the limit amount", "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			
			if(budget.getCycleDate()!=bud.getCycleDate()){
				if(access.getLevel()==1 || access.getLevel()==2){
					isOk = true;
					bud.setCycleDate(budgets.get(index).getCycleDate());
				}else{	
					isOk = false;
		        	budgets.get(index).setCycleDate(bud.getCycleDate());
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "You dont have a right to update the Cycle Date. Please ask the administrator to change the Cycle Date", "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			
			if(isOk){
				System.out.println("ok for saving...");
				if(budget.getAddedAmount()!=null){
				BigDecimal newAmnt = bud.getAmount().add(budget.getAddedAmount());
				bud.setAmount(newAmnt);
	        	budgets.get(index).setBudgetAmount(Currency.formatAmount(newAmnt));
				}
				try{bud.setLimitAmount(budget.getLimitAmount());}catch(Exception e){e.getMessage();}
				bud.setBudgetDate(DateUtils.getCurrentDateYYYYMMDD());
				
				bud.save();
				
				/*clearFields();
				init();*/
				if(budget.getAddedAmount()==null){
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully updated", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				}else{
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Please click Update button to recalculate the Budget Amount and Remaining Amount", "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			
		//}catch(Exception e){e.getMessage();}
	}
	
	public void updateBudget(){
		
		
	}
	
	private void updateTotal(){
		double total=0,usedamnt=0,remamnt=0;
		if(budgets.size()>0){
			for(IBudget bud : budgets){
				
				total += Double.valueOf(bud.getAmount()+"");
				usedamnt += Double.valueOf(bud.getUsedAmount().replace(",", ""));
				remamnt += Double.valueOf(bud.getRemainingAmount().replace(",", ""));
				
			}
			
		}
		setTotal(Currency.formatAmount(total+""));
		setTotalUsed(Currency.formatAmount(usedamnt+""));
		setTotalRem(Currency.formatAmount(remamnt+""));
	}
	
	public void clearFields(){
		setBudgetData(null);
		setGrandTotal(new BigDecimal("0.00"));
		setGrandTotalUsed(0.00);
		setGrandTotalRem(0.00);
		setTotal("0.00");
		setTotalUsed("0.00");
		setTotalRem("0.00");
	}
	
	private String getUserLogIn(){
		HttpSession session = SessionBean.getSession();
		String proc_by = session.getAttribute("username").toString();
		if(proc_by!=null){
			return proc_by;
		}else{
			return "error";
		}
	}
	
	private IBudget createNewBudget(BankAccounts accounts, BudgetType budgetType){
		IBudget bud = new Budget();
		try{bud.setBudgetDate(DateUtils.getCurrentDateYYYYMMDD());}catch(NullPointerException e){}
		try{bud.setAmount(new BigDecimal("0.00"));}catch(NullPointerException e){}
		
		try{bud.setProcessBy(getUserLogIn());}catch(NullPointerException e){}
		try{bud.setIsActive(1);}catch(NullPointerException e){}
		
		IBudgetType type = new com.italia.municipality.lakesebu.controller.BudgetType();
		try{type.setId(budgetType.getId());}catch(NullPointerException e){}
		bud.setBudgetType(type);
		
		bud.setAccounts(accounts);
		Budget.save(bud);
		return bud;
	}
}
