import java.util.BitSet;
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
		
		
		
		// RECEIVING		
		String receivedMsg = msg;
		//analyseReceivedHeader(receivedMsg, msg);		
		
		//TESTING CODE FOR BITSET MANIPULATIONS
		BitSet bt = new BitSet (32);
		bt.set(3);
		/*boolean btValue = true;
		for(int i = 0; i<bt.length();i++) {
			bt.set(i,btValue);
			btValue = !btValue;
		}*/
		System.out.println(bt.length());
			
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
	
	//HEADER
	public static void analyseReceivedHeader(BitSet headerReceived, BitSet headerSent) {
		//ID
		if(!headerReceived.get(0, 16).equals(headerSent.get(0, 16))) {
			System.out.println("not the good id");
		}//QR
		if(!headerReceived.get(16)) {
			System.out.println("this is not a response");
		}//OPCODE
		if(!headerReceived.get(17,21).equals(headerSent.get(17,21))) {
			System.out.println("not a standard query");
		}//AA
		if(headerReceived.get(21)) {
			System.out.println("authoritative response");
		}
		else {
			System.out.println("non authoritative response");
		}//TC
		if(headerReceived.get(22)) {
			System.out.println("truncated response");
		}//RD
		if(!headerReceived.get(23)) {
			System.out.println("no recursion requested?");
		}//RA
		if(!headerReceived.get(24)) {
			System.out.println("recursion possible from server");
		}
		else {
			System.out.println("no recursion possible from server");
		} 
		// ignored Z (3 bits)
		//TODO: RCODE, needs implementation,
		if(!headerReceived.get(28)||!headerReceived.get(29)||!headerReceived.get(30)
				|| !headerReceived.get(31)||!headerReceived.get(32)) {
			System.out.println("some error");
		}
						
	}
	
	
	public static void analyseReceivedQuestion
	(ByteBuffer questionReceived, ByteBuffer questionSent) {
		//check if both questionReceived and ByteBuffer questionSent are the same
		//check either as bytebuffers or as byte arrays
		if(!questionReceived.equals(questionSent)) {
			System.out.println("didn't receive the right question");
		}
	}
	
	public static void analyseReceivedAnswer
		(ByteBuffer answerReceived) {
		// parse bytes, but need to know when a byte is part of an
		// offset signal
		
		//NAME: most likely an offset to the sent package? need bit manipulations
		
		// TYPE: 16 bit (2 bytes) specific values
		
		// CLASS: should be 0x0001
		
		// TTL: 32 bit (4 byes) check if 0?
		
		//RDLENGTH: 16 bit int (4 bytes) length of RDATA
		
		// RDATAL: depends on TYPE
	}
}
