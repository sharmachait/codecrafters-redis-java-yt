package Components.Repository;

import Components.Service.RespSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class Store {
    public ConcurrentHashMap<String, Value> map;
    @Autowired
    public RespSerializer respSerializer;
    public Store(){
        map = new ConcurrentHashMap<>();
    }

    public Set<String> getKeys(){
        return map.keySet();
    }

    public String set(String key, String val){
        try{
            Value value = new Value(val, LocalDateTime.now(), LocalDateTime.MAX);
            map.put(key, value);
            return "+OK\r\n";
        } catch (Exception e) {
            log.error(e.getMessage());
            return "$-1\r\n";
        }
    }

    public String set(String key, String val, int expiryMilliseconds){
        try{
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime exp = now.plus(expiryMilliseconds, ChronoUnit.MILLIS);
            Value value = new Value(val, now, exp);
            map.put(key, value);
            return "+OK\r\n";
        } catch (Exception e) {
            log.error(e.getMessage());
            return "$-1\r\n";
        }
    }

    public String get(String key){
        try{
            LocalDateTime now = LocalDateTime.now();
            Value value = map.get(key);

            if(value.expiry.isBefore(now)){
                map.remove(key);
                return "$-1\r\n";
            }
            return respSerializer.serializeBulkString(value.val);
        } catch (Exception e) {
            log.error(e.getMessage());
            return "$-1\r\n";
        }
    }
}
