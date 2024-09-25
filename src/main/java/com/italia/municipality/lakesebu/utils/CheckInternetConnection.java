package com.italia.municipality.lakesebu.utils;
	
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

	public class CheckInternetConnection {
		
		
		public static void main(String[] args) {
			if(CheckInternetConnection.isInternetPresent("https://semaphore.co/")) {
				System.out.println("the site is accessible...");
			}else {
				System.out.println("not accessible at this moment....");
			}
		}
		
		/**
		 * example https://www.google.com/
		 * @param provider
		 * @return
		 */
		public static boolean isInternetPresent(String provider) {
			
			try {
				URL url = new URL(provider);
				URLConnection connection = url.openConnection();
				connection.connect();
				System.out.println("Connection Successful");
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Internet Not Connected");
				e.printStackTrace();
			}
			
			
			
			return false;
		}
		
		/**
		 * example www.google.com
		 * @param providerSite
		 * @return
		 */
		public static boolean isProviderPresent(String providerSite) {
			
			try {
				Process process = Runtime.getRuntime().exec("ping "+providerSite);
				int x = process.waitFor();
				if(x==0) {
					System.out.println("the site is accessible...");
					return true;
				}else {
					System.out.println("not accessible at this moment....");
					//Application.addMessage(3, "Error", "The site " + providerSite + " is currently not accessible.");
				}
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			return false;
		}
		
}
