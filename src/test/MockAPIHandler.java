
import java.io.File;

public class MockAPIHandler {

    public MockAudioHandler audioHandler;
    public MockGPT gptHandler;
    public MockWhisper whisperHandler;
    //private String MOCK_API_KEY = "f90q324j0j4359f90w";
    String[] answerSet = {"42", "The meaning of life is 42", "42 is the meaning of life"};

    MockAPIHandler() {
        audioHandler = new MockAudioHandler();
        gptHandler = new MockGPT(answerSet);
        whisperHandler = new MockWhisper("What is the meaning of life?");
    }

    public void startRecording() {
        //audioHandler.startRecording();
    }

    public void stopRecording() {
        //audioHandler.stopRecording();
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
