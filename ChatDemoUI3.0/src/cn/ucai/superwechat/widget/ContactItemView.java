package cn.ucai.superwechat.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.easeui.utils.EaseUserUtils;

import cn.ucai.superwechat.R;

public class ContactItemView extends LinearLayout{

    private TextView unreadMsgView;
    private  ImageView avatar;
    String name;
    public ContactItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ContactItemView(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs){
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ContactItemView);
        name = ta.getString(R.styleable.ContactItemView_contactItemName);
        Drawable image = ta.getDrawable(R.styleable.ContactItemView_contactItemImage);
        ta.recycle();

        LayoutInflater.from(context).inflate(R.layout.em_widget_contact_item, this);
        avatar = (ImageView) findViewById(R.id.avatar);
        unreadMsgView = (TextView) findViewById(R.id.unread_msg_number);
        TextView nameView = (TextView) findViewById(R.id.name);
        if(image != null){
            avatar.setImageDrawable(image);
        }
        nameView.setText(name);
    }

    public void setUnreadCount(int unreadCount){
        unreadMsgView.setText(String.valueOf(unreadCount));
    }
    public void setAvatar(){
        EaseUserUtils.setAppUserAvatar(this.getContext(),name,avatar);
    }

    public void showUnreadMsgView(){
        unreadMsgView.setVisibility(View.VISIBLE);
    }
    public void hideUnreadMsgView(){
        unreadMsgView.setVisibility(View.INVISIBLE);
    }

}