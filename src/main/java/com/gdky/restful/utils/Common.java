package com.gdky.restful.utils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Common {
	// 生成UUID
	public static String newUUID() {
		String uuid = UUID.randomUUID().toString().replace("-", "")
				.toUpperCase();
		return uuid;
	}

	// 获取目前时间的Mysql DateTime格式
	public static String getCurrentTime2MysqlDateTime() {
		java.util.Date dt = new java.util.Date();

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		String currentTime = sdf.format(dt);
		return currentTime;
	}
	
	public static String getDate2MysqlDateTime(Date date) {

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		String dt = sdf.format(date);
		return dt;
	}

	public static String getTime2MysqlDateTime(String date) {

		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat utcFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.CHINA);
		
		utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String time = "";
		try {
			Date d = utcFormat.parse(date);
			time = sdf.format(d);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return time;
	}
	public static Date getTimeFromJsToJava(String date) {

		SimpleDateFormat utcFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.CHINA);
		
		utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date d = null;
		try {
			d = utcFormat.parse(date);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return d;
	}

	// 数字补零兼转换字符串 
    public static String addZero(int num,int len){  
        StringBuffer s = new StringBuffer() ;  
        s.append(num) ;  
        while(s.length()<len){   // 如果长度不足，则继续补0  
            s.insert(0,"0") ;   // 在第一个位置处补0  
        }  
        return s.toString() ;  
    }
    
    public static Map<String,Object> decodeURItoMap(String URIstring){
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	try {
    		URIstring = java.net.URLDecoder.decode(URIstring, "UTF-8");
			ObjectMapper mapper = new ObjectMapper();
			map = mapper.readValue(URIstring,
					new TypeReference<Map<String, Object>>() {
					});
		} catch (Exception e) {
		}
		return map;
    }
}
