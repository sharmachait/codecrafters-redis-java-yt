package Components;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RespSerializerTest {
    @Autowired
    private RespSerializer respSerializer;
    @Test
    void returnsString() {
        String s = respSerializer.returnsString();
        assertEquals("hi",s);
    }
}