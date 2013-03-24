package se.kayarr.ircclient.activities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.pircbotx.Colors;

import se.kayarr.ircclient.R;
import se.kayarr.ircclient.exceptions.InvalidWindowTypeException;
import se.kayarr.ircclient.irc.CommandManager;
import se.kayarr.ircclient.irc.ServerConnection;
import se.kayarr.ircclient.irc.ServerConnection.OnCurrentWindowChangeListener;
import se.kayarr.ircclient.irc.ServerConnection.OnWindowListListener;
import se.kayarr.ircclient.irc.Window;
import se.kayarr.ircclient.irc.output.OutputLine;
import se.kayarr.ircclient.services.ServerConnectionService;
import se.kayarr.ircclient.services.ServerConnectionService.ServiceBinder;
import se.kayarr.ircclient.shared.DeviceInfo;
import se.kayarr.ircclient.shared.StaticInfo;
import se.kayarr.ircclient.shared.Util;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class ChatActivity extends CompatActionBarActivity
		implements ServiceConnection, OnWindowListListener, OnCurrentWindowChangeListener, OnPageChangeListener {
	private static final String WINDOW_KEY = "se.kayarr.ircclient.window-key";
	
	private ServerConnectionService service;
	
	private ServerConnection connection;
	
	private ViewPager fragmentPager;
	private WindowPagerAdapter pagerAdapter;
	
	private List<WeakReference<WindowFragment>> windowFragments = new LinkedList<WeakReference<WindowFragment>>();
	
	private boolean hasSavedInstanceState;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(DeviceInfo.isHoneycomb(true)) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		setContentView(R.layout.chat_base);
		
		fragmentPager = (ViewPager) findViewById(R.id.fragment_pager);
		
		pagerAdapter = new WindowPagerAdapter(getSupportFragmentManager());
		
		fragmentPager.setAdapter(pagerAdapter);
		fragmentPager.setOnPageChangeListener(this);
		
		PagerTabStrip titleStrip = (PagerTabStrip) fragmentPager.findViewById(R.id.fragment_pager_titlestrip);
		titleStrip.setTabIndicatorColor( getResources().getColor(R.color.holo_blue) );
		
		hasSavedInstanceState = (savedInstanceState != null);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		Intent serviceIntent = new Intent(getApplicationContext(), ServerConnectionService.class);
		if(!bindService(serviceIntent, this, 0)) {
			Log.e(StaticInfo.APP_TAG, "ChatActivity: Unable to bind to ServerConnectionService");
			finish();
		}
	}

	@Override
	protected void onStop() {
		//Log.d(StaticInfo.APP_TAG, "onStop " + this + " begin");
		
		if(service != null) {
			connection.removeOnCurrentWindowChangeListener(this);
			connection.removeOnWindowListListener(this);
			
			//Log.d(StaticInfo.APP_TAG, "onStop " + this + " midway");
			
			unbindService(this);
		}
		
		super.onStop();
		
		//Log.d(StaticInfo.APP_TAG, "onStop " + this + " end");
	}
	
	public void onServiceConnected(ComponentName name, IBinder b) {
		//Log.d(StaticInfo.APP_TAG, "onServiceConnected begin");
		
		ServerConnectionService.ServiceBinder binder = (ServiceBinder) b;
		
		service = binder.getService();
		
		long connId = getIntent().getLongExtra(StaticInfo.EXTRA_CONN_ID, -1);
		connection = service.getConnections().get(connId);
		
		getCompatActionBar().setTitle( connection.getSettingsItem().getDisplayName() );
		getCompatActionBar().setSubtitle( connection.getSettingsItem().getDisplayAddress() );
		
		connection.addOnCurrentWindowChangeListener(this);
		connection.addOnWindowListListener(this);
		onWindowListChanged(connection);
		
		int windowIndex = getIntent().getIntExtra(StaticInfo.EXTRA_CONN_WINDOW, -1);
		if(!hasSavedInstanceState && windowIndex >= 0) {
			connection.setCurrentWindowIndex(windowIndex);
		}
		
		fragmentPager.setCurrentItem(connection.getCurrentWindowIndex(), false);
		
		notifyWindowsAvailable();
		
		//Log.d(StaticInfo.APP_TAG, "onServiceConnected end");
	}

	public void onServiceDisconnected(ComponentName name) {
		this.service = null;
	}
	
	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		
		if(fragment instanceof WindowFragment) {
			WindowFragment windowFragment = (WindowFragment) fragment;
			windowFragments.add(new WeakReference<WindowFragment>(windowFragment));
		}
	}
	
	private void notifyWindowsAvailable() {
		for(WeakReference<WindowFragment> ref : windowFragments) {
			WindowFragment frag = ref.get();
			if(frag != null) frag.onWindowsAvailable(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chat_menu, menu);
		
		return true;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(DeviceInfo.isHoneycomb(true) && item.getItemId() == android.R.id.home) {
			
			Intent intent = new Intent(getApplicationContext(), ServerListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			
			startActivity(intent);
			
			return true;
		}
		
		switch(item.getItemId()) {
			case R.id.menu_disconnect: {
				connection.disconnect();
				
				return true;
			}
		}
		
		return super.onOptionsItemSelected(item);
	}

	public void onCurrentWindowChanged(ServerConnection connection, final int index,
			int oldindex) {
		if(index == oldindex) return;
		
		//Log.d(StaticInfo.APP_TAG, "ChatActivity: Current window changed from " + oldindex + " to " + index);
		
		runOnUiThread(new Runnable() {
			public void run() {
				fragmentPager.setCurrentItem(index, true);
			}
		});
		
	}
	
	public void onPageScrollStateChanged(int arg0) {} //Unused
	
	public void onPageScrolled(int arg0, float arg1, int arg2) {} //Unused

	public void onPageSelected(int index) {
		connection.setCurrentWindowIndex(index);
	}
	
	public void onWindowListChanged(final ServerConnection connection) {
		runOnUiThread(new Runnable() {
			public void run() {
				cachedWindowsList = new ArrayList<Window>( connection.getWindows() );
				pagerAdapter.notifyDataSetChanged();
			}
		});
	}
	
	private List<Window> cachedWindowsList;
	
	public class WindowPagerAdapter extends FragmentStatePagerAdapter {
		public WindowPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public int getCount() {
			return cachedWindowsList != null ? cachedWindowsList.size() : 0;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return cachedWindowsList.get(position).getTitle();
		}

		@Override
		public Fragment getItem(int position) {
			Bundle bundle = new Bundle();
			bundle.putInt(WINDOW_KEY, position);
			
			WindowFragment f = (WindowFragment) WindowFragment.instantiate(getApplicationContext(),
					WindowFragment.class.getName(), bundle);
			
			return f;
		}
		
		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
	}

	public static class WindowFragment extends Fragment {
		private int windowPosition;
		private Window window;
		
		private ListView outputList;
		private OutputLinesAdapter outputAdapter;
		
		private ImageButton sendButton;
		
		private EditText inputField;
		private boolean ctrlPressed = false;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			//Log.d(StaticInfo.APP_TAG, toString() + " onCreate begin " + savedInstanceState);
			
			super.onCreate(savedInstanceState);
			
			//setRetainInstance(true);
			outputAdapter = new OutputLinesAdapter();
			
			ChatActivity chatActivity = (ChatActivity) getActivity();
			
			Bundle args = getArguments();
			if(args != null) {
				windowPosition = args.getInt(WINDOW_KEY);
				
				if(chatActivity.cachedWindowsList != null) { //This is true if this is an ordinary case - fragment is created AFTER binding
					window = chatActivity.cachedWindowsList.get(windowPosition);
					window.addOnOutputListener(outputAdapter);
				}
			}
			
			//Log.d(StaticInfo.APP_TAG, toString() + " onCreate end " + savedInstanceState);
		}

		public void onWindowsAvailable(ChatActivity activity) { //This will only be called when fragment has been recreated by the system
			window = activity.connection.getWindows().get(windowPosition);
			window.addOnOutputListener(outputAdapter);
			outputAdapter.notifyDataSetChanged();
		}

		@Override
		public void onDestroy() {
			//Log.d(StaticInfo.APP_TAG, toString() + " onDestroy begin");
			
			window.removeOnOutputListener(outputAdapter);
			window = null;
			
			super.onDestroy();
			
			//Log.d(StaticInfo.APP_TAG, toString() + " onDestroy end");
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.chat_fragment_layout, container, false);
			
			outputList = (ListView) view.findViewById(R.id.output_list);
			
			outputList.setAdapter(outputAdapter);
			
			sendButton = (ImageButton) view.findViewById(R.id.send_btn);
			
			sendButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					onSend(!ctrlPressed);
				}
			});
			
			inputField = (EditText) view.findViewById(R.id.input_field);
			
			inputField.setOnKeyListener(new OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					ctrlPressed = Util.ctrlPressed(event);
					
					if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
						onSend(!ctrlPressed);
						return true;
					}
					if(Util.ctrlPressed(event) && event.getAction() == KeyEvent.ACTION_DOWN) {
						if(keyCode == KeyEvent.KEYCODE_B) inputField.append(Colors.BOLD);
						if(keyCode == KeyEvent.KEYCODE_U) inputField.append(Colors.UNDERLINE);
						if(keyCode == KeyEvent.KEYCODE_O) inputField.append(Colors.NORMAL);
						if(keyCode == KeyEvent.KEYCODE_I) inputField.append("\u001d"); //Italic
						if(keyCode == KeyEvent.KEYCODE_K) inputField.append("\u0003"); //Color
					}
					
					return false;
				}
			});
			
			inputField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if(actionId == EditorInfo.IME_ACTION_SEND) {
						sendButton.performClick();
						return true;
					}
					
					return false;
				}
			});
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
			inputField.setInputType(InputType.TYPE_CLASS_TEXT | 
					(prefs.getBoolean("auto_correct", true)?InputType.TYPE_TEXT_FLAG_AUTO_CORRECT:0) |
					(prefs.getBoolean("auto_capitalize", true)?InputType.TYPE_TEXT_FLAG_CAP_SENTENCES:0)
							);
			
			return view;
		}
		
		private void onSend(boolean runCommands) {
			String input = inputField.getText().toString();
			if(input.length() == 0) return;
			
			inputField.setText("");
			
			if(runCommands && input.startsWith("/")) {
				String[] cmdParams = input.substring(1).split(" ", 2);
				if(!CommandManager.getInstance().executeCommand(window.getConnection(), cmdParams[0],
						(cmdParams.length > 1) ? cmdParams[1] : "")) {
					window.output(Colors.RED + "There is no command called \"" + cmdParams[0] + "\"!"); //TODO Externalize
				}
			}
			else {
				try {
					window.sendMessage(input);
				} catch (InvalidWindowTypeException e) {
					window.output(Colors.RED + "You can't send messages to this window!"); //TODO Externalize
				}
			}
		}
		
		public String toString() {
			return "WindowFragment (" + hashCode() + ") [Window #" + windowPosition + " " + (window != null ? window.getTitle() : "NONE") + "]";
		}
		
		public class OutputLinesAdapter extends BaseAdapter implements Window.OnOutputListener {
			public int getCount() {
				return window != null ? window.getLines().size() : 0;
			}

			public OutputLine getItem(int position) {
				return window.getLines().get(position);
			}

			public long getItemId(int position) {
				return -1;
			}

			public View getView(int position, View convertView, ViewGroup parent) {
				if(convertView == null) {
					convertView = getActivity().getLayoutInflater().inflate(R.layout.chat_line, parent, false);
				}
				
				TextView mainText = (TextView) convertView.findViewById(R.id.message_text);
				mainText.setText( getItem(position).getOutput(), BufferType.SPANNABLE );
				
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
				TextView timestampText = (TextView) convertView.findViewById(R.id.timestamp_text);
				timestampText.setText( getItem(position).timestamp() );
				
				if(!prefs.getBoolean("enable_timestamp", true))
				{
					timestampText.setVisibility(View.GONE);
				}
				
				mainText.setMovementMethod(LinkMovementMethod.getInstance());
				return convertView;
			}

			public void onOutputLineAdded(Window window, OutputLine line) {
				notifyDataSetChanged();
			}

			public void onOutputCleared(Window window) {
				notifyDataSetChanged();
			}
		}
	}
}
