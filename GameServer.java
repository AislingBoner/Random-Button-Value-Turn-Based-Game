import java.io.*;
import java.net.*;

public class GameServer {

    private ServerSocket ss;
    private int numPlayers;
    private ServerSideConnection player1;
    private ServerSideConnection player2;
    private int turnsMade; //Tracks made
    private int maxTurns;
    private int[] values;//Points for each button
    private int player1ButtonNum;//Stores Button number player clicks on
    private int player2ButtonNum;



    public GameServer(){
        System.out.println("----Game Server-----");
        numPlayers = 0;
        turnsMade = 0;
        maxTurns = 4;
        values = new int[4];//array set to fourbuttons

        for(int i=0; i < values.length; i++){//Generate random number for the buttons
            values[i] = (int) Math.ceil(Math.random() * 100); //Gives a number anything up to 100 & Round Up
            System.out.println("Value #" + (i + 1)  + " is " + values[i]);
        }//Loop happens 4 times

        try{
            ss = new ServerSocket(51734); //Must match Client
        }catch (IOException ex){
            System.out.println("IO Exception from Game Server");
            
        }
    }

    public void acceptConnections(){
        try{
            System.out.println("Waiting for connections....");
            while(numPlayers<2){
                Socket s = ss.accept();
                numPlayers++;
                System.out.println("Player # " + numPlayers + " has connected.");
                ServerSideConnection ssc = new ServerSideConnection(s, numPlayers);

                if(numPlayers == 1){
                    player1 = ssc;
                }else{
                    player2 = ssc;
                }

                Thread t = new Thread(ssc);
                t.start();

            }
            System.out.println("We now have two players. No longer accepting connections.");
        }catch (IOException ex){
            System.out.println("IO Exception from acceptConnections()");

        }
    }
    
    // Create Runnable Object for each player. 1 Thread for each player
    private class ServerSideConnection implements Runnable{
        private Socket socket;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;
        private int playerID;

        //Constructor
        public ServerSideConnection(Socket s, int id){
            socket = s;
            playerID = id;
            try{
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());
            }catch(IOException ex){
                System.out.println("IO Exception from run() ServerSideConnection");
            }
        }

        

        public void run(){
            //1st thing I'd like the server to do.

            try{
                dataOut.writeInt(playerID);
                dataOut.writeInt(maxTurns);

                //Sdend out the values for buttons
                dataOut.writeInt(values[0]);
                dataOut.writeInt(values[1]);
                dataOut.writeInt(values[2]);
                dataOut.writeInt(values[3]);
                dataOut.flush();

                while(true){
                    //Do this multiple times - 4 turns

                    if(playerID == 1){
                        player1ButtonNum = dataIn.readInt();
                        System.out.println("Player 1 clicked button #" + player1ButtonNum);
                        player2.sendButtonNum(player1ButtonNum);//sends number selected to opposite player
                    }else{
                        player2ButtonNum = dataIn.readInt();
                        System.out.println("Player 2 clicked button #" + player2ButtonNum);
                        player1.sendButtonNum(player2ButtonNum);//Sends number to opposite player

                    }
                    turnsMade++;
                    if(turnsMade == maxTurns){
                        System.out.println("Max turns has been reached.");
                        break;//Run() method & thread terminates
                    }
                }

                    player1.closeConnection();
                    player2.closeConnection();
                    //End Game

                
            }catch(IOException ex){
                System.out.println("IO Exception from run() in ServerSideConnection");

            }


        }

        public void sendButtonNum(int n){ 
            try{

                dataOut.writeInt(n);
                dataOut.flush();
            }catch(IOException ex){
                System.out.println("IO Exception from sendButtonNum() Server Side Connection ");
            }


        }

        public void closeConnection(){
            try{
                socket.close();
                System.out.println("Server Side Connection Closed! ");
                
            }catch(IOException ex){ 
                System.out.println("IO Exception on closeConnection() server side connection.");
            

            }
        }


}

    public static void main(String[] args){
        GameServer gs = new GameServer();
        gs.acceptConnections();
    }
}