import java.util.BitSet;
import java.util.Random;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class DnsClient {
	// Global variables used in the program.
	public static int position = 0;
	public static int timeout = 5;
	public static int retries = 3;
	public static int port = 53;
	public static short queryType = 1;
	public static String IP, website;
	public static String authoritative = "nonauth";
	public static String error = "";

	public static void main(String[] args) throws IOException {
		// Calls input method to parse arguments.
		inputs(args);
		// Creates bytebuffer to be used for sending data.
		ByteBuffer sendData = ByteBuffer.allocate(1024);
		// Puts header data on bytebuffer
		GetHeader(sendData);
		// Parses and the requested string according to DNS protocol and puts on
		// byte buffer.
		getQuestion(website, sendData);
		// Prints out Request Type and adds to bytebuffer.
		short QTYPE = queryType;
		switch (QTYPE) {
		case 1:
			System.out.println("Request Type A");
			break;
		case 2:
			System.out.println("Request NS");
			break;
		case 15:
			System.out.println("Request MX");
			break;
		}
		sendData.putShort(QTYPE);
		short QCLASS = 1;
		sendData.putShort(QCLASS);
		// Creates socket used for communication.
		DatagramSocket clientSocket = new DatagramSocket();
		// Calls method to split input IP.
		byte[] ipAddr = splitIP(IP);
		byte[] snd = sendData.array();
		byte[] receiveData = new byte[1024];
		InetAddress ip = InetAddress.getByAddress(ipAddr);
		// creates send and receive packets for communication.
		DatagramPacket packet = new DatagramPacket(snd, snd.length, ip, port);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		// Atempts to send message to DNS Server based on input parameters for
		// timeout and retries.
		clientSocket.setSoTimeout(timeout * 1000);
		int attempt = 1;
		long timeToReceive = 0;
		boolean rc = false;
		long startTime = System.currentTimeMillis();
		while (attempt <= retries && !rc) {
			try {
				clientSocket.send(packet);

				clientSocket.receive(receivePacket);
				long endTime = System.currentTimeMillis();
				timeToReceive = endTime - startTime;
				rc = true;
				System.out.println("Response received after " + (double) timeToReceive / 1000 + " seconds (" + attempt
						+ " retries)");

				clientSocket.close();
				// Parsing the response
				byte[] received = receivePacket.getData();
				ByteBuffer recievedData = ByteBuffer.wrap(received);
				decodeReceivedPacket(sendData, recievedData);
			} catch (SocketTimeoutException e) {
				attempt++;
			}
		}
		if (attempt >= retries) {
			error += "ERROR \t Maximum number of retries " + retries + " exceeded \n";
			System.out.println(error);
			System.exit(0);

		}
	}

	// Helper method used to split IP string to bytearray.
	public static byte[] splitIP(String ip) {
		byte[] result = new byte[4];
		String[] IP = ip.split("\\.");
		for (int i = 0; i < IP.length; i++) {
			try {
				int seg = Integer.valueOf(IP[i]);
				result[i] = (byte) seg;
			} catch (NumberFormatException e) {
				System.out.println("ERROR \t incorrect IP arguments.");
				System.exit(0);

			}
		}
		return result;
	}

	// Helper method used to parse input and handles error.
	public static void inputs(String[] args) {

		if (args.length < 2) {
			System.out.println("ERROR \t too few arguments.");
			System.exit(0);
		}

		website = args[args.length - 1];
		System.out.println("DnsClient sending request for " + website);
		// Iterates through input arguments and modifies appropriate global
		// variables, terminating the program if there are incorrect inputs.
		for (int i = 0; i < args.length; i++) {

			if (args[i].contains("@")) {
				IP = args[i].substring(1, args[i].length());
				System.out.println("Server: " + IP);
			}

			switch (args[i]) {
			case "-t":
				if (isStringInt(args[i + 1])) {
					timeout = Integer.parseInt(args[i + 1]);
					break;
				} else {
					System.out.println("ERROR \t incorrect timeout argument.");
					System.exit(0);
				}

			case "-r":
				if (isStringInt(args[i + 1])) {
					retries = Integer.parseInt(args[i + 1]);
					break;
				} else {
					System.out.println("ERROR \t incorrect retries argument.");
					System.exit(0);
				}

			case "-p":
				if (isStringInt(args[i + 1])) {

					port = Integer.parseInt(args[i + 1]);
					break;
				} else {
					System.out.println("ERROR \t incorrect port argument.");
					System.exit(0);
				}

			case "-mx":
				queryType = 0x000f;
				break;

			case "-ns":
				queryType = 0x0002;
				break;
			}
		}
	}

	public static void GetHeader(ByteBuffer header) {
		// ByteBuffer header = ByteBuffer.allocate(12);
		short flags, QDCOUNT, ANCOUNT, NSCOUNT, ARCOUNT;

		// Generates random ID
		byte[] ID = new byte[2];
		Random rand = new Random();
		rand.nextBytes(ID);
		header.put(ID);
		// initializes question header and sets the appropriate bits.

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

		// return header;

	}

	public static void getQuestion(String msg, ByteBuffer question) {
		// split string message into three segments
		String[] sections = msg.split("\\.");

		for (int i = 0; i < sections.length; i++) {
			// Puts length of section on the Bytebuffer
			question.put((byte) sections[i].length());
			for (int j = 0; j < sections[i].length(); j++) {
				byte ch = (byte) sections[i].charAt(j);
				question.put(ch);
			}
		}
		// Places 0 byte to signal the end of the question.
		question.put((byte) 0);
	}

	// Helper method to help assert if string is integer.
	public static boolean isStringInt(String s) {
		try {
			int x = Integer.parseInt(s);
			if (x > 0) {
				return true;
			} else {
				return false;
			}

		} catch (NumberFormatException ex) {
			return false;
		}
	}

	public static String getID() {
		Random rand = new Random();
		int n = rand.nextInt(65535);
		System.out.println(n);
		String ID = String.format("%16s", Integer.toBinaryString(n)).replace(' ', '0');
		return ID;
	}

	public static String getQR(boolean isQuery) {
		if (isQuery) {
			return "1";
		} else
			return "0";
	}

	// DECODE the received packet and check for errors
	public static void decodeReceivedPacket(ByteBuffer sentData, ByteBuffer receivedData) {
		// HEADER
		// ID
		if (sentData.getShort(0) != receivedData.getShort(0)) {
			error += "ERROR \t incorrect id from the response \n";
		}

		// header involving bits
		byte[] byteArrReceived = new byte[2];
		byteArrReceived[0] = receivedData.get(3);
		byteArrReceived[1] = receivedData.get(2);
		BitSet headerBitsReceived = BitSet.valueOf(byteArrReceived);
		byte[] byteArrSent = new byte[2];
		byteArrSent[0] = sentData.get(3);
		byteArrSent[1] = sentData.get(2);
		BitSet headerBitsSent = BitSet.valueOf(byteArrSent);
		decodeBitsInHeader(headerBitsReceived, headerBitsSent);

		// rest of the header
		int qdCount = receivedData.getShort(4);
		int anCount = receivedData.getShort(6);
		int nsCount = receivedData.getShort(8);
		int arCount = receivedData.getShort(10);

		// System.out.println(qdCount + " " + anCount + " " + nsCount + " "+
		// arCount);

		position = 12;

		// Question
		// check if both questionReceived and ByteBuffer questionSent are the
		// same
		// check either as bytebuffers or as byte arrays

		boolean wrongQuestion = false;
		while (receivedData.get(position) != 0) {
			if (receivedData.get(position) != sentData.get(position)) {
				wrongQuestion = true;
			}
			position++;
		}
		for (int i = 0; i < 5; i++) {
			if (receivedData.get(position) != sentData.get(position)) {
				wrongQuestion = true;
			}
			position++;
		}

		if (wrongQuestion) {
			error += "ERROR \t did not receive the right question \n";
		}

		// Answer
		// parse bytes, need to know when a byte is part of an
		// offset signal

		if (anCount > 0) {
			System.out.println("***Answer Section (" + anCount + " records)***");
		}
		for (int j = 0; j < anCount; j++) {
			decodeAnswer(receivedData, false);
		}

		if (nsCount > 0) {
			System.out.println("***Authority Section (" + nsCount + " records) ignored***");
		}
		for (int j = 0; j < nsCount; j++) {
			decodeAnswer(receivedData, true);
		}

		if (arCount > 0) {
			System.out.println("***Additional Section (" + arCount + " records)***");
		}

		for (int j = 0; j < arCount; j++) {
			decodeAnswer(receivedData, false);
		}

		if (anCount == 0 && arCount == 0) {
			System.out.println("NOTFOUND");
		}
		System.out.println(error);
		System.exit(0);
	}

	public static void decodeAnswer(ByteBuffer receivedData, boolean authoritySection) {

		// Not using this variable: only for debugging purposes
		String nameInAnswer = dnsServerName(receivedData);
		// System.out.println("The name in answer is: " + nameInAnswer);

		// TYPE: 16 bit (2 bytes) specific values
		/*
		 * 0x0001 for a type-A query (host address) 0x0002 for a type-NS query
		 * (name server) 0x000f for a type-MX query (mail server) 0x0005
		 * corresponding to CNAME records.
		 */
		int type = receivedData.getShort(position);
		if (type != 1 && type != 2 && type != 5 && type != 15) {
			System.out.println("type of response not supported");
		}
		position += 2;

		// CLASS: should be 0x0001
		if (receivedData.getShort(position) != 1) {
			error += "ERROR \t not the good class \n";
		}
		position += 2;

		int TTL = 0;

		// TTL: 32 bit (4 byes) check if 0?
		TTL = receivedData.getInt(position);
		if (TTL <= 0) {
			error += "ERROR \t TTL is less or equal to 0 \n";
		}
		position += 4;

		// RDLENGTH: 16 bit int (2 bytes) length of RDATA
		int rdLength = receivedData.getShort(position);
		position += 2;

		// RDATAL: depends on TYPE
		String RData = "";
		if (type == 1) {
			// type A IP Address Record (4 bytes)
			for (int i = 0; i < rdLength; i++) {
				RData += receivedData.get(position) & 255; // convert to
															// unsigned
				if (i != 3) {
					RData += ".";
				}
				position++;
			}
			if (!authoritySection) {
				System.out.println("IP \t" + RData + "\t " + TTL + "\t" + authoritative);
			}
		} else if (type == 2) {
			// type NS server name same type as QNAME
			RData = dnsServerName(receivedData);
			if (!authoritySection) {
				System.out.println("NS \t" + RData + "\t " + TTL + "\t" + authoritative);
			}
		} else if (type == 15) {
			// type MX preference + exchange
			// Preference

			short preference = receivedData.getShort(position);
			position += 2;

			// Exchange
			RData = dnsServerName(receivedData);

			if (!authoritySection) {
				System.out.println("MX \t" + RData + "\t " + preference + "\t" + TTL + "\t" + authoritative);
			}
		} else if (type == 5) {
			// CNAME name of the alias
			RData = dnsServerName(receivedData);
			if (!authoritySection) {
				System.out.println("CNAME \t" + RData + "\t " + TTL + "\t" + authoritative);
			}
		}
	}

	// Decode HEADER bits
	public static void decodeBitsInHeader(BitSet headerReceived, BitSet headerSent) {
		// QR
		if (!headerReceived.get(15)) {
			error += "ERROR \t this is not a response \n";
		} // OPCODE
		if (!headerReceived.get(11, 15).equals(headerSent.get(11, 15))) {
			error += "ERROR \t this is not a standart query \n";
		} // AA
		if (headerReceived.get(10)) {
			// System.out.println("authoritative response");
			authoritative = "auth";
		} else {
			// System.out.println("non authoritative response");
		} // TC
		if (headerReceived.get(9)) {
			System.out.println("truncated response");
		} // RD
		if (headerReceived.get(8)) {
			// System.out.println("recursion requested");
		} // RA
		if (headerReceived.get(7)) {
			// System.out.println("recursion possible from server");
		} else {
			// System.out.println("no recursion possible from server");
			error += "ERROR \t server does not support recursive queries \n";
		}
		// ignored Z (3 bits)
		// RCODE
		if (!headerReceived.get(3) && !headerReceived.get(2) && !headerReceived.get(1) && headerReceived.get(0)) {
			error += "ERROR \t Format error \n";
		} else if (!headerReceived.get(3) && !headerReceived.get(2) && headerReceived.get(1)
				&& !headerReceived.get(0)) {
			error += "ERROR \t Server Failure \n";
		} else if (!headerReceived.get(3) && !headerReceived.get(2) && headerReceived.get(1) && headerReceived.get(0)) {
			error += "ERROR \t Name Failure \n";
		} else if (!headerReceived.get(3) && headerReceived.get(2) && !headerReceived.get(1)
				&& !headerReceived.get(0)) {
			error += "ERROR \t Not implemented \n";
		} else if (!headerReceived.get(3) && headerReceived.get(2) && !headerReceived.get(1) && headerReceived.get(0)) {
			error += "ERROR \t Refused \n";
		}
	}

	// Get server names or alias. Supports offsets
	public static String dnsServerName(ByteBuffer receivedData) {
		int namePosition = position;
		String domainName = "";
		boolean offsetFound = false;
		while (receivedData.get(namePosition) != 0) {
			if (isOffset(receivedData.get(namePosition))) {
				// if offset found, determine the new position
				namePosition = getOffset(receivedData, namePosition);
				offsetFound = true;
			} else {
				// if no offset, read the next byte (a number n) and get
				// the next n bytes, which are letters
				int nextNumberOfChars = receivedData.get(namePosition);
				namePosition++;
				for (int i = 0; i < nextNumberOfChars; i++) {
					domainName += (char) receivedData.get(namePosition);
					namePosition++;
					if (!offsetFound) {
						position = namePosition;
					}
				}
				if (receivedData.get(namePosition) != 0) {
					domainName += ".";
				}
			}
		}
		if (offsetFound) {
			position += 2;
		} else {
			position++;
		}
		return domainName;
	}

	// Determine whether the next two bytes indicate an offset
	public static boolean isOffset(byte bt1) {
		byte[] btArray = new byte[1];
		btArray[0] = bt1;
		BitSet btSet = BitSet.valueOf(btArray);
		if (btSet.get(6) && btSet.get(7)) {
			return true;
		}
		return false;
	}

	// Get the offset value in the next two bytes
	public static int getOffset(ByteBuffer bf, int offsetPosition) {
		byte[] btArray = { bf.get(offsetPosition + 1), bf.get(offsetPosition) };
		BitSet btSet = BitSet.valueOf(btArray);
		int offset = 0;
		for (int i = 0; i < 14; i++) {
			if (btSet.get(i)) {
				offset += Math.pow(2, i);
			}
		}
		return offset;
	}
}
