package com.grandartisans.advert.view;

import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.grandartisans.advert.R;
import com.grandartisans.advert.utils.DecodeImgUtil;

public class SubDisplay extends Presentation {
    private final String TAG = "SubDisplay";
    private Context mContext;
    private TextView tv;
    private LinearLayout main_ll;
    private Bitmap defaultBitMap = null;
    public SubDisplay(Context outerContext, Display display) {
        super(outerContext, display);
        mContext =outerContext;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.welcome_activity);
        main_ll = (LinearLayout) findViewById(R.id.main_ll);
        defaultBitMap = new DecodeImgUtil(mContext, R.raw.subdisplay).getBitmap();
        BitmapDrawable bd= new BitmapDrawable(defaultBitMap);
        main_ll.setBackground(bd);
    }
    public void updateSubDisplay(String imgPath){
        Log.i(TAG,"updateSubDisplay image path = " + imgPath);
        if(imgPath!=null && !imgPath.isEmpty()) {
            Bitmap bm = BitmapFactory.decodeFile(imgPath);
            BitmapDrawable bd = new BitmapDrawable(bm);
            main_ll.setBackground(bd);
        }else{
            BitmapDrawable bd= new BitmapDrawable(defaultBitMap);
            main_ll.setBackground(bd);
        }
    }
}
