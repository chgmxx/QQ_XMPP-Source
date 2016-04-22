package com.im.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.XMPPException;

import com.im.QQApplication;
import com.im.R;
import com.im.adapter.ChatAdapter;
import com.im.adapter.FaceVPAdapter;
import com.im.bean.Msg;
import com.im.bean.Session;
import com.im.db.ChatMsgDao;
import com.im.db.SessionDao;
import com.im.util.Const;
import com.im.util.ExpressionUtil;
import com.im.util.PreferencesUtils;
import com.im.util.ToastUtil;
import com.im.util.XmppUtil;
import com.im.util.VoiceUtil;
import com.im.view.DropdownListView;
import com.im.view.DropdownListView.OnRefreshListenerHeader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


/**
 * 聊天界面
 * @author 白玉梁
 * @blog http://blog.csdn.net/baiyuliang2013
 * @weibo http://weibo.com/274433520
 * 
 * */
@SuppressLint("SimpleDateFormat")
public class ChatActivity extends Activity implements OnClickListener,OnRefreshListenerHeader{
	private ViewPager mViewPager;
	private LinearLayout mDotsLayout;
	private EditText input;
	private TextView send;
	private DropdownListView mListView;
	private ChatAdapter mLvAdapter;
	private ChatMsgDao msgDao;
	private SessionDao sessionDao;
	
	private LinearLayout chat_face_container,chat_add_container;
	private ImageView image_face;//表情图标
	private ImageView image_add;//更多图标
	
	private TextView tv_title,tv_pic,//图片
	tv_camera,//拍照
	tv_voice,//语音
	tv_files;//位置
	
	//表情图标每页6列4行
	private int columns = 6;
	private int rows = 4;
	//每页显示的表情view
	private List<View> views = new ArrayList<View>();
	//表情列表
	private List<String> staticFacesList;
	//消息
	private List<Msg> listMsg;
	private SimpleDateFormat sd;
	private NewMsgReciver newMsgReciver;
	private MsgOperReciver msgOperReciver;
	private LayoutInflater inflater;
	private int offset;
	private String I,YOU;//为了好区分，I就是自己，YOU就是对方
	private String StrI, StrYou;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				mLvAdapter.notifyDataSetChanged();
				break;
			}
		}
	};
	
	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_chat);

		I = PreferencesUtils.getSharePreStr(this, "username");
		YOU = getIntent().getStringExtra("from");

        //StrI = getIntent().getStringExtra("strTo");
        //StrYou = getIntent().getStringExtra("strFrom");

		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		tv_title=(TextView) findViewById(R.id.tv_title);

		tv_title.setText(YOU);

		sd=new SimpleDateFormat("MM-dd HH:mm");
		msgDao=new ChatMsgDao(this);
		sessionDao=new SessionDao(this);

		msgOperReciver=new MsgOperReciver();
        IntentFilter intentFilter=new IntentFilter(Const.ACTION_MSG_OPER);
        registerReceiver(msgOperReciver, intentFilter);

		newMsgReciver=new NewMsgReciver();
		intentFilter=new IntentFilter(Const.ACTION_NEW_MSG);
		registerReceiver(newMsgReciver, intentFilter);

		staticFacesList=ExpressionUtil.initStaticFaces(this);
		initViews();
		//初始化控件

		initViewPager();
		//初始化表情

		initAdd();
		//初始化更多选项（即表情图标右侧"+"号内容）

		initData();
		//初始化数据

		updateMsgToReaded();
		//更新与该用户的聊天记录全部为已读
	}
	
	
	private void updateMsgToReaded() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				msgDao.updateAllMsgToRead(YOU,I);
			}
		}).start();
	}


	/**
	 * 初始化控件
	 */
	private void initViews() {
		mListView = (DropdownListView) findViewById(R.id.message_chat_listview);
		//表情图标
		image_face=(ImageView) findViewById(R.id.image_face);
		//更多图标
		image_add=(ImageView) findViewById(R.id.image_add);
		//表情布局
		chat_face_container=(LinearLayout) findViewById(R.id.chat_face_container);
		//更多
		chat_add_container=(LinearLayout) findViewById(R.id.chat_add_container);
		
		mViewPager = (ViewPager) findViewById(R.id.face_viewpager);
		mViewPager.setOnPageChangeListener(new PageChange());
		//表情下小圆点
		mDotsLayout = (LinearLayout) findViewById(R.id.face_dots_container);
		input = (EditText) findViewById(R.id.input_sms);
		send = (TextView) findViewById(R.id.send_sms);
		input.setOnClickListener(this);
		
		//表情按钮
		image_face.setOnClickListener(this);
		//更多按钮
		image_add.setOnClickListener(this);
		// 发送
		send.setOnClickListener(this);
		
		mListView.setOnRefreshListenerHead(this);
		mListView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(arg1.getAction()==MotionEvent.ACTION_DOWN){
					if(chat_face_container.getVisibility()==View.VISIBLE){
						chat_face_container.setVisibility(View.GONE);
					}
					if(chat_add_container.getVisibility()==View.VISIBLE){
						chat_add_container.setVisibility(View.GONE);
					}
					hideSoftInputView();
				}
				return false;
			}
		});
	}
	
	public void initAdd(){
		tv_pic=(TextView) findViewById(R.id.tv_pic);
		tv_camera=(TextView) findViewById(R.id.tv_camera);
		tv_files=(TextView) findViewById(R.id.tv_files);
		tv_voice = (TextView) findViewById(R.id.tv_voice);
		
		tv_pic.setOnClickListener(this);
		tv_camera.setOnClickListener(this);
		tv_files.setOnClickListener(this);
		tv_voice.setOnClickListener(this);
		
	}
	
	public void initData(){
		offset=0;
		listMsg=msgDao.queryMsg(YOU,I,offset);
		offset=listMsg.size();
        mLvAdapter = new ChatAdapter(this, listMsg);
		mListView.setAdapter(mLvAdapter);
		mListView.setSelection(listMsg.size());
	}
	
	/**
	 * 初始化表情 
	 */
	private void initViewPager() {
		int pagesize= ExpressionUtil.getPagerCount(staticFacesList.size(),columns,rows);
		// 获取页数
		for (int i = 0; i <pagesize; i++) {
			views.add(ExpressionUtil.viewPagerItem(this, i, staticFacesList,columns, rows, input));
			LayoutParams params = new LayoutParams(16, 16);
			mDotsLayout.addView(dotsItem(i), params);
		}
		FaceVPAdapter mVpAdapter = new FaceVPAdapter(views);
		mViewPager.setAdapter(mVpAdapter);
		mDotsLayout.getChildAt(0).setSelected(true);
	}

	/**
	 * 表情页切换时，底部小圆点
	 * @param position
	 * @return
	 */
	private ImageView dotsItem(int position) {
		View layout = inflater.inflate(R.layout.dot_image, null);
		ImageView iv = (ImageView) layout.findViewById(R.id.face_dot);
		iv.setId(position);
		return iv;
	}
	
	/**
	 */
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	
	
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.send_sms:
			String content=input.getText().toString();
			if(TextUtils.isEmpty(content)){
				return;
			}
			sendMsgText(content);
			break;
		case R.id.input_sms:
			if(chat_face_container.getVisibility()==View.VISIBLE){
				chat_face_container.setVisibility(View.GONE);
			}
			if(chat_add_container.getVisibility()==View.VISIBLE){
				chat_add_container.setVisibility(View.GONE);
			}
			break;
		case R.id.image_face:
			hideSoftInputView();//隐藏软键盘
			if(chat_add_container.getVisibility()==View.VISIBLE){
				chat_add_container.setVisibility(View.GONE);
			}
			if(chat_face_container.getVisibility()==View.GONE){
				chat_face_container.setVisibility(View.VISIBLE);
			}else{
				chat_face_container.setVisibility(View.GONE);
			}
			break;
		case R.id.image_add:
			hideSoftInputView();//隐藏软键盘
			if(chat_face_container.getVisibility()==View.VISIBLE){
				chat_face_container.setVisibility(View.GONE);
			}
			if(chat_add_container.getVisibility()==View.GONE){
				chat_add_container.setVisibility(View.VISIBLE);
			}else{
				chat_add_container.setVisibility(View.GONE);
			}
			break;
		case R.id.tv_pic://模拟一张图片路径
			//sendMsgImg("http://my.csdn.net/uploads/avatar/3/B/9/1_baiyuliang2013.jpg");
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 1);
			break;
		case R.id.tv_camera://拍照，换个美女图片吧
			sendMsgImg("http://b.hiphotos.baidu.com/image/pic/item/55e736d12f2eb93872b0d889d6628535e4dd6fe8.jpg");
			break;
		case R.id.tv_files://发送文件
			//sendMsgLocation("116.404,39.915");
			break;
	    case R.id.tv_voice://发送语音
			MediaRecorder mediaRecorder =new MediaRecorder();
			break;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			Uri uri = data.getData();
            sendMsgImg(uri.toString());
		}
	}
	
	/**
	 * 执行发送消息 图片类型
	 * @param imgpath
	 */
	void sendMsgImg(String imgpath){
		Msg msg=getChatInfoTo(imgpath,Const.MSG_TYPE_IMG);
		msg.setMsgId(msgDao.insert(msg));
		listMsg.add(msg);
		offset=listMsg.size();
		mLvAdapter.notifyDataSetChanged();
		final String message=YOU+Const.SPLIT+I+Const.SPLIT+Const.MSG_TYPE_IMG+Const.SPLIT+imgpath+Const.SPLIT+sd.format(new Date());
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					XmppUtil.sendMessage(QQApplication.xmppConnection, message, YOU, I);
				} catch (XMPPException e) {
					e.printStackTrace();
					Looper.prepare();
					ToastUtil.showShortToast(ChatActivity.this, "发送失败");
					Looper.loop();
				}
			}
		}).start();
		updateSession(Const.MSG_TYPE_CHAT,"[图片]");
	}
	
	/**
	 * 执行发送消息 文本类型
	 * @param content
	 */
	void sendMsgText(String content){
		Msg msg=getChatInfoTo(content,Const.MSG_TYPE_CHAT);
        msg.setMsgId(msgDao.insert(msg));
		listMsg.add(msg);
		offset=listMsg.size();
		mLvAdapter.notifyDataSetChanged();
		input.setText("");
		//final String message=YOU+Const.SPLIT+I+Const.SPLIT+Const.MSG_TYPE_TEXT+Const.SPLIT+content+Const.SPLIT+sd.format(new Date());
	    final String message = content;
        new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					XmppUtil.sendMessage(QQApplication.xmppConnection, message, YOU, I);
				} catch (XMPPException e) {
					e.printStackTrace();
					Looper.prepare();
					ToastUtil.showShortToast(ChatActivity.this, "发送失败");
					Looper.loop();
				}
			}
		}).start();
		updateSession(Const.MSG_TYPE_CHAT,content);
	}
	
	/**
	 * 执行发送地址 文本类型
	 * @param content
	 */
	void sendMsgLocation(String content){
		Msg msg=getChatInfoTo(content,Const.MSG_TYPE_LOCATION);
		msg.setMsgId(msgDao.insert(msg));
		listMsg.add(msg);
		offset=listMsg.size();
		mLvAdapter.notifyDataSetChanged();
		final String message=YOU+Const.SPLIT+I+Const.SPLIT+Const.MSG_TYPE_LOCATION+Const.SPLIT+content+Const.SPLIT+sd.format(new Date());
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					XmppUtil.sendMessage(QQApplication.xmppConnection, message, YOU);
				} catch (XMPPException e) {
					e.printStackTrace();
					Looper.prepare();
					ToastUtil.showShortToast(ChatActivity.this, "发送失败");
					Looper.loop();
				}
			}
		}).start();
		updateSession(Const.MSG_TYPE_CHAT,"[位置]");
	}
	
	/**
	 * 发送的信息
	 *  from为收到的消息，to为自己发送的消息
	 * @param message => 接收者卍发送者卍消息类型卍消息内容卍发送时间
	 * @return
	 */
	private Msg getChatInfoTo(String message,String msgtype) {
		String time=sd.format(new Date());
		Msg msg = new Msg();
		msg.setFromUser(YOU);
		msg.setToUser(I);
		msg.setType(msgtype);
		msg.setIsComing(1);
		msg.setContent(message);
		msg.setDate(time);
		return msg;
	}
	
	void updateSession(String type,String content){
		Session session=new Session();
		session.setFrom(YOU);
		session.setTo(I);
		session.setNotReadCount("");//未读消息数量
		session.setContent(content);
		session.setTime(sd.format(new Date()));
		session.setType(type);
		if(sessionDao.isContent(YOU, I)){
			sessionDao.updateSession(session);
		}else{
			sessionDao.insertSession(session);
		}
		Intent intent=new Intent(Const.ACTION_ADDFRIEND);//发送广播，通知消息界面更新
		sendBroadcast(intent);
	}
	
	
	/**
	 * 表情页改变时，dots效果也要跟着改变
	 * */
	class PageChange implements OnPageChangeListener {
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		@Override
		public void onPageSelected(int arg0) {
			for (int i = 0; i < mDotsLayout.getChildCount(); i++) {
				mDotsLayout.getChildAt(i).setSelected(false);
			}
			mDotsLayout.getChildAt(arg0).setSelected(true);
		}
	}

	/**
	 * 下拉加载更多
	 */
	@Override
	public void onRefresh() {
		List<Msg> list=msgDao.queryMsg(YOU,I,offset);
		if(list.size()<=0){
			mListView.setSelection(0);
			mListView.onRefreshCompleteHeader();
			return;
		}
		listMsg.addAll(0,list);
		offset=listMsg.size();
		mListView.onRefreshCompleteHeader();
		mLvAdapter.notifyDataSetChanged();
		mListView.setSelection(list.size());
	}
	
	/** 
	 * 弹出输入法窗口 
	 */  
	private void showSoftInputView(final View v) {  
	    new Handler().postDelayed(new Runnable() {  
	        @Override  
	        public void run() {  
	        	((InputMethodManager) v.getContext().getSystemService(Service.INPUT_METHOD_SERVICE)).toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);  
	        }  
	    }, 0);  
	}
	
	/**
	 * 隐藏软键盘
	 */
	public void hideSoftInputView() {
		InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getCurrentFocus() != null)
				manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
	
	/**
	 * 接收消息记录操作广播：删除复制
	 * @author baiyuliang
	 */
	private class MsgOperReciver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			int type=intent.getIntExtra("type", 0);
			final int position=intent.getIntExtra("position", 0);
			if(listMsg.size()<=0){
				return;
			}
			final Msg msg=listMsg.get(position);
			switch (type) {
			case 1://聊天记录操作
				Builder bd = new AlertDialog.Builder(ChatActivity.this);
				String[] items=null;
				if(msg.getType().equals(Const.MSG_TYPE_CHAT)){
					items =  new String[]{"删除记录","删除全部记录","复制文字"};
				}else{
					items =  new String[]{"删除记录","删除全部记录"};
				}
				bd.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						switch (arg1) {
						case 0://删除
							listMsg.remove(position);
							offset=listMsg.size();
							mLvAdapter.notifyDataSetChanged();
							msgDao.deleteMsgById(msg.getMsgId());
							break;
						case 1://删除全部
							listMsg.removeAll(listMsg);
							offset=listMsg.size();
							mLvAdapter.notifyDataSetChanged();
							msgDao.deleteAllMsg(YOU, I);
							break;
						case 2://复制
							ClipboardManager cmb = (ClipboardManager) ChatActivity.this.getSystemService(ChatActivity.CLIPBOARD_SERVICE);
							cmb.setText(msg.getContent());
							Toast.makeText(getApplicationContext(), "已复制到剪切板", Toast.LENGTH_SHORT).show();
							break;
						}
					}
				});
				bd.show();
				break;
			}
				
			}
	}
	
	/**
	 * 接收消息记录操作广播：
	 * @author baiyuliang
	 */
	private class NewMsgReciver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b= intent.getBundleExtra("msg");
			Msg msg=(Msg) b.getSerializable("msg");//用来区分不同的bundle
			listMsg.add(msg);
			offset=listMsg.size();
			mLvAdapter.notifyDataSetChanged();
		}
	}
	
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(msgOperReciver);
		unregisterReceiver(newMsgReciver);
	}
	
    @Override
    protected void onResume() {
 		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				//让输入框获取焦点
				input.requestFocus();
			}
		}, 100);
    	super.onResume();
    };
	
	/**
	 * 监听返回键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			 hideSoftInputView();
				if(chat_face_container.getVisibility()==View.VISIBLE){
					chat_face_container.setVisibility(View.GONE);
				}else if(chat_add_container.getVisibility()==View.VISIBLE){
					chat_add_container.setVisibility(View.GONE);
				}else{
					finish();
				}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	

}
