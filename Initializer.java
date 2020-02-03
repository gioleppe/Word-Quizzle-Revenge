import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

// The initializer class is used to setup and start everything server-side.
public class Initializer{
    public static void main(String[] args) throws Exception {
        String firstArg;
        //messing up with args in order to get everything right
        try {
            firstArg = args[0];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            firstArg = "--help";
        }
        if (firstArg.equals("--help")){
            System.out.println("To start the server use:\n$java -cp ./:lib/gson-2.8.6.jar Initializer [--help] [wordCount] [challengeTimer] [matchDuration]");
            System.exit(0);
        }

        if (args.length != 3){
            System.out.println("Wrong usage!, use -help for details");
            System.exit(0);
        }
        int wordCount = 0, challengeTimer = 0, matchDuration = 0;

        try {
            wordCount = Integer.parseInt(args[0]);
            challengeTimer = Integer.parseInt(args[1]);
            matchDuration = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("Only integer values accepted!");
        }

        //get the extractor instance.
        WordExtractor extractor = WordExtractor.getInstance();
        extractor.init(wordCount);

        //creates a new db and the RegistrationHandler RMI handler
        UserDB db = new UserDB();
        RegistrationHandler regHandler = new RegistrationHandler(db);
        regHandler.init();
        Thread serverThread = new Thread(new Server(extractor, db, challengeTimer, matchDuration));
        serverThread.start();
        System.out.println("Welcome to WordQuizzleRevenge. Use our wondrous client to play the game!");


        //UnicastRemoteObject.unexportObject(regHandler, true);
    }
}