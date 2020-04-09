package com.grandartisans.advert.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class AlwaysMarqueeTextView extends AppCompatTextView {

	public boolean focus = false;
	public AlwaysMarqueeTextView(Context context) {
		super(context);
	}

	public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlwaysMarqueeTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

    @Override
    public boolean isFocused() {
    	// TODO Auto-generated method stub
    	return true;
    }
}
