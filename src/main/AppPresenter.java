public class AppPresenter{
    AppFrame sayItApp;
    LoginWindow loginWindow;
    LoginDetailHandler loginDetailHandler;

    AppPresenter(){
        loginDetailHandler = new LoginDetailHandler();

        //if theres login info saved, go straight into the app
        if(loginDetailHandler.getUserInfoFromFile().getUsername() != null){
            new AppFrame(loginDetailHandler.getUserInfoFromFile());
            return;
        }

        //no login info was found, open the login window.
        loginWindow = new LoginWindow(this);
    }

    //called in the LoginPanel of App.java
    public void launchApp(UserInfo userInfo){
        new AppFrame(userInfo);
        loginWindow.setVisible(false);
    }

    
   
}