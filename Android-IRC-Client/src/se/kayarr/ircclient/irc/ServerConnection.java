package se.kayarr.ircclient.irc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;

import org.pircbotx.exception.NickAlreadyInUseException;

import se.kayarr.ircclient.irc.output.OutputLine;
import se.kayarr.ircclient.irc.output.SimpleStringLine;
import se.kayarr.ircclient.services.ServerConnectionService;
import se.kayarr.ircclient.shared.Settings;
import se.kayarr.ircclient.shared.StaticInfo;
import android.util.Log;

public class ServerConnection {
	@Getter private ServerConnectionService context;
	@Getter private long id;
	
	@Getter private Bot bot;
	@Getter private ServerSettingsItem settingsItem;
	@Getter private ServerConnectionStatus status;
	
	private List<Window> windows = new ArrayList<Window>();
	@Getter private Window statusWindow;
	@Getter private int currentWindowIndex = 0;
	
	public List<Window> getWindows() {
		return Collections.unmodifiableList(windows);
	}
	
	public String getDefaultNick() {
		String nick = settingsItem.getUserInfo().getNick();
		if(nick == null) nick = Settings.getInstance(context).getDefaultUserInfo().getNick();
		return nick;
	}
	
	public String getDefaultQuitMessage() {
		String quitMessage = settingsItem.getUserInfo().getQuitMessage();
		if(quitMessage == null) quitMessage = Settings.getInstance(context).getDefaultUserInfo().getQuitMessage();
		return quitMessage != null ? quitMessage : "";
	}

	public ServerConnection(ServerConnectionService context, long id, ServerSettingsItem settingsItem) {
		this.context = context;
		this.id = id;
		this.settingsItem = settingsItem;
		
		status = new ServerConnectionStatus(settingsItem.getDisplayName(), "");
		bot = new Bot(this);
		
		statusWindow = createWindow("Status", Window.Type.STATUS);
	}
	
	public void connect() {
		connect(false);
	}
	
	public void connectTo(ServerSettingsItem settingsItem) {
		this.settingsItem = settingsItem;
		connect(false);
	}
	
	public void connect(boolean reconnect) {
		if(isConnected()) disconnect();
		
		updateInfo("Connecting...");
		
		statusWindow.output("Connecting to " + settingsItem.getDisplayName() + "...");

		if(!reconnect) bot.setName(getDefaultNick());

		try {
			Log.d(StaticInfo.APP_TAG, "Connecting to " + settingsItem.getDisplayName());
			
			bot.connect(settingsItem.getAddress(), settingsItem.getPort());
			
			Log.d(StaticInfo.APP_TAG, "Connected, joining channel...");

			bot.joinChannel("#krprivate42");
		}
		catch(NickAlreadyInUseException e) {
			statusWindow.output("Error when connecting: nickname \""+bot.getNick()+"\" already in use.");
			updateInfo("Nick \""+bot.getNick()+"\" already in use");
		}
		catch(Exception e) {
			updateInfo(e.toString());
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		disconnect( getDefaultQuitMessage() );
	}
	
	public void disconnect(String quitMessage) {
		if(isConnected()) {
			String message = "Disconnected from " + settingsItem.getDisplayName() +
					((quitMessage != null && quitMessage.length() > 0) ? " ( " + quitMessage + " )" : "");
			outputAll(message);
			updateInfo(message);
			bot.quitServer(quitMessage);
		}
	}
	
	public boolean isConnected() {
		return bot.isConnected();
	}
	
	public Window getCurrentWindow() {
		return windows.get(currentWindowIndex);
	}
	
	public void setCurrentWindowIndex(int index) {
		setCurrentWindowIndex(index, true);
	}
	
	protected void setCurrentWindowIndex(int index, boolean triggerListeners) {
		if(index == currentWindowIndex) return;
		
		int oldindex = currentWindowIndex;
		currentWindowIndex = index;
		
		Log.d(StaticInfo.APP_TAG, "Current window changed from " + oldindex + " to " + index +
				", notifying: " + (triggerListeners ? "yes" : "no"));
		
		if(triggerListeners) {
			notifyCurrentWindowChanged(currentWindowIndex, oldindex);
		}
	}
	
	public void setCurrentWindow(Window currentWindow) {
		setCurrentWindow(currentWindow, true);
	}
	
	protected void setCurrentWindow(Window currentWindow, boolean triggerListeners) {
		if(currentWindow == null) throw new NullPointerException("Cannot set current window to null");
		if(currentWindow.getConnection() != this || !windows.contains(currentWindow)) return;
		
		setCurrentWindowIndex(windows.indexOf(currentWindow), triggerListeners);
	}
	
	public Window getWindow(String name) {
		for(Window w : windows) {
			if(w.getTitle().equals(name)) {
				return w;
			}
		}
		return null;
	}
	
	public Window getWindow(String name, boolean includeStatus) {
		for(Window w : windows) {
			if(w.getTitle().equals(name)) {
				if(!includeStatus && w.getType() == Window.Type.STATUS) continue;
				return w;
			}
		}
		return null;
	}
	
	public Window getWindowIgnoreCase(String name) {
		for(Window w : windows) {
			if(w.getTitle().equalsIgnoreCase(name)) {
				return w;
			}
		}
		return null;
	}
	
	public Window getWindowIgnoreCase(String name, boolean includeStatus) {
		for(Window w : windows) {
			if(w.getTitle().equalsIgnoreCase(name)) {
				if(!includeStatus && w.getType() == Window.Type.STATUS) continue;
				return w;
			}
		}
		return null;
	}
	
	public Window createWindow(String name, Window.Type type) {
		Window w = new Window(this, name, type);
		windows.add(w);
		notifyWindowListChanged();
		return w;
	}
	
	public void removeWindow(Window window) {
		if(window == statusWindow) return; //TODO When window is the status window, throw exception
		
		if(window == getCurrentWindow()) {
			int pos = windows.indexOf( window ) - 1;
			windows.remove(window);
			setCurrentWindow(windows.get(pos));
		}
		else windows.remove(window);
		
		notifyWindowListChanged();
	}
	
	public void output(String windowName, String line) {
		output(getWindowIgnoreCase(windowName, false), new SimpleStringLine(context, line));
	}
	
	public void output(Window window, String line) {
		output(window, new SimpleStringLine(context, line));
	}
	
	public void output(String windowName, OutputLine line) {
		output(getWindowIgnoreCase(windowName, false), line);
	}
	
	public void output(Window window, OutputLine line) {
		if(window != null) {
			window.output(line);
		}
		else throw new NullPointerException("Cannot output to null window");
	}
	
	public void outputAll(String line) {
		outputAll(new SimpleStringLine(context, line));
	}
	
	public void outputAll(OutputLine line) {
		for(Window window : windows) {
			window.output(line);
		}
	}
	
	public void updateInfo(String info) {
		status.setInfo(info);
		context.notifyInfoChanged(this);
	}
	
	private Set<OnWindowListListener> windowListCallbacks = new HashSet<OnWindowListListener>();
	
	public static interface OnWindowListListener {
		public void onWindowListChanged(ServerConnection connection);
	}
	
	public void addOnWindowListListener(OnWindowListListener l) {
		windowListCallbacks.add(l);
	}
	
	public void removeOnWindowListListener(OnWindowListListener l) {
		windowListCallbacks.remove(l);
	}
	
	private void notifyWindowListChanged() {
		for(OnWindowListListener l : windowListCallbacks) {
			l.onWindowListChanged(this);
		}
	}
	
	private Set<OnCurrentWindowChangeListener> currentWindowChangeCallbacks = new HashSet<OnCurrentWindowChangeListener>();
	
	public static interface OnCurrentWindowChangeListener {
		public void onCurrentWindowChanged(ServerConnection connection, int index, int oldindex);
	}
	
	public void addOnCurrentWindowChangeListener(OnCurrentWindowChangeListener l) {
		currentWindowChangeCallbacks.add(l);
	}
	
	public void removeOnCurrentWindowChangeListener(OnCurrentWindowChangeListener l) {
		currentWindowChangeCallbacks.remove(l);
	}
	
	private void notifyCurrentWindowChanged(int index, int oldindex) {
		for(OnCurrentWindowChangeListener l : currentWindowChangeCallbacks) {
			l.onCurrentWindowChanged(this, index, oldindex);
		}
	}
}
