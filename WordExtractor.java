import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//WordExtractor class used to get words and their relative translations. 
//The class is a Singleton and uses lazy inizialization.
public class WordExtractor{

    private static WordExtractor extractor = null; 
    private Integer wordCount = null;
    private ArrayList<String> dictionary = null;

    /**
     * The private constructor to the class. 
     * @throws IOException if there's some problem with either the readLine() or the close()
     * @throws FileNotFoundException if the dictionary is not in the folder (or has a wrong name).
     */
    private WordExtractor() throws IOException, FileNotFoundException {
        //opens up a filereader and decorates it with a BufferedReader for convenience.
        // also instantiates the dictionary arraylist.
        dictionary = new ArrayList<>();
        FileReader filereader = new FileReader("dictionary.txt");
        BufferedReader reader = new BufferedReader(filereader);
        String readString;
        while ((readString = reader.readLine()) != null){
            dictionary.add(readString);
        }
        reader.close();

    }

    /**
     * Returns the singleton instance to the caller.
     * @return the WordExtractor instance
     * @throws IOException if there's a problem instantiating the singleton. 
     */
    public static WordExtractor getInstance() throws IOException{
        if (extractor == null){
            extractor = new WordExtractor();
        }
        return extractor;
    }

    /**
     * this method inits the WordExtractor singleton.
     * @param wordCount the number of words to be extracted.
     */
    public void init(int wordCount){
        if (this.wordCount == null)
            this.wordCount = wordCount;
        else 
            System.out.println("Already initialized!");
    }

    private ArrayList<String> getTranslation(String word) throws IOException {
        ArrayList<String> translations = new ArrayList<String>();
        // building the request URL to connect to.
        String HTTPrequest = "https://api.mymemory.translated.net/get?q=" + word.replace(" ", "%20")
                + "&langpair=it|en";
        URL mymemoryAPI = new URL(HTTPrequest);
        InputStream stream = mymemoryAPI.openStream();
        BufferedReader buff = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        String inputLine;
        StringBuffer content = new StringBuffer();
        // reading the response to StringBuffer content
        while ((inputLine = buff.readLine()) != null) {
            content.append(inputLine);
        }
        // parsing to json and extracting the translations using GSON
        JsonElement jelement = JsonParser.parseString(content.toString());
        JsonObject jobject = jelement.getAsJsonObject();
        JsonArray jarray = jobject.get("matches").getAsJsonArray();
        for (int i = 0; i < jarray.size(); i++) {
            JsonObject translated = (JsonObject) jarray.get(i);
            String translation = translated.get("translation").getAsString();
            // cleaning junk from the received translations (MyMemory has a lot of weird/nonsense translations)
            translations.add(translation.toLowerCase().replaceAll("[^a-zA-Z0-9\\u0020]", ""));

        }
        return translations;
    }

    /**
     * This method is used to get a set comprising n° wordCount words to be used during a challenge.
     * @return the requested word set
     * @throws IOException if there's some problem during the translation phase.
     */
    public HashMap<String, ArrayList<String>> getWords() throws IOException{
        //instantiates a new String ArrayList to put selected words into,
        // and an hashmap to return the word set to the caller
        ArrayList<String> chosenWords = new ArrayList<String>();
        HashMap<String, ArrayList<String>> challengeSet = new HashMap<String, ArrayList<String>>();
        Random rand = new Random();
        int i = 0;
        String word;
        int dictSize = dictionary.size();
        //retrieves #wordCount different words from the dictionary. 
        while (i < wordCount){
            word = dictionary.get(rand.nextInt(dictSize));
            if (!chosenWords.contains(word)){
                chosenWords.add(word);
                i++;
            }
        }
        //cycle through the words and get the corresponding translations.
        for (String s : chosenWords){
            ArrayList<String> translations = getTranslation(s);
            challengeSet.put(s, translations);
        }
        return challengeSet;
    }

    /**
     * Prints the dictionary. this method is for testing purposes only.
     */
    public void printDictionary(){
        for (String e : dictionary)
            System.out.println(e);
    }

}