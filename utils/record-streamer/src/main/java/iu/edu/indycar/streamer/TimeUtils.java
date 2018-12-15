package iu.edu.indycar.streamer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {

  public static String TIME_PATTERN_STR = "(\\d+):(\\d+):(\\d+).(\\d+)";

  public static final Pattern TIME_PATTERN = Pattern.compile(TIME_PATTERN_STR);

  private static long MILLIS_PER_SECOND = 1000;
  private static long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
  private static long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;

  public static long convertTimestampToLong(String timeStamp) {
    Matcher matcher = TIME_PATTERN.matcher(timeStamp);

    if (matcher.find()) {
      int hours = Integer.parseInt(matcher.group(1));
      int minutes = Integer.parseInt(matcher.group(2));
      int seconds = Integer.parseInt(matcher.group(3));
      int milis = Integer.parseInt(matcher.group(4));

      return (hours * MILLIS_PER_HOUR) + (minutes * MILLIS_PER_MINUTE) + (seconds * MILLIS_PER_SECOND) + milis;
    } else {
      throw new RuntimeException("Unexpected date pattern in " + timeStamp);
    }
  }
}
