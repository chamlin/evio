import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import com.esereno.javalog.*;

public class test {

public static void main (String[] args) throws Exception {

    String filename = "ErrorLog.txt";

    String line;
    Parser p = new Parser ();
    BufferedReader br = new BufferedReader(new FileReader(filename));
    ArrayList<Event> bufferedEvents = new ArrayList<Event>();
    ArrayList<Event> threadEvent = null;
    int linenumber = 0;
    while ((line = br.readLine()) != null) {
        Event e = p.parse (filename, linenumber++, line);
        if (bufferedEvents.isEmpty ()) {
            bufferedEvents.add (e);
            continue;
        }
        switch (e.getEventType ()) {
            case TIMESTAMP:
                if (e.getAppServerContinued ())
                    (bufferedEvents.get (bufferedEvents.size() - 1)).mergeLines (e);
                else
                    bufferedEvents.add (e);
                break;
            default:
                break;
        }
        System.out.println("---------------------------------");
        System.out.println(e);
        System.out.println("---------------------------------");
    }
    
    for (Event e : bufferedEvents) {
        // System.out.println("---------------------------------");
        // System.out.println (e);
        // System.out.println("---------------------------------");
    }

}

}



