import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;


public class SayItServer {


 // initialize server port and hostname
 private static final int SERVER_PORT = 8100;
 private static final String SERVER_HOSTNAME = "localhost";


 public static void main(String[] args) throws IOException {
    // create a thread pool to handle requests
   ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

   Map<String, String> data = new HashMap<>();

   HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_HOSTNAME, SERVER_PORT),0);

   MongoHandler dbHandler = new MongoHandler();
   server.createContext("/login/", new LoginRequestHandler(dbHandler));
   server.createContext("/prompts/", new PromptRequestHandler(dbHandler));
   server.createContext("setupEmail", new SetupEmailRequestHandler(dbHandler));
   server.setExecutor(threadPoolExecutor);
   server.start();
   System.out.println("Server Started!");

 }
}

