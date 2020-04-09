package com.grandartisans.advert.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.HorizontalScrollView;

public class Myhsv extends HorizontalScrollView{

	public Myhsv(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public Myhsv(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public Myhsv(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected int computeHorizontalScrollOffset() {
		// TODO Auto-generated method stub
		return super.computeHorizontalScrollOffset();
	}

	@Override
	protected int computeHorizontalScrollRange() {
		// TODO Auto-generated method stub
		return super.computeHorizontalScrollRange();
	}

	@Override
	public boolean executeKeyEvent(KeyEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}
