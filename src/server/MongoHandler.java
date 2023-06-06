import static com.mongodb.client.model.Filters.eq;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static java.util.Arrays.asList;
import java.util.ArrayList;

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
import com.mongodb.client.result.InsertOneResult;

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


    /**
     * Creates a user in the db. 
     * 
     * @param username - the user's username
     * @param password - the user's password
     * @return true if the user was successfully added, false if something went wrong
     */
    public boolean addUser(String username, String password){

        Document user = new Document();
        Document emailInfo = new Document();

        //build everything a user needs
        emailInfo.append("smtpPort","")
                    .append("smtpHost","")
                    .append("email","")
                    .append("emailPassword","")
                    .append("firstName", "")
                    .append("lastName", "")
                    .append("displayName", "");


        user.append("username", username)
            .append("password", password)
            .append("prompts", asList())
            .append("emailInfo", emailInfo);

        InsertOneResult ir = userInfoCollection.insertOne(user);

        return ir.wasAcknowledged();
    }

    /**
     * Gets all prompts associated with a user.
     * 
     * @param username - the username of the user whose documents you're getting
     * @return Returns an ArrayList of MongoDB Documents, null if the user does not exist. 
     */
    public ArrayList<Document> getAllUserPrompts(String username){
        
        Bson filter = eq("username",username);
        ArrayList<Document> query = (ArrayList<Document>) userInfoCollection.find(filter).first().get("prompts");
        
        

        System.out.println(query.size());
        
        return query;
    }
    
    /**
     * Adds a prompt to a user's profile.
     * 
     * @param username - the user's username
     * @param promptType - the type of the prompt
     * @param question - the command "Create email to X"
     * @param answer - "the response of the prompt"
     * @return The id of the newly created prompt, or null if there was no user with the given username.
     */
    public String addPrompt(String username, String promptType, String question, String answer){
        
        //make the new prompt object/document
        ObjectId oid = new ObjectId(); 
        Document newPrompt = new Document();
        newPrompt.append("_id", oid)
        .append("promptType", promptType)
        .append("question", prompt)
        .append("answer", promptBody);
        
        //filter db query to only select the user we want
        Bson filter = eq("username", username);
        
        //set the operation to push it to the user's prompts field.
        Bson updateOperation = push("prompts", newPrompt);
        
        //apply the update to the db
        UpdateResult updateResult = userInfoCollection.updateOne(filter,updateOperation);

        //no user was found to append the prompt to
        if(updateResult.getMatchedCount() == 0){
            return null;
        }
        
        return oid.toString();
    }
    

    /**
     * Updates a user's email info
     * 
     * @param username
     * @param smtpHost
     * @param smtpPort
     * @param email
     * @param emailPassword
     * @param firstName
     * @param lastName
     * @param displayName
     * @return true if the info was successfully updated, and false if something went wrong
     */
    public boolean updateUserEmailInfo(String username, String smtpHost, String smtpPort, String email, String emailPassword, String firstName, String lastName, String displayName){
        
        //Instead of changing one field I'm just gonna replace the
        //email document in the user entirely
        Document emailInfo = new Document();

        emailInfo.append("smtpPort", smtpPort)
        .append("smtpHost",smtpHost)
        .append("email",email)
        .append("emailPassword",emailPassword)
        .append("firstName", firstName)
        .append("lastName", lastName)
        .append("displayName", displayName);
        
        Bson filter = eq("username", username);
        Bson updateOperation = set("emailInfo", emailInfo);
        
        UpdateResult updateResult = userInfoCollection.updateOne(filter,updateOperation);
        
        return updateResult.getModifiedCount() == 1;
    }

    /**
     * Gets the email info of a user. 
     * 
     * @param username - the username of the user whose info we're trying to get
     * @return the user's email info
     */
    public Document getUserEmailInfo(String username){

        Bson filter = eq("username",username);
        Document emailInfo = (Document) userInfoCollection.find(filter).first().get("emailInfo");     
        
        return emailInfo;
    }

    /**
     * Gets a user object
     * @param username - username of the user we're querying
     * @return the user's document
     */
    public Document getUser(String username){
        Bson filter = eq("username", username);
        return userInfoCollection.find(filter).first();
    }


    /**
     * Delete a user's prompt
     * @param username - username of user
     * @param promptID - the ID of the prompt you want to delete.
     * @return true if something was deleted, false if nothing was deleted.
     */ 
    public boolean deletePrompt(String username, String promptID){
        
        //what user does this prompt belong to
        Bson userFilter = eq("username", username);
        
        //remove the document in the prompts array that has the id of promptID
        Bson deleteOperation = pull("prompts", new Document().append("_id", new ObjectId(promptID)));
        
        UpdateResult updateResult = userInfoCollection.updateOne(userFilter,deleteOperation);
        
        return updateResult.getModifiedCount() == 1;
    }
    
    /**
     * Modifies the bnody of a prompt
     * @param username
     * @param promptID
     * @param newAnswer - the new body to replace the old body
     * @return true if the modification was successful and false if otherwise
     */
    public boolean modifyPromptAnswer(String username, String promptID, String newAnswer){
        Bson a = eq("username",username);
        Bson b = eq("prompts._id", new ObjectId(promptID));
        Bson q = combine(a,b);
        
        Bson setOperation = set("prompts.$.answer", newAnswer);

        UpdateResult ur = userInfoCollection.updateOne(q, setOperation);
        
        return ur.getModifiedCount() == 1;
    }

    public static void main(String[] args){
        MongoHandler mongo = new MongoHandler(); 
        mongo.addUser("foo","bar");
        mongo.addUser("baz","buzz");
    }
}
