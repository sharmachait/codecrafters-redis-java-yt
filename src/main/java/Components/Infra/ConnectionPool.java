package Components.Infra;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class ConnectionPool {
    private Queue<Client> clients;
    private Queue<Slave> slaves;

    public ConnectionPool() {
        clients = new ConcurrentLinkedQueue<>();
        slaves = new ConcurrentLinkedQueue<>();
    }

    public Queue<Client> getClients() {
        return clients;
    }

    public Queue<Slave> getSlaves() {
        return slaves;
    }

    public void addClient(Client client){
        if(client!=null)
            clients.add(client);
    }

    public void addSlave(Slave slave){
        if(slave!=null)
            slaves.add(slave);
    }

    public boolean removeClient(Client client){
        return clients.remove(client);
    }

    public boolean removeSlave(Slave slave){
        return slaves.remove(slave);
    }

    public boolean removeSlave(Client client){
        Slave slaveToRemove = null;
        for(Slave s: slaves){
            if(s.connection.equals(client)){
                slaveToRemove = s;
                break;
            }
        }

        return slaves.remove(slaveToRemove);
    }
}
