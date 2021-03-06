/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.superwechat.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupManager.EMGroupOptions;
import com.hyphenate.chat.EMGroupManager.EMGroupStyle;
//import com.hyphenate.easeui.domain.Group;

import com.hyphenate.easeui.utils.EaseImageUtils;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.exceptions.HyphenateException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import com.hyphenate.easeui.domain.Group;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.dao.NetDao;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.utils.ResultUtils;

public class NewGroupActivity extends BaseActivity {
    @BindView(R.id.group_avatar)
    ImageView groupAvatar;
    private EditText groupNameEditText;
    private ProgressDialog progressDialog;
    private EditText introductionEditText;
    private CheckBox publibCheckBox;
    private CheckBox memberCheckbox;
    private TextView secondTextView;
    Activity context;
    private static final int REQUESTCODE_PICK = 1;
    private static final int REQUESTCODE_CUTTING = 2;
    private static final int REQUESTCODE_PICK_CONTACT = 3;
    String TAG="NewGroupActivity";
    File file=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.em_activity_new_group);
        ButterKnife.bind(this);
        groupNameEditText = (EditText) findViewById(R.id.edit_group_name);
        introductionEditText = (EditText) findViewById(R.id.edit_group_introduction);
        publibCheckBox = (CheckBox) findViewById(R.id.cb_public);
        memberCheckbox = (CheckBox) findViewById(R.id.cb_member_inviter);
        secondTextView = (TextView) findViewById(R.id.second_desc);

        publibCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    secondTextView.setText(R.string.join_need_owner_approval);
                } else {
                    secondTextView.setText(R.string.Open_group_members_invited);
                }
            }
        });
    }

    /**
     * @param v
     */
    public void save(View v) {
        String name = groupNameEditText.getText().toString();
        if (TextUtils.isEmpty(name)) {
            new EaseAlertDialog(this, R.string.Group_name_cannot_be_empty).show();
        } else {
            // select from contact list
            startActivityForResult(new Intent(this, GroupPickContactsActivity.class).putExtra("groupName", name), REQUESTCODE_PICK_CONTACT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUESTCODE_PICK:
                if (data == null || data.getData() == null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            case REQUESTCODE_CUTTING:
                if (data != null) {
                   setPicToView(data);
                }
                break;
            case REQUESTCODE_PICK_CONTACT:
                createEMGroup(data);
                break;
            default:
                break;
        }
    }


    private void createEMGroup(final Intent data) {
        String st1 = getResources().getString(R.string.Is_to_create_a_group_chat);
        final String st2 = getResources().getString(R.string.Failed_to_create_groups);
        //new group
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(st1);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String groupName = groupNameEditText.getText().toString().trim();
                String desc = introductionEditText.getText().toString();
                String[] members = data.getStringArrayExtra("newmembers");
                try {
                    EMGroupOptions option = new EMGroupOptions();
                    option.maxUsers = 200;

                    String reason = NewGroupActivity.this.getString(R.string.invite_join_group);
                    reason = EMClient.getInstance().getCurrentUser() + reason + groupName;

                    if (publibCheckBox.isChecked()) {
                        option.style = memberCheckbox.isChecked() ? EMGroupStyle.EMGroupStylePublicJoinNeedApproval : EMGroupStyle.EMGroupStylePublicOpenJoin;
                    } else {
                        option.style = memberCheckbox.isChecked() ? EMGroupStyle.EMGroupStylePrivateMemberCanInvite : EMGroupStyle.EMGroupStylePrivateOnlyOwnerInvite;
                    }
                    EMGroup EMGroup = EMClient.getInstance().groupManager().createGroup(groupName, desc, members, reason, option);
                    createGroup(EMGroup);


                } catch (final HyphenateException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(NewGroupActivity.this, st2 + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        }).start();

    }

    private void createGroup(final EMGroup emGroup) {
        if (file==null) {
            NetDao.createGroup(context, emGroup, new OkHttpUtils.OnCompleteListener<String>() {
                @Override
                public void onSuccess(String s) {
                afterCreateGroup(s);
                    addGroupMembers(emGroup);
                }

                @Override
                public void onError(String error) {
                    progressDialog.dismiss();
                    CommonUtils.showShortToast("创建群组失败");
                }
            });
        }else {
            NetDao.createGroup(context, emGroup, file, new OkHttpUtils.OnCompleteListener<String>() {
                @Override
                public void onSuccess(String s) {
                    afterCreateGroup(s);
                    addGroupMembers(emGroup);
                }

                @Override
                public void onError(String error) {
                    progressDialog.dismiss();
                    CommonUtils.showShortToast("创建群组失败");
                }
            });
        }
    }

    private void addGroupMembers(final EMGroup emGroup) {
        NetDao.addGroupMembers(context, emGroup, new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (s!=null){
                    Result result=ResultUtils.getResultFromJson(s,Group.class);
                    if (result!=null&result.isRetMsg()){
                        Group group= (Group) result.getRetData();
                        Log.e(TAG,"group="+group);
                        progressDialog.dismiss();
                        MFGT.finish(context);
                    }
                }
            }

            @Override
            public void onError(String error) {
                    progressDialog.dismiss();
                    CommonUtils.showShortToast("创建群组失败");
            }
        });
    }

    private void afterCreateGroup(String s) {
        if (s!=null){
           Result result= ResultUtils.getResultFromJson(s,Group.class);
            Log.e(TAG,"result:"+result);
            if (result!=null&&result.isRetMsg()){
               Group group= (Group) result.getRetData();
               createSuccess();
            }
        }
    }


    public void createSuccess() {
        runOnUiThread(new Runnable() {
            public void run() {

                setResult(RESULT_OK);
                finish();
            }
        });
    }


    @OnClick(R.id.group_avatar)
    public void onClick() {
        uploadHeadPhoto();
    }

    private void uploadHeadPhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dl_title_upload_photo);
        builder.setItems(new String[]{getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload)},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                Toast.makeText(NewGroupActivity.this, getString(R.string.toast_no_support),
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(pickIntent, REQUESTCODE_PICK);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }

    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(getResources(), photo);
            groupAvatar.setImageDrawable(drawable);
            // uploadUserAvatar(Bitmap2Bytes(photo));
            file=saveBitmapFile(picdata);
            Log.e(TAG,"file="+file);
        }
    }

    public File saveBitmapFile(Intent data) {
        Bundle extra = data.getExtras();
        if (extra != null) {
            Bitmap bitmap = extra.getParcelable("data");
            String path = EaseImageUtils.getImagePath(SystemClock.currentThreadTimeMillis() + I.AVATAR_SUFFIX_JPG);
            L.e(TAG, "图片路径: " + path.toString());
            File file = new File(path);
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                try {
                    bos.flush();
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return file;
        }
        return null;
    }

}
