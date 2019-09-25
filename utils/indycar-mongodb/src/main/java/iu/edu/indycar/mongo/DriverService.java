package iu.edu.indycar.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class DriverService {

    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> drivers;

    public DriverService(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
        this.drivers = mongoDatabase.getCollection("drivers");
    }

    public List<Document> getAll() {
        List<Document> drivers = new ArrayList<>();
        //taking uids only
        this.drivers.find().projection(new Document("uid", 1).append("_id", 0)).into(drivers);
        return drivers;
    }

    public Document getDriver(String uid) {
        return this.drivers.find(new Document("uid", uid)).first();
    }
}
