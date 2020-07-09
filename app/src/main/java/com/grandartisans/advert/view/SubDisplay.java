package com.grandartisans.advert.view;

import android.app.Presentation;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.grandartisans.advert.R;
import com.grandartisans.advert.utils.DecodeImgUtil;

public class SubDisplay extends Presentation {
    private final String TAG = "SubDisplay";
    private Context mContext;
    private View mViewLayout;
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
        mViewLayout = getLayoutInflater().inflate(R.layout.welcome_activity, null);
        setContentView(mViewLayout);
        //We need to wait till the view is created so we can flip it and set the width & height dynamically
        mViewLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener());
    }
    private class OnGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener{

        @Override
        public void onGlobalLayout() {
            //height is ready
            mViewLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            int width = mViewLayout.getWidth();
            int height = mViewLayout.getHeight();

            mViewLayout.setTranslationX((width - height) / 2);
            mViewLayout.setTranslationY((height - width) / 2);
            mViewLayout.setRotation(90.0f);

            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams( height, width);

            // Inflate the layout.
            setContentView(mViewLayout, lp);

            findViews();
        }
    }
    private void findViews(){
        //setContentView(R.layout.welcome_activity);
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
