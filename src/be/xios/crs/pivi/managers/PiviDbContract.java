package be.xios.crs.pivi.managers;

import android.provider.BaseColumns;

public class PiviDbContract {

	public static final String DB_NAME = "PIVI_ULTIMATE_SEXY_DB";
	public static final int DB_VERSION = 1;
	
	protected static final class XmppMessagesTable implements BaseColumns{
		public static final String TABLE_NAME = "XmppMessagesTable";
		public static final String COL_SENDER = "sender";
		public static final String COL_MESSAGE = "message";
		public static final String COL_DATE = "date";
		public static final String COL_ROOM = "room";
		public static final String COL_SERVER = "server";
	}
	
}
