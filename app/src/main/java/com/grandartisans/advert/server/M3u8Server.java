package com.grandartisans.advert.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

//import fi.iki.elonen.NanoHTTPD.Response.Status;

import android.util.Log;

import com.westone.cryptoSdk.Api;


public class M3u8Server extends NanoHTTPD {
	
	private static String TAG = "M3U8Server";
	
	private static NanoHTTPD server;
	
	public static final int PORT = 8769;
	private static Api encApi=null;
	
	/**
	 * 启动服务
	 */
	public static void execute() {
        try {
        	server = M3u8Server.class.newInstance();
        	server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
			encApi = new Api();
        } catch (IOException ioe) {
            Log.e(TAG, "启动服务失败：\n" + ioe);
            System.exit(-1);
        } catch (Exception e) {
        	Log.e(TAG, "启动服务失败：\n" + e);
            System.exit(-1);
        }

        Log.i(TAG, "服务启动成功\n");

        try {
            System.in.read();
        } catch (Throwable ignored) {
        }
    }
	
	/**
	 * 关闭服务
	 */
	public static void finish() {
		if(server != null){
			server.stop();
			Log.i(TAG, "服务已经关闭");
			server = null;
		}
	}

    public M3u8Server() {
        super(PORT);
    }

    @Override
    public Response serve(IHTTPSession session) {
    	
    	String url = String.valueOf(session.getUri());
    	
    	//Log.d(TAG, "请求URL：" + url);
    	
    	File file = new File(url);
    	
    	if(file.exists()){
			//FileInputStream fis = null;
			InputStream fis = null;
    		if(url.contains(".m3u8")){
    		    /*
                Log.d(TAG, "is enc file");
				byte[] out = encApi.DecryptFileOnce(url);
                //Log.d(TAG, "解密结果：" + Arrays.toString(out));
				if(out!=null){
                    Log.d(TAG, "dec file ok ");
					fis = new ByteArrayInputStream(out);
				}else{
                    Log.d(TAG, "dec file error ");
                }
                */
                fis = encApi.DecryptFile(url);
				/*
				encApi.DecryptFile(url,url+".dec");
				file = new File(url+".dec");
				try {
					fis = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return newFixedLengthResponse(Status.NOT_FOUND, "text/html", "文件不存在：" + url);
				}
				*/
			}else {
				try {
					fis = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
                    Log.d(TAG, "read data from inputstream error 000!!");
					return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", "文件不存在：" + url);
				}
			}
    		long length = file.length();
    		// ts文件
    		String mimeType = "video/mpeg";
    		if(url.contains(".m3u8")){
    			// m3u8文件
    			mimeType = "video/x-mpegURL";
    		}
    		return newChunkedResponse(Response.Status.OK, mimeType, fis);
    	} else {
            Log.d(TAG, "请求的文件不存在 ！！！");
    		return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", "文件不存在：" + url);
    	}
    }
}
