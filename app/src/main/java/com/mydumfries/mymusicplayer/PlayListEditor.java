package com.mydumfries.mymusicplayer;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayListEditor extends ListActivity {
    EventDataSQLHelper mydb;
    static final int SEARCH_DIALOG_ID = 0;
    static final int SORT_DIALOG_ID = 1;
    static final int ARTIST_DIALOG_ID = 3;
    static final int ALBUM_DIALOG_ID = 4;
    String cancelsearchterm;
    String cancelsortorder;
    List<String> albumsList;
    List<String> artistsList;
    List<String> playlistList;
    int source;
    ArrayList<HashMap<String, String>> SongArray;
    EventDataSQLHelper songData;
    String searchterm;
    String sortorder;
    List<String> playedlist;
    EditText listname;
    String playlist;
    String pathtosdcard = Environment.getExternalStorageDirectory().getPath();
    String sdpath;
    private Button CreateListbutton;
    public static final String PHONELOCATION = "PhoneLocation";
    String folder;
    public static final String SETTINGS = "Settings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlisteditor);
        mydb = new EventDataSQLHelper(this);
        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                Context.MODE_PRIVATE);
        folder=Settings.getString(PHONELOCATION, "ERROR");
        sdpath = folder;
        if (folder.equals("ERROR")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PlayListEditor.this);
            builder.setMessage("Please use \n\"Location of Phone Music\"\nfirst to tell me were your DataBase file is located.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            Intent intent=new Intent(PlayListEditor.this,SettingsActivity.class);
            startActivity(intent);
            PlayListEditor.this.finish();
        }
        CreateListbutton = (Button) findViewById(R.id.button_createplaylist);
        listname = (EditText) findViewById(R.id.PlaylistName);
        playlistList = new ArrayList<String>();
        CreateListbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CreatePlayList();
            }
        });
        source = getIntent().getIntExtra("source", 1);
        playlist = getIntent().getStringExtra("playlist");
        songData = new EventDataSQLHelper(this);
        SongArray = new ArrayList<HashMap<String, String>>();
//        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("songs.db", null);
        SQLiteDatabase db=mydb.getWritableDatabase();
        searchterm = null;
        sortorder = "title ASC";
        playedlist = new ArrayList<String>();
        if (playlist != null) {
            Cursor cursor2 = db.query("'" + playlist + "'", null, null, null,
                    null, null, null);
            while (cursor2.moveToNext()) {
                playedlist.add(cursor2.getString(0));
            }
            listname = (EditText) findViewById(R.id.PlaylistName);
            listname.setText(playlist);
        }
        DisplayData(searchterm, sortorder, 0);
    }

    public void DisplayData(String search, final String sortorder, int pos) {
        Cursor cursor = getSongs(search, sortorder);
        SongArray.clear();
        while (cursor.moveToNext()) {
            final String mFileName2;
            String id2 = cursor.getString(0);
            mFileName2 = cursor.getString(1);
            String title = cursor.getString(2);
            final String artist = cursor.getString(3);
            final String album = cursor.getString(4);
            final String rating = cursor.getString(5);
            HashMap<String, String> song = new HashMap<String, String>();
            song.put("_id", id2);
            song.put("file", mFileName2);
            song.put("title", title);
            song.put("artist", artist);
            song.put("album", album);
            String stars = "";
            if (!rating.isEmpty()) {
                stars = "";
                int in = Integer.valueOf(rating);
                for (int x = 0; x < in; x++) {
                    stars += "*";
                }
            }
            if (playedlist.contains(id2)) {
                song.put("display", title + "\n" + artist + "\t | " + album
                        + "\t | " + stars + " | **SELECTED**");
                SongArray.add(song);
            } else if (pos >= 0) {
                song.put("display", title + "\n" + artist + "\t | " + album
                        + "\t | " + stars);
                SongArray.add(song);
            }
        }
        ListAdapter adapter = new SimpleAdapter(this, SongArray,
                R.layout.playlist_item, new String[]{"display"},
                new int[]{R.id.titletext});
        setListAdapter(adapter);

        // selecting single ListView item
        ListView lv = getListView();
        registerForContextMenu(lv);
        lv.setSelection(pos);
        // listening to single listitem click
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting listitem index
                HashMap<String, String> song = new HashMap<String, String>();
                song = SongArray.get(position);
                String id2 = song.get("_id");
                if (playedlist.contains(id2)) {
                    playedlist.remove(id2);
                } else {
                    playedlist.add(id2);
                }
                DisplayData(searchterm, sortorder, position);
            }
        });
    }

    private Cursor getSongs(String search, String sortorder) {
//        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("songs.db", null);
        SQLiteDatabase db=mydb.getWritableDatabase();
        String search1 = null;
        String search2 = null;
        Cursor cursor = null;
        if (search != null && !search.contains("LIKE")
                && !search.contains("Rating") && !search.contains("inner join")) {
            String[] separated = search.split("=");
            search1 = separated[0] + "=?";
            search2 = separated[1];
            if (source == 1)
                cursor = db.query(EventDataSQLHelper.TABLE, null, search1,
                        new String[]{search2}, null, null, sortorder);
            if (source == 2)
                cursor = db.query(EventDataSQLHelper.TABLE2, null, search1,
                        new String[]{search2}, null, null, sortorder);
        } else if (search != null && search.contains("inner join")) {
            cursor = db.rawQuery(search, null);
        } else {
            if (source == 1)
                cursor = db.query(EventDataSQLHelper.TABLE, null, search, null,
                        null, null, sortorder);
            if (source == 2)
                cursor = db.query(EventDataSQLHelper.TABLE2, null, search,
                        null, null, null, sortorder);
        }
        startManagingCursor(cursor);
        return cursor;
    }

    void CreatePlayList() {
//        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("songs.db", null);
        SQLiteDatabase db=mydb.getWritableDatabase();
        ContentValues values = new ContentValues();
        String table = listname.getText().toString();
        values.put(EventDataSQLHelper.NAME, table);
        values.put(EventDataSQLHelper.SOURCE, source);
        if (playlist == null)
            db.insert(EventDataSQLHelper.TABLE4, null, values);
        if (playlist != null) {
            db.execSQL("DROP TABLE IF EXISTS '" + table + "'");
        }
        String sql = "create table if not exists '" + table + "' ( "
                + "playitem integer default 0);";
        db.execSQL(sql);
        for (String item : playedlist) {
            ContentValues values2 = new ContentValues();
            values2.put("playitem", item);
            db.insert("'" + table + "'", null, values2);
        }
        PlayListEditor.this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.playlisteditormenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.search2_menu_item:
                showDialog(SEARCH_DIALOG_ID);
                return true;
            case R.id.selected_menu_item:
                sortorder = "title ASC";
                DisplayData(searchterm, sortorder, -1);
                return true;
            case R.id.displayall_menu_item:
                searchterm = null;
                sortorder = "title ASC";
                DisplayData(searchterm, sortorder, 0);
                return true;
            case R.id.addall_menu_item:
                AddAllToPlaylist();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SEARCH_DIALOG_ID:
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout = inflater.inflate(R.layout.search_dialog,
                        (ViewGroup) findViewById(R.id.root));
                final EditText search = (EditText) layout
                        .findViewById(R.id.EditText_IP);

                // ... other required overrides do nothing
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(layout);
                // Now configure the AlertDialog
                builder.setTitle("Search (Leave Blank for All Songs)");
                builder.setMessage("Enter Search Term");
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                PlayListEditor.this.removeDialog(SEARCH_DIALOG_ID);
                            }
                        });
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                searchterm = search.getText().toString();
                                searchterm = "title LIKE '%" + searchterm
                                        + "%' OR artist LIKE '%" + searchterm
                                        + "%' OR album LIKE '%" + searchterm + "%'";
                                sortorder = "title ASC";
                                DisplayData(searchterm, sortorder, 0);
                                PlayListEditor.this.removeDialog(SEARCH_DIALOG_ID);
                            }
                        });

                // Create the AlertDialog and return it
                AlertDialog searchDialog = builder.create();
                return searchDialog;

            case SORT_DIALOG_ID:
                LayoutInflater inflater2 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout2 = inflater2.inflate(R.layout.sort_dialog,
                        (ViewGroup) findViewById(R.id.root));

                // ... other required overrides do nothing
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setView(layout2);
                final Spinner spinner = (Spinner) layout2
                        .findViewById(R.id.sortorder_spinner);
                ArrayAdapter<CharSequence> adapter2 = new ArrayAdapter<CharSequence>(
                        this, android.R.layout.simple_spinner_item);
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                adapter2.add("title DESC");
                adapter2.add("title ASC");
                adapter2.add("artist DESC");
                adapter2.add("artist ASC");
                adapter2.add("album DESC");
                adapter2.add("album ASC");
                adapter2.add("rating ASC");
                adapter2.add("rating DESC");
                spinner.setAdapter(adapter2);
                spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
                // Now configure the AlertDialog
                builder2.setTitle("Sort Order");
                builder2.setMessage("Choose Your Sort Order");
                builder2.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                PlayListEditor.this.removeDialog(SORT_DIALOG_ID);
                            }
                        });
                builder2.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                DisplayData(searchterm, sortorder, 0);
                                PlayListEditor.this.removeDialog(SORT_DIALOG_ID);
                            }
                        });

                // Create the AlertDialog and return it
                AlertDialog sortDialog = builder2.create();
                return sortDialog;

            case ARTIST_DIALOG_ID:
                LayoutInflater inflater4 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout4 = inflater4.inflate(R.layout.sort_dialog,
                        (ViewGroup) findViewById(R.id.root));

                // ... other required overrides do nothing
                AlertDialog.Builder builder4 = new AlertDialog.Builder(this);
                builder4.setView(layout4);
                cancelsearchterm = searchterm;
                cancelsortorder = sortorder;
                final Spinner spinner4 = (Spinner) layout4
                        .findViewById(R.id.sortorder_spinner);
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                        this, android.R.layout.simple_spinner_dropdown_item,
                        artistsList);
                spinner4.setAdapter(spinnerArrayAdapter);
                spinner4.setOnItemSelectedListener(new MyOnItemSelectedListener2());
                // Now configure the AlertDialog
                builder4.setTitle("Select Artist");
                builder4.setMessage("Select Artist to Play");
                builder4.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                searchterm = cancelsearchterm;
                                sortorder = cancelsortorder;
                                DisplayData(searchterm, sortorder, 0);
                                PlayListEditor.this.removeDialog(ARTIST_DIALOG_ID);
                            }
                        });
                builder4.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                DisplayData(searchterm, sortorder, 0);
                                PlayListEditor.this.removeDialog(ARTIST_DIALOG_ID);
                            }
                        });

                // Create the AlertDialog and return it
                AlertDialog artistDialog = builder4.create();
                return artistDialog;

            case ALBUM_DIALOG_ID:
                LayoutInflater inflater5 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout5 = inflater5.inflate(R.layout.sort_dialog,
                        (ViewGroup) findViewById(R.id.root));

                // ... other required overrides do nothing
                AlertDialog.Builder builder5 = new AlertDialog.Builder(this);
                builder5.setView(layout5);
                cancelsearchterm = searchterm;
                cancelsortorder = sortorder;
                final Spinner spinner5 = (Spinner) layout5
                        .findViewById(R.id.sortorder_spinner);
                ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(
                        this, android.R.layout.simple_spinner_dropdown_item,
                        albumsList);
                spinner5.setAdapter(spinnerArrayAdapter2);
                spinner5.setOnItemSelectedListener(new MyOnItemSelectedListener3());
                // Now configure the AlertDialog
                builder5.setTitle("Select Album");
                builder5.setMessage("Select Album to Play");
                builder5.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                searchterm = cancelsearchterm;
                                sortorder = cancelsortorder;
                                DisplayData(searchterm, sortorder, 0);
                                PlayListEditor.this.removeDialog(ALBUM_DIALOG_ID);
                            }
                        });
                builder5.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                DisplayData(searchterm, sortorder, 0);
                                PlayListEditor.this.removeDialog(ALBUM_DIALOG_ID);
                            }
                        });

                // Create the AlertDialog and return it
                AlertDialog albumDialog = builder5.create();
                return albumDialog;
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
            case SEARCH_DIALOG_ID:
                return;
            case SORT_DIALOG_ID:
                return;

            case ARTIST_DIALOG_ID:
                return;
            case ALBUM_DIALOG_ID:
                return;
        }
    }

    void AddAllToPlaylist() {
//        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("songs.db", null);
        SQLiteDatabase db=mydb.getWritableDatabase();
        Cursor cursor = null;
        cursor = db.query(EventDataSQLHelper.TABLE4, null, null, null, null,
                null, null);
        startManagingCursor(cursor);
        HashMap<String, String> song = new HashMap<String, String>();
        for (int x = 0; x < SongArray.size(); x++) {
            song = SongArray.get(x);
            String id2 = song.get("_id");
            if (playedlist.contains(id2)) {
                playedlist.remove(id2);
            } else {
                playedlist.add(id2);
            }
        }
        DisplayData(searchterm, sortorder, 0);
    }

    public class MyOnItemSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            try {
                searchterm = null;
                sortorder = parent.getItemAtPosition(pos).toString();
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class MyOnItemSelectedListener2 implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            try {
                searchterm = "Artist="
                        + parent.getItemAtPosition(pos).toString();
                sortorder = null;
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class MyOnItemSelectedListener3 implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            try {
                searchterm = "Album="
                        + parent.getItemAtPosition(pos).toString();
                sortorder = null;
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }
}