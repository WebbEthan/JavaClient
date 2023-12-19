public class App {
    public static void main(String[] args) throws Exception
    {
        // Starts threads
        ThreadManager.StartThreads();
        // Creates client
        new MessangerApp();
    }
}
