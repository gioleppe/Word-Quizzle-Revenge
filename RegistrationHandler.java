import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * This class implements the remote registration method used by the client to register to the server.
 * In order to do so, it exports itself as a remote object for RMI.
 */
public class RegistrationHandler extends UnicastRemoteObject implements RegistrationInterface{

    /**
     * serialVersionUID required to avoid warning since this class may change.
     */
    private static final long serialVersionUID = 1L;
    UserDB db = null;

    /**
     * This method is the implementation of the remote method specified in the RegistrationInterface.
     * This is used to register to the server.
     * @param nickname the nickname of the user who wants to register
     * @param password the password of the user who wants to register
     */
    public String register(String nickname, String password){
        User registeringUser = new User(nickname, password);
        //if add user returns true, return success, else failure.
        if (db.addUser(nickname, registeringUser))
            return ("Registration Successful");
        else {
            return ("Registration failed!");
        }
    }

    /**
     * Class constructor used to instantiate the RegistrationHandler.
     * @param database the database used for registration means
     * @throws RemoteException must be thrown for RMI
     */
    public RegistrationHandler(UserDB database) throws RemoteException {
        db = database;
    }

    /**
     * Used to initialize the RegistrationHandler. This method is also used to export self as a remote object.
     * @throws RemoteException if there's some problem
     */
    public void init() throws RemoteException {
        //creates the registry and binds the stub in a freshly created registry.
        //there's no need to export the object since the 
        LocateRegistry.createRegistry(6464);
        Registry r = LocateRegistry.getRegistry(6464);
        r.rebind("REGISTRATION-HANDLER", this);
        System.out.println("Initialized the RegistrationHandler");
    }

}