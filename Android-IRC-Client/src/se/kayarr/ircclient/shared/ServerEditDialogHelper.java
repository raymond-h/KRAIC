package se.kayarr.ircclient.shared;

import se.kayarr.ircclient.R;
import se.kayarr.ircclient.irc.ServerSettingsItem;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;

public class ServerEditDialogHelper {
	
	public static final String ARGUMENT_SERVER_ITEM = "se.kayarr.ircclient.args_server_item";
	
	public static interface OnServerItemEditedListener {
		
		public void onServerItemEdited(ServerSettingsItem settingsItem, boolean added);
		
		public void onServerItemEditCancel(ServerSettingsItem settingsItem);
	}

	public static AlertDialog createDialog(Activity activity, final ServerSettingsItem serverItem, String title,
			final OnServerItemEditedListener l) {
		
		final boolean added = (serverItem == null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		final View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_add_server, null);
		
		final EditText serverNameEdit = (EditText) dialogView.findViewById(R.id.addserver_server_name);
		final EditText addressEdit = (EditText) dialogView.findViewById(R.id.addserver_address);
		final EditText portEdit = (EditText) dialogView.findViewById(R.id.addserver_port);
		
		portEdit.setFilters( new InputFilter[] { new NumberRangeInputFilter(0, 65535) } );
		
		if(!added) {
			serverNameEdit.setText(serverItem.getName());
			addressEdit.setText(serverItem.getAddress());
			portEdit.setText("" + serverItem.getPort());
		}
		
		builder
				.setTitle(title)
				.setView(dialogView)
				
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						l.onServerItemEditCancel(serverItem);
					}
				})
				
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						
						String name = serverNameEdit.getText().toString();
						String address = addressEdit.getText().toString();
						int port = Integer.parseInt(portEdit.getText().toString(), 10);
						
						ServerSettingsItem item;
						
						if(added) item = new ServerSettingsItem();
						else item = serverItem;
						
						item.setName(name);
						item.setAddress(address);
						item.setPort(port);
						
						l.onServerItemEdited(item, added);
						
						dialog.dismiss();
					}
				});
		
		return builder.create();
	}
}
