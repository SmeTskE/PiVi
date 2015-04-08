package be.xios.crs.pivi.managers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.xios.crs.pivi.enums.ChatRooms;
import be.xios.crs.pivi.models.PiviXmppMessage;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.ParseException;
import android.util.Log;

public class DbManager extends SQLiteOpenHelper {

	public DbManager(Context context){
		super(context, PiviDbContract.DB_NAME, null, PiviDbContract.DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String create_messages_table = "CREATE TABLE " +
										PiviDbContract.XmppMessagesTable.TABLE_NAME +
										" ( " + PiviDbContract.XmppMessagesTable._ID
										+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
										+ PiviDbContract.XmppMessagesTable.COL_DATE + " TEXT, "
										+ PiviDbContract.XmppMessagesTable.COL_MESSAGE + " TEXT, "
										+ PiviDbContract.XmppMessagesTable.COL_SENDER + " TEXT, "
										+ PiviDbContract.XmppMessagesTable.COL_ROOM + " TEXT, " 
										+ PiviDbContract.XmppMessagesTable.COL_SERVER + " LONG)";
		db.execSQL(create_messages_table);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// LETS SKIP THIS ONE		
	}
	
	public void insertMessage(PiviXmppMessage message, long serverId){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(PiviDbContract.XmppMessagesTable.COL_DATE, message.getMessageSend().toString());
		cv.put(PiviDbContract.XmppMessagesTable.COL_MESSAGE, message.getMessage());
		cv.put(PiviDbContract.XmppMessagesTable.COL_ROOM, message.getChatRoom().toString());
		cv.put(PiviDbContract.XmppMessagesTable.COL_SENDER, message.getSender());
		cv.put(PiviDbContract.XmppMessagesTable.COL_SERVER, serverId);
		long rowId = db.insert(PiviDbContract.XmppMessagesTable.TABLE_NAME, null, cv);
		if (rowId < 0){
			Log.d("PIVI DATABASE", "COULD NOT INSERT MESSAGE INTO DB");
		}else{
			Log.d("PIVI DATABASE", "INSERTED NEW ROW IN DATABASE");
		}
	}
	
	public ArrayList<PiviXmppMessage> getAllMessageByRoom(ChatRooms room, long serverId){
		SQLiteDatabase db = getWritableDatabase();
		ArrayList<PiviXmppMessage> list  = new ArrayList<PiviXmppMessage>();
		Cursor cursor = db.query(PiviDbContract.XmppMessagesTable.TABLE_NAME, 
				null, ("room='" + room.toString() + "' AND server=" + serverId), null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			PiviXmppMessage msg = new PiviXmppMessage();
			msg.setChatRoom(ChatRooms.byString(cursor.getString(cursor.getColumnIndex(PiviDbContract.XmppMessagesTable.COL_ROOM))));
			msg.setId(cursor.getLong(cursor.getColumnIndex(PiviDbContract.XmppMessagesTable._ID)));
			msg.setMessage(cursor.getString(cursor.getColumnIndex(PiviDbContract.XmppMessagesTable.COL_MESSAGE)));
			msg.setSender(cursor.getString(cursor.getColumnIndex(PiviDbContract.XmppMessagesTable.COL_SENDER)));
			
			String dtStart = cursor.getString(cursor.getColumnIndex(PiviDbContract.XmppMessagesTable.COL_DATE));  
			SimpleDateFormat  format = new SimpleDateFormat();  
			Date dte = null;
			try {  
			    dte = format.parse(dtStart);  
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			msg.setMessageSend(dte);
			list.add(msg);
			cursor.moveToNext();
		}
		
		return list;
	}
	
	public void deleteAllFromServer(long serverId){
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("DELETE FROM " + PiviDbContract.XmppMessagesTable.TABLE_NAME + " WHERE " + PiviDbContract.XmppMessagesTable.COL_SERVER + "=" + serverId);
	}
}
