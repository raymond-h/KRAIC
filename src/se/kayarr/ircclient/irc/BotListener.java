package se.kayarr.ircclient.irc;

import java.lang.reflect.Method;

import org.pircbotx.Channel;
import org.pircbotx.ReplyConstants;
import org.pircbotx.UserSnapshot;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.events.ServerResponseEvent;
import org.pircbotx.hooks.events.TopicEvent;

import se.kayarr.ircclient.irc.commands.Command;
import se.kayarr.ircclient.irc.output.ActionLine;
import se.kayarr.ircclient.irc.output.JoinLine;
import se.kayarr.ircclient.irc.output.MessageLine;
import se.kayarr.ircclient.irc.output.NickChangeLine;
import se.kayarr.ircclient.irc.output.NoticeLine;
import se.kayarr.ircclient.irc.output.PartLine;
import se.kayarr.ircclient.irc.output.QuitLine;
import se.kayarr.ircclient.irc.output.TopicLine;
import se.kayarr.ircclient.services.ServerConnectionService;
import android.util.Log;

public class BotListener extends ListenerAdapter<Bot> {
	
	public static final String TAG = BotListener.class.getName();
	
	private ServerConnectionService context;
	private ServerConnection connection;
	
	public BotListener(ServerConnectionService context, ServerConnection conn) {
		this.context = context;
		this.connection = conn;
	}
	
	@Override
	public void onEvent(final Event<Bot> event) {
		try {
			super.onEvent(event);
		}
		catch (Exception e) {
			Log.e(TAG, "An error has occured!", e);
		}
		
		try {
			CommandManager manager = CommandManager.getInstance();
			
			for(Command command : manager.getCommands()) {
				for(Method m : command.getClass().getMethods()) {
					Class<?>[] params = m.getParameterTypes();
					if(params.length == 1 && params[0] == event.getClass()) {
						m.invoke(command, event);
						break;
					}
				}
			}
		}
		catch(Exception e) {
			Log.e(TAG, "Exception when delegating event", e);
		}
	}
	
	@Override
	public void onConnect(ConnectEvent<Bot> event) throws Exception {
		connection.updateInfo("Connected as " + event.getBot().getNick());
		
		super.onConnect(event);
	}

	@Override
	public void onServerResponse(ServerResponseEvent<Bot> event) throws Exception {
		if(event.getCode() == ReplyConstants.RPL_MOTD)
			connection.getStatusWindow().output(
					event.getResponse().substring(event.getResponse().indexOf(":")+1)
					);
		
		super.onServerResponse(event);
	}

	@Override
	public void onTopic(TopicEvent<Bot> event) throws Exception {
		connection.output(event.getChannel().getName(), new TopicLine(context, event));
		
		super.onTopic(event);
	}

	@Override
	public void onJoin(JoinEvent<Bot> event) throws Exception {
		Window joinedChannel = connection.getWindowIgnoreCase(event.getChannel().getName());
		if(joinedChannel == null) {
			joinedChannel = connection.createWindow(event.getChannel().getName(), event.getChannel());
		}
		
		joinedChannel.output(new JoinLine(context, event));
		
		super.onJoin(event);
	}

	@Override
	public void onNickChange(NickChangeEvent<Bot> event) throws Exception {
		Log.d(TAG, "Got event " + event);
		Log.d(TAG, "User is " + event.getUser());
		
		for(Channel channel : event.getUser().getChannels())
			connection.output(channel.getName(), new NickChangeLine(context, event));
		
		Window pmWindow = connection.getWindow(event.getOldNick());
		if(pmWindow != null) {
			pmWindow.setTitle(event.getNewNick());
		}
		
		super.onNickChange(event);
	}

	@Override
	public void onPart(PartEvent<Bot> event) throws Exception {
		Window window = connection.getWindowIgnoreCase(event.getChannel().getName());
		if(window != null) window.output(new PartLine(context, event));
		
		super.onPart(event);
	}

	@Override
	public void onQuit(QuitEvent<Bot> event) throws Exception {
		UserSnapshot user = event.getUser();
		for(Channel channel : user.getChannels()) {
			Window window = connection.getWindowIgnoreCase(channel.getName(), false);
			
			if(window != null) //This should be guaranteed
				window.output(new QuitLine(context, event));
			
			//This is a WTF error if it ever happens
			else {
				Log.e(TAG, "Tried outputting QuitLine to channel without corresponding window");
				Log.e(TAG, "^ user: " + user.getNick() + ", channel: " + channel.getName());
			}
		}
		
		super.onQuit(event);
	}

	@Override
	public void onNotice(NoticeEvent<Bot> event) throws Exception {
		if(event.getChannel() != null) {
			connection.output(event.getChannel().getName(), new NoticeLine(context, event));
		}
		else {
			connection.getCurrentWindow().output(new NoticeLine(context, event));
		}
		
		super.onNotice(event);
	}

	@Override
	public void onMessage(MessageEvent<Bot> event) throws Exception {
		connection.output(event.getChannel().getName(), new MessageLine(context, event));
				
		super.onMessage(event);
	}
	
	@Override
	public void onPrivateMessage(PrivateMessageEvent<Bot> event) throws Exception {
		Window window = connection.getWindowIgnoreCase(event.getUser().getNick());
		if(window == null) {
			window = connection.createWindow(event.getUser().getNick(), event.getUser());
		}
		
		window.output(new MessageLine(context, event));
		
		super.onPrivateMessage(event);
	}

	@Override
	public void onAction(ActionEvent<Bot> event) throws Exception {
		if(event.getChannel() != null) { //Sent from channel
			connection.output(event.getChannel().getName(), new ActionLine(context, event));
		}
		
		else { //Sent as a PM
			Window window = connection.getWindowIgnoreCase(event.getUser().getNick());
			if(window == null) {
				window = connection.createWindow(event.getUser().getNick(), event.getUser());
			}
			
			window.output(new ActionLine(context, event));
		}
		
		super.onAction(event);
	}
}
