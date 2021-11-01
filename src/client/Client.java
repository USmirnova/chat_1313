package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
public static void main(String[] args) {
    try {
        Scanner scanner = new Scanner(System.in);
        Socket socket = new Socket("127.0.0.1", 8179); //8178
        System.out.println("Успешно подключен");
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String response = in.readUTF();
                        System.out.println(response);
                    }
                } catch (IOException e) {
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
    }
}
}
