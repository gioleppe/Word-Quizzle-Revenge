import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * The match handler class implements the server-side logic behind each match. 
 */
public class MatchHandler implements Runnable{

    Socket clientSock = null;
    HashMap<String, ArrayList<String>> dict = null;
    User challenger = null;
    int matchDuration = 0;
    ArrayList<String> answers = null;

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

    /**
    * The Matchhandler constructor is used to set up everything in order to serve the match request.
    * @param sock the client sock used for communicating results.
    * @param words
    * @param nick
    * @param duration
     */
    public MatchHandler(Socket sock, HashMap<String, ArrayList<String>> words, User nick, ArrayList<String> ans,  int duration){
        clientSock = sock;
        dict = words;
        challenger = nick;
        matchDuration = duration;
        answers = ans;
        

    }

    public void run(){

        String resp = "";
        long startTime = System.currentTimeMillis();

        for (String word : dict.keySet()){
            long elapsed = System.currentTimeMillis()-startTime;
            if (elapsed>(matchDuration*1000)){
                writeMsg(clientSock, "Match over: your time has run out! now wait for results.");
                break;
            }
            writeMsg(clientSock, "Translate the word: " + word);
            resp = readMsg(clientSock);
            //System.out.println(resp);
            answers.add(resp);
        }


        //helper.setAnswer(challenger, answers);

        writeMsg(clientSock, "Match over: wait for results!");
        return;

    }
}