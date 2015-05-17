package server;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread{
	boolean loggedIn;
	boolean passiveMode;
	private Socket socketCmd;
	private Socket socketData;
	private ServerSocket serverPasv;
	private Socket socketPasv;
	private BufferedWriter outCmd;
	private BufferedReader inCmd;
	private BufferedWriter outData;
	private BufferedReader inData;
	private BufferedWriter outPasv;
	private BufferedReader inPasv;
	private String defaultPath = "./example"; 								//domyslna sciezka folderu z plikami
	
	/*public String[] listFiles(String path) throws IOException{
		if(path.compareTo("") == 0){
			path = "./";
		}
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		String[] outputList = new String[listOfFiles.length];
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				outputList[i] = ("File " + listOfFiles[i].getName());
			} else if (listOfFiles[i].isDirectory()) {
				outputList[i] = ("Directory " + listOfFiles[i].getName());
			}
		}
		return outputList;
	}*/
	
	public String listFiles2(String path) throws IOException{
		if(path.compareTo("") == 0) path = "./";
		File folder = new File(path);
		String[] listOfFiles = folder.list();
		
		String files = "";
		for(int i = 0; i < listOfFiles.length; i++){
			files += listOfFiles[i] + "\r\n";
		}
		
		return files;
	}
	
	public void nlst(Socket socket, BufferedWriter out) throws IOException{
		if(socket != null && !socket.isClosed()){
			outCmd.write("150 Opening ASCII mode data connection for file list\r\n");
			outCmd.flush();
			
			out.write(listFiles2(defaultPath));
			out.flush();
			
			outCmd.write("226 Transfer complete\r\n");
			outCmd.flush();
			
			socket.close();
		}
		else{
			outCmd.write("425 Can't open data connection.\r\n");
			outCmd.flush();
		}
	}
	
	public ServerThread(Socket sCmd) throws IOException{
		loggedIn = false;
		passiveMode = false;
		
		socketCmd = sCmd;
		inCmd = new BufferedReader(new InputStreamReader(socketCmd.getInputStream()));
		outCmd = new BufferedWriter(new OutputStreamWriter(socketCmd.getOutputStream()));
		
		//wystartowanie wątku
		start();
	}
	
	public void run(){
		try{
			String str = "";
			outCmd.write("220 ProFTPD 1.3.4a Server ready.\r\n");
			outCmd.flush();
			
			while(true){
				str = inCmd.readLine();
				System.out.println(str);
				
				if(str.startsWith("USER ")){
					if(str.substring(5).compareTo("anonymous") == 0){
						outCmd.write("331 Anonymous login ok, send your complete email address as your password\r\n");
						outCmd.flush();
						str = inCmd.readLine();
						System.out.println(str);
						outCmd.write("230 Anonymous access granted, restrictions apply\r\n");
						outCmd.flush();
						loggedIn = true;
					}
					else{
						outCmd.write("530 Not logged in.\r\n");
						outCmd.flush();
						loggedIn = false;
					}
				}
				else if(str.startsWith("PORT ")){
					if(loggedIn){
						String[] parts = str.substring(5).split(",");
						String address = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
						int port = Integer.parseInt(parts[4]) * 256 + Integer.parseInt(parts[5]);
						
						try{
							socketData = new Socket(address, port, socketCmd.getLocalAddress(), 20);
							//inData = new BufferedReader(new InputStreamReader(socketData.getInputStream()));
							outData = new BufferedWriter(new OutputStreamWriter(socketData.getOutputStream()));
							
							outCmd.write("200 PORT command successful\r\n");
							outCmd.flush();
						}catch(ConnectException ce){
							outCmd.write("501 Syntax error in parameters or arguments.\r\n");
							outCmd.flush();
						}
					}
					else{
						outCmd.write("530 Not logged in.\r\n");
						outCmd.flush();
					}
				}
				else if(str.compareTo("PASV") == 0){
					if(loggedIn){
						int portPasv = 6666;									//tu zmienić port
						int port1 = portPasv/256;
						int port2 = portPasv%256;
						String addressAndPort = socketCmd.getLocalAddress().getHostAddress().replace('.', ',') + "," + port1 + "," + port2;
						
						serverPasv = new ServerSocket(portPasv);
						serverPasv.setSoTimeout(10000);
						
						outCmd.write("227 Entering Passive Mode (" + addressAndPort + ").\r\n");
						outCmd.flush();
						
						try{
							socketPasv = serverPasv.accept();
							//inPasv = new BufferedReader(new InputStreamReader(socketPasv.getInputStream()));
							outPasv = new BufferedWriter(new OutputStreamWriter(socketPasv.getOutputStream()));
							
							passiveMode = true;
						}catch(SocketTimeoutException ste){
							serverPasv.close();
							ste.printStackTrace();
						}
					}
					else{
						outCmd.write("530 Not logged in.\r\n");
						outCmd.flush();
					}
				}
				else if(str.compareTo("NLST") == 0){
					if(loggedIn){
						if(passiveMode){
							nlst(socketPasv, outPasv);
							passiveMode = false;
						}
						else{
							nlst(socketData, outData);
						}
					}
					else{
						outCmd.write("530 Not logged in.\r\n");
						outCmd.flush();
					}
				}
				else if(str.compareTo("QUIT") == 0){
					outCmd.write("221 Service closing control connection.\r\n");
					outCmd.flush();
					loggedIn = false;
					break;
				}
				else{
					outCmd.write("502 Command not implemented.\r\n");
					outCmd.flush();
					try{
						if(!socketData.isClosed()) socketData.close();
					}catch(NullPointerException npe){}
					
					try{
						if(!socketPasv.isClosed()){
							passiveMode = false;
							serverPasv.close();
							socketPasv.close();
						}
					}catch(NullPointerException npe){}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(passiveMode){
					socketCmd.close();
					serverPasv.close();
					socketPasv.close();
				}
				else{
					socketCmd.close();
					if(socketData != null){
						socketData.close();
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}