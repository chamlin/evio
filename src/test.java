import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import com.esereno.javalog.*;

public class test {

public static String[] eventStrings (ArrayList<Event> events) {
    String[] docStrings = new String[events.size()];
    return docStrings;
    
}

public static void main (String[] args) throws Exception {

    Multiinsert inserter = new Multiinsert ("localhost", 8000, "Documents", "admin", "admin");

    //String filename = "ErrorLog.txt";
    // String filename = "2XDMP.txt";
    // String filename = "foo.txt";
    String filename = "m13p_ErrorLog.txt";
    ArrayList<Event> eventArray = new ArrayList<Event>(15);

    String line;
    Parser p = new Parser ();
    BufferedReader br = new BufferedReader(new FileReader(filename));
    int events = 0;
    int linenumber = 0;
    while ((line = br.readLine()) != null) {

        if (eventArray.size () > 10)  {
            
            inserter.insertDocs (eventArray);
            eventArray = new ArrayList<Event>(15);
        }

        Event e = p.parse (filename, ++linenumber, line);

        if (eventArray.size () == 0) {
            eventArray.add (e);
            continue;
        }

        Event bufferedEvent = eventArray.get (eventArray.size () - 1);
        switch (e.getEventType ()) {
            case TIMESTAMP:
                if (e.getAppServerContinued ()) {
                    
                    bufferedEvent.mergeLines (e);
                    bufferedEvent.setAppServerContinued (true);
                }
                else {
                    if (bufferedEvent.getAppServerContinued ())  bufferedEvent.addValue ("continued", "true");
                    eventArray.add (e);
                    // inserter.insertDoc (bufferedEvent.toString ());
                    // bufferedEvent = e;
                }
                break;
            default:
                // huh?
                break;
        }
    }

    inserter.insertDocs (eventArray);

    inserter.shutdown ();

    System.out.println ("Inserted " + inserter.documentsInserted () + ".");
    

}

}



