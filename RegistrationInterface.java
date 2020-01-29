import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistrationInterface extends Remote {
    String register(String nickname, String password) throws RemoteException;
}