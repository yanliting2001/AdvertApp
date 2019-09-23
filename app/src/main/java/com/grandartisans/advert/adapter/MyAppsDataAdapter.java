package com.grandartisans.advert.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.grandartisans.advert.R;
import com.grandartisans.advert.utils.ValidUtils;
import com.grandartisans.advert.view.RotateTextView;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class MyAppsDataAdapter extends BaseAdapter {
	public List<Map<String, Object>> list;
	private Context context;
	private int SIZE = 0;
	private String type;
	private int selitem = 0;
	private int randomTemp = -1;
	int[] arrayOfInt;

	public MyAppsDataAdapter(Context context, List<Map<String, Object>> result, String type) {
		this.context = context;
		this.list = result;
		// SIZE = pageNum;
		// this.type = type;
		// list = new ArrayList<Map<String, Object>>();
		// int i = page * SIZE;
		// int iEnd = i + SIZE;
		// while ((i < result.size()) && (i < iEnd)) {
		// list.add(result.get(i));
		// i++;
		// }
		// arrayOfInt = new int[7];
		// arrayOfInt[0] = R.drawable.blue_no_shadow;
		// arrayOfInt[1] = R.drawable.dark_no_shadow;
		// arrayOfInt[2] = R.drawable.green_no_shadow;
		// arrayOfInt[3] = R.drawable.orange_no_shadow;
		// arrayOfInt[4] = R.drawable.pink_no_shadow;
		// arrayOfInt[5] = R.drawable.red_no_shadow;
		// arrayOfInt[6] = R.drawable.yellow_no_shadow;
	}

	public void seleitem(int seleitem) {
		this.selitem = seleitem;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Map<String, Object> map = new HashMap<String, Object>();
		map = list.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.apps_grid_itemview, null);
		}
		// convertView.setBackgroundResource(arrayOfInt[createRandom()]);

		PackageManager pm = context.getPackageManager();
		// FrameLayout imageViewLayout = (FrameLayout) convertView
		// .findViewById(R.id.myapp_fram_bg);
		ImageView app_icon = (ImageView) convertView.findViewById(R.id.app_icon);
		TextView textName = (TextView) convertView.findViewById(R.id.app_name);
		RotateTextView rotateTextView = (RotateTextView) convertView.findViewById(R.id.topright_tip);
		textName.setText(map.get("title").toString());
		Drawable ico;
		try {
			ico = pm.getApplicationIcon((String) map.get("package"));
			app_icon.setImageDrawable(ico);
			if (ValidUtils.validIsRunning(context, map.get("package").toString())) {
				map.put("isrunning", "running");
				rotateTextView.setText(context.getResources().getString(R.string.tips_isrunning));
				rotateTextView.setVisibility(View.VISIBLE);
			} else {
				map.put("isrunning", "none");
				rotateTextView.setVisibility(View.GONE);
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		if (position == selitem) {
			// imageViewLayout.setBackgroundResource(R.drawable.myapp_item_focus);
			// convertView.setScaleX(80);
			// convertView.setScaleY(80);
		} else {
			// imageViewLayout.setBackgroundResource(Color.TRANSPARENT);
			// convertView.setScaleX(50);
			// convertView.setScaleY(50);
		}
		return convertView;
	}

	private int createRandom() {
		// Random localRandom = new Random();
		// for (int i = localRandom.nextInt(paramInt);; i = localRandom
		// .nextInt(paramInt)) {
		// if (i != this.randomTemp) {
		// this.randomTemp = i;
		// return i;
		// }
		// }
		Random randon = new Random();
		int index = randon.nextInt(arrayOfInt.length);
		return index;
	}

}
