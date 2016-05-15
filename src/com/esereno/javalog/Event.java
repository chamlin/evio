package com.esereno.javalog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.time.*;

public class Event {

    public static enum lineType {
        UNKNOWN,   // anything starting with a timestamp
        TIMESTAMP,   // anything starting with a timestamp
        THREAD,      // part of a thread dump
        SYSTEM       // 
    };

    private LocalDateTime timestamp = LocalDateTime.of (1900, 1, 1, 0, 0, 0);
    private lineType type = lineType.UNKNOWN;
    private HashMap<String,ArrayList<String>> values = new HashMap<String,ArrayList<String>> ();
    private List<String> rawLines = new ArrayList<String> ();
    private String level = "Info";
    private boolean appServerContinued = false;

public Event () {
    // timestamp = LocalDateTime.parse (timestamp.replace (' ', 'T'));
}

public Event (LocalDateTime _timestamp, lineType _type) {
    timestamp = _timestamp;
    type = _type;
}

public Event (LocalDateTime _timestamp, lineType _type, String _line) {
    timestamp = _timestamp;
    type = _type;
    rawLines.add (_line);
}

public Event (LocalDateTime _timestamp, lineType _type, String _level, String _line) {
    timestamp = _timestamp;
    type = _type;
    level = _level;
    rawLines.add (_line);
}

public Event (LocalDateTime _timestamp, lineType _type, List<String> _lines, HashMap<String,ArrayList<String>> _values, String _level) {
    // timestamp = LocalDateTime.parse (timestamp.replace (' ', 'T'));
    timestamp = _timestamp;
    type = _type;
    rawLines = _lines;
    values = _values;
    level = _level;
}

public void addValue (String key, String value) {
    if (! values.containsKey (key)) {
        ArrayList<String> al = new ArrayList<String> ();
        al.add (value);
        values.put (key, al);
    } else {
        values.get (key).add (value);
    }
}

public boolean getAppServerContinued () {
    return appServerContinued;
}

public void setAppServerContinued (boolean _in) {
    appServerContinued = _in;
}

public void addLine (String line) {
    rawLines.add (line);
}

private static String createElement (String qname, String content) {
    StringBuffer sb = new StringBuffer ("<" + qname + "><![CDATA[");
    sb.append (content);
    sb.append ("]]></" + qname + ">");
    return sb.toString ();
}

public String toString () {
    StringBuffer sb = new StringBuffer ("<event xmlns='http://esereno.com/logging#event'>");
    sb.append (createElement ("timestamp", timestamp.toString ()));
    if (values.containsKey ("code"))
        for (String code : values.get ("code"))
            sb.append (createElement ("code", code));
    sb.append ("</event>");
    return sb.toString ();
}

public static void main (String[] args) {

    System.out.println (createElement ("forest", "Documents-f1"));
    
}

}


