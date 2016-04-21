package com.im.activity;


import com.im.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class WelcomeActivity extends Activity {
	protected static final String TAG = "WelcomeActivity";
	private Context mContext;
	private ImageView mImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_welcome);
		mContext = this;
		findView();
		init();
	}

	private void findView() {
		mImageView = (ImageView) findViewById(R.id.iv_welcome);
	}

	private void init() {
		mImageView.postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent =null;
				//因自动登录后，有时获取不到好友列表，暂时去掉
//				String username=PreferencesUtils.getSharePreStr(WelcomeActivity.this, "username");
//				String pwd=PreferencesUtils.getSharePreStr(WelcomeActivity.this, "pwd");
//				if(!TextUtils.isEmpty(username)&&!TextUtils.isEmpty(pwd)){
//					intent = new Intent(mContext, MainActivity.class);
//					intent.putExtra("isStartService", true);
//				}else{
//					intent = new Intent(mContext, LoginActivity.class);
//				}
				//Log.d("hualaishi", "谈笑风生的欢迎界面");
				intent = new Intent(mContext, LoginActivity.class);
				startActivity(intent);
				finish();
			}
		},2000);
		
	}
}
