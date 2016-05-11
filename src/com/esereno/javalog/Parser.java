package com.esereno.javalog;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.*;

public class Parser {

    public static enum lineType {
        ERROR,
        TIMESTAMP,
        APPSERVER,
        APPSERVER_CONTINUE,
        TRACE_EVENT,
        SYSTEM
    };


    private static Pattern timestampParse
        = Pattern.compile ("(\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d+) (\\S+):\\s+(.*)[\r\n]*");
    private static Pattern appserverContinueParse = Pattern.compile ("([a-zA-Z-_]+): ( in |   ).*");
    private static Pattern appserverParse = Pattern.compile ("([a-zA-Z-_]+): (.*)");
    private static Pattern eventTraceParse = Pattern.compile ("\\[Event:id=([^]]+)\\].*");

    private static Pattern codeParse = Pattern.compile ("(([A-Z]+|X509)-[A-Z]+): ");

    /**
    * Do a little work to see what type it is, before we try to go further.
    */
    public static lineType preparse (Map<String,String> results, String s) {
        lineType type = lineType.ERROR;
        results.clear ();
        Matcher timestampMatcher = timestampParse.matcher (s);
        // timestamped line, or system line
        if (timestampMatcher.matches ()) {

            String timestamp = timestampMatcher.group (1);
            String level = timestampMatcher.group (2);
            String text = timestampMatcher.group (3);

            results.put ("timestamp", timestamp);
            results.put ("level", level);
            results.put ("text", text);
            results.put ("line", s);

            Matcher appserverParseMatcher = appserverParse.matcher (text);
            Matcher eventTraceMatcher = eventTraceParse.matcher (text);

            if (appserverParseMatcher.matches ()) {
                // TODO this might be overoptimistic.  can know for sure on a continuation.
                results.put ("appserver", appserverParseMatcher.group (1));
                text = appserverParseMatcher.group (2);
                results.put ("text", text);
    
                if (text.startsWith ("  ") || text.startsWith ("in "))
                    type = lineType.APPSERVER_CONTINUE;
                else
                    type = lineType.APPSERVER;
            } else if (eventTraceMatcher.matches ()) {
                results.put ("event", eventTraceMatcher.group (1));
                type = lineType.TRACE_EVENT;
            } else {
                type = lineType.TIMESTAMP;
            }
            
        } else {
            results.put ("line", s);
            type = lineType.SYSTEM;
        }
        return type;
    }

    public static List<String> getCodes (String text) {

        List<String> matches = new ArrayList<String> ();
        Matcher codeMatcher = codeParse.matcher (text);

        while (codeMatcher.find()) {
            matches.add (codeMatcher.group (1));
        }

        return matches;
    }

    public static void main (String[] args) {
        Parser p = new Parser ();
        String line = "2016-02-11 00:16:15.167 Debug: LDAP user s060222 found in login cache";
        Map<String,String> results = new HashMap<String,String> ();
        p.preparse (results, line);
        System.out.println (results);
    }


}
