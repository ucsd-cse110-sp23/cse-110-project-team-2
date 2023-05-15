import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Scanner;
import java.io.File;
import javax.swing.JButton;
import java.awt.event.ActionEvent;

public class HistoryManager {
    private ArrayList<Prompt> historyList;
    private File historyFile;
    private Prompt selected; 

    public HistoryManager(String historyFilePath){
        historyList = new ArrayList<Prompt>();
        historyFile = new File(historyFilePath);
        selected = null;
        //Load a history file into a variable, build the readers/writers.
        try{
            if(!historyFile.exists()){
                historyFile.createNewFile();
            }
            readFileIntoArrayList();
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }

    public void setSelected(Prompt select){
        if(selected != null){
            selected.changeState();
        }

        selected = select;
        selected.changeState();
    }

    public Prompt getSelectedToDelete(){
        Prompt temp = selected;
        historyList.remove(temp);
        deleteFromHistory(temp);
        return temp;
    }
    
    
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
            sr.useDelimiter(",,,");
            while (sr.hasNext()) {
                tempQuestion = sr.next();
                tempAnswer = sr.next();
                tempQNA = new QNA(tempAnswer,tempQuestion);
                historyList.add(new Prompt(tempQNA));
            }
            sr.close();
            br.close();
            fr.close();
        } catch(Exception e){
            System.out.println("");
        }
    }
    
    public ArrayList<Prompt> getHistoryList(){
        return historyList;
    }

    public Prompt addToHistory(QNA question){
        Prompt newPrompt = new Prompt(question);
        setSelected(newPrompt);

        try{
            FileWriter history = new FileWriter(historyFile, true);
            history.write(question.getQuestion() + ",,," + question.getAnswer() + ",,,");
            history.close();
            historyList.add(newPrompt);
        } catch(Exception e){
            e.printStackTrace();
        }

        return newPrompt;
    }

    

    public void deleteFromHistory(Prompt question){
        FileWriter history;
        try{
            history = new FileWriter(historyFile);
        }catch(Exception e){
            return;
        }

        for(Prompt prompt: historyList){

            if(prompt.equals(question)){
                continue;
            }

            try{
                QNA qna = prompt.getQNA();
                history.write(qna.getQuestion() + ",,," + qna.getAnswer() + ",,,");
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        try{
            history.close();
        }catch(Exception e){

        }
        readFileIntoArrayList();
        return;
    }

    public void clearHistory() {
        try{
            FileWriter history = new FileWriter(historyFile);
            history.write("");
            history.close();
            historyList.clear();
            selected = null;
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}