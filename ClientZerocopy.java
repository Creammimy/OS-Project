import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.file.StandardOpenOption;

public class ClientZerocopy {
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
                FileOutputStream fos = new FileOutputStream("C:\\Client\\" + requestedFileName);
                FileChannel fileChannel = fos.getChannel();
                ReadableByteChannel socketChannel = Channels.newChannel(socket.getInputStream());

                long startTime = System.currentTimeMillis();

                long transferred = 0;
                while (transferred < fileSize) {
                    transferred += fileChannel.transferFrom(socketChannel, transferred, fileSize - transferred);
                }

                fileChannel.close();
                fos.close();

                long endTime = System.currentTimeMillis();
                long transferTime = endTime - startTime;

                System.out.println("File downloaded successfully!");
                System.out.println("File size: " + fileSize + " bytes");
                System.out.println("Transfer time: " + transferTime + " ms");

            } else {
                System.out.println("The file you requested does not exist in the Server.");
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
