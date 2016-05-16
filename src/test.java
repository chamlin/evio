import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import com.esereno.javalog.*;

public class test {

public static void main (String[] args) throws Exception {

    Multiinsert inserter = new Multiinsert ("localhost", 8000, "Documents", "admin", "admin");

    // String filename = "ErrorLog.txt";
    String filename = "2XDMP.txt";

    String line;
    Parser p = new Parser ();
    BufferedReader br = new BufferedReader(new FileReader(filename));
    ArrayList<Event> bufferedEvents = new ArrayList<Event>();
    Event bufferedEvent = null;
    ArrayList<Event> threadEvent = null;
    int linenumber = 0;
    while ((line = br.readLine()) != null) {
        Event e = p.parse (filename, ++linenumber, line);

        if (bufferedEvent == null) {
            bufferedEvent = e;
            continue;
        }
        switch (e.getEventType ()) {
            case TIMESTAMP:
                if (e.getAppServerContinued ()) {
                    bufferedEvent.mergeLines (e);
                    bufferedEvent.setAppServerContinued (true);
                }
                else {
                    if (bufferedEvent.getAppServerContinued ())  bufferedEvent.addValue ("continued", "true");
                    inserter.insertDoc (bufferedEvent.toString ());
                    bufferedEvent = e;
                }
                break;
            default:
                break;
        }
    }

    inserter.insertDoc (bufferedEvent.toString ());

    inserter.shutdown ();
    

}

}



