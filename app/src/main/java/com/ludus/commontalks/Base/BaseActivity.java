package com.ludus.commontalks.Base;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Created by imhwan on 2017. 12. 23..
 */

public class BaseActivity extends AppCompatActivity {

    public static HashMap<Integer,String> messageDayHashMap;
    public static HashMap<Integer,String> messageWeekendHashMap;

    public void progressON() {
        String msg;
        if (messageDayHashMap == null ) {
            messageDayHashMap = new HashMap<>();
            messageDayHashMap.put(1,"출출한 지금, 어떠세요?");
            messageDayHashMap.put(2,"유달리 감성 돋는 지금, 어떠세요?");
            messageDayHashMap.put(3,"잠 못 이루는 지금, 어떠세요?");
            messageDayHashMap.put(4,"눈꺼풀이 무거운 지금, 어떠세요?");
            messageDayHashMap.put(5,"주위를 둘러보는 지금, 어떠세요?");
            messageDayHashMap.put(6,"가장 기대되는 지금, 어떠세요?");
            messageDayHashMap.put(7,"가장 변함없는 일상인 지금, 어떠세요?");
            messageDayHashMap.put(8,"커피만으로 버티긴 힘든 지금, 어떠세요?");
            messageDayHashMap.put(9,"행복과 가까워지는 지금, 어떠세요?");
            messageDayHashMap.put(10,"뭐 할지 고민하는 지금, 어떠세요?");
            messageDayHashMap.put(11,"뒹굴뒹굴 지금, 어떠세요?");
            messageDayHashMap.put(12,"하루 끝의 지금, 어떠세요?");
        }
        if (messageWeekendHashMap == null) {
            messageWeekendHashMap = new HashMap<>();
            messageWeekendHashMap.put(1,"출출한 지금, 어떠세요?");
            messageWeekendHashMap.put(2,"유달리 감성 돋는 지금, 어떠세요?");
            messageWeekendHashMap.put(3,"내일을 걱정하지 않는 지금, 어떠세요?");
            messageWeekendHashMap.put(4,"남보다 조금 일찍 일어난 지금, 어떠세요?");
            messageWeekendHashMap.put(5,"어떤 하루일지 설레는 지금, 어떠세요?");
            messageWeekendHashMap.put(6,"조금 늦게 일어난 지금, 어떠세요?");
            messageWeekendHashMap.put(7,"특별한 점심을 먹은 지금, 어떠세요?");
            messageWeekendHashMap.put(8,"벌써 주말의 반을 넘긴 지금, 어떠세요?");
            messageWeekendHashMap.put(9,"한숨 자기 좋은 지금, 어떠세요?");
            messageWeekendHashMap.put(10,"심심한 지금, 어떠세요?");
            messageWeekendHashMap.put(11,"또 다른 일상이 시작되었어요!");
            messageWeekendHashMap.put(12,"조용한 지금, 어떠세요?");
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        int index = cal.get(Calendar.HOUR_OF_DAY)/2 + 1;
        int weekday = cal.get(Calendar.DAY_OF_WEEK);

        if (weekday == 1 || weekday == 6) {
            msg = messageWeekendHashMap.get(index);
        } else {
            msg = messageDayHashMap.get(index);
        }

        BaseApplication.getInstance().progressON(this, msg);
    }

    public void progressON(String message) {
        BaseApplication.getInstance().progressON(this, message);
    }

    public void progressOFF() {
        BaseApplication.getInstance().progressOFF();
    }


}
