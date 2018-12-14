package iu.edu.indycar.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MongoDriver {

    public static void main(String[] args) {
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase database = mongoClient.getDatabase("indycar");
        MongoCollection<Document> collection = database.getCollection("telemetry");

        getLogsList("/media/chathura/DATA/Code/indy-data/indy500/").forEach(file -> {
            try {
                writeToDB(file, collection);
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

    public static void writeToDB(File file, MongoCollection<Document> collection) throws IOException {
        System.out.println("Parsing file " + file.getAbsolutePath());

        FileReader fis = new FileReader(file);

        String date = file.getName().split("_")[2];

        BufferedReader br = new BufferedReader(fis);
        String line = br.readLine();

        List<Document> docs = new ArrayList<>();
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

                if (docs.size() == 100) {
                    collection.insertMany(docs);
                    docs.clear();
                } else {
                    docs.add(document);
                }
            }
            line = br.readLine();
        }
        collection.insertMany(docs);
        br.close();
    }
}
