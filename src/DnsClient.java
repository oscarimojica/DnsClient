import java.util.BitSet;
import java.util.Random;
import  java.io.*;
import  java.net.*;
import java.nio.ByteBuffer;

public class DnsClient {
	
	public static int position = 0;

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
		
		
		// RECEIVING		
		byte[] bytearr = new byte [2];
		bytearr[0] = (byte) 3;
		bytearr[1] = (byte) 4;
		ByteBuffer buf = ByteBuffer.wrap(bytearr);
		System.out.println(buf.getShort(0));

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

	//DECODE

	public static void decode(ByteBuffer sentData, ByteBuffer receivedData, int questionLength) {
		position = 0;
		// HEADER
		//ID
		if(sentData.getShort(0)!=receivedData.getShort(0)) {
			System.out.println("not the good id");
		}
		// header involving bits
		byte[] bytearr = new byte [2];
		bytearr[0] = receivedData.get(2);
		bytearr[1] = receivedData.get(3);
		BitSet headerPartReceived = BitSet.valueOf(bytearr);
		byte[] bytearr2 = new byte [2];
		bytearr2[0] = sentData.get(2);
		bytearr2[1] = sentData.get(3);
		BitSet headerPartSent = BitSet.valueOf(bytearr2);
		decodeHeader(headerPartReceived,headerPartSent);
		//rest of the header
		int qdCount = receivedData.getShort(4);
		int anCount = receivedData.getShort(6);
		int nsCount = receivedData.getShort(8);
		int arCount = receivedData.getShort(10);
		
		position = 13;
		
		//Question
		//check if both questionReceived and ByteBuffer questionSent are the same
		//check either as bytebuffers or as byte arrays
		while(position < 13 + questionLength) {
			if(receivedData.get(position)!=sentData.get(position)) {
				System.out.println("didn't receive the right question");
			}
			position++;
		}
		
		//Answer		
		// parse bytes, but need to know when a byte is part of an
		// offset signal
		
		//NAME: most likely an offset to the sent package? need byte manipulations
		position ++;
		
		dnsServerName(receivedData);		
		
		position++;
		
		// TYPE: 16 bit (2 bytes) specific values
		/*
		 	0x0001 for a type-A query (host address)
			0x0002 for a type-NS query (name server)
			0x000f for a type-MX query (mail server)
			0x0005 corresponding to CNAME records.
		 */
		
		
		int type = receivedData.getShort(position);
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
		position += 2;
		
		// CLASS: should be 0x0001
		if (receivedData.getShort(position)!=1) {
			System.out.println("not good class?");
		}
		position += 2;
		
		// TTL: 32 bit (4 byes) check if 0?
		if (receivedData.getInt(position)!=0) {
			System.out.println("not 0 TTL?");
		}
		else {
			System.out.println("0 TTL");
		}
		position += 4;
				
		//RDLENGTH: 16 bit int (2 bytes) length of RDATA
		
		int rdLength = receivedData.getInt(position);
		
		// RDATAL: depends on TYPE
		if(type == 1) {
			//type A IP Address Record (4 bytes)
		}
		else if (type ==2) {
			//type NS server name same type as QNAME
		}
		else if (type == 15) {
			//type MX preference + exchange
		}
		else if (type == 5) {
			// CNAME name of the alias
		}
		else {
			// error
		}
	}
	
	
	//HEADER
	public static void decodeHeader(BitSet headerReceived, BitSet headerSent) {
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
	
	public static String dnsServerName(ByteBuffer receivedData) {
		int offset = 0;		
		int namePosition = position;
		String domainName = "";
		boolean offsetFound = false;
		while (receivedData.get(namePosition)!=0) {
			if(isOffset(receivedData.get(namePosition))){
				offset = receivedData.getShort(namePosition) - 49152; //substract the value two leftmost bits TODO: change -> signed short
				namePosition = offset;
				offsetFound = true;
			}
			else {
				int nextNumberOfChars = receivedData.get(namePosition);
				for (int i=0; i<nextNumberOfChars; i++) {
					domainName += (char) receivedData.get(namePosition);
					namePosition++;
					if(!offsetFound) {
						position = namePosition;
					}
				}
				if(receivedData.get(namePosition+1)!=0) {
					domainName += ".";
				}
			}
		}
		return domainName;
	}
	
	public static boolean isOffset(byte bt1) {
		byte [] btArray = new byte [1];
		btArray[0] = bt1;
		BitSet btSet = BitSet.valueOf(btArray);
		if(btSet.get(0) && btSet.get(1)) {
			return true;
		}
		return false;
	}	
}
