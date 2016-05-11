import java.time.LocalDateTime;
import com.esereno.javalog.*;

/*
public static LocalDateTime of(int year,
                               int month,
                               int dayOfMonth,
                               int hour,
                               int minute,
                               int second)
*/

public class foo {

public static void main (String[] args) throws Exception {

    String text = "2015-04-10T18:57:44.169";
    int year = Integer.parseInt (text.substring (0, 4));
    int month = Integer.parseInt (text.substring (5, 7));
    int day = Integer.parseInt (text.substring (8, 10));
    int hour = Integer.parseInt (text.substring (11, 13));
    int minute = Integer.parseInt (text.substring (14, 16));
    int second = Integer.parseInt (text.substring (17, 19));
    int nanosecond = Integer.parseInt (text.substring (20) + "000000");



    System.out.println ("year :" + year);
    System.out.println ("month :" + month);
    System.out.println ("day :" + day);

    System.out.println ("hour :" + hour);
    System.out.println ("minute :" + minute);
    System.out.println ("sec :" + second);

    System.out.println ("nanosec :" + nanosecond);

    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse (text);

    System.out.println (ldt);

}



}

