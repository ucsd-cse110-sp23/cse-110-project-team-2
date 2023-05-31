
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
        whisperHandler = new MockWhisper("question What is the meaning of life?");
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
        return new QNA(question, answer, PromptType.QUESTION);
    }

    public QNA questionPromptType(String promptString){
        String[] strArr = promptString.split(" ", 2);

        if (strArr.length == 1 || strArr[1].equals("")){
            return new QNA("", "Your question was empty, please try asking the quesiton again.", PromptType.QUESTION);
        }
        String questionT = strArr[1];
        String answer = questionToAnswer(questionT);
        return new QNA(questionT,answer, PromptType.QUESTION);

    }


    public QNA audioToReply(){
        String promptString = audioToQuestion();
        if (promptString.equals("")){
            return new QNA("", "Your prompt was blank, please check your microphone and try again.", PromptType.NOCOMMAND);
        }
        PromptType pType = promptParser(promptString);

        switch (pType){
            case QUESTION:
                return questionPromptType(promptString);
            default:
                break;
        }
        

        return new QNA("NO COMMAND DETECTED PLEASE TRY AGAIN", "YOUR TEXT WAS " + promptString, PromptType.NOCOMMAND);
    }



    //Setup email (2 or 3 words?) , delete all, delete prompt, question, create email, send email
    public PromptType promptParser(String transcriptionString){

        String[] strArr = transcriptionString.split(" ", 2);
        if (strArr.length == 0 || transcriptionString.equals("")){
            System.out.println("Empty string voice input");
            return null;
        }
        strArr[0] = strArr[0].toLowerCase();
        
        //Question prompt case
        if (strArr[0].equals("question") || strArr[0].equals("question,") || strArr[0].equals("question.")){
            return PromptType.QUESTION;
        }

        if(transcriptionString.toUpperCase().equals("SETUP EMAIL.") ||
        transcriptionString.toUpperCase().equals("SET UP EMAIL.") ||
        transcriptionString.toUpperCase().equals("SET UP EMAIL") ||
        transcriptionString.toUpperCase().equals("SETUP EMAIL"))
        {
            //TODO OPEN THE EMAIL SETUP WINDOW
            return PromptType.SETUPEMAIL;
        }

        
        if(strArr.length > 1 ){
            strArr[1] = strArr[1].toLowerCase();
        }
        //two/three word length prompt cases
        

        return PromptType.NOCOMMAND;
        
    }
}
