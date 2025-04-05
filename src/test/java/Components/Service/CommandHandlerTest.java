package Components.Service;

import Components.Server.RedisConfig;
import Components.Server.TcpServer;
import Config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(classes = AppConfig.class)
public class CommandHandlerTest {

    @Autowired
    CommandHandler commandHandler;

    @BeforeAll
    public static void setUp(){
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);
        TcpServer app = context.getBean(TcpServer.class);
        RedisConfig redisConfig = context.getBean(RedisConfig.class);
        redisConfig.setPort(6379);
        redisConfig.setRole("master");
        app.startServer(6379);
    }

    @Test
    public void testInfo(){
        String result = commandHandler.info(new String[]{"INFO", "replication"});
        assertEquals("$11\r\nrole:master\r\n", result);
    }

}