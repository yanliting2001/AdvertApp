package com.grandartisans.advert.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import com.grandartisans.advert.R;
import com.grandartisans.advert.adapter.AreaListAdapter;

public class TimeSettingActivity extends Activity {

    private Context mContext;
    private LinearLayout date_ll;
    private LinearLayout Year_rl,Month_rl, Day_rl, Hour_rl, Minute_rl, Second_rl;
    private ListView YearList;
    private ListView MonthList;
    private ListView DayList;
    private ListView HourList;
    private ListView MinuteList;
    private ListView SecondList;

    private Button Btn_setting;

    private ArrayList<String> YeardataList = new ArrayList<String>();
    private ArrayList<String> MonthdataList = new ArrayList<String>();
    private ArrayList<String> DaydataList = new ArrayList<String>();
    private ArrayList<String> HourdataList = new ArrayList<String>();
    private ArrayList<String> MinutedataList = new ArrayList<String>();
    private ArrayList<String> SeconddataList = new ArrayList<String>();

    private AreaListAdapter mYearAdapter = null;
    private AreaListAdapter mMonthAdapter = null;
    private AreaListAdapter mDayAdapter = null;
    private AreaListAdapter mHourAdapter = null;
    private AreaListAdapter mMinuteAdapter = null;
    private AreaListAdapter mSecondAdapter = null;

    private TextView Year_text, Month_text, Day_text, Hour_text, Minute_text, Second_text;
    private TextView Date_text, Time_text;

    private static final int UPDATETIME = 1024;
    private final int DATA_SIZE = 15;

    private String lastYear = "";
    private String lastMonth = "";
    private String lastDay = "";
    private String lastHour = "";
    private String lastMinute = "";
    private String lastSecond = "";
    private int YearAreaPos = 0;
    private int MonthAreaPos = Integer.MAX_VALUE / 2;
    private int DayAreaPos = Integer.MAX_VALUE / 2;
    private int HourAreaPos = Integer.MAX_VALUE / 2;
    private int MinuteAreaPos = Integer.MAX_VALUE / 2;
    private int SecondAreaPos = Integer.MAX_VALUE / 2;

    private SimpleDateFormat sdf;

    Handler h = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATETIME:
                    h.sendEmptyMessageDelayed(UPDATETIME, 5*1000);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setAppFont();
        setContentView(R.layout.activity_time_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mContext = this;
        initView();
        initDateData();
        initFocus();
        Year_rl.requestFocus();
        Btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(TimeSettingActivity.this, "设置时间为: " + currentTime, Toast.LENGTH_SHORT).show();
                setSysDate(Integer.valueOf(lastYear),Integer.valueOf(lastMonth)-1,Integer.valueOf(lastDay));
                setSysTime(Integer.valueOf(lastHour),Integer.valueOf(lastMinute),Integer.valueOf(lastSecond));
            }
        });
        
    }

    private void initView() {
        date_ll = (LinearLayout) findViewById(R.id.datelocal_ll);

        YearList = (ListView) findViewById(R.id.AreaList_Year);
        MonthList = (ListView) findViewById(R.id.AreaList_Month);
        DayList = (ListView) findViewById(R.id.AreaList_Day);
        HourList = (ListView) findViewById(R.id.AreaList_Hour);
        MinuteList = (ListView) findViewById(R.id.AreaList_Minute);
        SecondList = (ListView) findViewById(R.id.AreaList_Second);

        Btn_setting = (Button) findViewById(R.id.btn_setting);
        
        Year_rl = (LinearLayout) findViewById(R.id.Year_rl);
        Month_rl = (LinearLayout) findViewById(R.id.Month_rl);
        Day_rl = (LinearLayout) findViewById(R.id.Day_rl);
        Hour_rl = (LinearLayout) findViewById(R.id.Hour_rl);
        Minute_rl = (LinearLayout) findViewById(R.id.Minute_rl);
        Second_rl = (LinearLayout) findViewById(R.id.Second_rl);

        Date_text = (TextView) findViewById(R.id.date_text);
        Time_text = (TextView) findViewById(R.id.time_text);

        h.sendEmptyMessage(UPDATETIME);
    }

    private void initDateData() {
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        int year = getCurrentYear(calendar);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        lastYear = Integer.toString(year);
        lastMonth = getIntegerFormat(month + 1);
        lastDay = getIntegerFormat(day);
        lastHour = getIntegerFormat(hour);
        lastMinute = getIntegerFormat(minute);
        lastSecond = getIntegerFormat(second);
        setFormatTime(calendar);
        setYearValue(year);
        setMonthValue();
        setDayValue(year, month);
        setHourValue();
        setMinuteValue();
        setSecondValue();
    }

    private void initFocus() {
        Year_rl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    YearList.requestFocus();
                }
            }
        });

        YearList.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                }
            }
        });

        Month_rl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    MonthList.requestFocus();
                }
            }
        });

        MonthList.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                }
            }
        });

        Day_rl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DayList.requestFocus();
                }
            }
        });

        DayList.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                }
            }
        });

        Hour_rl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    HourList.requestFocus();
                }
            }
        });

        HourList.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                }
            }
        });

        Minute_rl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    MinuteList.requestFocus();
                }
            }
        });

        MinuteList.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                }
            }
        });

        Second_rl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    SecondList.requestFocus();
                }
            }
        });

        SecondList.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                }
            }
        });
    }

    private void setFormatTime(Calendar calendar) {
        String time = sdf.format(calendar.getTimeInMillis());
        Time_text.setText(time);
    }

    private void setYearValue(int year) {
        YeardataList.clear();
        for (int i = 0; i < DATA_SIZE; i++) {
            YeardataList.add(Integer.toString(year + i));
        }
        for(int i = DATA_SIZE;i >0 ; i--) {
        	YeardataList.add(Integer.toString(year - i));
        }
        mYearAdapter = new AreaListAdapter(this, YeardataList);
        initYearList(YearList, mYearAdapter, YeardataList);
    }

    private void setMonthValue() {
        MonthdataList.clear();
        for (int i = 0; i < 12; i++) {
            if (i < 9)
                MonthdataList.add(0 + Integer.toString(i + 1));
            else
                MonthdataList.add(Integer.toString(i + 1));
        }
        mMonthAdapter = new AreaListAdapter(this, MonthdataList);
        initMonthList(MonthList, mMonthAdapter, MonthdataList);
    }

    private void setDayValue(int year, int month) {
        DaydataList.clear();
        int day_num = getMaxDayofMonth(year, month);
        for (int i = 0; i < day_num; i++) {
            if (i < 9)
                DaydataList.add(0 + Integer.toString(i + 1));
            else
                DaydataList.add(Integer.toString(i + 1));
        }
        mDayAdapter = new AreaListAdapter(this, DaydataList);
        initDayList(DayList, mDayAdapter, DaydataList);
    }

    private void setHourValue() {
        HourdataList.clear();
        for (int i = 0; i < 24; i++) {
            if (i < 10)
                HourdataList.add(0 + Integer.toString(i));
            else
                HourdataList.add(Integer.toString(i));
        }
        mHourAdapter = new AreaListAdapter(this, HourdataList);
        initHourList(HourList, mHourAdapter, HourdataList);
    }

    private void setMinuteValue() {
        MinutedataList.clear();
        for (int i = 0; i < 60; i++) {
            if (i < 10)
                MinutedataList.add(0 + Integer.toString(i));
            else
                MinutedataList.add(Integer.toString(i));
        }
        mMinuteAdapter = new AreaListAdapter(this, MinutedataList);
        initMinuteList(MinuteList, mMinuteAdapter, MinutedataList);
    }

    private void setSecondValue() {
        SeconddataList.clear();
        for (int i = 0; i < 60; i++) {
            if (i < 10)
                SeconddataList.add(0 + Integer.toString(i));
            else
                SeconddataList.add(Integer.toString(i));
        }
        mSecondAdapter = new AreaListAdapter(this, SeconddataList);
        initSecondList(SecondList, mSecondAdapter, SeconddataList);
    }

    private void initYearList(final ListView AreaList, final AreaListAdapter mAdapter, ArrayList<String> dataList) {
        YearAreaPos = Integer.MAX_VALUE / 2 + getkey(dataList.size(), getCurKeyPos(dataList, lastYear));
        mAdapter.setPosition(YearAreaPos);
        AreaList.setAdapter(mAdapter);
        AreaList.setSelectionFromTop(YearAreaPos, 75);
        AreaList.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_UP) {
                    AreaList.smoothScrollToPositionFromTop(YearAreaPos, 75, 100);
                    mAdapter.setPosition(YearAreaPos);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_UP) {
                    AreaList.smoothScrollToPositionFromTop(YearAreaPos, 75, 100);
                    mAdapter.setPosition(YearAreaPos);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {

                }
                return false;
            }
        });

        AreaList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                YearAreaPos = position;
                lastYear = parent.getSelectedItem().toString();
                updateTimeValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void initMonthList(final ListView AreaList, final AreaListAdapter mAdapter, ArrayList<String> dataList) {
        MonthAreaPos = Integer.MAX_VALUE / 2 + getkey(dataList.size(), getCurKeyPos(dataList, lastMonth));
        mAdapter.setPosition(MonthAreaPos);
        AreaList.setAdapter(mAdapter);
        AreaList.setSelectionFromTop(MonthAreaPos, 75);
        AreaList.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_UP) {
                    AreaList.smoothScrollToPositionFromTop(MonthAreaPos, 75, 100);
                    mAdapter.setPosition(MonthAreaPos);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_UP) {
                    AreaList.smoothScrollToPositionFromTop(MonthAreaPos, 75, 100);
                    mAdapter.setPosition(MonthAreaPos);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {

                }
                return false;
            }
        });

        AreaList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MonthAreaPos = position;
                lastMonth = parent.getSelectedItem().toString();
                updateTimeValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void initDayList(final ListView AreaList, final AreaListAdapter mAdapter, final ArrayList<String> dataList) {
        DayAreaPos = Integer.MAX_VALUE / 2 + getkey(dataList.size(), getCurKeyPos(dataList, lastDay));
        mAdapter.setPosition(DayAreaPos);
        AreaList.setAdapter(mAdapter);
        AreaList.setSelectionFromTop(DayAreaPos, 75);
        AreaList.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_UP) {
                    AreaList.smoothScrollToPositionFromTop(DayAreaPos, 75, 100);
                    mAdapter.setPosition(DayAreaPos);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_UP) {
                    AreaList.smoothScrollToPositionFromTop(DayAreaPos, 75, 100);
                    mAdapter.setPosition(DayAreaPos);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {

                }
                return false;
            }
        });

        AreaList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DayAreaPos = position;
                lastDay = parent.getSelectedItem().toString();
                updateTimeValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void initHourList(final ListView AreaList, final AreaListAdapter mAdapter, final ArrayList<String> dataList) {
        HourAreaPos = Integer.MAX_VALUE / 2 + getkey(dataList.size(), getCurKeyPos(dataList, lastHour));
        mAdapter.setPosition(HourAreaPos);
        AreaList.setAdapter(mAdapter);
        AreaList.setSelectionFromTop(HourAreaPos, 75);
        AreaList.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_UP) {
                    AreaList.smoothScrollToPositionFromTop(HourAreaPos, 75, 100);
                    mAdapter.setPosition(HourAreaPos);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_UP) {
                    AreaList.smoothScrollToPositionFromTop(HourAreaPos, 75, 100);
                    mAdapter.setPosition(HourAreaPos);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {

                }
                return false;
            }
        });

        AreaList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                HourAreaPos = position;
                lastHour = parent.getSelectedItem().toString();
                updateTimeValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void initMinuteList(final ListView AreaList, final AreaListAdapter mAdapter, final ArrayList<String> dataList) {
        MinuteAreaPos = Integer.MAX_VALUE / 2 + getkey(dataList.size(), getCurKeyPos(dataList, lastMinute));
        mAdapter.setPosition(MinuteAreaPos);
        AreaList.setAdapter(mAdapter);
        AreaList.setSelectionFromTop(MinuteAreaPos, 75);
        AreaList.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_UP) {
                    AreaList.smoothScrollToPositionFromTop(MinuteAreaPos, 75, 100);
                    mAdapter.setPosition(MinuteAreaPos);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_UP) {
                    AreaList.smoothScrollToPositionFromTop(MinuteAreaPos, 75, 100);
                    mAdapter.setPosition(MinuteAreaPos);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {

                }
                return false;
            }
        });

        AreaList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MinuteAreaPos = position;
                lastMinute = parent.getSelectedItem().toString();
                updateTimeValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void initSecondList(final ListView AreaList, final AreaListAdapter mAdapter, final ArrayList<String> dataList) {
        SecondAreaPos = Integer.MAX_VALUE / 2 + getkey(dataList.size(), getCurKeyPos(dataList, lastMinute));
        mAdapter.setPosition(SecondAreaPos);
        AreaList.setAdapter(mAdapter);
        AreaList.setSelectionFromTop(SecondAreaPos, 75);
        AreaList.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_UP) {
                    AreaList.smoothScrollToPositionFromTop(SecondAreaPos, 75, 100);
                    mAdapter.setPosition(SecondAreaPos);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_UP) {
                    AreaList.smoothScrollToPositionFromTop(SecondAreaPos, 75, 100);
                    mAdapter.setPosition(SecondAreaPos);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {

                }
                return false;
            }
        });

        AreaList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SecondAreaPos = position;
                lastSecond = parent.getSelectedItem().toString();
                updateTimeValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void updateDayValue() {
        DaydataList.clear();
        int year = Integer.parseInt(lastYear);
        int month = Integer.parseInt(lastMonth);
        int data_num = getMaxDayofMonth(year, month);
        for (int i = 0; i < data_num; i++) {
            if (i < 9)
                DaydataList.add(0 + Integer.toString(i + 1));
            else
                DaydataList.add(Integer.toString(i + 1));
        }
        mDayAdapter.notifyDataSetChanged();
    }

    private void updateTimeValue() {
        String time = lastYear + "-" + lastMonth + "-" + lastDay + " " +
                lastHour + ":" + lastMinute + ":" + lastSecond;
        Time_text.setText(time);
    }
    private void setAppFont() {
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/SourceHanSansCN-Light.otf");
        try {
            Field field = Typeface.class.getDeclaredField("MONOSPACE");
            field.setAccessible(true);
            field.set(null, typeface);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    private int getkey(int count, int num) {
        int n = 0;
        if (count > 0) {
            n = (Integer.MAX_VALUE / 2 / count + 1) * count - Integer.MAX_VALUE / 2 + num;
        }
        return n;
    }

    private int getCurKeyPos(ArrayList<String> str, String item) {
        for (int i = 0; i < str.size(); i++) {
            if (str.get(i).equals(item)) {
                return i;
            }
        }
        return 0;
    }
    /**
     * Get current year
     * if year is behind 2018 the value will be 2018.
     * @param calendar
     * @return
     */
    private int getCurrentYear(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        if (year < 2018)
            year = 2018;
        return year;
    }

    /**
     * Get days in month
     *
     * @param year
     * @param month
     * @return
     */
    private int getMaxDayofMonth(int year, int month) {
        int result = 0;
        if (month == 1 || month == 3 || month == 5 || month == 7 ||
                month == 8 || month == 10 || month == 12) {
            result = 31;
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            result = 30;
        } else {
            if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                result = 29;
            } else {
                result = 28;
            }
        }
        return result;
    }

    private String getIntegerFormat(int integer) {
        String str;
        if (integer < 10)
            str = "0" + Integer.toString(integer);
        else
            str = Integer.toString(integer);
        return str;
    }
    
    public void setSysDate(int year,int month,int day){
    	  Calendar c = Calendar.getInstance();
    	  c.set(Calendar.YEAR, year);
    	  c.set(Calendar.MONTH, month);
    	  c.set(Calendar.DAY_OF_MONTH, day);
    	  long when = c.getTimeInMillis();
    	  if(when / 1000 < Integer.MAX_VALUE){
    	      ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE)).setTime(when);
    	  }
    }
    
    public void setSysTime(int hour,int minute,int second){
    	  Calendar c = Calendar.getInstance();
    	  c.set(Calendar.HOUR_OF_DAY, hour);
    	  c.set(Calendar.MINUTE, minute);
    	  c.set(Calendar.SECOND, second);
    	  c.set(Calendar.MILLISECOND, 0);
    	  long when = c.getTimeInMillis();
    	  if(when / 1000 < Integer.MAX_VALUE){
    	      ((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE)).setTime(when);
    	  }
    }
}
