package com.esereno.javalog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.time.*;

public class Event {

    public static enum eventType {
        UNKNOWN ("unknown"),
        TIMESTAMP ("timestamp"),
        THREAD ("thread"),
        SYSTEM ("system");

        private String value;

        eventType (final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return this.getValue();
        }
    }

    private LocalDateTime timestamp = LocalDateTime.of (1900, 1, 1, 0, 0, 0);
    private eventType type = eventType.UNKNOWN;
    private HashMap<String,ArrayList<String>> values = new HashMap<String,ArrayList<String>> ();
    private ArrayList<String> rawLines = new ArrayList<String> ();
    private String level = null;
    private boolean appServerContinued = false;

    private String filename = "unknown";
    private int linenumber = 0;

public Event () {
    // timestamp = LocalDateTime.parse (timestamp.replace (' ', 'T'));
}

public Event (LocalDateTime _timestamp, eventType _type) {
    timestamp = _timestamp;
    type = _type;
}

public Event (LocalDateTime _timestamp, eventType _type, String _line) {
    timestamp = _timestamp;
    type = _type;
    rawLines.add (_line);
}

public Event (LocalDateTime _timestamp, eventType _type, String _level, String _line) {
    timestamp = _timestamp;
    type = _type;
    level = _level;
    rawLines.add (_line);
}

public Event (LocalDateTime _timestamp, eventType _type, ArrayList<String> _lines, HashMap<String,ArrayList<String>> _values, String _level) {
    // timestamp = LocalDateTime.parse (timestamp.replace (' ', 'T'));
    timestamp = _timestamp;
    type = _type;
    rawLines = _lines;
    values = _values;
    level = _level;
}

public eventType getEventType () { return type; }

public void setSource (String _file, int _linenumber) {
    filename = _file;
    linenumber = _linenumber;
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

public ArrayList<String> getLines () {
    return rawLines;
}

public void mergeLines (Event e) {
    rawLines.addAll (e.getLines ());
}

private static String createElement (String qname, String content) {
    // level is default null, for example.
    if (qname == null || content == null)  return "";

    StringBuffer sb = new StringBuffer ("<" + qname + "><![CDATA[");
    sb.append (content);
    sb.append ("]]></" + qname + ">");
    return sb.toString ();
}

public String toString () {
    StringBuffer sb = new StringBuffer ("<event xmlns='http://esereno.com/logging/event'>");
    sb.append (createElement ("filename", filename));
    sb.append (createElement ("linenumber", String.valueOf (linenumber)));
    sb.append (createElement ("timestamp", timestamp.toString ()));
    sb.append (createElement ("type", type.toString ()));
    sb.append (createElement ("level", level));
    if (! values.containsKey ("count"))  sb.append (createElement ("count", "1"));
    for (String key : values.keySet ()) {
        for (String value : values.get (key))
            sb.append (createElement (key, value));
    }
    StringBuffer lines = new StringBuffer ();
    for (String line : rawLines) {
        lines.append (line);
    }
    sb.append (createElement ("lines", lines.toString ()));
    sb.append ("</event>");
    return sb.toString ();
}

public static void main (String[] args) {

    System.out.println (createElement ("forest", "Documents-f1"));
    
}

}


