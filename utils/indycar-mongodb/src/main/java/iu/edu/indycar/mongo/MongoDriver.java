package iu.edu.indycar.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        MongoCollection<Document> telemetry = database.getCollection("telemetry");
        MongoCollection<Document> drivers = database.getCollection("drivers");
        drivers.createIndex(Indexes.ascending("uid"), new IndexOptions().unique(true));

        System.out.println("Parsing file " + file.getAbsolutePath());

        FileReader fis = new FileReader(file);

        String date = file.getName().split("_")[2];

        BufferedReader br = new BufferedReader(fis);
        String line = br.readLine();

        List<Document> telemetryRecords = new ArrayList<>();
        List<Document> entryRecords = new ArrayList<>();
        while (line != null) {
            if (line.startsWith("$P")) {
                String[] splits = line.split("�");
                String carNumber = splits[1];
                String timeOfDay = splits[2];
                String lapDistance = splits[3];
                String vehicleSpeed = splits[4];
                String engineSpeed = splits[5];
                String throttle = splits[6];

                Document document = new Document();

                document.append("car_num", carNumber);
                document.append("lap_distance", lapDistance);
                document.append("time_of_day", timeOfDay);
                document.append("vehicle_speed", vehicleSpeed);
                document.append("engine_rpm", engineSpeed);
                document.append("throttle", throttle);
                document.append("date", date);

                if (telemetryRecords.size() == 100) {
                    telemetry.insertMany(telemetryRecords);
                    telemetryRecords.clear();
                } else {
                    telemetryRecords.add(document);
                }
            } else if (line.startsWith("$E")) {
                String[] splits = line.split("�");
                Document entryDoc = new Document();

                entryDoc.append("car_num", splits[4]);
                entryDoc.append("uid", splits[5]);
                entryDoc.append("name", splits[6]);
                entryDoc.append("license", splits[13]);
                entryDoc.append("team", splits[14]);
                entryDoc.append("engine", splits[16]);
                entryDoc.append("home_town", splits[19]);

                drivers.updateOne(Filters.eq("uid", splits[5]),
                        new Document("$set", entryDoc), new UpdateOptions().upsert(true));
            }
            line = br.readLine();
        }
        telemetry.insertMany(telemetryRecords);
        br.close();
    }
}
