package com.lopez.julz.textrequest.api;

public class BaseURL {
    public static String baseUrl() { // default URL
        return "http://192.168.10.15/crm-boheco1/public/api/";
    }

    public static String baseUrl(String ip) {
        return "http://" + ip + "/crm-boheco1/public/api/";
    }
}
