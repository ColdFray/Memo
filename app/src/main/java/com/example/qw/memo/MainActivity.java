package com.example.qw.memo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener{

    EditText search_txt;
    ImageView search_btn;
    private List<OneMemo> memolist=new ArrayList<>();
    MemoAdapter adapter;
    ListView lv;
    int BIG_NUM_FOR_ALARM=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //自定义toolbar
        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        search_txt = findViewById(R.id.search_txt);
        search_btn = findViewById(R.id.search_btn);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tx = search_txt.getText().toString();
                Intent intent = new Intent(MainActivity.this,SearchActivity.class);
                intent.putExtra("search_text",tx);
                startActivity(intent);
            }
        });
        Connector.getDatabase();
        loadHistoryData();
        adapter=new MemoAdapter(MainActivity.this, R.layout.memo_list, memolist);
        lv=(ListView) findViewById(R.id.list);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(this);
        lv.setOnItemLongClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    //自定义toolbar此时只有一个按钮：添加便签按钮！！
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                onAdd();
                break;
            default:
        }
        return true;
    }

    private void loadHistoryData() {
        List<Memo> memoes= DataSupport.findAll(Memo.class);
        if(memoes.size()==0) {
            initializeLitePal();
            memoes = DataSupport.findAll(Memo.class);
        }

        for(Memo record:memoes) {
            Log.d("MainActivity", "current num: " + record.getNum());
            Log.d("MainActivity", "id: " + record.getId());
            Log.d("MainActivity", "getAlarm: " + record.getAlarm());
            int tag = record.getTag();
            String textDate = record.getTextDate();
            String textTime = record.getTextTime();
            boolean alarm = record.getAlarm().length() > 1 ? true : false;
            String textTitle = record.getTextTitle();
            String mainText = record.getMainText();
            OneMemo temp = new OneMemo(tag, textDate, textTime, alarm,textTitle, mainText);
            memolist.add(temp);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent it=new Intent(this,Edit.class);
        Memo record=getMemoWithNum(position);
        transportInformationToEdit(it, record);
        startActivityForResult(it,position);
    }
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        int n=memolist.size();
        if(memolist.get(position).getAlarm()) {
            cancelAlarm(position);
        }
        memolist.remove(position);
        adapter.notifyDataSetChanged();
        String whereArgs = String.valueOf(position); //why not position ?
        DataSupport.deleteAll(Memo.class, "num = ?", whereArgs);
        for(int i=position+1; i<n; i++) {
            ContentValues temp = new ContentValues();
            temp.put("num", i-1);
            String where = String.valueOf(i);
            DataSupport.updateAll(Memo.class, temp, "num = ?", where);
        }
        return true;
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent it) {
        if(resultCode==RESULT_OK) {
            updateLitePalAndList(requestCode, it);
        }
    }

    //更新数据库并更新显示列表
    //增加便签，requestCode大于当前list长度
    //修改便签，requestCode小于当前list长度
    private void updateLitePalAndList(int requestCode, Intent it) {

        int num=requestCode;
        int tag=it.getIntExtra("tag",0);

        Calendar c=Calendar.getInstance();
        String current_date=getCurrentDate(c);
        String current_time=getCurrentTime(c);

        String alarm=it.getStringExtra("alarm");
        String textTitle=it.getStringExtra("textTitle");
        String mainText=it.getStringExtra("mainText");

        boolean gotAlarm = alarm.length() > 1 ? true : false;
        OneMemo new_memo = new OneMemo(tag, current_date, current_time, gotAlarm,textTitle, mainText);

        if((requestCode+1)>memolist.size()) {

            addRecordToLitePal(num, tag, current_date, current_time, alarm,textTitle, mainText);

            memolist.add(new_memo);
        }
        else {

            if(memolist.get(num).getAlarm()) {
                cancelAlarm(num);
            }

            //更新之前的“num”便签
            ContentValues temp = new ContentValues();
            temp.put("tag", tag);
            temp.put("textDate", current_date);
            temp.put("textTime", current_time);
            temp.put("alarm", alarm);
            temp.put("textTitle",textTitle);
            temp.put("mainText", mainText);
            String where = String.valueOf(num);
            DataSupport.updateAll(Memo.class, temp, "num = ?", where);

            memolist.set(num, new_memo);
        }
        //如果已经设置闹钟
        if(gotAlarm) {
            loadAlarm(alarm, requestCode, 0);
        }

        adapter.notifyDataSetChanged();
    }
    //初始化，没有便签时初始
    private void initializeLitePal() {
        Calendar c=Calendar.getInstance();
        String textDate=getCurrentDate(c);
        String textTime=getCurrentTime(c);
        addRecordToLitePal(0,0,textDate,textTime,"","你好！","");
        addRecordToLitePal(1,1,textDate,textTime,"","点击可编辑","");
        addRecordToLitePal(2,2,textDate,textTime,"","长按可删除","");
    }
    //格式化日期 2019年/1/16
    private String getCurrentDate(Calendar c){
        return c.get(Calendar.YEAR)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.DAY_OF_MONTH);
    }
    //格式化时间 22:02
    private String getCurrentTime(Calendar c){
        String current_time="";
        if(c.get(Calendar.HOUR_OF_DAY)<10) current_time=current_time+"0"+c.get(Calendar.HOUR_OF_DAY);
        else current_time=current_time+c.get(Calendar.HOUR_OF_DAY);
        current_time=current_time+":";
        if(c.get(Calendar.MINUTE)<10) current_time=current_time+"0"+c.get(Calendar.MINUTE);
        else current_time=current_time+c.get(Calendar.MINUTE);
        return current_time;
    }
    //添加操作
    private void addRecordToLitePal(int num, int tag, String textDate, String textTime, String alarm,String textTitle, String mainText) {
        Memo record=new Memo();
        record.setNum(num);
        record.setTag(tag);
        record.setTextDate(textDate);
        record.setTextTime(textTime);
        record.setAlarm(alarm);
        record.setTextTitle(textTitle);
        record.setMainText(mainText);
        record.save();
    }
    //给Edit界面传参
    private void transportInformationToEdit(Intent it, Memo record) {
        it.putExtra("num",record.getNum());
        it.putExtra("tag",record.getTag());
        it.putExtra("textDate",record.getTextDate());
        it.putExtra("textTime",record.getTextTime());
        it.putExtra("alarm",record.getAlarm());
        it.putExtra("textTitle",record.getTextTitle());
        it.putExtra("mainText",record.getMainText());
    }

    //点击添加按钮
    public void onAdd() {
        Intent it=new Intent(this,Edit.class);

        int position = memolist.size();

        Calendar c=Calendar.getInstance();
        String current_date=getCurrentDate(c);
        String current_time=getCurrentTime(c);

        it.putExtra("num",position);
        it.putExtra("tag",0);
        it.putExtra("textDate",current_date);
        it.putExtra("textTime",current_time);
        it.putExtra("alarm","");
        it.putExtra("textTitle","");
        it.putExtra("mainText","");

        startActivityForResult(it,position);
    }


    //获取当前位置的Memo便签实例，传入参数为memolist中的位置position
    private Memo getMemoWithNum(int num) {
        String whereArgs = String.valueOf(num);
        Memo record= DataSupport.where("num = ?", whereArgs).findFirst(Memo.class);

        return record;
    }


    //通过“alarm”项设置闹钟
    private void loadAlarm(String alarm, int num, int days) {
        int alarm_hour=0;
        int alarm_minute=0;
        int alarm_year=0;
        int alarm_month=0;
        int alarm_day=0;

        //截取字符串，获得设置的闹钟 提醒 年 / 月 / 日  时:分
        int i=0, k=0;
        while(i<alarm.length()&&alarm.charAt(i)!='/') i++;
        alarm_year=Integer.parseInt(alarm.substring(k,i));
        k=i+1;i++;
        while(i<alarm.length()&&alarm.charAt(i)!='/') i++;
        alarm_month=Integer.parseInt(alarm.substring(k,i));
        k=i+1;i++;
        while(i<alarm.length()&&alarm.charAt(i)!=' ') i++;
        alarm_day=Integer.parseInt(alarm.substring(k,i));
        k=i+1;i++;
        while(i<alarm.length()&&alarm.charAt(i)!=':') i++;
        alarm_hour=Integer.parseInt(alarm.substring(k,i));
        k=i+1;i++;
        alarm_minute=Integer.parseInt(alarm.substring(k));

        Memo record=getMemoWithNum(num);

        Intent intent = new Intent(MainActivity.this, OneShotAlarm.class);
        intent.putExtra("alarmId",record.getId()+BIG_NUM_FOR_ALARM);

        //pendingIntent是一种特殊的Intent。主要的区别在于Intent的执行立刻的
        PendingIntent sender = PendingIntent.getBroadcast(
                MainActivity.this, record.getId()+BIG_NUM_FOR_ALARM, intent, 0);

        // 10秒后关闭提醒
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());


        Calendar alarm_time = Calendar.getInstance();
        alarm_time.set(alarm_year,alarm_month-1,alarm_day,alarm_hour,alarm_minute);

        int interval = 1000 * 60 * 60 * 24 *days;

        // 安排闹钟!
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

        am.set(AlarmManager.RTC_WAKEUP, alarm_time.getTimeInMillis(), sender);
    }

    //cancel the alarm
    private void cancelAlarm(int num) {
        Memo record=getMemoWithNum(num);

        Intent intent = new Intent(MainActivity.this,
                OneShotAlarm.class);
        //pendingIntent是一种特殊的Intent。主要的区别在于Intent的执行立刻的
        PendingIntent sender = PendingIntent.getBroadcast(
                MainActivity.this, record.getId()+BIG_NUM_FOR_ALARM, intent, 0);

        //调用系统的提醒服务
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }

}


