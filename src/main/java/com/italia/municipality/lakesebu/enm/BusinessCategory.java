package com.italia.municipality.lakesebu.enm;

public enum BusinessCategory {
		RETAILER(0, "Retailer"),
		WHOLESALER(1, "Wholesaler"),
		AMUSEMENT(2, "Amusement"),
		CONTRACTOR(3, "Contractor"),
		COTTAGES(4, "Cottages"),
		EATING_PLACES(5, "Eating Places"),
		FINANCIAL_INSTITUTION(6, "Financial Institution"),
		FISHERY_RENTAL(7, "Fishery Rental"),
		INSTITUTION(8,"Institution"),
		MANUFACTURER(9, "Manufacturer"),
		OTHER_RETAILER(10,"Other Retailer"),
		OTHER(11, "Other"),
		SKYLAB_PERMIT(12, "Skylab Permit"),
		SWIMMING_POOL(13, "Swimming Pool"),
		TRADER(14, "Trader"),
		SALON(15, "Salon"),
		RESORT(16, "Resort"),
		MINING(17, "Mining"),
		MOTORCYCLE_SHOP(18, "Motorcycle Shop");
		
		private int id;
		private String name;
		
		public int getId() {
			return id;
		}
		
		public String getName() {
			return name;
		}
		
		private BusinessCategory(int id, String name) {
			this.id = id;
			this.name = name;
		}
		
		
		public static BusinessCategory val(int id) {
			for(BusinessCategory g : BusinessCategory.values()) {
				if(id==g.getId()) {
					return g;
				}
			}
			return BusinessCategory.RETAILER;
		}
		public static BusinessCategory val(String name) {
			for(BusinessCategory g : BusinessCategory.values()) {
				if(name.equalsIgnoreCase(g.getName())) {
					return g;
				}
			}
			return BusinessCategory.RETAILER;
		}
}
