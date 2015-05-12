package server;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread{
	boolean passiveMode;
	private Socket socketCmd;
	private Socket socketData;
	private ServerSocket serverPasv;
	private Socket socketPasv;
	private DataInputStream inCmd;
	private DataOutputStream outCmd;
	private DataInputStream inData;
	private DataOutputStream outData;
	private DataInputStream inPasv;
	private DataOutputStream outPasv;
	private String defaultPath = "./example"; 								//domyslna sciezka folderu z plikami
	
	public String[] listFiles(String path) throws IOException{
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
	}
	
	public void nlst(DataOutputStream outStream) throws IOException{
		outStream.writeUTF("<lista plikow w katalogu>");
		String[] out = listFiles(defaultPath);
		for (int i = 0; i < out.length; i++) {
			outStream.writeUTF(out[i]);
		}
		outStream.writeUTF("<koniec listy>");
	}
	
	public ServerThread(Socket sCmd) throws IOException{
		passiveMode = false;
		
		socketCmd = sCmd;
		inCmd = new DataInputStream(socketCmd.getInputStream());
		outCmd = new DataOutputStream(socketCmd.getOutputStream());
		
		socketData = new Socket(socketCmd.getInetAddress(), socketCmd.getPort() + 1, socketCmd.getLocalAddress(), 20);		//tu zmienić port
		inData = new DataInputStream(socketData.getInputStream());
		outData = new DataOutputStream(socketData.getOutputStream());
		//wystartowanie wątku
		start();
	}
	
	public void run(){
		//System.out.println("socketCmd: z " + socketCmd.getLocalPort() + " do " + socketCmd.getPort());
		//System.out.println("socketData: z " + socketData.getLocalPort() + " do " + socketData.getPort());
		try{
			outCmd.writeUTF("Prosze podac login");
			while (true) {
				String str2 = inCmd.readUTF();
				if (str2.compareTo("anonymous") == 0 ) {
					outCmd.writeUTF("Zalogowano jako anonymous");
					break;
				} else {
					outCmd.writeUTF("Prosze podac login");
				}
			}
			
			String str = "";
			
			while(true){
				str = inCmd.readUTF();
				System.out.println(str);
				
				if(str.compareTo("QUIT") == 0){
					break;
				}else if(str.compareTo("NLST") == 0){
					if(passiveMode) nlst(outPasv);
					else nlst(outData);
				}else if(str.compareTo("PASV") == 0){
					if(passiveMode){
						outCmd.writeUTF("Tryb pasywny jest juz wlaczony");
					}
					else{
						int portPasv = 6666;									//tu zmienić port
						serverPasv = new ServerSocket(portPasv);
						outData.writeInt(portPasv);
						
						socketPasv = serverPasv.accept();
						inPasv = new DataInputStream(socketPasv.getInputStream());
						outPasv = new DataOutputStream(socketPasv.getOutputStream());
						
						socketData.close();
						passiveMode = true;
						outCmd.writeUTF("Tryb pasywny zostal wlaczony");
						//System.out.println("socketCmd: z " + socketCmd.getLocalPort() + " do " + socketCmd.getPort());
						//System.out.println("socketPasv: z " + socketPasv.getLocalPort() + " do " + socketPasv.getPort());
					}
				}else{
					outCmd.writeUTF("Nieznana komenda: " + str);
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
					socketData.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
}