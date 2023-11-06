import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);

            System.out.println("Server is waiting for connections...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connected to " + socket.getInetAddress());

                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                File serverDirectory = new File("C:\\Server");
                File[] files = serverDirectory.listFiles();

                dos.writeInt(files.length);

                for (File file : files) {
                    dos.writeUTF(file.getName());
                }

                String requestedFileName = dis.readUTF();
                File requestedFile = new File(serverDirectory, requestedFileName);

                if (requestedFile.exists()) {
                    dos.writeLong(requestedFile.length()); // ส่งขนาดไฟล์ให้ Client

                    FileInputStream fis = new FileInputStream(requestedFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    long startTime = System.currentTimeMillis(); // เวลาเริ่มต้นการส่ง

                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                    }

                    long endTime = System.currentTimeMillis(); // เวลาสิ้นสุดการส่ง
                    long transferTime = endTime - startTime; // เวลาที่ใช้ในการส่ง

                    fis.close();
                    System.out.println("File transferred successfully: " + requestedFileName);
                    System.out.println("File size: " + requestedFile.length() + " bytes");
                    System.out.println("Transfer time: " + transferTime + " ms");
                } else {
                    dos.writeLong(0); // ส่งขนาดไฟล์เป็น 0 ถ้าไม่มีไฟล์
                    System.out.println("The file you requested does not exist: " + requestedFileName);
                }

                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}