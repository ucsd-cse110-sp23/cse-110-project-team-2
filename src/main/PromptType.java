 //Setup email (2 or 3 words?) , delete all, delete prompt, question, create email, send email
enum PromptType {
    SETUPEMAIL,
    DELETEPROMPT,
    DELETEALL,
    QUESTION,
    CREATEEMAIL,
    SENDEMAIL,
    NOCOMMAND;

    @Override
    public String toString() {
        switch (this.ordinal()) {
            case 0:
                return "Set Up Email";
            case 1:
                return "Delete Prompt";
            case 2:
                return "Delete All";
            case 3:
                return "Question";
            case 4:
                return "Create Email";
            case 5:
                return "Send Email";
            case 6:
                return "No Command"; // ?
            default:
                return null;
        }
    }
}

