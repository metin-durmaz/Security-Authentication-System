import java.io.*;

/**
 * MailServer Class
 **/
public class MailServer extends Server {

    private static final int PORT = 3001;
    private static final String ID = "Mail";

    public MailServer(PrintWriter logFile) throws Exception {
        super(PORT, ID, logFile);
    }

    /**
     * MailServer Main
     **/
    public static void main(String[] args) throws Exception {
        PrintWriter logFile = new PrintWriter(new FileWriter("Mail_Log.txt", true));
        new MailServer(logFile);
    }

}