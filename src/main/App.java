import java.util.*;
// gui libraries
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.JList;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.StyleContext;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/*
 * Recording panel with buttons start and stop,with recording label
 */
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

/*
 * Total panel with the prompts/answers and the buttons?
 */
class QnaPanel extends JPanel {

    JButton startButton;
    JButton stopButton;

    QnaDisplay qnaDisplay;
    
    RecordPanel recordPanel;

    APIHandler apiHandler;
    EmailSetupPanel emailSetupPanel = new EmailSetupPanel();
/*
    private AudioHandler audioHandler;
    private GPTHandler gptHandler;
    private WhisperHandler whisperHandler;
    private static String APIKey = "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";
*/
    QnaPanel() {
        this.setPreferredSize(new Dimension(600, 800));
        this.setLayout(new BorderLayout());
        this.setBackground(Color.RED);
    }
    
    HistoryManager historyManager;
    GUIMediator guiMediator; 

    Color yellow = new Color(229, 239, 193);
    Color green = new Color(162, 213, 171);
    Color turquoise = new Color(57, 174, 169);
    Color blue = new Color(85, 123, 131);

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

        addListeners();
    }

    /*
     * Adds listeners for the start and stop recordings button
     */
    public void addListeners() {
        startButton.addActionListener(
            (ActionEvent e) -> {
                System.out.println("START PRESSED");
                
                //show the recording text then start recording
                recordPanel.showRecording();
                apiHandler.startRecording();
          }
        );
        stopButton.addActionListener(
        (ActionEvent e) -> {
            System.out.println("STOP PRESSED");
            recordPanel.hideRecording();
            apiHandler.stopRecording();
            //audioHandler.stopRecording();
            QNA gptPrompt = apiHandler.audioToAnswer();
            System.out.println(gptPrompt.getQuestion());

            if(gptPrompt.getCommand() == PromptType.SETUPEMAIL){
                emailSetupPanel.setupEmail();
                return;
            }

            if(gptPrompt.getCommand() == PromptType.CREATEEMAIL && !emailSetupPanel.isEmailSetup()){
                emailSetupPanel.setupEmail();
                return;
            }
          
            //user tries to send an email without having an email set up.
            if(historyManager.getSelected() != null && historyManager.getSelected().getPromptType() != PromptType.CREATEEMAIL && gptPrompt.getCommand() == PromptType.SENDEMAIL){
                //User says "send email" when they don't have an email selected
                gptPrompt = new QNA(gptPrompt.getQuestion(), "When trying to send an email, please select a prompt labeled \"Create email\"", PromptType.SENDEMAIL);
            }
            else if(gptPrompt.getCommand() == PromptType.SENDEMAIL && !emailSetupPanel.isEmailSetup()){
                gptPrompt.setAnswer("Please set up an email by saying \"set up email\" before trying to send an email.");
            }
            else if(historyManager.getSelected() != null && historyManager.getSelected().getPromptType() == PromptType.CREATEEMAIL && gptPrompt.getCommand() == PromptType.SENDEMAIL){
                //User says "send email" with an email selected
                MailSendingHandler msh = new MailSendingHandler(historyManager.getSelected().getQNA(), gptPrompt, emailSetupPanel);
                try{
                    msh.sendEmail();
                    gptPrompt.setAnswer("Email Successfully sent.");
                } catch(Exception ex){
                    ex.printStackTrace();
                    gptPrompt.setAnswer("There was an error sending the email.");
                }

            }

            else if(gptPrompt.getCommand() == PromptType.QUESTION) {
                guiMediator.changeQnaDisplayText(gptPrompt);
                 //add the prompt to the history manager/get prompt to display in history
                Prompt newPrompt = historyManager.addToHistory(gptPrompt);
                guiMediator.addHistoryListPrompt(newPrompt);            
                revalidate();  
                return;
            }
            else if(gptPrompt.getCommand() == PromptType.DELETEPROMPT) {
                guiMediator.clearQNADisplayText();
                guiMediator.deletePrompt();
                guiMediator.changeQnaDisplayText(gptPrompt);
                return;
            }
            else if(gptPrompt.getCommand() == PromptType.DELETEALL) {
                guiMediator.clearQNADisplayText();
                guiMediator.deleteAllPrompts();
                guiMediator.changeQnaDisplayText(gptPrompt);
                return;
            }

            //update the display to show the qna
            guiMediator.changeQnaDisplayText(gptPrompt);

            //add the prompt to the history manager/get prompt to display in history
            Prompt newPrompt = historyManager.addToHistory(gptPrompt);
            guiMediator.addHistoryListPrompt(newPrompt);            
            revalidate();                
            }
        );
    }
}

/*
 * Panel type to generically display content
 */
class ContentPanel extends JPanel {
    private static final int MIN_HEIGHT = 35;
    private String title, content;
    private TextPane titlePane, contentPane;

    ContentPanel(String title, String content, 
                 Color titleColor, Color titlePaneColor, 
                 Color contentColor,  Color contentPaneColor, 
                 int height, int width) {
        this.setPreferredSize(new Dimension(width, height));
        this.setLayout(new BorderLayout());

        this.title = title;
        this.content = content;

        titlePane = new TextPane(title, new Dimension(width, MIN_HEIGHT), 20, titleColor, titlePaneColor);
        contentPane = new TextPane(content, new Dimension(width, height-MIN_HEIGHT), 20, contentColor, contentPaneColor);
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

class EmailSetupPanel extends JPanel{
    private JTextField smtpHostField;
    private JTextField smtpPortField;
    private JTextField emailField;
    private JTextField passwordField;

    public EmailSetupPanel(){
        this.setLayout(new GridLayout(4,1));
        smtpHostField = new JTextField(20);
        smtpPortField = new JTextField(20);
        emailField = new JTextField(20);
        passwordField = new JTextField(20);

        this.add(new JLabel("SMTP HOST"));
        this.add(smtpHostField);
        this.add(new JLabel("SMTP PORT"));
        this.add(smtpPortField);
        this.add(new JLabel("YOUR EMAIL"));
        this.add(emailField);
        this.add(new JLabel("YOUR EMAIL PASSWORD"));
        this.add(passwordField);
    }

    public String getSmtpHostFieldContent(){
        return smtpHostField.getText();
    }

    public String getSmtpPortFieldContent(){
        return smtpPortField.getText();
    }

    public String getEmailFieldContent(){
        return emailField.getText();
    }

    public String getPasswordFieldContent(){
        return passwordField.getText();
    }

    public void setSmtpHostFieldContent(String s){
        smtpHostField.setText(s);
    }

    public void setSmtpPortFieldContent(String s){
        smtpPortField.setText(s);
    }

    public void setEmailFieldContent(String s){
        emailField.setText(s);
    }

    public void setPasswordFieldContent(String s){
        passwordField.setText(s);
    }

    public boolean isEmailSetup(){
        String EMPTY_STRING = "";
        return !getSmtpHostFieldContent().equals(EMPTY_STRING) && !getSmtpPortFieldContent().equals(EMPTY_STRING) && !getEmailFieldContent().equals(EMPTY_STRING) && !getPasswordFieldContent().equals(EMPTY_STRING);
    }

    

    public void setupEmail(){
        String oldSmtpHost = getSmtpHostFieldContent();
        String oldSmtpPort = getSmtpPortFieldContent();
        String oldEmail = getEmailFieldContent();
        String oldPassword = getEmailFieldContent();

        int result = JOptionPane.showConfirmDialog(null, this, "setup email", JOptionPane.OK_CANCEL_OPTION);
                
        if(result == JOptionPane.OK_OPTION){
            //idk
        } else{
            //user canceled setup, reverting to old info
            setSmtpHostFieldContent(oldSmtpHost);
            setSmtpPortFieldContent(oldSmtpPort);
            setEmailFieldContent(oldEmail);
            setPasswordFieldContent(oldPassword);
        }

        return;
    }
}

/*
 * class to be used as a text pane
 */
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


/*
 * Display for the question and answer
 */ 
class QnaDisplay extends JPanel {
    private static final int WIDTH = 600, HEIGHT = 600;

    Color yellow = new Color(229, 239, 193);
    Color green = new Color(162, 213, 171);
    Color turquoise = new Color(57, 174, 169);
    Color blue = new Color(85, 123, 131);

    ContentPanel commandContentPanel;
    ContentPanel questionContentPanel;
    ContentPanel answerContentPanel;
    QnaDisplay(GUIMediator guiM) {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setLayout(new BorderLayout());
        this.setBackground(Color.GREEN);
        
        commandContentPanel = new ContentPanel("Command", "DEFAULT_COMMAND",
                                                Color.BLACK, turquoise, Color.BLACK, yellow,
                                                HEIGHT/4, WIDTH);
        questionContentPanel = new ContentPanel("Question", "DEFAULT_QUESTION", 
                                                Color.BLACK, turquoise, Color.DARK_GRAY, yellow,
                                                HEIGHT/4, WIDTH);
        answerContentPanel =  new ContentPanel("Answer", "DEFAULT_ANSWER",
                                               Color.BLACK, turquoise, Color.BLUE, yellow,
                                               HEIGHT/2, WIDTH);

        JPanel qc = new JPanel(new GridLayout(2,1));
        qc.add(commandContentPanel);
        qc.add(questionContentPanel);

        this.add(qc, BorderLayout.NORTH);
        this.add(answerContentPanel, BorderLayout.CENTER);

        guiM.setQnaDisplay(this);
        
    }

    public void setQNASection(QNA qna){
        commandContentPanel.setContent(qna.getCommand().toString());
        questionContentPanel.setContent(qna.getQuestion());
        answerContentPanel.setContent(qna.getAnswer());
    }
}

/*
 * panel that holds all the prompts in the prompt history
 */
class HistoryList extends JPanel {
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
        System.out.println("this is the history list");
        this.historyManager = histManager;
        this.guiMediator = guiM;
        loadHistory();
    }

    public void updateLayout() {
        int numRows = (count < MIN_ROWS) ? MIN_ROWS : count;
        this.layout.setRows(numRows);
    }

    public Prompt getSelectedPrompt(){
        return historyManager.getSelected();
    }

    /*
     * Add a prompt to the history list
     */
    public void addPrompt(Prompt prompt){
        count++;
        updateLayout();
        this.add(prompt);
        JButton selectButton = prompt.getSelectButton();

        //Add a listener to the button on the prompt to select the prompt for viewing
        selectButton.addActionListener(
            (ActionEvent e2) -> {
                //TODO: update qnadisplay to show the selected prompt and answer
                historyManager.setSelected(prompt);
                guiMediator.changeQnaDisplayText(prompt.getQNA());
                revalidate(); // Updates the frame
            }
        );
    }

    /*
     * remove a prompt from the history list
     */
    public void removePrompt(Prompt prompt){
        count--;
        updateLayout();
        this.remove(prompt);
        revalidate();
        //repaint();
    }

    public void deletePrompt(){
        Prompt toDelete = historyManager.getSelectedToDelete();
        if (toDelete != null) {
            System.out.println("Deleting prompt" + toDelete.getQNA().toString());
            this.removePrompt(toDelete);
            repaint();
            revalidate();
        }
    }

    public void deleteAllPrompts() {
        for (Component prompt : this.getComponents()) {
            if (prompt instanceof Prompt) {
                historyManager.setSelected((Prompt) prompt);
                deletePrompt();
            }
        }
        historyManager.clearHistory();
    }

    /*
     * Load all the prompts from the history.txt file. delegates to history manager
     */
    private void loadHistory(){
        ArrayList<Prompt> promptList = historyManager.getHistoryList();
        for(Prompt prompt : promptList){
            addPrompt(prompt);
        }
        count = promptList.size();
    }
}


/*
 * Wrapper panel for history
 */
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

        // historyButtonPanel = new HistoryButtonPanel();
        // this.add(historyButtonPanel, BorderLayout.SOUTH);

        // this.deleteSingleButton = historyButtonPanel.getDeleteSingleButton();
        // this.deleteAllButton = historyButtonPanel.getDeleteAllButton();

        this.guiM = guiM;

        this.historyManager = histManager;
        // addListeners();

    }

    /*
     * OLD CODE FOR HISTORY PANEL; WE DONT NEED BUTTONS ANYMORE
     * add listeners to the delete selected, and the delete all buttons
     */
    // public void addListeners(){
    //     deleteSingleButton.addActionListener(
    //         (ActionEvent e) -> {
    //             Prompt toDelete = historyManager.getSelectedToDelete();
    //             if (toDelete != null) {
    //                 System.out.println("Deleting prompt" + toDelete.getQNA().toString());
    //                 historyList.removePrompt(toDelete);
    //                 guiM.clearQNADisplayText();
    //                 repaint();
    //                 revalidate();
    //             }
    //         }
    //     );
    
    //     deleteAllButton.addActionListener(
    //         (ActionEvent e) -> {
    //             for (Component prompt : historyList.getComponents()){
    //                 if (prompt instanceof Prompt) {
    //                     historyList.removePrompt((Prompt) prompt);
    //                     repaint();
    //                     revalidate();
    //                 }
    //             }
    //             historyManager.clearHistory();
    //             guiM.clearQNADisplayText();
    //             repaint();
    //             revalidate();
    //         }
    //     );
    // }
}


/*
 * Panel for delete buttons
 */ 
class HistoryButtonPanel extends JPanel {
    private JButton deleteAll;
    private JButton deleteSingle;

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

/*
 * Appframe that holds all the panels
 */
class AppFrame extends JFrame {

    AppFrame() {
        this.setTitle("Application");
        this.setSize(800, 800);
        this.setBackground(Color.DARK_GRAY);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close on exit

        String username = "fpeng";


        GUIMediator guiMediator =  new GUIMediator();
        HistoryManager historyManager = new HistoryManager("history.txt", username);

        HistoryPanel hp = new HistoryPanel(guiMediator, historyManager);
        QnaPanel qp = new QnaPanel(guiMediator, historyManager);
        this.add(hp, BorderLayout.WEST);
        this.add(qp, BorderLayout.CENTER);
        this.setVisible(true); // Make visible
    }
}

/*
 * prompts that are added to the history list. Each one encodes a qna
 */
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

      String promptText = qna.getCommand().toString() + "\n" + qna.getQuestion();
      qtext = new TextPane(promptText, new Dimension(150, 20), 16, Color.BLACK, yellow);
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

    public PromptType getPromptType(){
        return qna.getCommand();
    }
  
    public JButton getSelectButton() {
      return selectButton;
    }
  
    public boolean getState() {
      return selected;
    }
    
    /*
     * Change the color depending on if the prompt is selected
     */
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


/*
 * Run the app!
 */
public class App {
    public static void main(String[] args) throws Exception {
        new AppFrame();
    }
}
