package com.example.schoolnews;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isInit=true;
    private ProgressDialog progressDialog=new ProgressDialog();
    private List<News> newsList=new ArrayList<>();
    private boolean pullRefresh=false;
    private User user=new User();

    private TextView name;
    private TextView account;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent=getIntent();
        user=(User) intent.getSerializableExtra("user");


        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullRefresh=true;
                RefreshNewsTask task=new RefreshNewsTask();
                task.execute();
            }
        });




        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView=navigationView.getHeaderView(0);
        name=headerView.findViewById(R.id.textView_stuName);
        account=headerView.findViewById(R.id.textView_stuNumber);
        name.setText(user.getName());
        account.setText(user.getAccount());


        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        }

        navigationView.setCheckedItem(R.id.nav_home);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.nav_exit:
                        onBackPressed();
                        break;
                    case R.id.nav_home:
                        break;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });


    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(isInit){
            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                },1);
            }else{
                //权限检查正常，刷新列表
                RefreshNewsTask task=new RefreshNewsTask();
                task.execute();
            }
            isInit=false;
        }
    }


    class RefreshNewsTask extends AsyncTask<Void,Void,Boolean>{
        @Override
        protected void onPreExecute() {
            if(pullRefresh==false){
                progressDialog.show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            OkHttpClient client=new OkHttpClient();
            Request request=new Request.Builder()
                    .url("http://news.cqu.edu.cn/newsv2/news-126.html")
                    .build();
            try{
                Response response=client.newCall(request).execute();
                int times=0;
                while(response.code()!=200 && times<10){
                    try{Thread.sleep(500);}catch(Exception e){e.printStackTrace();}
                    response=client.newCall(request).execute();
                    times=times+1;
                }

                if(response.code()==200){
                    Document document= Jsoup.parse(response.body().string());
                    Elements elements=document.select("div[class='item']");
                    for(int i=0;i<elements.size();i++){
                        News news=new News();
                        news.setImageUrl(elements.get(i).select("img").attr("src"));
                        news.setTitle(elements.get(i).select("a[href]").text());
                        news.setDetailUrl(elements.get(i).select("a[href]").attr("href"));
                        newsList.add(news);
                        Request requestDetail=new Request.Builder()
                                .url(news.getDetailUrl())
                                .build();
                        Response responseDetail=client.newCall(requestDetail).execute();
                        int timesDetail=0;
                        while(response.code()!=200 && timesDetail<10){
                            try{Thread.sleep(500);}catch(Exception e){e.printStackTrace();}
                            responseDetail=client.newCall(requestDetail).execute();
                            times=times+1;
                        }
                        if(responseDetail.code()==200){
                            Document documentDetail=Jsoup.parse(responseDetail.body().string());
                            //Elements elements1=documentDetail.select("a[href='javascript:;']");
                            news.setAuthor(documentDetail.select("a[href='javascript:;']").get(4).text());
                            news.setTime(documentDetail.select("div[class='ibox']").select("span").get(1).text());
                            String content=new String();
                            for(int j=0;j<documentDetail.select("div[class=acontent]").select("p").size();j++){
                                content=content+documentDetail.select("div[class=acontent]").select("p").get(j).text()+"\n\n";
                            }
                            news.setBody(content);
                        }
                    }
                    return true;
                }


            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean==true){
                //更新列表
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Refresh succeed!", Toast.LENGTH_SHORT).show();
                NewsAdapter adapter=new NewsAdapter(newsList);
                RecyclerView recyclerView = findViewById(R.id.recycler_view);
                GridLayoutManager layoutManager = new GridLayoutManager(MainActivity.this, 1);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
            }else{
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Refresh failed！Please check your Internet！", Toast.LENGTH_SHORT).show();
            }
            if(pullRefresh==true){
                swipeRefreshLayout.setRefreshing(false);
            }
            pullRefresh=false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:
                RefreshNewsTask task=new RefreshNewsTask();
                task.execute();
                break;
                default:
                    break;
        }
    }

    //显示进度的弹窗
    class ProgressDialog {
        private PopupWindow popupWindow;
        private View popupView;
        private TranslateAnimation animation;

        private void show() {
            this.popupView = View.inflate(MainActivity.this, R.layout.layout_waiting, null);

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
            popupWindow.showAtLocation(MainActivity.this.findViewById(R.id.recycler_view), Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
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

