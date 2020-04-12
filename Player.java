import javax.swing.*;
import java.awt.*;
import java.net.Socket;
import java.io.*;
import java.net.*;
import java.awt.event.*;
import java.util.*;



public class Player extends JFrame {

    private int width;
    private int height;
    private Container contentPane;
    private JTextArea message;
    private JButton b1;
    private JButton b2;
    private JButton b3;
    private JButton b4;
    private int playerID;
    private int otherPlayer;
    private int[] values;
    private int maxTurns;
    private int turnsMade;
    private int myPoints;
    private int opponentPoints;
    private boolean buttonEnabled;
    private String playerName;
    private Scanner s;
    

    private ClientSideConnection csc;
    
    
    public Player (int w, int h){
        width = w;
        height = h;
        contentPane = this.getContentPane();
        message = new JTextArea();
        b1 = new JButton("1");
        b2 = new JButton("2");
        b3 = new JButton("3");
        b4 = new JButton("4");
        values = new int[4];
        turnsMade = 0;
        myPoints = 0;
        opponentPoints = 0;
        playerName = " ";
        

    }

    public String getPlayerName() {
        return playerName;
    }
    public void setPlayerName(String playerName){
        this.playerName = playerName.toUpperCase();
    }

    public void setUpGUI(){
        this.setSize(width, height);
        this.setTitle("Player #" + playerID + ": " + playerName );
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        contentPane.setLayout(new GridLayout(1,5));
        contentPane.add(message);
        message.setText("Creating a simple turn-based game in Java");
        message.setWrapStyleWord(true);
        message.setLineWrap(true);
        message.setEditable(false);
        contentPane.add(b1);
        contentPane.add(b2);
        contentPane.add(b3);
        contentPane.add(b4);
        b1.setBackground(Color.RED);
        b2.setBackground(Color.YELLOW);
        b3.setBackground(Color.GREEN);
        b4.setBackground(Color.BLUE);

        if(playerID ==1){
            message.setText("You are player #1.\n\nYou go First!");
            otherPlayer = 2;
            buttonEnabled = true;
        }else{
            message.setText("You are player #2.\n\nWait your Turn.");
            otherPlayer = 1;
            buttonEnabled = false;
            
            Thread t = new Thread(new Runnable(){//create a new thread that calls the update turn method
            
                @Override
                public void run() {
                    updateTurn();//calls csc recievebuttonnum
                    
                }
            });
            t.start();
        }
        toggleButtons();

        this.setVisible(true);

    }
    //Instanciate the clientside connection
    public void connectToServer(){
        csc = new ClientSideConnection();
        


    }

    public void setUpButtons(){
        ActionListener al = new ActionListener(){ //Anonymous class - need to end this class with ;
            
            public void actionPerformed(ActionEvent ae){
                JButton b = (JButton) ae.getSource();//Get the button that was clicked
                int bNum = Integer.parseInt(b.getText());

                message.setText("You Clicked Button #" + bNum + ".\nNow wait for player #" + otherPlayer);
                turnsMade ++; //Every time you click on a button it qualifies as a turn
                System.out.println("Turns Made: " + turnsMade);
                buttonEnabled = false;
                toggleButtons(); //after your turn your buttons become disabled

                myPoints += values[bNum - 1];
                System.out.println("My points: " + myPoints);
                csc.sendButtonNum(bNum);

                if(playerID == 2 && turnsMade == maxTurns){
                    checkWinner();
                }else{
                    Thread t = new Thread(new Runnable(){//create a new thread that calls the update turn method
            
                        @Override
                        public void run() {
                            updateTurn();//calls csc recievebuttonnum
                            
                        }
                    });
                    t.start();

                }

                


            }
        };

        b1.addActionListener(al);
        b2.addActionListener(al);
        b3.addActionListener(al);
        b4.addActionListener(al);
    }

    public void toggleButtons(){ //call set enabled method for each button

        b1.setEnabled(buttonEnabled);
        b2.setEnabled(buttonEnabled);
        b3.setEnabled(buttonEnabled);
        b4.setEnabled(buttonEnabled);

    }

    /*

    Dont need this anymore as we have an updateTurn() method ---
    public void startRecievingButtonNums(){

        Thread t = new Thread(new Runnable(){ //In a thread - dont want network code to block gui code - would have to wait until this completes so there would be delay
        
            @Override
            public void run() { //prevents delay of gui updates
                while(true){
                    csc.recieveButtonNum();
                }
                
            }
        });
        t.start();

    }
    */

    public void updateTurn(){
        //Call when we are waiting for a turn - Waiting mode - once recieved other players button
        int n = csc.recieveButtonNum();
        message.setText(playerName + " clicked button #" + n + ", Its Your Turn.");
        opponentPoints += values[n-1];
        System.out.println(playerName + " has " + opponentPoints + " points.");
        
        if (playerID == 1 && turnsMade == maxTurns){
            checkWinner();
        }else{
            buttonEnabled = true; //As long as game is still running
        }
        toggleButtons();
        

    }

    private void checkWinner(){
        buttonEnabled = false;

        if(myPoints > opponentPoints){

            message.setText("You WON!!\n" + playerName +  ": " + myPoints + "\nOpponent: " + opponentPoints);

        }else if (myPoints < opponentPoints){
            message.setText("You LOST!!\n" + playerName + ": " + myPoints + "\nOpponent: " + opponentPoints);

        }else{
            message.setText("Its a TIE!!\n" + "You both Got: " + myPoints);

        }
        csc.closeConnection();

        
    }

    //Encapsulates Networking Instructions for the client - 
    //Client Connection Inner Class
    //Connects and communicates with the server
    private class ClientSideConnection{

        private Socket socket;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;
       

        
        //Constructor
        public ClientSideConnection(){
            System.out.println("-----Client-----");
            try{
                //Initiales the connection request to the server. Once connected we get input and output stream.
                socket = new Socket ("localhost", 51734);  //Using locally on the computer @ Potr 51734
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());
                playerID = dataIn.readInt();
                System.out.println("Connected to server as Player # " + playerID + ":" + playerName);
                maxTurns = dataIn.readInt()/2; //Max turns split by users
               
                //Allows player to recieve the generated values from the server.
                //Need to be generated by Server as 2 player need the same numbers 
                //Not just randomly generate them here as game would be pointless no comparator
                //Need to generate it from the server
                values[0] = dataIn.readInt();
                values[1] = dataIn.readInt();
                values[2] = dataIn.readInt();
                values[3] = dataIn.readInt();
                System.out.println("Max Turns: " + maxTurns);
                System.out.println("Value #1 is " + values[0]);
                System.out.println("Value #2 is " + values[1]);
                System.out.println("Value #3 is " + values[2]);
                System.out.println("Value #4 is " + values[3]);

            }catch (IOException ex){
                System.out.println("IO Exception from Client Side Connection Constructor ");
            }
            
            
        }

        public void sendButtonNum(int n){//Sends the button number
        
            try{
                dataOut.writeInt(n);
                dataOut.flush();
    
            }catch(IOException ex){
                System.out.println("IO Exception from sendButtonNum() Client Side Connection ");
    
            }

        }

        public int recieveButtonNum(){
            int n = -1;

            try{
                n = dataIn.readInt();
                System.out.println("Player #" + otherPlayer + " clicked button #" + n);
            }catch(IOException ex){
                System.out.println("IO Exception from recieveButtonNum() Client Side Connection ");
            }
            return n;
        }

        public void closeConnection(){
            
        }

}
 

   public static void main(String[] args) {
    Player p = new Player(600, 200);
    Scanner s = new Scanner(System.in);
    p.connectToServer(); //Am I player 1 or 2
    System.out.println("Please enter your name: ");
    p.setPlayerName(s.nextLine());
    p.setUpGUI();
    p.setUpButtons();

        
    }
}



    

    

    

