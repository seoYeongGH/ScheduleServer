package com.schedule.message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class FirebaseMsgSender {
	
	public String sendGroupInvite(ArrayList<String> tokens) {
		final String apiKey = "apiKey";
        URL url = null;
		
        try {
			url = new URL("https://fcm.googleapis.com/fcm/send");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
		
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "key=" + apiKey);

        // 이걸로 보내면 특정 토큰을 가지고있는 어플에만 알림을 날려준다  위에 둘중에 한개 골라서 날려주자
        for(String token: tokens) {
        String input = "{\"notification\" : {\"title\" : \" Schappy \", \"body\" : \"새로운 초대가 있습니다.\"}, \"to\":\" "+token+"\"}";

        OutputStream os = conn.getOutputStream();
        
        // 서버에서 날려서 한글 깨지는 사람은 아래처럼  UTF-8로 인코딩해서 날려주자
        os.write(input.getBytes("UTF-8"));
        os.flush();
        os.close();
        
        int responseCode = conn.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + input);
        System.out.println("Response Code : " + responseCode);
        }
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "jsonView";

	}
}
