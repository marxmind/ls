package com.italia.municipality.lakesebu.enm;

/**
 * 
 * @author Mark Italia
 * @since 1-19-2022
 * @version 1.0
 *
 */
public enum BFilter {

	/**
	 * 
	 * @author Mark Italia
	 * @since 08-12-2018
	 * @version 1.0
	 *
	 */

		ENGAGEMENT(1, "ENGAGEMENT"),
		EXCEPTION(2, "EXCEPTION");
		
		private int id;
		private String name;
		
		public int getId(){
			return id;
		}
		
		public String getName(){
			return name;
		}
		
		private BFilter(int id, String name){
			this.id = id;
			this.name = name;
		}
		
		public static String nameId(int id){
			
			for(BFilter type : BFilter.values()){
				if(id==type.getId()){
					return type.getName();
				}
			}
			
			return BFilter.ENGAGEMENT.getName();
		}
		
		public static int idName(String name){
			
			for(BFilter type : BFilter.values()){
				if(name.equalsIgnoreCase(type.getName())){
					return type.getId();
				}
			}
			
			return BFilter.ENGAGEMENT.getId();
		}

}