package se.kayarr.ircclient.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import se.kayarr.ircclient.R;
import se.kayarr.ircclient.activities.ServerListActivity;
import se.kayarr.ircclient.irc.ServerConnection;
import se.kayarr.ircclient.irc.ServerSettingsItem;
import se.kayarr.ircclient.shared.DeviceInfo;
import se.kayarr.ircclient.shared.SettingsDatabaseHelper;
import se.kayarr.ircclient.shared.StaticInfo;
import se.kayarr.ircclient.shared.Util;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.ServiceCompat;
import android.util.Log;
import android.util.SparseIntArray;

public class ServerConnectionService extends Service {
	private NotificationManager notificationManager;
	
	private Map<Long, ServerConnection> connections = new HashMap<Long, ServerConnection>();
	
	private SettingsDatabaseHelper dbHelper;
	
	private Method setForegroundMethod;
	private int foregroundNotificationId = -1;
	
	private long idCounter = 0;
	
	public long getLastId() {
		return idCounter;
	}

	public Map<Long, ServerConnection> getConnections() {
		return Collections.unmodifiableMap(connections);
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		handleStartRequest(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleStartRequest(intent, startId);
		return ServiceCompat.START_STICKY;
	}
	
	public void handleStartRequest(Intent intent, int startId) {
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new ServiceBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		try {
			setForegroundMethod = getClass().getMethod("setForeground", new Class<?>[] { boolean.class });
		}
		catch (NoSuchMethodException e) {}
		
		Intent intent = new Intent(getApplicationContext(), ServerListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		
		PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
		
		Notification notification = 
				new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_notification)
				.setTicker("Started KRIRC")
				
				.setContentTitle(Util.getApplicationLabel(getApplicationContext()))
				.setContentText("Running Service")
				.setContentIntent(pending)
				.build();
		
		startForegroundCompat(StaticInfo.SERVICE_NOTIFICATION_ID, notification);
		
		dbHelper = new SettingsDatabaseHelper(this);
	}
	
	@Override
	public void onDestroy() {
		dbHelper.close();
		
		stopForegroundCompat(true);
		
		disconnectAll();
		
		super.onDestroy();
	}
	
	/*
	 * This demonstrates why removing methods between API levels is a bad idea.
	 * Thank god they learned their lesson after Eclair...
	 */
	
	@SuppressLint("NewApi")
	void startForegroundCompat(int id, Notification notification) {
		if(DeviceInfo.isEclair(true)) {
			startForeground(id, notification);
		}
		else {
			try {
				foregroundNotificationId = id;
				notificationManager.notify(foregroundNotificationId, notification);
				setForegroundMethod.invoke(this, true);
			}
			catch (IllegalArgumentException e) { e.printStackTrace(); }
			catch (IllegalAccessException e) { e.printStackTrace(); }
			catch (InvocationTargetException e) { e.printStackTrace(); }
		}
	}
	
	@SuppressLint("NewApi")
	void stopForegroundCompat(boolean removeNotification) {
		if(DeviceInfo.isEclair(true)) {
			stopForeground(removeNotification);
		}
		else {
			try {
				if(removeNotification) notificationManager.cancel(foregroundNotificationId);
				setForegroundMethod.invoke(this, false);
			}
			catch (IllegalArgumentException e) { e.printStackTrace(); }
			catch (IllegalAccessException e) { e.printStackTrace(); }
			catch (InvocationTargetException e) { e.printStackTrace(); }
		}
	}
	
	public ServerConnection connectTo(ServerSettingsItem item) {
		Log.d(StaticInfo.APP_TAG, "Connecting to: "+item);

		long id = ++idCounter;
		final ServerConnection conn = new ServerConnection(this, id, item);
		connections.put(id, conn);
		
		notifyConnectionListChanged();
		
		Runnable runnable = new Runnable() {
			public void run() {
				conn.connect();
			}	
		};
		(new Thread(runnable)).start();
		return conn;
	}
	
	public void removeConnection(long id) {
		connections.remove(id);
		
		notifyConnectionListChanged();
	}
	
	public void disconnectAll() {
		for(ServerConnection conn : connections.values()) {
			conn.disconnect();
		}
		connections.clear();
		
		notifyConnectionListChanged();
	}
	
	public class ServiceBinder extends Binder {
		public ServerConnectionService getService() {
			return ServerConnectionService.this;
		}
	}
	
	private Handler mainThreadHandler = new Handler(Looper.getMainLooper());
	
	public boolean runOnUiThread(Runnable runnable) {
		return mainThreadHandler.post(runnable);
	}
	
	private OnConnectionListListener connectionListCallback;
	
	public static interface OnConnectionListListener {
		public void onConnectionListChanged();
		
		public void onConnectionInfoChanged(ServerConnection conn);
	}
	
	public void setOnConnectionListListener(OnConnectionListListener l) {
		connectionListCallback = l;
	}
	
	public void removeOnConnectionListListener(OnConnectionListListener l) {
		if(connectionListCallback == l) connectionListCallback = null;
	}
	
	private void notifyConnectionListChanged() {
		connectionListCallback.onConnectionListChanged();
	}
	
	public void notifyInfoChanged(ServerConnection conn) {
		connectionListCallback.onConnectionInfoChanged(conn);
	}
	
	public SparseIntArray getColors() {
		return dbHelper.colors().getColors();
	}
}
