import java.io.*;

/**
 * WebServer Class
 **/
public class WebServer extends Server {

    private static final int PORT = 3002;
    private static final String ID = "Web";

    public WebServer(PrintWriter logFile) throws Exception {
        super(PORT, ID, logFile);
    }

    /**
     * WebServer Main
     **/
    public static void main(String[] args) throws Exception {
        PrintWriter logFile = new PrintWriter(new FileWriter("Web_Log.txt", true));
        new WebServer(logFile);
    }

}