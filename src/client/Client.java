package client;
// Здесь принимаем объект просто для наглядности. Теперь клиента запускаем через графический вариант
// Например через проект E:\ITcourse\professional_2021\java_educational_projects\folder_chats_1313\graphical_03112021_laba_10.1\GUI_chat_1313

import java.io.DataInputStream; // заменен на ObjectInputStream
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
public static void main(String[] args) {
    try {
        Scanner scanner = new Scanner(System.in);
        Socket socket = new Socket("127.0.0.1", 8179); //8178 // 127.0.0.1 // 5.181.108.28
        System.out.println("Успешно подключен");
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        //DataInputStream in = new DataInputStream(socket.getInputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        //String response = in.readUTF();
                        //System.out.println(response);
                        ArrayList<String> usersName = new ArrayList(); //Создадим здесь идентичную коллекцию как и на стороне сервера
                        String response; // Присвоим значение, когда выясним, что присланный объет именно строка
                        Object object = ois.readObject(); // тут снова требуется try/catch, т.к. исключения могут быть не только IOException, заменяем его на Exception
                        //System.out.println(object.getClass()); // метод getClass() покажет в консоли тип объекта (строка, массив, еще что-то) // class java.lang.String

                        if(object.getClass().equals(usersName.getClass())) { // если пришел тип объекта ArrayList, то
                            usersName = (ArrayList<String>) object; // этот объект надо преобразовать в коллекцию с помощью кастования - преобразования типов
                            System.out.println("Пользователи онлайн: "+usersName); // просто для наглядности
                        }
                        else {
                            response = object.toString(); // Преобразуем в строку
                            System.out.println(response);
                        }
                    }
                } catch (Exception e) {// IOException -> Exception
                    e.printStackTrace();
                    System.out.println("нет подключения.");
                }

            }
        });
        thread.start();
        while (true){
            String text = scanner.nextLine();
            out.writeUTF(text);
        }
    } catch (IOException e) {
        e.printStackTrace();
        System.out.println("Подключиться по указанныем ip и/или порту не удалось.");
    }
}
}
