package server;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Server extends JFrame {

    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocket server;
    private Socket connection;
    private  int totalppl=0;

    //constructor
    public Server(){
        super("ChatServer");
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent event){
                        sendMessage(event.getActionCommand());
                        userText.setText("");
                    }
                }
        );
        add(userText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow));
        setSize(300, 150); //Sets the window size
        setVisible(true);
    }

    public void startRunning(){
        try{
            server = new ServerSocket(6969, 100);

            while(true){
                try{
                    waitForConnection();
                    setupStreams();
                    whileChatting();
                }catch(EOFException eofException){
                    showMessage("\n Server ended the connection! ");
                } finally{
                    closeConnection(); //Changed the name to something more appropriate
                }
            }
        } catch (IOException ioException){
            ioException.printStackTrace();
        }
    }
    //wait for connection, then display connection information
    private void waitForConnection() throws IOException{
        showMessage(" Waiting for someone to connect... \n");
        if(totalppl<=2) {
            connection = server.accept();totalppl++;
        }
        showMessage(" Now connected to " + connection.getInetAddress().getHostName());
    }

    //get stream to send and receive data
    private void setupStreams() throws IOException{
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();

        input = new ObjectInputStream(connection.getInputStream());

        showMessage("\n Streams are now setup \n");
    }

    //during the chat conversation
    private void whileChatting() throws IOException{
        String message = " You are now connected! ";
        sendMessage(message);
        ableToType(true);
        do{
            try{
                message = (String) input.readObject();
                showMessage("\n" + message);
            }catch(ClassNotFoundException classNotFoundException){
                showMessage("The user has sent an unknown object!");
            }
        }while(!message.equals("CLIENT - END"));
    }

    public void closeConnection(){
        showMessage("\n Closing Connections... \n");
        ableToType(false);
        try{
            output.close(); //Closes the output path to the client
            input.close(); //Closes the input path to the server, from the client.
            connection.close(); //Closes the connection between you can the
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
    }

    //Send a mesage to the client
    private void sendMessage(String message){
        try{
            output.writeObject("SERVER - " + message);
            output.flush();
            showMessage("\nSERVER -" + message);
        }catch(IOException ioException){
            chatWindow.append("\n ERROR: CANNOT SEND MESSAGE, PLEASE RETRY");
        }
    }

    //update chatWindow
    private void showMessage(final String text){
        SwingUtilities.invokeLater(
                new Runnable(){
                    public void run(){
                        chatWindow.append(text);
                    }
                }
        );
    }

    private void ableToType(final boolean tof){
        SwingUtilities.invokeLater(
                new Runnable(){
                    public void run(){
                        userText.setEditable(tof);
                    }
                }
        );
    }
}