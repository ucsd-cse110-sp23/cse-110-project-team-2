
import java.io.File;

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

    public QNA audioToAnswer() {
        String question = audioToQuestion();
        String answer = questionToAnswer(question);
        return new QNA(question, answer);
    }
}
