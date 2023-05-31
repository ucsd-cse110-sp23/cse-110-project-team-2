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
        return audioToReply();
    }

    public QNA questionPromptType(String promptString){
        String[] strArr = promptString.split(" ", 2);

        if (strArr.length == 1 || strArr[1].equals("")){
            return new QNA("", 
                "Your question was empty, please try asking the question again.", 
                PromptType.QUESTION);
        }
        String questionT = strArr[1];
        String answer = questionToAnswer(questionT);
        return new QNA(questionT,answer,PromptType.QUESTION);

    }

    public QNA setupEmailPromptType(String promptString){
        return new QNA("open the email setup lol", "open the email setup lol", PromptType.SETUPEMAIL);
    }

    public QNA audioToReply(){
        String promptString = audioToQuestion(); //turn the current audio file into str

        if (promptString.equals("")){
            return new QNA("", "Your prompt was blank, please check your microphone and try again.", PromptType.NOCOMMAND);
        }

        System.out.println("parsing the line:" + promptString);
        PromptType pType = promptParser(promptString);

        switch (pType){
            case QUESTION:
                return questionPromptType(promptString);
            case SETUPEMAIL:
                return setupEmailPromptType(promptString); 
            default:
                break;
        }
        

        return new QNA("NO COMMAND DETECTED PLEASE TRY AGAIN", "YOUR TEXT WAS " + promptString, PromptType.NOCOMMAND);
    }



    //Setup email (2 or 3 words?) , delete all, delete prompt, question, create email, send email
    public PromptType promptParser(String transcriptionString){

        String[] strArr = transcriptionString.split(" ", 4);
        if (strArr.length == 0 || transcriptionString.equals("")){
            System.out.println("Empty string voice input");
            return null;
        }
        strArr[0] = strArr[0].toLowerCase();
        
        //Question prompt case
        if (strArr[0].equals("question") || strArr[0].equals("question,") || strArr[0].equals("question.")){
            return PromptType.QUESTION;
        }
        //Email Setup prompt case

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
