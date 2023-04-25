package assignment1;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

// server 에 메시지 송신
public class UDPClient extends Thread {
    private String addr;
    private String userName;
    private int portNum;
    UDPClient(String addr, int portNum, String userName) {
        this.addr = addr;
        this.portNum = portNum;
        this.userName = userName;
    }
    public void run() {
        try {
            //socket 생성
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("localhost");

            while (true) {
                //buffer 생성
                byte[] sendData;

                //input stream
                BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
                String msg = inFromUser.readLine();

                // #명령어
                if(!msg.isEmpty() && msg.charAt(0)=='#') {
                    if (msg.equals("#EXIT")) {
                        //send datagram to server(EXIT)
                        msg = userName + " EXIT.";
                        sendData = msg.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNum);
                        clientSocket.send(sendPacket);
                        break;
                    }
                    else {
                        System.out.println("Command not valid.");
                        continue;
                    }
                }
                //String chunk 분할
                int size = 512-userName.length()-3; //"[userName] " 포함 512바이트 단위 분할
                ArrayList<String> chunks = new ArrayList<String>((msg.length() + size - 1) / size);
                for (int i = 0; i < msg.length(); i += size) {
                    chunks.add(msg.substring(i, Math.min(msg.length(), i + size)));
                }
                for(int i=0;i< chunks.size(); i++) {
                    msg = "["+userName +"] "+ chunks.get(i);
                    sendData = msg.getBytes();
                    //send datagram to server
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNum);
                    clientSocket.send(sendPacket);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
