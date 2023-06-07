import java.util.ArrayList;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.mail.iap.Response;

import java.io.File;

public class HistoryManager {
    private ArrayList<Prompt> historyList;
    //private File historyFile;
    private Prompt selected; 

    private RequestHandler rh;
    private String username;

    //private final String DELIMITER = "%%%%%%%";


    private final String promptsEndpoint = "prompts";
    private final String putString = "PUT";
    private final String deleteString = "DELETE";
    private final String postString = "POST";

    public HistoryManager(String username){
        historyList = new ArrayList<Prompt>();

        rh = new RequestHandler();
        this.username = username;
        selected = null;
        readDBintoArraylist();



        //historyFile = new File(historyFilePath);
        
        /*Load a history file into a variable, build the readers/writers.
        try{
            if(!historyFile.exists()){
                historyFile.createNewFile();
            }
            readFileIntoArrayList();
        }catch(Exception e){
            e.printStackTrace();
        }
        */
        
    }

    public void setUsername(String username){
        this.username = username;
    }

    public Prompt getSelected(){
        return selected;
    }

    public void setSelected(Prompt select){
        if(selected != null){
            selected.changeState();
        }

        selected = select;
        selected.changeState();
    }

    public Prompt getSelectedToDelete(){
        if (selected == null) return null;

        Prompt temp = selected;
        historyList.remove(temp);
        deleteFromHistory(temp);
        selected = null;
        return temp;
    }
    
    /* 
    private void readFileIntoArrayList(){
        //Read the file into the ArrayList

        //might not be good 
        historyList.clear();
        String tempQuestion;
        String tempAnswer = "";
        QNA tempQNA;
        Prompt temptPrompt;

        try{
            FileReader fr = new FileReader(historyFile);
            BufferedReader br = new BufferedReader(fr);
            Scanner sr = new Scanner(br);
            sr.useDelimiter(DELIMITER);
            while (sr.hasNext()) {
                tempQuestion = sr.next();
                tempAnswer = sr.next();
                //TODO: Store command too now
                tempQNA = new QNA(tempQuestion,tempAnswer, PromptType.NOCOMMAND);
                historyList.add(new Prompt(tempQNA));
            }
            sr.close();
            br.close();
            fr.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    */
    public void readDBintoArraylist() {
        String body = rh.UsernameToJSON(username);
        System.out.print(body);
        String statusMessage = "";
        try {
            statusMessage = rh.sendHttpRequest(body, putString, promptsEndpoint);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.print(statusMessage);
        JSONObject statusMessageJSON = new JSONObject(statusMessage);
        String status = statusMessageJSON.getString("status");
        String response = statusMessageJSON.getString("message");
        JSONArray jsonArray = new JSONArray(response);
        QNA tempQNA;
        JSONObject tempJsonObject;
        for (int i = 0; i < jsonArray.length(); i++){
            tempJsonObject = jsonArray.getJSONObject(i);
            tempQNA = new QNA(tempJsonObject.get("_id").toString(), 
                tempJsonObject.getString("question"), 
                tempJsonObject.getString("answer"), 
                PromptType.valueOf(tempJsonObject.getString("promptType")));
            historyList.add(new Prompt(tempQNA));
        }
        
    }
    
    public ArrayList<Prompt> getHistoryList(){
        return historyList;
    }

    public Prompt addToHistory(QNA question){
        

        /* 
        try{
            FileWriter history = new FileWriter(historyFile, true);
            history.write(question.getQuestion() + DELIMITER + question.getAnswer() + DELIMITER);
            history.close();
            historyList.add(newPrompt);
        } catch(Exception e){
            e.printStackTrace();
        }
        */
        String body = rh.QNAToJSON(username, question);
        String responseString = "";
        try {
            responseString = rh.sendHttpRequest(body, postString, promptsEndpoint);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        question.setID(responseString);
        Prompt newPrompt = new Prompt(question);
        setSelected(newPrompt);
        historyList.add(newPrompt);

        return newPrompt;
    }

    public void deleteFromHistory(Prompt question){
        String body = rh.DeleteToJSON(username, question.getQNA().getID());
        String responseString = "";
        try {
            responseString = rh.sendHttpRequest(body, deleteString, promptsEndpoint);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        historyList.remove(question);
        /*
        FileWriter history;
        boolean deleted = false;
        try{
            history = new FileWriter(historyFile);
        }catch(Exception e){
            return;
        }

        for(Prompt prompt: historyList){

            if(prompt.equals(question) && !deleted){
                deleted = true;
                continue;
            }

            try{
                QNA qna = prompt.getQNA();
                history.write(qna.getQuestion() + DELIMITER + qna.getAnswer() + DELIMITER);
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        try{
            history.close();
        }catch(Exception e){

        }
        //readFileIntoArrayList();
        */
        return;
    }

    public void clearHistory() {
        for (Prompt p : historyList) {
            String body = rh.DeleteToJSON(username, p.getQNA().getID());
            String responseString = "";
            try {
                responseString = rh.sendHttpRequest(body, deleteString, promptsEndpoint);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        historyList.clear();
        selected = null;
    }

}
