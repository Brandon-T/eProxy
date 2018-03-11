//
//  SocketThread.java
//  Proxy
//
//  Created by Brandon T on 2017-11-28.
//  Copyright © 2018 Brandon T. All rights reserved.
//

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketThread extends Thread {
    private Socket clientSocket = null;
    private Object lock = new Object();

    public SocketThread(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try {
            InputStream clientInputStream = clientSocket.getInputStream();
            OutputStream clientOutputStream = clientSocket.getOutputStream();

            byte[] buffer = new byte[8196];
            int len = clientInputStream.read(buffer);

            if (len > 0) {
                synchronized(lock) {
                    System.out.println("REQUEST:");
                    System.out.println(new String(buffer, 0, len).trim());
                }

                HttpParser parser = new HttpParser(new String(buffer, 0, len));

                //Process connection between client and myself..
                if (parser.getRequestMethod().equals("CONNECT")) {
                    String response = "HTTP/1.1 200 Connection established\r\nProxy-Agent: BrandonT\r\n\r\n";

                    clientOutputStream.write(response.getBytes());
                    clientOutputStream.flush();

                    synchronized(lock) {
                        System.out.println("RESPONDED TO PROXY REQUEST..");
                    }
                }


                synchronized(lock) {
                    System.out.println("PROCESSING REQUEST..");
                }

                String host = parser.getRequestHostURL();
                int port = parser.getRequestPort();

                if (Main.shouldBlockRequest(host)) {
                    synchronized(lock) {
                        System.out.println("REQUEST BLOCKED..\n");
                    }
                    clientSocket.close();
                    return;
                }

                // Write request
                Socket socket = new Socket(host, port);
                InputStream serverInputStream = socket.getInputStream();
                OutputStream serverOutputStream = socket.getOutputStream();


                Thread t = new Thread(() -> {
                    try {
                        //DO NOT SEND `CONNECT` PACKET
                        if (!parser.getRequestMethod().equals("CONNECT")) {
                            serverOutputStream.write(buffer, 0, len);
                            serverOutputStream.flush();
                        }

                        int length = 0;
                        byte[] clientBuffer = new byte[1024];

                        while ((length = clientInputStream.read(clientBuffer)) > 0) {
                            serverOutputStream.write(clientBuffer, 0, length);
                            serverOutputStream.flush();
                        }

                        serverOutputStream.flush();
                        serverOutputStream.close();
                    }
                    catch (Exception e) {
                        //e.printStackTrace();
                        synchronized(lock) {
                            System.out.println("CONNECTION CLOSED 😞");
                        }
                    }
                });
                t.start();


                try {
                    int length = 0;
                    byte[] clientBuffer = new byte[1024];
                    while ((length = serverInputStream.read(clientBuffer)) != -1) {
                        clientOutputStream.write(clientBuffer, 0, length);
                        clientOutputStream.flush();
                    }

                    clientOutputStream.flush();
                    clientOutputStream.close();
                }
                catch (Exception e) {
//                    e.printStackTrace();
                    synchronized(lock) {
                        System.out.println("CONNECTION CLOSED 😞");
                    }
                }

                t.join();
                synchronized(lock) {
                    System.out.println("COMPLETED\n");
                }
                socket.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
