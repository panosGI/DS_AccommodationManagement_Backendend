 import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Thread {

    public static void main(String[]args){
        //user
        try {
            System.out.println("User started");
            Socket socket = new Socket("localhost",9090);//connection with server

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));  //input poy pairnei plhroforia apo to socket
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); //input poy pairnei plhroforia apo to keyboard (system.in)
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true); //writer using output stream

            while (true) {
                String serverResponse = input.readLine();   //gets input from server
                System.out.println("Server says : " + serverResponse);
                System.out.println("--> Help for user : Type a SPECIFIC COMMAND if server asked you OR ELSE type ANYTHING to proceed if server gave no instructions");
                String command = keyboard.readLine();
                out.println(command);       //send it to server..
            }
            //socket.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
//...USER OF APP