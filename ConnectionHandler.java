import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

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
            this.login(params[1], params[2], params[3]);
            break;
        case "logout":
            this.logout(params[1], params[2]);
            break;
        case "friend":
            this.add_friend(params[1], params[2], params[3]);
            break;
        case "friend_list":
            this.friend_list(params[1], params[2]);
            break;
        case "score":
            this.score(params[1], params[2]);
            break;
        case "scoreboard":
            this.scoreboard(params[1], params[2]);
            break;
        case "match":
            this.match(params[1], params[2], params[3]);
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

    private void login(String nick, String password, String port){
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
                user.setUDP(Integer.parseInt(port));
            }
            this.writeMsg(this.clientSock, "Successfully logged in. Session ID:" + uniqueID);
            System.out.println(nick + " logged in.");
            System.out.println(nick + " is using UDP port: " + port);
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
            System.out.println(nick + " logged out.");
        }
    }

    private void add_friend(String nickname, String friendNick, String sessionID){
        User user = db.getUser(nickname);
        if (!user.isLogged())
            writeMsg(this.clientSock, "You're not logged in!");
        else if (nickname.equals(friendNick))
            writeMsg(this.clientSock, "You can't be friend with yourself!");
        else if (!user.getId().equals(sessionID)){
            writeMsg(this.clientSock, "You're using an invalid sessionID, please stop doing nasty things!");
        }
        else if (user.isFriend(friendNick))
            writeMsg(this.clientSock, "Don't worry, you and " + friendNick + " are already friends.");
        else if (db.getUser(friendNick) == null)
            writeMsg(this.clientSock, "We don't have that user registered with us. Is that your imaginary friend?");
        else {
            synchronized(user){
                user.addFriend(friendNick);
            }
            User friend = db.getUser(friendNick);
            synchronized(friend){
                friend.addFriend(nickname);
            }
            writeMsg(this.clientSock, friendNick + " succesfully added to your friend list.");
            System.out.println(nickname + " and " + friendNick + " are now friends!");
        } 
    }

    private void friend_list(String nick, String sessionID){
        User user = db.getUser(nick);
        if (!user.getId().equals(sessionID)){
            writeMsg(this.clientSock, "You're using an invalid sessionID, please stop doing nasty things!");
        }
        else {
            String message = new String();
            synchronized (user){
                ArrayList<String> friends = user.getFriends();
                for (String s : friends)
                    message += s + " ";
            }
            writeMsg(this.clientSock, message);
            System.out.println(nick + " requested his friends list.");
        }
    }

    private void score(String nick, String sessionID){
        User user = db.getUser(nick);
        if (!user.getId().equals(sessionID)){
            writeMsg(this.clientSock, "You're using an invalid sessionID, please stop doing nasty things!");
        }
        else {
            int score = 0;
            synchronized(user){
                score = user.getScore();
            } 
            String message = Integer.toString(score);
            writeMsg(this.clientSock, message);
            System.out.println(nick + " requested his score.");
        }
    }

    private void scoreboard(String nick, String sessionID){
        User user = db.getUser(nick);
        if (!user.getId().equals(sessionID)){
            writeMsg(this.clientSock, "You're using an invalid sessionID, please stop doing nasty things!");
        }
        else {
            ArrayList<String> friends = user.getFriends();
            if (friends.isEmpty()){
                writeMsg(this.clientSock, "You don't have any friends, so we can't show you any score, sorry!");
                return;
            }
            else {
                ArrayList<User> scoreboardFriends = new ArrayList<User>();
                for (String friend : friends)
                    scoreboardFriends.add(db.getUser(friend));
                scoreboardFriends.add(db.getUser(nick));
                scoreboardFriends.sort(null);
                String message = new String();
                for (User u : scoreboardFriends) {
                    message += u.getNickname() + " " + u.getScore() + " ";
                }
                writeMsg(this.clientSock, message);
            }
            
        }
    }

    private void match(String nickname, String friendNick, String sessionID) throws SocketException, IOException, InterruptedException{
        User user = db.getUser(nickname);
        if (!user.isLogged())
            writeMsg(this.clientSock, "ERROR: You're not logged in!");
        else if (nickname.equals(friendNick))
            writeMsg(this.clientSock, "ERROR: You can't match yourself!");
        else if (!user.getId().equals(sessionID)){
            writeMsg(this.clientSock, "ERROR: You're using an invalid sessionID, please stop doing nasty things!");
        }
        else if (db.getUser(friendNick) == null)
            writeMsg(this.clientSock, "ERROR: We don't have that user registered with us. Is that your imaginary friend?");
        else if (!user.isFriend(friendNick))
            writeMsg(this.clientSock, "ERROR: You and " + friendNick + " are not friends! Add him first!");
        else if(!db.getUser(friendNick).isLogged())
            writeMsg(this.clientSock, "ERROR: your friend is not logged in!");
        else {
            User friend = db.getUser(friendNick);
            int port = friend.getUDP();
            System.out.println(Integer.toString(port));
            DatagramSocket sock = new DatagramSocket(0);

            ServerSocket challengeSock = new ServerSocket(0);

            String message  = nickname + " challenged you, sock at:" + challengeSock.getLocalPort();
            DatagramPacket req = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getLocalHost(), port);
            message = "TIMEOUT " + nickname;
            DatagramPacket timeout = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getLocalHost(), port);
            byte[] buf = new byte[512];
            DatagramPacket resp = new DatagramPacket(buf, buf.length);


            try {
                sock.setSoTimeout(15000);
                sock.send(req);
                writeMsg(this.clientSock, "Sent match request to: " + friendNick);
                System.out.println(nickname + " sent a match request to " + friendNick);
                sock.receive(resp);
                writeMsg(this.clientSock,  friendNick + " accepted your match request, sock at:" + challengeSock.getLocalPort());
                String contentString = new String(resp.getData(), resp.getOffset(), resp.getLength(),
                StandardCharsets.UTF_8);
                System.out.println(contentString);

                Thread[] challengeThreads = new Thread[2];
                HashMap<String, ArrayList<String>> words = extr.getWords();
           
                MatchHelper helper = new MatchHelper();

                Socket challenger = challengeSock.accept(); 
                challengeThreads[0] = new Thread(new MatchHandler(challenger, words, db.getUser(nickname), helper));
                Socket challenged = challengeSock.accept(); 
                challengeThreads[1] = new Thread(new MatchHandler(challenged, words, db.getUser(friendNick), helper));

                System.out.println("Accepted match connections from users " + nickname + friendNick);

                for (Thread t : challengeThreads)
                    t.start();
                for (Thread t : challengeThreads)
                    t.join();

            }

            catch (SocketTimeoutException e) {
                sock.send(timeout);
                writeMsg(this.clientSock, "Invitation to: " + friendNick + " timed out.");
                System.out.println(nickname + "'s match request to " + friendNick + " timed out");
            }

            finally {
                sock.close();
                //this.writeMsg(this.clientSock, "ERROR: There's been some problem with the request");
            }

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