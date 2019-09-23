package com.grandartisans.advert.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.grandartisans.advert.R;
import com.grandartisans.advert.adapter.MyAppsDataAdapter;
import com.grandartisans.advert.app.AdvertApp;
import com.grandartisans.advert.receiver.PackageState;
import com.grandartisans.advert.utils.Go3CPlayerDef;
import com.grandartisans.advert.view.AppManagerSubMenuPop;
import com.grandartisans.advert.view.SmoothScrollGridView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MyAppActivity extends Activity {

	final private String APP_BROWSER = "app_browser";
	private String appmode = APP_BROWSER;
	private SmoothScrollGridView appsGridView;
	private ImageView backImg;
	private List<Map<String, Object>> installed = new ArrayList<Map<String, Object>>();
	private TextView myAppLoading;
	private Map<String, Object> map;
	private PackageState packageState = null;
	private AdvertApp app;
	private Handler playerHandler;
	private Context mcontext;
	private int curPosition = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_myapp);
		Intent intent = getIntent();
		appmode = intent.getStringExtra("appmode");
		mcontext = MyAppActivity.this;
		app = (AdvertApp) getApplication();
		//playerHandler = app.getPlayerHandler();
		TextView titleView = (TextView) findViewById(R.id.myapp_title);
		if (appmode.equals(APP_BROWSER)) {
			titleView.setText(R.string.activity_button_myapp);
			initPackageState();
		} else {
			titleView.setText(R.string.activity_button_appmanager);
			initPackageState();
		}
		myAppLoading = (TextView) findViewById(R.id.myAppLoading);
		appsGridView = (SmoothScrollGridView) findViewById(R.id.apps_grid_view);
		appsGridView.setOnKeyListener(new gridViewOnkey());
		backImg = (ImageView) findViewById(R.id.backImg);

		backImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startAppMenuActivity();
			}
		});

		getAppsDataView();
		// GetAppsAsync getapps = new GetAppsAsync();
		// getapps.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		// getapps.execute();
		map = new HashMap<String, Object>();

		// initcomment();
		closeMenuDelay();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (packageState != null)
			packageState.unregisterPackageListener(mcontext);
	}

	class gridViewOnkey implements OnKeyListener {

		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			closeMenuDelay();
			if (arg1 == KeyEvent.KEYCODE_DPAD_LEFT && arg2.getAction() == KeyEvent.ACTION_DOWN) {
				int position = appsGridView.getSelectedItemPosition();
				int Columns = appsGridView.getNumColumns();
				if (position % Columns == 0) {
					startAppMenuActivity();
					return true;
				}
			} else if (arg1 == KeyEvent.KEYCODE_BACK && arg2.getAction() == KeyEvent.ACTION_DOWN) {
				startAppMenuActivity();
				return true;
			}
			appsGridView.smoothOnKeyListener.onKey(arg0, arg1, arg2);
			return false;
		}

	}

	private void startAppMenuActivity() {
		/*
		Intent it = new Intent();
		it.setClass(this, AppMenuActivity.class);
		startActivity(it);
		*/
		this.finish();
	}

	private void getAppsDataView() {
		if (!appmode.equals(APP_BROWSER)) {
			for (Map<String, Object> map : app.getInstalled()) {
				int isSystem = (Integer) map.get("issystem");
				if (isSystem == 0) {
					installed.add(map);
				}
			}
		} else {
			for (Map<String, Object> map : app.getInstalled()) {
				String packageName = (String) map.get("package");

				if (!"com.adobe.flashplayer".equals(packageName) && !"com.android.browser".equals(packageName) && !"com.android.providers.downloads.ui".equals(packageName) && !"com.android.settings".equals(packageName) && !"com.softwinner.miracastReceiver".equals(packageName)) {
					installed.add(map);
				}
			}
		}

		setSeriesGrid(MyAppActivity.this, installed);
	}

	private void initPackageState() {
		packageState = new PackageState();
		packageState.init(mcontext);
		packageState.setPackageStateChangeListener(new PackageState.PackageStateChangeListener() {

			@Override
			public void onInstall(String packageName) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onRemove(String packageName) {
				// TODO Auto-generated method stub
				uninstallUpdate(packageName);
			}

		});
	}

	public void uninstallUpdate(String packageName) {
		/*
		 * System.out.println("uninstall update del main menu app id = " +
		 * packageName); removeApp(packageName);
		 * setSeriesGrid(MyAppActivity.this, installed);
		 */
		// 我的应用中处理卸载应用的UI操作
		try {
			if (AppManagerSubMenuPop.appsDataAdapter != null && AppManagerSubMenuPop.appsDataAdapter.list != null && AppManagerSubMenuPop.appsDataAdapter.list.size() > 0) {
				for (int i = 0; i < AppManagerSubMenuPop.appsDataAdapter.list.size(); i++) {
					if (AppManagerSubMenuPop.appsDataAdapter.list.get(i).get("issystem") != null && "0".equals(AppManagerSubMenuPop.appsDataAdapter.list.get(i).get("issystem").toString())) {// 卸载
						if (AppManagerSubMenuPop.appsDataAdapter.list.get(i).equals(AppManagerSubMenuPop.map)) {
							AppManagerSubMenuPop.appsDataAdapter.list.remove(i);
						}
					}
				}
			}
			AppManagerSubMenuPop.appsDataAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void removeApp(String packageName) {
		if (installed != null) {
			int size = installed.size();
			for (int i = 0; i < size; i++) {
				Map<String, Object> map = installed.get(i);
				if (map.get("package").equals(packageName)) {
					installed.remove(i);
					break;
				}
			}
		}
	}

	private void setSeriesGrid(Context context, List<Map<String, Object>> list) {
		if (list == null || list.size() == 0) {// 如果返回空说明没数据，则给予列表为空的提示
			myAppLoading.setVisibility(View.VISIBLE);
			myAppLoading.setText(getResources().getString(R.string.local_list_empty));
		} else {// 如果有数据则隐藏列表为空的提示，并加载数据
			myAppLoading.setVisibility(View.GONE);

			appsGridView.setAdapter(new MyAppsDataAdapter(context, list, "series"));
			appsGridView.setNumColumns(4);
			appsGridView.setOnItemClickListener(mOnAppItemClick);
		}
	}

	private void closeMenuDelay() {
		/*
		playerHandler.removeMessages(Go3CPlayerDef.HIDE_ACTIVITY_CMD);

		Message msg = new Message();
		msg.what = Go3CPlayerDef.HIDE_ACTIVITY_CMD;
		msg.obj = this;
		//playerHandler.sendMessageDelayed(msg, Utils.getSubmenuCloseTime(this) * 1000);
		playerHandler.sendMessageDelayed(msg, 10 * 1000);
		*/
	}

	public void updateApp() {
		setSeriesGrid(MyAppActivity.this, installed);
	}

	OnItemClickListener mOnAppItemClick = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			MyAppsDataAdapter adapter = (MyAppsDataAdapter) arg0.getAdapter();
			map = (Map<String, Object>) adapter.getItem(arg2);
			if (appmode.equals(APP_BROWSER)) {
				// String packageName = (String) map.get("package");
				// Constants.openAPP(MyAppActivity.this, packageName);
				AppManagerSubMenuPop appManagerSubMenuPop = new AppManagerSubMenuPop(mcontext, map, adapter);
				appManagerSubMenuPop.showPopupWindow(arg1, 176, -202);
			} else {
				curPosition = arg2;
				AppManagerSubMenuPop appManagerSubMenuPop = new AppManagerSubMenuPop(mcontext, map, adapter);
				appManagerSubMenuPop.showPopupWindow(arg1, 176, -202);
			}
		}
	};

	@Override
	public boolean onKeyDown(int arg0, KeyEvent arg1) {
		if (arg0 == KeyEvent.KEYCODE_DPAD_LEFT && myAppLoading.getVisibility() == View.VISIBLE && getResources().getString(R.string.local_list_empty).equals(myAppLoading.getText())) {
			startAppMenuActivity();
			return true;
		} else if (arg0 == KeyEvent.KEYCODE_BACK && myAppLoading.getVisibility() == View.VISIBLE && getResources().getString(R.string.local_list_empty).equals(myAppLoading.getText())) {
			startAppMenuActivity();
			return true;
		}
		return super.onKeyDown(arg0, arg1);
	}
}
