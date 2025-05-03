package Components.Repository;

import Components.Infra.Client;
import Components.Service.RespSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class Store {
    private static final Logger logger = Logger.getLogger(Store.class.getName());

    public ConcurrentHashMap<String, Value> map;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    @Autowired
    public RespSerializer respSerializer;
    public Store(){
        map = new ConcurrentHashMap<>();
    }

    public Set<String> getKeys(){
        rwLock.readLock().lock();
        try{
            return map.keySet();
        } finally{
            rwLock.readLock().unlock();
        }

    }

    public String set(String key, String val){
        rwLock.writeLock().lock();
        try{
            Value value = new Value(val, LocalDateTime.now(), LocalDateTime.MAX);
            map.put(key, value);
            return "+OK\r\n";
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            return "$-1\r\n";
        } finally{
            rwLock.writeLock().unlock();
        }
    }

    public String set(String key, String val, int expiryMilliseconds){
        rwLock.writeLock().lock();
        try{
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime exp = now.plus(expiryMilliseconds, ChronoUnit.MILLIS);
            Value value = new Value(val, now, exp);
            map.put(key, value);
            return "+OK\r\n";
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            return "$-1\r\n";
        } finally{
            rwLock.writeLock().unlock();
        }
    }

//    public ReentrantReadWriteLock acquireLock(){
//        rwLock.writeLock().lock();
//        return rwLock;
//    }

    public String get(String key){
        rwLock.readLock().lock();
        try{
            LocalDateTime now = LocalDateTime.now();
            Value value = map.get(key);

            if(value!=null && value.expiry.isBefore(now)){
                map.remove(key);
                return "$-1\r\n";
            }
            return respSerializer.serializeBulkString(value.val);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            return "$-1\r\n";
        } finally{
            rwLock.readLock().unlock();
        }
    }

    public Value getValue(String key){
        rwLock.readLock().lock();
        try{
            LocalDateTime now = LocalDateTime.now();
            Value value = map.get(key);

            if(value!=null && value.expiry.isBefore(now)){
                map.remove(key);
                return null;
            }
            return value;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            return null;
        } finally{
            rwLock.readLock().unlock();
        }
    }

    public void executeTransaction(
            Client client,
            BiFunction<String[], Map<String, Value>, String> commandApplier
    ) {
        rwLock.writeLock().lock();

        try {

            Map<String, Value> transactionalMap = new HashMap<>();
            List<String> results = new ArrayList<>();

            while(client.commandQueue!=null && !client.commandQueue.isEmpty()){

                String[] command = client.commandQueue.poll();
                String result = commandApplier.apply(command, transactionalMap);

                results.add(result);
            }

            for (Map.Entry<String, Value> entry : transactionalMap.entrySet()) {
                String key = entry.getKey();
                Value value = entry.getValue();

                if (value.isDeletedInTransaction) {
                    map.remove(key);
                } else {
                    map.put(key, value);
                }
            }
            client.transactionResponse = results;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

}























