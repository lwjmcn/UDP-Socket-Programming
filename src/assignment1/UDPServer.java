package assignment1;

import java.net.*;

// UDPClient 로부터 메시지 수신
// UDPReceiver(multicast group members)에게 메시지 송신
public class UDPServer extends Thread{
    private String addr;
    private int portNum;
    private String userName;
    UDPServer(String addr, int portNum, String userName) {
        this.addr = addr;
        this.portNum = portNum;
        this.userName = userName;
    }
    public void run() {
        try {
            //multicast socket 생성
            DatagramSocket serverSocket = new DatagramSocket(portNum);

            while (true) {
                //buffer 생성
                byte[] sendData = new byte[512];
                byte[] receiveData = new byte[512];

                //receive datagram from client
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                //wait to receive
                serverSocket.receive(receivePacket);
                String msg = new String(receivePacket.getData(),0,receivePacket.getLength());

                //send datagram to multicast receivers
                sendData = msg.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(addr), 2022);
                serverSocket.send(sendPacket);
                //EXIT
                if (msg.equals(userName+" EXIT.")) {
                    serverSocket.close();
                    break;
                }
            } // loop back and wait for another datagram
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
