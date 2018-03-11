//
//  Main.java
//  Proxy
//
//  Created by Brandon T on 2017-11-28.
//  Copyright © 2018 Brandon T. All rights reserved.
//

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        ServerSocket socket = null;

        try {
            socket = new ServerSocket(port);

            while(true) {
                new SocketThread(socket.accept()).start();
                Thread.sleep(1);
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    public static boolean shouldBlockRequest(String host) {
        for (String badURL : URLS_TO_BLOCK) {
            if (host.contains(badURL)) {
                return true;
            }
        }
        return false;  //Change this to true to block ALL requests.
    }

    private static String[] URLS_TO_BLOCK = {
            "sun.hac.lp1.d4c.nintendo.net",
            "receive-lp1.dg.srv.nintendo.net",
            "app-b01.lp1.npns.srv.nintendo.net",
            ".baas.nintendo.com"
    };
}
