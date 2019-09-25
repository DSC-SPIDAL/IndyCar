package iu.edu.indycar.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.DeleteResult;
import iu.edu.indycar.streamer.RecordStreamer;
import iu.edu.indycar.streamer.TimeUtils;
import iu.edu.indycar.streamer.records.TelemetryRecord;
import iu.edu.indycar.streamer.records.policy.AbstractRecordAcceptPolicy;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MongoDriver {

  public static void main(String[] args) {
    MongoClient mongoClient = MongoClients.create(new ConnectionString("mongodb://localhost"));
    MongoDatabase database = mongoClient.getDatabase("indycar");

    getLogsList("/home/chathura/Downloads/indy_data/").forEach(file -> {
      try {
        writeToDB(file, database);
      } catch (IOException e) {
        System.out.println("Error in witting data of file : " + file.getAbsolutePath());
      }
    });
  }

  public static List<File> getLogsList(String folderPath) {
    File folder = new File(folderPath);
    File[] listOfFiles = folder.listFiles();

    if (listOfFiles == null) {
      return Collections.emptyList();
    }

    return Arrays.stream(listOfFiles)
            .filter(file -> file.getName().matches("IPBroadcaster_Input_\\d{4}-\\d{2}-\\d{2}_\\d+.log")).collect(Collectors.toList());
  }

  public static void writeToDB(File file, MongoDatabase database) throws IOException {
    final String fileName = file.getName();
    if (!"IPBroadcaster_Input_2018-05-27_0.log".equals(fileName)) {
      return;
    }
    final Document fileNameDoc = new Document("file", fileName);

    MongoCollection<Document> telemetry = database.getCollection("telemetry");
    MongoCollection<Document> cars = database.getCollection("cars");
    MongoCollection<Document> laps = database.getCollection("laps");

    // create indexes
    laps.createIndex(Indexes.ascending("car_number"));

    // delete existing records for this file
    System.out.println("Lap Records Deleted : " + laps.deleteMany(fileNameDoc).getDeletedCount());
    System.out.println("Cars Deleted : " + cars.deleteMany(fileNameDoc).getDeletedCount());
    System.out.println("Telemetry Deleted : " + telemetry.deleteMany(fileNameDoc).getDeletedCount());

    RecordStreamer recordStreamer = new RecordStreamer(
            file, false, 10000000, s -> s.split("_")[2]);

    // subscribing to records
    recordStreamer.setCompleteLapRecordRecordListener(completeLapRecord -> {
      Document document = new Document("file", fileName);
      document.append("car_number", completeLapRecord.getCarNumber());
      document.append("completed_laps", completeLapRecord.getCompletedLaps());
      document.append("elapsed_time", completeLapRecord.getElapsedTime());
      document.append("last_lap_time", completeLapRecord.getTime());
      document.append("lap_status", completeLapRecord.getLapStatus());
      document.append("fastest_lap", completeLapRecord.getFastestLap());
      document.append("fastest_lap_time", completeLapRecord.getFastestLapTime());
      document.append("laps_behind_leader", completeLapRecord.getLapsBehindLeader());
      document.append("time_behind_leader", completeLapRecord.getTimeBehindLeader());
      document.append("laps_behind_preceding_car", completeLapRecord.getLapsBehindPrec());
      document.append("time_behind_preceding_car", completeLapRecord.getTimeBehindPrec());
      document.append("laps_led", completeLapRecord.getLapsLed());
      document.append("last_pitted_lap", completeLapRecord.getLastPittedLap());
      document.append("overall_best_lap_time", completeLapRecord.getOverallBestLapTime());
      document.append("overall_rank", completeLapRecord.getOverallRank());
      document.append("track_status", completeLapRecord.getTrackStatus());
      document.append("pitstops_count", completeLapRecord.getPitStopsCount());
      document.append("start_position", completeLapRecord.getStartPosition());
      document.append("rank", completeLapRecord.getRank());
      laps.insertOne(document);
    });

    recordStreamer.setEntryRecordRecordListener(entryRecord -> {
      if (cars.countDocuments(new Document("uid", entryRecord.getUid())) == 0) {
        Document document = new Document("file", fileName);
        document.append("car_number", entryRecord.getCarNumber());
        document.append("driver_name", entryRecord.getDriverName());
        document.append("engine", entryRecord.getEngine());
        document.append("hometown", entryRecord.getHometown());
        document.append("license", entryRecord.getLicense());
        document.append("team", entryRecord.getTeam());
        document.append("uid", entryRecord.getUid());
        cars.insertOne(document);
      }
    });

    recordStreamer.setTelemetryRecordListener(telemetryRecord -> {
      Document document = new Document("file", fileName);
      document.append("car_number", telemetryRecord.getCarNumber());
      document.append("time_of_day_long", telemetryRecord.getTimeOfDayLong());
      document.append("time_of_day", telemetryRecord.getTimeOfDay());
      document.append("engine_speed", telemetryRecord.getEngineSpeed());
      document.append("lap_distance", telemetryRecord.getLapDistance());
      document.append("throttle", telemetryRecord.getThrottle());
      document.append("vehicle_speed", telemetryRecord.getVehicleSpeed());
      telemetry.insertOne(document);
    });

    // end of subscribing

    final long startTime = TimeUtils.convertTimestampToLong("16:23:00.000");

    recordStreamer.addRecordAcceptPolicy(TelemetryRecord.class,
            new AbstractRecordAcceptPolicy<TelemetryRecord>() {

              HashMap<String, Boolean> metFirstNonZero = new HashMap<>();

              @Override
              public boolean evaluate(TelemetryRecord record) {
                if (metFirstNonZero.containsKey(record.getCarNumber())) {
                  return true;
                } else if (record.getTimeOfDayLong() > startTime) {
                  metFirstNonZero.put(record.getCarNumber(), true);
                  return true;
                }
                return false;
              }
            });

    recordStreamer.start();

//    System.out.println("Parsing file " + file.getAbsolutePath());
//
//    FileReader fis = new FileReader(file);
//
//    String date = file.getName().split("_")[2];
//
//    BufferedReader br = new BufferedReader(fis);
//    String line = br.readLine();
//
//    List<Document> telemetryRecords = new ArrayList<>();
//    List<Document> entryRecords = new ArrayList<>();
//    while (line != null) {
//      if (line.startsWith("$P")) {
//        String[] splits = line.split("�");
//        String carNumber = splits[1];
//        String timeOfDay = splits[2];
//        String lapDistance = splits[3];
//        String vehicleSpeed = splits[4];
//        String engineSpeed = splits[5];
//        String throttle = splits[6];
//
//        Document document = new Document();
//
//        document.append("car_num", carNumber);
//        document.append("lap_distance", lapDistance);
//        document.append("time_of_day", timeOfDay);
//        document.append("vehicle_speed", vehicleSpeed);
//        document.append("engine_rpm", engineSpeed);
//        document.append("throttle", throttle);
//        document.append("date", date);
//
//        if (telemetryRecords.size() == 100) {
//          telemetry.insertMany(telemetryRecords);
//          telemetryRecords.clear();
//        } else {
//          telemetryRecords.add(document);
//        }
//      } else if (line.startsWith("$E")) {
//        String[] splits = line.split("�");
//        Document entryDoc = new Document();
//
//        entryDoc.append("car_num", splits[4]);
//        entryDoc.append("uid", splits[5]);
//        entryDoc.append("name", splits[6]);
//        entryDoc.append("license", splits[13]);
//        entryDoc.append("team", splits[14]);
//        entryDoc.append("engine", splits[16]);
//        entryDoc.append("home_town", splits[19]);
//
//        cars.updateOne(Filters.eq("uid", splits[5]),
//                new Document("$set", entryDoc), new UpdateOptions().upsert(true));
//      }
//      line = br.readLine();
//    }
//    telemetry.insertMany(telemetryRecords);
//    br.close();
  }
}
