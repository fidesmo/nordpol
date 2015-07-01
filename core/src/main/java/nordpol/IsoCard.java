package nordpol;

import java.io.IOException;

public interface IsoCard {
    public void close() throws IOException;
    public void connect() throws IOException;
    public int getMaxTransceiveLength() throws IOException;
    public int getTimeout();
    public boolean isConnected();
    public void setTimeout(int timeout);
    public byte[] transceive(byte[] data) throws IOException;
}
