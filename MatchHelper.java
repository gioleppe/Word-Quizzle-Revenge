import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class MatchHelper{
    
    private ConcurrentHashMap<User, ArrayList<String>> challengers = new ConcurrentHashMap<User, ArrayList<String>>();

    public MatchHelper(){
    }

    public void setAnswer(User challenger, ArrayList<String> answers){
        challengers.put(challenger, answers);
    }

    

}