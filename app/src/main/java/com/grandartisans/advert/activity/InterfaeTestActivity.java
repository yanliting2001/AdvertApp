package com.grandartisans.advert.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.grandartisans.advert.R;
import com.grandartisans.advert.model.AdvertModel;
import com.grandartisans.advert.model.DownloadModel;
import com.grandartisans.advert.model.MovieModel;
import com.grandartisans.advert.model.entity.post.AdvertParameter;
import com.grandartisans.advert.model.entity.post.AppUpgradeParameter;
import com.grandartisans.advert.model.entity.res.AdListHttpResult;
import com.grandartisans.advert.model.entity.res.Advert;
import com.grandartisans.advert.model.entity.res.AdvertFile;
import com.grandartisans.advert.model.entity.res.AdvertPositionVo;
import com.grandartisans.advert.model.entity.res.AdvertVo;
import com.grandartisans.advert.model.entity.res.AppUpgradeData;
import com.grandartisans.advert.model.entity.res.DateSchedule;
import com.grandartisans.advert.model.entity.res.DateScheduleVo;
import com.grandartisans.advert.model.entity.res.HttpResult;
import com.grandartisans.advert.model.entity.res.MovieRes;
import com.grandartisans.advert.model.entity.res.TemplateRegion;
import com.grandartisans.advert.model.entity.res.TimeSchedule;
import com.grandartisans.advert.model.entity.res.TimeScheduleVo;
import com.grandartisans.advert.model.entity.res.TokenData;
import com.grandartisans.advert.model.entity.post.TokenParameter;
import com.grandartisans.advert.model.entity.res.TokenHttpResult;
import com.grandartisans.advert.model.entity.res.UpgradeHttpResult;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.EncryptUtil;
import com.grandartisans.advert.utils.Utils;
import com.ljy.devring.DevRing;
import com.ljy.devring.base.activity.IBaseActivity;
import com.ljy.devring.base.fragment.IBaseFragment;
import com.ljy.devring.http.support.body.ProgressInfo;
import com.ljy.devring.http.support.observer.CommonObserver;
import com.ljy.devring.http.support.observer.DownloadObserver;
import com.ljy.devring.other.RingLog;
import com.ljy.devring.util.FileUtil;
import com.westone.cryptoSdk.Api;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class InterfaeTestActivity extends Activity implements IBaseFragment,IBaseActivity {
    private final String TAG = "InterfaeTestActivity";
    @Override
    public boolean isUseFragment() {
        return true;
    }

    @Override
    public void onSaveState(Bundle bundle) {

    }

    @Override
    public void onRestoreState(Bundle bundle) {

    }

    @Override
    public boolean isUseEventBus() {
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interfae_test);
        Log.d(TAG,"onCreate");
        //getPlayingMovie(0,20,0);

        //downloadFile();

        //getToken();

        //appUpgrade(InterfaeTestActivity.this);

        new Thread(runableDecryptFile).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
    }

    Runnable runableDecryptFile = new Runnable() {
        @Override
        public void run() {
            DecryptFileTest();
        }
    };
    /*
    private void DecryptFileTest(){
        Api api = new Api();
        String path = FileUtil.getExternalCacheDir(getApplicationContext());
        //String path = "/storage/udisk0";
        Log.d(TAG,"DecryptFileTest Start path = " + path);
        //api.DecryptFile(path +"/input.mp4",path + "/output.mp4");
        //Log.d(TAG,"DecryptFileTest finished");
        Log.d(TAG,"DecryptFileTest return Stream start");
        api.DecryptFile(path +"/input.mp4");
        Log.d(TAG,"DecryptFileTest return Stream end");
    }
    */
    private void DecryptFileTest() {
        Api api = new Api();
        String key = api.GenerateKey();
        String iv = api.GenerateIV();
        byte[] plain = new byte[]{1, 2, 3, 4, 5, 6, 7};
        byte[] cipher1 = api.EncryptData(key, iv, plain);
        byte[] plain2 = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        byte[] cipher2 = api.EncryptData(key, iv, plain2);
        api.DecryptData(key, iv, cipher1);
        api.DecryptData(key, iv, cipher2);
        /*
        String file1 = api.FileEnc(plain);
        api.FileDec(file1);
        */
        String path = FileUtil.getExternalCacheDir(getApplicationContext());
        String FileNameIn = path + "/1.txt";
        String FileNameOut = path + "/2.txt";
        String FileNameOut2 = path + "/3.txt";
        long t1 = System.currentTimeMillis();
        long count = 0L;
        long t2 = 0L;
        System.out.println("file enc start enc start time: " + t1 );
        api.EncryptFile(FileNameIn, FileNameOut);
        t1 = System.currentTimeMillis();
        System.out.println("file end end enc end time: " + t1 );
        /*
        while(true) {
            t1 = System.currentTimeMillis();
            byte[] out = api.DecryptFileOnce(FileNameOut);
            t2 = System.currentTimeMillis();
            if (out == null) {
                return;
            }

            System.out.println("DecryptFileOnce " + count++ + " times len = " + out.length + ",time = " + (t2 - t1));
        }
        */
        int ret = api.DecryptFile(FileNameOut, FileNameOut2);
        t1 = System.currentTimeMillis();
        System.out.println("file dec end dec time: " + t1 );
        InputStream inputStream = api.DecryptFile(FileNameOut);
        t1 = System.currentTimeMillis();
        System.out.println("file dec end decstream time: " + t1 );
        int l = 1;
        byte[] re = new byte[1000];

        while(l > 0) {
            try {
                l = inputStream.read(re);
                if (l <= 0) {
                    inputStream.close();
                    break;
                }

                byte[] p = new byte[l];
                System.arraycopy(re, 0, p, 0, l);
            } catch (Exception var22) {
                var22.printStackTrace();
            }
        }

        System.out.println("file dec path result is " + (ret == 0));

    }

    /**
     * 获取正在上映的电影
     *
     * @param start 请求电影的起始位置
     * @param count 获取的电影数量
     * @param type  类型：初始化数据INIT、刷新数据REFRESH、加载更多数据LOADMORE
     */
    private void getPlayingMovie(int start, int count, final int type) {
        MovieModel mIModel = new MovieModel();
        DevRing.httpManager().commonRequest(mIModel.getPlayingMovie(start, count), new CommonObserver<HttpResult<List<MovieRes>>>() {
            @Override
            public void onResult(HttpResult<List<MovieRes>> result) {
                RingLog.d("获取" + result.getTitle() + "成功");
            }

            @Override
            public void onError(int errType, String errMessage) {

            }
        }, null);
    }

    private void appUpgrade (Context context) {
        String signed="";
        AdvertModel mIModel = new AdvertModel();
        AppUpgradeParameter parameter = new AppUpgradeParameter();
        parameter.setAndroidVersion(Utils.getAppVersionCode(context));
        //parameter.setAndroidVersion(1);
        parameter.setAppIdent(Utils.getAppPackageName(context));
        //parameter.setAppIdent("123456");
        parameter.setAppName(Utils.getAppPackageName(context));
        //parameter.setAppName("君匠广告");
        parameter.setDeviceClientid(CommonUtil.getEthernetMac());
        //parameter.setDeviceClientid("1111");
        parameter.setRequestUuid(CommonUtil.getRandomString(50));
        parameter.setSystemVersion("2.0.1");
        parameter.setTimestamp(System.currentTimeMillis());
        parameter.setVersion(Utils.getAppVersionName(context));
        StringBuilder sign = new StringBuilder();
        EncryptUtil encrypt = new EncryptUtil();
        sign.append(parameter.getDeviceClientid()).append("$").append(parameter.getTimestamp()).append("$123456");
        signed = encrypt.MD5Encode(sign.toString(),"");
        parameter.setSign(signed);
        DevRing.httpManager().commonRequest(mIModel.appUpgrade(parameter), new CommonObserver<UpgradeHttpResult>() {
            @Override
            public void onResult(UpgradeHttpResult appUpgradeDataAdHttpResult) {
                RingLog.d("appupgrade ok status = " + appUpgradeDataAdHttpResult.getStatus() );
            }

            @Override
            public void onError(int i, String s) {
                RingLog.d("appupgrade error ");
            }
        },null);
    }
    private void getToken() {
        String signed="";
        AdvertModel mIModel = new AdvertModel();
        TokenParameter tokenParameter = new TokenParameter();
        tokenParameter.setDeviceClientid(CommonUtil.getEthernetMac());
        tokenParameter.setTimestamp(System.currentTimeMillis());
        StringBuilder sign = new StringBuilder();
        EncryptUtil encrypt = new EncryptUtil();
        sign.append(tokenParameter.getDeviceClientid()).append("$").append(tokenParameter.getTimestamp()).append("$123456");
        signed = encrypt.MD5Encode(sign.toString(),"");
        tokenParameter.setSign(signed);

        DevRing.httpManager().commonRequest(mIModel.getToken(tokenParameter), new CommonObserver<TokenHttpResult>() {
            @Override
            public void onResult(TokenHttpResult tokenDataAdHttpResult) {
                RingLog.d("gettoken ok status = " + tokenDataAdHttpResult.getStatus() );
                getAdList(tokenDataAdHttpResult.getData().getToken());
            }

            @Override
            public void onError(int i, String s) {
                RingLog.d("gettoken error i = " + i + "msg = " + s );
            }
        },null);

    }

    private void getAdList(String token) {
        AdvertModel mIModel = new AdvertModel();
        AdvertParameter parameter = new AdvertParameter();
        parameter.setDeviceClientid(CommonUtil.getEthernetMac());
        parameter.setRequestUuid(CommonUtil.getRandomString(50));
        parameter.setTimestamp(System.currentTimeMillis());
        parameter.setToken(token);

        DevRing.httpManager().commonRequest(mIModel.getAdertList(parameter), new CommonObserver<AdListHttpResult>() {
            @Override
            public void onResult(AdListHttpResult result) {
                //RingLog.d("getAdList ok result = " + result );
                if(result.getStatus() ==0 ) {
                    List<TemplateRegion> regionList  = result.getData().getTemplate().getRegionList();


                    TemplateRegion region = regionList.get(0);
                    Long advertPositionId = result.getData().getRelationMap().get(region.getIdent());
                    AdvertPositionVo advertPositionVo = result.getData().getAdvertPositionMap().get(advertPositionId);
                    List<DateScheduleVo> dateScheduleVos  = advertPositionVo.getDateScheduleVos();
                    int size = dateScheduleVos.size();
                        for(int i=0;i<size;i++) {
                            DateScheduleVo dateSchedueVo = dateScheduleVos.get(i);
                            DateSchedule dateSchedue = dateSchedueVo.getDateSchedule();
                            RingLog.d("getAdList Schedue start date = " +  dateSchedue.getStartDate() + "end date=" + dateSchedue.getEndDate());
                            List<TimeScheduleVo> TimeSchedueVos = dateSchedueVo.getTimeScheduleVos();
                            for(int j=0;j<TimeSchedueVos.size();j++){
                                TimeScheduleVo timeScheduleVo = TimeSchedueVos.get(j);
                                TimeSchedule timeSchedule = timeScheduleVo.getTimeSchedule();
                                RingLog.d("getAdList Schedue start time = " +  timeSchedule.getStartTime() + "end time=" + timeSchedule.getEndTime());
                                List<AdvertVo> packageAdverts = timeScheduleVo.getPackageAdverts();
                                for(int k=0;k<packageAdverts.size();k++) {
                                    AdvertVo advertVo = packageAdverts.get(k);
                                    Advert advert = advertVo.getAdvert();
                                    RingLog.d("getAdList advert name = " +  advert.getName() + "advert description :" + advert.getDescription());
                                    List<AdvertFile> fileList= advertVo.getFileList();
                                    for(int l=0;l<fileList.size();l++) {
                                        AdvertFile advertFile = fileList.get(l);
                                        RingLog.d("getAdList advert file md5  = " +  advertFile.getFileMd5() + "path= " + advertFile.getFilePath());
                                    }
                                }
                            }
                        }
                    }

            }

            @Override
            public void onError(int i, String s) {
                RingLog.d("getAdList onError i = " + i + "message = " + s );
            }

        },null);
    }

    DownloadObserver mDownloadObserver;
    private String downloadUrl = "http://update.thewaxseal.cn/apks/advert-signed.apk";
    /**
     * 下载文件
     */
    public void downloadFile() {
        File file = FileUtil.getFile(FileUtil.getExternalCacheDir(this), "CloudMusic_2.apk");
        DownloadModel mIModel = new DownloadModel();
        //不为空则不重新构造DownloadObserver，避免创造了多个进度监听回调
        if (mDownloadObserver == null) {
            //DownloadObserver构造函数传入要要监听的下载地址
            mDownloadObserver = new DownloadObserver(downloadUrl) {
                @Override
                public void onResult(boolean isSaveSuccess, String filePath) {
                    if (isSaveSuccess) {
                        Log.i("InterfaceTest","DownloadSuccess:" + filePath );
                        Utils.installSilently(filePath);
                    }else {
                        Log.i("InterfaceTest","DownloadFailed:" + filePath );
                    }
                }

                @Override
                public void onError(long progressInfoId, String errMessage) {

                }

                @Override
                public void onProgress(ProgressInfo progressInfo) {
                    Log.i("InterfaceTest","DownloadProgress:" + progressInfo.getPercent() );
                }
            };
        }
        DevRing.httpManager().downloadRequest(file, mIModel.downloadFile(downloadUrl), mDownloadObserver, null);
    }
}
