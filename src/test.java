import java.util.regex.Pattern;
import java.util.regex.Matcher;
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

    // probably should check more
    if (prop.getProperty ("hostname") == null) {
        System.out.println ("no host!?");
        System.out.println ("props: " + prop);
        return;
    }

    if (args.length == 0)  {
        System.out.println ("no input files specified");
        return;
    }

    String hostname = prop.getProperty ("hostname");
    int port = Integer.valueOf (prop.getProperty ("port"));
    String database = prop.getProperty ("database");
    String username = prop.getProperty ("username");
    String password = prop.getProperty ("password");
    String batchSizeString = prop.getProperty ("batchsize");
    int batchSize = Integer.valueOf (batchSizeString);
    String poolSizeString = prop.getProperty ("poolsize");
    int poolSize = Integer.valueOf (poolSizeString);
    String keyName = prop.getProperty ("keyname");
    if (keyName == null)  keyName = "node";

    // set up filename => key mapping (usually node)
    String keyValue = prop.getProperty ("keyvalue");
    Pattern keyPattern = null;
    try { keyPattern = Pattern.compile (prop.getProperty ("keymatch")); } catch (Exception e) { }

    HashMap<String,String> fileKeys = new HashMap<String,String> (args.length);
    for (String filename: args) {
        String key = filename;
        if (keyValue != null)  key = keyValue;
        else if (keyPattern != null) {
            Matcher keyMatcher = keyPattern.matcher (filename);
            if (keyMatcher.find ())  key = keyMatcher.group (1);
        }
        fileKeys.put (filename, key);
    }

    Multiinsert inserter = new Multiinsert (hostname, port, database, username, password, poolSize);

    int totalInserted = 0;
    
    for (String filename: fileKeys.keySet ()) {
        ArrayList<Event> eventArray = new ArrayList<Event>(15);

        String key = fileKeys.get (filename);

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
            e.addValue ("node", key);

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
                    }
                    break;
                default:
                    // huh?
                    break;
            }
        }

        inserter.insertDocs (eventArray);
        totalInserted += inserter.documentsInserted ();
        System.out.println ("Inserted " + inserter.documentsInserted () + " for " + filename + ".");
    }

    inserter.shutdown ();

    System.out.println ("Total inserted " + totalInserted + ".");


    

}

}



