import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class FSServer {


    private static boolean mPackList[];
    private static String mPath;
    private static RandomAccessFile mTempFile;
    private static String mFilename;

    private static String mTempFilename="temp.txt";
    static boolean openFile(String filename){
        try {
            mTempFile= new RandomAccessFile(mPath+mTempFilename,"rw");
            mFilename= filename;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void main(String args[]) throws IOException {
        mPath= args[0];

        mPackList= new boolean[128];
        for(int i=0;i<128; i++)
            mPackList[i]=false;
        DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(args[1]));
        System.out.println("Server Listening @ "+ args[1]);
        byte[] receiveData = new byte[1024];
        while(true){
            DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
            serverSocket.receive(receivePacket);
            UDPPacket packet= new UDPPacket(receivePacket.getData());
            switch (Integer.valueOf(packet.getPacketType())){
                case UDPPacket.PACKET_TYPE_OPEN:
                    System.out.print("PACKET_TYPE_OPEN");
                    if(openFile(packet.getPacketData()))
                        continue;
                    else
                        System.err.println("Error Opening file: " +packet.getPacketData());
                    break;

                case UDPPacket.PACKET_TYPE_DATA:
                    System.out.print("PACKET_TYPE_DATA");
                    System.out.print(packet.toString());
                    String arr[]= packet.getPacketData().split(UDPPacket.VAR_SPLIT);
                    mPackList[Integer.valueOf(packet.getPacketNo())]= true;
                    byte[] data= arr[0].getBytes();
                    int offset= Integer.valueOf(arr[1]);
                    mTempFile.seek(offset);
                    mTempFile.write(data,0,data.length);
                    break;
                case UDPPacket.PACKET_TYPE_PRECOMMIT:
                    System.out.print("PACKET_TYPE_PRECOMMIT");
                    String packList[]= packet.getPacketData().split(",");
                    String lostPacks="";
                    for (int i=0;i<packList.length;i++)
                        if(!mPackList[Integer.valueOf(packList[i])])
                            lostPacks=lostPacks+packList[i]+",";
                    System.out.println("Packets lost: "+ lostPacks+"\n");
                    if(!lostPacks.equals(""))
                        requestRetransmit(lostPacks, receivePacket.getAddress(), receivePacket.getPort());
                        preCommitAccept(receivePacket.getAddress(),receivePacket.getPort());
                    break;
                case UDPPacket.PACK_TYPE_CONFIRM_COMMIT:
                    System.out.print("PACK_TYPE_CONFIRM_COMMIT");
                    confirmCommit(receivePacket.getAddress(), receivePacket.getPort());
                    break;
                case UDPPacket.PACKET_TYPE_ABORT:
                    System.out.print("PACKET_TYPE_ABORT");
                    abort(receivePacket.getAddress(),receivePacket.getPort());
                    break;
                case UDPPacket.PACKET_TYPE_CLOSE:
                    System.out.print("PACKET_TYPE_CLOSE");
                    close(receivePacket.getAddress(),receivePacket.getPort());
            }
        }

    }

    private static void close(InetAddress address, int port) throws IOException {
        for(int i=0;i<mPackList.length;i++)
            mPackList[i]=false;
        mTempFile.close();
        FileReader tempFile = null;
        FileWriter actualFile= null;
        try {
            tempFile = new FileReader(mPath+mTempFilename);
            actualFile = new FileWriter(mPath+mFilename);
            int c = tempFile.read();
            while(c!=-1) {
                actualFile.write(c);
                c = tempFile.read();
            }
            actualFile.flush();
            actualFile.close();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            tempFile.close();
            actualFile.close();
        }
        File delFile= new File(mPath+mTempFilename);
        delFile.delete();
        DatagramSocket serverSocket= new DatagramSocket();
        UDPPacket packet= new UDPPacket();
        packet.setPacketNo("-1");
        packet.setPacketData("");
        packet.setPacketType(String.valueOf(UDPPacket.PACKET_TYPE_CLOSE));
        byte[] data= packet.toString().getBytes();
        DatagramPacket closePacket= new DatagramPacket(data,data.length,address,port);
        serverSocket.send(closePacket);
    }

    private static void abort(InetAddress address, int port) throws IOException {
        for(int i=0;i<mPackList.length;i++)
            mPackList[i]=false;
        mTempFile.close();
        File delFile= new File(mPath+mTempFilename);
        delFile.delete();

        DatagramSocket serverSocket= new DatagramSocket();
        UDPPacket packet= new UDPPacket();
        packet.setPacketNo("-1");
        packet.setPacketData("");
        packet.setPacketType(String.valueOf(UDPPacket.PACKET_TYPE_ABORT));
        byte[] data= packet.toString().getBytes();
        DatagramPacket closePacket= new DatagramPacket(data,data.length,address,port);
        serverSocket.send(closePacket);
    }
    private static void confirmCommit(InetAddress address, int port) throws IOException {
        DatagramSocket serverSocket = new DatagramSocket();
        UDPPacket packet = new UDPPacket();
        packet.setPacketData("");
        packet.setPacketNo("-1");
        packet.setPacketType(String.valueOf(UDPPacket.PACK_TYPE_CONFIRM_COMMIT));
        byte[] sendData=packet.toString().getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
        serverSocket.send(sendPacket);
        for(int i=0;i<mPackList.length;i++)
            mPackList[i]=false;
        mTempFile.close();
        FileReader tempFile = null;
        FileWriter actualFile= null;
        try {
            tempFile = new FileReader(mPath+mTempFilename);
            actualFile = new FileWriter(mPath+mFilename);
            int c = tempFile.read();
            while(c!=-1) {
                actualFile.write(c);
                c = tempFile.read();
            }
            actualFile.flush();
            actualFile.close();
        } catch(IOException e) {
            e.printStackTrace();
        }finally {
            tempFile.close();
            actualFile.close();
        }
        mTempFile= new RandomAccessFile(mPath+mTempFilename,"rw");
    }
    private static void preCommitAccept(InetAddress address, int port) throws IOException {
        DatagramSocket serverSocket = new DatagramSocket();
        UDPPacket packet = new UDPPacket();
        packet.setPacketData("");
        packet.setPacketNo("-1");
        packet.setPacketType(String.valueOf(UDPPacket.PACKET_TYPE_PRECOMMIT_ACCEPT));
        byte[] sendData=packet.toString().getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
        serverSocket.send(sendPacket);
    }
    private static void requestRetransmit(String lostPacks, InetAddress address, int port) throws IOException {
        DatagramSocket serverSocket = new DatagramSocket();
        UDPPacket packet = new UDPPacket();
        packet.setPacketData(lostPacks);
        packet.setPacketNo("-1");
        packet.setPacketType(String.valueOf(UDPPacket.PACKET_TYPE_REQUEST_RETRANSMIT));
        byte[] sendData=packet.toString().getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
        serverSocket.send(sendPacket);
    }

}
