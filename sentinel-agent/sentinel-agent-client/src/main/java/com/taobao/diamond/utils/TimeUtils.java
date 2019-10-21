package com.taobao.diamond.utils;

import java.sql.Timestamp;
import java.util.Date;

public class TimeUtils {

	public static Timestamp getCurrentTime() {
        Date date = new Date();
        return new Timestamp(date.getTime());
    }
	
}
