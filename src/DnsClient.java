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
		byte[] ipAddr = translateIPAddress("74.15.208.134");
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
		
		byte[] received = receivePacket.getData();
		ByteBuffer recievedData = ByteBuffer.wrap(received);
		decode(sendData, recievedData,19);
		
//		BitSet headerBitsSent = BitSet.valueOf(new byte [] {3,7});
//		for(int i =0; i<16; i++) {
//			System.out.println(headerBitsSent.get(i));
//		}
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
		byte[] byteArrReceived = new byte [2];
		byteArrReceived[0] = receivedData.get(3);
		byteArrReceived[1] = receivedData.get(2);
		BitSet headerBitsReceived = BitSet.valueOf(byteArrReceived);
		byte[] byteArrSent = new byte [2];
		byteArrSent[0] = sentData.get(3);
		byteArrSent[1] = sentData.get(2);
		BitSet headerBitsSent = BitSet.valueOf(byteArrSent);
		decodeBitsInHeader(headerBitsReceived,headerBitsSent);
		System.out.println(receivedData.get(3));
		
		for(int i =0; i<16; i++) {
			//System.out.println(headerBitsReceived.get(i));
		}
		
		//rest of the header
		int qdCount = receivedData.getShort(4);
		int anCount = receivedData.getShort(6);
		int nsCount = receivedData.getShort(8);
		int arCount = receivedData.getShort(10);
		
		System.out.println(qdCount + " " + anCount + " " + nsCount + " "+ arCount);
		
		position = 12;
		
		int endOfQuestion = position + questionLength;
		//Question
		//check if both questionReceived and ByteBuffer questionSent are the same
		//check either as bytebuffers or as byte arrays
		while(position < endOfQuestion) {
			if(receivedData.get(position)!=sentData.get(position)) {
				System.out.println("didn't receive the right question");
			}
			position++;
		}
		
		System.out.println("position of the answer is: " + position);
		
		//Answer		
		// parse bytes, but need to know when a byte is part of an
		// offset signal
		
		//NAME: most likely an offset to the sent package? need byte manipulations
				
		String nameInAnswer = dnsServerName(receivedData);
		System.out.println("The name in answer is: " + nameInAnswer);
		
		System.out.println("position after the answer is: " + position);
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
			System.out.println("the type is host address");
		}
		else if (type ==2) {
			//type NS
			System.out.println("name server");
		}
		else if (type == 15) {
			//type MX
			System.out.println("mail server");
		}
		else if (type == 5) {
			// CNAME
			System.out.println("the type is CNAME");
		}
		else {
			// error
		}
		position += 2;
		
		// CLASS: should be 0x0001
		if (receivedData.getShort(position)!=1) {
			System.out.println("not good class");
		}
		position += 2;
		
		// TTL: 32 bit (4 byes) check if 0?
		if (receivedData.getInt(position)!=0) {
			System.out.println("TTL is " + receivedData.getInt(position));
		}
		else {
			System.out.println("0 TTL");
		}
		position += 4;
				
		//RDLENGTH: 16 bit int (2 bytes) length of RDATA
		
		int rdLength = receivedData.getShort(position);
		System.out.println("The rdLength is: " + rdLength);
		position+=2;
		String RData = "";
		
		// RDATAL: depends on TYPE
		if(type == 1) {
			//type A IP Address Record (4 bytes)
			for(int i=0;i<rdLength;i++) {
				RData += receivedData.get(position)&255; //convert to unsigned
				if(i!=3) {
					RData += ".";
				}
				position++;
			}
			System.out.println("the RDATA IP is:" + RData);
			
		}
		else if (type ==2) {
			//type NS server name same type as QNAME
			String NSNameRData = dnsServerName(receivedData);
			System.out.println("The name in answer is: " + NSNameRData);
			position++;
		}
		else if (type == 15) {
			//type MX preference + exchange
			//Preference
			
			//Exchange
			
		}
		else if (type == 5) {
			// CNAME name of the alias
		}
		else {
			// error
		}
	}
	
	
	//HEADER bit part
	public static void decodeBitsInHeader(BitSet headerReceived, BitSet headerSent) {
		//QR
		if(headerReceived.get(15)) {
			System.out.println("this is a response");
		}//OPCODE
		if(headerReceived.get(11,15).equals(headerSent.get(11,15))) {
			System.out.println("standard query");
		}//AA
		if(headerReceived.get(10)) {
			System.out.println("authoritative response");
		}
		else {
			System.out.println("non authoritative response");
		}//TC
		if(headerReceived.get(9)) {
			System.out.println("truncated response");
		}//RD
		if(headerReceived.get(8)) {
			System.out.println("recursion requested");
		}//RA
		if(headerReceived.get(7)) {
			System.out.println("recursion possible from server");
		}
		else {
			System.out.println("no recursion possible from server");
		} 
		// ignored Z (3 bits)
		//TODO: RCODE, needs implementation,
		if(headerReceived.get(3)||headerReceived.get(2)||headerReceived.get(1)
				|| headerReceived.get(0)) {
			System.out.println("some error");
			System.out.println(headerReceived.get(15));
		}
						
	}
	
	public static String dnsServerName(ByteBuffer receivedData) {
		int namePosition = position;
		String domainName = "";
		boolean offsetFound = false;
		while (receivedData.get(namePosition)!=0) {
			System.out.println(namePosition);
			if(isOffset(receivedData.get(namePosition))){				
				namePosition = getOffset(receivedData, namePosition);
				offsetFound = true;
			}
			else {
				int nextNumberOfChars = receivedData.get(namePosition);
				namePosition++;
				System.out.println("next chars: " +nextNumberOfChars);
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
		if(offsetFound) {
			position+=2;
		}
		else {
			position++;
		}
		return domainName;
	}
	
	public static boolean isOffset(byte bt1) {
		byte [] btArray = new byte [1];
		btArray[0] = bt1;
		BitSet btSet = BitSet.valueOf(btArray);
		for(int i =0; i<16; i++) {
			//System.out.println(btSet.get(i));
		}
		if(btSet.get(6) && btSet.get(7)) {
			return true;
		}
		return false;
	}
	
	public static int getOffset(ByteBuffer bf, int offsetPosition) {
		byte [] btArray = {bf.get(offsetPosition+1), bf.get(offsetPosition)};
		BitSet btSet = BitSet.valueOf(btArray);
		int offset = 0;
		System.out.println(bf.get(offsetPosition));
		System.out.println(bf.get(offsetPosition+1));
		System.out.println(offsetPosition);
		for(int i =0; i<16; i++) {
			//System.out.println(btSet.get(i));
		}
		for(int i =0; i<14; i++) {
			if(btSet.get(i)) {
				offset += Math.pow(2,i);
			}					
		}
		System.out.println("the offset is:" + offset);
		return offset;
	}
}
