package Components.Server;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class RedisConfig {
    public String role;
    public int port;
}
