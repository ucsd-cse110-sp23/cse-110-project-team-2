import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import org.bson.io.OutputBuffer;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;

public class LoginDetailHandler {
    private File loginFile; 
    
    LoginDetailHandler(){
        loginFile = new File("./login.txt");
    }

    public void saveLoginDetails(String username, String password){
        
        try{
            if(!loginFile.exists()){
                loginFile.createNewFile();
            } 
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            FileWriter loginWriter = new FileWriter(loginFile, false);
            loginWriter.write(username + "\n" + password);
            loginWriter.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean doesLoginFileExist(){
        return loginFile.exists();
    }

    public UserInfo getUserInfoFromFile(){

        try{
            FileReader fr = new FileReader(loginFile);
            BufferedReader br = new BufferedReader(fr);
            
            String username = br.readLine();
            String password = br.readLine();

            return new UserInfo(username,password);
        }catch(IOException e){
            e.printStackTrace();
        }

        return null;
    }

    
}
