import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import com.esereno.javalog.*;

public class test {

public static Properties getProperties (String filename) {

    Properties prop = new Properties ();
    InputStream input = null;
    
    try {
        input = test.class.getClassLoader().getResourceAsStream(filename);
        if (input == null) {
            System.out.println("Sorry, unable to find " + filename);
        } else {
            //load a properties file from class path, inside static method
            prop.load(input);
        }
    } catch (IOException ex) {
        ex.printStackTrace();
    } finally {
        if (input != null) { try { input.close(); } catch (IOException e) { } }
    }

    return prop;
}

public static void main (String[] args) throws Exception {


    Properties prop = getProperties ("jalopar.config");
    if (prop.getProperty ("hostname") == null) {
        System.out.println ("no host!?");
        System.out.println ("props: " + prop);
        return;
    }

    String hostname = prop.getProperty ("hostname");
    int port = Integer.valueOf (prop.getProperty ("port"));
    String database = prop.getProperty ("database");
    String username = prop.getProperty ("username");
    String password = prop.getProperty ("password");
    String batchSizeString = prop.getProperty ("batchsize");
    int batchSize = batchSizeString == null ? 10 : Integer.valueOf (batchSizeString);
    String poolSizeString = prop.getProperty ("poolsize");
    int poolSize = poolSizeString == null ? 10 : Integer.valueOf (poolSizeString);

    Multiinsert inserter = new Multiinsert (hostname, port, "Documents", "admin", "admin", poolSize);

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

        if (eventArray.size () >= batchSize)  {
            
            inserter.insertDocs (eventArray);
            eventArray = new ArrayList<Event>(batchSize+1);
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



