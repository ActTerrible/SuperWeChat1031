package com.hyphenate.easeui.utils;

import android.content.Context;
import android.nfc.Tag;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.controller.EaseUI.EaseUserProfileProvider;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.domain.Group;
import com.hyphenate.easeui.domain.User;

public class EaseUserUtils {

    static EaseUserProfileProvider userProvider;

    static {
        userProvider = EaseUI.getInstance().getUserProfileProvider();
    }

    static final String TAG = EaseUserUtils.class.getName();

    /**
     * get EaseUser according username
     *
     * @param username
     * @return
     */
    public static EaseUser getUserInfo(String username) {
        if (userProvider != null)
            return userProvider.getUser(username);

        return null;
    }

    public static User getUserAppInfo(String username) {
        if (userProvider != null)
            return userProvider.getAppUser(username);

        return null;
    }

    /**
     * set user avatar
     *
     * @param username
     */
    public static void setUserAvatar(Context context, String username, ImageView imageView) {
        User user = getUserAppInfo(username);
        if (user != null && user.getMAvatarPath() != null) {
            try {
                int avatarResId = Integer.parseInt(user.getAvatar());
                Glide.with(context).load(avatarResId).into(imageView);
            } catch (Exception e) {
                //use default avatar
                Glide.with(context).load(user.getMAvatarPath()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ease_default_avatar).into(imageView);
            }
        } else {
            Glide.with(context).load(R.drawable.ease_default_avatar).into(imageView);
        }
    }

    /**
     * set user's nickname
     */
    public static void setUserNick(String username, TextView textView) {
        if (textView != null) {
//        	EaseUser user = getUserInfo(username);
            User user = getUserAppInfo(username);
            Log.e(TAG, user.toString());
            if (user != null && user.getMUserNick() != null) {
                Log.e(TAG, "进入获取昵称分支");
                textView.setText(user.getMUserNick());

            } else {
                Log.e(TAG, "进入把昵称设置成用户名分支");
                textView.setText(username);
            }
        }
    }


    public static void setCurrentUserName(TextView wechatNumber) {
        if (wechatNumber != null) {
//        	EaseUser user = getUserInfo(username);
            String username = EMClient.getInstance().getCurrentUser();
            wechatNumber.setText(username);

        }
    }

    public static void setAppUserNick(String username, TextView textView) {
        if (textView != null) {
            User user = getUserAppInfo(username);
            Log.e(TAG, "user=" + user);
            if (user != null && user.getMUserNick() != null) {
                textView.setText(user.getMUserNick());
            } else {
                textView.setText(username);
            }
        }
    }


    public static void setAppUserAvatar(Context context, String username, ImageView imageView) {
        User user = getAppUserInfo(username);
        if (user == null) {
            user = new User(username);
        }
        if (user != null && user.getAvatar() != null) {
            try {
                Log.e(TAG, user.getAvatar());
                int avatarResId = Integer.parseInt(user.getAvatar());
                Glide.with(context).load(avatarResId).into(imageView);
            } catch (Exception e) {
                //use default avatar
                Glide.with(context).load(user.getAvatar()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.default_hd_avatar).into(imageView);
            }
        } else {
            Glide.with(context).load(R.drawable.default_hd_avatar).into(imageView);
        }
    }

    private static User getAppUserInfo(String username) {
        if (userProvider != null)
            return userProvider.getAppUser(username);

        return null;
    }

    public static void setCurrentAppUserName(TextView textView) {
        String username = EMClient.getInstance().getCurrentUser();
        setAppUserName("", username, textView);
    }

    public static void setAppUserName(String suffix, String username, TextView textView) {
        textView.setText(suffix + username);
    }

    public static void setCurrentAppUserNick(TextView textView) {
        String username = EMClient.getInstance().getCurrentUser();
        setAppUserNick(username, textView);
    }

    public static void setCurrentAppUserAvatar(FragmentActivity activity, ImageView imageView) {
        String username = EMClient.getInstance().getCurrentUser();
        setAppUserAvatar(activity, username, imageView);
    }

    public static User getCurrentAppUserInfo() {
        String username = EMClient.getInstance().getCurrentUser();
        if (userProvider != null)
            return userProvider.getAppUser(username);

        return null;
    }

    public static void setAppUserNameWithInfo(String name, TextView wechatNumber) {
        setAppUserName("wechat:", name, wechatNumber);
    }


    public static void setUserInitialLetter(User user) {
        EaseCommonUtils.setUserAppInitialLetter(user);
    }


    public static void setAppGroupAvatar(Context context,String hxid, ImageView imageView) {
        try {
        int avatarResId = Integer.parseInt(Group.getAvatar(hxid));
        Glide.with(context).load(avatarResId).into(imageView);
    } catch (Exception e) {
        //use default avatar
        Glide.with(context).load(Group.getAvatar(hxid)).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.default_hd_avatar).into(imageView);
    }

    }
}
