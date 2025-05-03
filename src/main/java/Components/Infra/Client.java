package Components.Infra;

import Components.Repository.Value;
import Components.Service.ResponseDto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

public class Client {
    public Socket socket;
    public InputStream inputStream;
    public OutputStream outputStream;

    private boolean transactionalContext;

    public List<String>  transactionResponse;


    public Queue<String[]> commandQueue;

    public boolean getTransactionContext(){
        return transactionalContext;
    }

    public boolean beginTransaction(){
        if(transactionalContext)
            return false;
        transactionalContext = true;

        commandQueue = new LinkedList<>();
        return true;
    }

    public void endTransaction(){
        commandQueue = null;
        transactionalContext = false;
    }


    public int id;

    public Client(Socket socket,
                  InputStream inputStream,
                  OutputStream outputStream,
                  int id){
        this.socket = socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.id = id;
    }

    public void send(ResponseDto res) throws IOException {
        String response = res.response;
        byte[] data = res.data;
        if(response !=null && !response.isEmpty())
            outputStream.write(response.getBytes());
        if(data!=null)
            outputStream.write(data);
    }

    public void send(String response, byte[] data) throws IOException {

        if(response !=null && !response.isEmpty())
            outputStream.write(response.getBytes());
        if(data!=null)
            outputStream.write(data);
    }

    public void send(byte[] data) throws IOException {
        if(data!=null)
            outputStream.write(data);
    }
    public void send(String data) throws IOException {
        if(data!=null)
            outputStream.write(data.getBytes());
    }
}
