package com.softvan.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

/**
 * 
 软件启动Activity
 */
public class InitActivity extends Activity {
	public static final int REFRESH_INIT = 1;
	/** Called when the activity is first created. */

	private ImageView logoView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 取消标题�?
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 全屏
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.logo);
		logoView = (ImageView) this.findViewById(R.id.logoImage);
		AlphaAnimation animation = new AlphaAnimation(0.1f, 1.0f);
		animation.setDuration(1000);
		logoView.startAnimation(animation);
		animation.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			// 当动画结�?
			public void onAnimationEnd(Animation animation) {
				Intent intent = new Intent(InitActivity.this,
						MainActivity.class);
				startActivity(intent);
				// 设置动画
				overridePendingTransition(android.R.anim.fade_in,
						android.R.anim.fade_out);
				finish();
				overridePendingTransition(android.R.anim.fade_in,
						android.R.anim.fade_out);
			}
		});

	}

}