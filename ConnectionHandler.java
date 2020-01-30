import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

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

        System.out.println("At least I got this far!");

        String response = null;
        try {
            // blocks waiting from an answer from the server
            response = in.readLine();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        System.out.println("I never got here");

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
        } catch (final IOException e) {
            e.printStackTrace();
        }
        
    }

    public void run(){
        try {System.out.println("hey man u out of ur mind!");
        System.out.println(this.readMsg(this.clientSock));
        writer.write("diocane");
        writer.newLine();
        writer.flush();
        clientSock.close();}
        catch (Exception e){}
    }
}