package com.italia.municipality.lakesebu.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.TabChangeEvent;

import com.italia.municipality.lakesebu.controller.Article;
import com.italia.municipality.lakesebu.controller.ArticleParticular;
import com.italia.municipality.lakesebu.controller.ArticleSubtype;
import com.italia.municipality.lakesebu.controller.Login;
import com.italia.municipality.lakesebu.controller.RevenueCode;
import com.italia.municipality.lakesebu.enm.AccessLevel;
import com.italia.municipality.lakesebu.utils.Application;

import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author Mark Italia
 * @since 10/07/2021
 * @version 1.0
 *
 */
@Named
@ViewScoped
public class ArticleBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 54354675465431L;
	
	@Setter @Getter  private List<Article> articles;
	@Setter @Getter  private String searchArticle;
	@Setter @Getter  private boolean normalUser;
	
	@Setter @Getter  private List<ArticleSubtype> subArticles;
	@Setter @Getter  private String searchSubArticle;
	
	@Setter @Getter  private List arts;
	@Setter @Getter  private List subArts;
	
	
	@Setter @Getter  private List<ArticleParticular> particulars;
	@Setter @Getter  private String searchParticular;
	
	@Setter @Getter  private Map<Long, Article> mapArticle;
	@Setter @Getter  private Map<Long, ArticleSubtype> mapSubArticle;
	
	@Setter @Getter  private String searchRevenueCode;
	
	@Setter @Getter  private List<RevenueCode> revenues;
	
	public void openRevenue() {
		
		
		loadCommon();
		
		Login in = Login.getUserLogin();
		 if(AccessLevel.DEVELOPER.getId()==in.getAccessLevel().getLevel() || AccessLevel.ADMIN.getId()==in.getAccessLevel().getLevel()) {
			 setNormalUser(true);
			 loadArticle();
		 }else {
			 loadAll();
		 }
		 
		 
		 
		 PrimeFaces pf = PrimeFaces.current();
		 pf.executeScript("PF('dlgArticle').show(1000)");
		 
	}
	
	public void loadCommon() {
		mapArticle = new LinkedHashMap<Long, Article>();
		mapSubArticle = new LinkedHashMap<Long, ArticleSubtype>();
		
		arts = new ArrayList<>();
		 arts.add(new SelectItem(0, "Select Article"));
		 for(Article a : Article.retrieve("",new String[0])) {//ORDER BY st.artcode
			 arts.add(new SelectItem(a.getId(), a.getCode()));
			 mapArticle.put(a.getId(), a);
		 }
		 
		 subArts = new ArrayList<>();
		 subArts.add(new SelectItem(0, "Select Sub Article"));
		 for(ArticleSubtype a : ArticleSubtype.retrieve("",new String[0])) {//ORDER BY sub.subname
			 subArts.add(new SelectItem(a.getId(), a.getName()));
			 mapSubArticle.put(a.getId(), a);
		 }
	}
	
	
	public void loadArticle() {
		articles = new ArrayList<Article>();
		String sql = "";
		if(getSearchArticle()!=null && !getSearchArticle().isEmpty()) {
			sql = " AND (st.artcode like '%"+ getSearchArticle() +"%' OR st.artname like '%"+ getSearchArticle() +"%')";
			sql += " ORDER BY st.artcode";
		}else {
			sql = " ORDER BY st.artcode LIMIT 10";
		}
		
		//add default value colum for input
		articles.add(Article.builder()
				.code("Add Code")
				.name("Add Name")
				.description("Add Description")
				.isActive(1)
				.build());
		
		articles.addAll(Article.retrieve(sql, new String[0]));
	}
	
	public void loadSub() {
		subArticles = new ArrayList<ArticleSubtype>();
		String sql = "";
		if(getSearchSubArticle()!=null && !getSearchSubArticle().isEmpty()) {
			sql = " AND sub.subname like '%"+ getSearchSubArticle() +"%'";
			sql += " ORDER BY sub.subname";
		}else {
			sql = " ORDER BY art.artcode LIMIT 10";
		}
		
		//add default value colum for input
		subArticles.add(ArticleSubtype.builder()
				.name("Add Name")
				.article(Article.builder().code("Select Article").build())
				.articleId(0)
				.articles(getArts())
				.isActive(1)
				.build());
		
		for(ArticleSubtype a : ArticleSubtype.retrieve(sql, new String[0])) {
			a.setArticleId(a.getArticle().getId());
			a.setArticles(getArts());
			subArticles.add(a);
		}
		
	}
	
	public void loadParticulars() {
		particulars = new ArrayList<ArticleParticular>();
		String sql = "";
		if(getSearchParticular()!=null && !getSearchParticular().isEmpty()) {
			sql = " AND (pt.parname like '%"+ getSearchParticular() +"%' OR sub.subname like '%"+ getSearchParticular() +"%' OR art.artcode like '%"+ getSearchParticular() +"%' OR art.artname like '%"+ getSearchParticular() +"%' )";
			sql += " ORDER BY pt.parname";
		}else {
			sql = " ORDER BY art.artcode LIMIT 10";
		}
		
		particulars.add(
				ArticleParticular.builder()
					.articleId(0)
					.articles(getArts())
					.article(Article.builder().code("Select Article").build())
					.subId(0)
					.subs(getSubArts())
					.articleSubtype(ArticleSubtype.builder().name("Select Subtype").build())
					.name("Add Name")
					.description("Add Description")
					.amount(0.00)
					.isActive(1)
				.build()
				);
		
		for(ArticleParticular p : ArticleParticular.retrieve(sql, new String[0])) {
			p.setArticleId(p.getArticle().getId());
			p.setArticles(getArts());
			
			p.setSubId(p.getArticleSubtype().getId());
			p.setSubs(getSubArts());
			
			particulars.add(p);
		}
		
		
		
	}
	
	public void loadAll() {
		revenues = new ArrayList<RevenueCode>();
		String sql = "";
		if(getSearchRevenueCode()!=null) {
			sql = " AND (pt.parname like '%"+ getSearchRevenueCode() +"%' OR pt.pardesc like '%"+ getSearchRevenueCode() +"%' OR "
					+ " sub.subname like '%"+ getSearchRevenueCode() +"%' OR art.artcode like '%"+ getSearchRevenueCode() +"%' OR art.artname like '%"+ getSearchRevenueCode() +"%')";
			sql += " ORDER BY art.artcode";
		}else {
			sql = " ORDER BY art.artcode LIMIT 10";
		}
		
		Map<Long, Map<Long, List<ArticleParticular>>> mainPart = new LinkedHashMap<Long,Map<Long, List<ArticleParticular>>>();
		Map<Long, List<ArticleParticular>> subMainPart = new LinkedHashMap<Long, List<ArticleParticular>>();
		List<ArticleParticular> parts = new ArrayList<ArticleParticular>();
		
		for(ArticleParticular p : ArticleParticular.retrieve(sql, new String[0])) {
			
			long artId = p.getArticle().getId();
			long subId = p.getArticleSubtype().getId();
			
			if(mainPart!=null) {
				
				if(mainPart.containsKey(artId)) {
					
					if(mainPart.get(artId).containsKey(subId)) {
						mainPart.get(artId).get(subId).add(p);
					}else {
						parts = new ArrayList<ArticleParticular>();
						parts.add(p);
						mainPart.get(artId).put(subId, parts);
					}
					
				}else {
					parts = new ArrayList<ArticleParticular>();
					parts.add(p);
					subMainPart = new LinkedHashMap<Long, List<ArticleParticular>>();
					subMainPart.put(subId, parts);
					mainPart.put(artId, subMainPart);
				}
				
			}else {
				parts.add(p);
				subMainPart.put(subId, parts);
				mainPart.put(artId, subMainPart);
			}
			
		}
		
		Map<Long, Map<Long, List<ArticleParticular>>> sortedArt = new TreeMap<Long, Map<Long, List<ArticleParticular>>>(mainPart);
		
		for(long artId : sortedArt.keySet()) {
			
			
			for(long subId : sortedArt.get(artId).keySet()) {
				int count = 1;
				for(ArticleParticular p : sortedArt.get(artId).get(subId)) {
					
					RevenueCode rv = null;
					
					if(count==1) {
						
						rv = RevenueCode.builder()
								.articleCode(p.getArticle().getCode())
								.articleName(p.getArticle().getName())
								.subName(p.getArticleSubtype().getName())
								.particular(p.getName())
								.description(p.getDescription())
								.fee(p.getAmount())
								.mpf(p.getMpf())
								.espf(p.getEspf())
								.emf(p.getEmf())
								.rmf(p.getRmf())
								.sf(p.getSf())
								.ssf(p.getSsf())
								.build();
					}else {
						
						rv = RevenueCode.builder()
								.articleCode("")
								.articleName("")
								.subName("")
								.particular(p.getName())
								.description(p.getDescription())
								.fee(p.getAmount())
								.mpf(p.getMpf())
								.espf(p.getEspf())
								.emf(p.getEmf())
								.rmf(p.getRmf())
								.sf(p.getSf())
								.ssf(p.getSsf())
								.build();
						
					}
					
					count++;
					revenues.add(rv);
					
				}
				
			}
			
		}
		
	}
	
	public void onChange(TabChangeEvent event) {
		//Tab activeTab = event.getTab();
		//...
		if("Article".equalsIgnoreCase(event.getTab().getTitle())) {
			loadArticle();
		}else if("Sub Article".equalsIgnoreCase(event.getTab().getTitle())){
			loadSub();
		}else if("Particulars".equalsIgnoreCase(event.getTab().getTitle())){
			loadParticulars();
		}else if("Revenue Code Details".equalsIgnoreCase(event.getTab().getTitle())){
			loadAll();
		}		
	}
	
	public void onCellEditArticle(CellEditEvent event) {
		   Object oldValue = event.getOldValue();
		   Object newValue = event.getNewValue();
	}	
	
	public void onCellEditSubArticle(CellEditEvent event) {
		   Object oldValue = event.getOldValue();
		   Object newValue = event.getNewValue();
		   int index = event.getRowIndex();
		   long id = 0;
		   if( event.getNewValue() instanceof Long) {
			   id = (Long)newValue;
			   subArticles.get(index).setArticleId(id);
			   subArticles.get(index).setArticle(getMapArticle().get(id));
			   subArticles.get(index).getArticle().setCode(getMapArticle().get(id).getCode());
		   }
	}	
	
	public void onCellEditArticlePart(CellEditEvent event) {
		   Object oldValue = event.getOldValue();
		   Object newValue = event.getNewValue();
		   int index = event.getRowIndex();
		   String key =  event.getColumn().getHeaderText();
		   long id = 0;
		   
		   if( event.getNewValue() instanceof Long) {
			   id = (Long)newValue;
			   
			   if("Article".equalsIgnoreCase(key)) {
				   
				   particulars.get(0).setArticleId(id);
				   particulars.get(index).setArticle(getMapArticle().get(id));
				   particulars.get(index).getArticle().setCode(getMapArticle().get(id).getCode());
				   
			   }else if("Sub Article".equalsIgnoreCase(key)) {
				   
				   particulars.get(0).setSubId(id);
				   particulars.get(index).setArticleSubtype(getMapSubArticle().get(id));
				   particulars.get(index).getArticleSubtype().setName(getMapSubArticle().get(id).getName());
				   
			   }
			   
		   }   
	}
	
	public void saveArticle(Article article) {
		        if(!"Add Code".equalsIgnoreCase(article.getCode()) && !"Add Name".equalsIgnoreCase(article.getName())) {
		        	article.save();
		        	loadArticle();
		        	loadCommon();
		        	Application.addMessage(1, "Success", "Data was successfully saved.");
		        }else {
		        	Application.addMessage(2, "Error", "Saving was not successfully please check your data.");
		        }
		}
	public void saveArticleSub(ArticleSubtype sub) {
		
		if(sub!=null && !"Add Name".equalsIgnoreCase(sub.getName())) {
        	sub.save();
        	loadSub();
        	loadCommon();
        	Application.addMessage(1, "Success", "Data was successfully saved.");
        }else {
        	Application.addMessage(2, "Error", "Saving was not successfully please check your data.");
        }
	}
	
	public void saveParticular(ArticleParticular part) {
		if(part!=null && !"Add Name".equalsIgnoreCase(part.getName()) && !"Add Description".equalsIgnoreCase(part.getDescription()) ) {
			part.save();
			loadParticulars();
			loadCommon();
			Application.addMessage(1, "Success", "Data was successfully saved.");
		}else {
			Application.addMessage(2, "Error", "Saving was not successfully please check your data.");
		}
	}
}
