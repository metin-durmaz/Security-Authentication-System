import java.io.*;

/**
 * DatabaseServer Class
 **/
public class DatabaseServer extends Server {

    private static final int PORT = 3003;
    private static final String ID = "Database";

    public DatabaseServer(PrintWriter logFile) throws Exception {
        super(PORT, ID, logFile);
    }

    /**
     * DatabaseServer Main
     **/
    public static void main(String[] args) throws Exception {
        PrintWriter logFile = new PrintWriter(new FileWriter("Database_Log.txt", true));
        new DatabaseServer(logFile);
    }

}