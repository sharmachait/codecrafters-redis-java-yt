package Components;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TcpServer {
    @Autowired
    private RespSerializer respSerializer;
    public void startServer(){
        respSerializer.printWokring();
    }
}
