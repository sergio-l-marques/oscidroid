package com.megateam.oscidroid;

import android.util.Log;

public class L {
	private static final boolean SHUT_UP = false;

	public static void d(Object o){
		if(BuildConfig.DEBUG && !SHUT_UP)
			Log.d("OsciDroid", String.valueOf(o));
	}
	public static void d(String s, Object ... args){
		if(BuildConfig.DEBUG && !SHUT_UP)
			Log.d("OsciDroid", String.format(s,args));
	}
	public static void i(Object o){
		if(BuildConfig.DEBUG && !SHUT_UP)
			Log.d("OsciDroid", String.valueOf(o));
	}
	public static void i(String s, Object ... args){
		if(BuildConfig.DEBUG && !SHUT_UP)
			Log.d("OsciDroid", String.format(s,args));
	}
	public static void e(Object o){
		if(BuildConfig.DEBUG && !SHUT_UP)
			Log.e("OsciDroid", String.valueOf(o));
	}
}
