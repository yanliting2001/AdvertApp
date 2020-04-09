package com.grandartisans.advert.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.grandartisans.advert.R;

import java.util.List;


public class AreaListAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mLData;
    private int curPosition;

    public AreaListAdapter(Context context, List<String> mlist) {
        mContext = context;
        mLData = mlist;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewCache viewCache;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.common_area_list_item, null);
            viewCache = new ViewCache(convertView);
            convertView.setTag(viewCache);
        } else {
            viewCache = (ViewCache) convertView.getTag();
        }
        String item = mLData.get(position % mLData.size());

        TextView TitleTextView = viewCache.getTitleTextView();
        TitleTextView.setText(item);
        if (curPosition == position) {
            TitleTextView.setTextSize(26);
            if (parent.hasFocus())
                TitleTextView.setSelected(true);
        } else {
            TitleTextView.setTextSize(26);
            if (parent.hasFocus())
                TitleTextView.setSelected(false);
        }

        return convertView;
    }

    public void setPosition(int p) {
        curPosition = p;
        notifyDataSetChanged();
    }

    public int getCount() {
        return Integer.MAX_VALUE;
    }

    public Object getItem(int position) {
        position = position % mLData.size();
        if (mLData != null && mLData.size() > position) {
            return mLData.get(position);
        } else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public class ViewCache {
        private View baseView;
        private TextView TitleTextView;

        public ViewCache(View baseView) {
            this.baseView = baseView;
        }

        public TextView getTitleTextView() {
            if (TitleTextView == null) {
                TitleTextView = (TextView) baseView.findViewById(R.id.areaText);
            }
            return TitleTextView;
        }
    }
}
