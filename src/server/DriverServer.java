import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import org.bson.Document;



public class DriverServer {
    public static void main(String[] args) throws Exception{ 
        String URL = "http://localhost:8100/prompts/";
        URL url = new URL(URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(
              conn.getOutputStream()
        );


        Document d = new Document("yo mama", "so fat");
        d.append("she really", "fat");
        String s =  d.toJson();
        out.write(s);
        out.flush();
        out.close();
        BufferedReader in = new BufferedReader(
            new InputStreamReader(conn.getInputStream())
        );
        String response = in.readLine();
        in.close();
        System.out.print(response);

    }
}
