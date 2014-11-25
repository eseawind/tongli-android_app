package com.softvan.app;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

import com.softvan.app.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;


public class MainMenuView extends Activity {
	/**
	 * 底部菜单栏
	 */
	GridView menuGrid, toolbarGrid;
	final Context myApp = this;
	
	
	private Handler mHandler = new Handler();

	// private String HOMEPAGE = "http://192.168.2.124:8080/cxshtcms/vertical/index.html";
	private String HOMEPAGE = "http://114.215.184.44/w/index.ac";

 
	/** 浏览器 **/
	public static WebView wv;
	/** Toolbar底部菜单选项下标 **/
	private final int TOOLBAR_ITEM_PAGEHOME = 0;// 首页
	private final int TOOLBAR_ITEM_BACK = 1;// 退后
	private final int TOOLBAR_ITEM_FORWARD = 2;// 前进
	private final int TOOLBAR_ITEM_REFRESH = 3;// 刷新
	private final int TOOLBAR_ITEM_EXIT = 4;// 退出

	/** 底部菜单图片 **/
	int[] menu_toolbar_image_array = { R.drawable.controlbar_homepage,
			R.drawable.controlbar_backward_enable,
			R.drawable.controlbar_forward_enable, R.drawable.menu_refresh,
			R.drawable.menu_close_window };
	/** 底部菜单文字 **/
	String[] menu_toolbar_name_array = { "首页", "后退", "前进",
			// "停止",
			"刷新", "退出" };

	
	private Timer timer;
	
	  private long timeout = 5000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/** 标题栏的布局 **/
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		final ProgressBar pbar = (ProgressBar) findViewById(R.id.updatepbar);// 进度条
		wv = (WebView) findViewById(R.id.webView1);
		
		 wv.setWebViewClient(new WebViewClient() {
			 public boolean shouldOverrideUrlLoading(final WebView view, final String url){
					view.loadUrl(url);
					return true;
				}
		     
			 @Override
	            public void onReceivedError(WebView view, int errorCode,
	                    String description, String failingUrl) {
				 wv.loadUrl("file:///android_asset/404.html");
	            }
	            

			});

	   WebSettings webSettings = wv.getSettings(); // webView设置
		wv.getSettings().setJavaScriptEnabled(true);



		wv.setScrollBarStyle(0);

		// 支持页面弹出alert框
		wv.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					final android.webkit.JsResult result) {
				new AlertDialog.Builder(myApp)
						.setTitle("javaScript dialog")
						.setMessage(message)
						.setPositiveButton(android.R.string.ok,
								new AlertDialog.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										result.confirm();
									}
								}).setCancelable(false).create().show();

				return true;
			};

			// 页面加载进度条
			public void onProgressChanged(WebView view, int progress) {// 载入进度改变而触发
				if (progress == 100) {
					pbar.setVisibility(View.GONE);// 进度条
					findViewById(R.id.pbtext).setVisibility(View.GONE);// 显示的文字
				}else{
					pbar.setVisibility(View.VISIBLE);// 进度条
					findViewById(R.id.pbtext).setVisibility(View.VISIBLE);// 显示的文字
				}
				super.onProgressChanged(view, progress);
			}

		});

		// 创建底部菜单 Toolbar
		toolbarGrid = (GridView) findViewById(R.id.GridView_toolbar);
		toolbarGrid.setBackgroundResource(R.drawable.channelgallery_bg);// 设置背景
		toolbarGrid.setNumColumns(5);// 设置每行列数
		toolbarGrid.setGravity(Gravity.CENTER);// 位置居中
		toolbarGrid.setVerticalSpacing(10);// 垂直间隔
		toolbarGrid.setHorizontalSpacing(10);// 水平间隔
		toolbarGrid.setAdapter(getMenuAdapter(menu_toolbar_name_array,
				menu_toolbar_image_array));// 设置菜单Adapter
		/** 监听底部菜单选项 **/
		toolbarGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				switch (arg2) {
				case TOOLBAR_ITEM_PAGEHOME:
					wv.loadUrl(HOMEPAGE);
					/*if (validStatusCode(HOMEPAGE)) {
						wv.loadUrl(HOMEPAGE);

					} else {
						wv.loadUrl("file:///android_asset/404.html");
						pbar.setVisibility(View.GONE);// 进度条
						findViewById(R.id.pbtext).setVisibility(View.GONE);// 显示的文字
					}*/
					break;
				case TOOLBAR_ITEM_BACK:
					wv.goBack();
					break;
				case TOOLBAR_ITEM_FORWARD:
					wv.goForward();
					break;
				case TOOLBAR_ITEM_REFRESH:
					wv.reload();
					break;
				case TOOLBAR_ITEM_EXIT:
					dialog();
					break;
				}
			}
		});
		wv.addJavascriptInterface(new JSInterface(), "client");
		
		if (isConnect(this)==false) 
        {   
			MainMenuView.this.createDialog(this, R.string.NoRouteToHostException,
					R.string.NoSignalException, R.string.nosure,
					R.string.apn_is_wrong2_setnet,
					android.provider.Settings.ACTION_WIRELESS_SETTINGS);
			wv.loadUrl("file:///android_asset/404.html");
        }else{
        	
        	wv.loadUrl(HOMEPAGE);
        	
        }
	
		/*if (validStatusCode(HOMEPAGE)) {
			wv.loadUrl(HOMEPAGE);

		} else {
			wv.loadUrl("file:///android_asset/404.html");
			pbar.setVisibility(View.GONE);// 进度条
			findViewById(R.id.pbtext).setVisibility(View.GONE);// 显示的文字
		}*/

	}

	// 提供访问页面访问android的java方法的接口
	public class JSInterface {
		public JSInterface() {

		}

		public void clickOnMap(final double longitude, final double latitude) {
			mHandler.post(new Runnable() {
				public void run() {
					Intent intent = new Intent();
					// Log.v("tags", "aaaaa=");
//					intent.setClass(MainMenuView.this, RouteDemo.class);
					Bundle bundle = new Bundle();
					// 保存输入的信息longitude 经度
					bundle.putDouble("longitude", longitude);
					// 保存输入的信息longitude 纬度
					bundle.putDouble("latitude", latitude);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
		}
	}

	/**
	 * 构造菜单Adapter
	 * 
	 * @param menuNameArray
	 *            名称
	 * @param imageResourceArray
	 *            图片
	 * @return SimpleAdapter
	 */
	private SimpleAdapter getMenuAdapter(String[] menuNameArray,
			int[] imageResourceArray) {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < menuNameArray.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", imageResourceArray[i]);
			map.put("itemText", menuNameArray[i]);
			data.add(map);
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(this, data,
				R.layout.item_menu, new String[] { "itemImage", "itemText" },
				new int[] { R.id.item_image, R.id.item_text });
		return simperAdapter;
	}

	public void loadurl(final WebView view, final String url) {
		new Thread() {
			public void run() {
				view.loadUrl(url);
			}
		}.start();

	}

	// 按下系统返回键的处理方法
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if(wv.canGoBack()){
				wv.goBack();
				return true;
				
			}else{
				dialog();
			}
			
		}  
		return super.onKeyDown(keyCode, event);
	}

	// 退出系统确认方法
	protected void dialog() {
		AlertDialog.Builder builder = new Builder(MainMenuView.this);
		builder.setMessage("确定要退出吗?");
		builder.setTitle("提示");
		builder.setPositiveButton("确认",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
//						Intent getMyLocationService = new Intent(
//								MainMenuView.this, GetMyLocationService.class);
//						MainMenuView.this.stopService(getMyLocationService);
						android.os.Process.killProcess(android.os.Process
								.myPid());
					}
				});
		builder.setNegativeButton("取消",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	// 判断访问服务的状态
	private boolean validStatusCode(String url) {
		try {
			HttpURLConnection.setFollowRedirects(false);
			URL validatedURL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) validatedURL
					.openConnection();
			
			con.setRequestMethod("GET");
			con.setConnectTimeout(6000);
			con.setReadTimeout(6000);
			long s=System.currentTimeMillis();
			int responseCode = con.getResponseCode();
			if (responseCode == 404 || responseCode == 405
					|| responseCode == 504) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean isConnect(Context context) { 
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理） 
    try { 
        ConnectivityManager connectivity = (ConnectivityManager) context 
                .getSystemService(Context.CONNECTIVITY_SERVICE); 
        if (connectivity != null) { 
            // 获取网络连接管理的对象 
            NetworkInfo info = connectivity.getActiveNetworkInfo(); 
            if (info != null&& info.isConnected()) { 
                // 判断当前网络是否已经连接 
                if (info.getState() == NetworkInfo.State.CONNECTED) { 
                    return true; 
                } 
            } 
        } 
    } catch (Exception e) { 
    // TODO: handle exception 
    Log.v("error",e.toString()); 
} 
        return false; 
    } 
	
	
	/**
	 * 弹出软件信息提示框
	 * @param context
	 * @param title 提示框标题
	 * @param msg 提示信息
	 * @param leftButton 左边按钮名称
	 * @param rightButton 右边按钮名称
	 * @param activity 要执行的activity
	 */
	public static void createDialog(final Context context, int title, int msg,
			int leftButton, int rightButton, final String activity) {
		if (context != null) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			dialog.setTitle(title);
			dialog.setMessage(msg);
			dialog.setNegativeButton(leftButton, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			dialog.setPositiveButton(rightButton, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Intent intent = new Intent(activity);
					context.startActivity(intent);
				}
			});
			dialog.show();
		}
	} 

}