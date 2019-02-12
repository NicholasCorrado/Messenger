import javax.swing.JFrame;

public class ServerDriver {

    public static void main(String[] args) {
	Server s = new Server(5903);
	s.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	s.run();
	
    }
    
}
