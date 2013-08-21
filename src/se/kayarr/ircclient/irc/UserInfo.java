package se.kayarr.ircclient.irc;

import lombok.AllArgsConstructor;
import lombok.Data;
import android.os.Parcel;
import android.os.Parcelable;

@Data @AllArgsConstructor(suppressConstructorProperties=true)
public class UserInfo implements Parcelable {
	public UserInfo() {}
	
	private String nick;
	private String userName;
	private String realName;
	
	private String quitMessage;
	
	public UserInfo(Parcel src) {
		readFromParcel(src);
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(nick);
		dest.writeString(userName);
		dest.writeString(realName);
		dest.writeString(quitMessage);
	}
	
	public void readFromParcel(Parcel src) {
		nick = src.readString();
		userName = src.readString();
		realName = src.readString();
		quitMessage = src.readString();
	}
	
	public static final Parcelable.Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
		
		public UserInfo createFromParcel(Parcel source) {
			return new UserInfo(source);
		}
		
		public UserInfo[] newArray(int size) {
			return new UserInfo[size];
		}
	};
}