package Components;

import org.springframework.stereotype.Component;

@Component
public class RespSerializer {
    public void printWokring(){
        System.out.println("-------------------------------------------------------------------------------------------");
    }
    public String returnsString(){
        return "hi";
    }
}
