import java.io.*;
import java.net.*;
import org.bson.Document;
import org.bson.json.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;



public class DriverServer {
    public static void main(String[] args) throws Exception{ 

        test1();

    }

    public static void test1() throws Exception{
        RequestHandler rh = new RequestHandler();
        String username = "fpeng";
        QNA qna = new QNA( "yo mama?", "so fat", PromptType.NOCOMMAND);
        String s = rh.QNAToJSON(username, qna);

        String URL = "http://localhost:8100/prompts/";
        URL url = new URL(URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(
              conn.getOutputStream()
        );
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

    public static void test2() throws Exception{
        RequestHandler rh = new RequestHandler();
        String username = "fpeng";
        
        String s = rh.DeleteToJSON(username, "647fa181e5ac275613d49065");

        String URL = "http://localhost:8100/prompts/";
        URL url = new URL(URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(
              conn.getOutputStream()
        );
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

    public static void test3() throws Exception{
        System.out.println("TEST3XD");
        RequestHandler rh = new RequestHandler();
        String username = "fpeng";
        
        String s = rh.UsernameToJSON(username);

        String response = rh.sendHttpRequest(s,"PUT", "prompts");
        JSONArray jsonArray = new JSONArray(response);
        for (int i = 0; i < jsonArray.length(); i++){
            System.out.print(jsonArray.getJSONObject(0).getString("promptType"));

        }
    }
}
