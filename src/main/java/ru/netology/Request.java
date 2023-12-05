package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Request {
    private BufferedInputStream inputStream;
    private List<String> headers = new ArrayList<>();

    private String requestLine;

    public void setRequestLine(String requestLine) {
        this.requestLine = requestLine;
    }

    public String getRequestLine() {
        return requestLine;
    }

    public void addHeader(String header) {
        headers.add(header);
    }

    public void addHeaders(List<String> headers) {
        this.headers.addAll(headers);
    }

    public void showHeaders() {
        for (String str : headers) {
            System.out.println(str);
        }
    }

    public List<NameValuePair> getQueryParams() throws MalformedURLException {
        return URLEncodedUtils.parse(URI.create("http://localhost:9999" + requestLine.split(" ")[1]), "UTF-8");
    }
    public String getQueryParam(String name) throws MalformedURLException {
        var params = getQueryParams();
        for(NameValuePair entry:params){
            if(entry.getName().equals(name)){
                return entry.getValue();
            }
        }
        return null;
    }

    public String getPath() {
        String path = requestLine.split(" ")[1];
//        return URLEncodedUtils.CONTENT_TYPE;
        int index = path.indexOf('?');
        return path.substring(0, index);
    }

    public String getMethod() {
        return requestLine.split(" ")[0];
    }
}
