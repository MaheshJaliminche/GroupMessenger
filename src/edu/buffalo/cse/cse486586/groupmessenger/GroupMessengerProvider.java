package edu.buffalo.cse.cse486586.groupmessenger;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {

	private static final String DbName="GrpMessengerDB";
	private static final String TableName="GrpMessengerTable";
	private static final int version=1;
	SQLiteDatabase SQLitedb;
	DbClass dbHelper;
	SQLiteQueryBuilder QueryBuilder;
	
	public class DbClass extends SQLiteOpenHelper
	{
		private static final String SQL_CREATE = "CREATE TABLE " +
				TableName +
	            "(" +                           // The columns in the table
	            " key String PRIMARY KEY, " +
	            " value String"+ ")";
		
		
		
		/*private static final String SQL_CREATE =
				"CREATE TABLE GrpMessengerTable (key String PRIMARY KEY,value String)";*/
		
		public DbClass(Context context) {
			super(context, DbName, null, version);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			 db.execSQL(SQL_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }
    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
    	DbClass dbHelper= new DbClass(getContext());
    	SQLitedb= dbHelper.getWritableDatabase();
    	return(dbHelper==null)?false:true;
        //return false;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that I used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */
    	SQLitedb.insert(TableName, null, values);
        Log.v("insert", values.toString());
        return uri;
    }

   

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         * 
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         * 
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */
        QueryBuilder = new SQLiteQueryBuilder();
    	QueryBuilder.setTables(TableName);
    	String[] columns={"key","value"};
    	Cursor cursor = SQLitedb.query(TableName,columns,"key = "+"'"+selection+"'", null, null, null, null);
        Log.v("query", selection);
        return cursor;
    }

       
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }
}
