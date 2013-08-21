package se.kayarr.ircclient.activities;

import lombok.Getter;
import se.kayarr.ircclient.irc.ServerConnection;
import se.kayarr.ircclient.services.ServerConnectionService;
import se.kayarr.ircclient.services.ServerConnectionService.ServiceBinder;
import se.kayarr.ircclient.shared.SettingsDatabaseHelper;
import se.kayarr.ircclient.shared.StaticInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

public class WindowTilesActivity extends ActionBarActivity
		implements ServiceConnection
//					ServerWindowTilesFragment.ServiceRetriever
					{
	
	public static final String EXTRA_CONN_ID = "se.kayarr.ircclient.extra_connection_id";
	
	@Getter private ServerConnectionService service;
	
	private SettingsDatabaseHelper dbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dbHelper = new SettingsDatabaseHelper(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		Intent serviceIntent = new Intent(getApplicationContext(), ServerConnectionService.class);
		bindService(serviceIntent, this, 0);
	}
	
	@Override
	protected void onStop() {
		if(service != null) unbindService(this);
		
		dbHelper.close();
		
		super.onStop();
	}

	public void onServiceConnected(ComponentName name, IBinder b) {
		ServerConnectionService.ServiceBinder binder = (ServiceBinder) b;
		
		service = binder.getService();
		
		ServerConnection connection = service.getConnections().get( getIntent().getLongExtra(EXTRA_CONN_ID, -1) );
		
		getSupportActionBar().setTitle( connection.getSettingsItem().getDisplayName() );
		getSupportActionBar().setSubtitle( connection.getSettingsItem().getDisplayAddress() );
		
		if(getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
			//No fragment is currently shown
			ServerWindowTilesFragment f =
					ServerWindowTilesFragment.createInstance(
							getIntent().getLongExtra(EXTRA_CONN_ID, -1)
					);
			
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, f)
					.commit();
		}
	}

	public void onServiceDisconnected(ComponentName name) {
		this.service = null;
	}
	
	@Override
	public void onAttachFragment(Fragment fragment) {
		Log.d(StaticInfo.APP_TAG, getClass().getSimpleName() + ".onAttachFragment called with " + fragment);
		
		super.onAttachFragment(fragment);
	}
}
