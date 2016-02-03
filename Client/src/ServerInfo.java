/**
 * Created by bhaiya on 12/19/14.
 */
public class ServerInfo{
    private String ipAddress;
    private String portNo;
    private String id;

    public ServerInfo(String serverDetails){
        String[] tokens= serverDetails.split(" ");
        id= tokens[0];
        ipAddress=tokens[1];
        portNo=tokens[2];
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPortNo() {
        return portNo;
    }

    public void setPortNo(String portNo) {
        this.portNo = portNo;
    }

}
