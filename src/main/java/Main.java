import Components.TcpServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class Main {

  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");
      AnnotationConfigApplicationContext context =
              new AnnotationConfigApplicationContext(AppConfig.class);
      TcpServer app =context.getBean(TcpServer.class);

      app.startServer();
    //  Uncomment this block to pass the first stage
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;
        try {
          serverSocket = new ServerSocket(port);
          serverSocket.setReuseAddress(true);

            while (true) {
                clientSocket = serverSocket.accept();
                Socket finalClientSocket = clientSocket;
                CompletableFuture.runAsync(() -> {
                    try {
                        handleClient(finalClientSocket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

        } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
        } finally {
          try {
            if (clientSocket != null) {
                clientSocket.close();
            }
          } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
          }
        }
  }

    private static void handleClient(Socket clientSocket) throws IOException {
        InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream();
        Scanner sc = new Scanner(inputStream);
//        sc.useDelimiter("\r\n\r\n\r\n\r\n");

        while(sc.hasNextLine()){
            String nextLine = sc.next();
            System.out.println("=====================================================================================================");
            System.out.println(nextLine);
            System.out.println("=====================================================================================================");
//            if(nextLine.contains("PING")){
//                outputStream.write("+PONG\r\n".getBytes());
//            }
//            if(nextLine.contains("ECHO")){
//                String respHeader = sc.nextLine();
//                String respBody = sc.nextLine();
//                String response = respHeader + "\r\n" + respBody+"\r\n";
//                outputStream.write(response.getBytes());
//            }
        }

    }

    public static String encodingRespString(String s){
      String resp = "$";
      resp+=s.length();
      resp+="\r\n";
      resp+=s;
      resp+="\r\n";
      return resp;
    }
}
