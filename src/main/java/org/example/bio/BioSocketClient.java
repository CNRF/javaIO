package org.example.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

/**
 * @author : mark.wang
 */
public class BioSocketClient {
    public static void main(String[] args) throws InterruptedException {
        int clientNumber = 20;
        CountDownLatch countDownLatch = new CountDownLatch(clientNumber);
        //启动20个客户端并发访问
        for (int i = 0; i < clientNumber; i++, countDownLatch.countDown()){
            ClientRequestThread clientRequestThread=new ClientRequestThread(countDownLatch, i);
            new Thread(clientRequestThread).start();
        }
        // 这个wait不涉及到具体的实验逻辑，只是为了保证守护线程在启动所有线程后，进入等待状态
        synchronized (BioSocketClient.class) {
            BioSocketClient.class.wait();
        }
    }
}

/**
 * 一个客户端请求线程
 */
class ClientRequestThread implements Runnable {
    private final CountDownLatch countDownLatch;
    private final Integer clientIndex;

    public ClientRequestThread(CountDownLatch countDownLatch, Integer clientIndex) {
        this.countDownLatch = countDownLatch;
        this.clientIndex = clientIndex;
    }

    @Override
    public void run() {
        Socket socket;
        OutputStream clientRequest = null;
        InputStream clientResponse = null;
        try {
            socket = new Socket("localhost", 8081);
            clientRequest = socket.getOutputStream();
            clientResponse = socket.getInputStream();
            //等待所有线程都启动完成一起发送请求
            this.countDownLatch.await();
            //发送请求
            clientRequest.write(("这是第" + this.clientIndex + "个客户端的请求").getBytes());
            clientRequest.flush();
            //等待服务器返回信息
            System.out.println("第" + this.clientIndex + "个客户端的请求发送完成，等待服务器返回信息");
            int maxLen = 1024;
            byte[] contextBytes = new byte[maxLen];
            int realLen;
            StringBuilder message = new StringBuilder();
            //程序执行到这里，会一直等待服务器返回信息（注意，前提是in和out都不能close，如果close了就收不到服务器的反馈了）
            while ((realLen = clientResponse.read(contextBytes, 0, maxLen)) != -1) {
                message.append(new String(contextBytes, 0, realLen));
            }
            //String messageEncode = new String(message , "UTF-8");
            message = new StringBuilder(URLDecoder.decode(message.toString(), "UTF-8"));
            System.out.println("第" + this.clientIndex + "个客户端接收到来自服务器的信息:" + message);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (clientRequest != null) {
                    clientRequest.close();
                }
                if (clientResponse != null) {
                    clientResponse.close();
                }
            } catch (IOException ignored) {

            }
        }
    }
}
