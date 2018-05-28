package ru.raznometov.ma_launcher_v2;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // Variables
    private static final String TAG = "MA_launcher";
    Timer timer;
    TimerTask timerTask;
    Handler handler;
    static DateFormat timeFormat = new SimpleDateFormat("HH:mm");
    static DateFormat timeFormat1 = new SimpleDateFormat("HH mm");
    static DateFormat dateFormatWeek = new SimpleDateFormat("EEE");
    static DateFormat dateFormatDay = new SimpleDateFormat("dd");
    static DateFormat dateFormatMonth = new SimpleDateFormat("M");
    static int global_battlevel;
    static boolean global_isCharging;
    static boolean global_isFull;
    private TextView mContentTime;
    private TextView mContentBatt;
    private TextView mContentDate;
    private TextView mContentInetstatus;
    ImageView imgSettings;
    ImageView imgMessage;
    ImageView imgBatt;
    ImageView imgCell;
    boolean timerotate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // скрыть панель
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Определяем элементы на View
        mContentTime = findViewById(R.id.textTime);
        mContentBatt = findViewById(R.id.textBatt);
        mContentDate = findViewById(R.id.textDate);
        mContentInetstatus = findViewById(R.id.textInetstatus);

        // Запускаем таймер
        handler = new Handler();
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 1000, 1000); //

        // Определяем графические элементы на экране
        imgSettings = (ImageView) findViewById(R.id.imageViewSettings);
        imgMessage = (ImageView) findViewById(R.id.imageViewMessage);
        imgBatt = (ImageView) findViewById(R.id.imageViewBatt);
        imgCell = (ImageView) findViewById(R.id.imageViewCell);

        imgMessage.setColorFilter(Color.YELLOW);
        imgBatt.setColorFilter(Color.WHITE);
        imgSettings.setColorFilter(Color.YELLOW);
        imgCell.setColorFilter(Color.WHITE);

        // Вешаем обработчики на кнопки на экране
        imgSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_LONG).show();
            }
        });
        imgMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Message", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(intent);
            }
        });
    }

    // Определяем уровень заряда батареи
    public void GetBattStatus() {
        IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryIntent = registerReceiver(null, batteryIntentFilter);
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int battlevel = (int) 100 * level / scale;
        // Are we charging / charged?
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        boolean isFull = status == BatteryManager.BATTERY_STATUS_FULL;
        // Фиксируем показания глобально
        global_battlevel = (int) Math.round(battlevel);
        global_isCharging = isCharging;
        global_isFull = isFull;
    }

    // статус подключения к интернету (пока не используем)
    boolean isNetworkOnline() {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
                    status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;
    }

    // Инициализация таймера
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        GetBattStatus();
                        // Вместо секунд - мигающие точки
                        if (timerotate) {
                            mContentTime.setText(timeFormat.format(new Date()));
                            timerotate = false;
                        } else {
                            mContentTime.setText(timeFormat1.format(new Date()));
                            timerotate = true;
                        }
                        //Просто дата: mContentDate.setText(dateFormat.format(new Date()));
                        String PrepareDateString = dateFormatDay.format(new Date()) + " " + GetMonth(Integer.valueOf(dateFormatMonth.format(new Date()))) + " " + GetDayOfTheWeek(dateFormatWeek.format(new Date()));
                        mContentDate.setText(PrepareDateString);
                        // Отслеживаем и отображаем "charging..." при зарядке
                        if (global_isCharging)
                        {
                            mContentBatt.getLayoutParams().width = 180; mContentBatt.requestLayout();
                            mContentBatt.setText("charging... " + global_battlevel + "%");
                            if (global_isFull) {
                                mContentBatt.setText("FULL " + global_battlevel + "%");
                            }
                        } else {
                            mContentBatt.getLayoutParams().width = 80; mContentBatt.requestLayout();
                            mContentBatt.setText(global_battlevel + "%");
                        }
                        // иконка статуса подключения к интернету
                        /*
                        if (isNetworkOnline())
                        {
                            mContentInetstatus.setText(" i");
                        }
                        */
                        Log.i(TAG, "timer ");
                    }
                });
            }
        };
    }


    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) // заменил onKeyDown
    {
        Log.i(TAG, String.valueOf(keyCode));
        if (keyCode == 4) // на часах от Кингвеар кейкод 4
        {
            // запуск виброоповещения
            long[] pattern = {300, 500, 100, 500};
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(pattern, -1);
            }
            //vibrator.cancel();

            // запуск звонка средствами Андроид
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:88002500960"));
            startActivity(intent);
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) // просто перехватываем
    {
        Log.i(TAG, String.valueOf(keyCode));
        if (keyCode == 4) // на часах от Кингвеар кейкод 4
        {
            //Toast.makeText(MainActivity.this, "onKeyDown", Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private String GetDayOfTheWeek(String day) {
        switch (day) {
            case "Mon":
                return "ПН";
            case "Tue":
                return "ВТ";
            case "Wed":
                return "СРД";
            case "Thu":
                return "ЧТВ";
            case "Fri":
                return "ПТ";
            case "Sat":
                return "СБ";
            case "Sun":
                return "ВСK";
        }
        return "Worng Day";
    }

    private String GetMonth(int month) {
        switch (month) {
            case 1:
                return "января";
            case 2:
                return "февраля";
            case 3:
                return "марта";
            case 4:
                return "апреля";
            case 5:
                return "мая";
            case 6:
                return "июня";
            case 7:
                return "июля";
            case 8:
                return "августа";
            case 9:
                return "сентября";
            case 10:
                return "октября";
            case 11:
                return "ноября";
            case 12:
                return "декабря";
        }
        return "Worng Day";

    }
}
