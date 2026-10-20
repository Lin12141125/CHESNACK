package util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {
	public static boolean isEmpty(String str) {
		return str==null||str.trim().equals("");
	}

	/**
	 * Format a DB datetime object (Date / LocalDateTime / String) as yyyy/MM/dd
	 */
	public static String fmtDate(Object o) {
		if (o == null) return "";
		if (o instanceof Date) {
			return new SimpleDateFormat("yyyy/MM/dd").format((Date) o);
		}
		String s = o.toString();
		int t = s.indexOf('T');
		if (t > 0) s = s.substring(0, t);
		int sp = s.indexOf(' ');
		if (sp > 0) s = s.substring(0, sp);
		return s;
	}

	/**
	 * Format a DB datetime object as HH:mm
	 */
	public static String fmtTime(Object o) {
		if (o == null) return "";
		String s = o.toString();
		int t = s.indexOf('T');
		if (t > 0) s = s.substring(t + 1);
		int sp = s.indexOf(' ');
		if (sp > 0) s = s.substring(sp + 1);
		if (s.length() > 5) s = s.substring(0, 5);
		return s;
	}
}