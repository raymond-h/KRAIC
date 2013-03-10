package se.kayarr.ircclient.shared;

import java.util.ArrayList;
import java.util.List;

import se.kayarr.ircclient.irc.ServerSettingsItem;
import se.kayarr.ircclient.irc.UserInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.SparseIntArray;

public class SettingsDatabaseHelper extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "settings.db";
	public static final int VERSION = 2;

	public SettingsDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		ServerItemsTable.onCreate(db);
		ColorsTable.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		ServerItemsTable.onUpgrade(db, oldVersion, newVersion);
		ColorsTable.onUpgrade(db, oldVersion, newVersion);
	}
	
	public ServerItemsTable serverItems() {
		return new ServerItemsTable(this);
	}
	
	public ColorsTable colors() {
		return new ColorsTable(this);
	}
	
	public static class ServerItemsTable {
		public static final String NAME = "server_items";
		
		public static final String COLUMN_ID = "_ID";
		public static final String COLUMN_SERVER_NAME = "server_name";
		public static final String COLUMN_SERVER_ADDRESS = "server_address";
		public static final String COLUMN_SERVER_PORT = "server_port";
		
		private static void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + NAME + " (" +
		
					COLUMN_ID +				" INTEGER PRIMARY KEY AUTOINCREMENT," +
					COLUMN_SERVER_NAME +	" TEXT," +
					COLUMN_SERVER_ADDRESS +	" TEXT," +
					COLUMN_SERVER_PORT +	" INTEGER" +
					
					")");
			
			ContentValues values = new ContentValues();
			
			values.put(COLUMN_SERVER_NAME, "EsperNet #1");
			values.put(COLUMN_SERVER_ADDRESS, "irc.esper.net");
			values.put(COLUMN_SERVER_PORT, 6667);
			db.insert(NAME, null, values);
			
			values.put(COLUMN_SERVER_NAME, "EsperNet #2");
			values.put(COLUMN_SERVER_ADDRESS, "irc.esper.net");
			values.put(COLUMN_SERVER_PORT, 6668);
			db.insert(NAME, null, values);
		}
		
		private static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
		
		private SettingsDatabaseHelper dbHelper;
		
		private ServerItemsTable(SettingsDatabaseHelper dbHelper) {
			this.dbHelper = dbHelper;
		}
		
		public ServerSettingsItem addServer(String name, String address, int port, UserInfo userInfo) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(COLUMN_SERVER_NAME, name);
			values.put(COLUMN_SERVER_ADDRESS, address);
			values.put(COLUMN_SERVER_PORT, port);
			
			if(userInfo != null) {
				//TODO Handle user info for this server here
			}
			
			long id = db.insert(NAME, null, values);
			
			//Log.d(StaticInfo.APP_TAG, "Inserted " + id + " database");
			
			return new ServerSettingsItem(id, name, address, port);
		}
		
		public ServerSettingsItem addServer(ServerSettingsItem item) {
			return addServer(item.getName(), item.getAddress(), item.getPort(), item.getUserInfo());
		}
		
		public void removeServer(ServerSettingsItem item) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.delete(NAME, COLUMN_ID +  "=?", new String[] { Long.toString(item.getId()) });
			
			//Log.d(StaticInfo.APP_TAG, "Deleted " + item.getId() + " from database");
		}
		
		public void updateServer(ServerSettingsItem item) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			
			ContentValues values = new ContentValues();
			values.put(COLUMN_SERVER_NAME, item.getName());
			values.put(COLUMN_SERVER_ADDRESS, item.getAddress());
			values.put(COLUMN_SERVER_PORT, item.getPort());
			
			db.update(NAME, values, COLUMN_ID +  "=?", new String[] { Long.toString(item.getId()) });
			
			//Log.d(StaticInfo.APP_TAG, "Updated database for ID " + item.getId() + " (" + affected + " affected)");
		}
		
		public List<ServerSettingsItem> getAllServers() {
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			
			Cursor dataCursor = db.query(NAME,
					new String[] { COLUMN_ID, COLUMN_SERVER_NAME, COLUMN_SERVER_ADDRESS, COLUMN_SERVER_PORT },
					null, null, null, null, null);
			
			List<ServerSettingsItem> servers = new ArrayList<ServerSettingsItem>();
			
			if(dataCursor.moveToFirst()) {
				do {
					ServerSettingsItem item = new ServerSettingsItem(
							dataCursor.getLong(0), dataCursor.getString(1),
							dataCursor.getString(2), dataCursor.getInt(3) );
					
					servers.add(item);
				}
				while(dataCursor.moveToNext());
			}
			
			//Log.d(StaticInfo.APP_TAG, "Fetched " + servers.size() + " server items");
			
			return servers;
		}
	}
	
	public static class ColorsTable {
		public static final String NAME = "colors";
		
		public static final String COLUMN_ID = "_ID";
		public static final String COLUMN_COLOR_NUM = "color_num";
		public static final String COLUMN_COLOR = "color";
		
		private static void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + NAME + " (" +
					
					COLUMN_ID +				" INTEGER PRIMARY KEY AUTOINCREMENT," +
					COLUMN_COLOR_NUM +		" INTEGER," +
					COLUMN_COLOR +			" INTEGER" +
					
					")");
			
			int colors[] = {
					Color.WHITE,
					Color.BLACK,
					Color.rgb(0, 0, 128),
					Color.rgb(0, 128, 0),
					Color.RED,
					Color.rgb(128, 0, 0),
					Color.rgb(128, 0, 128),
					Color.rgb(255, 128, 0),
					Color.YELLOW,
					Color.GREEN,
					Color.rgb(0, 128, 128),
					Color.CYAN,
					Color.BLUE,
					Color.rgb(255, 64, 255),
					Color.DKGRAY,
					Color.LTGRAY
			};
			
			ContentValues values = new ContentValues();
			for(int i = 0; i < colors.length; i++) {
				values.put(COLUMN_COLOR_NUM, i);
				values.put(COLUMN_COLOR, colors[i]);
				
				db.insert(NAME, null, values);
			}
		}
		
		private static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if(oldVersion < 2 && newVersion >= 2) {
				//Upgraded from a color table-less DB ver. to one where it is present
				//So we simply make a fresh table!
				onCreate(db);
			}
		}
		
		private SettingsDatabaseHelper dbHelper;
		
		private ColorsTable(SettingsDatabaseHelper dbHelper) {
			this.dbHelper = dbHelper;
		}
		
		public SparseIntArray getColors() {
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			
			Cursor dataCursor = db.query(NAME,
					new String[] { COLUMN_COLOR_NUM, COLUMN_COLOR },
					null, null, null, null, null);
			
			SparseIntArray array = new SparseIntArray();
			
			if(dataCursor.moveToFirst()) {
				do {
					array.append(dataCursor.getInt(0), dataCursor.getInt(1));
				}
				while(dataCursor.moveToNext());
			}
			
			return array;
		}
	}
}
