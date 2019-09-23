package com.grandartisans.advert.view;

import java.util.Map;

import android.app.ActionBar.LayoutParams;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.PopupWindow.OnDismissListener;

import com.grandartisans.advert.R;
import com.grandartisans.advert.activity.MyAppActivity;
import com.grandartisans.advert.adapter.MyAppsDataAdapter;
import com.grandartisans.advert.interfaces.IDialogInterface;
import com.grandartisans.advert.utils.CommonUtil;
import com.grandartisans.advert.utils.Go3CPlayerDef;
import com.grandartisans.advert.utils.UIHelper;
import com.grandartisans.advert.utils.Utils;
import com.grandartisans.advert.utils.ValidUtils;

public class AppManagerSubMenuPop {
	private PopupWindow mMenuWindow;
	private View mContainer;
	private Context mContext;
	public static Map<String, Object> map;
	public static MyAppsDataAdapter appsDataAdapter;
	private Handler playerHandler;

	private TextView appmanager_run;
	private TextView appmanager_stop;
	private TextView appmanager_cleardata;
	private TextView appmanager_clearcache;
	private TextView appmanager_uninstall;

	public AppManagerSubMenuPop(Context c, Map<String, Object> m, MyAppsDataAdapter adapter) {
		this.map = m;
		this.mContext = c;
		this.appsDataAdapter = adapter;
		/*
		Go3cApplication app = (Go3cApplication) mContext.getApplicationContext();
		playerHandler = app.getPlayerHandler();
		*/
		findView();
	}

	/**
	 * 
	 * @描述: 显示菜单
	 * @方法名: showPopupWindow
	 * @param view
	 * @param x
	 * @param y
	 * @返回类型 void
	 * @创建人 "liting yan"
	 * @创建时间 2014-5-16下午10:16:40
	 * @修改人 "liting yan"
	 * @修改时间 2014-5-16下午10:16:40
	 * @修改备注
	 * @since
	 * @throws
	 */
	public void showPopupWindow(View view, int x, int y) {
		if (mMenuWindow != null && mMenuWindow.isShowing())
			return;
		mMenuWindow = new PopupWindow(mContainer, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		mMenuWindow.setBackgroundDrawable(new BitmapDrawable());
		mMenuWindow.setAnimationStyle(R.style.mypopwindow_anim_style);

		mMenuWindow.setFocusable(true);
		mMenuWindow.setTouchable(true);
		mMenuWindow.setOutsideTouchable(true);
		mMenuWindow.showAsDropDown(view, x, y);
		mMenuWindow.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {

			}
		});
		closeMenuDelay();
	}

	/**
	 * 
	 * @描述:隐藏菜单
	 * @方法名: hidePopupWindow
	 * @返回类型 void
	 * @创建人 "liting yan"
	 * @创建时间 2014-5-16下午10:17:02
	 * @修改人 "liting yan"
	 * @修改时间 2014-5-16下午10:17:02
	 * @修改备注
	 * @since
	 * @throws
	 */
	public void hidePopupWindow() {
		if (mMenuWindow != null && mMenuWindow.isShowing()) {
			mMenuWindow.dismiss();
		}
	}

	public View findView() {
		if (mContainer == null) {
			mContainer = LayoutInflater.from(mContext).inflate(R.layout.appmanager_submenu, null);
		}
		initAll();
		return mContainer;
	}

	/**
	 * 
	 * @描述:初始化View
	 * @方法名: initAll
	 * @返回类型 void
	 * @创建人 gao
	 * @创建时间 2014-6-3下午7:32:35
	 * @修改人 gao
	 * @修改时间 2014-6-3下午7:32:35
	 * @修改备注
	 * @since
	 * @throws
	 */
	private void initAll() {
		appmanager_run = (TextView) mContainer.findViewById(R.id.appmanager_run);
		appmanager_stop = (TextView) mContainer.findViewById(R.id.appmanager_stop);
		appmanager_cleardata = (TextView) mContainer.findViewById(R.id.appmanager_cleardata);
		appmanager_clearcache = (TextView) mContainer.findViewById(R.id.appmanager_clearcache);
		appmanager_uninstall = (TextView) mContainer.findViewById(R.id.appmanager_uninstall);

		if (map.get("isrunning") != null && "running".equals(map.get("isrunning").toString())) {// 强制停止
			appmanager_stop.setVisibility(View.VISIBLE);
		} else {// 运行
			appmanager_stop.setVisibility(View.GONE);
		}
		/*
		String path = SongsDbManager.wwwrootdir + "/www/data/apk_preinstall.json";
		boolean isPreInstall = ValidUtils.validIsBoot(path, (String) map.get("package"));
		*/
		if ((map.get("issystem") != null && "1".equals(map.get("issystem").toString())) ) {// 卸载：1.系统应用不能卸载；2.预安装应用不能卸载
			appmanager_uninstall.setVisibility(View.GONE);
		} else {// 隐藏卸载按钮
			appmanager_uninstall.setVisibility(View.VISIBLE);
		}

		appmanager_run.setOnKeyListener(new TextViewOnKeyListener());
		appmanager_stop.setOnKeyListener(new TextViewOnKeyListener());
		appmanager_cleardata.setOnKeyListener(new TextViewOnKeyListener());
		appmanager_clearcache.setOnKeyListener(new TextViewOnKeyListener());
		appmanager_uninstall.setOnKeyListener(new TextViewOnKeyListener());

		appmanager_run.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 运行操作
				hidePopupWindow();

				try {
					String packageName = (String) map.get("package");
					// Constants.openAPP(mContext, packageName);

					if (CommonUtil.IsAppInstalled(mContext, packageName)) {
						String mode = "mode";
                        CommonUtil.openAPP(mContext, packageName, mode);

					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		appmanager_stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 强制停止操作
				hidePopupWindow();
                /*
				try {
					ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
					am.forceStopPackage(map.get("package").toString());

					MyAppActivity myAppActivity = (MyAppActivity) mContext;
					myAppActivity.updateApp();
				} catch (Exception e) {
					e.printStackTrace();
				}
                */
			}
		});
		appmanager_cleardata.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 清除数据操作
				hidePopupWindow();
				/*
				try {
					// 清除数据
					ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
					am.clearApplicationUserData(map.get("package").toString(), new ClearDataObserver());

					// 清除缓存
					PackageManager mPm = mContext.getPackageManager();
					mPm.deleteApplicationCacheFiles(map.get("package").toString(), new ClearCacheObserver());

					UIHelper.showMyToast(mContext, mContext.getString(R.string.cleardata_success));
					playerHandler.sendEmptyMessageDelayed(Go3CPlayerDef.CANCEL_TOAST, 3000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				*/
			}
		});
		appmanager_clearcache.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 清楚缓存操作
				hidePopupWindow();
                /*
				try {
					PackageManager mPm = mContext.getPackageManager();
					mPm.deleteApplicationCacheFiles(map.get("package").toString(), new ClearCacheObserver());

					UIHelper.showMyToast(mContext, mContext.getString(R.string.clear_cache_sucess));
					playerHandler.sendEmptyMessageDelayed(Go3CPlayerDef.CANCEL_TOAST, 3000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				*/
			}
		});
		appmanager_uninstall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 卸载
				hidePopupWindow();

				SysDialog dialog = new SysDialog(mContext);
				dialog.setInterface(new IDialogInterface() {
					@Override
					public void onPositive() {
						// TODO Auto-generated method stub
						System.out.println("onPositive click");
						String packName = (String) map.get("package");
						Uri packageURI = Uri.parse("package:" + packName);
						//弹出框卸载
						 Intent uninstallIntent = new
						 Intent(Intent.ACTION_DELETE, packageURI);
						 mContext.startActivity(uninstallIntent);
                        /*
						// 静默卸载
						if (!PackageManagerState.isUninstalling) {
							Intent intent = new Intent();
							intent.setAction("com.go3c.packageinstaller.DEFAULT_UNINSTALL");
							intent.putExtra("uri", packageURI);
							mContext.sendBroadcast(intent);
						} else {
							SysDialog sysDialog = new SysDialog(mContext);
							sysDialog.showDialog3(mContext.getString(R.string.uninstall_tips_ing), mContext.getString(R.string.confirm));
						}
						*/
					}

					@Override
					public void onNegative() {
						// TODO Auto-generated method stub
						System.out.println("onNegative click");
					}
				});
				String title = mContext.getResources().getString(R.string.system_tips);
				String config = mContext.getResources().getString(R.string.confirm);
				String cancel = mContext.getResources().getString(R.string.cancel);
				String message = mContext.getResources().getString(R.string.uninstallapp);
				dialog.showDialog1(title, message, config, cancel);
			}
		});
	}

	private void closeMenuDelay() {
	    /*
		playerHandler.removeMessages(Go3CPlayerDef.HIDE_ACTIVITY_CMD);

		Message msg = new Message();
		msg.what = Go3CPlayerDef.HIDE_ACTIVITY_CMD;
		msg.obj = mContext;
		playerHandler.sendMessageDelayed(msg, Utils.getSubmenuCloseTime(mContext) * 1000);
		*/
	}

	class TextViewOnKeyListener implements OnKeyListener {
		@Override
		public boolean onKey(View arg0, int keyCode, KeyEvent keyEvent) {
			closeMenuDelay();
			if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_LEFT:
					hidePopupWindow();
					return true;
				}
			}
			return false;
		}
	}
}
