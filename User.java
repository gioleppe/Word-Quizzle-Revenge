import java.util.ArrayList;

/**
 * Class modeling the user. It has methods to get password hashes and nicknames.
 */
public class User {

    private String nickname;
    private int passwordHash = 0;
    private ArrayList<String> friends = new ArrayList<String>();
    private int score = 0;

    /**
     * Class constructor.
     * @param nick the nickname of the user
     * @param pwd the password the user wants
     */
    public User(String nick, String pwd){
        nickname = nick;
        passwordHash = pwd.hashCode();
    }

    /**
     * Getter method, returns the nickname of the user
     * @return this user's nickname;
     */
    public String getNickname(){
        return nickname;
    }

    /**
     * Getter method for the password hash
     * @return the hash of this user's password.
     */
    public int getHash(){
        return passwordHash;
    }

    /**
     * This method is used to update the user's score.
     * @param delta the score change. Could be either positive or negative.
     */
    public void modifyScore(int delta){
        score += delta;
    }

    /**
     * This method is used to add a friend to the user's friendlist.
     * @param friendNick the friend to be added to this user's friendlist
     * @return a boolean representing the outcome of the operation 
     */
    public boolean addFriend(String friendNick){
        if (friends.contains(friendNick))
            return false;
        else 
            return true;
    }

}