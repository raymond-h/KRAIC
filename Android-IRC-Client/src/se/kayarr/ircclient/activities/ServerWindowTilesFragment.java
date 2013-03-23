package se.kayarr.ircclient.activities;

import se.kayarr.ircclient.R;
import se.kayarr.ircclient.activities.ServerListActivity.WindowGridAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

public class ServerWindowTilesFragment extends Fragment implements OnItemClickListener {
	
	private GridView tileGridView;
	private TextView tileGridAreaText;
	private WindowGridAdapter gridAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View layout = inflater.inflate(R.layout.serverlist_tile_grid, container, false);
		
		tileGridView = (GridView) layout.findViewById(R.id.tile_grid_view);
		tileGridAreaText = (TextView) layout.findViewById(R.id.tile_grid_area_text);
		
		return layout;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public void onItemClick(AdapterView<?> parent, View child, int pos, long id) {
	}
}
