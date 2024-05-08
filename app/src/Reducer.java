import com.sun.source.tree.TryTree;
import netscape.javascript.JSObject;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Reducer extends Thread{


    private BufferedReader input;  //gets input from worker
    private PrintWriter out;    //sends output to worker
    static BufferedReader inputMaster;
    static PrintWriter outMaster;

    static int counter=0;
     int flag ;
     static String results;

    static String totalFilters;
    static String mapID;

    public Reducer(int flag){
        this.flag=flag;
    }
    public Reducer(Socket workerSocket) throws IOException {
        input = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));  //init to input me to socket pou milaei
        out = new PrintWriter(workerSocket.getOutputStream(),true); //init to output me to socket pou milaei
    }

    public static void main(String[] args) throws IOException {
        //---------------------------------------------------------------------------------------------------------------------------

        System.out.println("Reducer started");
        Socket socket = new Socket("localhost",2222);//connection with server

        inputMaster = new BufferedReader(new InputStreamReader(socket.getInputStream()));  //input poy pairnei plhroforia apo to socket
        outMaster = new PrintWriter(socket.getOutputStream(),true); //writer using output stream

        Reducer masterThread =  new Reducer(1);
        masterThread.start();

        //-----------------------------------SERVER-------------------------------------------------------------------------
        ServerSocket serverSocket = new ServerSocket(3333); // pass port number, init Server socket

        while (true){
            System.out.println("Reducer : Waiting for Workers...");

            Socket worker = serverSocket.accept();    //waits for node to connect
            System.out.println("Worker connected!");

            Reducer workerThread = new Reducer(worker);
            workerThread.start();
        }
        //-----------------------------------------------------------------------------------------------------------------
    }
    @Override
    public void run() {

        if (this.flag == 1) {                   //master
            try {
                System.out.println(inputMaster.readLine()); //msg from master
                while (true){
                    String masterRequest = inputMaster.readLine();
                    System.out.println("master says : " + masterRequest);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {                                             //worker

            //aithmata apo sugkekrimeno worker...(client)
            out.println("Connected to Reducer");

            while (true) {
                try {
                    String filteredrooms = input.readLine();
                    counter++;
                    System.out.println("got : "+ filteredrooms + "counter is at : " + counter);
                    if(counter>=Master.numOfWorkers){
                        results=filteredrooms;
                        outMaster.println(results);
                        counter=0;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
