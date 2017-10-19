import java.util.BitSet;
import java.util.Random;
import  java.io.*;
import  java.net.*;
import java.nio.ByteBuffer;

public class DnsClient {

	public static void main(String[] args) throws IOException {

		//byte[] sendData = new byte[1024];
		ByteBuffer sendData = ByteBuffer.allocate(1024);

		//System.out.println(hex);
//		sendData = msg.getBytes();
//		System.out.println(sendData);
		//printBB(sendData);

		GetHeader(sendData);

		//printBB(sendData);

		getQuestion("www.mcgill.ca",sendData);

		short QTYPE = 1;
		sendData.putShort(QTYPE);
		short QCLASS = 1;
		sendData.putShort(QCLASS);


		printBB(sendData);

		DatagramSocket clientSocket = new DatagramSocket();
		byte[] ipAddr = translateIPAddress("132.206.85.18");
		byte[] snd = sendData.array();
		InetAddress ip = InetAddress.getByAddress(ipAddr);
		DatagramPacket packet = new DatagramPacket(snd, snd.length, ip, 53);
		System.out.println("RP is :"+ packet);
		clientSocket.send(packet);

		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		System.out.println("RP is :"+ receivePacket);
		clientSocket.receive(receivePacket);

		printbyte(receiveData);
		System.out.println("RP is :"+ receivePacket);
		clientSocket.close();
		System.out.println("RP is :"+ receivePacket);
		// RECEIVING
		//String receivedMsg = msg;
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
	//COPIEDDDDDD CHANGE LATER
	public static byte[] translateIPAddress(String ip) {
		byte[] result = new byte[4];

		// break into individual octets
		String[] subIp = ip.split("\\.");
		for (int i = 0; i < subIp.length; i++) {
			int octet = Integer.valueOf(subIp[i]);

			// conversion from int to unsigned byte is as easy as casting
			result[i] = (byte) octet;

			System.out.println("Original: " + octet + " Altered: " + result[i]);
		}

		return result;
	}
	//COPIED CHANGE LATERRRRRR
	public static void printbyte(byte[] b) {
		for (int i = 0; i<b.length;i++){
			System.out.print(b[i] + " ");
		}
		System.out.println("Done");
	}

	public static void GetHeader(ByteBuffer header){
		//ByteBuffer header = ByteBuffer.allocate(12);
		short flags,QDCOUNT,ANCOUNT,NSCOUNT,ARCOUNT;

		//Generates random ID
		byte[] ID = new byte[2];
		Random rand = new Random();
		rand.nextBytes(ID);
		header.put(ID);
		//initializes question header and sets the appropriate bits.

		flags = 0b0000000100000000;
		header.putShort(flags);
		QDCOUNT = 1;
		header.putShort(QDCOUNT);
		ANCOUNT = 0;
		header.putShort(ANCOUNT);
		NSCOUNT = 0;
		header.putShort(NSCOUNT);
		ARCOUNT = 0;
		header.putShort(ARCOUNT);


		//return header;

	}

	public static void getQuestion(String msg, ByteBuffer question){
		//split string message into three segments
		String[] sections = msg.split("\\.");

		for (int i = 0; i< sections.length; i++ ){
			//Puts length of section on the Bytebuffer
			question.put((byte) sections[i].length());
			for (int j = 0; j<sections[i].length();j++){
				byte ch = (byte) sections[i].charAt(j);
				question.put(ch);
			}
		}
		//Places 0 byte to signal the end of the question.
		question.put((byte) 0);
	}

	public static void printBB(ByteBuffer header){
		byte[] test = header.array();
		for (int i = 0; i<test.length;i++){
			System.out.print(test[i] + " ");
		}
		System.out.println("Done");
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
		/*
		 	0x0001 for a type-A query (host address)
			0x0002 for a type-NS query (name server)
			0x000f for a type-MX query (mail server)
			0x0005 corresponding to CNAME records.
		 */
		int i =4;

		int type = answerReceived.getShort(i);
		if(type == 1) {
			//type A
		}
		else if (type ==2) {
			//type NS
		}
		else if (type == 15) {
			//type MX
		}
		else if (type == 5) {
			// CNAME
		}
		else {
			// error
		}

		// CLASS: should be 0x0001
		if (answerReceived.getShort(i+2)!=1) {
			System.out.println("not good class?");
		}

		// TTL: 32 bit (4 byes) check if 0?
		if (answerReceived.getInt(i+4)!=0) {
			System.out.println("not 0 TTL?");
		}
		else {
			System.out.println("0 TTL");
		}

		//RDLENGTH: 16 bit int (4 bytes) length of RDATA

		// RDATAL: depends on TYPE
	}
}
