import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * Client Class
 **/
public class Client {

    private static PrintWriter logFile;
    private static Scanner input;
    private static String ticket;
    private static String sessionKey;
    private static int serverPort;
    private static String serverName;

    /**
     * Client Main
     **/
    public static void main(String[] args) throws Exception {

        while (true) {
            clientSide();                       // Alice will always want to connect with any server
            authentication();
        }
    }

    /**
     * Requests a password for the connection session.
     * It asks for the name of the server it wants to connect to.
     * It encrypts the text with the KDC's public key and sends it to the KDC.
     * Meanwhile, the KDC sends a message back to the client.
     * If there is information that the password does not match in the message content, it repeats these processes.
     * If the passwords match, it decrypts the first part of the message with its own private key and keeps the second part as ticket information.
     **/
    private static void clientSide() throws Exception {

        logFile = new PrintWriter(new FileWriter("Alice_Log.txt", true));

        Socket clientSocket = new Socket("localhost", 3000);
        BufferedReader clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);

        String inputMessage, encryptedClientMessage;
        inputMessage = "Alice," + passwordGetter() + "," + serverNameGetter() + "," + CryptFunctions.timeStampGetter();
        encryptedClientMessage = "Alice," + (CryptFunctions.encryptRSA(inputMessage, CryptFunctions.getPublicKey("KDCCert")));
        clientOutput.println(encryptedClientMessage);
        String[] parts = inputMessage.split(",");
        logFile.println(CryptFunctions.timeStampGetter() + " Alice->KDC : \"Alice\", " + "[" + parts[1] + "], " + serverName + ", " + parts[3]);
        logFile.println(CryptFunctions.timeStampGetter() + " Alice->KDC : \"Alice\", " + encryptedClientMessage.substring(6));

        while (true) {
            String encryptedServerMessage = clientInput.readLine();
            if (encryptedServerMessage.equals("FALSE")) {
                logFile.println(CryptFunctions.timeStampGetter() + " KDC->Alice : \"Password Denied\"");
                inputMessage = "Alice," + passwordGetter() + "," + serverNameGetter() + "," + CryptFunctions.timeStampGetter();
                encryptedClientMessage = "Alice," + (CryptFunctions.encryptRSA(inputMessage, CryptFunctions.getPublicKey("KDCCert")));
                clientOutput.println(encryptedClientMessage);
                parts = inputMessage.split(",");
                logFile.println(CryptFunctions.timeStampGetter() + " Alice->KDC : \"Alice\", " + "[" + parts[1] + "], " + serverName + ", " + parts[3]);
                logFile.println(CryptFunctions.timeStampGetter() + " Alice->KDC : \"Alice\", " + encryptedClientMessage);
            } else {
                logFile.println(CryptFunctions.timeStampGetter() + " KDC->Alice : \"Password Verified\"");
                logFile.println(CryptFunctions.timeStampGetter() + " KDC->Alice : " + encryptedServerMessage + "," + ticket);
                String[] keyTicket = encryptedServerMessage.split(", ");
                String[] words = CryptFunctions.decryptRSA(keyTicket[0], CryptFunctions.getPrivateKeyFromTXT("ClientPriKey")).split(",");
                sessionKey = words[0];
                ticket = keyTicket[1];
                logFile.println(CryptFunctions.timeStampGetter() + " Message Decrypted : " + sessionKey + ", " + words[1] + ", " + words[2]);
                break;
            }
        }
        clientSocket.close();

    }

    /**
     * It was written to get a password from the user.
     *
     * @return --> Entered password
     **/
    private static String passwordGetter() {
        System.out.print("Alice password: ");
        input = new Scanner(System.in);
        return input.nextLine();
    }

    /**
     * It was written to get the name of the server that the user wants to connect to.
     *
     * @return --> Entered server name
     **/
    private static String serverNameGetter() {
        input = new Scanner(System.in);
        do {
            System.out.print("Server name: ");
            serverName = input.nextLine();
        } while (!serverName.equals("Mail") && !serverName.equals("Web") && !serverName.equals("Database"));

        serverPortSetter();
        return serverName;
    }

    /**
     * Written to assign the port number belongs to the server name entered by the user.
     **/
    private static void serverPortSetter() {
        switch (serverName) {
            case "Mail":
                serverPort = 3001;
                break;
            case "Web":
                serverPort = 3002;
                break;
            case "Database":
                serverPort = 3003;
                break;
        }
    }

    /**
     * First, it produces the nonceValue1.
     * It encrypts the nonceValue1 with the session key in hand.
     * Combines the resulting text with ticket information and sends it to the server it is connected to.
     * Decrypts the encrypted message from the server with the session key.
     * Obtains the nonceValue2 from the second part of the message.
     * To ensure authentication, it encrypts the nonceValue2 it obtains with the session key and sends it to the server it is connected to.
     **/
    public static void authentication() throws Exception {

        Socket socket = new Socket("localhost", serverPort);
        BufferedReader clientInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter clientOutput = new PrintWriter(socket.getOutputStream(), true);

        int nonceValue = new Random().nextInt(9999);
        String encryptedNonceValue1 = CryptFunctions.encryptAES(String.valueOf(nonceValue), sessionKey);
        clientOutput.println("Alice," + ticket + "," + encryptedNonceValue1);
        logFile.println(CryptFunctions.timeStampGetter() + " Alice->" + serverName + " : \"Alice\", " + nonceValue);
        logFile.println(CryptFunctions.timeStampGetter() + " Alice->" + serverName + " : \"Alice\", " + ticket + ", " + encryptedNonceValue1);

        String encryptedServerMessage = clientInput.readLine();
        logFile.println(CryptFunctions.timeStampGetter() + " " + serverName + "->Alice : " + encryptedServerMessage);
        String[] serverMessageParts = CryptFunctions.decryptAES(encryptedServerMessage, sessionKey).split(",");

        if (Integer.parseInt(serverMessageParts[0]) == nonceValue + 1) {
            String nonceValue2 = String.valueOf(Integer.parseInt(serverMessageParts[1]) + 1);
            logFile.println(CryptFunctions.timeStampGetter() + " Message Decrypted : N1 is OK, N2=" + (Integer.parseInt(nonceValue2) - 1));
            String encryptedNonceValue2 = CryptFunctions.encryptAES(nonceValue2, sessionKey);

            clientOutput.println(encryptedNonceValue2);
            logFile.println(CryptFunctions.timeStampGetter() + " Alice->" + serverName + " : " + nonceValue2);
            logFile.println(CryptFunctions.timeStampGetter() + " Alice->" + serverName + " : " + encryptedNonceValue2);
            logFile.println(CryptFunctions.timeStampGetter() + " " + serverName + "->Alice : \"Authentication is completed!\"");

        }
        socket.close();
        logFile.close();
    }

}