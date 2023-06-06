import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;


public class LoginRequestHandler implements HttpHandler {

    private MongoHandler dbHandler;

    public LoginRequestHandler(MongoHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        String response = "Request received";
        String method = httpExchange.getRequestMethod();

        try{
            if(method.equals("GET")){
                response = handleGet(httpExchange);
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

    // Add method for adding new user info? 

    private String handleGet(HttpExchange httpExchange) throws IOException {
        //check if the provided username and password match with a user
        return "foo";
    }   
}

