//package server;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread{
	private Socket socketCmd;
	private Socket socketData;
	private DataInputStream inCmd;
	private DataOutputStream outCmd;
	private DataInputStream inData;
	private DataOutputStream outData;
	
	public ServerThread(Socket sCmd) throws IOException{
		socketCmd = sCmd;
		inCmd = new DataInputStream(socketCmd.getInputStream());
		outCmd = new DataOutputStream(socketCmd.getOutputStream());
		
		socketData = new Socket(socketCmd.getInetAddress(), 20);	//tu zmienić port
		inData = new DataInputStream(socketData.getInputStream());
		outData = new DataOutputStream(socketData.getOutputStream());
		//wystartowanie wątku
		start();
	}
	
	public void run(){
		try{
			String str = "";
			
			while(true){
				str = inCmd.readUTF();
				System.out.println(str);
				
				if(str.compareTo("QUIT") == 0){
					break;
				}else if(str.compareTo("NLST") == 0){
					outData.writeUTF("<lista plikow w katalogu>");		//TODO
				}else{
					outCmd.writeUTF(str);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				socketCmd.close();
				socketData.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
}