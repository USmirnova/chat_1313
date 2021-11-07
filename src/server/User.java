package server;
// Пользователь идентифицируется по соккету, не используем uuid
// DataOutputStream out -> OojectOutputStream oos
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class User {
private Socket socket;
private String userName;
//private DataOutputStream out; // уже не нужен
private DataInputStream in; // не удаляем, т.к. используется в файле Server.java
private ObjectOutputStream oos;

public User(Socket socket) throws IOException {
	this.socket = socket;
	this.in = new DataInputStream(socket.getInputStream()); // Используется в серверной части для получения имени и сообщений от клиента
	//this.out = new DataOutputStream(socket.getOutputStream());
	this.oos = new ObjectOutputStream(socket.getOutputStream()); // чтобы с сервера отправлять объект по сети
}
public String getUserName() {return userName;}
public void setUserName(String userName) {this.userName = userName;}

//public DataOutputStream getOut() {return out;}
public DataInputStream getIn() {return in;}
public ObjectOutputStream getOos() {return oos;} // создаем геттер для использования этого свойства

public Socket getSocket() {
	return socket;
}
}
