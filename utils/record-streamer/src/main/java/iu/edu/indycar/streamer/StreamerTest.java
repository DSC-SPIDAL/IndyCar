package iu.edu.indycar.streamer;

import java.io.File;
import java.io.IOException;

public class StreamerTest {
  public static void main(String[] args) throws IOException {
    File file = new File("/home/chathura/Downloads/indy_data/IPBroadcaster_Input_2018-05-16_0.log");

    RecordStreamer recordStreamer = new RecordStreamer(file, true, 100, record -> {
      System.out.println(record.getCarNumber());
      System.out.println(record.getTimeOfDay());
      System.out.println(record.getEngineSpeed());
      System.out.println(record.getLapDistance());
      System.out.println(record.getThrottle());
      System.out.println(record.getThrottle());
    });

    recordStreamer.start();
  }
}
