


import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;



public class Tenant {

    String TenantName;

    private Socket clientSocket = null;         //Client socket me to opoio tha milaei to particular master object
    private BufferedReader in;  //gets input
    private PrintWriter out;    //sends output

    public Tenant(Socket clientSocket) throws IOException {
        this.clientSocket=clientSocket;             //pairnei to socket wste na to xeiristei to sugkekrimeno master obj poy ftiaxthke
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));  //init to input me to socket pou milaei
        out = new PrintWriter(clientSocket.getOutputStream(),true); //init to output me to socket pou milaei
    }


    public Filter search(){

        String inputFromClient;
        Filter filters = null;


        out.println("select your filters");
        inputFromClient=getInputFromClient();


        out.println("type FILTER to select filters or type NO for no filters : ");
        inputFromClient=getInputFromClient();


        //filters...........
        String area="";
        String startDate="";
        String endDate="";
        int noOfPersons=0;
        int price=0;
        int stars=0;
        //..................

        if(inputFromClient.contains("NO")){

            filters = new Filter(area,startDate,endDate,noOfPersons,price,stars);

            out.println("unfiltered results loading....");
            inputFromClient=getInputFromClient();

        } else if (inputFromClient.contains("FILTER")) {

            out.println("Do you want to filter the areas? TYPE YES OR NO : ");
            inputFromClient=getInputFromClient();

            if(inputFromClient.contains("YES")){

                out.println("Tell us the area you want to check  : ");
                inputFromClient=getInputFromClient();

                area=inputFromClient;

            }

            out.println("Do you want to filter the dates? TYPE YES OR NO : ");
            inputFromClient=getInputFromClient();

            if(inputFromClient.contains("YES")){

                out.println("Give us the dates you want to check (format ex. 15/4/23-18/4/23). Firstly, give us the startDate(from when :)");
                inputFromClient=getInputFromClient();

                startDate=inputFromClient;

                out.println("Now, please give us the endDate(Until when :)");
                inputFromClient=getInputFromClient();

                endDate=inputFromClient;
            }

            out.println("Do you want to filter the number of persons you wish your room to FIT ? TYPE YES OR NO : ");
            inputFromClient=getInputFromClient();

            if(inputFromClient.contains("YES")){

                out.println("Give the number of people your room wish to fit : ");
                inputFromClient=getInputFromClient();

                noOfPersons = Integer.parseInt(inputFromClient);
            }

            out.println("Do you want to filter the price? TYPE YES OR NO : ");
            inputFromClient=getInputFromClient();

            if(inputFromClient.contains("YES")){

                out.println("give the maximum price you are willing to pay:");
                inputFromClient=getInputFromClient();

                price =Integer.parseInt(inputFromClient);
            }

            out.println("Do you want to filter the stars rating TYPE YES OR NO : ");
            inputFromClient=getInputFromClient();

            if(inputFromClient.contains("YES")){

                out.println("give the minimum stars you wish your room to have :");
                inputFromClient=getInputFromClient();

                stars =Integer.parseInt(inputFromClient);
            }

            filters = new Filter(area,startDate,endDate,noOfPersons,price,stars);

            out.println("filtered results loading....");
            inputFromClient=getInputFromClient();
        }
        else {
            out.println("Something went wrong... you typed : " + inputFromClient + "..." );
            inputFromClient = getInputFromClient(); //to continue...
        }
        return filters;
    }




    public void book() throws IOException, ParseException {
        String inputFromClient;

        out.println("Give the name of the room you want to book : ");
        inputFromClient=getInputFromClient();

        String roomname = inputFromClient;
        int nodeID = Manager.hash(roomname) % Master.numOfWorkers;

        String avSdate;
        String avEdate;

        out.println("Tell us from when you want to book it (MM/DD/YYYY): ");
        inputFromClient=getInputFromClient();

        String startdate=inputFromClient;

        out.println("Tell us until when you want to book it (MM/DD/YYYY): ");
        inputFromClient=getInputFromClient();

        String enddate=inputFromClient;

       /* BookWithDates(nodeID,roomname,startdate,enddate); //UPDATE ROOM IN WORKER

        DateFormat dateFormat = new SimpleDateFormat("DD/MM/YYYY");
        Date startDate = dateFormat.parse(startdate);
        Date endDate = dateFormat.parse(enddate);

        PairDate pairDate = new PairDate(startDate,endDate);

        for(Manager m : Master.managers){                   //UPDATE ROOM IN MANAGER
           for(int i=0;i<=m.roomsOwned.size();i++){
               if(m.roomsOwned.get(i).get("roomName").equals(roomname)){
                    if(!m.BookedDates.get(roomname).equals(pairDate)){
                         m.BookedDates.put(roomname,pairDate);
                    }else{
                        out.println("ALREADY BOOKED");
                        inputFromClient=getInputFromClient();
                    }
               }
           }
        }*/

        out.println("Booking performed");
        inputFromClient=getInputFromClient();

    }

    public void rate(String rateName) {

        String inputFromClient;
        int nodeID = Manager.hash(rateName) % Master.numOfWorkers; //poios exei to room

        out.println("Give us the rating [1-5] : ");
        inputFromClient=getInputFromClient();

        String rating = inputFromClient;

    }
    private void BookWithDates(int nodeID,String room,String startdate,String enddate) throws IOException {
        Master.workersOutputs.get(nodeID).println("Updating ...[BOOK]");

        Master.workersOutputs.get(nodeID).println(room);  //stelnei room
        Master.workersOutputs.get(nodeID).println(startdate);  //stelnei start kai end Dates
        Master.workersOutputs.get(nodeID).println(enddate);
    }


    private String getInputFromClient() {
        try {
            String request;
            request = in.readLine();  //gets input
            return request;
        } catch (IOException e) {
            e.printStackTrace();
            return "exception....";
        }
    }
}





