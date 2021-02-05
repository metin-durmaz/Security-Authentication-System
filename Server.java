import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Server class
 **/
public class Server {

    private static ServerSocket serverSocket;
    private static PrintWriter currentLogFile;

    public Server(int PORT, String serverName, PrintWriter logFile) throws Exception {
        serverSocket = new ServerSocket(PORT);      // Server type created
        currentLogFile = logFile;                   // Server logFile created
        while (true)
            serverRunner(serverName);
    }

    /**
     * Allows the server to accept new clients.
     * It receives the encrypted text of the client in step 3.
     * By decoding the encrypted text, it accesses the nonceValue numbers within the text.
     * It checks the nonceValue1 by the number nonceValue1 in itself.
     * If the numbers are equal, it confirms the authentication.
     *
     * @param serverName --> Type of server running
     **/
    private void serverRunner(String serverName) throws Exception {

        Socket client = serverSocket.accept();
        BufferedReader serverInput = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter serverOutput = new PrintWriter(client.getOutputStream(), true);

        String[] clientMessage = serverInput.readLine().split(",");
        String[] ticket = CryptFunctions.decryptRSA(clientMessage[1], CryptFunctions.getPrivateKeyFromTXT(serverName + "PriKey")).split(",");
        int nonceValue1 = Integer.parseInt(CryptFunctions.decryptAES(clientMessage[2], ticket[3]));
        int nonceValue2 = new Random().nextInt(9999);

        currentLogFile.println(CryptFunctions.timeStampGetter() + " Alice->" + serverName + " : \"Alice\", " + clientMessage[1] + ", " + clientMessage[2]);
        currentLogFile.println(CryptFunctions.timeStampGetter() + " \"Ticket Decrypted\" : \"Alice\", " + ticket[1] + ", " + ticket[2] + ", " + ticket[3]);
        currentLogFile.println(CryptFunctions.timeStampGetter() + " \"Message Decrypted\" : N1=" + nonceValue1);

        String encryptedServerMessage = CryptFunctions.encryptAES((nonceValue1 + 1) + "," + nonceValue2, ticket[3]);
        serverOutput.println(encryptedServerMessage);

        currentLogFile.println(CryptFunctions.timeStampGetter() + " " + serverName + "->Alice : " + (nonceValue1 + 1) + ", " + nonceValue2);
        currentLogFile.println(CryptFunctions.timeStampGetter() + " " + serverName + "->Alice : " + encryptedServerMessage);

        String encryptedClientMessage = serverInput.readLine();
        currentLogFile.println(CryptFunctions.timeStampGetter() + " Alice->" + serverName + ": " + encryptedClientMessage);
        if (Integer.parseInt(CryptFunctions.decryptAES(encryptedClientMessage, ticket[3])) == nonceValue2 + 1) {
            currentLogFile.println(CryptFunctions.timeStampGetter() + " \"Message Decrypted\" : " + (nonceValue2 + 1));
            currentLogFile.println(CryptFunctions.timeStampGetter() + " " + serverName + "->Alice : \"Authentication is completed!\"");
        }
        currentLogFile.close();
        client.close();
    }

}