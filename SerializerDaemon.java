import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.*;
import java.lang.reflect.Type;


public class SerializerDaemon implements Runnable{

    ConcurrentHashMap<String, User> db;

    /**
     * The constructor to the SerializerDaemon. 
     * Takes the database as an argument realizing a Dependency Injection inside the class.
     * @param database the ConcurrentHashMap database from the UserDB class.
     */
    public SerializerDaemon(ConcurrentHashMap<String, User> database){
        db = database;
    }

    /**
     * This method is used to actually serialize the database on the filesystem.
     * @throws IOException if there's some problem creating the FileWriter
     */
    public void serialize() throws IOException{
        Gson gson = new Gson();
        String json = gson.toJson(db);
        FileWriter writer = new FileWriter("./database.json");
        writer.write(json);
        writer.close();
    }

    public void run(){
        try {
            //serializes on disk every 15 seconds.
            Thread.sleep(15000);
            try {
                serialize();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }
    
}