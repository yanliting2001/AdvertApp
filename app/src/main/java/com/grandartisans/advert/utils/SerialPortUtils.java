package com.grandartisans.advert.utils;

import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import android_serialport_api.SerialPort;

public class SerialPortUtils {
    private final String TAG = "SerialPortUtils";
    //private String path = "/dev/ttyUSB0";
    //private String path = "/dev/ttyS3";
    private int baudrate = 115200;
    public boolean serialPortStatus = false; //是否打开串口标志
    public String data_;
    public boolean threadStatus; //线程状态，为了安全终止线程

    public SerialPort serialPort = null;
    public InputStream inputStream = null;
    public OutputStream outputStream = null;
    //public ChangeTool changeTool = new ChangeTool();


    /**
     * 打开串口
     * @return serialPort串口对象
     */
    public SerialPort openSerialPort(String path){
        try {
            Log.i(TAG, "openSerialPort: device name =  " + path);
            serialPort = new SerialPort(new File(path),baudrate,0);
            this.serialPortStatus = true;
            threadStatus = false; //线程状态

            //获取打开的串口中的输入输出流，以便于串口数据的收发
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();

            new ReadThread().start(); //开始线程监控是否有数据要接收
        } catch (IOException e) {
            Log.e(TAG, "openSerialPort: 打开串口异常：" + e.toString());
            return serialPort;
        }
        Log.i(TAG, "openSerialPort: 打开串口");
        return serialPort;
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort(){
        try {
            if(inputStream!=null)inputStream.close();
            if(outputStream!=null)outputStream.close();

            this.serialPortStatus = false;
            this.threadStatus = true; //线程状态
            if(serialPort!=null)serialPort.close();
        } catch (IOException e) {
            Log.e(TAG, "closeSerialPort: 关闭串口异常："+e.toString());
            return;
        }
        Log.i(TAG, "closeSerialPort: 关闭串口成功");
    }

    /**
     * 发送串口指令（字符串）
     * @param data String数据指令
     */
    public void sendSerialPort(String data){
        Log.i(TAG, "sendSerialPort: 发送数据");

        try {
            byte[] sendData = data.getBytes(); //string转byte[]
            this.data_ = new String(sendData); //byte[]转string
            if (sendData.length > 0) {
                outputStream.write(sendData);
                outputStream.write('\n');
                //outputStream.write('\r'+'\n');
                outputStream.flush();
                Log.i(TAG, "sendSerialPort: 串口数据发送成功:" + Arrays.toString(sendData));
            }
        } catch (IOException e) {
            Log.e(TAG, "sendSerialPort: 串口数据发送失败："+e.toString());
        }

    }

    /**
     * 发送串口指令（Bytes）
     * @param sendData Bytes数据指令
     */
    public void sendSerialPort(byte[] sendData){
        Log.i(TAG, "sendSerialPort: 发送数据");

        try {
            //byte[] sendData = data.getBytes(); //string转byte[]
            this.data_ = new String(sendData); //byte[]转string
            if (sendData.length > 0) {
                outputStream.write(sendData);
                //outputStream.write('\n');
                //outputStream.write('\r'+'\n');
                outputStream.flush();
                Log.i(TAG, "sendSerialPort: 串口数据发送成功:" + Arrays.toString(sendData));
            }
        } catch (IOException e) {
            Log.e(TAG, "sendSerialPort: 串口数据发送失败："+e.toString());
        }

    }

    /**
     * 单开一线程，来读数据
     */
    private class ReadThread extends Thread{
        @Override
        public void run() {
            super.run();
            //判断进程是否在运行，更安全的结束进程
            while (!threadStatus){
                //Log.d(TAG, "进入线程run");
                //64   1024
                byte[] buffer = new byte[9];
                int size=0; //读取数据的大小
                try {
                    size = inputStream.read(buffer,0,1);
                    if(size==1) {
                        if(buffer[0]==0x59) {
                            while (size < 9) {
                                size += inputStream.read(buffer, size, 9 - size);
                            }
                            if (size > 0) {
                                //Log.d(TAG, "run: 接收到了数据：" + changeTool.ByteArrToHex(buffer));
                                // Log.i(TAG, "run: 接收到了数据大小：" + String.valueOf(size));
                                if (onDataReceiveListener != null)
                                    onDataReceiveListener.onDataReceive(buffer, size);
                            }
                        }else{
                            Log.i(TAG, "run: data head ：" + buffer[0] + "is error");
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "run: 数据读取异常：" +e.toString());
                }
            }
        }
    }

    //这是写了一监听器来监听接收数据
    public OnDataReceiveListener onDataReceiveListener = null;
    public static interface OnDataReceiveListener {
        public void onDataReceive(byte[] buffer, int size);
    }
    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }
}
