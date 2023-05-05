import java.io.*;
import java.net.*;
import org.json.*;

/**
 * Copied and pasted from lab, the only public method is transcribeAudio.
 */
public class WhisperHandler {
    //TODO Make DRYER
    private static final String API_ENDPOINT = "https://api.openai.com/v1/audio/transcriptions";
    private String API_KEY;
    private static final String MODEL = "whisper-1";

    public WhisperHandler(String API_KEY){
        this.API_KEY = API_KEY;
    }

    private void writeParameterToOutputStream (
        OutputStream outputStream,
        String parameterName,
        String parameterValue,
        String boundary
    ) throws IOException {
        outputStream.write(("--" + boundary + "\r\n").getBytes());
        outputStream.write(
            (
                "Content-Disposition: form-data; name=\"" + parameterName + "\"\r\n\r\n"
            ).getBytes()
        );
        outputStream.write((parameterValue + "\r\n").getBytes());
    }

    private void writeFileToOutputStream(
        OutputStream outputStream,
        File file,
        String boundary
    ) throws IOException {
        outputStream.write(("--" + boundary + "\r\n").getBytes());
        outputStream.write(
            (
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + 
                file.getName() +
                "\"\r\n"
            ).getBytes()
        );
        outputStream.write(("Content-Type: audio/mpeg\r\n\r\n").getBytes());

        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while((bytesRead = fileInputStream.read(buffer)) != -1){
            outputStream.write(buffer,0, bytesRead);
        }
        fileInputStream.close();
    }

    private String handleSuccessResponse(HttpURLConnection connection)
        throws IOException, JSONException {
            BufferedReader in = new BufferedReader( new InputStreamReader(connection.getInputStream()));

            String inputLine;
            StringBuilder response = new StringBuilder();
            while((inputLine = in.readLine()) != null){
                response.append(inputLine);
            }
            in.close();

            JSONObject responseJson = new JSONObject(response.toString());

            String generatedText = responseJson.getString("text");

            //print the transcription result

            return generatedText;
        }

    private String handleErrorResponse(HttpURLConnection connection)
        throws IOException, JSONException {
            BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(connection.getErrorStream())
            );

            String errorLine;
            StringBuilder errorResponse = new StringBuilder();
            while((errorLine = errorReader.readLine()) != null){
                errorResponse.append(errorLine);
            }

            errorReader.close();
            String errorResult = errorResponse.toString();
            return errorResult;
        }

    public String transcribeAudio(File Audio)
        throws IOException{
                        //create file object from file path
        File file = Audio;

        //Set up HTTP connection
        URL url = new URL(API_ENDPOINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        //Set up request headers
        String boundary = "Boundary-" + System.currentTimeMillis();
        connection.setRequestProperty(
            "Content-Type",
            "multipart/form-data; boundary=" + boundary
        );

        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);

        OutputStream outputStream = connection.getOutputStream();

        writeParameterToOutputStream(outputStream, "model", MODEL, boundary);
        writeFileToOutputStream(outputStream, file, boundary);
        outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());

        //Flush and close output stream
        outputStream.flush();
        outputStream.close();

        //Get response code

        int responseCode = connection.getResponseCode();
        String returnString;

        if(responseCode == HttpURLConnection.HTTP_OK){
            returnString = handleSuccessResponse(connection);
        } else {
            returnString = handleErrorResponse(connection);
        }

        connection.disconnect();
        return returnString; 
    }

}
