package se.kayarr.ircclient.activities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import se.kayarr.ircclient.R;
import se.kayarr.ircclient.irc.ServerConnection;
import se.kayarr.ircclient.irc.ServerSettingsItem;
import se.kayarr.ircclient.irc.Window;
import se.kayarr.ircclient.irc.output.OutputLine;
import se.kayarr.ircclient.services.ServerConnectionService;
import se.kayarr.ircclient.services.ServerConnectionService.ServiceBinder;
import se.kayarr.ircclient.shared.DeviceInfo;
import se.kayarr.ircclient.shared.ServerEditDialogHelper;
import se.kayarr.ircclient.shared.SettingsDatabaseHelper;
import se.kayarr.ircclient.shared.StaticInfo;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class ServerListActivity extends CompatActionBarActivity
		implements ServiceConnection, ServerConnectionService.OnConnectionListListener,
					ServerConnection.OnWindowListListener, OnItemClickListener,
					ServerListDialogFragment.OnServerListClickedListener,
					ServerEditDialogHelper.OnServerItemEditedListener {
	
	private ServerConnectionService service;
	
	private AlertDialog currentDialog;
	
	private ListView serverList;
	private GridView gridAreaView;
	private TextView gridAreaText;
	
	private ServerListAdapter listAdapter;
	private WindowGridAdapter gridAdapter;
	
	private SettingsDatabaseHelper dbHelper;
	
	@Getter private boolean dualPane;
	
	private ServerConnection currentConn;
	private int currentConnPos = ListView.INVALID_POSITION;
	
	private static final String SAVED_BUNDLE_CURR_SELECTION = "se.kayarr.ircclient.current_selected_server";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dbHelper = new SettingsDatabaseHelper(this);
		
		setContentView(R.layout.serverlist);
		
		serverList = (ListView) findViewById(R.id.server_list);
		serverList.setOnItemClickListener(this);
		
		listAdapter = new ServerListAdapter();
		serverList.setAdapter(listAdapter);
		
		serverList.setEmptyView( findViewById(R.id.server_list_empty_view) );
		
		gridAreaView = (GridView) findViewById(R.id.wide_grid_view);
		gridAreaText = (TextView) findViewById(R.id.wide_grid_area_text);
		
		dualPane = ( gridAreaView != null && gridAreaText != null );
		
		Intent serviceIntent = new Intent(getApplicationContext(), ServerConnectionService.class);
		startService(serviceIntent);
		
		if(dualPane) {
			serverList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			
			gridAreaText.setVisibility(View.GONE);
			
			gridAdapter = new WindowGridAdapter();
			gridAreaView.setAdapter(gridAdapter);
			
			gridAreaView.setOnItemClickListener(this);
			
			if(savedInstanceState != null) {
				currentConnPos = savedInstanceState.getInt(SAVED_BUNDLE_CURR_SELECTION, ListView.INVALID_POSITION);
			}
			
			Log.d(StaticInfo.DEBUG_TAG, "CurrentConnPos is " + currentConnPos);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if(dualPane) {
			Log.d(StaticInfo.DEBUG_TAG, "Saving currentConnPos as " + currentConnPos);
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
		if(dualPane) {
			for(GridViewUpdateHelper helper : gridAdapter.helpers) {
				helper.update(null);
			}
		}
		
		if(service != null) unbindService(this);
		
		dbHelper.close();
		
		super.onStop();
	}
	
	@Override
	public void onPause() {
		if(currentDialog != null) currentDialog.dismiss();
		
		super.onPause();
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
				currentConn = null;
				currentConnPos = ListView.INVALID_POSITION;
				
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

	public void onServiceConnected(ComponentName name, IBinder b) {
		ServerConnectionService.ServiceBinder binder = (ServiceBinder) b;
		
		service = binder.getService();
		service.setOnConnectionListListener(this);
		
		listAdapter.setConnections(service.getConnections());
		
		if(dualPane) {
			if(currentConnPos != ListView.INVALID_POSITION) {
				setShownWindows(currentConnPos);
			}
			else if(listAdapter.getCount() > 0) gridAreaText.setVisibility(View.VISIBLE);
		}
	}

	public void onServiceDisconnected(ComponentName name) {
		this.service.removeOnConnectionListListener(this);
		this.service = null;
	}
	
	public void onConnectionListChanged() {
		runOnUiThread(new Runnable() {
			public void run() {
				listAdapter.setConnections(service.getConnections());
				
				if(dualPane) {
					if(listAdapter.getCount() == 0) currentConn = null;
					
					if(currentConn == null) {
						gridAdapter.updateWindowList(null);
						
						if(listAdapter.getCount() > 0) gridAreaText.setVisibility(View.VISIBLE);
					}
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
	
	@SuppressLint("NewApi")
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		if(parent == serverList) { //Item on server list was clicked
			
			if(!dualPane) { //If using a single-pane layout (just server list), open ChatActivity for that conn.
				Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
				intent.putExtra(StaticInfo.EXTRA_CONN_ID, id);
				startActivity(intent);
			}
			else {
				currentConnPos = position;
				setShownWindows(position); //Otherwise in dual-pane, show conn. windows as a grid to the right
			}
			
		}
		
		else if(dualPane && parent == gridAreaView) { //Item in grid was clicked (only in dual-pane mode)
			
			Window window = currentConn.getWindows().get(position);
			
			Log.d(StaticInfo.APP_TAG, "Item " + position + " " + window.getTitle() + " clicked in grid");
			
			Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
			intent.putExtra(StaticInfo.EXTRA_CONN_ID, currentConn.getId());
			intent.putExtra(StaticInfo.EXTRA_CONN_WINDOW, position);
			
			if(DeviceInfo.isJellyBean(true)) {
				ActivityOptions options = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
				startActivity(intent, options.toBundle());
			}
			else startActivity(intent);
			
		}
	}
	
	//*** This method is only called in dual-pane mode
	private void setShownWindows(int position) {
		if(!dualPane) return; //Dual-pane only!
		
		Log.d(StaticInfo.DEBUG_TAG, "*** Showing windows for pos " + position);
		
		serverList.setItemChecked(position, true);
		
		if( currentConn != null ) {
			currentConn.removeOnWindowListListener(this);
		}
		
		currentConn = listAdapter.getItem(position);
		
		Log.d(StaticInfo.DEBUG_TAG, "Getting info for conn " + currentConn.getId() + " at " + position);
		
		currentConn.addOnWindowListListener(this);
		
		onWindowListChanged(currentConn);
		
		gridAreaText.setVisibility(View.GONE);
		
		Log.d(StaticInfo.APP_TAG, "Showing windows for " + currentConn.getStatus().getTitle() + ","
				+ " it has " + currentConn.getWindows().size());
	}

	public void onWindowListChanged(final ServerConnection connection) {
		runOnUiThread(new Runnable() {
			public void run() {
				Log.d(StaticInfo.APP_TAG, "*************** UPDATING GRIDADAPTER");
				gridAdapter.updateWindowList(connection != null ? connection.getWindows() : null);
				gridAdapter.notifyDataSetChanged();
			}
		});
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
			return connections != null ? connections.get(position) : null;
		}

		public long getItemId(int position) {
			return connections != null ? getItem(position).getId() : -1;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.serverlist_item, parent, false);
			}
			
			TextView serverName = (TextView) convertView.findViewById(R.id.serveritem_name);
			TextView serverInfo = (TextView) convertView.findViewById(R.id.serveritem_info);
			
			ServerConnection conn = getItem(position);
			serverName.setText(conn.getStatus().getTitle());
			serverInfo.setText(conn.getStatus().getInfo());
			
			return convertView;
		}
	}
	
	public class WindowGridAdapter extends BaseAdapter {
		private List<Window> windows;
		private List<GridViewUpdateHelper> helpers = new LinkedList<GridViewUpdateHelper>();
		
		public void updateWindowList(List<Window> windows) {
			this.windows = windows;
		}
		
		public int getCount() {
			return windows != null ? windows.size() : 0;
		}

		public Window getItem(int position) {
			return windows != null ? windows.get(position) : null;
		}

		public long getItemId(int position) {
			return -1;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			GridViewUpdateHelper helper;
			
			if(convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.serverlist_wide_grid_tile, parent, false);
				
				helper = new GridViewUpdateHelper(convertView);
				helpers.add(helper);
				convertView.setTag(helper);
			}
			else {
				helper = (GridViewUpdateHelper) convertView.getTag();
			}
			
			Window window = getItem(position);
			helper.update(window);
			
			Log.d(StaticInfo.DEBUG_TAG, "Pos #" + position + ": Window " + window.getTitle() + " assoc. with " + convertView);
			
			switch(window.getType()) {
				case CHANNEL: 
					convertView.setBackgroundResource(R.drawable.grid_tile_border_channel);
					helper.cornerIcon.setImageResource(R.drawable.ic_tile_corner_chan);
					break;
				case USER:
					convertView.setBackgroundResource(R.drawable.grid_tile_border_pm);
					helper.cornerIcon.setImageResource(R.drawable.ic_tile_corner_pm);
					break;
				case STATUS:
					convertView.setBackgroundResource(R.drawable.grid_tile_border_status);
					helper.cornerIcon.setImageResource(R.drawable.ic_tile_corner_status);
					break;
			}
			
			TextView title = helper.title;
			TextView line1 = helper.line1;
			TextView line2 = helper.line2;
			
			title.setText( window.getTitle() );
			
			List<OutputLine> lines = window.getLines();
			
			Log.d(StaticInfo.DEBUG_TAG, "There are " + lines.size() + " lines for " + window.getTitle());
			
			if(lines.size() >= 1) {
				line2.setText( lines.get(lines.size()-1).getOutput() );
				
				if(lines.size() >= 2) {
					line1.setText( lines.get(lines.size()-2).getOutput() );
				}
				else line1.setText("");
			}
			else {
				line2.setText("");
				line1.setText("");
			}
			
			return convertView;
		}
	}
	
	private static class GridViewUpdateHelper implements Window.OnOutputListener {
		@SuppressWarnings("unused")
		private WeakReference<View> view;
		
		private TextView title;
		private TextView line1;
		private TextView line2;
		private ImageView cornerIcon;
		
		private Window window;
		
		public GridViewUpdateHelper(View view) {
			this.view = new WeakReference<View>(view);
			
			title = (TextView)view.findViewById(R.id.grid_tile_title);
			line1 = (TextView)view.findViewById(R.id.grid_tile_line_1);
			line2 = (TextView)view.findViewById(R.id.grid_tile_line_2);
			cornerIcon = (ImageView) view.findViewById(R.id.grid_tile_corner_icon);
		}
		
		public void update(Window window) {
			if(this.window != null)
				this.window.removeOnOutputListener(this);
			
			this.window = window;
			
			if(window != null)
				window.addOnOutputListener(this);
		}
		
		public void onOutputLineAdded(Window window, OutputLine line) {
			//Log.d(StaticInfo.APP_TAG, "View " + view + ", window " + window.getTitle() + " got line " + line);
			
			line1.setText(line2.getText());
			line2.setText(line.getOutput());
		}

		public void onOutputCleared(Window window) {
			//Log.d(StaticInfo.APP_TAG, "View " + view + ", window " + window.getTitle() + " was cleared");
			
			line1.setText("");
			line2.setText("");
		}
	}
}
