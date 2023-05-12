import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Scanner;
import java.io.File;

public class HistoryManager {
    private ArrayList<QNA> historyList;
    private File historyFile;

    public HistoryManager(String historyFilePath){
        historyList = new ArrayList<QNA>();
        historyFile = new File("history.txt");
        historyList = new ArrayList<QNA>();
        
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
    
    private void readFileIntoArrayList(){
        //Read the file into the ArrayList

        //might not be good 
        historyList.clear();
        String tempQuestion;
        String tempAnswer;

        try{
            FileReader fr = new FileReader(historyFile);
            BufferedReader br = new BufferedReader(fr);
            Scanner sr = new Scanner(br);
            sr.useDelimiter(",,,");
            while (sr.hasNext()) {
                tempQuestion = sr.next();
                tempAnswer = sr.next();
                historyList.add(new QNA(tempQuestion,tempAnswer));
            }
            sr.close();
            br.close();
            fr.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public ArrayList<QNA> getHistoryList(){
        return historyList;
    }

    public void addToHistory(QNA question){
        try{
            FileWriter history = new FileWriter(historyFile, true);
            history.write(question.getQuestion() + ",,," + question.getAnswer() + ",,,");
            history.close();
            historyList.add(question);
        } catch(Exception e){
            e.printStackTrace();
        }
        return;
    }

    public void deleteFromHistory(QNA question){
        return;
    }
}
