import java.util.Random;
import  java.io.*;
import  java.net.*;

public class DnsClient {
	
	public static void main(String[] args) {
		
		byte[] sendData = new byte[1024];
		boolean isQuery = true;
		//Move to a makeHeader method later.
		String id = getID();
		String QR = getQR(isQuery);
		String OPCODE = String.format("%4s", "0").replace(' ', '0');
		String Flags = "0010000000";
		String QDCOUNT = String.format("%16s", "1").replace(' ', '0');
		String ANCOUNT = String.format("%16s", "0").replace(' ', '0');
		String NSCOUNT = String.format("%16s", "0").replace(' ', '0');
		String ARCOUNT = String.format("%16s", "1").replace(' ', '0');
		
		String msg = id + QR + OPCODE + Flags + QDCOUNT + ANCOUNT + NSCOUNT+ARCOUNT;
		int decimal = Integer.parseInt(msg, 2);
		String hex = Integer.toString(decimal, 16);
		
		System.out.println(hex);
		sendData = msg.getBytes();
		System.out.println(sendData);
	}
	
	
	public static String getID(){
		Random rand = new Random();
		int  n = rand.nextInt(65535);
		System.out.println(n);
		String ID = String.format("%16s", Integer.toBinaryString(n)).replace(' ', '0');
		return ID;
	}
	
	public static String getQR(boolean isQuery){
		if (isQuery){
			return "1";
		}else 
			return "0";
	}
}
