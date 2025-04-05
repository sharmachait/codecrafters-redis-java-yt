package Components.Server;


import org.springframework.stereotype.Component;

@Component
public class RedisConfig {
    public String getRole() {
        return role;
    }

    public int getPort() {
        return port;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String role;
    public int port;

}
