import java.util.*;
import java.util.List;

public final class ThreadManager
{
    private static Boolean _programActive = true;
    private static Thread _mainThread;
    public static List<Action> ToExecuteOnMainThread = new ArrayList<Action>();
    private static Thread _networkThread;
    public static List<Action> SubscribedToNetworkThread = new ArrayList<Action>();
    private static Thread _networkUDPThread;
    public static List<Action> SubscribedToNetworkUDPThread = new ArrayList<Action>();
    private static Thread _applicationThread;
    public static List<Action> ToExecuteOnApplicationThread = new ArrayList<Action>();
    public static void StartThreads()
    {
        System.out.println("Starting Threads");
        _mainThread = new Thread() 
        {
            public void run()
            {
                while (_programActive)
                {
                    try
                    {
                        while (ToExecuteOnMainThread.size() > 0)
                        {
                            ToExecuteOnMainThread.get(0).Invoke();
                            ToExecuteOnMainThread.remove(0);
                        }
                        Thread.sleep(10);
                    }
                    catch(Exception ex) 
                    {
                        System.err.println(ex);
                    }
                }
            }
        };
        _mainThread.start();
        _networkThread = new Thread() 
        {
            public void run()
            {
                while (_programActive)
                {
                    try
                    {
                        for (int i = 0; i < SubscribedToNetworkThread.size(); i++)
                        {
                            SubscribedToNetworkThread.get(i).Invoke();
                        }
                        Thread.sleep(10);
                    }
                    catch(Exception ex) 
                    {
                        System.err.println(ex);
                    }
                }
            }
        };
        _networkThread.start();
        _networkUDPThread = new Thread() 
        {
            public void run()
            {
                while (_programActive)
                {
                    try
                    {
                        for (int i = 0; i < SubscribedToNetworkUDPThread.size(); i++)
                        {
                            SubscribedToNetworkUDPThread.get(i).Invoke();
                        }
                        Thread.sleep(10);
                    }
                    catch(Exception ex) 
                    {
                        System.err.println(ex);
                    }
                }
            }
        };
        _networkUDPThread.start();
        _applicationThread = new Thread() 
        {
            public void run()
            {
                while (_programActive)
                {
                    try
                    {
                        while (ToExecuteOnApplicationThread.size() > 0)
                        {
                            ToExecuteOnApplicationThread.get(0).Invoke();
                            ToExecuteOnApplicationThread.remove(0);
                        }
                        Thread.sleep(10);
                    }
                    catch(Exception ex) 
                    {
                        System.err.println(ex);
                    }
                }
            }
        };
        _applicationThread.start();
    }
    public static void StopThreads()
    {
        _programActive = false;
    }
}
