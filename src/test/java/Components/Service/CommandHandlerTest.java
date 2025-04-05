package Components.Service;

import Components.Server.RedisConfig;
import Components.Server.TcpServer;
import Config.AppConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AppConfig.class)
public class CommandHandlerTest {

    @Autowired
    CommandHandler commandHandler;

    @BeforeAll
    public static void setUp(@Autowired ApplicationContext context) throws InterruptedException {

        RedisConfig redisConfig = context.getBean(RedisConfig.class);

        redisConfig.setPort(6379);
        redisConfig.setRole("master");

        TcpServer app = context.getBean(TcpServer.class);

        CompletableFuture.runAsync(()->{
            app.startServer(6379);
        });

        Thread.sleep(1000);

    }

    @Test
    public void testInfo(){
        String result = commandHandler.info(new String[]{"INFO", "replication"});
        assertEquals("$11\r\nrole:master\r\n", result);
    }

}