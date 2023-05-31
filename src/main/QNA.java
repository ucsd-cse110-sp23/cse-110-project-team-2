public class QNA {
    private static final String DELIM = " ";
    //REFACTOR QNA TO INCLUDE PROMPT
    private String question;
    private String answer;
    private PromptType promptType = PromptType.NOCOMMAND;

    public QNA(String question, String answer, PromptType command){
        this.question = question;
        this.answer = answer;
        this.promptType = command;
    }

    public boolean equals(QNA otherQuestion){
        return this.question.equals(otherQuestion.getQuestion()) 
            && this.answer.equals(otherQuestion.getAnswer());
    }

    public void setCommand(PromptType pt) {
        promptType = pt;
    }

    public void setAnswer(String answer){
        this.answer = answer;
    }

    public void setQuestion(String question){
        this.question = question;
    }

    public PromptType getCommand() {
        return promptType;
    }

    public String getQuestion(){
        return question;
    }

    public String getAnswer(){ 
        return answer;
    }

    public String toString() { 
        return this.promptType + DELIM + this.question + DELIM + this.answer;
    }
}