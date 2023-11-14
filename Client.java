import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);

            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            int fileCount = dis.readInt();
            System.out.println("Files available in the Server:");

            for (int i = 0; i < fileCount; i++) {
                System.out.println(dis.readUTF());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Please enter the name of the file you want: ");
            String requestedFileName = reader.readLine();
            dos.writeUTF(requestedFileName);

            long fileSize = dis.readLong();
            if (fileSize > 0) {
                // ให้ผู้ใช้เลือกวิธีการรับไฟล์
                System.out.println("Choose transfer method: 1. Normal, 2. Zero Copy");
                int transferMethod = Integer.parseInt(reader.readLine());
                dos.writeInt(transferMethod);

                if (transferMethod == 1) {
                    // ใช้แบบธรรมดา
                    receiveFileUsingNormalMethod(requestedFileName, fileSize, dis);
                } else if (transferMethod == 2) {
                    // ใช้ Zero Copy
                    receiveFileUsingZeroCopy(requestedFileName, fileSize, dis);
                }
            } else {
                System.out.println("The file you requested does not exist in the Server.");
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveFileUsingNormalMethod(String requestedFileName, long fileSize, DataInputStream dis) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("C:\\Client\\" + requestedFileName))) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            long startTime = System.currentTimeMillis(); // เวลาเริ่มต้นการรับ

            while ((bytesRead = dis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            long endTime = System.currentTimeMillis(); // เวลาสิ้นสุดการรับ
            long transferTime = endTime - startTime; // เวลาที่ใช้ในการรับ

            System.out.println("File downloaded successfully!");
            System.out.println("File size: " + fileSize + " bytes");
            System.out.println("Transfer time: " + transferTime + " ms");
        }
    }

    private static void receiveFileUsingZeroCopy(String requestedFileName, long fileSize, DataInputStream dis) throws IOException {
        try (FileChannel fileChannel = FileChannel.open(Path.of("C:\\Client\\" + requestedFileName), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            long startTime = System.currentTimeMillis(); // เวลาเริ่มต้นการรับ

            fileChannel.transferFrom(Channels.newChannel(dis), 0, fileSize);

            long endTime = System.currentTimeMillis(); // เวลาสิ้นสุดการรับ
            long transferTime = endTime - startTime; // เวลาที่ใช้ในการรับ

            System.out.println("File downloaded successfully (Zero Copy)!");
            System.out.println("File size: " + fileSize + " bytes");
            System.out.println("Transfer time: " + transferTime + " ms");
        }
    }
}
