package server;
// домашка, группа 1313, урок от 03.11.2021, лаба 10.1  // пересылаем коллекцию имен по сети
// Сериализуем коллекцию, заменяем потоки ввода/вывода для пересылки объекта.
// Например: DataOutputStream out -> OojectOutputStream oos
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    static ArrayList<User> users = new ArrayList<>(); // в статическом методе public static void main(String[] args) можно использовать только статические свойства
// поэтому вынося сюда коллекцию делаем ее единой и доступной для всех (статической)
// Еще static позволяет обращаться к свойству класса без создания объекта.
// что представляет собой коллекция пользователей: [server.User@9a9a12e, server.User@112ddb77, server.User@32388450]
    public static void main(String[] args) {
        try {
            //ArrayList<User> users = new ArrayList<>(); // будет иметь вид: [server.User@9a9a12e, server.User@112ddb77, server.User@32388450]
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
                                currentUser.getOos().writeObject("Введите имя: ");// Спрашиваете имя клиента
                                String userName = currentUser.getIn().readUTF(); // Записываете его имя в переменную
                                if (userName.length()<3 || userName.indexOf(" ")>-1) {// проверка на Enter, пробел, короткое имя
                                    currentUser.getOos().writeObject("Имя должно содержать минимум 3 символа и не должно содержать пробелы.");
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
                                        currentUser.getOos().writeObject("Пользователь с таким именем уже существует, придумайте другое имя.");
                                    }
                                    else {
                                        currentUser.setUserName(userName); // устанавливаем имя
                                        currentUser.getOos().writeObject("Добро пожаловать на сервер, "+currentUser.getUserName()+"!");// Приветсвуете клиента на сервере
                                        // рассылка всем сообщения о новом клиенте
                                        for (User user: users) { // рассылка сообщений
                                            if(currentUser.getSocket().equals(user.getSocket())) continue; // кроме отправителя
                                            user.getOos().writeObject("Пользователь "+currentUser.getUserName()+" присоединился к беседе."); // всем клиентам кроме отправителя
                                        }
                                        System.out.println("Пользователь "+currentUser.getUserName()+" присоединился к беседе."); // в консоль сервера
                                        sendUserList(); // 1) рассылаем объект пользователей онлайн
                                    }
                                }
                            }//while
                            while (true) {
                                String request = currentUser.getIn().readUTF(); //Ожидаем сообщение от клиента
                                System.out.println(currentUser.getUserName()+": "+request); // в консоль сервера
                                if(request.equals("/onlineUsers")) { // работает и в этой версии, т.к. не удалили
                                    String usersName = "";
                                    for (User user: users) usersName += user.getUserName()+", "; { // формируем строку с пользователями online
                                        currentUser.getOos().writeObject(usersName); // отправляем текущему клиенту
                                        //sendUserList(); // не сработает, т.к. надо правильно принять объект. см. графический клиент
                                    }
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
                                            user.getOos().writeObject("Личное от "+currentUser.getUserName() + ":" + request); // отправляем ему сообщение
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
                                        currentUser.getOos().writeObject("Пользователь "+recipientName + " отсутствует в чате. Ваше сообщение не доставлено!");
                                        System.out.println("Выше сообщение для отсутствующего пользователя");
                                    }
                                }
                                else // обычные сообщения для всех
                                    for (User user: users) { // рассылка сообщений
                                        if(currentUser.getSocket().equals(user.getSocket())) continue; // кроме отправителя
                                        user.getOos().writeObject(currentUser.getUserName()+": "+request); // всем клиентам
                                    }
                            }
                        } catch (IOException e) {
                            users.remove(currentUser); // удаляем из коллекции, чтобы не вызывать ошибку
                            System.out.println("Пользователь "+currentUser.getUserName()+" покинул чат."); // Сообщение на сервер
                            try {
                                for (User user: users) // рассылка известия всем
                                    user.getOos().writeObject("Пользователь "+currentUser.getUserName()+" покинул чат"); // Сообщение всем клиентам online
                            }
                            catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            sendUserList(); // 2) рассылаем объект пользователей онлайн
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
    static void sendUserList() { // статический метод для рассылки списка пользователей онлайн // 3 раза в коде будет применен
        ArrayList<String> usersName = new ArrayList(); // можно использовать коллекцию, а лучше
        //String [] usersName = new String[users.size()]; // использовать массив не получится, т.к. хотя размер известен, но потребуется добавлять в него данные
        for (User user : users) { usersName.add(user.getUserName()); } // Добавляем в коллекцию все имена
        for (User user : users) { // эту коллекцию (объект) отправляем по сети всем
            try {
                user.getOos().writeObject(usersName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
