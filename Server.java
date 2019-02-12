
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Owner
 */
public class Server extends JFrame{
    
    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocket server;
    private Socket connection; // socket = connection between computers 
    
    private int port;
    private String username;
    
    public Server(int port) {
	
	super("SERVER");
	
	this.port = port;
	userText = new JTextField();
	userText.setEditable(false);
	userText.addActionListener(
		new ActionListener() {
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
	chatWindow.setFont(new Font("Courier New", Font.PLAIN, 12));
	chatWindow.setEditable(false);
	chatWindow.setLineWrap(true);      // to wrap text
	chatWindow.setWrapStyleWord(true); // to wrap at whitespace boundaries
	add(new JScrollPane(chatWindow));
	
	setSize(400,300);
	setResizable(false);
	setVisible(true);
    }
    
    
    public void run() {
	try {
	    // where is our boat going to dock? which port?
	    // how many people are allowed to wait at the port (queue length, backlog)?
	    server = new ServerSocket(port, 100);
	    while (true) {
		try {
		   waitForConnection();
		   setupIO();
		   chatLoop();
		}
		catch(EOFException e) { // end of stream connection.
		   displayMessage("SERVER ended the connection."); 
		}
		finally {
		    closeConnection();
		}
	    }
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    
    // wait for connection, then display connection information.
    private void waitForConnection() {
	displayMessage("Waiting for someone to connect...");
	try {
	    // blocks a connection until it is made
	    connection = server.accept(); // is someone connected? how bout now? now? now? ...
	} catch (IOException e) {
	    e.printStackTrace();
	}
	displayMessage("Now connected to " + connection.getInetAddress().getHostName());
    }
    
    private void setupIO() throws IOException {
	output = new ObjectOutputStream(connection.getOutputStream());
	output.flush(); // why?
	
	input = new ObjectInputStream(connection.getInputStream());
	displayMessage("I/O streams are ready.");
    }
    
    
    private void chatLoop() throws IOException {
	
	try { // retrieve client username
	    username = (String) input.readObject();
	} catch (ClassNotFoundException ex) {
	    System.out.println("Could not retrieve client username!");
	}
	String message = "You are now connected to " + username + "!";
	displayMessage(message);
	canSendMessages(true);
	
	//loadMessages(username + "_messages.txt");
	
	do {
	    try {
		message = (String) input.readObject(); //reading in objects from the input stream
		displayMessage(message);
	    }
	    // if the object the other guy sent is not a string
	    catch (ClassNotFoundException e) {
		displayMessage(username + " sent an unknown object!");
	    }
	    catch (SocketException e) {
		displayMessage(username + " has disconnected.");
		break;
	    }
	} while(!message.equals(username + " has disconnected."));
    }
    
    private void closeConnection() {
	canSendMessages(false);
	try {
	    output.close();
	    input.close();
	    connection.close(); //closing the connection between the computers
	    //saveToFile(username + "_messages.txt");
	}
	catch(IOException e) {
	    e.printStackTrace();
	}
    }
	
    // only updating one part of the gui
    private void canSendMessages(final boolean b) {
	SwingUtilities.invokeLater(
	    new Runnable() {
		public void run() {
		    userText.setEditable(b);
		}
	    }
	);
    }
    
    private void sendMessage(String message) {
	try {
	    output.writeObject("SERVER: " + message);
	    output.flush();
	    displayMessage("SERVER: " + message);
	}
	catch(IOException e) {
	    chatWindow.append("ERROR: Messeage failed to send");
	}
    }
    
    // updates chat window
    // updating only the chat window part of the gui
    private void displayMessage(final String message) {
	
	// Swing data structures are not thread-safe. This static method allows
	// this append to be executed on the AWT event dispatch thread 
	SwingUtilities.invokeLater(
	    new Runnable() {
		public void run() {
		    chatWindow.append(message + "\n");
		}
	    }
	);
    }
    
    public void saveToFile(String filename) {

	String tmpFilename = "tmp_" + filename;
	BufferedReader readFile = null;
	BufferedWriter writeFile = null;
	
	try {
	    //readFile = new BufferedReader(new FileReader(filename));
	    writeFile = new BufferedWriter(new FileWriter("tmp_" + filename)); // a if the file already exists, it will be overwritten! Maybe try to handle this in the future?
	    writeFile.write(chatWindow.getText());
	    
	} 
	catch (FileNotFoundException ex) {
	    System.out.println("File to read not found.");
	} 
	catch (IOException ex) {
	    System.out.println("File to write cannot be created. Some possible causes:");
	    System.out.println("\t" + tmpFilename + " is already opened");
	    System.out.println("\t" + tmpFilename + " exists as a directory");
	}
	finally {
	    try{
		if (readFile != null) readFile.close();
		if (writeFile != null) writeFile.close();
	    }
	    catch (IOException ex) {}
	}
	
	File oldFile = new File(filename);
	oldFile.delete(); // if the file doesn't exist, that's okay; nothing happens.
	File newFile = new File(tmpFilename);
	newFile.renameTo(oldFile);
  }
  
    private void loadMessages(String filename) {

	BufferedReader readFile = null;
	
	try {
	    readFile = new BufferedReader(new FileReader(filename));
	    
	    String line;
	    while ((line = readFile.readLine()) != null) {
		loadMessage(line);
	    }
	} 
	catch (FileNotFoundException ex) {
	    System.out.println("Input data file not found. A blank list has been loaded.");
	} 
	catch (IOException ex) {
	    System.out.println("I don't know what exception this is 1.");
	}
	finally {
	    try {
		if (readFile != null) readFile.close();
	    }
	    catch (IOException ex) {
		System.out.println("I don't know what exception this is 2.");
	    }
	}
    }
    
    
    private void loadMessage(String message) {
	try {
	    output.writeObject(message);
	    output.flush();
	    displayMessage(message);
	}
	catch(IOException e) {
	    chatWindow.append("ERROR: Message load failed.");
	}
    }
}
