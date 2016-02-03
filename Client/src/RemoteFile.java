import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

public class RemoteFile {

    short mPacketNo;
    short temp=0;
    ArrayList<UDPPacket> mPacketList;
    public RemoteFile() {
        mPacketNo=0;
        mPacketList= new ArrayList<UDPPacket>();
    }

    public String getName() {

        return "";
    }

    public int writeBlock(byte[] data, int offset){
        UDPPacket packet = new UDPPacket();
        packet.setPacketData(new String(data)+ UDPPacket.VAR_SPLIT+ String.valueOf(offset));
        packet.setPacketNo(String.valueOf(mPacketNo));
        packet.setPacketType(String.valueOf(UDPPacket.PACKET_TYPE_DATA));
        for(ServerInfo server: ReplicatedFS.mServerList){
            try {
                DatagramSocket clientSocket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName(server.getIpAddress());

                byte[] sendData=packet.toString().getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.valueOf(server.getPortNo()));

                    if(temp%2==0){
                        try {
                            clientSocket.send(sendPacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            }catch (SocketException e){
                e.printStackTrace();
            }catch (UnknownHostException e){
                e.printStackTrace();
            }
        }
        mPacketList.add(packet);
        mPacketNo++;
        temp++;
        return data.length;
    }

    public int commit() {
        String packList="";

        for(int i=0;i<mPacketList.size();i++)
            packList=packList+ mPacketList.get(i).getPacketNo()+",";
        if(packList.equals(""))
            return 1;
        for(ServerInfo server: ReplicatedFS.mServerList){
            boolean didCommit=false;
            while(!didCommit){
                try {
                    DatagramSocket clientSocket = new DatagramSocket();
                    InetAddress IPAddress = InetAddress.getByName(server.getIpAddress());
                    UDPPacket preCommitPack = new UDPPacket();
                    preCommitPack.setPacketData(packList);
                    preCommitPack.setPacketNo(String.valueOf(mPacketNo));
                    preCommitPack.setPacketType(String.valueOf(UDPPacket.PACKET_TYPE_PRECOMMIT));
                    byte[] sendData = preCommitPack.toString().getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.valueOf(server.getPortNo()));
                    byte[] receiveData;
                    DatagramPacket receivePacket = sendTillResReceived(clientSocket,sendPacket);
                    receiveData = receivePacket.getData();
                    UDPPacket recPack = new UDPPacket(receiveData);
                    switch (Integer.valueOf(recPack.getPacketType())) {
                        case UDPPacket.PACKET_TYPE_PRECOMMIT_ACCEPT:
                            UDPPacket conCommitPack=new UDPPacket();
                            conCommitPack.setPacketData("");
                            conCommitPack.setPacketNo("-1");
                            conCommitPack.setPacketType(String.valueOf(UDPPacket.PACKET_TYPE_CONFIRM_COMMIT));
                            sendData= conCommitPack.toString().getBytes();
                            sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.valueOf(server.getPortNo()));
                            DatagramPacket receiveConfirmPacket= sendTillResReceived(clientSocket, sendPacket);
                            receiveData = receiveConfirmPacket.getData();
                            UDPPacket receiveConfirmPacketUDP=new UDPPacket(receiveData);
                            if(receiveConfirmPacketUDP.getPacketType().equals(String.valueOf(UDPPacket.PACKET_TYPE_CONFIRM_COMMIT)))
                                didCommit=true;
                            break;
                        case UDPPacket.PACKET_TYPE_REQUEST_RETRANSMIT:
                            retransmitPackets(recPack.getPacketData(),server);
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                    return -1;
                }
            }
        }
        mPacketList.clear();
        mPacketNo=0;
        return 0;
    }

    private void retransmitPackets(String packetData, ServerInfo server) {
        System.out.print(packetData.toString());
        String[] retryPacketsArr= packetData.split(",");

        for(int i=0;i<retryPacketsArr.length;i++){
            try {
                DatagramSocket clientSocket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName(server.getIpAddress());
                UDPPacket retryPacket= mPacketList.get(Integer.valueOf(retryPacketsArr[i]));
                byte[] sendData = retryPacket.toString().getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.valueOf(server.getPortNo()));
                clientSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static DatagramPacket sendTillResReceived(DatagramSocket clientSocket, DatagramPacket sendPacket) throws IOException {

        clientSocket.send(sendPacket);
        byte[] recData=new byte[1024];
        DatagramPacket recPacket= new DatagramPacket(recData,recData.length,sendPacket.getAddress(),sendPacket.getPort());
        clientSocket.setSoTimeout(1000);
        while(true){
            try{
                clientSocket.receive(recPacket);
                break;
            }catch (SocketTimeoutException e){
                clientSocket.send(sendPacket);
            }
        }
        return recPacket;
    }
    public void abort() throws IOException {
        UDPPacket abortPack= new UDPPacket();
        abortPack.setPacketType(String.valueOf(UDPPacket.PACKET_TYPE_ABORT));
        abortPack.setPacketNo("-1");
        abortPack.setPacketData("");
        for(ServerInfo server: ReplicatedFS.mServerList){
            try{
                DatagramSocket clientSocket= new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName(server.getIpAddress());
                byte[] sendData = abortPack.toString().getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.valueOf(server.getPortNo()));
                byte[] receiveData= new byte[1024];
                DatagramPacket receivedPacket=sendTillResReceived(clientSocket,sendPacket);
                UDPPacket resPacket= new UDPPacket(receivedPacket.getData());
                if(resPacket.getPacketType().equals(String.valueOf(UDPPacket.PACKET_TYPE_ABORT)))
                    continue;
                }catch (IOException e){
                e.printStackTrace();
                    return;
            }
        }
    }

    public int close() {
        commit();
        for(ServerInfo server:ReplicatedFS.mServerList){
            try{
                UDPPacket closePacket= new UDPPacket();
                closePacket.setPacketData("");
                closePacket.setPacketNo("-1");
                closePacket.setPacketType(String.valueOf(UDPPacket.PACKET_TYPE_CLOSE));
                DatagramSocket clientSocket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName(server.getIpAddress());
                byte[] sendData = closePacket.toString().getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.valueOf(server.getPortNo()));
                DatagramPacket recPac=sendTillResReceived(clientSocket,sendPacket);
                UDPPacket confirmClosePack= new UDPPacket(recPac.getData());
                if(confirmClosePack.getPacketType().equals(String.valueOf(UDPPacket.PACKET_TYPE_CLOSE)))
                    continue;
            }catch (IOException e){
                e.printStackTrace();
                return -1;
            }

        }
        return 1;
    }
}