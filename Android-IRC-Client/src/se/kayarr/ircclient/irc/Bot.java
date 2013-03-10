package se.kayarr.ircclient.irc;

import lombok.Getter;
import lombok.Setter;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.managers.BackgroundListenerManager;

import se.kayarr.ircclient.irc.Window.Type;
import se.kayarr.ircclient.irc.output.ActionLine;
import se.kayarr.ircclient.irc.output.MessageLine;
import se.kayarr.ircclient.irc.output.NoticeLine;
import se.kayarr.ircclient.shared.StaticInfo;
import se.kayarr.ircclient.shared.Util;
import android.util.Log;

public class Bot extends PircBotX {
	private ServerConnection connection;
	
	public ServerConnection getConnectionContext() {
		return connection;
	}
	
	@Getter @Setter private String realName = "";
	
	public String getActualVersion() {
		return version;
	}
	
	public void setActualVersion(String version) {
		this.version = version;
	}
	
	/**
	 * This method is overrided to supply the real name instead, since PircBotX uses the return value as
	 * the real name of the client when connecting. To get the actual version string, use {@link #getActualVersion()}.
	 * 
	 * @return The real name of the client
	 */
	@Override
	public String getVersion() { //Because PircBotX puts its version as its real name when connecting, which is undesired
		return getRealName();
	}
	
	@SuppressWarnings("unchecked")
	public Bot(ServerConnection conn) {
		super();
		
		connection = conn;
		setVersion(
				Util.getApplicationLabel(connection.getContext()) +
				" v" + Util.getVersionName(connection.getContext()));
		setLogin("KRIRC");
		setRealName("Wollapolladollalolla");
		setFinger("What's the point of this again?");
		setMessageDelay(0);
		
		setVerbose(true);
		
		BackgroundListenerManager manager = new BackgroundListenerManager();
		manager.addListener(new OwnCoreHooks());
		manager.addListener(new BotListener(conn.getContext(), conn), true);
		setListenerManager(manager);
	}
	
	public Channel getChannelCaseInsensitive(String name) {
        if (name == null)
            throw new NullPointerException("Can't get a null channel");
	    for (Channel curChan : userChanInfo.getAValues())
	            if (curChan.getName().equalsIgnoreCase(name))
	                    return curChan;
	    
	    return null;
	}
	
	public boolean isProperChannel(String name) {
		if(name.length() <= 1)
			return false; //Neither an empty line nor a single letter (including prefix) can be a channel
		
		String channelPrefixes = "#&"; //TODO This is temporary, make it use server info when that's properly added
		if(channelPrefixes.contains(name.substring(0, 1))) return true;
		return false;
	}
	
	public String fixChannelName(String name) {
		if(!isProperChannel(name)) return "#" + name;
		return name;
	}
	
	@Override
	public void sendMessage(String target, String message) {
		super.sendMessage(target, message);
		
		if(isConnected()) {
			Window w = connection.getWindowIgnoreCase(target, false);
			if(w != null) {
				w.output(new MessageLine(connection.getContext(),
						((w.getType() == Type.CHANNEL) ? name : null), connection.getBot().getNick(), message));
			}
		}
	}

	@Override
	public void sendAction(String target, String action) {
		super.sendAction(target, action);
		
		if(isConnected()) {
			Window w = connection.getWindowIgnoreCase(target, false);
			if(w != null) {
				w.output(new ActionLine(connection.getContext(),
						((w.getType() == Type.CHANNEL) ? name : null), connection.getBot().getNick(), action));
			}
		}
	}

	@Override
	public void sendNotice(String target, String notice) {
		super.sendNotice(target, notice);
		
		if(isConnected()) {
			NoticeLine line = new NoticeLine(connection.getContext(),
					target, isProperChannel(target) ? target : null, notice);
			
			for(Window window : connection.getWindows())
				window.output(line);
		}
	}

	@Override
	public void log(String line) {
		if(isVerbose()) {
			Log.v(StaticInfo.APP_TAG, line);
		}
	}
}
