import java.util.*;
// gui libraries
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.JList;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.StyleContext;

import org.json.JSONObject;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import javax.sound.sampled.*;

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
    EmailSetupPanel emailSetupPanel;

/*
    private AudioHandler audioHandler;
    private GPTHandler gptHandler;
    private WhisperHandler whisperHandler;
    private static String APIKey = "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";
*/
    QnaPanel(String username) {
        this.setPreferredSize(new Dimension(600, 800));
        this.setLayout(new BorderLayout());
        this.setBackground(Color.RED);
        this.emailSetupPanel = new EmailSetupPanel(username); 
    }
    
    HistoryManager historyManager;
    GUIMediator guiMediator; 

    Color yellow = new Color(229, 239, 193);
    Color green = new Color(162, 213, 171);
    Color turquoise = new Color(57, 174, 169);
    Color blue = new Color(85, 123, 131);

    private String username;

    QnaPanel(GUIMediator guiM, HistoryManager histManager, String username) {
        this.setPreferredSize(new Dimension(600, 300));

        this.setLayout(new BorderLayout());
        this.setBackground(yellow);

        this.emailSetupPanel = new EmailSetupPanel(username);

        qnaDisplay = new QnaDisplay(guiM);
        recordPanel = new RecordPanel();
        this.add(qnaDisplay, BorderLayout.CENTER);
        this.add(recordPanel, BorderLayout.SOUTH);

        this.username = username;

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
            QNA gptPrompt = apiHandler.audioToAnswer(username);
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
                if (gptPrompt.getQuestion().contains(" at ")){
                    gptPrompt.setQuestion(gptPrompt.getQuestion().replace(" at ", "@"));
                }
                try{
                    msh.sendEmail();
                    gptPrompt.setAnswer("Email Successfully sent.");
                } catch(Exception ex){
                    ex.printStackTrace();
                    gptPrompt.setAnswer("There was an error sending the email.");
                }

            }

            else if(gptPrompt.getCommand() == PromptType.QUESTION){
                guiMediator.changeQnaDisplayText(gptPrompt);
                 //add the prompt to the history manager/get prompt to display in history
                Prompt newPrompt = historyManager.addToHistory(gptPrompt);
                guiMediator.addHistoryListPrompt(newPrompt);
                try {
                    apiHandler.textToSpeech(gptPrompt);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField displayNameField;
    private JTextField smtpHostField;
    private JTextField smtpPortField;
    private JTextField emailField;
    private JTextField passwordField;
    private RequestHandler requestHandler;
    private String username;

    public EmailSetupPanel(String username){
        this.setLayout(new GridLayout(7,1));
        smtpHostField = new JTextField(20);
        smtpPortField = new JTextField(20);
        emailField = new JTextField(20);
        passwordField = new JTextField(20);
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        displayNameField = new JTextField(20);
        this.username = username;
        requestHandler = new RequestHandler();

        checkDBForEmailCredentials();

        this.add(new JLabel("FIRST NAME"));
        this.add(firstNameField);
        this.add(new JLabel("LAST NAME"));
        this.add(lastNameField);
        this.add(new JLabel("DISPLAY NAME"));
        this.add(displayNameField);
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

    public String getFirstNameFieldContent(){
        return firstNameField.getText();
    }

    public String getLastNameFieldContent(){
        return lastNameField.getText();
    }

    public String getDisplayNameFieldContent(){
        return displayNameField.getText();
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

    public void setFirstNameFieldContent(String s){
        firstNameField.setText(s);
    }

    public void setLastNameFieldContent(String s){
        lastNameField.setText(s);
    }

    public void setDisplaynameFieldContent(String s){
        displayNameField.setText(s);
    }



    public boolean isEmailSetup(){
        String EMPTY_STRING = "";
        return !getSmtpHostFieldContent().equals(EMPTY_STRING) 
        && !getSmtpPortFieldContent().equals(EMPTY_STRING) 
        && !getEmailFieldContent().equals(EMPTY_STRING) 
        && !getPasswordFieldContent().equals(EMPTY_STRING)
        && !getFirstNameFieldContent().equals(EMPTY_STRING)
        && !getLastNameFieldContent().equals(EMPTY_STRING)
        && !getDisplayNameFieldContent().equals(EMPTY_STRING);
    }

    public void checkDBForEmailCredentials(){
        
        try{
            String usernameJSON = requestHandler.UsernameToJSON(username);
            String userInfo = requestHandler.sendHttpRequest(usernameJSON, "POST", "email");

            System.out.println(userInfo);

            JSONObject emailInfoJSON = new JSONObject(userInfo);

            setFirstNameFieldContent(emailInfoJSON.get("firstName").toString());
            setLastNameFieldContent(emailInfoJSON.get("lastName").toString());
            setDisplaynameFieldContent(emailInfoJSON.get("displayName").toString());
            setSmtpHostFieldContent(emailInfoJSON.get("smtpHost").toString());
            setSmtpPortFieldContent(emailInfoJSON.get("smtpPort").toString());
            setEmailFieldContent(emailInfoJSON.get("email").toString());
            setPasswordFieldContent(emailInfoJSON.get("emailPassword").toString());
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setupEmail(){
        String oldSmtpHost = getSmtpHostFieldContent();
        String oldSmtpPort = getSmtpPortFieldContent();
        String oldEmail = getEmailFieldContent();
        String oldPassword = getPasswordFieldContent();

        int result = JOptionPane.showConfirmDialog(null, this, "setup email", JOptionPane.OK_CANCEL_OPTION);
                
        if(result == JOptionPane.OK_OPTION){
            System.out.println("CLICKED OK");
            if(isEmailSetup()){
                System.out.println("all fields were filled");
                String emailDetailsJSON = requestHandler.SetupEmailToJSON(
                    username, 
                    getFirstNameFieldContent(), 
                    getLastNameFieldContent(), 
                    getDisplayNameFieldContent(), 
                    getSmtpHostFieldContent(), 
                    getSmtpPortFieldContent(), 
                    getEmailFieldContent(), 
                    getPasswordFieldContent());

                System.out.println("json?\n" + emailDetailsJSON);
                
                try{
                    requestHandler.sendHttpRequest(emailDetailsJSON, "PUT", "email");
                } catch(Exception e){

                }

            } else {
                JOptionPane.showMessageDialog(null, "Some fields were left empty, email has not been fully set up.");
            }
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

    PlaySound playSound;

    AppFrame(UserInfo userInfo) {
        this.setTitle("Application");
        this.setSize(800, 800);
        this.setBackground(Color.DARK_GRAY);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close on exit

        System.out.println("CONSTRUCTING WITH USER" + userInfo); 

        GUIMediator guiMediator =  new GUIMediator();
        HistoryManager historyManager = new HistoryManager(userInfo.getUsername());
        HistoryPanel hp = new HistoryPanel(guiMediator, historyManager);
        QnaPanel qp = new QnaPanel(guiMediator, historyManager, userInfo.getUsername());
        this.add(hp, BorderLayout.WEST);
        this.add(qp, BorderLayout.CENTER);
        this.setVisible(true); // Make visible

        playSound = new PlaySound();

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                playSound.close();
            }
        });
    }

}

//Kinda MVP, Model is the Login/Create account windows, Kinda does both the View/Presenter job.
class LoginWindow extends JFrame {
    
    JLabel title;
    LoginPanel loginPanel;
    CreateAccountPanel createAccountPanel;
    LoginDetailHandler loginDetailHandler;
    AppPresenter appPresenter;

    LoginWindow(AppPresenter appPresenter) {
        
        this.setSize(500,500);
        this.setBackground(Color.DARK_GRAY);
        this.setTitle("Log Into SayIt!");

        GridLayout gl = new GridLayout(1, 1);
        this.setLayout(gl);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Observe the panels for when the user changes what they wanna do
        loginPanel = new LoginPanel(this, appPresenter);
        createAccountPanel = new CreateAccountPanel(this);

        this.add(loginPanel); 
        this.setVisible(true);
    }

    public void switchToCreateAccount(){
        this.remove(loginPanel);
        this.add(createAccountPanel);
        revalidate();
        repaint();
    }

    public void switchToLogin(){
        this.remove(createAccountPanel);
        this.add(loginPanel);
        revalidate();
        repaint();
    }
}

class LoginPanel extends JPanel {
    JTextField usernameField; 
    JTextField passwordField;
    JLabel usernameLabel;
    JLabel passwordLabel;
    JButton loginButton;
    JButton createAccountButton;
    JCheckBox keepMeLoggedInBox;
    LoginWindow lw; 
    LoginDetailHandler loginDetailHandler;
    AppPresenter appPresenter;

    RequestHandler requestHandler;


    LoginPanel(LoginWindow lw, AppPresenter appPresenter) {
        this.setPreferredSize(new Dimension(100,400));

        GridLayout layout = new GridLayout(7,1);
        layout.setVgap(5);
        layout.setHgap(10);

        this.setLayout(layout);

        usernameField = new JTextField(20);
        passwordField = new JTextField(20);
        loginButton = new JButton("Log In");
        createAccountButton = new JButton("Create Account");
        usernameLabel = new JLabel("Username");
        passwordLabel = new JLabel("Password");
        usernameLabel.setHorizontalAlignment(JLabel.CENTER);
        passwordLabel.setHorizontalAlignment(JLabel.CENTER);
        keepMeLoggedInBox = new JCheckBox("Keep me logged in");
        keepMeLoggedInBox.setHorizontalAlignment(JCheckBox.CENTER);
        loginDetailHandler = new LoginDetailHandler();

        requestHandler = new RequestHandler();

        this.appPresenter = appPresenter;

        this.add(usernameLabel);
        this.add(usernameField);
        this.add(passwordLabel);
        this.add(passwordField);
        this.add(loginButton);
        this.add(createAccountButton);
        this.add(keepMeLoggedInBox);

        this.lw = lw;

        addListeners();
    }

    public boolean isValidLogin(String username, String password){
        String userJSON = requestHandler.CredentionalsToJSON(username, password);
            try{
                System.out.println("Sending the req w body" + userJSON);
                String response = requestHandler.sendHttpRequest(userJSON, "POST", "login");
                JSONObject responseJSON = new JSONObject(response);

                System.out.println("RESPONSE \n" + response);

                if(responseJSON.has("error")){
                    throw new Exception(responseJSON.get("error").toString());
                }

                return true;
            } catch(Exception ex){
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        return false;
    }

    public boolean allFieldsFilledIn(){
        return (!usernameField.getText().equals("") &&
        !passwordField.getText().equals(""));
    }

    public void addListeners(){
        createAccountButton.addActionListener((ActionEvent e) -> {
            lw.switchToCreateAccount();
        });

        loginButton.addActionListener((ActionEvent e) -> {

            if(!allFieldsFilledIn()){
                JOptionPane.showMessageDialog(null, "Error: Make sure all fields are filled in.");
                return;
            }

            String username = usernameField.getText();
            String password = passwordField.getText();

            //validate login and save credentials to login.txt if they opted into it.
            if(isValidLogin(username, password)){

                if(keepMeLoggedInBox.isSelected()){
                    loginDetailHandler.saveLoginDetails(username, password);
                    System.out.println("Saved login details as \n" + loginDetailHandler.getUserInfoFromFile());
                }
                
                System.out.println("launching app with" + new UserInfo(username,password).getUsername().length());
                appPresenter.launchApp(new UserInfo(username, password));

            }
        });
    }
}

class CreateAccountPanel extends JPanel {

    JTextField usernameField; 
    JTextField passwordField;
    JTextField confirmPasswordField; 

    JLabel usernameLabel;
    JLabel passwordLabel;
    JLabel confirmPasswordLabel;

    JButton createAccountButton;

    RequestHandler requestHandler;

    //MVP
    LoginWindow lw; 

    CreateAccountPanel(LoginWindow lw) {
        this.setPreferredSize(new Dimension(100,400));

        GridLayout layout = new GridLayout(7,1);
        layout.setVgap(5);
        layout.setHgap(10);

        this.setLayout(layout);

        usernameField = new JTextField(20);
        passwordField = new JTextField(20);
        confirmPasswordField = new JTextField(20);

        requestHandler = new RequestHandler();

        createAccountButton = new JButton("Create Account");
        usernameLabel = new JLabel("Username");
        passwordLabel = new JLabel("Password");
        confirmPasswordLabel = new JLabel("Confirm Password");
        usernameLabel.setHorizontalAlignment(JLabel.CENTER);
        passwordLabel.setHorizontalAlignment(JLabel.CENTER);
        confirmPasswordLabel.setHorizontalAlignment(JLabel.CENTER);

        this.add(usernameLabel);
        this.add(usernameField);
        this.add(passwordLabel);
        this.add(passwordField);
        this.add(confirmPasswordLabel);
        this.add(confirmPasswordField);

        this.add(createAccountButton);

        this.lw = lw;
        addListeners();
    }

    public String getPasswordFieldText(){
        return passwordField.getText();
    }

    public String getConfirmPasswordFieldText(){
        return confirmPasswordField.getText();
    }

    public boolean areAllFieldsFilled(){
        return (!confirmPasswordField.getText().equals("") &&
        !passwordField.getText().equals("") &&
        !usernameField.getText().equals(""));
    }

    public void addListeners(){
        createAccountButton.addActionListener((ActionEvent e) -> {
            

            String username = usernameField.getText();

            String password = passwordField.getText();
            String passwordConfirmation = confirmPasswordField.getText();

            if(!password.equals(passwordConfirmation)){

                JOptionPane.showMessageDialog(null, "Error: The password and confirmation do not match, please try again.");
            } else if(!areAllFieldsFilled()){
                JOptionPane.showMessageDialog(null, "Error: Make sure all Username/Password fields have been filled in."); 
            }else{

                try{
                    String credentialsJSON = requestHandler.CredentionalsToJSON(username,password);
                    String res = requestHandler.sendHttpRequest(credentialsJSON,"PUT","login");
                    JSONObject responseJSON = new JSONObject(res);

                    if(responseJSON.has("error")){
                        throw new Exception(responseJSON.get("error").toString());
                    }
                }catch(Exception ex){
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                    return;
                }

                lw.switchToLogin();
                JOptionPane.showMessageDialog(null, "The account was successfully created. You may now log in with your credentials."); 
            }
            
        });
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
        SayItServer.runServer();
        new AppPresenter();
    }
}
