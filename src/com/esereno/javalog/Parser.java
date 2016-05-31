package com.esereno.javalog;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.*;
import java.time.LocalDateTime;


public class Parser {

    private static Pattern timestampParse
        = Pattern.compile ("(\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d+) (\\S+):\\s+(.*)[\r\n]*");
    private static Pattern appserverContinueParse = Pattern.compile ("([a-zA-Z-_]+): ( in |   ).*");
    private static Pattern appserverParse = Pattern.compile ("([a-zA-Z-_]+):( .*)");
    private static Pattern threadParse = Pattern.compile ("^(Thread \\d|#\\d)");
    private static Pattern eventTraceParse = Pattern.compile ("\\[Event:id=([^]]+)\\].*");

    private static Pattern codeParse = Pattern.compile ("(([A-Z]{2,}|X509)-[A-Z]{2,}): ");
    private static Pattern localMountParse = Pattern.compile ("^Mounted forest (\\S+) locally.*");
    private static Pattern unmountParse = Pattern.compile ("^Unmounted forest (\\S+)");
    private static Pattern savedParse = Pattern.compile ("^Saved (\\d+) MB in \\d+ sec at (\\d+) MB/sec to .*/([^/]+)/([^/]+)$");
    private static Pattern mergedParse = Pattern.compile ("^Merged (\\d+) MB in \\d+ sec at (\\d+) MB/sec to .*/([^/]+)/([^/]+)$");
    private static Pattern deletedParse = Pattern.compile ("^Deleted (\\d+) MB in \\d+ sec at (\\d+) MB/sec .*/([^/]+)/([^/]+)$");
    private static Pattern mergingParse = Pattern.compile ("^Merging (\\d+) MB from (.+) to ([^, ]+).*");




    private static Event defaultEvent = new Event ();

    private static LocalDateTime defaultDateTime = LocalDateTime.of (1900, 1, 1, 0, 0, 0);;

    /**
    * Parse the line to an Event
    */
    public Event parse (String filename, int lineNumber, String s) {
        Event e = defaultEvent;
        Matcher timestampMatcher = timestampParse.matcher (s);
        if (timestampMatcher.matches ()) {
            // timestamped line
            String timestamp = timestampMatcher.group (1).replaceAll (" ", "T");
            LocalDateTime dateTime = LocalDateTime.parse (timestamp);
            String level = timestampMatcher.group (2);
            String text = timestampMatcher.group (3);

            e = new Event (dateTime, Event.eventType.TIMESTAMP, level, s);
            e.setSource (filename, lineNumber);
            addCodes (e, s);

            Matcher appserverParseMatcher = appserverParse.matcher (text);
            Matcher eventTraceMatcher = eventTraceParse.matcher (text);
            Matcher localMountMatcher = localMountParse.matcher (text);
            Matcher savedMatcher = savedParse.matcher (text);
            Matcher mergedMatcher = mergedParse.matcher (text);
            Matcher unmountMatcher = unmountParse.matcher (text);
            Matcher deletedMatcher = deletedParse.matcher (text);
            Matcher mergingMatcher = mergingParse.matcher (text);

            if (appserverParseMatcher.matches ()) {
                // TODO this might be overoptimistic.  can know for sure on a continuation?
                e.addValue ("appserver", appserverParseMatcher.group (1));
                String newText = appserverParseMatcher.group (2);
                e.addValue ("newText", newText);

                if (newText.matches ("^[ \\t]{2,}.*") || newText.startsWith ("in "))
                    e.setAppServerContinued (true);
            } else if (localMountMatcher.matches ()) {
                e.addValue ("forest", localMountMatcher.group (1));
                e.addValue ("name", "mount");
            } else if (unmountMatcher.matches ()) {
                e.addValue ("forest", unmountMatcher.group (1));
                e.addValue ("name", "unmount");
            } else if (savedMatcher.matches ()) {
                e.addValue ("name", "saved");
                e.addValue ("value", savedMatcher.group (1));
                e.addValue ("rate", savedMatcher.group (2));
                e.addValue ("forest", savedMatcher.group (3));
                e.addValue ("stand", savedMatcher.group (3) + "/" + savedMatcher.group (4));
            } else if (deletedMatcher.matches ()) {
                e.addValue ("name", "deleted");
                e.addValue ("value", deletedMatcher.group (1));
                e.addValue ("rate", deletedMatcher.group (2));
                e.addValue ("forest", deletedMatcher.group (3));
                e.addValue ("stand", deletedMatcher.group (3) + "/" + deletedMatcher.group (4));
            } else if (mergedMatcher.matches ()) {
                e.addValue ("name", "merged");
                e.addValue ("value", mergedMatcher.group (1));
                e.addValue ("rate", mergedMatcher.group (2));
                e.addValue ("forest", mergedMatcher.group (3));
                e.addValue ("stand", mergedMatcher.group (3) + "/" + mergedMatcher.group (4));
            } else if (mergingMatcher.matches ()) {
// TODO add timestamp as optional (when did it come in?)
                e.addValue ("name", "merging");
                e.addValue ("value", mergingMatcher.group (1));
                String[] oldStands = mergingMatcher.group (2).split ("( and |, and |, )");
                for (String stand: oldStands)  e.addValue ("stand", stand);
                e.addValue ("stand", mergingMatcher.group (3));
            } else if (text.startsWith ("New configuration state retrieved from foreign cluster")) {
                e.addValue ("name", "config-retrieved");
            } else if (eventTraceMatcher.matches ()) {
                // should use the trace?
                e.addValue ("name", "trace");
                e.addValue ("trace", eventTraceMatcher.group (1));
            }
        } else if (threadParse.matcher (s).matches ()) {
            e = new Event (defaultDateTime, Event.eventType.THREAD, s);
        } else {
            e = new Event (defaultDateTime, Event.eventType.SYSTEM, s);
        }
        return e;
    }

    public static void addCodes (Event e, String text) {

        List<String> matches = new ArrayList<String> ();
        Matcher codeMatcher = codeParse.matcher (text);
        while (codeMatcher.find())    e.addValue ("code", codeMatcher.group (1));
    }

    public static void main (String[] args) {
        Parser p = new Parser ();
        // String line = "2016-02-11 00:16:15.167 Debug: LDAP user s060222 found in login cache";
        String line = "2015-08-24 10:29:19.891 Info: App-Services:   </error:variable>";
        Event e = p.parse ("foo", 1, line);
        System.out.println (e);
    }


}
