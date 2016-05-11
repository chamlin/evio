package com.esereno.javalog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Event {

    private LocalDateTime timestamp = null;
    private String traceEvent = null;
    private List<String> rawLines = new ArrayList<String> ();
    private List<String> codes = new ArrayList<String> ();


public Event (Map<String,String> values) {
    String line = values.get ("line");
    timestamp =  LocalDateTime.parse (values.get ("timestamp").replace (' ', 'T'));
    rawLines.add (line);
    codes.addAll (Parser.getCodes (line));
    if (values.contains ("event"))  traceEvent = values.get ("event");
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
    
}

}


