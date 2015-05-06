import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client{
	public static void main(String[] args) throws IOException{
		String serverName = args[0];	//TODO: obsłużyć brak parametru
		int portCmd = 21;
		int portData = 20;
		
		System.out.println("Connecting to " + serverName + " on port " + portCmd);
		Socket socketCmd = new Socket(serverName, portCmd);
		System.out.println("Just connected to " + socketCmd.getRemoteSocketAddress());
		
		ServerSocket clientData = new ServerSocket(portData);
		Socket socketData = clientData.accept();
		//System.out.println("Just connected to " + socketData.getRemoteSocketAddress());
		
		try{
			DataOutputStream outCmd = new DataOutputStream(socketCmd.getOutputStream());
			DataInputStream inCmd = new DataInputStream(socketCmd.getInputStream());
			
			DataOutputStream outData = new DataOutputStream(socketData.getOutputStream());
			DataInputStream inData = new DataInputStream(socketData.getInputStream());
			
			Scanner scanner = new Scanner(System.in);
			String str = "";
			
			while(true){
				str = scanner.nextLine();
				outCmd.writeUTF(str);
				
				if(str.compareTo("QUIT") == 0){
					break;
				}else if(str.compareTo("NLST") == 0){
					System.out.println("Server says: " + inData.readUTF());
				}else{
					System.out.println("Server says: " + inCmd.readUTF());
				}
			}
			scanner.close();
		}finally{
			socketCmd.close();
			socketData.close();
			clientData.close();
		}
	}
}