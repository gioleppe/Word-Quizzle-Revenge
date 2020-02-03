import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.net.Socket;

/**
 * Server class used to implement the main loop in the application. implements a multithreaded server.
 */
public class Server implements Runnable{
    WordExtractor extractor = null;
    UserDB database = null;
    int challengeTimer = 0;
    int matchDuration = 0;
    ThreadPoolExecutor tpool = null;
    ServerSocket sSock = null;

    /**
     * The constructor to the Server class.
     * This takes all the appropriate parameters in order to inject dependencies into the ConnectionHandler class.
     * @param extr the word extractor
     * @param db the database
     * @param challengeTimer the duration of a match invitation
     * @param matchDuration the duration of a match
     */
    public Server(WordExtractor extr, UserDB db, int challengeTimer, int matchDuration){
        this.extractor = extr;
        this.database = db;
        this.challengeTimer = challengeTimer;
        this.matchDuration = matchDuration;
        this.tpool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        //the socket is opened on a specific port but this might be easily modified.
        try {
            this.sSock = new ServerSocket(1518);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void run(){

        while(true){
            try {
                // for each new connection spawn a connection handler.
                Socket clientSock = sSock.accept();
                ConnectionHandler handler = new ConnectionHandler(clientSock, database, extractor, challengeTimer, matchDuration);
                tpool.execute(handler);
            }
            catch (Exception e){

            }
        }
    }


}