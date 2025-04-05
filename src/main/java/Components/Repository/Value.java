package Components.Repository;

import java.time.LocalDateTime;

public class Value {
    public String val;
    public LocalDateTime created;
    public LocalDateTime expiry;

    public Value( String val, LocalDateTime created, LocalDateTime expiry) {
        this.created = created;
        this.val = val;
        this.expiry = expiry;
    }

}
