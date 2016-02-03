/**
 * Created by bhaiya on 12/20/14.
 */
public class UDPPacket {

    public static final String VAR_SPLIT="__";
    public static final int PACKET_TYPE_OPEN=0;
    public static final int PACKET_TYPE_DATA=1;
    public static final int PACKET_TYPE_PRECOMMIT=2;
    public static final int PACKET_TYPE_REQUEST_RETRANSMIT=3;
    public static final int PACKET_TYPE_PRECOMMIT_ACCEPT=4;
    public static final int PACK_TYPE_CONFIRM_COMMIT=5;
    public static final int PACKET_TYPE_ABORT=6;
    public static final int PACKET_TYPE_CLOSE=7;
    private String packetType;
    private String packetData;
    private String packetNo;
    UDPPacket(byte[] bytePack){
        String stringPack= new String(bytePack);
        String arr[]= stringPack.split(VAR_SPLIT);
        packetType= arr[0];
        packetNo= arr[1];
        if(packetType.equals(String.valueOf(PACKET_TYPE_DATA)))     //Packet Data = data + offset FOR DATATYPEPACKET
            packetData= arr[2]+VAR_SPLIT+arr[3];
        else
            packetData= arr[2];

    }

    public UDPPacket() {

    }

    public String getPacketNo() {
        return packetNo;
    }

    public void setPacketNo(String packetNo) {
        this.packetNo = packetNo;
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
        return packetType+VAR_SPLIT+packetNo+VAR_SPLIT+packetData+VAR_SPLIT;
    }
}
