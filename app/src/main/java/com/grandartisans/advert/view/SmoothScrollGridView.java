package com.grandartisans.advert.view;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ListAdapter;

public class SmoothScrollGridView extends GridView {

	private int itemsCount;
	private int itemHeight;
	private ListAdapter adapter;
	private int scrollDuration = 2000;
	private boolean isScrollTop;
	private Timer timer;
	private OnScrollBottomListener onScrollBottomListener;
	private OnScrollTopListener onScrollTopListener;

	public SmoothScrollGridView(Context context) {
		this(context, null);
	}

	public SmoothScrollGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOnKeyListener(smoothOnKeyListener);
		this.setSmoothScrollbarEnabled(true);

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (adapter != null) {
			// 获取每个item 的高度，因为要调用滑动的方法，每次滑动的距离就是item 的高度
			itemHeight = this.getChildAt(0).getHeight();
		}

	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		this.adapter = adapter;
		// 获取listview
		// item的count，一定要是由adapter获得，不能通过listView，因为listView是动态添加删除孩子的，可以打印一下比较看看
		itemsCount = adapter.getCount();

	}

	/**
	 * 设置滚动动画的滚动时间
	 * 
	 * @param scrollDutation
	 */
	public void setScrollDuration(int scrollDutation) {
		this.scrollDuration = scrollDutation;

	}

	public OnKeyListener smoothOnKeyListener = new OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_UP) {
				return false;
			}
			// 获取当前被选中的状态
			int currentItemPosition = getSelectedItemPosition();

			if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {

				// 当时倒数第二个的时候
				if (currentItemPosition == itemsCount - 2) {
					// 如果listView的最后一个可见Item是倒数第二个item，或者是倒数第一个item同时timer不为空，这时要滚动一次，并让最后一个item获取焦点
					if (getLastVisiblePosition() == itemsCount - 2 || (getLastVisiblePosition() == itemsCount - 1 && timer != null)) {
						smoothScrollBy(itemHeight, scrollDuration);

						if (timer == null) {
							smoothScrollToBottom();
						} else {
							timer.cancel();
							timer = null;
							// 延迟一下，再让最后一个item编程selected状态，不让没有动画，太突兀
							smoothScrollToBottom();
						}

						// this.smoothScrollToPositionFromTop(itemsCount - 1, 0,
						// scrollDuration);
						// this.setSelection(itemsCount - 1);
					}
					return false;
				} else if (currentItemPosition == itemsCount - 1) {
					// 当是最后一个item是selectionItem，则给出回调，让他不要在滚动了
					if (onScrollBottomListener != null) {
						onScrollBottomListener.onScrollBottom();
					}
					return true;
				} else {
					// 是中间其他状态的时候，滚动一个item的距离
					smoothScrollBy(itemHeight, scrollDuration);
					// this.smoothScrollToPositionFromTop(currentItemPosition +
					// 1,
					// 0, scrollDuration);
					return false;
				}
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				if (currentItemPosition == 1) {

					if (getChildAt(0).isFocusable() == false) {
						smoothScrollBy(-itemHeight, scrollDuration);
					}
					return false;
				} else if (currentItemPosition == 0) {

					if (onScrollTopListener != null) {
						onScrollTopListener.onScrollTop();
					}

					return isScrollTop;
				} else {
					smoothScrollBy(-itemHeight, scrollDuration);
					return false;
				}
			}

			return false;
		}
	};

	private void smoothScrollToBottom() {
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				SmoothScrollGridView.this.post(new Runnable() {

					@Override
					public void run() {
						SmoothScrollGridView.this.setSelection(SmoothScrollGridView.this.getLastVisiblePosition());

					}
				});

			}
		}, scrollDuration / 3);
	}

	/**
	 * 当滚动到底部的时候的监听
	 */
	public interface OnScrollBottomListener {
		void onScrollBottom();
	}

	public void setOnScrollBottomListener(OnScrollBottomListener onScrollBottomListener) {
		this.onScrollBottomListener = onScrollBottomListener;
	}

	/**
	 * 当滚动到顶部的时候的监听
	 */
	public interface OnScrollTopListener {
		void onScrollTop();
	}

	public void setOnScrollTopListener(OnScrollTopListener onScrollTopListener) {
		isScrollTop = true;
		this.onScrollTopListener = onScrollTopListener;
	}

}