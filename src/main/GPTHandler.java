
import java.net.URI;
import java.net.URISyntaxException;
//import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Mostly copied and pasted from lab 4, only new methods are
 * askQuestion(String query) which could be used to re-ask an already
 * transcribed question without using up tokens on another Whisper call.
 * 
 */

public class GPTHandler {
    private static final String API_ENDPOINT = "https://api.openai.com/v1/completions";
    private String API_KEY;
    private static final String MODEL = "text-davinci-003";
    private static final int MAX_TOKENS = 100; 

    public GPTHandler(String API_KEY){
        this.API_KEY = API_KEY;
    }

    /**
     * @param query - The question to be aksed to ChatGPT
     * @return ChatGPT's response to the question
     */
    // TODO: handle exception elsewhere
    public String askQuestion(String query) throws Exception { // temporary exception
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest GPTRequest = buildGPTHttpRequest(query);

        HttpResponse<String> response = client.send(
            GPTRequest,
            HttpResponse.BodyHandlers.ofString()
            );

        String responseBody = response.body();
        JSONObject responseJson = new JSONObject(responseBody);
        JSONArray choices = responseJson.getJSONArray("choices");

        return choices.getJSONObject(0).getString("text");
    }

    /**
     * Builds the HTTPRequest for the ChatGPT API
     * @param prompt - the question to ask ChatGPT
     * @return
     */
    // TODO: handle exception elsewhere
    private HttpRequest buildGPTHttpRequest(String prompt){ // temporary exception
        //Create a request body which you will pass into request object
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", MAX_TOKENS);
        requestBody.put("temperature", 1.0);

        HttpClient client = HttpClient.newHttpClient();

        //Create the request object
        try{
            HttpRequest request = HttpRequest
            .newBuilder()
            .uri(new URI(API_ENDPOINT))
            .header("Content-Type", "application/json")
            .header("Authorization", String.format("Bearer %s", API_KEY))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build();
    
            return request; 

        }catch(URISyntaxException e){
            return null;
        }
    }
}