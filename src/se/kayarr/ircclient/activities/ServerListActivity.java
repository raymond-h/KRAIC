package se.kayarr.ircclient.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import se.kayarr.ircclient.R;
import se.kayarr.ircclient.irc.ServerConnection;
import se.kayarr.ircclient.irc.ServerSettingsItem;
import se.kayarr.ircclient.services.ServerConnectionService;
import se.kayarr.ircclient.services.ServerConnectionService.ServiceBinder;
import se.kayarr.ircclient.shared.ServerEditDialogHelper;
import se.kayarr.ircclient.shared.SettingsDatabaseHelper;
import se.kayarr.ircclient.shared.StaticInfo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class ServerListActivity extends ActionBarActivity
		implements ServiceConnection, OnItemClickListener,
		
//					ServerWindowTilesFragment.ServiceRetriever,
		
					ServerConnectionService.OnConnectionListListener,
					
					ServerListDialogFragment.OnServerListClickedListener,
					ServerEditDialogHelper.OnServerItemEditedListener {
	
	@Getter private ServerConnectionService service;
	
	private AlertDialog currentDialog;
	
	private ListView serverList;
	private View tileGridNoneSelected;
	
	private ServerListAdapter listAdapter;
	
	private SettingsDatabaseHelper dbHelper;
	
	@Getter private boolean dualPane;
	
	private ServerConnection currentConn;
	private int currentConnPos = ListView.INVALID_POSITION;
	
	private static final String SAVED_BUNDLE_CURR_SELECTION = "se.kayarr.ircclient.current_selected_server";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dbHelper = new SettingsDatabaseHelper(this);
		
		setContentView(R.layout.serverlist_conn_list);
		
		serverList = (ListView) findViewById(R.id.serverlist_connection_list);
		serverList.setOnItemClickListener(this);
		
		tileGridNoneSelected = findViewById(R.id.serverlist_tile_grid_none_selected_view);
		if(tileGridNoneSelected != null) tileGridNoneSelected.setVisibility(View.GONE);
		
		listAdapter = new ServerListAdapter();
		serverList.setAdapter(listAdapter);
		
		serverList.setEmptyView( findViewById(R.id.serverlist_connection_list_empty_view) );
		
		View tileGrid = findViewById(R.id.serverlist_grid_fragment_container);
		dualPane = ( tileGrid != null && tileGrid.getVisibility() == View.VISIBLE );
		
		Intent serviceIntent = new Intent(getApplicationContext(), ServerConnectionService.class);
		startService(serviceIntent);
		
		if(dualPane) {
			serverList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			
			if(savedInstanceState != null) {
				currentConnPos = savedInstanceState.getInt(SAVED_BUNDLE_CURR_SELECTION, ListView.INVALID_POSITION);
			}
			
			Log.d(StaticInfo.APP_TAG, "CurrentConnPos is " + currentConnPos);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if(dualPane) {
			Log.d(StaticInfo.APP_TAG, "Saving currentConnPos as " + currentConnPos);
			
			outState.putInt(SAVED_BUNDLE_CURR_SELECTION, currentConnPos);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
	
	@Override
	public void onPause() {
		if(currentDialog != null) currentDialog.dismiss();
		
		super.onPause();
	}
	
	public void onServiceConnected(ComponentName name, IBinder b) {
		ServerConnectionService.ServiceBinder binder = (ServiceBinder) b;
		
		service = binder.getService();
		service.setOnConnectionListListener(this);
		
		listAdapter.setConnections(service.getConnections());
		
		// Set pos to first item if there are existing items and none are selected.
		if(currentConnPos == ListView.INVALID_POSITION && listAdapter.getCount() != 0) {
			currentConnPos = 0;
		}
		
		if(dualPane) {
			setShownWindows(currentConnPos);
		}
	}

	public void onServiceDisconnected(ComponentName name) {
		this.service.removeOnConnectionListListener(this);
		this.service = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.serverlist_menu, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_connect: {
				List<ServerSettingsItem> allServers = dbHelper.serverItems().getAllServers();
				
				if(allServers.size() > 0) {
					String[] serverNames = new String[allServers.size()+1];
					serverNames[0] = getString(R.string.serverlist_manual_connect);
					
					int i = 1;
					for(ServerSettingsItem serverItem : allServers) {
						serverNames[i++] = serverItem.getDisplayName();
					}
					
					Bundle args = new Bundle();
					args.putStringArray(ServerListDialogFragment.ARGUMENT_SERVER_NAMES, serverNames);
					
					ServerListDialogFragment dialog = new ServerListDialogFragment();
					dialog.setArguments(args);
					
					dialog.show(getSupportFragmentManager(), "server_list_dialog");
				}
				else {
					onServerListItemClicked(null, 0);
				}
				
				return true;
			}
			
			case R.id.menu_disconnect_all: {
				this.service.disconnectAll();
				
				return true;
			}
			
			case R.id.menu_settings: {
				Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
				startActivity(settingsIntent);
				
				return true;
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public void onServerListItemClicked(ServerListDialogFragment dialogFragment, int position) {
		if(position == 0) {
			//This is where we show "manual connect" dialog
			Log.v(StaticInfo.APP_TAG, "Manual connect goes here!");
			
			currentDialog = ServerEditDialogHelper.createDialog(this, null,
					
					getString(R.string.serverlist_connect_to), getString(R.string.serverlist_connect),
					
					this);
			
			currentDialog.show();
		}
		else {
			this.service.connectTo(dbHelper.serverItems().getAllServers().get(position-1));
		}
	}
	
	public void onServerItemEdited(ServerSettingsItem settingsItem,
			boolean added) {
		
		Log.d(StaticInfo.APP_TAG, "MANUAL CONNECT TO: " + settingsItem);
		this.service.connectTo(settingsItem);
	}

	public void onServerItemEditCancel(ServerSettingsItem settingsItem) {
		Log.d(StaticInfo.APP_TAG, "Cancelled manual connect");
	}
	
	public void onConnectionListChanged() {
		runOnUiThread(new Runnable() {
			public void run() {
				listAdapter.setConnections(service.getConnections());
				
				if(dualPane) {
					
					if(listAdapter.getCount() == 0) setShownWindows(ListView.INVALID_POSITION);
					
					updateNoneSelectedView();
				}
			}
		});
	}

	public void onConnectionInfoChanged(ServerConnection conn) {
		runOnUiThread(new Runnable() {
			public void run() {
				listAdapter.notifyDataSetChanged();
			}
		});
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		setShownWindows(position);
		
//		if(!dualPane) { //If using a single-pane layout (just server list), open ChatActivity for that conn.
//			Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
//			intent.putExtra(StaticInfo.EXTRA_CONN_ID, id);
//			startActivity(intent);
//		}
//		else {
//			setShownWindows(position); //Otherwise in dual-pane, show conn. windows as a grid to the right
//		}
		
	}
	
	private void setShownWindows(int position) {
		Log.d(StaticInfo.APP_TAG, "*** Showing windows for pos " + position);
		
		currentConnPos = position;
		currentConn = listAdapter.getItem(position);
		
		if(dualPane) {
			serverList.setItemChecked(position, true);
			
			ServerWindowTilesFragment tilesFragment =
					(ServerWindowTilesFragment) getSupportFragmentManager()
					.findFragmentById(R.id.serverlist_grid_fragment_container);
			
			if(currentConn != null) {
				if(tilesFragment == null || tilesFragment.getCurrentConnectionId() != currentConn.getId()) {
					//Time to change fragment
					
					tilesFragment = ServerWindowTilesFragment.createInstance( currentConn.getId() );
					
					getSupportFragmentManager().beginTransaction()
							.replace(R.id.serverlist_grid_fragment_container, tilesFragment)
							.commit();
				}
			}
			else {
				if(tilesFragment != null) {
					getSupportFragmentManager().beginTransaction()
							.remove(tilesFragment)
							.commit();
				}
			}
			
			updateNoneSelectedView();
		}
		else {
			if(currentConn != null) {
				//Start another activity with a new fragment for the server
				
				Intent intent = new Intent(getApplicationContext(), WindowTilesActivity.class);
				intent.putExtra(WindowTilesActivity.EXTRA_CONN_ID, currentConn.getId());
				startActivity(intent);
			}
		}
		
		if(currentConn != null)
			Log.d(StaticInfo.APP_TAG, "Showing windows for " + currentConn.getStatus().getTitle() + ","
				+ " it has " + currentConn.getWindows().size());
		else Log.d(StaticInfo.APP_TAG, "Showing no windows");
	}
	
	private void updateNoneSelectedView() {
		if(tileGridNoneSelected != null) {
			if(listAdapter.getCount() > 0 && currentConn == null) tileGridNoneSelected.setVisibility(View.VISIBLE);
			else tileGridNoneSelected.setVisibility(View.GONE);
		}
	}
	
	public class ServerListAdapter extends BaseAdapter {
		private List<ServerConnection> connections = new ArrayList<ServerConnection>();
		
		public void setConnections(final Map<Long, ServerConnection> connectionsMap) {
			connections.clear();
			connections.addAll(connectionsMap.values());
			
			notifyDataSetChanged();
			Log.v(StaticInfo.APP_TAG, "Dataset changed! Connections: " + connections.size());
		}
		
		public int getCount() {
			return connections != null ? connections.size() : 0;
		}

		public ServerConnection getItem(int position) {
			try {
				return connections != null ? connections.get(position) : null;
			}
			catch(IndexOutOfBoundsException e) {
				return null;
			}
		}

		public long getItemId(int position) {
			return connections != null ? getItem(position).getId() : -1;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.serverlist_connection_item, parent, false);
			}
			
			TextView serverName = (TextView) convertView.findViewById(R.id.serveritem_name);
			TextView serverInfo = (TextView) convertView.findViewById(R.id.serveritem_info);
			
			ServerConnection conn = getItem(position);
			serverName.setText(conn.getStatus().getTitle());
			serverInfo.setText(conn.getStatus().getInfo());
			
			return convertView;
		}
	}
}
