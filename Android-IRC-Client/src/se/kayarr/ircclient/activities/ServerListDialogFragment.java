package se.kayarr.ircclient.activities;

import se.kayarr.ircclient.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ServerListDialogFragment extends DialogFragment {
	public static final String ARGUMENT_SERVER_NAMES = "se.kayarr.ircclient.args_server_names";
	
	public static interface OnServerListClickedListener {
		public void onServerListItemClicked(ServerListDialogFragment dialogFragment, int position);
	}

	private OnServerListClickedListener listItemClickedListener;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			listItemClickedListener = (OnServerListClickedListener) activity;
		}
		catch(ClassCastException e) {
			throw new ClassCastException(activity + " must implement ServerListDialogFragment.OnServerListClickedListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
		
		final String[] serverNames = getArguments().getStringArray(ARGUMENT_SERVER_NAMES);
		
		b
			.setTitle(R.string.serverlist_label)
			.setItems(serverNames, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					listItemClickedListener.onServerListItemClicked(ServerListDialogFragment.this, which);
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//User cancelled
				}
			});
		
		return b.create();
	}
}