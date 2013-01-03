package se.kayarr.ircclient.shared;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.Getter;
import se.kayarr.ircclient.irc.ServerSettingsItem;
import se.kayarr.ircclient.irc.UserInfo;
import android.content.Context;
import android.graphics.Color;
import android.util.SparseIntArray;

public class Settings {
	private static Settings instance;
	
	public static Settings getInstance(Context context) {
		if(instance == null) instance = new Settings(context);
		return instance;
	}
	
	private Settings(Context context) {
		serverSettings.add(
				new ServerSettingsItem.Builder()
				.setName("EsperNet #1")
				.setAddress("irc.esper.net").setPort(6667).build()
		);
		serverSettings.add(
				new ServerSettingsItem.Builder()
				.setName("EsperNet #2")
				.setAddress("irc.esper.net").setPort(6668)
				.setNick("RayKayyyy").build()
		);
		
		colorMap.append(0, Color.WHITE);
		colorMap.append(1, Color.BLACK);
		colorMap.append(2, Color.rgb(0, 0, 128));
		colorMap.append(3, Color.rgb(0, 128, 0));
		colorMap.append(4, Color.RED);
		colorMap.append(5, Color.rgb(128, 0, 0));
		colorMap.append(6, Color.rgb(128, 0, 128));
		colorMap.append(7, Color.rgb(255, 128, 0));
		colorMap.append(8, Color.YELLOW);
		colorMap.append(9, Color.GREEN);
		colorMap.append(10, Color.rgb(0, 128, 128));
		colorMap.append(11, Color.CYAN);
		colorMap.append(12, Color.BLUE);
		colorMap.append(13, Color.rgb(255, 64, 255));
		colorMap.append(14, Color.DKGRAY);
		colorMap.append(15, Color.LTGRAY);
	}
	
	//*
	@Getter private UserInfo defaultUserInfo = new UserInfo("KR-AIC", "KR-AIC", "Hubba Hubba", "");
	
	@Getter private List<ServerSettingsItem> serverSettings = new ArrayList<ServerSettingsItem>();
	
	@Getter private boolean usingTimestamps = true;
	@Getter private SimpleDateFormat timestampFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
	
	@Getter private SparseIntArray colorMap = new SparseIntArray();
	
	private boolean formatChanged = false;
	
	public boolean outputFormatsChanged() {
		return formatChanged;
	}
	//*/
}
