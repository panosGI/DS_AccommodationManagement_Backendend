
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class Worker extends Thread {

    public ArrayList<JSONObject> roomsSaved = new ArrayList<org.json.simple.JSONObject>();         //dwmatia tou worker
    HashMap<String, PairDate> availiableDates = new HashMap<String,PairDate>();  //dates availiable gia kathe dwmatio tou sugkekrimenou worker

    private BufferedReader inputFromMaster;  //gets input
    private PrintWriter outToMaster;    //sends output

    private BufferedReader inputFromReducer;  //gets input from reducer
    private PrintWriter outToReducer;    //sends output to reducer

    public Worker() throws IOException {

    }

    public static void main(String[]args){

       try {

               System.out.println("Connecting to reducer...");
               Socket reducerSocket = new Socket("localhost",3333);//connection with Reducer

               Worker worker = new Worker();

               worker.inputFromReducer=new BufferedReader(new InputStreamReader(reducerSocket.getInputStream()));  //input poy pairnei plhroforia apo to socket
               worker.outToReducer= new PrintWriter(reducerSocket.getOutputStream(),true); //writer using output stream

               System.out.println(worker.inputFromReducer.readLine()); //msg apo reducer server

               //--------------------------------------------------------------------------------------------------------
               ServerSocket serverSocket = new ServerSocket(5555); // pass port number, init Server socket
               System.out.println("Worker started. Waiting for Master...");

               Socket master = serverSocket.accept();
               System.out.println("Master connected!");

              worker.inputFromMaster = new BufferedReader(new InputStreamReader(master.getInputStream()));  //init to input me to socket pou milaei
              worker.outToMaster = new PrintWriter(master.getOutputStream(),true); //init to output me to socket pou milaei


               worker.outToMaster.println("Connection with worker established");  //stelnei se masterclient

               worker.work();
               //-------------------------------------------------------------------------------------------------------

              //--------------------------------------------------------------------------------------------------------


       }catch (Exception e){
           e.printStackTrace();
       }
    }

    private void work() {
        try {
            while (true){
                System.out.println("Waiting for request...");

                String  masterMsg = this.inputFromMaster.readLine(); //waiting for masters message...
                System.out.println("Master says : " + masterMsg );

                if (masterMsg.contains("ADD ROOM")){
                    System.out.println("Preparing to add room...");

                    String  jsonROOM  = this.inputFromMaster.readLine(); //waiting for string by master to save room
                    String jsonString = jsonROOM;

                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonString); //creating jsonobject

                    roomsSaved.add(jsonObject); //adding room

                    System.out.println(jsonObject);
                    System.out.println("Saved room!");
                }
                if(masterMsg.contains("FILTER")){
                    System.out.println("Preparing to get filters...");
                    //SAN NO FILTER...
                    String area =""; //this.inputMaster.readLine();
                    String startDate =""; //this.inputMaster.readLine();
                    String endDate = "";//this.inputMaster.readLine();
                    int noOfPersons = 0;//this.inputMaster.read();
                    int price = 0;//this.inputMaster.read();
                    int stars =0; //this.inputMaster.read();

                    String filteredRooms = doFiltering(area,startDate,endDate,noOfPersons,price,stars); //Ta dwmatia tou worker pou exipiretoun ta filtra
                    //morfh : ||RoomName||RoomName2||RoomName3||

                    outToReducer.println(filteredRooms);  //stelnei ta filtra tou ston reducer
                    System.out.println("I sent my matching results to Reducer: " + filteredRooms);
                }
                if(masterMsg.contains("DATES")){
                    System.out.println("Preparing to update availiable dates...");

                    String room = this.inputFromMaster.readLine();    //gets room and dates
                    String startdate = this.inputFromMaster.readLine();
                    String enddate = this.inputFromMaster.readLine();

                    DateFormat dateFormat = new SimpleDateFormat("DD/MM/YYYY");
                    Date startDate = dateFormat.parse(startdate);
                    Date endDate = dateFormat.parse(enddate);

                    PairDate avDates = new PairDate(startDate,endDate); //ta avaliable dates pou mas edwse gia to sugkekrimeno room o sugkekrimenos manager
                    availiableDates.put(room,avDates);         //add sthn lista me ta avDates gia kathe dwmatio tou sugkekrimenou manager

                    System.out.println("Availiable dates updated for room : " + room +" that i manage!");
                }
                if(masterMsg.contains("BOOK")){
                    System.out.println("Preparing to book dates...");

                    String room = this.inputFromMaster.readLine();    //gets room and dates
                    String startdate = this.inputFromMaster.readLine();
                    String enddate = this.inputFromMaster.readLine();

                    DateFormat dateFormat = new SimpleDateFormat("DD/MM/YYYY");
                    Date startDate = dateFormat.parse(startdate);
                    Date endDate = dateFormat.parse(enddate);


                    availiableDates.get(room).startDate=endDate; //availiable apo edw kai meta

                    System.out.println("booked dates for room : " + room + " that i manage! " + startdate +" "+ enddate);
                }
            }
        } catch (IOException | ParseException | java.text.ParseException e) {
            e.printStackTrace();
        }
    }

//...

    @Override
    public void run(){

    }
    public  String  doFiltering( String area,String startDate,String endDate,int noOfPersons,int price,int stars) throws java.text.ParseException {

        String filteredRooms ="||";

        for(int i=0; i<this.roomsSaved.size();i++) {
            //parse ta dates gia na ginei comparison
       //     DateFormat dateFormat = new SimpleDateFormat("DD/MM/YYYY");
         //   Date avSDate = dateFormat.parse((String) this.roomsSaved.get(i).get("startDate"));  //availiable dates gia ayto to dwmatio
         //   Date avEDate = dateFormat.parse((String) this.roomsSaved.get(i).get("endDate"));


            if (startDate != "" || endDate != "") {

                if ((this.roomsSaved.get(i).get("area").equals(area) || area == "") && ((int) this.roomsSaved.get(i).get("noOfPersons") >= noOfPersons || noOfPersons == 0) && ((int) this.roomsSaved.get(i).get("price") <= price || price == 0) && ((int) this.roomsSaved.get(i).get("stars") >= stars || stars == 0)) {
                    //if the room matches the filters (or the abscense of them)
                    filteredRooms += this.roomsSaved.get(i).get("roomName"); //KRATA TO
                }

            } else {
               // Date filterStartDate = dateFormat.parse(startDate);
               // Date filterEndDate = dateFormat.parse(endDate);

              //  if((this.roomsSaved.get(i).get("area").equals(area) || area=="") && ( filterStartDate.after(avSDate))&& ( filterEndDate.before(avEDate))
              //          && ((int)this.roomsSaved.get(i).get("noOfPersons")>=noOfPersons || noOfPersons==0) && ((int)this.roomsSaved.get(i).get("price")<=price || price==0) && ((int)this.roomsSaved.get(i).get("stars")>=stars || stars==0) ){
                    //if the room matches the filters (or the abscense of them)
               //     filteredRooms += this.roomsSaved.get(i).get("roomName"); //KRATA TO
            }
            filteredRooms += this.roomsSaved.get(i).get("roomName") + "||";
        }
        return filteredRooms;
    }
}

