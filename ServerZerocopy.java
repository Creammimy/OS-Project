import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.file.StandardOpenOption;

public class ServerZerocopy {
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
                    dos.writeLong(requestedFile.length());

                    FileInputStream fis = new FileInputStream(requestedFile);
                    FileChannel fileChannel = fis.getChannel();
                    WritableByteChannel socketChannel = Channels.newChannel(socket.getOutputStream());

                    long startTime = System.currentTimeMillis();

                    long transferred = 0;
                    while (transferred < requestedFile.length()) {
                        transferred += fileChannel.transferTo(transferred, requestedFile.length() - transferred, socketChannel);
                    }

                    fileChannel.close();
                    fis.close();

                    long endTime = System.currentTimeMillis();
                    long transferTime = endTime - startTime;

                    System.out.println("File transferred successfully: " + requestedFileName);
                    System.out.println("File size: " + requestedFile.length() + " bytes");
                    System.out.println("Transfer time: " + transferTime + " ms");
                } else {
                    dos.writeLong(0);
                    System.out.println("The file you requested does not exist: " + requestedFileName);
                }

                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
