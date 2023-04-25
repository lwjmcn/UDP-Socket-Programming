package assignment1;

import java.io.*;
import java.net.*;

// UDPServer 로부터 메시지 수신 후 출력
public class UDPReceiver extends Thread {
    private String addr;
    private int portNum;
    private String userName;

    UDPReceiver(String addr, int portNum, String userName) {
        this.addr = addr;
        this.portNum = portNum;
        this.userName = userName;
    }
    public void run() {
        try {
            //multicast socket 생성
            MulticastSocket clientSocket = new MulticastSocket(2022);
            clientSocket.setTimeToLive(64); //ttl 지정

            //multicast server 지정
            InetAddress mulAddress = InetAddress.getByName(addr);
            InetSocketAddress group = new InetSocketAddress(mulAddress, 2022);
            NetworkInterface netIf = NetworkInterface.getByName("bge0");

            //multicast server 접속
            clientSocket.joinGroup(group, netIf);

            //send datagram to multicast receivers(ENTER)
            String msg = userName + " ENTER.";
            byte[] sendData = msg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(addr), 2022);
            clientSocket.send(sendPacket);

            while(true) {
                //receive datagram from server
                byte[] receiveData = new byte[512];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                //wait to receive
                clientSocket.receive(receivePacket);
                msg = new String(receivePacket.getData(), 0, receivePacket.getLength());
                //EXIT
                if (msg.equals(userName+" EXIT.")) {
                    clientSocket.leaveGroup(group, netIf);
                    break;
                }
                System.out.println(msg);
            } // loop back and wait for another datagram
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
