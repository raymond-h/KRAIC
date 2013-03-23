package se.kayarr.ircclient.activities;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import se.kayarr.ircclient.R;
import se.kayarr.ircclient.irc.ServerConnection;
import se.kayarr.ircclient.irc.Window;
import se.kayarr.ircclient.irc.output.OutputLine;
import se.kayarr.ircclient.services.ServerConnectionService;
import se.kayarr.ircclient.shared.StaticInfo;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ServerWindowTilesFragment extends Fragment
		implements OnItemClickListener, ServerConnection.OnWindowListListener {
	
	public static final String ARGS_CONN_ID = "se.kayarr.ircclient.server_id";
	
	private GridView tileGridView;
	private WindowGridAdapter gridAdapter;
	
	private ServerConnectionService service;
	
	@Getter private long currentConnectionId;
	private ServerConnection currentConnection;
	
	public static ServerWindowTilesFragment createInstance(long connId) {
		ServerWindowTilesFragment instance = new ServerWindowTilesFragment();
		
		Bundle args = new Bundle();
		args.putLong(ARGS_CONN_ID, connId);
		instance.setArguments(args);
		
		return instance;
	}
	
	public static interface ServiceRetriever {
		public ServerConnectionService getService();
	}
	
	private ServiceRetriever attachedActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			attachedActivity = (ServiceRetriever) activity;
		}
		catch(ClassCastException e) {
			throw new ClassCastException(activity + " needs to implements ServiceRetriever");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		currentConnectionId = getArguments().getLong(ARGS_CONN_ID);
		
		service = attachedActivity.getService();
		
		currentConnection = service.getConnections().get(currentConnectionId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View layout = inflater.inflate(R.layout.serverlist_tile_grid, container, false);
		
		tileGridView = (GridView) layout.findViewById(R.id.tile_grid_view);
		
		gridAdapter = new WindowGridAdapter();
		tileGridView.setAdapter(gridAdapter);
		
		tileGridView.setOnItemClickListener(this);
		
		onWindowListChanged(currentConnection);
		
		Log.d(StaticInfo.APP_TAG, "Now showing " + currentConnection.getWindows().size() + " tiles");
		
		return layout;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		currentConnection.addOnWindowListListener(this);
		
		gridAdapter.notifyDataSetChanged();
		
		for(GridViewUpdateHelper helper : gridAdapter.helpers) {
			helper.registerToWindow();
		}
	}

	@Override
	public void onStop() {
		for(GridViewUpdateHelper helper : gridAdapter.helpers) {
			helper.unregisterFromWindow();
		}
		
		currentConnection.removeOnWindowListListener(this);
		
		super.onStop();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	public void setCurrentConnection(ServerConnection currentConnection) {
		this.currentConnection = currentConnection;
	}

	public void onItemClick(AdapterView<?> parent, View child, int position, long id) {
		
		Window window = currentConnection.getWindows().get(position);
		
		Log.d(StaticInfo.APP_TAG, "Item " + position + " " + window.getTitle() + " clicked in grid");
		
		Intent intent = new Intent( getActivity().getApplicationContext(), ChatActivity.class );
		
		intent.putExtra(StaticInfo.EXTRA_CONN_ID, currentConnection.getId());
		intent.putExtra(StaticInfo.EXTRA_CONN_WINDOW, position);
		
		startActivity(intent);
		
//		if(DeviceInfo.isJellyBean(true)) {
//			ActivityOptions options = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
//			startActivity(intent, options.toBundle());
//		}
//		else startActivity(intent);
		
	}
	
	public void onWindowListChanged(final ServerConnection connection) {
		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				Log.d(StaticInfo.APP_TAG, "*************** UPDATING GRIDADAPTER IN FRAGMENT");
				gridAdapter.updateWindowList(connection != null ? connection.getWindows() : null);
				gridAdapter.notifyDataSetChanged();
			}
		});
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
				convertView = getActivity().getLayoutInflater().inflate(R.layout.serverlist_wide_grid_tile, parent, false);
				
				helper = new GridViewUpdateHelper(convertView);
				helpers.add(helper);
				convertView.setTag(helper);
			}
			else {
				helper = (GridViewUpdateHelper) convertView.getTag();
			}
			
			Window window = getItem(position);
			helper.update(window);
			
			//Log.d(StaticInfo.APP_TAG, "Pos #" + position + ": Window " + window.getTitle() + " assoc. with " + convertView);
			
			switch(window.getType()) {
				case CHANNEL:
					helper.cornerIcon.setImageResource(R.drawable.ic_tile_corner_chan);
					break;
				case USER:
					helper.cornerIcon.setImageResource(R.drawable.ic_tile_corner_pm);
					break;
				case STATUS:
					helper.cornerIcon.setImageResource(R.drawable.ic_tile_corner_status);
					break;
			}
			
			TextView title = helper.title;
			TextView line1 = helper.line1;
			TextView line2 = helper.line2;
			
			title.setText( window.getTitle() );
			
			List<OutputLine> lines = window.getLines();
			
			//Log.d(StaticInfo.APP_TAG, "There are " + lines.size() + " lines for " + window.getTitle());
			
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
		
		public void registerToWindow() {
			window.addOnOutputListener(this);
		}
		
		public void unregisterFromWindow() {
			window.removeOnOutputListener(this);
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
