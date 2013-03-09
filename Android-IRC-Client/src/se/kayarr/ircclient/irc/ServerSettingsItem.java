package se.kayarr.ircclient.irc;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public class ServerSettingsItem implements Serializable, Parcelable {
	private static final long serialVersionUID = -9187385204145219989L;
	
	@Getter private long id = -1;
	@Getter @Setter private String name;
	@Getter @Setter private String address;
	@Getter @Setter private int port;
	
	@Getter private UserInfo userInfo = new UserInfo();
	
	public ServerSettingsItem(long id, String name, String address, int port) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.port = port;
	}
	
	public ServerSettingsItem(Parcel src) {
		readFromParcel(src);
	}
	
	public ServerSettingsItem() {
	}

	public String getDisplayName() {
		return (name != null) ? name : address + ":" + port;
	}
	
	@Data @Accessors(chain=true)
	public static class Builder {
		private int id = -1;
		private String name = "Unnamed Server";
		private String address = "";
		private int port = 6667;
		private String nick;
		private String userName;
		private String realName;
		private String quitMessage;
		
		public Builder setUserInfo(UserInfo info) {
			this.nick = info.getNick();
			this.userName = info.getUserName();
			this.realName = info.getRealName();
			this.quitMessage = info.getQuitMessage();
			return this;
		}
		
		public ServerSettingsItem build() {
			ServerSettingsItem item = new ServerSettingsItem(id, name, address, port);
			item.userInfo = new UserInfo(nick, userName, realName, quitMessage);
			return item;
		}
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeString(address);
		dest.writeInt(port);
	}
	
	public void readFromParcel(Parcel src) {
		id = src.readLong();
		name = src.readString();
		address = src.readString();
		port = src.readInt();
	}
	
	public static final Parcelable.Creator<ServerSettingsItem> CREATOR = new Creator<ServerSettingsItem>() {
		
		public ServerSettingsItem createFromParcel(Parcel source) {
			return new ServerSettingsItem(source);
		}
		
		public ServerSettingsItem[] newArray(int size) {
			return new ServerSettingsItem[size];
		}
	};
}
