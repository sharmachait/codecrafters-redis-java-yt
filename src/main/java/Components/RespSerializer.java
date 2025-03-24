package Components;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class RespSerializer {
    public int getParts(char []dataArr, int i, String[] subArray){
        int j=0;
        while(i< dataArr.length && j < subArray.length){
            if(dataArr[i] == '$'){
                //bulk String
                //$<length>\r\n<data>\r\n
                i++;
                String partLength = "";
                while(i < dataArr.length && Character.isDigit(dataArr[i])){
                    partLength += dataArr[i];
                    i++;
                }
                i+=2;
                String part = "";
                for(int k=0; k<Integer.parseInt(partLength);k++){
                    part+=dataArr[i++];
                }
                i+=2;
                subArray[j++]=part;
            }
        }
        return i;
    }

    public List<String[]> deseralize(byte[] command){
        try{
            String data = new String(command, StandardCharsets.UTF_8);
            char[] dataArr = data.toCharArray();
            List<String[]> res = new ArrayList<>();

            int i=0;
            while(i < dataArr.length){

                char curr = dataArr[i];

                if(curr=='\u0000'){
                    System.out.println("=================================================================================");
                    System.out.println(i);
                    System.out.println(curr);
                    System.out.println(dataArr.length);
                    break;
                }

                if(curr == '*'){
                    //array
                    String arrLen = "";
                    i++;
                    while(i < dataArr.length && Character.isDigit(dataArr[i])){
                        arrLen += dataArr[i++];
                    }
                    i+=2;
                    if(dataArr[i] == '*'){
                        // *2
                        // *3\r\n#3set\r\n#3key\r\n#5value
                        // *3\r\n#3set\r\n#3key\r\n#5value
                        for(int t=0;t<Integer.parseInt(arrLen);t++){
                            String nestedLen = "";
                            i++;
                            char c = dataArr[i];
                            while(i < dataArr.length && Character.isDigit(dataArr[i])){

                                nestedLen += dataArr[i++];
                            }
                            i+=2;
                            String[] subArray = new String[Integer.parseInt(nestedLen)];
                            i = getParts(dataArr, i, subArray);
                            res.add(subArray);
                        }
                    }else{
                        // *3\r\n#3set\r\n#3key\r\n#5value
                        String[] subArray = new String[Integer.parseInt(arrLen)];
                        i = getParts(dataArr, i, subArray);
                        res.add(subArray);
                    }
                }
            }
            return res;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ArrayList<>();
    }
}
