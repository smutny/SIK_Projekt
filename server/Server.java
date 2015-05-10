package server;

import java.net.*;
import java.io.*;

public class Server{
	public static void main(String[] args) throws IOException{
		int portCmd = 21;
		//int portData = 20;
		
		ServerSocket server = new ServerSocket(portCmd);
		try{
			while(true){
				Socket socket = server.accept();
				
				System.out.println("Socket port: " + socket.getPort());
				System.out.println("Just connected to " + socket.getRemoteSocketAddress());
				try{
					new ServerThread(socket);
				}catch(IOException e){
					socket.close();
				}
			}
		}finally{
			server.close();
		}
	}
}