package com.im.listener;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import com.im.R;
import com.im.activity.ChatActivity;
import com.im.bean.Msg;
import com.im.bean.Session;
import com.im.db.ChatMsgDao;
import com.im.db.SessionDao;
import com.im.service.MsfService;
import com.im.util.Const;
import com.im.util.PreferencesUtils;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Switch;


/**
 * @author baiyuliang
 */

@SuppressWarnings("static-access")
public class MsgListener implements MessageListener{
	
	private MsfService context;
	private NotificationManager mNotificationManager;
	
	
	private Notification mNotification;
	private KeyguardManager mKeyguardManager = null;
	
	private boolean isShowNotice=false;
	
	private ChatMsgDao msgDao;
	private SessionDao sessionDao;
	
	public MsgListener(MsfService context,NotificationManager mNotificationManager){
		this.context=context;
		this.mNotificationManager=mNotificationManager;
		mKeyguardManager = (KeyguardManager) context.getSystemService(context.KEYGUARD_SERVICE);    
		sessionDao=new SessionDao(context);
		msgDao=new ChatMsgDao(context);
	}
	
	@Override
	public void processMessage(Chat arg0, Message message) {
        Log.e("message", message.toXML());
        try {
			String msgContent = message.getBody();
			if (TextUtils.isEmpty(msgContent))
				return;

			String chatType = message.getType().toString();//聊天类型
            String msgType = message.getSubType().toString(); //MSG类型
			String to = message.getTo();
			String from = message.getFrom();
			String subType = message.getSubType();
            String msgTime= message.getTime();//消息时间

			String[] strFrom = from.split("@");
			String[] strTo = to.split("@");

			Session session=new Session();

			Msg msg=new Msg();

			switch(msgType) {
				case Const.MSG_TYPE_TEXT:
					msg.setToUser(strTo[0]);
					msg.setFromUser(strFrom[0]);
					msg.setIsComing(0);
					msg.setContent(msgContent);
					msg.setDate(msgTime);
					msg.setIsReaded("0");
					msg.setType(chatType);
					msgDao.insert(msg);
					sendNewMsg(msg);
                    showNotice(strFrom[0] + ':' + msgContent, strFrom[0]);

					session.setFrom(strFrom[0]);
					session.setTo(strTo[0]);
					session.setType(Const.MSG_TYPE_CHAT);
					session.setContent(msgContent);
					if(sessionDao.isContent(strFrom[0], strTo[0])){//判断最近联系人列表是否已存在记录
						sessionDao.updateSession(session);
					}else{
						sessionDao.insertSession(session);
					}
					break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	void sendNewMsg(Msg msg){
		Intent intent=new Intent(Const.ACTION_NEW_MSG);//发送广播到聊天界面
		Bundle b=new Bundle();
		b.putSerializable("msg", msg);
		intent.putExtra("msg", b);
		context.sendBroadcast(intent);
	}
	
	@SuppressWarnings("deprecation")
	public void showNotice(String content, String from) {
		// 更新通知栏
        CharSequence tickerText = content;
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("from", from);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mNotification = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_notice)
                .setContentText(tickerText)
                .setContentTitle("您收到了新消息")
                .setContentIntent(pendingIntent)
                .setNumber(1)
                .getNotification();

		//mNotification = new Notification(R.drawable.ic_notice, tickerText, System.currentTimeMillis());
		mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		if(PreferencesUtils.getSharePreBoolean(context, Const.MSG_IS_VOICE)){
			// 设置默认声音
			mNotification.defaults |= Notification.DEFAULT_SOUND;
		}
		if(PreferencesUtils.getSharePreBoolean(context, Const.MSG_IS_VIBRATE)){
			// 设定震动(需加VIBRATE权限)
			mNotification.defaults |= Notification.DEFAULT_VIBRATE;
		}
		// LED灯
		mNotification.defaults |= Notification.DEFAULT_LIGHTS;
		mNotification.ledARGB = 0xff00ff00;
		mNotification.ledOnMS = 500;
		mNotification.ledOffMS = 1000;
		mNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
		mNotificationManager.notify(Const.NOTIFY_ID, mNotification);// 通知
	}
	
 }

