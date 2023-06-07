import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;
import org.bson.Document;

public class SetupEmailRequestHandler implements HttpHandler {

    private MongoHandler dbHandler;
    private String TRUE = "true", FALSE = "false";

    public SetupEmailRequestHandler(MongoHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        String response = "Request received";
        String method = httpExchange.getRequestMethod();

        try{
            if(method.equals("GET")){
                System.out.println("We dont have a get method for this endpoint you trolling");
                //response = //handleGet(httpExchange);
            } else if (method.equals("POST")){
                response = handlePost(httpExchange);
            } else if (method.equals("DELETE")){
                System.out.println("We dont have a delete method for this endpoint you trolling");
            } else if (method.equals("PUT")){
                response = handlePut(httpExchange);
            } 
            else {
                throw new Exception("INVALID REQUEST");
            }
        }catch(Exception e){
            System.out.println("oopsies");
            e.printStackTrace();
        }

        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream outStream = httpExchange.getResponseBody();
        outStream.write(response.getBytes());
        outStream.close();
    } 

    // Add method for adding new user info? -- Done in LoginRequestHandler

    /**
     * Get email credentials. 
     * @param httpExchange Contains the request bodyString
     * @require bodyString syntax: {"username": "amongus"}
     * @returns statusMessage syntax: if successful, {"status": "true", "message": "blahblah"}
     *                                otherwise, {"status": "false", "message": "Some error"}
     */
    private String handlePost(HttpExchange httpExchange) throws IOException {
        System.out.println("Setup Email POST request recieved.");
        Scanner scnr = new Scanner(httpExchange.getRequestBody());
        String requestBody = scnr.nextLine(); 
        scnr.close();

        JSONObject jsonRequest = new JSONObject(requestBody);
        String username = jsonRequest.getString("username");

        String status = "";
        String message = "";

        Document userDoc = dbHandler.getUser(username);

        if (userDoc != null) {
            JSONObject emailJSON = (new JSONObject(userDoc)).getJSONObject("emailInfo");
            status = TRUE;
            message = emailJSON.toString();
        }
        else {
            status = FALSE;
            message = "User " + username + " not found.";
        }

        return RequestHandler.statusMessageToJSON(status, message);
    }   
    
    /**
     * Update email credentials. 
     * @param httpExchange Contains the request bodyString
     * @require bodyString syntax: {"username": "amongus", 
     *                              "firstName": "sus",
     *                              ... the rest of the email setup credentials}
     * @returns statusMessage syntax: if successful, {"status": "true", "message": "blahblah"}
     *                                otherwise, {"status": "false", "message": "Some error"}
     */
    private String handlePut(HttpExchange httpExchange) throws IOException {
        System.out.println("Setup Email PUT request recieved.");
        Scanner scnr = new Scanner(httpExchange.getRequestBody());
        String requestBody = scnr.nextLine(); 
        scnr.close();

        JSONObject jsonRequest = new JSONObject(requestBody);
        String username = jsonRequest.getString("username");
        String firstName = jsonRequest.getString("firstName");
        String lastName = jsonRequest.getString("lastName");
        String displayName = jsonRequest.getString("displayName");
        String smtpHost = jsonRequest.getString("smtpHost");
        String smtpPort = jsonRequest.getString("smtpPort");
        String email = jsonRequest.getString("email");
        String emailPassword = jsonRequest.getString("emailPassword");

        String status = "";
        String message = "";

        if (dbHandler.getUser(username) != null) {
            boolean updatedEmail = dbHandler.updateUserEmailInfo(username, smtpHost, smtpPort, 
                    email, emailPassword, firstName, lastName, displayName);
            if (updatedEmail) {
                status = TRUE;
                message = "User " + username + "'s email setup was successfully updated.";
            }
            else {
                status = FALSE;
                message = "User "+username+" was valid but email setup update went wrong.";
            }
        }
        else {
            status = FALSE;
            message = "User "+username+" does not exist in the database.";
        }

        return RequestHandler.statusMessageToJSON(status, message);
    }   
}

