
/**
 * Created by bhaiya on 12/20/14.
 */
public class UDPPacket {

    public static final String VAR_SPLIT="__";
    public static final int PACKET_TYPE_SUBSCRIBE=0;
    public static final int PACKET_TYPE_MESSAGE=1;
    public static final int PACKET_TYPE_PUSH=2;
    private String packetType;
    private String packetData;
    UDPPacket(byte[] bytePack){
        String stringPack= new String(bytePack);
        String arr[]= stringPack.split(VAR_SPLIT);
        packetType= arr[0];
        packetData= arr[2];
    }

    public UDPPacket() {

    }

    public String getPacketType() {
        return packetType;
    }

    public void setPacketType(String packetType) {
        this.packetType = packetType;
    }

    public String getPacketData() {
        return packetData;
    }

    public void setPacketData(String packetData) {
        this.packetData = packetData;
    }

    @Override public String toString(){
        return packetType+VAR_SPLIT+VAR_SPLIT+packetData+VAR_SPLIT;
    }
}