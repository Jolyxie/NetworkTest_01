package com.android.jolyxie.networktest_01;

import android.accounts.NetworkErrorException;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView content;
    String resultStr = "";
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        content = (TextView) findViewById(R.id.text);
        handler = new Handler();
        Thread visitBaiduThread = new Thread(new VisitWebRunnable());
        visitBaiduThread.start();
    }

    class VisitWebRunnable implements Runnable {

        @Override
        public void run() {
            String data = getURLResponse("http://www.baidu.com");
            //String data = get("http://www.baidu.com");
            resultStr = data;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //在UI线程更新UI
                    content.setText(resultStr);
                }
            });
        }
    }

    /**
     * 获取指定URL的响应字符串
     * @param urlString
     * @return
     */
    public static String getURLResponse(String urlString){
        HttpURLConnection conn = null; //连接对象
        InputStream is = null;
        String resultData = "";
        try {
            URL url = new URL(urlString); //URL对象
            conn = (HttpURLConnection)url.openConnection(); //使用URL打开一个链接
            conn.setRequestMethod("GET"); //使用get请求
            //conn.setDoInput(true); //允许输入流，即允许下载
            //conn.setDoOutput(true); //允许输出流，即允许上传
            conn.setUseCaches(false); //不使用缓冲
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(100000);

            int requestCode = conn.getResponseCode();
            if(requestCode == 200) {
                is = conn.getInputStream();   //获取输入流，此时才真正建立链接
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader bufferReader = new BufferedReader(isr);
                String inputLine = "";
                while ((inputLine = bufferReader.readLine()) != null) {
                    resultData += inputLine + "\n";
                }
            }
        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            if(is != null){
                try{
                    is.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }if(conn != null){
                conn.disconnect();
            }
        }
        return resultData;
    }

    public static String get(String url){
        HttpURLConnection conn = null;
        try{
            URL mUrl = new URL(url);
            conn = (HttpURLConnection) mUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(100000);

            int requestCode = conn.getResponseCode();
            if(requestCode == 200){
                InputStream in = conn.getInputStream();
                String respond = getStringFromInputStream(in);
                return respond;
            }else{
                throw new NetworkErrorException("respond state is "+requestCode);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    public static String post(String url , String content){
        HttpURLConnection conn = null;
        try {
            URL mUrl= new URL(url);
            conn = (HttpURLConnection) mUrl.openConnection();

            conn.setRequestMethod("POST");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(10000);
            conn.setDoOutput(true);

            String data = content;
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            int requestCode = conn.getResponseCode();
            if(requestCode == 200){
                InputStream in = conn.getInputStream();
                String res = getStringFromInputStream(in);
                return res;
            }else{
                throw new NetworkErrorException("respond state is "+requestCode);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }
        return  null;
    }

    private static String getStringFromInputStream(InputStream in){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String res = null;
        byte buffer[] = new byte[1024];
        int len = -1;
        try {
            while ((len = in.read(buffer)) != -1) {
                os.write(buffer,0,len);
            }
            res = os.toString();
            in.close();
            os.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return res;
    }
}
