package com.italia.municipality.lakesebu.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CheckServerConnection {

public static void main(String[] args) {
		
		//getNetworkIPs();
		//locateHost();
		
		//pingIp();
	}
	
	public static void getNetworkIPs() {
	    final byte[] ip;
	    try {
	        ip = InetAddress.getLocalHost().getAddress();
	    } catch (Exception e) {
	        return;     // exit method, otherwise "ip might not have been initialized"
	    }

	    for(int i=1;i<=254;i++) {
	        final int j = i;  // i as non-final variable cannot be referenced from inner class
	        new Thread(new Runnable() {   // new thread for parallel execution
	            public void run() {
	                try {
	                    ip[3] = (byte)j;
	                    InetAddress address = InetAddress.getByAddress(ip);
	                    String output = address.toString().substring(1);
	                    if (address.isReachable(5000)) {
	                        System.out.println(output + " is on the network");
	                    } else {
	                        System.out.println("Not Reachable: "+output);
	                    }
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        }).start();     // dont forget to start the thread
	    }
	}
	
	public static boolean pingIp(String ip) {
		try {
			String ipAddress = ip;
			InetAddress inet = InetAddress.getByName(ipAddress);
			System.out.println("Sending Ping Request to " + ipAddress);
			if (inet.isReachable(5000)) {
				System.out.println(ipAddress + " is reachable.");
				return true;
			} else {
				System.out.println(ipAddress + " NOT reachable.");
				return false;
			}
		} catch (Exception e) {
			System.out.println("Exception:" + e.getMessage());
		}
		return false;
	}
	
	public static void locateHost() {
		InetAddress ip;
        String hostname;
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
            System.out.println("Your current IP address : " + ip);
            System.out.println("Your current Hostname : " + hostname);
 
        } catch (UnknownHostException e) {
 
            e.printStackTrace();
        }
	}

}
