import java.io.File;
import org.json.*;

//API Handler class that serves as an interface for the 3 different API's we use
public class APIHandler {

    private AudioHandler audioHandler;
    private GPTHandler gptHandler;
    private WhisperHandler whisperHandler;
    private static String APIKey = "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";
    private RequestHandler rh;

    APIHandler() {
        audioHandler = new AudioHandler();
        gptHandler = new GPTHandler(APIKey);
        whisperHandler = new WhisperHandler(APIKey);
        rh = new RequestHandler();
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
    public QNA audioToAnswer(String username) {
        return audioToReply(username);
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

    public QNA deletePromptType() {
        String promptT = "";
        String answer = "Your prompt was deleted.";
        return new QNA(promptT,answer,PromptType.DELETEPROMPT);
    }

    public QNA deleteAllPromptType() {
        String promptT = "";
        String answer = "All prompts were deleted.";
        return new QNA(promptT,answer,PromptType.DELETEALL);
    }


    public QNA setupEmailPromptType(String promptString){
        return new QNA("open the email setup lol", "open the email setup lol", PromptType.SETUPEMAIL);
    }

    public QNA createEmailPromptType(String promptString, String username){

        boolean tempIsDisplayNameSetup = true;

        //TODO: ACTUALLY IMPLEMENT THIS WHEN DB STUFF IS DONE.
        if(!tempIsDisplayNameSetup){
            return new QNA(promptString, "email creation unsuccessful - email not setup", PromptType.SETUPEMAIL);
        }

        String emailInfo = "";
        try {
            emailInfo = rh.sendHttpRequest(rh.UsernameToJSON(username), "POST", "email");
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("oopsie woopsie poopsie");
        }
        String displayName = (new JSONObject(emailInfo)).getString("displayName");

        //this is just a fancy question type, asks gpt to generate
        //we also ask it to sign it using our name
        //String displayName = "QUANDALE";
        String signatureRequest = ", sign my name with Best Regards, and my first name" + displayName + ". DO NOT CREATE A SUBJECT LINE.";
        String signedPromptString = promptString + signatureRequest;

        QNA questionSubQNA = questionPromptType(signedPromptString);

        //make sure the QNA does not have the fields that the creation method gives it
        questionSubQNA.setCommand(PromptType.CREATEEMAIL);
        questionSubQNA.setQuestion(promptString);

        return questionSubQNA;
    }

    public QNA handleSendEmailPromptType(String promptString){
        return new QNA(promptString,null,PromptType.SENDEMAIL);
    }

    public QNA handNoCommandPromptType(String promptString){
        return new QNA("NO COMMAND DETECTED PLEASE TRY AGAIN", "YOUR TEXT WAS " + promptString, PromptType.NOCOMMAND);
    }


    public QNA audioToReply(String username){
        String promptString = audioToQuestion(); //turn the current audio file into str

        if (promptString.equals("")){
            return new QNA("", "Your prompt was blank, please check your microphone and try again.", PromptType.NOCOMMAND);
        }

        System.out.println("parsing the line:" + promptString);
        PromptType pType = promptParser(promptString);

        switch (pType){
            case QUESTION:
                return questionPromptType(promptString);
            case DELETEPROMPT:
                return deletePromptType();
            case DELETEALL:
                return deleteAllPromptType();
            case SETUPEMAIL:
                return setupEmailPromptType(promptString); 
            case CREATEEMAIL:
                return createEmailPromptType(promptString, username);
            case SENDEMAIL:
                return handleSendEmailPromptType(promptString);
            case NOCOMMAND:
                return handNoCommandPromptType(promptString);
            default:
                break;
        }
        

        return handNoCommandPromptType(promptString);
    }

    public String parseSendEmailString(String s){
        return "horse";
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
        if (checkPunctuationEquals(strArr[0], "question")){
            return PromptType.QUESTION;
        }

        if (strArr.length == 1){
            return PromptType.NOCOMMAND;
        }

        //Email Setup prompt case-
        String wordTuple = strArr[0] + " " + strArr[1];
        if(checkPunctuationEquals(wordTuple.toLowerCase(), "create email")){
            return PromptType.CREATEEMAIL;
        }

        //"SEND EMAIL TO {RECIPIENT}"
        if(wordTuple.toLowerCase().equals("send email")){
            return PromptType.SENDEMAIL;
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
        
        // Delete prompt case
        if (checkPunctuationEquals(strArr[0], "delete") && checkPunctuationEquals(strArr[1], "prompt")){
            return PromptType.DELETEPROMPT;
        }

        // Delete all case
        if(checkPunctuationEquals(strArr[0], "delete") && checkPunctuationEquals(strArr[1], "all")){
            return PromptType.DELETEALL;
        }

        return PromptType.NOCOMMAND;
        
    }

    public Boolean checkPunctuationEquals(String str1, String str2) {
        if (str1.equals(str2)) {
            return true;
        }
        if (str1.equals(str2 + ".") || str1.equals(str2 + ",") || str1.equals(str2 + "?") || str1.equals(str2 + "!")) {
            return true;
        }
        return false;

    }
}
