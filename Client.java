import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client{
	public static void main(String[] args) throws IOException{
		
		boolean passiveMode = false;
		
		String defaultName = "localhost";
		String serverName = (args.length > 0 ? args[0] : defaultName);	//nadaje domyslna nazwe jesli brak parametru
		int portCmd = 21;
		int portData = 20;
		
		System.out.println("Lacze z " + serverName + " na porcie " + portCmd);
		Socket socketCmd = new Socket(serverName, portCmd);
		System.out.println("Wlasnie polaczono z " + socketCmd.getRemoteSocketAddress());
		
		ServerSocket clientData = new ServerSocket(socketCmd.getLocalPort() + 1);
		Socket socketData = clientData.accept();
		//System.out.println("Just connected to " + socketData.getRemoteSocketAddress());
		
		Socket socketPasv = null;
		DataOutputStream outPasv = null;
		DataInputStream inPasv = null;
		
		//System.out.println("socketCmd: z " + socketCmd.getLocalPort() + " do " + socketCmd.getPort());
		//System.out.println("socketData: z " + socketData.getLocalPort() + " do " + socketData.getPort());
		
		try{
			DataOutputStream outCmd = new DataOutputStream(socketCmd.getOutputStream());
			DataInputStream inCmd = new DataInputStream(socketCmd.getInputStream());
			
			DataOutputStream outData = new DataOutputStream(socketData.getOutputStream());
			DataInputStream inData = new DataInputStream(socketData.getInputStream());
			
			Scanner scanner = new Scanner(System.in);
			String str = "";
			
			System.out.println(inCmd.readUTF());
			while (true) {
				str = scanner.nextLine();
				outCmd.writeUTF(str);
				String str2 = inCmd.readUTF();
				System.out.println(str2);
				if (str2.contains("Zalogowano")) break;
			}
			
			while(true){
				System.out.print("ftp> ");
				str = scanner.nextLine();
				outCmd.writeUTF(str);
				
				if(str.compareTo("QUIT") == 0){
					break;
				}else if(str.compareTo("NLST") == 0){
					String str1;
					while (true){
						if(passiveMode) str1 = inPasv.readUTF();
						else str1 = inData.readUTF();
						
						System.out.println(str1);
						if (str1.compareTo("<koniec listy>") == 0 ){
							break;
						}
					}
				}else if(str.compareTo("PASV") == 0){
					if(passiveMode){
						System.out.println(inCmd.readUTF());
					}
					else{
						int portPasv = inData.readInt();
						socketData.close();
						clientData.close();
						socketPasv = new Socket(serverName, portPasv, socketCmd.getLocalAddress(), socketCmd.getLocalPort() + 1);
						outPasv = new DataOutputStream(socketPasv.getOutputStream());
						inPasv = new DataInputStream(socketPasv.getInputStream());
						
						passiveMode = true;
						System.out.println(inCmd.readUTF());
						//System.out.println("socketCmd: z " + socketCmd.getLocalPort() + " do " + socketCmd.getPort());
						//System.out.println("socketPasv: z " + socketPasv.getLocalPort() + " do " + socketPasv.getPort());
					}
				}else{
					System.out.println(inCmd.readUTF());
				}
			}
			scanner.close();
		}finally{
			if(passiveMode){
				socketCmd.close();
				socketPasv.close();
			}
			else{
				socketCmd.close();
				socketData.close();
				clientData.close();
			}
		}
	}
}