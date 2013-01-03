package se.kayarr.ircclient.irc.output;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.pircbotx.hooks.events.TopicEvent;

import se.kayarr.ircclient.shared.Util;
import android.content.Context;
import android.text.SpannableStringBuilder;

public class TopicLine extends OutputLine {
	private String channel;
	private String topic;
	private String setter;
	private Date setTime;
	private boolean justSet = false;
	
	public TopicLine(Context context, TopicEvent<?> event) {
		super( context, event.getTimestamp() );
		
		channel = event.getChannel().getName();
		topic = event.getTopic();
		setter = event.getUser().getNick();
		setTime = new Date( event.getDate() );
		justSet = event.isChanged();
	}

	@Override
	protected CharSequence outputString() { //TODO Make it use a global format
		SpannableStringBuilder output = new SpannableStringBuilder(super.outputString());
		
		if(!justSet)
			output.append("The topic for " + channel + " is \"").append(Util.parseForSpans(getContext(), topic))
					.append("\", was set by " + setter + " at " + SimpleDateFormat.getInstance().format(setTime));
		
		else
			output.append(setter + " changed the topic in " + channel + " to \"")
					.append(Util.parseForSpans(getContext(), topic)).append("\"");
		
		return output;
	}
}
