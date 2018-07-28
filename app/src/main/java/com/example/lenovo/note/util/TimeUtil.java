package com.example.lenovo.note.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Lenovo on 2018/7/27.
 */

public class TimeUtil {
//    private static SimpleDateFormat simpleDateFormat=new SimpleDateFormat
//            ("MM月dd日 HH:mm");
//
//    public static long currentTime(){
//        return System.currentTimeMillis();
//    }
    private static SimpleDateFormat HHmm=new SimpleDateFormat("HH:mm");
    private static SimpleDateFormat MMddHHmm=new SimpleDateFormat("MM/dd HH:mm");
    private static SimpleDateFormat yyyyMMddHHmm=new SimpleDateFormat("yyyy/MM/dd HH:mm");

    public static String timeString(long time){
        Date date=new Date(time);
        if(isThisYear(date)){
            if(isToday(date)){
                return "今天 "+HHmm.format(date);
            }else if(isYesterday(date)){
                return "昨天 "+HHmm.format(date);
            }else{
                return MMddHHmm.format(date);
            }
        }else{
            return yyyyMMddHHmm.format(date);
        }
    }

    public static boolean isToday(Date date){
        return isThisTime(date,"yyyy/MM/dd");
    }

    public static boolean isYesterday(Date date){
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE,1);
        return isThisTime(calendar.getTime(),"yyyy/MM/dd");
    }

    public static boolean isThisYear(Date date){
        return isThisTime(date,"yyyy");
    }

    private static boolean isThisTime(Date date,String timeFormat){
        SimpleDateFormat sdf=new SimpleDateFormat(timeFormat);
        String now=sdf.format(new Date());
        String dateStr=sdf.format(date);
        if(now.equals(dateStr)){
            return true;
        }
        return false;
    }
}
