package com.im.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.im.R;
import com.im.view.TitleBarView;

/**
 * Created by L on 2016/4/21.
 */
public class InfoActivity extends Activity implements OnClickListener {
    Context mContext;
    private TitleBarView mTitleBarView;
    private ImageView icon;
    private TextView name;
    private TextView title;
    private TextView group;
    private TextView mobile;
    private TextView email;
    private TextView phone;
    private TextView address;
    private Button sendmessage;




    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        mContext = this;
        findView();
        init();
        initData();
    }

    private void findView(){
        mTitleBarView= (TitleBarView)findViewById(R.id.title_bar);
        mTitleBarView.setCommonTitle(View.GONE, View.VISIBLE, View.GONE, View.GONE);
        mTitleBarView.setTitleText(R.string.constacts);//标题
        icon= (ImageView) findViewById(R.id.pic_info_information);
        name= (TextView) findViewById(R.id.name_info_information);
        title= (TextView) findViewById(R.id.job_info_information);
        group= (TextView) findViewById(R.id.content_group_information);
        mobile= (TextView) findViewById(R.id.content_mobile_information);
        email= (TextView) findViewById(R.id.content_email_information);
        phone= (TextView) findViewById(R.id.content_phone_information);
        address= (TextView) findViewById(R.id.content_address_information);
        sendmessage= (Button) findViewById(R.id.btn_sendmessage);
    }
    private void init(){
        sendmessage.setOnClickListener(this);
    }
    private void initData(){
        if(getIntent().getParcelableExtra("icon")!=null)
            icon.setImageBitmap((Bitmap) getIntent().getParcelableExtra("icon"));
        if(getIntent().getStringExtra("name")!=null)
            name.setText(getIntent().getStringExtra("name"));
        if(getIntent().getStringExtra("group")!=null)
            group.setText(getIntent().getStringExtra("group"));
        if(getIntent().getStringExtra("title")!=null)
            title.setText(getIntent().getStringExtra("title"));
        if(getIntent().getStringExtra("note")!=null)
            mobile.setText(getIntent().getStringExtra("note"));
        if(getIntent().getStringExtra("email")!=null)
            email.setText(getIntent().getStringExtra("email"));
        if(getIntent().getStringExtra("adr")!=null)
            address.setText(getIntent().getStringExtra("adr"));
        if(getIntent().getStringExtra("tel")!=null)
            phone.setText(getIntent().getStringExtra("tel"));
    }




    @Override
    public void onClick(View v) {
        Intent intent =new Intent(mContext, ChatActivity.class);
        intent.putExtra("from",getIntent().getStringExtra("name"));
        startActivity(intent);
    }
}
