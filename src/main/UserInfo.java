public class UserInfo {
    private String username;
    private String password;

    UserInfo(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String toString(){
        return "Username: " + username + "\n Password: " + password;
    }
}
