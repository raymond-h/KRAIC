package se.kayarr.ircclient.activities;

import java.util.List;

import se.kayarr.ircclient.R;
import se.kayarr.ircclient.irc.ServerSettingsItem;
import se.kayarr.ircclient.irc.UserInfo;
import se.kayarr.ircclient.shared.DeviceInfo;
import se.kayarr.ircclient.shared.NumberRangeInputFilter;
import se.kayarr.ircclient.shared.Settings;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

@TargetApi(11)
public class SettingsActivity extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(!DeviceInfo.isHoneycomb(true)) {
			//TODO Pre-Honeycomb code goes here
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@TargetApi(11)
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.settings_headers, target);
	}
	
	@TargetApi(11)
	public static class SettingsFragment extends PreferenceFragment
			implements SharedPreferences.OnSharedPreferenceChangeListener {
		private AlertDialog currentDialog;
		private String settingsHeader;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			setHasOptionsMenu(true);
			
			SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
			prefs.edit()
				.clear()
				.putString("default_nickname", "NGJRSJIHRSH")
				.commit();
			
			settingsHeader = getArguments().getString("settings");
			
			if("general".equals(settingsHeader)) {
				addPreferencesFromResource(R.xml.settings_general);
			}
			else if("server_list".equals(settingsHeader)) {
				addPreferencesFromResource(R.xml.settings_serverlist);
				
				updateServerList();
			}
			
			//*
			Preference nickPref = findPreference("default_nickname");
			if(nickPref != null) {
				nickPref.setSummary(prefs.getString("default_nickname", ""));
			}
			//*/
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
		}
		
		@Override
		public void onResume() {
			super.onResume();
			
			getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
		}
		
		@Override
		public void onPause() {
			if(currentDialog != null) currentDialog.dismiss();
			
			getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
			
			super.onPause();
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			if("server_list".equals(settingsHeader))
				inflater.inflate(R.menu.settings_serverlist_menu, menu);
			
			super.onCreateOptionsMenu(menu, inflater);
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch(item.getItemId()) {
				case R.id.menu_serverlist_add: {
					ServerSettingsItem settingsItem = new ServerSettingsItem();
					showServerEditDialog(null, settingsItem, true);
					
					return true;
				}
			}
			
			return super.onOptionsItemSelected(item);
		}
		
		@Override
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
				Preference preference) {
			
			if(preference instanceof ServerItem) {
				ServerItem serverItem = (ServerItem) preference;
				showServerEditDialog(serverItem, serverItem.item, false);
				
				return true;
			}
			
			return super.onPreferenceTreeClick(preferenceScreen, preference);
		}

		public void updateServerList() {
			getPreferenceScreen().removeAll();
			
			for(ServerSettingsItem item : Settings.getInstance(getActivity()).getServerSettings()) {
				getPreferenceScreen().addPreference(new ServerItem(getActivity(), item));
			}
		}
		
		private void showServerEditDialog(final ServerItem preference,
				final ServerSettingsItem settingsItem, final boolean added) {
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			
			final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_server, null);
			
			final EditText serverNameEdit = (EditText) dialogView.findViewById(R.id.addserver_server_name);
			final EditText addressEdit = (EditText) dialogView.findViewById(R.id.addserver_address);
			final EditText portEdit = (EditText) dialogView.findViewById(R.id.addserver_port);
			
			portEdit.setFilters( new InputFilter[] { new NumberRangeInputFilter(0, 65535) } );
			
			serverNameEdit.setText(settingsItem.getName());
			addressEdit.setText(settingsItem.getAddress());
			portEdit.setText("" + settingsItem.getPort());
			
			currentDialog = builder
					.setCancelable(false)
					.setTitle(added ?
							R.string.addserver_dialog_title_add_server :
							R.string.addserver_dialog_title_edit_server)
					.setView(dialogView)
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							settingsItem.setName( serverNameEdit.getText().toString() );
							settingsItem.setAddress( addressEdit.getText().toString() );
							settingsItem.setPort( Integer.parseInt(portEdit.getText().toString(), 10) );
							
							if(!added) preference.update();
							else {
								ServerItem item = new ServerItem(getActivity(), settingsItem);
								getPreferenceScreen().addPreference(item);
								Settings.getInstance(getActivity()).getServerSettings().add(settingsItem);
							}
							
							dialog.dismiss();
						}
					})
					.show();
		}
		
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			
			Preference preference = findPreference(key);
			
			if(key.equals("default_nickname")) {
				UserInfo defaults = Settings.getInstance(getActivity()).getDefaultUserInfo();
				String defaultNick = sharedPreferences.getString(key, defaults.getNick());
				defaults.setNick( defaultNick );
				preference.setSummary(defaultNick);
			}
		}
	}
	
	public static class ServerItem extends Preference {
		private ServerSettingsItem item;
		
		public ServerItem(Context context, ServerSettingsItem item) {
			super(context);
			
			this.item = item;
			update();
		}

		public void update() {
			setTitle(item.getDisplayName());
			if(item.getName() != null) setSummary(item.getAddress() + ":" + item.getPort());
			else setSummary("");
		}
	}
}
