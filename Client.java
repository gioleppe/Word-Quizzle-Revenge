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
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Client{

    Console cons;
    String sessionID = null;
    String nick = null;

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

        System.out.println("At least I got this far!");

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

        System.out.println("At least I got THIS far!");

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

    public void login(String nickname, String password) throws UnknownHostException, IOException{
        Socket sock = new Socket("127.0.0.1", 1518);
        String message = "login " + nickname + " " + password;
        this.writeMsg(sock, message);
        String response = this.readMsg(sock);
        System.out.println(response);
        String[] raw = response.split(" ");
        if (raw[0].equals("ERROR!"))
            return;
        nick = nickname;
        sessionID = response.substring(response.indexOf(":") + 1);

    }

    public void logout() throws UnknownHostException, IOException{
        Socket sock = new Socket("127.0.0.1", 1518);
        String message = "logout " + nick + " " + sessionID;
        this.writeMsg(sock, message);
        String response = this.readMsg(sock);
        System.out.println(response);
        String[] raw = response.split(" ");
        if (raw[0].equals("ERROR!"))
            return;

    }



    private void parseInput(final String input) throws IOException, RemoteException, InterruptedException {
        final String[] params = input.split(" ");
        switch (params[0]) {
        case "registration":
            this.registration(params[1], params[2]);
            break;
        case "login":
            this.login(params[1], params[2]);
            break;
        case "logout":
            this.logout();
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

        /*try {Socket sock = new Socket(args[0], 1518);
        BufferedInputStream in = new BufferedInputStream(sock.getInputStream());
        BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream());
        byte[] buff = new byte[128];
        out.write("diocane".getBytes());
        out.flush();
        System.out.println("written everything");
        in.read(buff);
        String received = new String(buff);
        System.out.println(received);}
        catch (Exception e){
            e.printStackTrace();
        }*/
    }
}