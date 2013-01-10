package se.kayarr.ircclient.shared;

import java.text.SimpleDateFormat;
import java.util.Locale;

import lombok.Getter;
import android.content.Context;

public class Settings {
	private static Settings instance;
	
	public static Settings getInstance(Context context) {
		if(instance == null) instance = new Settings(context);
		return instance;
	}
	
	private Settings(Context context) {
	}
	
	//*
	@Getter private boolean usingTimestamps = true;
	@Getter private SimpleDateFormat timestampFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
	
	private boolean formatChanged = false;
	
	public boolean outputFormatsChanged() {
		return formatChanged;
	}
	//*/
}
