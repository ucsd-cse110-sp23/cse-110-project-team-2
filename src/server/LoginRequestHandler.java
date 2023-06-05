import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;


public class LoginRequestHandler implements HttpHandler {

    public LoginRequestHandler() {
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

    private String handleGet(HttpExchange httpExchange) throws IOException {
        //check if the provided username and password match with a user
    }   
}

