import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import java.util.Scanner;

public class Manager {

        String managerName;
        ArrayList<JSONObject> roomsOwned = new ArrayList<org.json.simple.JSONObject>();         //dwmatia tou sugkekrimenou manager
        HashMap<String, PairDate> availiableDates = new HashMap<String,PairDate>();  //dates availiable gia kathe dwmatio tou sugkekrimenou manager(apo - mexri)
        HashMap<String, PairDate> BookedDates = new HashMap<String,PairDate>();  //dates booked gia kathe dwmatio tou sugkekrimenou manager(apo - mexri)

        private Socket clientSocket = null;         //Client socket me to opoio tha milaei to particular master object
        private BufferedReader in;  //gets input
        private PrintWriter out;    //sends output

    public Manager(Socket clientSocket) throws IOException {
        this.clientSocket=clientSocket;             //pairnei to socket wste na to xeiristei to sugkekrimeno master obj poy ftiaxthke
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));  //init to input me to socket pou milaei
        out = new PrintWriter(clientSocket.getOutputStream(),true); //init to output me to socket pou milaei
    }


    void addRoom(){
        String inputFromClient;

        out.println("dwse to path gia json (grapse otidhpote gia to example)");
        inputFromClient = getInputFromClient();

        String path = inputFromClient;  //typika...
        path = "E:\\SXOLH\\SXOLH 5o ETOS\\katanemhmena\\app\\extra\\room.json";      //gia to example

        //read--------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        JSONParser parser = new JSONParser();
        try {
            FileReader reader = new FileReader(path); // read
            Object obj = parser.parse(reader);

            JSONObject jsonObject = (JSONObject) obj;   //cast tis plhrofories se json object


            // Assuming your JSON has a key "roomName"
            String roomName = (String)jsonObject.get("roomName");   //get to name apo to room pou o master ekane read se json morfh


            //epilogh random worker node  pou tha apothikeftei to kataluma me voithia hash sunarthshs
            int nodeID = hash(roomName) % Master.numOfWorkers ;

            out.println("Your room will be saved in Worker : [" + nodeID +"]   (DEBUGING PURPOSES-IGNORE)" );
            inputFromClient=getInputFromClient();

            saveToWorker(nodeID,jsonObject); //saving room to node...

            this.roomsOwned.add(jsonObject);         //save room ston sugkekrimeno manager

            out.println("saved : " + roomName);
            inputFromClient=getInputFromClient();

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //telos to read-------------------------------------------------------------------------------------------------------------------------------------------------------------------
    }
    void addDate() throws ParseException, IOException {
            String inputFromClient;

            String yourRooms ="";
            for(int i=0;i<roomsOwned.size();i++){
                yourRooms += "||" + roomsOwned.get(i).get("roomName") + " || ";
            }

            out.println("your rooms are  : " + yourRooms);
            inputFromClient=getInputFromClient();


            out.println("Tell us the name of the room you want to set the availiable dates for : ");
            inputFromClient=getInputFromClient();

            String room = inputFromClient;

            out.println("room given : " + room);
            inputFromClient=getInputFromClient();

            out.println("Give us the availiable dates for this room (Format will be: from StartDate until EndDate). First, give us the StartDate (MM/DD/YYYY):");
            inputFromClient=getInputFromClient();

            String startdate = inputFromClient;

            out.println("Now give us the EndDate (MM/DD/YYYY) : ");
            inputFromClient=getInputFromClient();

            String enddate = inputFromClient;

            DateFormat dateFormat = new SimpleDateFormat("DD/MM/YYYY");
            Date startDate = dateFormat.parse(startdate);
            Date endDate = dateFormat.parse(enddate);

            PairDate avDates = new PairDate(startDate,endDate); //ta avaliable dates pou mas edwse gia to sugkekrimeno room o sugkekrimenos manager
            this.availiableDates.put(room,avDates);         //add sthn lista me ta avDates gia kathe dwmatio tou sugkekrimenou manager

            int nodeID=hash(room) % Master.numOfWorkers;  //poios to xei to room
            updateWorkerWithDates(nodeID,room,startdate,enddate); //bale ston worker pou diaxeirizetai to sygkekrimeno room ta availiable dates

            out.println("Availiable dates UPDATED for room : " + room + " from : " + startdate + " to : " + enddate);
            inputFromClient=getInputFromClient();

    }
    void showReservations(){
        String inputFromClient;

        String yourRooms ="";
        for(int i=0;i<roomsOwned.size();i++){
            yourRooms += "||" + roomsOwned.get(i).get("roomName") + " || ";
        }

        out.println("your rooms are  : " + yourRooms);
        inputFromClient=getInputFromClient();

        String bookingsForRooms="";
        for(String room : this.BookedDates.keySet()){
            String startDate;
            String endDate;

           startDate = this.BookedDates.get(room).startDate.toString();
           endDate = this.BookedDates.get(room).endDate.toString();

           bookingsForRooms += " || room : " + room + " is booked from : " + startDate + " to : " + endDate + " || ";
        }
        out.println(" Showing Reservations : " + bookingsForRooms);
        inputFromClient=getInputFromClient();
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

    private void saveToWorker(int nodeID, JSONObject j) throws IOException {
        Master.workersOutputs.get(nodeID).println("Adding room to you... [ADD ROOM]"); //sending request
        Master.workersOutputs.get(nodeID).println(j.toString()); //stelnei json room
    }
    private void updateWorkerWithDates(int nodeID,String room,String startdate,String enddate) throws IOException {
        Master.workersOutputs.get(nodeID).println("Updating Dates...[DATES]");

        Master.workersOutputs.get(nodeID).println(room);  //stelnei room
        Master.workersOutputs.get(nodeID).println(startdate);  //stelnei start kai end Dates
        Master.workersOutputs.get(nodeID).println(enddate);
    }
   public static int hash(String roomName){
            int hashedName = Math.abs(roomName.hashCode() % Master.numOfWorkers);       //Workers arraylist size = numOfNodes, result = 0-numOfNodes
            return hashedName;  //kati <= numOfNodes
    }
}
