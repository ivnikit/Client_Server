import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;

public class Client_Server {
    private static final int SERVER_PORT = 9050;

    public static void main(String[] args) {
        Server server = new Server(SERVER_PORT);
        server.setDaemon(true);
        server.start();
        Client client = new Client();
        client.start();
    }
}

class Server extends Thread {

    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void run() {

        System.out.println("Поток сервера: Сервер ожидает подключения. Порт сервера - " + port);//9050

        try (ServerSocket serverSocket = new ServerSocket(port);
             Socket socket = serverSocket.accept()) {

            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            System.out.println("Поток сервера: Соединение с клиентом установлено.");
            String query = dataInputStream.readUTF();
            URL url = new URL(query);

            InputStream dataStream = url.openStream();

            byte[] buffer = new byte[1024];
            int size;

            while ((size = dataStream.read(buffer)) != -1) {
                socket.getOutputStream().write(buffer, 0, size);
            }
            socket.getOutputStream().flush();
            dataStream.close();

            System.out.println("Поток сервера: Данные отправлены.");

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Поток сервера: Работа сервера завершена.");
    }
}

class Client extends Thread {

    public void run() {

        System.out.println("Поток клиента: Клиент запущен.");

        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите адрес сервера:"); //127.0.0.1
        String host = scanner.nextLine();

        System.out.println("Введите номер порта:");
        int port = Integer.parseInt(scanner.nextLine());

        try (Socket socket = new Socket(host, port);
             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("Введите url: ");
            String query = scanner.nextLine();

            System.out.println("Поток клиента: Отправляю запрос - " + query);

            dataOutputStream.writeUTF(query);
            dataOutputStream.flush();

            byte[] buffer = new byte[4096];
            int size;

            System.out.println("Поток клиента: Получен ответ от сервера.");

            String[] urlSegments = query.trim().split("/");
            String fileName = urlSegments[urlSegments.length - 1];

            File file = new File(fileName);

            FileOutputStream outputStream = new FileOutputStream(file);

            while ((size = socket.getInputStream().read(buffer)) != -1) {
                outputStream.write(buffer, 0, size);
            }
            outputStream.flush();
            outputStream.close();

            System.out.println("Поток клиента: Файл " + fileName + " создан.");

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Поток клиента: Работа клиента завершена.");
    }
}