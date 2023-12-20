import java.util.Scanner;

public class MessangerApp extends Client
{
    public MessangerApp()
    {
        // Set Handles
        Handles.put((byte)0, this::_msg);
        // Call Connect
        Connect("127.0.0.1", 25578);
    }
    protected void OnAuthentication()
    {
        // Joins match
        JoinMatch();
    }
    protected void OnDisconnect()
    {
        ThreadManager.StopThreads();
    }
    protected void OnMatchJoin()
    {
        System.out.println("Write a message or type /quit to disconnect.");
        while (true)
        {
            Scanner consoleReader = new Scanner(System.in);
            String msg = consoleReader.nextLine();
            if (!msg.isEmpty())
            {
                if (msg == "/quit")
                {
                    Disconnect();
                    consoleReader.close();
                    return;
                }
                Packet packet = new Packet((byte)0);
                packet.Write(msg);
                SendData(packet, ProtocalType.TCP);
            }
        }
        
    }
    private void _msg(Packet packet, ProtocalType protocalType)
    {
        System.out.println(packet.ReadString());
    }
}
