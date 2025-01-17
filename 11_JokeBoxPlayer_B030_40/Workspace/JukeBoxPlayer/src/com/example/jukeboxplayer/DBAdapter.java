package com.example.jukeboxplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;
	private final Context context;

	public static final String KEY_ROWID = "_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_PATH = "path";
	private static final String TAG = "DBAdapter";

	private static final String DATABASE_NAME = "SongDB";
	private static final String DATABASE_TABLE = "songs";
	private static final int DATABASE_VERSION = 4;

	private static final String DATABASE_CREATE = "create table songs (_id integer primary key autoincrement, "
			+ "title text not null, path text not null);";
	
	// Constructor
	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	// To create and upgrade a database in an Android application SQLiteOpenHelper subclass is usually created 
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// onCreate() is called by the framework, if the database does not exist
			Log.d("Create", "Creating the database");
			
			try {
				db.execSQL(DATABASE_CREATE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Sends a Warn log message
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			
			// Method to execute an SQL statement directly
			db.execSQL("DROP TABLE IF EXISTS songs");
			onCreate(db);
		}
	}
 
	// Opens the database
	public DBAdapter open() throws SQLException {
		// Create and/or open a database that will be used for reading and writing		
		db = DBHelper.getWritableDatabase();
		return this;
	}

	// Closes the database
	public void close() {
		// Closes the database
		DBHelper.close();
	}

	// Insert a song into the database
	public long addSong(String title, String path) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TITLE, title);
		initialValues.put(KEY_PATH, path);
		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	// Deletes a particular song
	public boolean deleteSong(long rowId) {
		return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	// Retrieves all the songs
	public Cursor getAllSongs() {
		return db.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_TITLE,
				KEY_PATH }, null, null, null, null, null);
	}

	// Retrieves a particular song
	public Cursor getSong(long rowId) throws SQLException {
		Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_TITLE, KEY_PATH}, KEY_ROWID + "=" + rowId,
				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	//Returns path of a particular song
	public String getFilePath(long rowId){
		this.open();
		Cursor c = this.getSong(rowId);
		String filePath = c.getString(2) ;
		this.close();

		return filePath;
	}
	
	//Returns title of a particular song
	public String getSongTitle(long rowId){
		this.open();
		Cursor c = this.getSong(rowId);
		String title = c.getString(1) ;
		this.close();

		return title;
	}
	
	//Returns the number of songs stored in database
	public int getNumberofSongs(){
		this.open();
		Cursor c = getAllSongs();
		int size = c.getCount();
		this.close();
		return size;
	}

	// Updates a song
	public boolean updateSong(long rowId, String title, String path) {
		// This methods returns the number of rows affected by the conducted operation
		ContentValues args = new ContentValues();
		args.put(KEY_TITLE, title);
		args.put(KEY_PATH, path);
		return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
}
