import java.util.BitSet;
import java.util.Random;
import  java.io.*;
import  java.net.*;
import java.nio.ByteBuffer;

public class DnsClient {
	
	public static int position = 0;
	public static String authoritative = "nonauth";
	public static String error = "";

	public static void main(String[] args) throws IOException {

		//byte[] sendData = new byte[1024];
		ByteBuffer sendData = ByteBuffer.allocate(1024);

		//System.out.println(hex);
//		sendData = msg.getBytes();
//		System.out.println(sendData);
		//printBB(sendData);

		GetHeader(sendData);

		//printBB(sendData);

		getQuestion("www.utoronto.ca",sendData);

		short QTYPE = 1;
		sendData.putShort(QTYPE);
		short QCLASS = 1;
		sendData.putShort(QCLASS);


		printBB(sendData);

		DatagramSocket clientSocket = new DatagramSocket();
		byte[] ipAddr = translateIPAddress("192.168.2.1");
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
		decode(sendData, recievedData);
		
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

	public static void decode(ByteBuffer sentData, ByteBuffer receivedData) {
		position = 0;
		
		// HEADER
		//ID
		if(sentData.getShort(0)!=receivedData.getShort(0)) {
			error += "ERROR \t incorrect id from the response \n";
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
				
		//rest of the header
		int qdCount = receivedData.getShort(4);
		int anCount = receivedData.getShort(6);
		int nsCount = receivedData.getShort(8);
		int arCount = receivedData.getShort(10);
		
		System.out.println(qdCount + " " + anCount + " " + nsCount + " "+ arCount);
		
		position = 12;
		
		//Question
		//check if both questionReceived and ByteBuffer questionSent are the same
		//check either as bytebuffers or as byte arrays
		
		while(receivedData.get(position)!=0) {
			if(receivedData.get(position)!=sentData.get(position)) {
				error += "ERROR \t did not receive the right question";
			}
			position++;
		}
		for (int i = 0; i<5; i++) {
			if(receivedData.get(position)!=sentData.get(position)) {
				error += "ERROR \t did not receive the right question";
			}
			position++;
		}
		//System.out.println("position of the answer is: " + position);
		//System.out.println("the next byte is: " + receivedData.get(position) );
		
		//Answer		
		// parse bytes, but need to know when a byte is part of an
		// offset signal
		
		if(anCount>0) {
			System.out.println("***Answer Section (" + anCount + " records)***");
		}
		for(int j = 0; j<anCount; j++) {
			//System.out.println("current anCount is: " + j);
			decodeAnswer(receivedData, false);
		}
		
		if(nsCount>0) {
			System.out.println("***Authority Section (" + nsCount + " records) ignored***");
		}
		for(int j = 0; j<nsCount; j++) {
			//System.out.println("current nsCount is: " + j);
			decodeAnswer(receivedData, true);
		}
		
		if(arCount>0) {
			System.out.println("***Additional Section (" + arCount + " records)***");
		}
		
		for(int j = 0; j<arCount; j++) {
			//System.out.println("current arCount is: " + j);
			decodeAnswer(receivedData, false);
		}		
				
		if(anCount==0 && arCount ==0) {
			System.out.println("NOTFOUND");
		}
		System.out.println(error);
	}
	
	public static void decodeAnswer (ByteBuffer receivedData, boolean authoritySection) {		
		
		//NAME: most likely an offset to the sent package? need byte manipulations
				
		String nameInAnswer = dnsServerName(receivedData);
		//System.out.println("The name in answer is: " + nameInAnswer);
		
		//System.out.println("position after the answer is: " + position);
		// TYPE: 16 bit (2 bytes) specific values
		/*
		 	0x0001 for a type-A query (host address)
			0x0002 for a type-NS query (name server)
			0x000f for a type-MX query (mail server)
			0x0005 corresponding to CNAME records.
		 */		
		int type = receivedData.getShort(position);
		if (type !=1 && type != 2 && type != 5 && type != 15) {
			System.out.println("type of response not supported");
		}
		position += 2;
		
		// CLASS: should be 0x0001
		if (receivedData.getShort(position)!=1) {
			error += "ERROR \t not the good class";
		}
		position += 2;
		
		int TTL = 0;
		// TTL: 32 bit (4 byes) check if 0?
		TTL = receivedData.getInt(position);
		if (TTL<=0) {
			error += "ERROR \t TTL is less or equal to 0";
		}
		position += 4;
				
		//RDLENGTH: 16 bit int (2 bytes) length of RDATA
		
		int rdLength = receivedData.getShort(position);
		//System.out.println("The rdLength is: " + rdLength);
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
			//System.out.println("the RDATA IP is:" + RData);
			if(!authoritySection) {
				System.out.println("IP \t" + RData + "\t " + TTL + "\t" + authoritative );
			}			
		}
		else if (type ==2) {
			//type NS server name same type as QNAME
			RData = dnsServerName(receivedData);
			//System.out.println("The name in answer is: " + RData);
			if(!authoritySection) {
				System.out.println("NS \t" + RData + "\t " + TTL + "\t" + authoritative );
			}			
		}
		else if (type == 15) {
			//type MX preference + exchange
			//Preference
			short preference = receivedData.getShort(position); 
			//System.out.println("the preference is: " + preference);
			position += 2;
			
			//Exchange
			RData = dnsServerName(receivedData);
			//System.out.println("The mx name in answer is: " + RData);
			if(!authoritySection) {
				System.out.println("MX \t" + RData + "\t " + preference + "\t" + TTL + "\t" + authoritative );	
			}
					
		}
		else if (type == 5) {
			// CNAME name of the alias
			//System.out.println(position + " " + receivedData.get(position));
			RData = dnsServerName(receivedData);
			//System.out.println("The cname in answer is: " + RData);
			if(!authoritySection) {
				System.out.println("CNAME \t" + RData + "\t " + TTL + "\t" + authoritative );
			}
			
		}
		else {
			System.out.println("type of response not supported");
		}
	}
	
	
	//HEADER bit part
	public static void decodeBitsInHeader(BitSet headerReceived, BitSet headerSent) {
		//QR
		if(!headerReceived.get(15)) {
			error += "ERROR \t this is not a response \n";
		}//OPCODE
		if(!headerReceived.get(11,15).equals(headerSent.get(11,15))) {
			error += "ERROR \t this is not a standart query \n";
		}//AA
		if(headerReceived.get(10)) {
			//System.out.println("authoritative response");
			authoritative = "auth";
		}
		else {
			//System.out.println("non authoritative response");
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
			if(isOffset(receivedData.get(namePosition))){				
				namePosition = getOffset(receivedData, namePosition);
				offsetFound = true;
			}
			else {
				int nextNumberOfChars = receivedData.get(namePosition);
				namePosition++;
				//System.out.println("next chars: " +nextNumberOfChars);
				for (int i=0; i<nextNumberOfChars; i++) {
					domainName += (char) receivedData.get(namePosition);
					namePosition++;
					if(!offsetFound) {
						position = namePosition;
					}
				}
				if(receivedData.get(namePosition)!=0) {
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
		if(btSet.get(6) && btSet.get(7)) {
			return true;
		}
		return false;
	}
	
	public static int getOffset(ByteBuffer bf, int offsetPosition) {
		byte [] btArray = {bf.get(offsetPosition+1), bf.get(offsetPosition)};
		BitSet btSet = BitSet.valueOf(btArray);
		int offset = 0;
		for(int i =0; i<14; i++) {
			if(btSet.get(i)) {
				offset += Math.pow(2,i);
			}					
		}
		//System.out.println("the offset is:" + offset);
		return offset;
	}
}
