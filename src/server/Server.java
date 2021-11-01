package server;
// домашка, группа 1313, урок от 29.10.2021  // идентификатор пользователя - сокет вместо юайди
// лабораторная работа №9 | чат консольный | имя уникально, без пробелов | приватные сообщения  | список онлайн пользователей
// проверка уникальности имен
// отправка личных сообщений: /m ivan Привет
// Итого: чат консольный | имя уникально, без пробелов | приватные сообщения  | список онлайн пользователей
// Метод для рассылки всем кроме отправителя не создан, не уверена, что получится и вообще уместен ли он.
// рассылка сообщений всем клиентам в 2х местах: 1)о новом присоединившемся клиенте; 2)публичные сообщения.
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
public static void main(String[] args) {
    try {
        ArrayList<User> users = new ArrayList<>(); // будет иметь вид: [server.User@9a9a12e, server.User@112ddb77, server.User@32388450]
        ServerSocket serverSocket = new ServerSocket(8179); // 8178
        System.out.println("Сервер запущен");
        while (true) {
            Socket socket = serverSocket.accept(); // Ожидаем подключения клиента
            User currentUser = new User(socket); // как только клиент подключился, создаем текущего с его сокетом
            users.add(currentUser); // добавляем в коллекцию текущего пользователя
            System.out.println("Клиент "+currentUser+" подключился"); // объект клиента выглядит примерно так: server.User@7291c18f
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (currentUser.getUserName()==null) { // цикл бесконечен пока текущий пользователь не введет адекватное имя
                            currentUser.getOut().writeUTF("Введите имя: ");// Спрашиваете имя клиента
                            String userName = currentUser.getIn().readUTF(); // Записываете его имя в переменную
                            if (userName.length()<3 || userName.indexOf(" ")>-1) {// проверка на Enter, пробел, короткое имя
                                currentUser.getOut().writeUTF("Имя должно содержать минимум 3 символа и не должно содержать пробелы.");
                            }
                            else { // имя адекватное
                                boolean identity = false;
                                for (User user : users) { // перебираем коллекцию
                                    if (user.getUserName()!=null) { // если имя задано
                                        if(user.getUserName().equals(userName)) { // сравниваем. если совпадает с только что введенным
                                            identity = true; // меняем значение переменной
                                        }
                                    }
                                }
                                if (identity == true) {
                                    currentUser.getOut().writeUTF("Пользователь с таким именем уже существует, придумайте другое имя.");
                                }
                                else {
                                    currentUser.setUserName(userName); // устанавливаем имя
                                    currentUser.getOut().writeUTF("Добро пожаловать на сервер, "+currentUser.getUserName()+"!");// Приветсвуете клиента на сервере
                                    // рассылка всем сообщения о новом клиенте
                                    for (User user: users) { // рассылка сообщений
                                        if(currentUser.getSocket().equals(user.getSocket())) continue; // кроме отправителя
                                        user.getOut().writeUTF("Пользователь "+currentUser.getUserName()+" присоединился к беседе."); // всем клиентам кроме отправителя
                                    }
                                    System.out.println("Пользователь "+currentUser.getUserName()+" присоединился к беседе."); // в консоль сервера
                                }
                            }
                        }//while
                        while (true) {
                            String request = currentUser.getIn().readUTF(); //Ожидаем сообщение от клиента
                            System.out.println(currentUser.getUserName()+": "+request); // в консоль сервера
                            if(request.equals("/onlineUsers")) {
                                String usersName = "";
                                for (User user: users) usersName += user.getUserName()+", "; // формируем строку с пользователями online
                                currentUser.getOut().writeUTF(usersName); // отправляем текущему клиенту
                            }
                            else if (request.indexOf("/m ")==0) { // отправка личных сообщений /m 111 привет тебе!
                                String[] arr = request.split(" "); // строку разбиваем на массив
                                String recipientName = arr[1]; // вычленяем имя рецепиента
                                request = ""; // обнуляем переменную
                                for (int i = 2; i < arr.length; i++) { //
                                    request += " "+arr[i]; // записываем в строку все сообщение реципиенту, состоящее из нескольких слов
                                }
                                boolean recipientIsHere = false; // наличие реципиента в чате
                                for (User user : users) {
                                    //if (user.contains(recipientName)) { // не сработает - user не коллекция, а объект
                                    if (user.getUserName().equals(recipientName)) {  // если в коллекции клиентов есть с нужным именем
                                        user.getOut().writeUTF("Личное от "+currentUser.getUserName() + ":" + request); // отправляем ему сообщение
                                        recipientIsHere = true;
                                        break; // как только нашли, выходим из цикла
                                    }
                                }
//								if (users.contains(recipientName)) { // этот способ не работает.
//									System.out.println("такой есть"); // В коллекции объекты клиентов, а не их имена
//									recipientIsHere = true;
//								}
//								else {
//									System.out.println("users.contains(recipientName): "+users.contains(recipientName));
//									System.out.println(users);
//								}
                                if (recipientIsHere == false) {
                                    currentUser.getOut().writeUTF("Пользователь "+recipientName + " отсутствует в чате. Ваше сообщение не доставлено!");
                                    System.out.println("Выше сообщение для отсутствующего пользователя");
                                }
                            }
                            else // обычные сообщения для всех
                                for (User user: users) { // рассылка сообщений
                                    if(currentUser.getSocket().equals(user.getSocket())) continue; // кроме отправителя
                                    user.getOut().writeUTF(currentUser.getUserName()+": "+request); // всем клиентам
                                }
                        }
                    } catch (IOException e) {
                        users.remove(currentUser); // удаляем из коллекции, чтобы не вызывать ошибку
                        System.out.println("Пользователь "+currentUser.getUserName()+" покинул чат."); // Сообщение на сервер
                        try {
                            for (User user: users) // рассылка известия всем
                                user.getOut().writeUTF(currentUser.getUserName()+" покинул чат");
                        }
                        catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            thread.start();
        }
    } catch (IOException e) {
        e.printStackTrace();
        System.out.println("Сервер не запустился. Возможно проблемы с портом.");
    }
}
}
