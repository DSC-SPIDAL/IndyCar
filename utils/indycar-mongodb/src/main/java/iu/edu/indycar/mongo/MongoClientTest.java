package iu.edu.indycar.mongo;

import org.bson.Document;

import java.util.List;

public class MongoClientTest {
    public static void main(String[] args) {
        IndycarDBClient indycarDBClient = new IndycarDBClient("mongodb://localhost");

        List<Document> allDrivers = indycarDBClient.drivers().getAll();
        System.out.println(allDrivers);
        //

        Document driver0 = indycarDBClient.drivers().getDriver("0");
        System.out.println(driver0);
    }
}
