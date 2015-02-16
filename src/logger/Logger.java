package logger;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

import messagepasser.TimeStampedMessage;
import yamlparser.YamlParser;

public class Logger {
	ServerSocket serversocket;
	File file = new File("log.txt");
	BufferedWriter bw;
	
	ArrayList<TimeStampedMessage> array = new ArrayList<TimeStampedMessage>();
	
	public Logger(String filename) {
		YamlParser yamlparser = new YamlParser();
		try {
			yamlparser.yamiParser(filename);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		String[] loggerinfo = yamlparser.getLoggerInfo();
	    //need to judge the ip first.
		try {
			serversocket = new ServerSocket(Integer.parseInt(loggerinfo[1]));
		} catch (NumberFormatException e) {
			System.out.println("Cannot parse the int");
		} catch (IOException e) {
			System.out.println("IO exception");
		}
	}
	
	public void listen() {
		while (true) {
			try {
				//System.out.println("here1");
				Socket socket = serversocket.accept();
				try {
					bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
				} catch (IOException e1) {
					System.out.println("failure on bufferedwriter");
				}

				//System.out.println("here2");
				ObjectInputStream receive = new ObjectInputStream(socket.getInputStream());
				TimeStampedMessage message;
				try {
					message = (TimeStampedMessage)receive.readObject();
					array.add(message);
				} catch (ClassNotFoundException e) {
					System.out.println("Failure on transversion to TimeStampedMessage");
				}
				
				Collections.sort(array);
				
				bw.write(array.get(0).toString());
				for (int i = 1; i < array.size(); i++) {
					if (array.get(i).compareTo(array.get(i - 1)) > 0) {
						bw.write(" \n------------------------------------------------"
								+ "------------------------------------------------\nThe "
								+ i + "th event :" + array.get(i).toString());
					} else {
						bw.write("\nThe " + i + "th event : \n" + array.get(i).toString());
					}
				}
				bw.newLine();
				//System.out.println("content : " + content);
				//bw.write(message.toString());
				
				String[][] matrix = new String[array.size()][array.size()];
				for (int i = 0; i < array.size(); i++) {
					for (int j = 0; j < array.size(); j++) {
						if (i == j) {
							matrix[i][j] = "*";
						} else {
							int result = array.get(i).compareTo(array.get(j));
							if (result == 0) {
								matrix[i][j] = "0";
							} else if (result == 1) {
								matrix[i][j] = "-1";
							} else {
								matrix[i][j] = "+1";
							}
						}
					}
				}
				
				bw.write("the sequence matrix is given below : \n");
				for (int i = 0; i < array.size(); i++) {
					for (int j = 0; j < array.size(); j++) {
						bw.write(matrix[i][j] + "\t");
					}
					bw.newLine();
				}
				//System.out.println("content : " + content);
				//bw.write(message.toString());
				receive.close();
				socket.close();
				bw.flush();
				
			} catch (IOException e) {
				System.out.println("Failure on acception");
			}
			
		}
		
	}
	
	public static void main(String args[]) {
		Logger logger = new Logger("ConfigurationFile.txt");
		logger.listen();
	}
	
	
}

