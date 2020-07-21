package com.grandartisans.advert.utils;

import android.content.Context;
import android.util.Log;

import com.grandartisans.advert.app.constant.SystemConstants;
import com.grandartisans.advert.interfaces.ElevatorDoorEventListener;
import com.tencent.mmkv.MMKV;

public class ElevatorDoorManager {
    private final String TAG = "ElevatorDoorManager";
    private SerialPortUtils serialPortUtils = null;
    private ElevatorDoorEventListener mElevatorDoorEventListener=null;
    private int distance = 0;
    private int strength = 0;
    private int threshold_distance = 0;
    private int lastdistance = 0;
    private int mtfminError = 0;

    static final int DOOR_STATE_INIT  = 0;
    static final int DOOR_STATE_OPENED  = 1;
    static final int DOOR_STATE_CLOSED = 2;

    private int mDoorStatus = DOOR_STATE_INIT;
    private static ElevatorDoorManager mElevatorDoorInstance = null;

    private MMKV mKV;
    public static ElevatorDoorManager getInstance() {
        if (mElevatorDoorInstance == null) {
            synchronized (ElevatorDoorManager.class) {
                if (mElevatorDoorInstance == null) {
                    mElevatorDoorInstance = new ElevatorDoorManager();
                }
            }
        }
        return mElevatorDoorInstance;
    }
    public ElevatorDoorManager(){
        mKV =  MMKV.defaultMMKV();
        threshold_distance = mKV.decodeInt("tfmin_threshold",0);
        setDoorStatus(DOOR_STATE_INIT);
        initserialPort();
        openSerialPort();
    }
    public void setDefaultDistance(int defaultDistance){
        threshold_distance = defaultDistance;
        mKV.encode("tfmin_threshold",defaultDistance);
    }
    public int getDefaultDistance(){
        return threshold_distance;
    }
    public int getDoorStatus(){
        return mDoorStatus;
    }
    public boolean isDoorOpened(){
        if(mDoorStatus == DOOR_STATE_OPENED) return true;
        else return false;
    }
    private void setDoorStatus(int  status){
        mDoorStatus = status;
    }
    public void  registerListener(ElevatorDoorEventListener eventListener){
        if(eventListener!=null)
            mElevatorDoorEventListener = eventListener;
    }
    public void openSerialPort(){
        if (serialPortUtils != null) {
            if (serialPortUtils.openSerialPort(SystemConstants.TFMIN_MOUDLE_NAME,SystemConstants.TFMIN_MOUDLE_BOUDRATE) == null){
            //if (serialPortUtils.openSerialPort("/dev/" + CommonUtil.getTFMiniDevice()) == null){

            }
        }
    }
    public void closeSeriaPort(){
        if (serialPortUtils != null) {
            serialPortUtils.closeSerialPort();
        }
    }
    public void printDoorManagerInfo(){
        Log.i(TAG,"lastdistance= "+ lastdistance + " distance = "  + distance + "threshold_distance = " + threshold_distance);
    }
    private void initserialPort()
    {
        serialPortUtils = new SerialPortUtils();
        //串口数据监听事件
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void onDataReceive(byte[] buffer, int size) {
                //Log.i(TAG,"onDataRecive" + "size = " + size);
                if(!CommonUtil.getTFMiniEnabled()) return;
                byte[] subbuffer = new byte[9];
                if(size>=9) {
                    for (int i = 0; i < size / 9; i++) {
                        int leftLen = size - i*9;
                        if(leftLen<9) break;
                        System.arraycopy(buffer,i*9,subbuffer,0,9);
                        int value = dealWithData(subbuffer, 9);
                        if (value == 0) {
                            mtfminError += 1;
                            if (mtfminError > 300) {
                                if (mElevatorDoorEventListener != null)
                                    mElevatorDoorEventListener.onElevatorError();
                            }
                        } else {
                            distance = value;
                            mtfminError = 0;
                        }
                        if (serialPortUtils.serialPortStatus) {
                            //handler.post(ReadThread);
                            if (lastdistance != distance && Math.abs(distance - lastdistance) > 5) {
                                Log.i(TAG, "lastdistance = " + lastdistance + "distance = " + distance + "threshold_distance= " + threshold_distance);
                                if (threshold_distance > 0 && (distance - threshold_distance > 10)) {
                                    if (getDoorStatus() != DOOR_STATE_OPENED) {
                                        if (mElevatorDoorEventListener != null)
                                            mElevatorDoorEventListener.onElevatorDoorOpen();
                                        setDoorStatus(DOOR_STATE_OPENED);
                                    }
                                } else {
                                    if (getDoorStatus() != DOOR_STATE_CLOSED) {
                                        if (mElevatorDoorEventListener != null)
                                            mElevatorDoorEventListener.onElevatorDoorClose();
                                        setDoorStatus(DOOR_STATE_CLOSED);
                                    }
                                }
                                if (lastdistance != distance) {
                                    lastdistance = distance;
                                }

                            }
                            if (mElevatorDoorEventListener != null) {
                                //Log.i(TAG,"ondistance changed distanc = " + distance);
                                mElevatorDoorEventListener.onValueChanged(distance);
                            }
                        }
                    }
                }
            }
        });

    }

    private int  dealWithData(byte[] buffer, int size)
    {
        int distance;
        if(size!=9) {
            Log.i(TAG,"receiv data error ,size= "+ size + " is not 9 bytes");
            distance = 0;
            return distance;
        }
        if(buffer[0] != 0x59 || buffer[1] !=0x59) {
            Log.i(TAG,"receiv data error ,head data0 = "+ buffer[0] +"data1 = "+buffer[1]);
            distance = 0;
            return distance;
        }
        int low = buffer[2]&0xff;
        int high = buffer[3]&0xff;
        distance = low + high*256;
        low = buffer[4]&0xff;
        high = buffer[5]&0xff;
        strength = low + high*256;
        /*
		Log.i(TAG,"strength = " + strength + "dist is " + distance);
		for(int i=0;i<size;i++) {
			Log.i(TAG,""+ (buffer[i]&0xff));
		}
        */
		return distance;
    }
}
