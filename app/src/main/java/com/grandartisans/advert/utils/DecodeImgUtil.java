package com.grandartisans.advert.utils;

import java.io.InputStream;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class DecodeImgUtil {
	
	private int ResId;
	private Context mContext;
	public DecodeImgUtil(Context mContext,int ResId){
		this.ResId = ResId;
		this.mContext = mContext;
	}
	
	public Bitmap getBitmap(){
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		//获取资源图片
		InputStream is = mContext.getResources().openRawResource(ResId);
		return BitmapFactory.decodeStream(is,null,opt);
	}
	public Drawable getDrawableImg(){
		BitmapDrawable bd = null;
		if(getBitmap()!=null){
			bd = new BitmapDrawable(getBitmap());
		}
		return bd;
	}
}
