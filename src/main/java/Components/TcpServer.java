package Components;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TcpServer {

    private final RespSerializer respSerializer;
    public void startServer(){
        respSerializer.printWorking();
    }
}
