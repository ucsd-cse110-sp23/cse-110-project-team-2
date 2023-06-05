import static com.mongodb.client.model.Filters.eq;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static java.util.Arrays.asList;

import java.nio.file.attribute.UserPrincipalNotFoundException;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriterSettings;


import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;

public class MongoHandler {

    private final String connectionUri = "mongodb+srv://sayit:team2@sayit.ach3d95.mongodb.net/?retryWrites=true&w=majority";
    private MongoClient mongoClient;
    private MongoDatabase db; 
    private MongoCollection<Document> userInfoCollection;
    private MongoCollection<Document> promptCollection;

    public MongoHandler(){
        mongoClient = MongoClients.create(connectionUri);
        db = mongoClient.getDatabase("SayIt");

        userInfoCollection = db.getCollection("UserInfo");
        promptCollection = db.getCollection("Prompts"); 
    }

    public int addUser(String username, String password){

        Document user = new Document();
        Document emailInfo = new Document();

        //build everything a user needs
        emailInfo.append("smtpPort","")
                    .append("smtpHost","")
                    .append("email","")
                    .append("emailPassword","");

        user.append("username", username)
            .append("password", password)
            .append("prompts", asList())
            .append("emailInfo", emailInfo);

        userInfoCollection.insertOne(user);

        //Http Status OK
        return 200;
    }

    //TODO method header is NOT final.
    public String addPrompt(String username, String promptType, String prompt, String promptBody){

        //make the new prompt object/document
        Document newPrompt = new Document();
        newPrompt.append("_id", new ObjectId())
                    .append("promptType", promptType)
                    .append("prompt", prompt)
                    .append("promptBody", promptBody);

        //filter db query to only select the user we want
        Bson filter = eq("username", username);

        //set the operation to push it to the user's prompts field.
        Bson updateOperation = push("prompts", newPrompt);

        //apply the update to the db
        UpdateResult updateResult = userInfoCollection.updateOne(filter,updateOperation);


        return "foo";
    }

    public String updateUserEmailInfo(String username, String smtpHost, String smtpPort, String email, String emailPassword){
        
        //Instead of changing one field I'm just gonna replace the
        //email document in the user entirely
        Document emailInfo = new Document();
        emailInfo.append("smtpPort", smtpPort)
            .append("smtpHost",smtpHost)
            .append("email",email)
            .append("emailPassword",emailPassword);

        Bson filter = eq("username", username);
        Bson updateOperation = set("emailInfo", emailInfo);

        UpdateResult updateResult = userInfoCollection.updateOne(filter,updateOperation);

        return "foo";
    }

    //prompts should have a unique identifier, dunno what it'll be but they should 
    public String deletePrompt(String username, String promptID){

        //what user does this prompt belong to
        Bson userFilter = eq("username", username);

        //remove the document in the prompts array that has the id of promptID
        Bson deleteOperation = pull("prompts", new Document().append("_id", new ObjectId(promptID)));

        UpdateResult updateResult = userInfoCollection.updateOne(userFilter,deleteOperation);
        return "foo";
    }

    public String modifyPromptBody(String username, String promptID, String newBody){
        Bson a = eq("username",username);
        Bson b = eq("prompts._id", new ObjectId(promptID));
        Bson q = combine(a,b);

        Bson setOperation = set("prompts.$.promptBody", newBody);

        UpdateResult ur = userInfoCollection.updateOne(q, setOperation);

        return "foo";
    }

    public static void main(String[] args){
        MongoHandler mongo = new MongoHandler(); 
        mongo.addUser("foo","bar");
        mongo.addUser("baz","buzz");
    }
}
