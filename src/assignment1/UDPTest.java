package assignment1;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Scanner;

public class UDPTest {
    public static void main(String args[]) {
        //input port number
        int portNum = Integer.parseInt(args[0]);
        try {
            Thread thread = new Thread();
            while (true){
                if(thread.isAlive()){ //thread의 종료 여부 확인
                    Thread.sleep(1000); //반복문의 resource낭비 제약
                    continue; //종료되지 않았다면 뒤로 넘어가지 못함
                }
                //초기화
                String addr = "225";
                String chatroomName = null;
                String userName = null;
                while (true) {
                    //start
                    System.out.println("채팅을 시작하시려면 #JOIN (채팅방이름) (사용자이름), 종료하시려면 #QUIT을 입력해주세요.");
                    Scanner s = new Scanner(System.in);
                    String command = s.next();
                    //QUIT
                    if (command.equals("#QUIT")) {
                        System.exit(0);
                    }
                    //JOIN
                    else if (command.equals("#JOIN")) {
                        chatroomName = s.next();
                        userName = s.next();

                        //SHA-256 암호화
                        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                        byte[] sha = messageDigest.digest(chatroomName.getBytes(StandardCharsets.UTF_8));
                        for (int i = 0; i < 3; i++) {
                            int tmp = 0xff & sha[sha.length - i - 1]; //int 형변환
                            String tmp2 = String.valueOf(tmp); //String 형변환
                            addr = addr + "." + tmp2; //225.x.y.z
                        }
                        break;
                    } else System.out.println("Command not valid.");
                }
                //thread 시작
                thread = new UDPReceiver(addr,portNum, userName);
                thread.start();
                new UDPServer(addr, portNum, userName).start();
                new UDPClient(addr, portNum, userName).start();
                //loop back
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
