package com.example.schoolnews;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.litepal.LitePal;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    EditText editTextStuNumber;
    EditText editTextStuPassword;
    private User user=new User();
    private List<User> userList=new ArrayList<>();
    private Button buttonRegiste;
    private EditText editTextName;
    private Button buttonLogin;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        android.support.v7.widget.Toolbar toolbar=findViewById(R.id.toolbar_login);
        setSupportActionBar(toolbar);
        LitePal.getDatabase();

        linearLayout=findViewById(R.id.linearLayout_name);
        editTextStuNumber=findViewById(R.id.editText_username);
        editTextStuPassword=findViewById(R.id.editText_password);
        editTextName=findViewById(R.id.editText_name);
        buttonLogin=findViewById(R.id.button_login);
        buttonRegiste=findViewById(R.id.button_registe);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userList=LitePal.findAll(User.class);
                boolean exist=false;
                for(User user:userList){
                    if(editTextStuNumber.getText().toString().equals(user.getAccount())){
                        if(editTextStuPassword.getText().toString().equals(user.getPassword())){
                            Intent intent=new Intent(LoginActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("user",user);
                            startActivity(intent);
                        }
                        exist=true;
                    }
                }

                if(exist==false){
                    Toast.makeText(LoginActivity.this, "Not exist! Please registe!", Toast.LENGTH_SHORT).show();
                    linearLayout.setVisibility(View.VISIBLE);
                    buttonRegiste.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonRegiste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setAccount(editTextStuNumber.getText().toString());
                user.setName(editTextName.getText().toString());
                user.setPassword(editTextStuPassword.getText().toString());
                user.save();
                Toast.makeText(LoginActivity.this, "Registe succeed! Please login!", Toast.LENGTH_SHORT).show();
                linearLayout.setVisibility(View.GONE);
                buttonRegiste.setVisibility(View.GONE);
            }
        });


    }


    //显示进度的弹窗
    class ProgressDialog {
        private PopupWindow popupWindow;
        private View popupView;
        private TranslateAnimation animation;

        private void show() {
            this.popupView = View.inflate(LoginActivity.this, R.layout.layout_waiting, null);

            // 参数2,3：指明popupwindow的宽度和高度
            popupWindow = new PopupWindow(popupView, 500, 500);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.alpha = 0.4f;
            getWindow().setAttributes(lp);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    WindowManager.LayoutParams lp = getWindow().getAttributes();
                    lp.alpha = 1f;
                    getWindow().setAttributes(lp);
                }
            });

            // 设置点击popupwindow外屏幕其它地方消失
            popupWindow.setFocusable(false);
            popupWindow.setOutsideTouchable(false);
            // 平移动画相对于手机屏幕的底部开始，X轴不变，Y轴从1变0
            animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0,
                    Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, 0);
            animation.setInterpolator(new AccelerateInterpolator());
            animation.setDuration(200);
            // 设置popupWindow的显示位置，此处是在手机屏幕底部且水平居中的位置
            popupWindow.showAtLocation(LoginActivity.this.findViewById(R.id.recycler_view), Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
            popupView.startAnimation(animation);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        private void dismiss() {
            popupWindow.dismiss();
            popupView = null;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }




}
