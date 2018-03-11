//
//  HttpParser.java
//  Proxy
//
//  Created by Brandon T on 2017-11-28.
//  Copyright © 2018 Brandon T. All rights reserved.
//

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

public class HttpParser {
    private String request;
    private Hashtable<String, String> headers;

    public HttpParser(String request) throws IOException {
        headers = new Hashtable<>();
        this.parse(request);
    }

    private void parse(String request) throws IOException, RuntimeException {
        BufferedReader reader = new BufferedReader(new StringReader(request));

        //Parse first line of the request..
        String line = reader.readLine();
        if (line == null || line.length() == 0) {
            throw new RuntimeException("Invalid Request: " + line);
        }
        this.request = line;

        String header = reader.readLine();
        while (header.length() > 0) {
            int idx = header.indexOf(":");
            if (idx == -1) {
                throw new RuntimeException("Invalid Header: " + header);
            }
            headers.put(header.substring(0, idx), header.substring(idx + 1, header.length()));
            header = reader.readLine();
        }
    }

    public String getRequest() {
        return request;
    }


    public String getHeader(String headerName){
        return headers.get(headerName);
    }

    public String getRequestMethod() {
        String line = this.getRequest();
        String[] chunks = line.split("\\s");
        return chunks[0];
    }

    public String getRequestHostURL() {
        String line = this.getRequest();
        String[] chunks = line.split("\\s");

        try {
            if (!chunks[1].contains("://")) {
                chunks[1] = "http://" + chunks[1];
            }

            URL url = new URL(chunks[1]);
            return url.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getRequestPort() {
        String line = this.getRequest();
        String[] chunks = line.split("\\s");

        try {
            if (!chunks[1].contains("://")) {
                chunks[1] = "http://" + chunks[1];
            }

            URL url = new URL(chunks[1]);
            return url.getPort() <= 0 ? 80 : url.getPort();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return 80;
    }
}
