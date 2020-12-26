package com.mydumfries.mymusicplayer;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class PlayListActivity extends ListActivity {
    EventDataSQLHelper mydb;
    public static final String SETTINGS = "Settings";
    public static final String PCLOCATION = "PCLocation";
    public static final String PHONELOCATION = "PhoneLocation";
    public static final String USER = "User";
    public static final String PASSWORD = "Password";
    public static final String IP = "IP";
    // Songs list
    EventDataSQLHelper listData;
    Button newPlaylist;
    Button newPCPlaylist;
    Button newItunesPlaylist;
    ArrayList<HashMap<String, String>> listArray;
    int listIndex;
    String pathtosdcard = Environment.getExternalStorageDirectory().getPath();
    String sdpath;
    String folder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlistviewer);
        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                Context.MODE_PRIVATE);
        folder=Settings.getString(PHONELOCATION, "ERROR");
        sdpath = folder;
        mydb = new EventDataSQLHelper(this);
        if (folder.equals("ERROR")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PlayListActivity.this);
            builder.setMessage("Please use \n\"Location of Phone Music\"\nfirst to tell me were your Music is located.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            Intent intent=new Intent(PlayListActivity.this,SettingsActivity.class);
            startActivity(intent);
            PlayListActivity.this.finish();
        }
        newPlaylist = (Button) findViewById(R.id.button_createplaylist);
        newPlaylist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent player = new Intent(PlayListActivity.this,
                        PlayListEditor.class);
                player.putExtra("source", 1);
                startActivity(player);
            }
        });
        newPCPlaylist = (Button) findViewById(R.id.button_createpcplaylist);
        newPCPlaylist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences Settings = getSharedPreferences(SETTINGS,
                        Context.MODE_PRIVATE);
                String folder = Settings.getString(PCLOCATION, "ERROR");
                if (folder.equals("ERROR")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PlayListActivity.this);
                    builder.setMessage("Please use \n\"Location of Network Music\"\nfirst to tell me were your Music is located.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do things
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    Intent player = new Intent(PlayListActivity.this,
                            PlayListEditor.class);
                    player.putExtra("source", 2);
                    startActivity(player);
                }
            }
        });
        newItunesPlaylist = (Button) findViewById(R.id.button_createItunesplaylist);
        newItunesPlaylist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                importfromitunes();
            }
        });
        listData = new EventDataSQLHelper(this);
        listArray = new ArrayList<HashMap<String, String>>();
//        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("songs.db", null);
        SQLiteDatabase db=mydb.getWritableDatabase();
        String sql = "create table if not exists " + EventDataSQLHelper.TABLE4
                + "( " + EventDataSQLHelper.NAME + " text, "
                + EventDataSQLHelper.SOURCE + " integer default 1);";
        db.execSQL(sql);
        DisplayData(null, null);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Playlist Menu");
        menu.add(0, v.getId(), 0, "Delete Playlist");
        menu.add(0, v.getId(), 0, "Edit Playlist");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
                .getMenuInfo();
        if (item.getTitle().equals("Delete Playlist")) {
            DeletePlaylist(menuInfo.position);
        }
        if (item.getTitle().equals("Edit Playlist")) {
            EditPlaylist(menuInfo.position);
        }
        return true;
    }

    public void DisplayData(String search, String sortorder) {
        final Cursor cursor = getSongs(search, sortorder);
        listArray.clear();
        HashMap<String, String> list = new HashMap<String, String>();
        list.put("file", "5 Star on Phone");
        list.put("display", "5 Star on Phone");
        listArray.add(list);
        HashMap<String, String> list2 = new HashMap<String, String>();
        list2.put("file", "5 Star on Desktop");
        list2.put("display", "5 Star on Desktop");
        listArray.add(list2);
        HashMap<String, String> list3 = new HashMap<String, String>();
        list3.put("file", "4 Star Plus on Phone");
        list3.put("display", "4 Star Plus on Phone");
        listArray.add(list3);
        HashMap<String, String> list4 = new HashMap<String, String>();
        list4.put("file", "4 Star Plus on Desktop");
        list4.put("display", "4 Star Plus on Desktop");
        listArray.add(list4);
        HashMap<String, String> list5 = new HashMap<String, String>();
        list5.put("file", "3 Star Plus on Phone");
        list5.put("display", "3 Star Plus on Phone");
        listArray.add(list5);
        HashMap<String, String> list6 = new HashMap<String, String>();
        list6.put("file", "3 Star Plus on Desktop");
        list6.put("display", "3 Star Plus on Desktop");
        listArray.add(list6);
        HashMap<String, String> list7 = new HashMap<String, String>();
        list7.put("file", "Unrated on Phone");
        list7.put("display", "Unrated on Phone");
        listArray.add(list7);
        HashMap<String, String> list8 = new HashMap<String, String>();
        list8.put("file", "Unrated on Desktop");
        list8.put("display", "Unrated on Desktop");
        listArray.add(list8);
        HashMap<String, String> list10 = new HashMap<String, String>();
        list10.put("file", "Not on Phone on Desktop");
        list10.put("display", "Not on Phone on Desktop");
        listArray.add(list10);
        while (cursor.moveToNext()) {
            final String mName2;
            mName2 = cursor.getString(0);
            String mSource=cursor.getString(1);
            String mDisplay=null;
            mDisplay=mName2+" on ";
            if (mSource.equals("1")) mDisplay=mDisplay+"Phone";
            if (mSource.equals("2")) mDisplay=mDisplay+"Desktop";
            HashMap<String, String> list9 = new HashMap<String, String>();
            list9.put("file", mName2);
            list9.put("display", mDisplay);
            listArray.add(list9);
        }
        ListAdapter adapter = new SimpleAdapter(this, listArray,
                R.layout.playlist_item, new String[]{"display"},
                new int[]{R.id.titletext});
        setListAdapter(adapter);

        // selecting single ListView item
        ListView lv = getListView();
        registerForContextMenu(lv);
        // listening to single listitem click
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting listitem index
                listIndex = position;
                HashMap<String, String> list = new HashMap<String, String>();
                list = listArray.get(listIndex);
                String mName2 = list.get("file");
                if (listIndex == 0) {
                    Intent player = new Intent(PlayListActivity.this,
                            PlayPhoneMusic.class);
                    player.putExtra("source", 1);
                    player.putExtra("searchterm", "Rating>=5");
                    startActivity(player);
                }
                if (listIndex == 1) {
                    Intent player = new Intent(PlayListActivity.this,
                            PlayPhoneMusic.class);
                    player.putExtra("source", 2);
                    player.putExtra("searchterm", "Rating>=5");
                    startActivity(player);
                }
                if (listIndex == 2) {
                    Intent player = new Intent(PlayListActivity.this,
                            PlayPhoneMusic.class);
                    player.putExtra("source", 1);
                    player.putExtra("searchterm", "Rating>=4");
                    startActivity(player);
                }
                if (listIndex == 3) {
                    Intent player = new Intent(PlayListActivity.this,
                            PlayPhoneMusic.class);
                    player.putExtra("source", 2);
                    player.putExtra("searchterm", "Rating>=4");
                    startActivity(player);
                }
                if (listIndex == 4) {
                    Intent player = new Intent(PlayListActivity.this,
                            PlayPhoneMusic.class);
                    player.putExtra("source", 1);
                    player.putExtra("searchterm", "Rating>=3");
                    startActivity(player);
                }
                if (listIndex == 5) {
                    Intent player = new Intent(PlayListActivity.this,
                            PlayPhoneMusic.class);
                    player.putExtra("source", 2);
                    player.putExtra("searchterm", "Rating>=3");
                    startActivity(player);
                }
                if (listIndex == 6) {
                    Intent player = new Intent(PlayListActivity.this,
                            PlayPhoneMusic.class);
                    player.putExtra("source", 1);
                    player.putExtra("searchterm", "Rating<1");
                    startActivity(player);
                }
                if (listIndex == 7) {
                    Intent player = new Intent(PlayListActivity.this,
                            PlayPhoneMusic.class);
                    player.putExtra("source", 2);
                    player.putExtra("searchterm", "Rating<1");
                    startActivity(player);
                }
                if (listIndex == 8)
                {
                    Intent player = new Intent(PlayListActivity.this,
                            PlayPhoneMusic.class);
                    player.putExtra("source", 4);
//                    player.putExtra("searchterm",
//                            "select * from pcsongs q inner join 'phonesongs' a on q.Title!=a.Title");
                    player.putExtra("searchterm","SELECT * FROM pcsongs WHERE Title NOT IN (SELECT Title FROM phonesongs)");
                    startActivity(player);
                }
                if (listIndex > 8) {
 //                   SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(
//                            "songs.db", null);
                    SQLiteDatabase db=mydb.getWritableDatabase();
                    int source = 0;
                    Cursor cursor2 = db.query(EventDataSQLHelper.TABLE4, null,
                            "name='" + mName2 + "'", null, null, null, null);
                    while (cursor2.moveToNext()) {
                        source = cursor2.getInt(1);
                    }
                    Intent player = new Intent(PlayListActivity.this,
                            PlayPhoneMusic.class);
                    player.putExtra("source", source + 2);
                    if (source == 1) {
                        player.putExtra("searchterm",
                                "select * from phonesongs q inner join '"
                                        + mName2 + "' a on q._id=a.playitem");
                    }
                    if (source == 2) {
                        player.putExtra("searchterm",
                                "select * from pcsongs q inner join '" + mName2
                                        + "' a on q._id=a.playitem");
                    }
                    startActivity(player);
                }
            }
        });
    }

    private Cursor getSongs(String search, String sortorder) {
 //       SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("songs.db", null);
        SQLiteDatabase db=mydb.getWritableDatabase();
        Cursor cursor = null;
        cursor = db.query(EventDataSQLHelper.TABLE4, null, null, null, null,
                null, null);
        startManagingCursor(cursor);
        return cursor;
    }

    void DeletePlaylist(int position) {
 //       SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("songs.db", null);
        SQLiteDatabase db=mydb.getWritableDatabase();
        HashMap<String, String> list = new HashMap<String, String>();
        list = listArray.get(position);
        String mName2 = list.get("file");
        String where = EventDataSQLHelper.NAME + "='" + mName2 + "'";
        db.delete(EventDataSQLHelper.TABLE4, where, null);
        db.execSQL("DROP TABLE IF EXISTS '" + mName2 + "'");
    }

    void EditPlaylist(int position) {
 //       SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("songs.db", null);
        SQLiteDatabase db=mydb.getWritableDatabase();
        HashMap<String, String> list = new HashMap<String, String>();
        list = listArray.get(position);
        String mName2 = list.get("file");
        String where = EventDataSQLHelper.NAME + "='" + mName2 + "'";
        Cursor cursor = db.query(EventDataSQLHelper.TABLE4, null, where, null,
                null, null, null);
        startManagingCursor(cursor);
        int mSource = 1;
        while (cursor.moveToNext()) {
            mSource = cursor.getInt(1);
        }
        Intent player = new Intent(PlayListActivity.this, PlayListEditor.class);
        player.putExtra("source", mSource);
        player.putExtra("playlist", mName2);
        startActivity(player);
    }

    private void processBooks(XmlPullParser tunes, String ItunesFileString)
            throws XmlPullParserException, IOException {
        //this code not used at the moment
        //but is handy for importing itunes playlists
        //so do not remove
//        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("songs.db", null);
        SQLiteDatabase db=mydb.getWritableDatabase();
        int eventType = tunes.getEventType();
        boolean bFoundScores = false;
        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                Context.MODE_PRIVATE);
        String pcfolder = Settings.getString(PCLOCATION, "ERROR");
        int len=pcfolder.length();
        len=len+22;
        ContentValues values3=new ContentValues();
        int playcount=0;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.TEXT) {
                String strName = tunes.getText();
                if (bFoundScores == true) {
                    String file2 = tunes.getText();
                    file2 = file2.substring(len);
                    file2 = java.net.URLDecoder.decode(file2, "UTF-8");
                    if (!file2.contains("'")) {
                        Cursor cursor2 = db.query(EventDataSQLHelper.TABLE2, null,
                                "File='" + file2 + "'", null, null, null, null);
                        int source = 0;
                        int playcount2=0;
                        while (cursor2.moveToNext()) {
                            source = cursor2.getInt(0);
                            playcount2=cursor2.getInt(6);
                        }
                        if (source != 0) {
                            ContentValues values2 = new ContentValues();
                            values2.put("playitem", source);
                            playcount=playcount+playcount2;
 //removing next line, but leaving rest of code as would be handy if need to re-load database.
//                            values3.put(EventDataSQLHelper.PLAYEDCOUNT,playcount);
                            db.insert("'" + ItunesFileString + "'", null, values2);
                            db.update(EventDataSQLHelper.TABLE2,values3,"_id= ?", new String[] {String.valueOf(source)});
                            values3.clear();
                        }
                    }
                    bFoundScores = false;
                }
                if (strName.equals("Rating"))
                {
                    String strName2="continue";
                    eventType = tunes.next();
                    while (strName2.equals("continue")) {
                        if (eventType == XmlPullParser.TEXT) {
                            String rating = null;
                            strName2 = tunes.getText();
                            int rating2=Integer.parseInt(strName2)/20;
                            rating=String.valueOf(rating2);
                            values3.put("Rating",rating);
                        }
                        eventType = tunes.next();
                    }
                }
                if (strName.equals("Play Count"))
                {
                    String strName2="continue";
                    eventType = tunes.next();
                    while (strName2.equals("continue")) {
                        if (eventType == XmlPullParser.TEXT) {
                            strName2 = tunes.getText();
                            playcount=Integer.parseInt(strName2);
                        }
                        eventType = tunes.next();
                    }
                }
                if (strName.equals("Location")) {
                    bFoundScores = true;
                }
            }
            eventType = tunes.next();
        }
        // Handle no scores available
        if (bFoundScores == false) {

        }
    }

    void importfromitunes() {
        LayoutInflater ITunesinflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View ITuneslayout = ITunesinflater.inflate(R.layout.ip_dialog,
                (ViewGroup) findViewById(R.id.root));
        final EditText ITunesFile = (EditText) ITuneslayout
                .findViewById(R.id.EditText_IP);

        // ... other required overrides do nothing
        AlertDialog.Builder ITunesbuilder = new AlertDialog.Builder(this);
        ITunesbuilder.setView(ITuneslayout);
        // Now configure the AlertDialog
        ITunesbuilder.setTitle("How To Import An Itunes Playlist");
        ITunesbuilder
                .setMessage("On Desktop Computer\n" +
                        "1. In ITunes, select the Playlist\n" +
                        "2. Select File-Library-Export Playlist\n" +
                        "3. Save the file as a .xml\n" +
                        "On The Phone\n" +
                        "1. Use ES File Explorer to move the saved file to '"+ folder + "'\n" +
                        "2. Type the name of the file in the box below (do not include the .xml)\n" +
                        "3. Press OK");
        ITunesbuilder.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                    }
                });
        ITunesbuilder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
//                        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(
//                                "songs.db", null);
                        SQLiteDatabase db=mydb.getWritableDatabase();
                        final String ItunesFileString = ITunesFile.getText().toString();
                        ContentValues values = new ContentValues();
                        values.put(EventDataSQLHelper.NAME, ItunesFileString);
                        values.put(EventDataSQLHelper.SOURCE, 2);
                        db.insert(EventDataSQLHelper.TABLE4, null, values);
                        String sql = "create table if not exists '" + ItunesFileString + "' ( "
                                + "playitem integer default 0);";
                        db.execSQL(sql);
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // do network action in this function
                                XmlPullParserFactory factory = null;
                                try {
                                    factory = XmlPullParserFactory.newInstance();
                                } catch (XmlPullParserException e) {
                                    // TODO Auto-generated catch block

                                }
                                factory.setNamespaceAware(true);
                                XmlPullParser xpp = null;
                                try {
                                    xpp = factory.newPullParser();
                                } catch (XmlPullParserException e) {
                                    // TODO Auto-generated catch block

                                }

                                File file = new File(sdpath + "/" + ItunesFileString + ".xml");
                                FileInputStream fis = null;
                                try {
                                    fis = new FileInputStream(file);
                                } catch (FileNotFoundException e1) {
                                    // TODO Auto-generated catch block

                                }
                                try {
                                    xpp.setInput(new InputStreamReader(fis));
                                } catch (XmlPullParserException e1) {
                                    // TODO Auto-generated catch block

                                }
                                try {
                                    processBooks(xpp, ItunesFileString);
                                } catch (XmlPullParserException e) {
                                    // TODO Auto-generated catch block

                                } catch (IOException e) {
                                    // TODO Auto-generated catch block

                                }
                            }
                        });
                        thread.start();
                    }
                });

        // Create the AlertDialog and return it
        AlertDialog ITunesDialog = ITunesbuilder.create();
        ITunesDialog.show();
    }
}