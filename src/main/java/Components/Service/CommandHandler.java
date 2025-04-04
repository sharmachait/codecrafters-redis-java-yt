package Components.Service;

import Components.Repository.Store;
import Components.Server.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class CommandHandler {
    private static final Logger logger = Logger.getLogger(CommandHandler.class.getName());
    @Autowired
    public RespSerializer respSerializer;

    @Autowired
    public Store store;

    @Autowired
    public RedisConfig redisConfig;

    public String ping(String[] command){
        return "+PONG\r\n";
    }
    public String echo(String[] command){
        return respSerializer.serializeBulkString(command[1]);
    }
    public String set(String[] command){
        // TODO global exception handling
        try{
            String key = command[1];
            String value = command[2];

            int pxFlag = Arrays.stream(command).toList().indexOf("px");
            // -1
            if(pxFlag > -1){
                int delta = Integer.parseInt( command[ pxFlag + 1 ] );
                return store.set(key, value, delta);
            }else{
                return store.set(key, value);
            }
        }catch (Exception e){
            logger.log(Level.SEVERE, e.getMessage());
            return "$-1\r\n";
        }
    }

    public String get(String[] command){
        try{
            String key = command[1];
            return store.get(key);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            return "$-1\r\n";
        }
    }

    public String info(String[] command){
        // command[0]; info
        int replication = Arrays.stream(command).toList().indexOf("replication");
        if(replication > -1){
            return respSerializer.serializeBulkString("role:"+redisConfig.getRole());
        }
        return "";
    }

}
