import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.UUID;

/**
 * This class is used to implement the logic behind each task requested. 
 * It has methods to serve each client's request.
 */
public class ConnectionHandler implements Runnable{
    Socket clientSock = null;
    UserDB db = null;
    WordExtractor extr = null;
    InputStream in = null;
    OutputStream out = null;
    BufferedReader reader = null;
    BufferedWriter writer = null;

    ConnectionHandler(Socket socket, UserDB database, WordExtractor extractor){
        clientSock = socket;
        db = database;
        extr = extractor;

        try {
            in = clientSock.getInputStream();
            out = clientSock.getOutputStream();
            reader = new BufferedReader(new InputStreamReader(in));
            writer = new BufferedWriter(new OutputStreamWriter(out));
        }
        catch (IOException e){
            try {
                    socket.close();
            }
            catch (IOException i){
                i.printStackTrace();
            };
        }

    }

    private String readMsg(Socket sock){
        // gets input stream from the socket passed as argument
        InputStream dataIn = null;
        try {
            dataIn = sock.getInputStream();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // creates a new InputStreamReader and decorates it with a BufferedReader
        final InputStreamReader isr = new InputStreamReader(dataIn);
        final BufferedReader in = new BufferedReader(isr);

        String response = null;
        try {
            // blocks waiting from an answer from the server
            response = in.readLine();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private void writeMsg(Socket sock, String message){
        // gets output stream from the socket passed as argument
        OutputStream dataOut = null;
        try {
            dataOut = sock.getOutputStream();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // creates a new OutputStreamWriter and decorates it with a BufferedWriter
        final OutputStreamWriter osw = new OutputStreamWriter(dataOut);
        final BufferedWriter writerOut = new BufferedWriter(osw);

        try {
            // sends the request on the socket
            writerOut.write(message);
            writerOut.newLine();
            writerOut.flush();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        
    }

    private void parseInput(final String input) throws IOException, RemoteException, InterruptedException {
        final String[] params = input.split(" ");
        switch (params[0]) {
        case "login":
            this.login(params[1], params[2]);
            break;
        case "logout":
            this.logout(params[1], params[2]);
            break;
        case "add_friend":
            //this.add_friend(params[1]);
            break;
        case "friend_list":
            //this.friend_list();
            break;
        case "score":
            //this.score();
            break;
        case "scoreboard":
            //this.scoreboard();
            break;
        case "match":
            //this.match(params[1]);
            break;
        case "show_matches":
            //this.showMatches();
            break;
        case "accept_match":
            //this.acceptMatch(params[1]);
            break;
        case "quit":
            System.exit(0);
        default:
            System.out.println("Client sent an unrecognized message");
            break;
        }
    }

    private void login(String nick, String password){
        User user = db.getUser(nick);
        if (user.equals(null)){
            this.writeMsg(this.clientSock, "ERROR! You have to register first!");
            return;
        }
        else if (user.getHash() != password.hashCode()){
            this.writeMsg(this.clientSock, "ERROR! You entered a wrong password!");
            return;
        }
        else {
            String uniqueID = UUID.randomUUID().toString();
            synchronized(user){
                user.setId(uniqueID);
                user.setLogged(true);
                user.setUDP(1111);
            }
            this.writeMsg(this.clientSock, "Successfully logged in. Session ID:" + uniqueID);
        }

    }

    private void logout(String nick, String sessionID){
        User user = db.getUser(nick);
        if (!user.getId().equals(sessionID)){
            writeMsg(this.clientSock, "You're using an invalid sessionID, please stop doing nasty things!");
        }
        else {
            user.setLogged(false);
            user.setId(null);
            writeMsg(this.clientSock, "Successfully logged out");
        }
    }

    public void run(){
        try {
            parseInput(this.readMsg(this.clientSock));
            clientSock.close();}
        catch (Exception e){

        }
    }
}