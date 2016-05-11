import java.util.Map;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
import com.esereno.javalog.*;

public class test {

public static void main (String[] args) throws Exception {
    Map<String,String> parts = new HashMap<String,String>();
    Parser.lineType e;

    String line;
    BufferedReader br = new BufferedReader(new FileReader("test.txt"));
    Event bufferedEvent = null;
    while ((line = br.readLine()) != null) {
        // System.out.println("---------------------------------");
        e = Parser.preparse (parts, line);
        // System.out.println (e + "\n" + line + "\n" + parts + "\n");
        // have to take care of sys lines at some time
        if (bufferedEvent == null) 
            bufferedEvent = new Event (parts);
        else if (e == Parser.lineType.APPSERVER_CONTINUE) {
            bufferedEvent.addLine (parts.get ("line"));
        } else {
            System.out.println (bufferedEvent);
            bufferedEvent = new Event (parts);
        }
        // System.out.println("---------------------------------");
    }
    System.out.println (bufferedEvent);

    
}

}



