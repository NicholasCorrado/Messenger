import javax.swing.JFrame;

public class ClientDriver {
    
    public static void main(String[] args) {
	Client c = new Client("127.0.0.1", "NICHOLAS"); //local host--my computer
	c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	c.run();
    }
}
