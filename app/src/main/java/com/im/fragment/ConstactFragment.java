package com.im.fragment;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;

import com.im.QQApplication;
import com.im.R;
import com.im.activity.AddFriendActivity;
import com.im.activity.ChatActivity;
import com.im.activity.InfoActivity;
import com.im.adapter.ConstactAdapter;
import com.im.bean.Child;
import com.im.bean.Group;
import com.im.util.Const;
import com.im.util.ToastUtil;
import com.im.view.IphoneTreeView;
import com.im.view.TitleBarView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

public class ConstactFragment extends Fragment {
	private Context mContext;
	private View mBaseView;
	private TitleBarView mTitleBarView;
	private IphoneTreeView mIphoneTreeView;
	private ConstactAdapter mExpAdapter;
	private List<Group> listGroup=new ArrayList<Group>();;

	private FriendsOnlineStatusReceiver friendsOnlineStatusReceiver;

	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 1:
					initData();
					break;
				case 2:
					freshData();
					break;

			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		mContext = getActivity();
		mBaseView = inflater.inflate(R.layout.fragment_constact_father, null);
		friendsOnlineStatusReceiver=new FriendsOnlineStatusReceiver();
		IntentFilter intentFilter=new IntentFilter(Const.ACTION_FRIENDS_ONLINE_STATUS_CHANGE);
		mContext.registerReceiver(friendsOnlineStatusReceiver, intentFilter);
		findView();
		return mBaseView;
	}

	private void findView() {
		mTitleBarView=(TitleBarView) mBaseView.findViewById(R.id.title_bar);
		mTitleBarView.setCommonTitle(View.GONE, View.VISIBLE, View.GONE, View.GONE);
		mTitleBarView.setTitleText(R.string.constacts);//标题
//		mTitleBarView.setBtnLeftOnclickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				findFriends();
//			}
//		});
//		mTitleBarView.setBtnRightOnclickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent intent=new Intent(mContext, AddFriendActivity.class);
//				startActivity(intent);
//			}
//		});
		mIphoneTreeView = (IphoneTreeView) mBaseView.findViewById(R.id.iphone_tree_view);
		mIphoneTreeView.setHeaderView(LayoutInflater.from(mContext).inflate(R.layout.fragment_constact_head_view, mIphoneTreeView, false));
		mIphoneTreeView.setGroupIndicator(null);
		mIphoneTreeView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView arg0, View arg1, int arg2,int arg3, long arg4) {
				Intent intent =new Intent(mContext, InfoActivity.class);
				if(listGroup.get(arg2).getChildList().get(arg3).getUsername()!=null)
					intent.putExtra("name",listGroup.get(arg2).getChildList().get(arg3).getUsername());
				if(listGroup.get(arg2).getChildList().get(arg3).getGroup()!=null)
					intent.putExtra("group",listGroup.get(arg2).getChildList().get(arg3).getGroup());
				if(listGroup.get(arg2).getChildList().get(arg3).getVcard().getAvatar()!=null)
					intent.putExtra("icon", BitmapFactory.decodeStream(new ByteArrayInputStream(listGroup.get(arg2).getChildList().get(arg3).getVcard().getAvatar())));
				if(listGroup.get(arg2).getChildList().get(arg3).getVcard().getField("TITLE")!=null)
					intent.putExtra("title", listGroup.get(arg2).getChildList().get(arg3).getVcard().getField("TITLE"));
				if(listGroup.get(arg2).getChildList().get(arg3).getVcard().getField("NOTE")!=null)
					intent.putExtra("note", listGroup.get(arg2).getChildList().get(arg3).getVcard().getField("NOTE"));
				if(listGroup.get(arg2).getChildList().get(arg3).getVcard().getField("EMAIL")!=null)
					intent.putExtra("email", listGroup.get(arg2).getChildList().get(arg3).getVcard().getField("EMAIL"));
				if(listGroup.get(arg2).getChildList().get(arg3).getVcard().getField("ADR")!=null)
					intent.putExtra("adr", listGroup.get(arg2).getChildList().get(arg3).getVcard().getField("ADR"));
				if(listGroup.get(arg2).getChildList().get(arg3).getVcard().getField("TEL")!=null)
					intent.putExtra("tel", listGroup.get(arg2).getChildList().get(arg3).getVcard().getField("TEL"));
				startActivity(intent);
				return true;
			}
		});
		if(QQApplication.xmppConnection==null){
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mHandler.sendEmptyMessage(1);
				}
			}, 1000);
		}else{
			initData();
		}
	}

	/**
	 * 加载数据
	 */
	void initData(){
		findFriends();
		if(listGroup.size()<=0){
			mIphoneTreeView.setVisibility(View.GONE);
			ToastUtil.showShortToast(mContext, "暂无好友");
			return;
		}
		mExpAdapter = new ConstactAdapter(mContext, listGroup, mIphoneTreeView);
		mIphoneTreeView.setAdapter(mExpAdapter);
	}

	void freshData(){
		findFriends();
		if(listGroup.size()<=0){
			mIphoneTreeView.setVisibility(View.GONE);
			ToastUtil.showShortToast(mContext, "暂无好友");
			return;
		}
		mExpAdapter.notifyDataSetChanged();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void findFriends() {
		try {
			listGroup.clear();
			XMPPConnection conn = QQApplication.xmppConnection;
			Roster roster = conn.getRoster();
			Collection<RosterGroup> groups = roster.getGroups();
			for (RosterGroup group : groups) {
				Group mygroup=new Group();
				mygroup.setGroupName(group.getName());
				Collection<RosterEntry> entries = group.getEntries();
				List<Child> childListNotOnline=new ArrayList<Child>();//不在线
				List<Child> childListOnline=new ArrayList<Child>();//在线
				for (RosterEntry entry : entries) {
					if(entry.getType().name().equals("both")){
						Presence presence = roster.getPresence(entry.getUser());
						Child child=new Child();
						VCard vcard=new VCard();
						vcard.load(conn,entry.getUser());
						child.setVcard(vcard);
						child.setUsername(entry.getUser().split("@")[0]);
						child.setGroup(mygroup.getGroupName());
						if(presence.isAvailable()){
							if(!TextUtils.isEmpty(presence.getStatus())) {
								child.setMood("[" + presence.getStatus() + "]");
							}
							else {
								child.setMood("[在线]");
							}
							child.setOnline_status("1");
							childListOnline.add(child);
						}else{//下线
							child.setMood("[离开]");
							child.setOnline_status("0");
							childListNotOnline.add(child);
						}
					}
				}
				childListOnline.addAll(childListNotOnline);//在线的靠前排列
				mygroup.setChildList(childListOnline);

				listGroup.add(mygroup);
			}
			Log.e("jj", "好友数量="+listGroup.get(0).getChildList().size());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	class FriendsOnlineStatusReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context arg0, Intent intent) {
//			String from=intent.getStringExtra("from");
//			int status=intent.getIntExtra("status",0);
//			if(!TextUtils.isEmpty(from)){
//				if(status== 1){
//					ToastUtil.showShortToast(mContext, from+"上线了");
//				}else if(status==0){
//					ToastUtil.showShortToast(mContext, from+"下线了");
//				}
//			}
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mHandler.sendEmptyMessage(2);

				}
			}, 1000);
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mContext.unregisterReceiver(friendsOnlineStatusReceiver);
	}

}
