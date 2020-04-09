package com.grandartisans.advert.utils;

import android.content.Context;
import android.util.Log;

import com.grandartisans.advert.interfaces.ElevatorDoorEventListener;

public class ALFu700ProjectManager {
    private final String TAG = "ALFu700ProjectManager";
    private volatile static ALFu700ProjectManager mProjectInstance = null;
    private SerialPortUtils serialPortUtils = null;
    private boolean isDeviceOpened  = false;
    public ALFu700ProjectManager(Context context){}
    public static ALFu700ProjectManager getInstance(Context context) {
        if (mProjectInstance == null) {
            synchronized (ALFu700ProjectManager.class) {
                if (mProjectInstance == null) {
                    mProjectInstance = new ALFu700ProjectManager(context);
                }
            }
        }
        return mProjectInstance;
    }

    public boolean openProject(){
        if(isDeviceOpened==false) {
            serialPortUtils = new SerialPortUtils();
            if (serialPortUtils != null) {
                if (serialPortUtils.openSerialPort("/dev/ttyS4") == null) {
                    Log.i(TAG,"can not open device /dev/ttyS4");
                    return false;
                }
                isDeviceOpened = true;
            }
        }
        return true;
    }
    public void closeProject(){
        if (serialPortUtils != null) {
            serialPortUtils.closeSerialPort();
        }
        isDeviceOpened = true;
    }

   public boolean SleepProject(){
       //关闭投影：AB 00 00 00 3B 00 00 00 00 00 00 00 00 00 00 10 00 00 00 F6
       //String hexData = "AB0000003B0000000000000000000010000000F6";
       String hexData = "AB000000100000000000000000000000000000BB";
       return sendHexData(hexData);
   }
   public boolean WakeUpProject(){
       //开机：AF 00 00 00 10 01 00 00 00 00 00 00 00 00 00 00 00 00 00 C0
       String hexData = "AF000000100100000000000000000000000000C0";
       return sendHexData(hexData);
   }
   public boolean sWitchHdmi(){
        //HDMI：AB 00 00 00 3B 00 00 00 00 00 00 00 00 00 02 00 00 00 00 E8
       String hexData = "AB0000003B0000000000000000000200000000E8";
       return sendHexData(hexData);
   }
   public boolean setPosition(){
        //镜头位置菜单：AB 00 00 00 3B 00 00 00 00 00 00 00 00 00 00 00 08 00 00 EE
       String hexData = "AF0000023B0000000000000000000000080000F4";
       return sendHexData(hexData);
   }
   public boolean setScale(){
        //镜头缩放菜单：AB 00 00 00 3B 00 00 00 00 00 00 00 00 00 00 00 10 00 00 F6
       String hexData = "AF0000023B0000000000000000000000100000FC";
       return sendHexData(hexData);
   }
   public boolean setFocus(){
        //镜头聚焦菜单：AB 00 00 00 3B 00 00 00 00 00 00 00 00 00 00 00 20 00 00 06
       String hexData = "AF0000023B00000000000000000000002000000C";
       return sendHexData(hexData);
   }
    public boolean sendUpSlow(){
        //上：AB 00 00 00 3B 00 00 00 00 00 00 00 00 00 00 02 00 00 00 E8
        String hexData = "AF0000023B0000000000000000000002000008F6";
        return sendHexData(hexData);
    }
    public boolean sendDownSlow(){
        //下：AB 00 00 00 3B 00 00 00 00 00 00 00 00 00 00 08 00 00 00 EE
        String hexData = "AF0000023B0000000000000000000008000008FC";
        return sendHexData(hexData);
    }
    public boolean sendLeftSlow(){
        //左：AB 00 00 00 3B 00 00 00 00 00 00 00 00 00 40 00 00 00 00 26
        String hexData = "AF0000023B000000000000000000400000000834";
        return sendHexData(hexData);
    }
    public boolean sendRightSlow(){
        //右：AB 00 00 00 3B 00 00 00 00 00 00 00 00 00 00 40 00 00 00 26
        String hexData = "AF0000023B000000000000000000004000000834";
        return sendHexData(hexData);
    }
   public boolean sendUp(){
        //上：AB 00 00 00 3B 00 00 00 00 00 00 00 00 00 00 02 00 00 00 E8
       String hexData = "AB0000003B0000000000000000000002000000E8";
       return sendHexData(hexData);
   }
   public boolean sendDown(){
        //下：AB 00 00 00 3B 00 00 00 00 00 00 00 00 00 00 08 00 00 00 EE
       String hexData = "AB0000003B0000000000000000000008000000EE";
       return sendHexData(hexData);
   }
   public boolean sendLeft(){
        //左：AB 00 00 00 3B 00 00 00 00 00 00 00 00 00 40 00 00 00 00 26
       String hexData = "AB0000003B000000000000000000400000000026";
       return sendHexData(hexData);
   }
   public boolean sendRight(){
        //右：AB 00 00 00 3B 00 00 00 00 00 00 00 00 00 00 40 00 00 00 26
       String hexData = "AB0000003B000000000000000000004000000026";
       return sendHexData(hexData);
   }

   private boolean sendHexData(String hexData){
       byte[] data =FileOperator.hexStr2Bytes(hexData);
       //Log.i(TAG,"send Project data: " + data);
       if (serialPortUtils != null) {
           serialPortUtils.sendSerialPort(data);
       }else{
           Log.i(TAG,"send Project cmd error,device is not opened ");
           return false;
       }
       return true;
   }
}
