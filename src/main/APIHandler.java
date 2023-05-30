
import java.io.File;


//API Handler class that serves as an interface for the 3 different API's we use
public class APIHandler {

    private AudioHandler audioHandler;
    private GPTHandler gptHandler;
    private WhisperHandler whisperHandler;
    private static String APIKey = "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";

    APIHandler() {
        audioHandler = new AudioHandler();
        gptHandler = new GPTHandler(APIKey);
        whisperHandler = new WhisperHandler(APIKey);
    }

    public void startRecording() {
        audioHandler.startRecording();
    }

    public void stopRecording() {
        audioHandler.stopRecording();
    }

    /*
     *  Sends the audio to whisper API, then recieves the transcription. Delegates to whisper API handler
     * in:
     * out: the return string from whisperAPI
     */
    private String audioToQuestion() {
        File newFile = new File("recording.wav");
        String whisperResponse = "";
        try {
            System.out.println("getting API responses...");
            whisperResponse = whisperHandler.transcribeAudio(newFile);
        } catch (Exception e) {
            e.printStackTrace();
            return "Return a message that only has the text: \"There was an error in audio transcription\"";
        }
        return whisperResponse;
    }

    /*
     * Sends the question transcription to GPT API and recieves the response
     * in: transcription String
     * out: answer String
     */
    private String questionToAnswer(String transcription) {
        String gptResponse = "";
        try {
            gptResponse = gptHandler.askQuestion(transcription);
            System.out.println(gptResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return "Something went wrong uwu (someone's fault)";
        }
        return gptResponse;
    }

    /*
     * Goes from recording file to transcription to GPT answer. 
     * in: transcription String
     * out: answer String
     */
    public QNA audioToAnswer() {
        String question = audioToQuestion();
        String answer = questionToAnswer(question);
        return new QNA(question, answer);
    }
}
