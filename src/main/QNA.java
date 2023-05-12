public class QNA {
    private String question;
    private String answer;

    public QNA(String question, String answer){
        this.question = question;
        this.answer = answer;
    }

    public boolean equals(QNA otherQuestion){
        return this.question.equals(otherQuestion.getQuestion()) && this.answer.equals(otherQuestion.getAnswer());
    }


    public void setAnswer(String answer){
        this.answer = answer;
    }

    public String getQuestion(){
        return question;
    }

    public String getAnswer(){ 
        return answer;
    }

    public String toString(){
        return this.question + " " + this.answer;
    }
}