import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class ReplicatedFS {

    public static ArrayList<ServerInfo> mServerList= new ArrayList<ServerInfo>();
    public ReplicatedFS(String configFileName, int packetLoss) throws IOException {
        FileReader reader= new FileReader(configFileName);
        BufferedReader textReader= new BufferedReader(reader);
        String line;
        while((line=textReader.readLine())!=null)
            mServerList.add(new ServerInfo(line));

    }
    public RemoteFile open(String filename) throws IOException {
        for(ServerInfo server:mServerList)
        {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(server.getIpAddress());
            UDPPacket packet= new UDPPacket();
            packet.setPacketNo("-1");
            packet.setPacketType(String.valueOf(UDPPacket.PACKET_TYPE_OPEN));
            packet.setPacketData(filename);
            byte[] sendData= packet.toString().getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.valueOf(server.getPortNo()));
            clientSocket.send(sendPacket);
        }
        RemoteFile file= new RemoteFile();
        return file;
    }
}


