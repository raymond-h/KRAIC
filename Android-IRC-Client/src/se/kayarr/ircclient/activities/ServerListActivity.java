package se.kayarr.ircclient.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import se.kayarr.ircclient.R;
import se.kayarr.ircclient.irc.ServerConnection;
import se.kayarr.ircclient.irc.ServerListDatabaseHelper;
import se.kayarr.ircclient.services.ServerConnectionService;
import se.kayarr.ircclient.services.ServerConnectionService.OnConnectionListListener;
import se.kayarr.ircclient.services.ServerConnectionService.ServiceBinder;
import se.kayarr.ircclient.shared.StaticInfo;
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
import android.widget.ListView;
import android.widget.TextView;


public class ServerListActivity extends CompatActionBarActivity implements ServiceConnection, OnConnectionListListener {
	private ServerConnectionService service;
	
	private ListView serverList;
	private ServerListAdapter listAdapter;
	
	private ServerListDatabaseHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dbHelper = new ServerListDatabaseHelper(this);
		
		setContentView(R.layout.serverlist);
		
		serverList = (ListView) findViewById(R.id.server_list);
		
		listAdapter = new ServerListAdapter();
		serverList.setOnItemClickListener(listAdapter);
		serverList.setAdapter(listAdapter);
		
		Intent serviceIntent = new Intent(getApplicationContext(), ServerConnectionService.class);
		startService(serviceIntent);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.serverlist_menu, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_connect: {
				this.service.connectTo(dbHelper.serverItems().getAllServers().get(0));
				
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

	public void onServiceConnected(ComponentName name, IBinder service) {
		ServerConnectionService.ServiceBinder binder = (ServiceBinder) service;
		
		this.service = binder.getService();
		this.service.setOnConnectionListListener(this);
		
		listAdapter.setConnections(this.service.getConnections());
	}

	public void onServiceDisconnected(ComponentName name) {
		this.service.removeOnConnectionListListener(this);
		this.service = null;
	}
	
	public void onConnectionListChanged() {
		runOnUiThread(new Runnable() {
			public void run() {
				listAdapter.setConnections(service.getConnections());
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
	
	public class ServerListAdapter extends BaseAdapter implements OnItemClickListener {
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

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
			intent.putExtra(StaticInfo.EXTRA_CONN_ID, id);
			startActivity(intent);
		}
	}
}
