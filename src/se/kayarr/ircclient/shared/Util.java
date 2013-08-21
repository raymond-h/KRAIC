package se.kayarr.ircclient.shared;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pircbotx.Colors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;

public class Util {
	/**
	 * Formats a {@link Date} object using the global timestamp format used for chat lines.
	 * 
	 * @param date The {@link Date} object.
	 * @return The timestamp as a {@link String}
	 */
	public static String formatTimestamp(Context context, Date date) {
		return Settings.getInstance(context).getTimestampFormat().format(date);
	}
	
	/**
	 * Formats a millisecond value from January 1, 1970 GMT using the global timestamp format used for chat lines.
	 * 
	 * @param ms Milliseconds from January 1, 1970 GMT
	 * @return The timestamp as a {@link String}
	 */
	public static String formatTimestamp(Context context, long ms) {
		return formatTimestamp(context, new Date(ms));
	}
	
	/**
	 * Helper method to check if the Ctrl key on a keyboard (built-in or perhaps a USB one) is pressed.
	 * 
	 * @param event The {@link KeyEvent} to get the information from
	 * @return {@code true} if the Ctrl key is pressed, otherwise {@code false} (always returns {@code false} on pre-Honeycomb)
	 */
	@SuppressLint("NewApi")
	public static boolean ctrlPressed(KeyEvent event) {
		//Suppressing the warning is fine, as the method calls DeviceInfo.isHoneycomb() to
		//really make sure the device is running at least Honeycomb (API level 11)
		
		if(DeviceInfo.isHoneycomb(true)) {
			return event.isCtrlPressed();
		}
		return false;
	}
	
	private static PackageInfo ownPackageInfo;
	
	/**
	 * Helper method for getting the {@code PackageInfo} of this app, to get information
	 * such as the name and the textual version of it etc.
	 * 
	 * @param context The application context
	 * @return The {@code PackageInfo} instance for this app
	 */
	public static PackageInfo getOwnPackageInfo(Context context) {
		if(ownPackageInfo == null) {
			try {
				ownPackageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			}
			catch (NameNotFoundException e) {
				Log.e(StaticInfo.APP_TAG, "Unable to find my own package...?", e);
			}
		}
		
		return ownPackageInfo;
	}
	
	/**
	 * Helper method for getting the name of this app.
	 * 
	 * @param context The application context
	 * @return The name of the application
	 */
	public static String getApplicationLabel(Context context) {
		return context.getString(getOwnPackageInfo(context).applicationInfo.labelRes);
	}
	
	/**
	 * Helper method for getting the textual representation of the version of this app.
	 * 
	 * @param context The application context
	 * @return The textual version
	 */
	public static String getVersionName(Context context) {
		return getOwnPackageInfo(context).versionName;
	}
	
	/**
	 * Performs a check to see if {@code stringToCheck} matches {@code matchString},
	 * which can contain wildcards. (*)
	 * 
	 * @param matchString The string (optionally with wildcards) to match against
	 * @param stringToCheck The string to match
	 * @return Whether or not the string to check matches the match-string
	 */
	public static boolean matches(String matchString, String stringToCheck) {
		String[] tokens = matchString.split("\\*", -1);
		
		if(tokens.length == 0)
			return true; //The entire string was just a wildcard, which matches EVERYTHING
		
		if(tokens.length == 1)
			return tokens[0].equals(stringToCheck); //There was no wildcard at all, so let's just check if both strings are equal
		
		if(tokens[0].length() > 0 && !stringToCheck.startsWith(tokens[0]))
			return false; //The matchstring started with a word, and if the string to check doesn't start with that...
		
		if(tokens[tokens.length-1].length() > 0 && !stringToCheck.endsWith(tokens[tokens.length-1]))
			return false; //The matchstring ended with a word, and if the string to check doesn't end with that...
		
		for(String token : tokens) { //Do actual matching
			int pos = stringToCheck.indexOf(token);
			
			if(pos == -1) return false;
			
			stringToCheck = stringToCheck.substring(pos + token.length());
		}
		
		return true;
	}
	
	public static String removeFormattingIncludingItalic(String line) { //Yes, it's almost completely a copypaste.
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
                char ch = line.charAt(i);
                if (ch != '\u000f' && ch != '\u0002' && ch != '\u001f' && ch != '\u0016' && ch != '\u001d')
                    buffer.append(ch);
        }
        return buffer.toString(); //The original didn't real with mIRC's italic code. Deal with it.
	}
	
	/**
	 * Does exactly what {@link String#replaceAll(String, String)} does, except it works on all
	 * CharSequences and retains all spans if given a Spanned object.
	 * 
	 * @param regex The Regex pattern to use
	 * @param input The input CharSequence to match against
	 * @param replacement The CharSequence to replace matches with
	 * @return The final product
	 */
	public static CharSequence replaceAll(String regex, CharSequence input, CharSequence replacement) {
		SpannableStringBuilder output = new SpannableStringBuilder(input);
		Matcher matcher = Pattern.compile(regex.intern()).matcher(input);
		int dispos = 0;
		while(matcher.find()) {
			int oldLen = output.length();
			output.replace(matcher.start()-dispos, matcher.end()-dispos, replacement);
			dispos += oldLen - output.length();
		}
		return output;
	}
	
	public static CharSequence stripColorCodes(CharSequence input) {
		return replaceAll("\\u0003\\d{0,2}(?:,\\d{0,2})?", input, "");
	}
	
	public static CharSequence stripFormattingCodes(CharSequence input) {
		return replaceAll("\\u000f|\\u0002|\\u001f|\\u0016|\\u001d", input, "");
	}
	
	public static CharSequence stripAllControlCodes(CharSequence input) {
		return replaceAll("\\u000f|\\u0002|\\u001f|\\u0016|\\u001d|(?:\\u0003\\d{0,2}(?:,\\d{0,2})?)", input, "");
	}
	
	/**
	 * Turns a {@link String} into a rainbow'd version of it (using IRC control codes) :)
	 * 
	 * @param input The input string
	 * @return The resulting, colorful string
	 */
	public static String ircRainbowString(String input) {
		StringBuilder builder = new StringBuilder();
		String[] rainbowCodes =
			{ Colors.RED, Colors.OLIVE, Colors.YELLOW, Colors.GREEN, Colors.BLUE, Colors.DARK_BLUE, Colors.PURPLE };
		
		int colorIndex = 0;
		for(int i = 0; i < input.length(); i++) {
			if(!Character.isISOControl(input.charAt(i))) builder.append(rainbowCodes[colorIndex++]);
			builder.append(input.charAt(i));
			if(colorIndex >= rainbowCodes.length) colorIndex = 0;
		}
		
		return builder.toString();
	}
	
	/**
	 * Parses a {@link CharSequence} for IRC control codes such as Bold ({@code \u0002}), Underlined ({@code \u001f})
	 * and Colors ({@code \u0003}) and makes it into a {@link Spanned} object that can be used in the Android UI.
	 * 
	 * @param input The input text to parse
	 * @param colorMap The {@link SparseIntArray} with mappings from IRC colors to Android colors
	 * @return The resulting {@link Spanned} object
	 */
	public static Spanned ircCodesToSpanned(CharSequence input, SparseIntArray colorMap) {
		SpannableStringBuilder output = new SpannableStringBuilder( stripAllControlCodes(input) );
		
		int dispos = 0;
		int startBoldPos = -1;
		int startUnderlinePos = -1;
		int startItalicsPos = -1;
		int startFgColorPos = -1;
		int startBgColorPos = -1;
		
		int fgColor = -1;
		int bgColor = -1;
		
		for(int i = 0; i < input.length(); i++) {
			switch(input.charAt(i)) {
			case '\u0002':
				if(startBoldPos >= 0) {
					output.setSpan(new StyleSpan(Typeface.BOLD), startBoldPos, i-dispos, 0);
					startBoldPos = -1;
				}
				else startBoldPos = i-dispos;
				dispos++;
				break;
				
			case '\u001f':
				if(startUnderlinePos >= 0) {
					output.setSpan(new UnderlineSpan(), startUnderlinePos, i-dispos, 0);
					startUnderlinePos = -1;
				}
				else startUnderlinePos = i-dispos;
				dispos++;
				break;
				
			case '\u001d':
				if(startItalicsPos >= 0) {
					output.setSpan(new StyleSpan(Typeface.ITALIC), startItalicsPos, i-dispos, 0);
					startItalicsPos = -1;
				}
				else startItalicsPos = i-dispos;
				dispos++;
				break;
				
			case '\u0003': //TODO Make it use Regex instead?
				String fg = "";
				String bg = "";
				int startPos = i;
				int length = 0;
				
				try {
					for(i++; i < startPos+3; i++) {
						if(Character.isDigit(input.charAt(i))) {
							fg += input.charAt(i);
							length++;
						}
						else break;
					}
					if(input.charAt(i) == ',' && fg.length() > 0) {
						int newStart = i;
						length++;
						for(i++; i < newStart+3; i++) {
							if(Character.isDigit(input.charAt(i))) {
								bg += input.charAt(i);
								length++;
							}
							else break;
						}
					}
					
					i--;
				}
				catch(IndexOutOfBoundsException e) {
					Log.v(StaticInfo.APP_TAG, "Reached the end of the string, it seems!");
				}
				finally {
					//Incase of IndexOutOfBoundsException in charAt(), because we reached the end,
					//we just consider the control code parsing done and continue on
					//*
					if(fg.length() == 0) { //Kill all colors
						if(startFgColorPos >= 0) {
							if(fgColor >= 0 && colorMap.indexOfKey(fgColor) >= 0)
								output.setSpan(new ForegroundColorSpan(colorMap.get(fgColor)), startFgColorPos, startPos-dispos, 0);
							startFgColorPos = -1;
						}
						if(startBgColorPos >= 0) {
							if(bgColor >= 0 && colorMap.indexOfKey(bgColor) >= 0)
								output.setSpan(new BackgroundColorSpan(colorMap.get(bgColor)), startBgColorPos, startPos-dispos, 0);
							startBgColorPos = -1;
						}
					}
					else {
						if(startFgColorPos >= 0) {
							if(fgColor >= 0 && colorMap.indexOfKey(fgColor) >= 0)
								output.setSpan(new ForegroundColorSpan(colorMap.get(fgColor)),
										startFgColorPos, startPos-dispos, 0);
						}
						startFgColorPos = startPos-dispos;
						fgColor = Integer.parseInt(fg);
						
						if(bg.length() > 0) {
							if(startBgColorPos >= 0) {
								if(bgColor >= 0 && colorMap.indexOfKey(bgColor) >= 0)
									output.setSpan(new BackgroundColorSpan(colorMap.get(bgColor)),
											startBgColorPos, startPos-dispos, 0);
							}
							startBgColorPos = startPos-dispos;
							bgColor = Integer.parseInt(bg);
						}
					}//*/
				}
				dispos += 1+length;
				
				break;
				
			case '\u000f':
				if(startBoldPos >= 0) {
					output.setSpan(new StyleSpan(Typeface.BOLD), startBoldPos, i-dispos, 0);
					startBoldPos = -1;
				}
				if(startUnderlinePos >= 0) {
					output.setSpan(new UnderlineSpan(), startUnderlinePos, i-dispos, 0);
					startUnderlinePos = -1;
				}
				if(startItalicsPos >= 0) {
					output.setSpan(new StyleSpan(Typeface.ITALIC), startItalicsPos, i-dispos, 0);
					startItalicsPos = -1;
				}
				if(startFgColorPos >= 0) {
					if(fgColor >= 0 && colorMap.indexOfKey(fgColor) >= 0)
						output.setSpan(new ForegroundColorSpan(colorMap.get(fgColor)), startFgColorPos, i-dispos, 0);
					startFgColorPos = -1;
				}
				if(startBgColorPos >= 0) {
					if(bgColor >= 0 && colorMap.indexOfKey(bgColor) >= 0)
						output.setSpan(new BackgroundColorSpan(colorMap.get(bgColor)), startBgColorPos, i-dispos, 0);
					startBgColorPos = -1;
				}
				dispos++;
				break;
				
			default:
				break;
			}
		}
		
		if(startBoldPos >= 0)
			output.setSpan(new StyleSpan(Typeface.BOLD), startBoldPos, output.length(), 0);
		
		if(startUnderlinePos >= 0)
			output.setSpan(new UnderlineSpan(), startUnderlinePos, output.length(), 0);
		
		if(startItalicsPos >= 0)
			output.setSpan(new StyleSpan(Typeface.ITALIC), startItalicsPos, output.length(), 0);
		
		if(startFgColorPos >= 0 && fgColor >= 0 && colorMap.indexOfKey(fgColor) >= 0)
				output.setSpan(new ForegroundColorSpan(colorMap.get(fgColor)), startFgColorPos, output.length(), 0);
		
		if(startBgColorPos >= 0 && bgColor >= 0 && colorMap.indexOfKey(bgColor) >= 0)
				output.setSpan(new BackgroundColorSpan(colorMap.get(bgColor)), startBgColorPos, output.length(), 0);
		
		return output;
	}
	
	public static Spanned parseForSpans(CharSequence input, SparseIntArray colorMap) {
		Spannable output = new SpannableString(input);
		Linkify.addLinks(output, Linkify.ALL);
		
		/*
		URLSpan[] urlSpans = output.getSpans(0, output.length(), URLSpan.class);
		
		for(URLSpan span : urlSpans) {
			output.setSpan(new URLNoFormattingSpan(span.getURL()),
					output.getSpanStart(span), output.getSpanEnd(span), output.getSpanFlags(span));
			output.removeSpan(span);
		}
		//*/
		
		return ircCodesToSpanned(output, colorMap);
	}
	
	public static String toBold(String text) {
		return Colors.BOLD + text + Colors.BOLD;
	}
	
	public static String toUnderline(String text) {
		return Colors.UNDERLINE + text + Colors.UNDERLINE;
	}
	
	public static String toColor(String text, int fg) {
		if(fg < 0 || fg > 15) return text;
		
		return
				new StringBuilder().append('\3').append(fg)
				.append(text)
				.append('\3')
				.toString();
	}
	
	public static String toColor(String text, int fg, int bg) {
		if(bg < 0 || bg > 15) return toColor(text, fg);
		if(fg < 0 || fg > 15) return text;
		
		return
				new StringBuilder().append('\3').append(fg).append(",").append(bg)
				.append(text)
				.append('\3')
				.toString();
	}
}
