import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.bson.Document;
import org.bson.json.JsonObject;
import org.json.JSONObject;


public class EmailRequestHandler implements HttpHandler {

    private MongoHandler dbHandler;

    public EmailRequestHandler(MongoHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        String response = "request received";
        System.out.println("METHOD IS" + method);

        try{
            if(method.equals("POST")){
                response = handlePost(httpExchange);
            } else if (method.equals("PUT")){
                response = handlePut(httpExchange);
            } else {
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

    //get user email info here
    public String handlePost(HttpExchange httpExchange){
        System.out.println("prompt GET request recieved");
        Scanner scnr = new Scanner(httpExchange.getRequestBody());
        String requestBody = scnr.nextLine(); 
        scnr.close();
        
        JSONObject jsonRequest = new JSONObject(requestBody);


        Document emailInfo = dbHandler.getUserEmailInfo(jsonRequest.get("username").toString());
        JSONObject response = new JSONObject(emailInfo.toJson());

        return response.toString();
    }
    
    //set a user's email info
    public String handlePut(HttpExchange httpExchange){
        System.out.println("prompt GET request recieved");
        Scanner scnr = new Scanner(httpExchange.getRequestBody());
        String requestBody = scnr.nextLine(); 
        scnr.close();
        
        JSONObject jsonRequest = new JSONObject(requestBody);
        JSONObject response = new JSONObject();

        dbHandler.updateUserEmailInfo(jsonRequest.get("username").toString(),
        jsonRequest.get("smtpHost").toString(),
        jsonRequest.get("smtpPort").toString(),
        jsonRequest.get("email").toString(),
        jsonRequest.get("emailPassword").toString(),
        jsonRequest.get("firstName").toString(),
        jsonRequest.get("lastName").toString(),
        jsonRequest.get("displayName").toString());

        response.append("status","200");

        return response.toString();
    }

    //SEND EMAIL HERE
    public String handlePatch(HttpExchange httpExchange){

        return "{\"test\" : \"foo\"}";
    }
}