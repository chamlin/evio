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

    private static Pattern codeParse = Pattern.compile ("(([A-Z]+|X509)-[A-Z]+): ");

    private static Event defaultEvent = new Event ();

    private static LocalDateTime defaultDateTime = LocalDateTime.of (1900, 1, 1, 0, 0, 0);;

    /**
    * Parse the line to an Event
    */
    public Event parse (String filename, int lineNumber, String s) {
        Event e = defaultEvent;
        Matcher timestampMatcher = timestampParse.matcher (s);
        // timestamped line, or system line
        if (timestampMatcher.matches ()) {

            String timestamp = timestampMatcher.group (1);
            LocalDateTime dateTime = LocalDateTime.parse (timestamp.replaceAll (" ", "T"));
            String level = timestampMatcher.group (2);
            String text = timestampMatcher.group (3);

            e = new Event (dateTime, Event.eventType.TIMESTAMP, level, s);
            e.setSource (filename, lineNumber);

            Matcher appserverParseMatcher = appserverParse.matcher (text);
            Matcher eventTraceMatcher = eventTraceParse.matcher (text);

            if (appserverParseMatcher.matches ()) {
                // TODO this might be overoptimistic.  can know for sure on a continuation?
                e.addValue ("appserver", appserverParseMatcher.group (1));
                String newText = appserverParseMatcher.group (2);
                e.addValue ("newText", newText);

                if (newText.matches ("^[ \\t]{2,}.*") || newText.startsWith ("in "))
                    e.setAppServerContinued (true);
            } else if (eventTraceMatcher.matches ()) {
                e.addValue ("event", eventTraceMatcher.group (1));
            }
        } else if (threadParse.matcher (s).matches ()) {
            e = new Event (defaultDateTime, Event.eventType.THREAD, s);
        } else {
            e = new Event (defaultDateTime, Event.eventType.SYSTEM, s);
        }
        return e;
    }

    public static List<String> getCodes (String text) {

        List<String> matches = new ArrayList<String> ();
        Matcher codeMatcher = codeParse.matcher (text);

        while (codeMatcher.find()) {
            matches.add ("code");
            matches.add (codeMatcher.group (1));
        }

        return matches;
    }

    public static void main (String[] args) {
        Parser p = new Parser ();
        // String line = "2016-02-11 00:16:15.167 Debug: LDAP user s060222 found in login cache";
        String line = "2015-08-24 10:29:19.891 Info: App-Services:   </error:variable>";
        Event e = p.parse ("foo", 1, line);
        System.out.println (e);
    }


}
