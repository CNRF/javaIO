package org.example.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 一客户端一线程
 * @author : mark.wang
 */
public class BioSocketServer {
    /**
     * 默认端口
     */
    private static final int DEFAULT_PORT = 8081;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            System.out.println("监听" + DEFAULT_PORT + "端口信息");
            serverSocket = new ServerSocket(DEFAULT_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                SocketServerThread socketServerThread = new SocketServerThread(socket);
                new Thread(socketServerThread).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}

class SocketServerThread implements Runnable {
    private final Socket socket;

    public SocketServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            //接收的消息
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            int clientPort = socket.getPort();
            int maxLen = 1024;
            byte[] bytes = new byte[maxLen];
            //使用线程，同样无法解决read方法的阻塞问题，
            //也就是说read方法处同样会被阻塞，直到操作系统有数据准备好
            int read = inputStream.read(bytes, 0, maxLen);
            //读取信息
            String message = new String(bytes, 0, read);
            //打印信息
            System.out.println("clientPort" + clientPort + "信息" + message);

            //发送信息
            outputStream.write(("对" + clientPort + "发送响应信息").getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            //先关闭输入输出流，之后关闭socket
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
                if (null != outputStream) {
                    outputStream.close();
                }
                if (this.socket != null) {
                    this.socket.close();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

        }
    }
}
