import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
public class Packet
{
    public byte PacketType;
    public byte[] Data = new byte[0];
    private int _readerPos = 0;

    public Packet(byte[] data)
    {
        PacketType = data[0];
        if (data.length > 1)
        {
            Data = Arrays.copyOfRange(data, 1, data.length-1);
        }
        //DisplayBytes(data);
    }
    public Packet(byte packetType)
    {
        PacketType = packetType;
    }
    public void SetData(byte[] data)
    {
        PacketType = data[0];
        Data = Arrays.copyOfRange(data, 1, data.length-1);
    }
    //#region writing Method
    public void Write(int value)
    {
        byte[] bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
        InsertBytes(bytes, Data.length);
    }
    public void Write(String value)
    {
        Write(value.length());
        InsertBytes(value.getBytes(), Data.length);
    }
    public void Write(boolean value)
    {

    }
    //#endregion
    //#region ReadingMethods
    public int ReadInt()
    {
        _readerPos += 4;
        return ByteBuffer.wrap(Arrays.copyOfRange(Data, _readerPos - 4, _readerPos)).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
    public String ReadString()
    {
        int length = ReadInt();
        String msg = new String(Arrays.copyOfRange(Data, _readerPos, _readerPos + length));
        _readerPos += length;
        return msg;
    }
    public boolean ReadBool()
    {
        _readerPos += 1;
        return Data[_readerPos -1] == -128;
    }
    //#endregion
    public void InsertBytes(byte[] bytes, int index)
    {
        byte[] result = new byte[Data.length + bytes.length];
        System.arraycopy(Data, 0, result, 0, index);
        System.arraycopy(bytes, 0, result, index, bytes.length);
        System.arraycopy(Data, index, result, index + bytes.length, Data.length - index);
        Data = result; 
    }
    public byte[] UnreadData()
    {
        if (_readerPos > Data.length)
        {
            return new byte[0];
        }
        return Arrays.copyOfRange(Data, _readerPos, Data.length);
    }
    public void PrepForSending()
    {
        InsertBytes(new byte[] { PacketType }, 0);
    }
}
