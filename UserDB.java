import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

/**
 * The UserDB class 
 */
public class UserDB{

    ConcurrentHashMap<String, User> db = new ConcurrentHashMap<String, User>();

    /**
     * Constructor to the UserDB class, initializes the db by deserializing it,
     *  and starts the SerializerDaemon thread.
     */
    public UserDB(){
        deserialize();
        SerializerDaemon serializer = new SerializerDaemon(db);
        Thread serializerThread = new Thread(serializer);
        serializerThread.setDaemon(true);
        serializerThread.start();
    }

    /**
     * This methos is used to deserialize the database from the filesystem.
     * If the database is empty it doesn't 
     */
    private void deserialize(){

        String jsonDB = null;
        Gson gson = new Gson();
        //creates a TypeToken for the ConcurrentHashMap. Required by GSON for a correct deserialization. 
        final Type type = new TypeToken<ConcurrentHashMap<String, User>>() {
        }.getType();
        try {
            jsonDB = new String(Files.readAllBytes(Paths.get("Database.json")), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        //if the database is empty just return, since it doesn't make sense to deserialize it.
        //this is an edge case, if the server was shut down/killed before the first serialization.
        if (jsonDB.equals(""))
            return;

        ConcurrentHashMap<String, User> deserializedDB = gson.fromJson(jsonDB, type);
        db = deserializedDB;
    }

    public void init(){
    }

    /**
     * getter method used to retrieve a User from the db. 
     * @param nick the nickname of the user to be retrieved
     * @return the instance of the User if it was present, else it return false.
     */
    public User getUser(String nick){
        return db.get(nick);
    }

    /**
     * this method is used to add a user to the db.
     * @param nick the nick of the user, used as a key in the concurrent hashmap
     * @param newUser the new user class to be added.
     * @return a boolean representing the outcome of the operation.
     */
    public boolean addUser(String nick, User newUser){
        if (db.containsKey(nick))
            return false;
        else {
            db.put(nick, newUser);
            return true;
        }
    }

    /**
     * Main method used for testing purposes only. 
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        UserDB database = new UserDB();
        System.out.println(database.addUser("ciccio", new User("ciccio", "cazzo")));
        Thread.sleep(20000);
        System.out.println("check for serialization!");
    }
}

