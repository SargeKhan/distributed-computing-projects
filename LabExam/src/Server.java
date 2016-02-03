import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bhaiya on 1/26/15.
 */
public class Server {

    public static List<ClientInfo> clients= new ArrayList<ClientInfo>();
    public static final int portNo= 4545;
    public static void main(String args[]) throws IOException {
        System.out.println("Server listening at port"+ portNo);
        DatagramSocket socket= new DatagramSocket(portNo);
        while(true){
            byte[] recData= new byte[1024];
            DatagramPacket recPacket= new DatagramPacket(recData,recData.length);
            socket.receive(recPacket);

            if(!clientAlreadyExists(recPacket.getPort()))
            {
                ClientInfo cl= new ClientInfo(recPacket.getPort());
                clients.add(cl);
            }
            UDPPacket parsedPacket= new UDPPacket(recPacket.getData());
            System.out.println(parsedPacket.getPacketData());
            switch (Integer.valueOf(parsedPacket.getPacketType())){
                case UDPPacket.PACKET_TYPE_SUBSCRIBE:{
                    addKeyWord(recPacket.getPort(),parsedPacket.getPacketData());
                    break;
                }
                case UDPPacket.PACKET_TYPE_MESSAGE:{
                    pushMessages(parsedPacket.getPacketData());
                    break;
                }
            }
        }
    }

    private static boolean pushMessages(String message) throws IOException{
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");
        UDPPacket packet= new UDPPacket();
        packet.setPacketType(String.valueOf(UDPPacket.PACKET_TYPE_PUSH));
        packet.setPacketData(message);
        byte[] sendData= packet.toString().getBytes();
        for(ClientInfo client: clients){
            for(String keyWord: client.keyWords){
                if(message.contains(keyWord)){
                    System.out.print("Port No: " +client.portNo);
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), client.portNo);
                    clientSocket.send(sendPacket);
                    break;
                }
            }
        }
        return true;
    }

    private static boolean clientAlreadyExists(int portNo){
        for(ClientInfo client: clients){
            if(client.portNo==portNo)
                return true;
        }
        return false;
    }
    private static boolean addKeyWord(int portNo, String keyWord){
        for(ClientInfo client: clients){
            if(client.portNo==portNo)
            {
                client.keyWords.add(keyWord);
                return true;
            }
        }
        return false;
    }
}
