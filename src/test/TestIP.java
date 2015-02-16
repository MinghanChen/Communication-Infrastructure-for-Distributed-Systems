package test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import messagepasser.Message;
import messagepasser.MessagePasser;

public class TestIP {
	public static void main(String args[]) {
		InetAddress address;
		try {
			address = InetAddress.getLocalHost();
			String ip=address.getHostAddress();
			System.out.println("the ip is : " + ip);
		} catch (UnknownHostException e) {
			System.out.println("address wrong");
		}
		
	}
	
}
