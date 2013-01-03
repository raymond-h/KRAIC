package se.kayarr.ircclient.shared;

import java.util.regex.Pattern;

public class StaticInfo {
	static final public String APP_TAG = "Kayarr-Android-IRC-Client";
	static final public String DEBUG_TAG = "KRAIC_DEBUG";
	
	static final public int SERVICE_NOTIFICATION_ID = 7558;
	
	static final public String EXTRA_CONN_ID = "se.kayarrr.ircclient.extra_connection_id";
	
	static final public int MSG_BINDED = 1;
	static final public int MSG_UNBINDED = 2;
	static final public int MSG_ESTABLISHED_CONN = 3;
	static final public int MSG_KILL_SERVICE = 4;
	
	static final public int MSG_CONNECT_TO = 5;
	static final public int MSG_DISCONNECT_ALL = 6;
	
	static final public int MSG_UPDATE_SERVER_LIST = 7;
	
	static final public int MSG_REQUEST_CONN = 9;
	static final public int MSG_REPLY_WITH_CONN = 10;
	
	static final public int MSG_CHANGED_CHANNEL = 11;
	
	static final public int MSG_WINDOWS_MODIFIED = 12;
	
	static final public Pattern URI_PATTERN = Pattern.compile("(\\b[a-z\\+\\-\\.]+://[!#$&-;=?-\\[\\]_a-z~]+)");
}
