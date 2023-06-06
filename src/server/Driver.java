public class Driver {
    public static void main(String[] args){
        MongoHandler mongo = new MongoHandler(); 
        // mongo.addUser("foo","bar");
        // mongo.addUser("baz","buzz");
        // mongo.addPrompt("foo","question","hi there", "abc");
        // mongo.addPrompt("foo","question","hi there", "abc");
        // mongo.addPrompt("foo","question","hi there", "abc");
        // mongo.updateUserEmailInfo("foo", "google", "1", "example@gmail.com", "hunter2");
        // mongo.updateUserEmailInfo("foo", "bing", "1", "example@gmail.com", "hunter2");
        mongo.getAllUserPrompts("baz");
        mongo.deletePrompt("foo", "647dd1d0409aa55cfb3df284");  
        mongo.addUser("aaa","aaa");      

        mongo.addPrompt("foo", "aa", "abc", "jifij");
        mongo.addPrompt("jacob", "aa", "abc", "jifij");
    }
}
