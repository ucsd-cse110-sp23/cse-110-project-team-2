import java.io.*;
import java.util.*;
// gui libraries
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.JList;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;


// Recording panel with buttons start and stop,with recording label
class RecordPanel extends JPanel {
    private JButton startButton;
    private JButton stopButton;
    private JLabel recordingLabel;

    Color yellow = new Color(229, 239, 193);
    Color green = new Color(162, 213, 171);
    Color turquoise = new Color(57, 174, 169);
    Color blue = new Color(85, 123, 131);
    
    //private Border emptyBorder = BorderFactory.createEmptyBorder();

    RecordPanel(){
        this.setPreferredSize(new Dimension(600, 100));
        this.setBackground(blue);

        startButton = new JButton("Start");
        startButton.setPreferredSize(new Dimension(80, 20));
        startButton.setFont(new Font("Verdana", 0, 15));
        //startButton.setBackground(blue);
        this.add(startButton);
        stopButton = new JButton("Stop");
        stopButton.setPreferredSize(new Dimension(80, 20));
        stopButton.setFont(new Font("Verdana", 0, 15));
        //stopButton.setBackground(blue);
        this.add(stopButton); 

        recordingLabel = new JLabel("Recording");
        recordingLabel.setVerticalAlignment(JLabel.CENTER);
        recordingLabel.setForeground(Color.RED);
        recordingLabel.setPreferredSize(new Dimension(100, 20));
        recordingLabel.setFont(new Font("Verdana", 0, 15));
        recordingLabel.setVisible(false);
        this.add(recordingLabel);

    }


    //TODO: Move listeners to QNA panel class possibly.
    
    

    public JButton getStartButton(){
        return startButton;
    }

    public JButton getStopButton(){
        return stopButton;
    }

    public void hideRecording(){
        recordingLabel.setVisible(false);
    }
    public void showRecording(){
        recordingLabel.setVisible(true);
    }
}

//  Total panel with the prompts/answers and the buttons?
class QnaPanel extends JPanel {

    JButton startButton;
    JButton stopButton;

    //TODO: REFACTOR TO USE THIS STYLE (IMPORTANT)
    QnaDisplay qnaDisplay;
    
    public HistoryList historyList;
    RecordPanel recordPanel;
    

    APIHandler apiHandler;
    HistoryManager historyManager;
    GUIMediator guiMediator; 

    Color yellow = new Color(229, 239, 193);
    Color green = new Color(162, 213, 171);
    Color turquoise = new Color(57, 174, 169);
    Color blue = new Color(85, 123, 131);
/*
    private AudioHandler audioHandler;
    private GPTHandler gptHandler;
    private WhisperHandler whisperHandler;
    private static String APIKey = "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";
*/

    QnaPanel(GUIMediator guiM, HistoryManager histManager) {
        this.setPreferredSize(new Dimension(600, 300));

        this.setLayout(new BorderLayout());
        this.setBackground(yellow);


        qnaDisplay = new QnaDisplay(guiM);
        recordPanel = new RecordPanel();
        this.add(qnaDisplay, BorderLayout.CENTER);
        this.add(recordPanel, BorderLayout.SOUTH);


        startButton = recordPanel.getStartButton();
        stopButton = recordPanel.getStopButton();

        apiHandler = new APIHandler();
        historyManager = histManager;
        guiMediator = guiM;

/*     
        audioHandler = new AudioHandler();
        gptHandler = new GPTHandler(APIKey);
        whisperHandler = new WhisperHandler(APIKey);
*/

        addListeners();
    }

    public QnaDisplay getQnaDisplay(){
        return qnaDisplay;
    }

    public void setHistoryList(HistoryList hl){
        historyList = hl;
    }

    public void addListeners() {
        startButton.addActionListener(
          (ActionEvent e) -> {
              System.out.println("START PRESSED");
              //audioHandler.startRecording();
              recordPanel.showRecording();
              apiHandler.startRecording();
          }
        );
        stopButton.addActionListener(
        (ActionEvent e) -> {
            System.out.println("STOP PRESSED");
            //TODO: maybe multithread?
            recordPanel.hideRecording();
            apiHandler.stopRecording();
            QNA gptQNA = apiHandler.audioToAnswer();

            guiMediator.changeQnaDisplayText(gptQNA);
            Prompt newPrompt = historyManager.addToHistory(gptQNA);
            guiMediator.addHistoryListPrompt(newPrompt);            
            revalidate();                
          }
        );
    }
}


// Panel type to display content
class ContentPanel extends JPanel {

    private String title, content;
    private TextPane titlePane, contentPane;

    ContentPanel(String title, String content, 
                 Color titleColor, Color titlePaneColor, 
                 Color contentColor,  Color contentPaneColor) {
        this.setPreferredSize(new Dimension(600, 300));
        this.setLayout(new BorderLayout());

        this.title = title;
        this.content = content;

        titlePane = new TextPane(title, new Dimension(600,50), 20, titleColor, titlePaneColor);
        contentPane = new TextPane(content, new Dimension(600,250), 20, contentColor, contentPaneColor);
        this.setTitle(title);
        this.setContent(content);

        this.add(titlePane, BorderLayout.NORTH);
        this.add(contentPane, BorderLayout.CENTER);
    }

    public void setContent(String content){
        this.content = content;
        contentPane.replace(content);
    }

    public String getContent(){
        return content;
    }

    public void setTitle(String title){
        this.title = title;
        titlePane.replace(title);
    }


    public String getTitle(){
        return title;
    }
}


// class to be used as a text pane
class TextPane extends JTextPane {   

    TextPane(String text, Dimension size, int fontSize, Color textColor, Color paneColor) {
        this.setEditable(false);
        this.setBackground(paneColor);
        this.setPreferredSize(size);
        //this.setFont(new Font("Open Sans", 0, size));

        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, textColor);
        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Verdana");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_CENTER);
        aset = sc.addAttribute(aset, StyleConstants.FontSize, fontSize);
        this.setCharacterAttributes(aset, false);

        this.replace(text);
    }

    public void replace(String msg) {
        this.setEditable(true);
        this.setText(msg);
        this.setEditable(false);
    }

    public void setBG(Color paneColor) {
        this.setBackground(paneColor);
    }
}


// Display for the question and answer
class QnaDisplay extends JPanel {
    Color yellow = new Color(229, 239, 193);
    Color green = new Color(162, 213, 171);
    Color turquoise = new Color(57, 174, 169);
    Color blue = new Color(85, 123, 131);

    //Refactor maybe?
    ContentPanel questionContentPanel;
    ContentPanel answerContentPanel;
    QnaDisplay(GUIMediator guiM) {
        this.setPreferredSize(new Dimension(600, 600));
        this.setLayout(new GridLayout(2, 1));
        this.setBackground(Color.GREEN);

        questionContentPanel = new ContentPanel("Question", "Select a question from the history list.", 
                                                Color.BLACK, turquoise, Color.DARK_GRAY, yellow);
        answerContentPanel =  new ContentPanel("Answer", "Or ask a question using the record button.",
                                               Color.BLACK, turquoise, Color.BLUE, yellow);

        this.add(questionContentPanel);
        this.add(answerContentPanel);

        guiM.setQnaDisplay(this);
        
    }

    public void setQNASection(QNA qna){
        questionContentPanel.setContent(qna.getQuestion());
        answerContentPanel.setContent(qna.getAnswer());
        
    }
}

//SHOULD ONLY DISPLAY QUESTIONS
// TODO: Separate class for managing history (keep business logic out of gui)
class HistoryList extends JPanel {
    public QnaDisplay qnaDisplay;
    private GridLayout layout;
    private final int MIN_ROWS = 6;

    Color backgroundColor = new Color(240, 248, 255);
    HistoryManager historyManager;
    GUIMediator guiMediator;
    int count;
    Color yellow = new Color(229, 239, 193);
    Color green = new Color(162, 213, 171);
    Color turquoise = new Color(57, 174, 169);
    Color blue = new Color(85, 123, 131);

    HistoryList(GUIMediator guiM, HistoryManager histManager){
        layout = new GridLayout(0, 1);
        layout.setVgap(5); // Vertical gap
        layout.setColumns(1);

        this.setLayout(layout); // 10 tasks
        //this.setPreferredSize(new Dimension(100, 500));
        this.setBackground(green);

        guiM.setHistoryList(this);
        this.historyManager = histManager;
        this.guiMediator = guiM;
        loadHistory();
    }

    public void setQnaDisplay(QnaDisplay qd){
        qnaDisplay = qd;
    }

    public void updateLayout() {
        int numRows = (count < MIN_ROWS) ? MIN_ROWS : count;
        this.layout.setRows(numRows);
    }

    public void addPrompt(Prompt prompt){
        count++;
        updateLayout();
        this.add(prompt);
        JButton selectButton = prompt.getSelectButton();
        selectButton.addActionListener(
            (ActionEvent e2) -> {
                //TODO: update qnadisplay to show the selected prompt and answer
                historyManager.setSelected(prompt);
                guiMediator.changeQnaDisplayText(prompt.getQNA());
                setQnaDisplay(qnaDisplay);
                revalidate(); // Updates the frame
            }
        );
    }

    public void removePrompt(Prompt prompt){
        count--;
        updateLayout();
        this.remove(prompt);
        revalidate();
        //repaint();
    }

    private void loadHistory(){
        ArrayList<Prompt> promptList = historyManager.getHistoryList();
        for(Prompt prompt : promptList){
            addPrompt(prompt);
        }
        count = promptList.size();
    }
}


// Entire panel for history
class HistoryPanel extends JPanel {
    //private JPanel historyFooter;
    private HistoryButtonPanel historyButtonPanel;
    private HistoryList historyList;
    private HistoryManager historyManager;
    private JButton deleteSingleButton;
    private JButton deleteAllButton;

    private GUIMediator guiM;
    Color yellow = new Color(229, 239, 193);
    Color green = new Color(162, 213, 171);
    Color turquoise = new Color(57, 174, 169);
    Color blue = new Color(85, 123, 131);


    HistoryPanel(GUIMediator guiM, HistoryManager histManager) {
        this.setPreferredSize(new Dimension(200, 800));
        this.setBackground(green);
        this.setLayout(new BorderLayout());

        TextPane headerPanel = new TextPane("History", new Dimension(200, 50), 20, Color.LIGHT_GRAY, blue);
        this.add(headerPanel, BorderLayout.NORTH);

        historyList = new HistoryList(guiM, histManager);
        //this.add(historyList, BorderLayout.CENTER);

        JScrollPane sp = new JScrollPane(historyList);
        sp.setPreferredSize(new Dimension(200, 670));
        this.setAutoscrolls(true);
        this.add(sp, BorderLayout.CENTER);

        historyButtonPanel = new HistoryButtonPanel();
        this.add(historyButtonPanel, BorderLayout.SOUTH);

        this.deleteSingleButton = historyButtonPanel.getDeleteSingleButton();
        this.deleteAllButton = historyButtonPanel.getDeleteAllButton();

        this.guiM = guiM;

        this.historyManager = histManager;
        addListeners();

    }

    public void setQnaDisplay(QnaDisplay qd){
        historyList.setQnaDisplay(qd);
    }

    public HistoryList getHistoryList(){
        return historyList;
    }

    public void addListeners(){
        deleteSingleButton.addActionListener(
            (ActionEvent e) -> {
                Prompt toDelete = historyManager.getSelectedToDelete();
                if (toDelete != null) {
                    System.out.println("Deleting prompt" + toDelete.getQNA().toString());
                    historyList.removePrompt(toDelete);
                    guiM.clearQNADisplayText();
                    repaint();
                    revalidate();
                }
            }
        );

        deleteAllButton.addActionListener(
            (ActionEvent e) -> {
                for (Component prompt : historyList.getComponents()){
                    if (prompt instanceof Prompt) {
                        historyList.removePrompt((Prompt) prompt);
                        repaint();
                        revalidate();
                    }
                }
                historyManager.clearHistory();
                guiM.clearQNADisplayText();
                repaint();
                revalidate();
            }
        );
    }
}


// Panel for buttons
class HistoryButtonPanel extends JPanel {
    private JButton deleteAll;
    private JButton deleteSingle;

    private JLabel deleteSelected; // Maybe implement when any delete is selected, label shows up
    HistoryButtonPanel() {
        this.setLayout(new GridLayout(2, 1));
        deleteAll = new JButton("Delete All");
        deleteAll.setFont(new Font("Verdana", 0, 15));
        deleteAll.setPreferredSize(new Dimension(80, 20));


        deleteSingle = new JButton("Delete Selected");
        deleteSingle.setFont(new Font("Verdana", 0, 15));
        deleteSingle.setPreferredSize(new Dimension(80, 20));

        this.setPreferredSize(new Dimension(200, 100));
        this.add(deleteAll, BorderLayout.CENTER);
        this.add(deleteSingle, BorderLayout.CENTER);
    }

    public JButton getDeleteAllButton(){
        return deleteAll;
    }

    public JButton getDeleteSingleButton(){
        return deleteSingle;
    }



}

// appframe
class AppFrame extends JFrame {

    AppFrame() {
        this.setTitle("Application");
        this.setSize(800, 800);
        this.setBackground(Color.DARK_GRAY);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close on exit


        GUIMediator guiMediator =  new GUIMediator();
        HistoryManager historyManager = new HistoryManager("history.txt");

        HistoryPanel hp = new HistoryPanel(guiMediator, historyManager);
        QnaPanel qp = new QnaPanel(guiMediator, historyManager);
        hp.getHistoryList().qnaDisplay = qp.getQnaDisplay();
        this.add(hp, BorderLayout.WEST);
        this.add(qp, BorderLayout.CENTER);
        qp.setHistoryList(hp.getHistoryList());
        this.setVisible(true); // Make visible
    }
}


// qna prompt in the list
class Prompt extends JPanel {

    TextPane qtext;
    JButton selectButton;
  
    Color gray = new Color(218, 229, 234);
    Color green = new Color(188, 226, 158);
    Color yellow = new Color(229, 239, 193);
  
    private boolean selected;
    private QNA qna;
  
    Prompt(QNA qna) {
      this.qna = qna;
      this.setPreferredSize(new Dimension(200, 75)); // set size of task
      this.setBackground(gray); // set background color of task
  
      this.setLayout(new BorderLayout()); // set layout of task
  
      //markedDone = false;
  
      /*qtext = new JLabel(qna.getQuestion()); // create index label
      qtext.setPreferredSize(new Dimension(150, 20)); // set size of index label
      qtext.setHorizontalAlignment(JLabel.CENTER); // set alignment of index label
      this.add(qtext, BorderLayout.WEST); // add index label to task*/

      qtext = new TextPane(qna.getQuestion(), new Dimension(150, 20), 16, Color.BLACK, yellow);
      this.add(qtext, BorderLayout.WEST);
  
      selectButton = new JButton("Select");
      selectButton.setPreferredSize(new Dimension(50, 20));
      selectButton.setFont(new Font("Verdana", 0, 15));
      selectButton.setBorder(BorderFactory.createEmptyBorder());
      selectButton.setFocusPainted(false);
  
      this.add(selectButton, BorderLayout.EAST);
    }

    public boolean equals(Prompt otherPrompt){
        return this.qna.equals(otherPrompt.getQNA());
    }
  
    public void changeIndex(int num) {
      this.qtext.setText(num + ""); // num to String
      this.revalidate(); // refresh
    }

    public QNA getQNA(){
      return qna;
    }
  
    public JButton getSelectButton() {
      return selectButton;
    }
  
    public boolean getState() {
      return selected;
    }
  
    // TODO: deselect every time we select a new one
    public void changeState() {
      if(!this.getState()){
        this.qtext.setBackground(green);
        selected = true;
      }
      else{
        this.qtext.setBackground(yellow);
        selected = false;
      }
      revalidate();
    }
}



public class App {
    public static void main(String[] args) throws Exception {
        new AppFrame();
    }
}
