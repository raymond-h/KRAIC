package se.kayarr.ircclient.shared;

import android.os.Build;

public class DeviceInfo {
	/**
	 * Wrapper method that simply returns {@code Build.VERSION.SDK_INT}, which
	 * represents the API level of the Android version that the device is running.
	 * 
	 * @return The value of {@code Build.VERSION.SDK_INT}
	 */
	public static int apiVersion() {
		return Build.VERSION.SDK_INT;
	}
	
	public static boolean isJellyBeanMr1(boolean atleast) {
		return atleast ?
			(apiVersion() >= Build.VERSION_CODES.JELLY_BEAN_MR1):
			(apiVersion() == Build.VERSION_CODES.JELLY_BEAN_MR1);
	}
	
	public static boolean isJellyBean(boolean atleast) {
		return atleast ?
				(apiVersion() >= Build.VERSION_CODES.JELLY_BEAN) :
				(apiVersion() == Build.VERSION_CODES.JELLY_BEAN);
	}

	public static boolean isIceCreamSandwich(boolean atleast) {
		return atleast ?
				(apiVersion() >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) :
				(apiVersion() == Build.VERSION_CODES.ICE_CREAM_SANDWICH);
	}
	
	public static boolean isHoneycomb(boolean atleast) {
		return atleast ?
				(apiVersion() >= Build.VERSION_CODES.HONEYCOMB) :
				(apiVersion() == Build.VERSION_CODES.HONEYCOMB);
	}
	
	@Deprecated
	public static boolean isGingerbread(boolean atleast) {
		return atleast ?
			(apiVersion() >= Build.VERSION_CODES.GINGERBREAD) :
			(apiVersion() == Build.VERSION_CODES.GINGERBREAD);
	}
	
	@Deprecated
	public static boolean isFroyo(boolean atleast) {
		return atleast ?
			(apiVersion() >= Build.VERSION_CODES.FROYO) :
			(apiVersion() == Build.VERSION_CODES.FROYO);
	}
	
	@Deprecated
	public static boolean isEclairMr1(boolean atleast) {
		return atleast ?
			(apiVersion() >= Build.VERSION_CODES.ECLAIR_MR1):
			(apiVersion() == Build.VERSION_CODES.ECLAIR_MR1);
	}
	
	@Deprecated
	public static boolean isEclair01(boolean atleast) {
		return atleast ?
			(apiVersion() >= Build.VERSION_CODES.ECLAIR_0_1):
			(apiVersion() == Build.VERSION_CODES.ECLAIR_0_1);
	}
	
	@Deprecated
	public static boolean isEclair(boolean atleast) {
		return atleast ?
			(apiVersion() >= Build.VERSION_CODES.ECLAIR):
			(apiVersion() == Build.VERSION_CODES.ECLAIR);
	}
}
