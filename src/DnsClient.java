import java.util.Random;
import  java.io.*;
import  java.net.*;
import java.nio.ByteBuffer;

public class DnsClient {
	
	public static void main(String[] args) throws IOException {
		
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
		//String msg = id + QR;
		//int decimal = Integer.parseInt(msg, 2);
		//String hex = Integer.toString(decimal, 16);
		
		//System.out.println(hex);
		sendData = msg.getBytes();
		System.out.println(sendData);
		
		String receivedMsg = msg;
		analyseReceivedHeader(receivedMsg, msg);
		
		
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
	
	public static void analyseReceivedHeader(String headerReceived, String headerSent) {
		//HEADER
		//ID
		if(!headerReceived.substring(0, 16).equals(headerSent.substring(0, 16))) {
			System.out.println("not the good id");
		}//QR
		if(!headerReceived.substring(16,17).equals("1")) {
			System.out.println("this is not a response");
		}//OPCODE
		if(!headerReceived.substring(17,22).equals("0000")) {
			System.out.println("not a standard query");
		}//AA
		if(headerReceived.substring(22,23).equals("1")) {
			System.out.println("authoritative response");
		}
		else {
			System.out.println("non authoritative response");
		}//TC
		if(headerReceived.substring(23,24).equals("1")) {
			System.out.println("truncated response");
		}//RD
		if(!headerReceived.substring(23,24).equals("1")) {
			System.out.println("no recursion requested?");
		}//RA
		if(!headerReceived.substring(24,25).equals("1")) {
			System.out.println("recursion possible from server");
		}
		else {
			System.out.println("no recursion possible from server");
		} //TODO: RCODE, needs implementation, ignored Z (3 bits)
		if(!headerReceived.substring(28,33).equals("00000")) {
			System.out.println("some error");
		}
						
	}
	
	
	public static void analyseReceivedQuestion
	(ByteBuffer questionReceived, ByteBuffer questionSent) {
		//check if both questionReceived and ByteBuffer questionSent are the same
	}
	
	public static void analyseReceivedAnswer
		(ByteBuffer answerReceived) {
			
		}
}
