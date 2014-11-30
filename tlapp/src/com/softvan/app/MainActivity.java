package com.softvan.app;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MainActivity extends Activity {
	/**
	 * 底部菜单栏
	 */
	GridView menuGrid, toolbarGrid;
	final Context myApp = this;
	// private Handler mHandler = new Handler();
	private String HOMEPAGE = "http://114.215.184.44/w/index.ac";

	/** 浏览器 **/
	public static WebView wv;
	/** 进度条 */
	private ProgressBar progressBar;
	/** Toolbar底部菜单选项下标 **/
	private final int TOOLBAR_ITEM_PAGEHOME = 0;// 首页
	private final int TOOLBAR_ITEM_BACK = 1;// 退后
	private final int TOOLBAR_ITEM_FORWARD = 2;// 前进
	private final int TOOLBAR_ITEM_REFRESH = 3;// 刷新
	private final int TOOLBAR_ITEM_EXIT = 4;// 退出

	public static final int FILECHOOSER_RESULTCODE = 1;
	private static final int REQ_CAMERA = FILECHOOSER_RESULTCODE + 1;
	private static final int REQ_CHOOSE = REQ_CAMERA + 1;

	/** 底部菜单图片 **/
	int[] menu_toolbar_image_array = { R.drawable.controlbar_homepage,
			R.drawable.controlbar_backward_enable,
			R.drawable.controlbar_forward_enable, R.drawable.menu_refresh,
			R.drawable.controlbar_menu };
	/** 底部菜单文字 **/
	String[] menu_toolbar_name_array = { "首页", "后退", "前进",
			// "停止",
			"刷新", "更多" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/** 标题栏的布局 **/
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		progressBar = (ProgressBar) findViewById(R.id.updatepbar);// 进度条
		wv = (WebView) findViewById(R.id.webView1);

		wv.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(final WebView view,
					final String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				wv.loadUrl("file:///android_asset/404.html");
			}
		});
		WebSettings webSettings = wv.getSettings();
		webSettings.setJavaScriptEnabled(true);
		// webSettings.setSavePassword(true);
		// webSettings.setSaveFormData(true);
		// webSettings.setSupportZoom(false);
		// wv.setScrollBarStyle(0);

		// 支持页面弹出alert框
		wv.setWebChromeClient(new MyWebChromeClient());

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
					/*
					 * if (validStatusCode(HOMEPAGE)) { wv.loadUrl(HOMEPAGE);
					 * 
					 * } else { wv.loadUrl("file:///android_asset/404.html");
					 * pbar.setVisibility(View.GONE);// 进度条
					 * findViewById(R.id.pbtext).setVisibility(View.GONE);//
					 * 显示的文字 }
					 */
					break;
				case TOOLBAR_ITEM_BACK:
					wv.loadUrl("http://192.168.8.101/w/index.ac");
					// wv.goBack();
					break;
				case TOOLBAR_ITEM_FORWARD:
					// wv.goForward();
					wv.loadUrl("http://192.168.8.102/w/index.ac");
					break;
				case TOOLBAR_ITEM_REFRESH:
					// wv.reload();
					wv.loadUrl("http://192.168.8.103/w/index.ac");
					break;
				case TOOLBAR_ITEM_EXIT:
					// dialog();
					// wv.loadUrl("javascript:$('.deploy-sidebar').click();");
					wv.loadUrl("http://192.168.8.104/w/index.ac");
					break;
				}
			}
		});
		if (isConnect(this) == false) {
			MainActivity.this.createDialog(this,
					R.string.NoRouteToHostException,
					R.string.NoSignalException, R.string.nosure,
					R.string.apn_is_wrong2_setnet,
					android.provider.Settings.ACTION_WIRELESS_SETTINGS);
			wv.loadUrl("file:///android_asset/404.html");
		} else {

			wv.loadUrl(HOMEPAGE);

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

	// // 按下系统返回键的处理方法
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	// if (wv.canGoBack()) {
	// wv.goBack();
	// return true;
	//
	// } else {
	// dialog();
	// }
	//
	// }
	// return super.onKeyDown(keyCode, event);
	// }

	// 退出系统确认方法
	protected void dialog() {
		AlertDialog.Builder builder = new Builder(MainActivity.this);
		builder.setMessage("确定要退出吗?");
		builder.setTitle("提示");
		builder.setPositiveButton("确认",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// Intent getMyLocationService = new Intent(
						// MainMenuView.this, GetMyLocationService.class);
						// MainMenuView.this.stopService(getMyLocationService);
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
			long s = System.currentTimeMillis();
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
				if (info != null && info.isConnected()) {
					// 判断当前网络是否已经连接
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			Log.v("error", e.toString());e.printStackTrace();
		}
		return false;
	}

	/**
	 * 弹出软件信息提示框
	 * 
	 * @param context
	 * @param title
	 *            提示框标题
	 * @param msg
	 *            提示信息
	 * @param leftButton
	 *            左边按钮名称
	 * @param rightButton
	 *            右边按钮名称
	 * @param activity
	 *            要执行的activity
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

	private ValueCallback<Uri> mUploadMessage;

	/**
	 * 支持页面弹出alert框
	 */
	private class MyWebChromeClient extends WebChromeClient {
		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				final android.webkit.JsResult result) {
			new AlertDialog.Builder(myApp)
					.setTitle("提示信息")
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

		/**
		 * 处理confirm弹出框
		 */
		@Override
		public boolean onJsConfirm(WebView view, String url, String message,
				JsResult result) {
			// 对confirm的简单封装
			new AlertDialog.Builder(myApp)
					.setTitle("Confirm")
					.setMessage(message)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							}).create().show();
			result.confirm();
			return true;
			// 如果采用下面的代码会另外再弹出个消息框，目前不知道原理
			// return super.onJsConfirm(view, url, message, result);
		}

		/**
		 * 处理prompt弹出框
		 */
		@Override
		public boolean onJsPrompt(WebView view, String url, String message,
				String defaultValue, JsPromptResult result) {
			result.confirm();
			return super.onJsPrompt(view, url, message, message, result);
		}

		// 页面加载进度条
		public void onProgressChanged(WebView view, int progress) {// 载入进度改变而触发
			if (progress == 100) {
				progressBar.setVisibility(View.GONE);// 进度条
				findViewById(R.id.pbtext).setVisibility(View.GONE);// 显示的文字
			} else {
				progressBar.setVisibility(View.VISIBLE);// 进度条
				findViewById(R.id.pbtext).setVisibility(View.VISIBLE);// 显示的文字
			}
			super.onProgressChanged(view, progress);
		}

		// For Android 3.0+
		public void openFileChooser(ValueCallback<Uri> uploadMsg,
				String acceptType) {
			if (mUploadMessage != null)
				return;
			mUploadMessage = uploadMsg;
			selectImage();
		}

		// For Android < 3.0
		public void openFileChooser(ValueCallback<Uri> uploadMsg) {
			openFileChooser(uploadMsg, "");
		}

		// For Android > 4.1.1
		public void openFileChooser(ValueCallback<Uri> uploadMsg,
				String acceptType, String capture) {
			openFileChooser(uploadMsg, acceptType);
		}
	}

	/**
	 * 检查SD卡是否存在
	 * 
	 * @return
	 */
	public final boolean checkSDcard() {
		boolean flag = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if (!flag) {
			Toast.makeText(this, "请插入手机存储卡再使用本功能", Toast.LENGTH_SHORT).show();
		}
		return flag;
	}

	String compressPath = "";

	protected final void selectImage() {
		try {
			if (!checkSDcard())
				return;
			String[] selectPicTypeStr = { "拍照", "相册" };
			AlertDialog alertDialog = new AlertDialog.Builder(this)
			 .setCancelable(false)
					.setItems(selectPicTypeStr,
							new DialogInterface.OnClickListener() {
								// @Override
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									// 相机拍摄
									case 0:
										openCarcme();
										break;
									// 手机相册
									case 1:
										chosePic();
										break;
									default:
										break;
									}
									compressPath = Environment
											.getExternalStorageDirectory()
											.getPath()
											+ "/tongli_wmp/temp";
									new File(compressPath).mkdirs();
									compressPath = compressPath
											+ File.separator + (System.currentTimeMillis())+".jpg";
								}
							}).create();
			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.show();
		} catch (Exception e) {
			Log.v("error", e.toString());e.printStackTrace();
		}
	}

	String imagePaths;
	Uri cameraUri;

	/**
	 * 打开照相机
	 */
	private void openCarcme() {
		try {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			imagePaths = Environment.getExternalStorageDirectory().getPath()
					+ "/tongli_wmp/temp/"
					+ (System.currentTimeMillis() + ".jpg");
			// 必须确保文件夹路径存在，否则拍照后无法完成回调
			File vFile = new File(imagePaths);
			if (!vFile.exists()) {
				File vDirPath = vFile.getParentFile();
				vDirPath.mkdirs();
			} else {
				if (vFile.exists()) {
					vFile.delete();
				}
			}
			cameraUri = Uri.fromFile(vFile);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
			startActivityForResult(intent, REQ_CAMERA);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.v("error", e.toString());e.printStackTrace();
		}
	}

	/**
	 * 拍照结束后
	 */
	private void afterOpenCamera() {
		try {
			File f = new File(imagePaths);
			Log.v("error",""+ f.exists());
			addImageGallery(f);
			FileUtils.compressFile(f.getPath(), compressPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.v("error", e.toString());e.printStackTrace();
		}
	}

	/** 解决拍照后在相册中找不到的问题 */
	private void addImageGallery(File file) {
		try {
			if(file==null||!file.exists()){
				Log.v("error", "文件不存在");
			}
			ContentValues values = new ContentValues();
			values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
			values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
			getContentResolver().insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.v("error", e.toString());e.printStackTrace();
		}
	}

	/**
	 * 本地相册选择图片
	 */
	private void chosePic() {
		try {
//			FileUtils.delFile(compressPath);
			Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); // "android.intent.action.GET_CONTENT"
			String IMAGE_UNSPECIFIED = "image/*";
			innerIntent.setType(IMAGE_UNSPECIFIED); // 查看类型
			Intent wrapperIntent = Intent.createChooser(innerIntent, null);
			startActivityForResult(wrapperIntent, REQ_CHOOSE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.v("error", e.toString());e.printStackTrace();
		}
	}

	/**
	 * 选择照片后结束
	 * 
	 * @param data
	 */
	private Uri afterChosePic(Intent data) {

		try {
			// 获取图片的路径：
			String[] proj = { MediaStore.Images.Media.DATA };
			if(data==null){
				return null;
			}
			// 好像是android多媒体数据库的封装接口，具体的看Android文档
			Cursor cursor = managedQuery(data.getData(), proj, null, null, null);
			if (cursor == null) {
				Toast.makeText(this, "上传的图片仅支持png或jpg格式", Toast.LENGTH_SHORT)
						.show();
				return null;
			}
			// 按我个人理解 这个是获得用户选择的图片的索引值
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			// 将光标移至开头 ，这个很重要，不小心很容易引起越界
			cursor.moveToFirst();
			// 最后根据索引值获取图片路径
			String path = cursor.getString(column_index);
			if (path != null
					&& (path.endsWith(".png") || path.endsWith(".PNG")
							|| path.endsWith(".jpg") || path.endsWith(".JPG"))) {
				File newFile = FileUtils.compressFile(path, compressPath);
				return Uri.fromFile(newFile);
			} else {
				Toast.makeText(this, "上传的图片仅支持png或jpg格式", Toast.LENGTH_SHORT)
						.show();
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			Log.v("error", e.toString());e.printStackTrace();
		}
		return null;
	}

	/**
	 * 返回文件选择
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		try {
			if (null == mUploadMessage) {
				return;
			}
			Uri uri = null;
			if (requestCode == REQ_CAMERA) {
				afterOpenCamera();
				uri = cameraUri;
			} else if (requestCode == REQ_CHOOSE) {
				uri = afterChosePic(intent);
			}
			mUploadMessage.onReceiveValue(uri);
			mUploadMessage = null;
			super.onActivityResult(requestCode, resultCode, intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.v("error", e.toString());e.printStackTrace();
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && wv.canGoBack()) {
			wv.goBack();
			return true;
		} else {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
