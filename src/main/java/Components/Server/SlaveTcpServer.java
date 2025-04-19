package Components.Server;

import Components.Infra.ConnectionPool;
import Components.Infra.Slave;
import Components.Service.CommandHandler;
import Components.Service.RespSerializer;
import Components.Infra.Client;
import Components.Service.ResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class SlaveTcpServer {
    private static final Logger logger = Logger.getLogger(SlaveTcpServer.class.getName());
    @Autowired
    private RespSerializer respSerializer;
    @Autowired
    private CommandHandler commandHandler;
    @Autowired
    private RedisConfig redisConfig;
    @Autowired
    private ConnectionPool connectionPool;
    public void startServer(){
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = redisConfig.getPort();

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);

            CompletableFuture<Void> slaveConnectionFuture = CompletableFuture.runAsync(this::initiateSlavery);
            slaveConnectionFuture.thenRun(()->System.out.println("Replication complted"));

            int id = 0;
            while (true) {
                clientSocket = serverSocket.accept();
                id++;
                Socket finalClientSocket = clientSocket;

                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream();

                Client client = new Client(finalClientSocket, inputStream, outputStream, id );
                CompletableFuture.runAsync(() -> {
                    try {
                        handleClient(client);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }
    }

    private void initiateSlavery() {
        try(Socket master = new Socket(redisConfig.getMasterHost(), redisConfig.getMasterPort())){
            InputStream inputStream = master.getInputStream();
            OutputStream outputStream = master.getOutputStream();
            byte[] inputBuffer = new byte[1024];

            //part 1 of the handshake
            byte[] data = "*1\r\n$4\r\nPING\r\n".getBytes();
            outputStream.write(data);
            int bytesRead = inputStream.read(inputBuffer,0,inputBuffer.length);
            String response = new String(inputBuffer,0,bytesRead, StandardCharsets.UTF_8);
            logger.log(Level.FINE, response);

            //part 2 of the handshake
            int lenListeningPort = (redisConfig.getPort()+"").length();
            int listeningPort = redisConfig.getPort();
            String replconf = "*3\r\n$8\r\nREPLCONF\r\n$14\r\nlistening-port\r\n$" +
                    (lenListeningPort+"") + "\r\n" + (listeningPort+"") +
                    "\r\n";
            data = replconf.getBytes();
            outputStream.write(data);
            bytesRead = inputStream.read(inputBuffer,0,inputBuffer.length);
            response = new String(inputBuffer,0,bytesRead, StandardCharsets.UTF_8);
            logger.log(Level.FINE, response);

            replconf = "*3\r\n$8\r\nREPLCONF\r\n$4\r\ncapa\r\n$6\r\npsync2\r\n";
            data = replconf.getBytes();
            outputStream.write(data);
            bytesRead = inputStream.read(inputBuffer,0,inputBuffer.length);
            response = new String(inputBuffer,0,bytesRead, StandardCharsets.UTF_8);
            logger.log(Level.FINE, response);

            // part 3 of the handshake
            String psync = "*3\r\n$5\r\nPSYNC\r\n$1\r\n?\r\n$2\r\n-1\r\n";
            data = psync.getBytes();
            outputStream.write(data);
            List<Integer> psyncResponse = handlePsyncResponse(inputStream);

            while(master.isConnected()){
                int offset = 1;
                StringBuilder sb = new StringBuilder();
                List<Byte> bytes = new ArrayList<>();

                while(true){
                    int b = inputStream.read();
                    if(b==(int)'*')
                        break;

                    offset++;
                    bytes.add((byte)b);

                    if(inputStream.available()<=0)
                        break;
                }

                for(Byte b : bytes)
                    sb.append((char)(b.byteValue() & 0xFF));

                if(bytes.isEmpty())
                    continue;
                String command = sb.toString();
                String[] parts = command.split("\r\n");

                if (command.equals("+OK\r\n"))
                    continue;
                String[] commandArray = respSerializer.parseArray(parts);

                String res = handleCommandsFromMaster(commandArray,master);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    public String handleCommandsFromMaster(String[] command, Socket ConnectionWithMaster) {
        String cmd = command[0];
        cmd = cmd.toUpperCase();
        String res = "";
        switch (cmd)
        {
            case "SET":
                System.out.println("Slave received: " +Arrays.stream(command).toList());
                res = commandHandler.set(command);
//                CompletableFuture.runAsync(()->sendCommandToSlaves(connectionPool.getSlaves(),command));
                break;

            case "PING":
                break;
            default:
                res = "+No Response\r\n";
                break;
        }

        return res;
    }
    public void sendCommandToSlaves(Queue<Slave> slaves, String[] command){
        for(Slave slave : slaves){
            String commandRespString = respSerializer.respArray(command);
            try {
                slave.send(commandRespString.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<Integer> handlePsyncResponse(InputStream inputStream) throws IOException {
        List<Integer> psyncResponse = new ArrayList<>();
        while(true){
            if(inputStream.available()<=0)
                continue;

            int b = inputStream.read();
            psyncResponse.add(b);
            if(b==(int)'*'){
                break;
            }
        }
        return psyncResponse;
    }

    private void handleClient(Client client) throws IOException {
        connectionPool.addClient(client);
        while(client.socket.isConnected()){
            byte[] buffer = new byte[client.socket.getReceiveBufferSize()];
            int bytesRead = client.inputStream.read(buffer);

            if(bytesRead > 0){
                // bytes parsing into strings
                List<String[]> commands = respSerializer.deseralize(buffer);

                for(String[] command :commands){
                    handleCommand(command, client);
                }
            }
        }
        connectionPool.removeClient(client);
        connectionPool.removeSlave(client);
    }

    private void handleCommand(String[] command, Client client) throws IOException {
        String res = "";
        byte[] data = null;
        switch (command[0]){
            case "PING":
                res = commandHandler.ping(command);
                break;
            case "ECHO":
                res = commandHandler.echo(command);
                break;
            case "SET":
                res = "-READONLY You can't write against a replica.\r\n";
                break;
            case "GET":
                res = commandHandler.get(command);
                break;
            case "INFO":
                res = commandHandler.info(command);
                break;
            case "PSYNC":
                ResponseDto resDto = commandHandler.psync(command);
                res = resDto.response;
                data = resDto.data;
                break;
        }
        client.send(res, data);

    }
}
