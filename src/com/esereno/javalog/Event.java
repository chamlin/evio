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
    private List<String> codes = new ArrayList<String> ();
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

public Event (LocalDateTime _timestamp, lineType _type, List<String> _lines, List<String> _codes, String _level) {
    // timestamp = LocalDateTime.parse (timestamp.replace (' ', 'T'));
    timestamp = _timestamp;
    type = _type;
    rawLines = _lines;
    codes = _codes;
    level = _level;
}

public void addCode (String code, String value) {
    codes.add (code);  codes.add (value);
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

public String toString () {
    return 
        timestamp.toString () + "\n" +
        rawLines + "\n" +
        codes
    ;
}

public static void main (String[] args) {

    System.out.println (new Event ());
    
}

}


