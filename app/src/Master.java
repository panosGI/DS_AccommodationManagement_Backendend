
import org.json.simple.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Master extends Thread{

    private Socket clientSocket = null;         //Client socket me to opoio tha milaei to particular master object
    private BufferedReader in;  //gets input
    private PrintWriter out;    //sends output

    private static BufferedReader inReducer;  //I/O with reducer 'client'- server
    private static PrintWriter outReducer;
    private int flag = 0;

    public static ArrayList<Manager> managers = new ArrayList<Manager>();        //arraylist me tous manager synolika toy MASTER

    public static ArrayList<BufferedReader> workersInputs = new ArrayList<BufferedReader>();
    public static ArrayList<PrintWriter> workersOutputs = new ArrayList<PrintWriter>();

    public static int numOfWorkers;

    public Master(Socket clientSocket) throws IOException {
        this.clientSocket=clientSocket;             //pairnei to socket wste na to xeiristei to sugkekrimeno master obj poy ftiaxthke
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));  //init to input me to socket pou milaei
        out = new PrintWriter(clientSocket.getOutputStream(),true); //init to output me to socket pou milaei
    }

    @Override
    public void run(){
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            String inputFromClient;

            out.println("-------------------------Welcome to the APP----------------------------");
            inputFromClient = getInputFromClient();

            out.println("-------------------------TYPE MANAGER FOR MANAGER MODE OR TYPE TENANT FOR TENANT MODE----------------------------");
            inputFromClient = getInputFromClient();

            if (inputFromClient.contains("MANAGER")) {
                //----------------------------------------------------------------------------------------------------------------
                out.println("----------MANAGER MODE---------");
                inputFromClient = getInputFromClient();

                Manager user = null;  //anafora se manager (h new h existing...analoga to choice)

                out.println("Is it your first time on the platform as a manager? TYPE YES OR NO");
                inputFromClient = getInputFromClient();

                if (inputFromClient.contains("YES")) {

                    //new user
                    out.println("give us your name , so we can remember you : ");
                    inputFromClient = getInputFromClient();

                    String nameTyped = inputFromClient;

                    try {
                        user = new Manager(clientSocket);               //neo manager antikeimeno
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    user.managerName = nameTyped;       //me auto to onoma ws attribute
                    managers.add(user);                 //krata ton sthn lista (static lista gia olh th master class ara anhkei sto server kai oxi se sugkekrimeno master object, dhladh thread poy exyphretei sygkekrimeno client)..

                    out.println("User Saved : " + nameTyped);
                    inputFromClient = getInputFromClient(); //to continue...

                } else if (inputFromClient.contains("NO")) {

                    //existing user
                    out.println("what is your name? please write here : ");
                    inputFromClient = getInputFromClient();

                    String nameTyped = inputFromClient;


                    boolean found = false;
                    for (Manager m : managers) {
                        if (m.managerName.contains(nameTyped)) {
                            user = m;               //ton brhkame opote h anafora user koitaei ston yparxonta manager 'm'
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        out.println("welcome : " + nameTyped);
                        inputFromClient = getInputFromClient(); //to continue...
                    } else {
                        out.println("Something went wrong... you typed :" + inputFromClient + " no user with such name...Returning to home screen...");
                        inputFromClient = getInputFromClient(); //to continue...
                        run();
                    }


                } else {
                    out.println("Something went wrong... you typed :" + inputFromClient + "Returning to home screen...");
                    inputFromClient = getInputFromClient(); //to continue...
                    run();
                }

                //EPILOGES SE MANAGER MODE

                out.println("~~~~~~~~~~Manager's Options~~~~~~~~~~~~~");
                inputFromClient = getInputFromClient();//to continue...

                out.println("|||If you want to add a room, please type ADD|||If you want to add availiable dates for your rooms please type DATES|||If you want to show reservations please type RESERVATIONS|||");
                inputFromClient = getInputFromClient();

                if (inputFromClient.contains("ADD")) {
                    user.addRoom();
                } else if (inputFromClient.contains("DATES")) {
                    try {
                        user.addDate();
                    } catch (ParseException | IOException e) {
                        e.printStackTrace();
                    }
                } else if (inputFromClient.contains("RESERVATIONS")) {
                    user.showReservations();
                } else {
                    out.println("Something went wrong... you typed :" + inputFromClient + "Returning to home screen...");
                    inputFromClient = getInputFromClient(); //to continue...
                    run();
                }

                //-------------------------------------------------------------------------------------------------------------

            } else if (inputFromClient.contains("TENANT")) {
                //-----------------------------------------------------------------------------------------------------------
                out.println("----------TENANT MODE---------");
                inputFromClient = getInputFromClient(); //to continue...

                out.println("Please give us your name : ");
                inputFromClient = getInputFromClient(); //to continue...


                Tenant user = null;
                try {
                    user = new Tenant(clientSocket);       //creating tenant user
                } catch (IOException e) {
                    e.printStackTrace();
                }
                user.TenantName = inputFromClient;

                out.println("Showing availiable housing");
                inputFromClient = getInputFromClient(); //to continue...

                Filter filters = null;

                filters = user.search();  //Calls search to get filters...


                //master stelnei ta filtra se olous tous worker...
                for (int i = 0; i < workersOutputs.size(); i++) {
                    workersOutputs.get(i).println("Sending you filters...[FILTER]");

                  /*
                    workersOutputs.get(i).println(filters.area);
                    workersOutputs.get(i).println(filters.startDate);
                    workersOutputs.get(i).println(filters.endDate);
                    workersOutputs.get(i).print(filters.noOfPersons);
                    workersOutputs.get(i).print(filters.price);
                    workersOutputs.get(i).print(filters.stars);
                                                                   */
                }

                try {
                    outReducer.println("I WANT FILTERED RESULTS");

                    String results = inReducer.readLine();

                    out.println("Results based on your criteria : " + results);
                    inputFromClient = getInputFromClient();

                } catch (IOException e) {
                    e.printStackTrace();
                }


                out.println("....press enter to continue....");
                inputFromClient = getInputFromClient();




                out.println("Do you wish to make a booking? TYPE YES OR NO :");
                inputFromClient = getInputFromClient();

                if (inputFromClient.contains("YES")) {
                    try {
                        user.book();
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }

                } else if (inputFromClient.contains("NO")) {

                    out.println("Do you wish to rate a room? TYPE YES OR NO");
                    inputFromClient = getInputFromClient();

                    if (inputFromClient.contains("YES")) {

                        out.println("Tell us the name of the room you wish to rate : ");
                        inputFromClient = getInputFromClient();

                        user.rate(inputFromClient);
                    } else {
                        return;
                    }

                } else {
                    out.println("Something went wrong... you typed :" + inputFromClient + "Returning to home screen...");
                    inputFromClient = getInputFromClient(); //to continue...
                    run();
                }


                //---------------------------------------------------------------------------------------------------------------
            } else {
                out.println("Something went wrong... you typed :" + inputFromClient + "Returning to home screen...");
                inputFromClient = getInputFromClient(); //to continue...
                run();
            }
            //---------------------------------------------------------------------------------------------------------------------
            out.println("Do you wish to exit application or return to the home screen. TYPE RETURN OR TYPE ANYTHING ELSE TO EXIT");
            inputFromClient = getInputFromClient();

            if (inputFromClient.contains("RETURN")) {
                out.println("returning ...");
                inputFromClient = getInputFromClient(); //to continue...
                run();
            } else {
                out.println("EXIT...");
                inputFromClient = getInputFromClient(); //to continue...
                try {
                    terminate();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //----------------------------------------------------------------------------------------------------------------------


            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    }

    public static void main(String[]args) throws IOException, InterruptedException {

        //config........................................................................................................
        Scanner scanner = new Scanner(System.in);

        System.out.println("CONFIG SETTINGS FOR THE APP\n");
        System.out.println("Give number of workers : ");

        int numOfWorkers = scanner.nextInt();
        Master.numOfWorkers = numOfWorkers;

        System.out.println("Worker Servers : " + numOfWorkers);
        initServer(numOfWorkers);
    }

    //Establish Server-clients connection----------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static void initServer(int numOfWorkers){
        try {

            ServerSocket serverSocketR = null;
            try {
                serverSocketR = new ServerSocket(2222);

                System.out.println(" \n Server waiting for Reducer...");

                Socket reducer = serverSocketR.accept();  //waits for reducer to connect to server and returns socket
                System.out.println("Reducer connected!");

                inReducer = new BufferedReader(new InputStreamReader(reducer.getInputStream()));  //init to input me to socket pou milaei
                outReducer = new PrintWriter(reducer.getOutputStream(),true); //init to output me to socket pou milaei

                outReducer.println("Connection with master established");

            } catch (IOException e) {
                e.printStackTrace();
            }

            //-------------------------------------------------------------------------------------------------------------------
            ArrayList<String> IPworkers = new ArrayList<String>();  //IP twn worker
            Scanner scanner = new Scanner(System.in);

            for (int i=0;i<numOfWorkers;i++){
                String IP;
                System.out.println("Type IP adress for Worker Server [" + i + "] so that master can connect : (gia to example press anything IP--->localhost)");
                IP=scanner.nextLine();
                IP="localhost";  //symbatika gia to example...
                IPworkers.add(IP); //oloi oi workers auth thn IP
            }
            //--------------------------workers----------------------------------------------------------------------------
            for (int i=0;i<numOfWorkers;i++){

                System.out.println("Connecting to Worker....");

                //localhost ---> IP kathe worker
                Socket socket = new Socket(IPworkers.get(i),5555);
                //connection me sugkekrimeno Worker

                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));  //input apo sugkekrimeno worker
                PrintWriter out = new PrintWriter(socket.getOutputStream(),true); // output gia sygkekrimeno worker

                workersInputs.add(input);
                workersOutputs.add(out);
                //Saves ta I/0 epikoinwnias gia kathe enan apo tous n worker ...nodeID=Index...
                System.out.println(input.readLine());  //mhnyma worker server
            }

            //---------------------------------------SERVER-----------------------------------------------------------------

            ServerSocket serverSocket = new ServerSocket(9090);

            while (true) {
                System.out.println(" \n Server waiting for users...");

                Socket client = serverSocket.accept();  //waits for clients to connect to server and returns socket
                System.out.println("user connected!");

                Master clientThread = new Master(client); //neo master object gia na diaxeiristei to sugkekrimeno client

                clientThread.start();
            }
            //----------------------------------------SERVER----------------------------------------------------------------
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public String getInputFromClient(){
        try {
            String request;
            request = in.readLine();  //gets input
            return request;
        } catch (IOException e) {
            e.printStackTrace();
            return "exception....";
        }
    }
    private void terminate() throws IOException {
        out.println("CONNECTION ENDS.");
        getInputFromClient();
        in.close();
        out.close();
        clientSocket.close();
        System.exit(0);
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
}
