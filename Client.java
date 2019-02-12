
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame{
    
    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message = "";
    private String serverIP;
    private Socket connection;
    
    private String username;
    private boolean serverOnline = false;
    private boolean loggedIn = false;
    
    public Client(String host, String username) {
	
	super("CLIENT: " + username);

	serverIP = host;
	this.username = username.toUpperCase();
	userText = new JPasswordField();
	userText.setEditable(false);
	userText.addActionListener(
	    new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		    if (!e.getActionCommand().equals("")) {
			sendMessage(e.getActionCommand());
			userText.setText("");
		    }
		}
	    }
	);
	add(userText, BorderLayout.SOUTH);
	
	chatWindow = new JTextArea();
	chatWindow.setEditable(false);
	chatWindow.setFont(new Font("Courier New", Font.PLAIN, 12));
	chatWindow.setLineWrap(true);      // to wrap text
	chatWindow.setWrapStyleWord(true); // to wrap at whitespace boundaries
	add(new JScrollPane(chatWindow), BorderLayout.CENTER);
	
	setSize(400,300);
	setResizable(false);
	setVisible(true);
	

    }
    
    //connect to server
    public void run() {
	displayMessage("Attempting connection...");
	while (true) {
	    try {
		if (canConnectToServer()) {
		    setupIO();
		    chatLoop();
		}
	    }
	    catch(IOException e) {
		displayMessage("SERVER is offline.");
	    }
	    finally{
		if (connection != null) {
		    closeConnection();
		}
	    }
	}
    }
    	
    private boolean canConnectToServer() throws IOException {
	connection = null;
	try {
	    
	    connection = new Socket(InetAddress.getByName(serverIP), 5903);
	    displayMessage("Now connected to " + connection.getInetAddress().getHostName());
	    System.out.println("TRUE");
	    return true;
	} catch (IOException e) {
	    return false;
	}
    }
    
    private void setupIO() throws IOException, SocketException {
	output = new ObjectOutputStream(connection.getOutputStream());
	output.flush();
	input = new ObjectInputStream(connection.getInputStream());
	displayMessage("I/O streams are ready.");
	
	// Let SERVER know what your username is!
	output.writeObject(username);
	output.flush();
	
	displayMessage("You are now connected to SERVER!");	
    }
    
    // @TODO
    private void login() {
	displayMessage("Enter username: ");
    }    
    
    private void chatLoop() throws IOException {
	canSendMessages(true);
	do{
	    try {
		message = (String) input.readObject();
		displayMessage(message);
	    }catch(ClassNotFoundException e) {
		displayMessage("Unknown object passed to input stream!");
	    }
	    
	}while(!message.equals(username + ": END CHAT"));
    }
    
    private void closeConnection() {
	canSendMessages(false);
	try {
	    output.close();
	    input.close();
	    connection.close();
	    displayMessage("Connection successfuly closed.");
	    displayMessage("\nAttempting connection...");
	}
	catch(IOException e){
	    displayMessage("Cannot close connection; connection does not exist!");
	}
	catch(NullPointerException e) {
	    displayMessage("I/O streams have not been setup!");
	}
    }
    
    private void sendMessage(String message) {
	try{
	    output.writeObject(username + ": " + message);
	    output.flush();
	    displayMessage(username + ": " + message);
	}catch(IOException e) {
	    chatWindow.append("Message send failed!");
	}
    }
    
    private void displayMessage(final String message) {
	SwingUtilities.invokeLater(
	    new Runnable() {
		public void run() {
		    chatWindow.append(message + "\n");
		}
	    }
	);
    }
    
    private void canSendMessages(final boolean b) {
	SwingUtilities.invokeLater(
	    new Runnable() {
		public void run() {
		    userText.setEditable(b);
		}
	    }
	);
    }
}
