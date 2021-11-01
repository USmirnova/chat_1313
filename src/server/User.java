package server;
// Пользователь идентифицируется по соккету, не используем uuid
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class User {
private Socket socket;
private String userName;
private DataOutputStream out;
private DataInputStream in;

public User(Socket socket) throws IOException {
	this.socket = socket;
	this.in = new DataInputStream(socket.getInputStream());
	this.out = new DataOutputStream(socket.getOutputStream());
}
public String getUserName() {return userName;}
public void setUserName(String userName) {this.userName = userName;}

public DataOutputStream getOut() {return out;}
public DataInputStream getIn() {return in;}

public Socket getSocket() {
	return socket;
}
}
