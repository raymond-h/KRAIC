package se.kayarr.ircclient.irc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import se.kayarr.ircclient.exceptions.InvalidWindowTypeException;
import se.kayarr.ircclient.irc.output.OutputLine;
import se.kayarr.ircclient.irc.output.SimpleStringLine;

public class Window {
	public static enum Type { STATUS, CHANNEL, USER };
	
	@Getter private ServerConnection connection;
	
	@Getter @Setter private String title = "";
	@Getter @Setter private Type type = Type.STATUS;
	@Getter private List<OutputLine> lines = new ArrayList<OutputLine>();
	private Set<OnOutputListener> outputCallbacks = new HashSet<OnOutputListener>();
	
	Window(ServerConnection connection, String title, Type type) {
		this.connection = connection;
		this.title = title;
		this.type = type;
	}
	
	public void output(final OutputLine line) {
		connection.getContext().runOnUiThread(new Runnable() {
			public void run() {
				lines.add(line);
				notifyOutputLineAdded(line);
			}
		});
	}
	
	public void output(String text) {
		output(new SimpleStringLine(connection.getContext(), text));
	}
	
	public void clearOutput() {
		connection.getContext().runOnUiThread(new Runnable() {
			public void run() {
				lines.clear();
				notifyOutputCleared();
			}
		});
	}
	
	public void sendMessage(String message) throws InvalidWindowTypeException {
		if(type == Type.CHANNEL || type == Type.USER) connection.getBot().sendMessage(title, message);
		else throw new InvalidWindowTypeException("Cannot send message to window of type " + type.name());
	}
	
	public void sendAction(String message) throws InvalidWindowTypeException {
		if(type == Type.CHANNEL || type == Type.USER) connection.getBot().sendAction(title, message);
		else throw new InvalidWindowTypeException("Cannot send action to window of type " + type.name());
	}
	
	public void sendNotice(String message) throws InvalidWindowTypeException {
		if(type == Type.CHANNEL || type == Type.USER) connection.getBot().sendNotice(title, message);
		else throw new InvalidWindowTypeException("Cannot send notice to window of type " + type.name());
	}
	
	public static interface OnOutputListener {
		public void onOutputLineAdded(OutputLine line);
		
		public void onOutputCleared();
	}
	
	public void addOnOutputListener(OnOutputListener l) {
		outputCallbacks.add(l);
	}
	
	public void removeOnOutputListener(OnOutputListener l) {
		outputCallbacks.remove(l);
	}
	
	private void notifyOutputLineAdded(OutputLine line) {
		for(OnOutputListener l : outputCallbacks) {
			l.onOutputLineAdded(line);
		}
	}
	
	private void notifyOutputCleared() {
		for(OnOutputListener l : outputCallbacks) {
			l.onOutputCleared();
		}
	}
}