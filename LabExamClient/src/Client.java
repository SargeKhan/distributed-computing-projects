import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

/**
 * Created by bhaiya on 1/26/15.
 */
public class Client {
    public static final int SUBSCRIBE=1;
    public static final int MESSAGE=2;
    public static final int EXIT=3;
    private static DatagramSocket clientSocket;
    private static class ReceivedSubs implements Runnable{


        @Override
        public void run() {
            System.out.print("Runnable Started");
            while(true){

                try {
                    byte[] data= new byte[1024];
                    System.out.print(clientSocket.getLocalPort() + clientSocket.getPort());
                    DatagramPacket receiveSub= new DatagramPacket(data,data.length,InetAddress.getLocalHost(),clientSocket.getLocalPort());
                    clientSocket.receive(receiveSub);
                    UDPPacket packet= new UDPPacket(receiveSub.getData());
                    System.out.println("Packet Type: "+ packet.getPacketType()+ "\n"+"Data :" +packet.getPacketData());
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void main(String args[]) throws IOException {

        clientSocket= new DatagramSocket();
        Scanner scanner= new Scanner(System.in);
        Thread printData= new Thread(new ReceivedSubs());
        printData.start();
        int decision=0;
        System.out.println("Press 1 to Subscribe, 2 to send Message, 3 to exit");
        while(decision!=3){
            System.out.print("Enter your option");
            decision=Integer.valueOf(scanner.nextLine());
            switch (decision){
                case SUBSCRIBE:{
                    InetAddress IPAddress = InetAddress.getByName("localhost");
                    UDPPacket packet= new UDPPacket();
                    packet.setPacketType(String.valueOf(UDPPacket.PACKET_TYPE_SUBSCRIBE));
                    System.out.println("Enter Keyword");
                    String keyWord= scanner.nextLine();
                    packet.setPacketData(keyWord);
                    byte[] sendData= packet.toString().getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), 4545);
                    clientSocket.send(sendPacket);
                    break;
                }
                case MESSAGE:{
                    InetAddress IPAddress = InetAddress.getByName("localhost");
                    UDPPacket packet= new UDPPacket();
                    packet.setPacketType(String.valueOf(UDPPacket.PACKET_TYPE_MESSAGE));
                    System.out.println("Enter message");
                    String message= scanner.nextLine();
                    packet.setPacketData(message);
                    byte[] sendData= packet.toString().getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), 4545);
                    clientSocket.send(sendPacket);
                    break;
                }
                case EXIT:{
                    break;
                }

            }
        }
    }
}
