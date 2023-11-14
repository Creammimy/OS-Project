import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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

                    // เลือกวิธีส่งข้อมูล
                    System.out.println("Choose transfer method: 1. Normal, 2. Zero Copy");
                    int transferMethod = dis.readInt();
                    dos.writeInt(transferMethod); // ส่ง transferMethod ไปที่ Client

                    if (transferMethod == 1) {
                        // ใช้แบบธรรมดา
                        sendFileUsingNormalMethod(requestedFile, dos);
                    } else if (transferMethod == 2) {
                        // ใช้ Zero Copy
                        sendFileUsingZeroCopy(requestedFile, dos);
                    }
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

    private static void sendFileUsingNormalMethod(File file, DataOutputStream dos) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            

            while ((bytesRead = bis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }

            

            System.out.println("File transferred successfully: " + file.getName());
            System.out.println("File size: " + file.length() + " bytes");
           
        }
    }

    private static void sendFileUsingZeroCopy(File file, DataOutputStream dos) throws IOException {
        try (FileChannel fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            long fileSize = fileChannel.size();
            dos.writeLong(fileSize); // ส่งขนาดไฟล์ให้ Client

            

            fileChannel.transferTo(0, fileSize, Channels.newChannel(dos));

            

            System.out.println("File transferred successfully (Zero Copy): " + file.getName());
            System.out.println("File size: " + fileSize + " bytes");
            
        }
    }
}
