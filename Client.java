import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class Client{

    private Console cons;
    private String sessionID = "notLogged";
    private String nick = null;
    private boolean logged = false;
    private DatagramSocket sockUDP = null;
    private ConcurrentHashMap<String, DatagramPacket> receivedChallenges = null;

    /**
     * Constructor to the User class.
     */
    public Client(){
        cons = System.console();
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

    /**
     * This method calls the remote method to register an user.
     * @param nick new user's nickname.
     * @param pwd user's password.
     * @throws RemoteException
     */
    public void registration(String nick, String pwd) throws RemoteException{
        RegistrationInterface remoteHandler = null;
        Remote remoteObj = null;
        Registry r = LocateRegistry.getRegistry(6464);

        try {
            remoteObj = r.lookup("REGISTRATION-HANDLER");
            remoteHandler = (RegistrationInterface) remoteObj;
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // calls the remote method and prints the result of the invocation
        System.out.println(remoteHandler.register(nick, pwd));
    }

    /**
     * Method used by an user to login.
     * @param nickname user's nickname.
     * @param password user's password.
     * @param listenerPort user's UDP port.
     * @throws UnknownHostException
     * @throws IOException
     */
    public void login(String nickname, String password, String listenerPort) throws UnknownHostException, IOException{
        Socket sock = new Socket("127.0.0.1", 1518);
        String message = "login " + nickname + " " + password + " " + listenerPort;
        this.writeMsg(sock, message);
        String response = this.readMsg(sock);
        System.out.println(response);
        String[] raw = response.split(" ");
        if (raw[0].equals("ERROR!"))
            return;
        nick = nickname;
        logged = true;
        sessionID = response.substring(response.indexOf(":") + 1);

    }

    /**
     * Method used by an user to logout from the server.
     * @throws UnknownHostException
     * @throws IOException
     */
    public void logout() throws UnknownHostException, IOException{
        if (!logged){
            System.out.println("You're not logged in!");
            return;
        }
        Socket sock = new Socket("127.0.0.1", 1518);
        String message = "logout " + nick + " " + sessionID;
        this.writeMsg(sock, message);
        String response = this.readMsg(sock);
        System.out.println(response);
        sessionID = "notLogged";
        String[] raw = response.split(" ");
        if (raw[0].equals("ERROR!"))
            return;
        logged = false;

    }

    /**
     * Used by an user to add someone as his friend.
     * @param nick user's nickname.
     * @param friendNick friend's nickname.
     * @param sessionID sessionID used to identify user's connection.
     * @throws UnknownHostException
     * @throws IOException
     */
    public void add_friend(String nick, String friendNick, String sessionID) throws UnknownHostException, IOException{
        if (!logged){
            System.out.println("You're not logged in!");
            return;
        }
        Socket sock = new Socket("127.0.0.1", 1518);
        String message = "friend " + nick + " " + friendNick + " " + sessionID;
        this.writeMsg(sock, message);
        String response = this.readMsg(sock);
        System.out.println(response);
    }

    /**
     * Used by an user to request his friend list.
     * @throws UnknownHostException
     * @throws IOException
     */
    public void friend_list() throws UnknownHostException, IOException{
        if (!logged){
            System.out.println("You're not logged in!");
            return;
        }
        Socket sock = new Socket("127.0.0.1", 1518);
        String message = "friend_list " + this.nick + " " + sessionID;
        this.writeMsg(sock, message);
        String response = this.readMsg(sock);
        System.out.println(response);
    }

    /**
     * Used by an user to get his score.
     * @throws UnknownHostException
     * @throws IOException
     */
    public void score() throws UnknownHostException, IOException{
        if (!logged){
            System.out.println("You're not logged in!");
            return;
        }
        Socket sock = new Socket("127.0.0.1", 1518);
        String message = "score " + this.nick + " " + sessionID;
        this.writeMsg(sock, message);
        String response = this.readMsg(sock);
        System.out.println(response);
    }

    /**
     * Used by an user to show his scoreboard.
     * @throws UnknownHostException
     * @throws IOException
     */
    public void scoreboard() throws UnknownHostException, IOException{
        if (!logged){
            System.out.println("You're not logged in!");
            return;
        }
        Socket sock = new Socket("127.0.0.1", 1518);
        String message = "scoreboard " + this.nick + " " + sessionID;
        this.writeMsg(sock, message);
        String response = this.readMsg(sock);
        System.out.println(response);
    }

    /**
     * Method used by an user to match a friend of his.
     * @param friendNick friend's nickname.
     * @throws UnknownHostException
     * @throws IOException
     * @throws InterruptedException
     */
    public void match(String friendNick) throws UnknownHostException, IOException, InterruptedException{
        if (!logged){
            System.out.println("You're not logged in!");
            return;
        }
        Socket sock = new Socket("127.0.0.1", 1518);
        String message = "match " + this.nick + " " + friendNick + " " + sessionID;
        this.writeMsg(sock, message);
        String response = this.readMsg(sock);
        if (response.substring(0, response.indexOf(":")).equals("ERROR")){
            System.out.println(response.substring(response.indexOf(" ") + 1));
            return;
        }
        else {
            System.out.println(response);
        }
        response = this.readMsg(sock);
        System.out.println(response);

        //System.out.println(response.substring(0, response.indexOf(",")));
        if (response.substring(0, response.indexOf(" ")).equals("Invitation")){
            return;
        }
        else {
        //open match
        Socket newSock = new Socket("127.0.0.1", Integer.parseInt(response.substring(response.indexOf(":") + 1)));
        matchLogic(newSock);
        newSock.close();
        }
    }

    /**
     * Method used to show all pending matches.
     */
    private void showMatches(){
        if (!logged){
            System.out.println("You're not logged in!");
            return;
        }
        else if (receivedChallenges.isEmpty()){
            System.out.println("You haven't received any challenge yet!");
        }
        else{
            System.out.println("You have received challenges from the following people:");
            for (String e : receivedChallenges.keySet())
                System.out.println(e + " ");
        }

    }

    /**
     * Used by a friend to accept one of his pending challenges.
     * @param friendNick friend's nickname.
     * @throws IOException
     * @throws InterruptedException
     */
    private void acceptMatch(String friendNick) throws IOException, InterruptedException {
        if (!logged){
            System.out.println("You're not logged in!");
            return;
        }
        else if (!receivedChallenges.containsKey(friendNick)){
            System.out.println("Your friend didn't challenge you yet!");
            return;
        }
        DatagramPacket request = this.receivedChallenges.get(friendNick);
        String msg = "accepted";
        byte[] resp = msg.getBytes();
        DatagramPacket acceptance = new DatagramPacket(resp, resp.length, request.getAddress(), request.getPort());
        DatagramSocket sock = new DatagramSocket(0);
        sock.send(acceptance);
        sock.close();

        //open match sock
        String contentString = new String(request.getData(), request.getOffset(), request.getLength(),
                    StandardCharsets.UTF_8);
        int port = Integer.parseInt(contentString.substring(contentString.indexOf(":") + 1));

        Socket newSock = new Socket("127.0.0.1", port);
        matchLogic(newSock);
        newSock.close();

    }

    /**
     * This method implements user's match logi.
     * @param sock the freshly opened socket used to exchange messages with the server.
     * @throws InterruptedException
     */
    private void matchLogic(Socket sock) throws InterruptedException{
        System.out.println("Match starting in a while, be ready!");
        Thread.sleep(500);
        String resp = null;
        String req = null;
        while (!(resp = readMsg(sock)).substring(0, resp.indexOf(":")).equals("Match over")){
            System.out.println(resp);
            req = cons.readLine("%s ", ">"); 
            writeMsg(sock, req);
        }
        System.out.println(resp);
        return;
    }


    /**
     * This method is used to parse user's input.
     * @param input user's input string.
     * @throws IOException
     * @throws RemoteException
     * @throws InterruptedException
     */
    private void parseInput(final String input) throws IOException, RemoteException, InterruptedException {
        final String[] params = input.split(" ");
        switch (params[0]) {
        case "registration":
            this.registration(params[1], params[2]);
            break;
        case "login":
            this.login(params[1], params[2], Integer.toString(this.sockUDP.getLocalPort()));
            break;
        case "logout":
            this.logout();
            break;
        case "add_friend":
            this.add_friend(this.nick, params[1], this.sessionID);
            break;
        case "friend_list":
            this.friend_list();
            break;
        case "score":
            this.score();
            break;
        case "scoreboard":
            this.scoreboard();
            break;
        case "match":
            this.match(params[1]);
            break;
        case "show_matches":
            this.showMatches();
            break;
        case "accept_match":
            this.acceptMatch(params[1]);
            break;
        case "quit":
            System.exit(0);
        default:
            System.out.println("wrong usage");
            break;
        }
    }

    public static void main(String[] args) {

        if (args[0].equals("--help")) {
            System.out
                    .println("Commands:" + "\n" + "registration <username> <password>: registers to the remote service"
                            + "\n" + "login <username> <password>: logs in an user." 
                            + "\n" + "logout: logs out the user"
                            + "\n" + "add_friend <nickFriend>: adds nickFriend as a friend" 
                            + "\n" + "friend_list: shows the friend lists"
                            + "\n" + "match <nickFriend>: sends a match request to a friend" 
                            + "\n" + "show_matches: shows the pending match invitations" 
                            + "\n" + "accept_match: <nickFriend>: accepts nickFriend's match invitation" 
                            + "\n" + "score: shows the user's score" 
                            + "\n" + "scoreboard: shows the user's scoreboard" 
                            + "\n" + "quit: exits the WQWords client");
            System.exit(0);
        }

        Client cli = new Client();
        try {
            cli.sockUDP = new DatagramSocket(0);
        }
        catch (SocketException e){
            e.printStackTrace();
        }

        cli.receivedChallenges = new ConcurrentHashMap<String, DatagramPacket>();

        MatchListener UDPListener = new MatchListener(cli.sockUDP, cli.receivedChallenges);
        Thread listener = new Thread(UDPListener);
        listener.start();

        String input;

        while (true){
            input = cli.cons.readLine("%s ", ">");   
            try {
                cli.parseInput(input); 
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

    /**
 * This class implements the UDP socket listener waiting to receive match
 * invitations.
 * 
 */
class MatchListener implements Runnable {

    DatagramSocket UDPSocket;
    ConcurrentHashMap<String, DatagramPacket> challengers;

    /**
     * The constructor to MatchListener.
     * 
     * @param UDPSock     the UDP socket where the listener will wait for
     *                    invitations
     * @param challengers the HashMap all pending invitations are put
     */
    MatchListener(DatagramSocket UDPSock, ConcurrentHashMap<String, DatagramPacket> challengers) {
        this.UDPSocket = UDPSock;
        this.challengers = challengers;
    }

    public void run() {
        System.out.println("Started Listener thread");
        while (true) {
            byte[] buf = new byte[512];
            DatagramPacket response = new DatagramPacket(buf, buf.length);
            try {
                // receives datagrams
                UDPSocket.receive(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // gets the content from the packet
            String contentString = new String(response.getData(), response.getOffset(), response.getLength(),
                    StandardCharsets.UTF_8);
            // if timed out message removes the challenger from the pending list
            if (contentString.substring(0, contentString.indexOf(" ")).equals("TIMEOUT")) {
                String timedOutChallenger = contentString.substring(contentString.indexOf(" ") + 1);
                challengers.remove(timedOutChallenger);
                System.out.println(timedOutChallenger + "'s match request timed out.");
                continue;
            }
            String challenger = contentString.substring(0, contentString.indexOf(" "));
            System.out.println("Received a challenge from: " + challenger);
            System.out.print(">");
            // puts the challenger in the pending list
            challengers.put(challenger, response);

        }
    }
}