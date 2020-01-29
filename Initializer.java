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
        extractor.printDictionary();

        /*HashMap<String, ArrayList<String>> challengeSet = extractor.getWords();
        for (String e : challengeSet.keySet()){
            System.out.println("Selected Word: " + e + " with " + challengeSet.get(e).size() + " different translations that are: ");
            for (String s : challengeSet.get(e))
                System.out.print(s + " ");
            System.out.println();
        }*/

        //creates a new db and the RegistrationHandler RMI handler
        UserDB db = new UserDB();
        RegistrationHandler regHandler = new RegistrationHandler(db);
        regHandler.init();
        System.out.println(regHandler.register("cazzolo", "gioleppe"));
        Thread.sleep(20000);
        System.out.println("Finished all the testing fuckssss. Check for serialization");
        UnicastRemoteObject.unexportObject(regHandler, true);
    }
}