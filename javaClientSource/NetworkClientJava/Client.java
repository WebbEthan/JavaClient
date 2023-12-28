
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.io.*;
public class Client
{
    private Boolean _active = false;
    public Boolean IsActive() { return _active; }
    private String _ip;
    private int _port;
    public String CurrentMatchCode;
    public Boolean IsHost = false;
    public String Username = "";
    @FunctionalInterface
    protected interface PacketScript
    {
        void Invoke(Packet packet, ProtocalType protocalType);
    }
    // A store of methods defined in the client class to be run by the network
    protected Map<Byte, PacketScript> Handles = new HashMap<Byte, PacketScript>();;

    public void Connect(String ip, int port)
    {
        _ip = ip;
        _port = port;
        try 
        {
            // Starts TCP
            _active = true;
            _tcpProtocal = new _tcp(ip, port, this);   
        } catch (Exception ex)
        {
            System.err.println(ex);
        }
    }
    // Sends data through the TCP an UDP classes
    public void SendData(Packet packet, ProtocalType type)
    {
        try
        {

            if (type == ProtocalType.TCP && _tcpProtocal != null)
            {
                packet.InsertBytes(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(packet.Data.length).array(), 0);
                packet.PrepForSending();
                _tcpProtocal.SendData(packet);
            }
            else if (_udpProtocal != null)
            {
                packet.PrepForSending();
                _udpProtocal.SendData(packet);
            }
        }
        catch (Exception ex) 
        {
            System.err.println("Error sending data: " + ex);
        }
    }
    // Called when another client joins the match you are in.
    protected void AddClient(String id) {}
    // Called when another client leaves the match you are in.
    protected void RemoveClient(String id) {}
    // Called when you successfully authenticate to the server.
    protected void OnAuthentication() {}
    // Called when you join a match
    protected void OnMatchJoin() {}
    // Called when you are kicked from a match
    protected void OnMatchKicked() {}
    // Called when the sockets are closed
    protected void OnDisconnect() {}
    // Tells the server to put you in a random match
    public void JoinMatch()
    {
        Packet packet = new Packet((byte)-1);
        packet.Write("0");
        packet.Write(Username);
        SendData(packet, ProtocalType.TCP);
    }
    // Tells the server to put you in the match with the inputed code
    public void JoinMatch(String code)
    {
        Packet packet = new Packet((byte)-1);
        packet.Write(code);
        packet.Write(Username);
        SendData(packet, ProtocalType.TCP);
    }
    public void LeaveMatch()
    {
        Packet packet = new Packet((byte)-4);
        SendData(packet, ProtocalType.TCP);
    }
    // Disconnects from the server
    public void Disconnect()
    {
        if (_active)
        {
            try 
            {
                _active = false;
                _tcpProtocal.Disconnect();
                ThreadManager.SubscribedToNetworkThread.clear();
                if (_udpProtocal != null)
                {
                    _udpProtocal.Disconnect();
                }
                ThreadManager.SubscribedToNetworkUDPThread.clear();
                _tcpProtocal = null;
                _udpProtocal = null;
                OnDisconnect();
                System.out.println("Disconnected");
            } 
            catch (Exception ex)
            {
                System.err.println("Disconnection Error: " + ex);
            }
        }
    }
    // After successful connection this create the UDP class and runs authentication
    private void _connectionCallback(int partialClientID)
    {
        System.out.println("Setting Up UDP");
        _udpProtocal = new _udp(_ip, _port, partialClientID, this);
    }
    private _tcp _tcpProtocal;
    private _udp _udpProtocal;
    private class _tcp
    {
        private Client _reference;
        private boolean _active = true;
        private Socket _socket;
        private DataInputStream _inputStream;
        private DataOutputStream _outputStream;
        public _tcp(String ip, int port, Client reference) throws Exception
        {
            _reference = reference;
            // Calls connect
            _socket = new Socket(ip, port);
            // starts listening to data
            _inputStream = new DataInputStream(_socket.getInputStream());
            _outputStream = new DataOutputStream(_socket.getOutputStream());
            ThreadManager.SubscribedToNetworkThread.add(() -> {_recieveData();});
        }
        public void SendData(Packet packet) throws Exception
        {
            _outputStream.write(packet.Data);
        }
        private void _recieveData()
        {
            if (_active)
            {
                try 
                {
                    // Checks for disconnection and waits for data
                    byte connectionCheck = _inputStream.readByte();
                    byte[] data;
                    // detects data and assembles data
                    if (_inputStream.available() > 0)
                    {
                        // Reads the rest of the data
                        byte[] restOfData = _inputStream.readNBytes(_inputStream.available());
                        data = new byte[restOfData.length + 1];
                        System.arraycopy(new byte[] { connectionCheck }, 0, data, 0, 1);
                        System.arraycopy(restOfData, 0, data, 1, restOfData.length);
                    }
                    else
                    {
                        data = new byte[] { connectionCheck };
                    }
                    _handleData(new Packet(data));
                }
                catch (Exception ex)
                {
                    // Disconnects
                    _reference.Disconnect();
                }
            }
        }
        private void _handleData(Packet packet)
        {
            // Moves to main thread
            ThreadManager.ToExecuteOnMainThread.add(() ->
            {
                try
                {
                    while (true)
                    {
                        if (_reference._udpProtocal == null)
                        {
                            // Runs authentication
                            System.err.println(packet.ReadString());
                            _reference._connectionCallback(packet.ReadInt());
                            break;
                        }
                        else
                        {
                            if (packet.PacketType == 0xff)
                            {
                                // Reads data when match is joined
                                _reference.CurrentMatchCode = packet.ReadString();
                                _reference.IsHost = packet.ReadBool();
                                // Add Clients already in match
                                int connectedUsers = packet.ReadInt();
                                for (int i = 0; i < connectedUsers; i++)
                                {
                                    _reference.AddClient(packet.ReadString());
                                }
                                System.out.printf("Joined Match %s\n", _reference.CurrentMatchCode);
                                ThreadManager.ToExecuteOnApplicationThread.add(() -> { _reference.OnMatchJoin(); });
                            }
                            else if (packet.PacketType == 0xfe)
                            {
                                // New Client in match data
                                _reference.AddClient(packet.ReadString());
                            }
                            else if (packet.PacketType == 0xfd)
                            {
                                // Authentication callback
                                System.out.println("Succesfully Authenticated");
                                ThreadManager.ToExecuteOnApplicationThread.add(() -> { _reference.OnAuthentication(); });
                            }
                            else if (packet.PacketType == 0xfc)
                            {
                                // Client left your match
                                _reference.RemoveClient(packet.ReadString());
                            }
                            else if (packet.PacketType ==0xfb)
                            {

                                System.out.println("You were kicked from the match reason : " + packet.ReadString());
                                _reference.OnMatchKicked();
                            }
                            else
                            {
                                // Runs what is scripted in the handle
                                Handles.get(packet.PacketType).Invoke(packet, ProtocalType.TCP);
                            }
                            // Runs packets recieved in rececion
                            byte[] data = packet.UnreadData();
                            if (data.length == 0)
                            {
                                break;
                            }
                            else
                            {
                                packet.SetData(data);
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    System.err.println("Error handling TCP data: " + ex);
                }
            });
        }
        public void Disconnect() throws IOException
        {
            _active = false;
            _socket.close();
        }
    }
    private class _udp
    {
        private Client _reference;
        private DatagramSocket _socket;
        private InetAddress _address;
        public _udp(String ip, int port, int partialClientID, Client reference)
        {
            try 
            {
                _reference = reference;
                _address = InetAddress.getByName(ip);
                _socket = new DatagramSocket();
                // Starts listening for UDP data
                ThreadManager.SubscribedToNetworkUDPThread.add(() -> {_recieveData();});
                // Send authentication
                Packet packet = new Packet((byte)0);
                packet.Write(partialClientID);
                packet.PrepForSending();
                SendData(packet);
            }
            catch (Exception ex) 
            {
                System.err.println(ex);
            }
        }
        public void SendData(Packet packet) throws Exception
        {
            DatagramPacket rawData = new DatagramPacket(packet.Data, packet.Data.length, _address, _port);
            _socket.send(rawData);
        }
        byte[] buffer = new byte[4096];
        private void _recieveData()
        {
            try
            {
                DatagramPacket rawData = new DatagramPacket(buffer, buffer.length);
                _socket.receive(rawData);
                // separate data from buffer
                byte[] data = new byte[rawData.getLength()];
                data = Arrays.copyOfRange(buffer, 0, data.length);
                // Runs handle
                Packet packet = new Packet(data);
                Handles.get(packet.PacketType).Invoke(packet, ProtocalType.UDP);
            }
            catch (Exception ex)
            {
                _reference.Disconnect();
            }
        }
        public void Disconnect()
        {
            _socket.close();
        }
    }
}
