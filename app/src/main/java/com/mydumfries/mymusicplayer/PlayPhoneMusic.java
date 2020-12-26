package com.mydumfries.mymusicplayer;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.hierynomus.smbj.auth.AuthenticationContext;

import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import ch.swaechter.smbjwrapper.SmbConnection;
import ch.swaechter.smbjwrapper.SmbDirectory;
import ch.swaechter.smbjwrapper.SmbFile;
import ch.swaechter.smbjwrapper.SmbItem;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class PlayPhoneMusic extends ListActivity implements
        OnCompletionListener, SeekBar.OnSeekBarChangeListener {
    ArrayList<Music> songs;
    EventDataSQLHelper mydb;
    boolean isLocationWriteable;
    public static final String SETTINGS = "Settings";
    public static final String LAST_PLAYED = "LastPlayed";
    public static final String LAST_PLAYED_ID = "LastPlayedID";
    public static final String ALARMYESNO = "AlarmYesNo";
    public static final String USER = "User";
    public static final String PASSWORD = "Password";
    public static final String PCLOCATION = "PCLocation";
    public static final String PHONELOCATION = "PhoneLocation";
    public static final String IP = "IP";
    public static final String SOURCE = "source";
    public static final String HOUR = "Hour";
    public static final String MINUTE = "Minute";
    public static final String SATURDAY = "Saturday";
    public static final String SUNDAY = "Sunday";
    public static final String MONDAY = "Monday";
    public static final String TUESDAY = "Tuesday";
    public static final String WEDNESDAY = "Wednesday";
    public static final String THURSDAY = "Thursday";
    public static final String FRIDAY = "Friday";
    public static final String PLAY_NEXT = "PlayNext";
    static final int SEARCH_DIALOG_ID = 0;
    static final int SORT_DIALOG_ID = 1;
    static final int SLEEP_DIALOG_ID = 2;
    static final int ARTIST_DIALOG_ID = 3;
    static final int ALBUM_DIALOG_ID = 4;
    static final int EDITTAGS_DIALOG_ID = 5;
    static final int ADDTOPLAYLIST_DIALOG_ID = 6;
    static final int EDITTAGS_DIALOG_ID2 = 7;
    static final int ADDTOPLAYLIST_DIALOG_ID2 = 8;
    static final int CHANGEIP = 9;
    private static final String SMB_FILE = "smbfile";
    private static final int FILE_SELECT_CODE = 0;
    AsyncTask<String, String, String> downloading;
    String itunes;
    //all references to skydrive are actually GoogleDrive due to change in cloud storage
    String skydrive = "net://StuartMclaren690@gdrive/iTunes Music/";
    String sdpath;
    String downloadresult = "0";
    List<String> albumsList;
    List<String> artistsList;
    List<String> playlistList;
    List<String> playedlist;
    String PlayLabel = null;
    String PlayLabel3 = null;
    String mName5;
    int computerfound = 1;
    int sleeptime = 0;
    int source = 1;
    String addtoplaylist;
    MediaPlayer mPlayer2 = new MediaPlayer();
    int songIndex;
    int selectedIndex;
    String mFileName3;
    String mFileName2;
    String mTitle;
    String mArtist;
    String mAlbum;
    String mRating;
    String searchterm;
    String sortorder;
    Spinner spin;
    Spinner spin2;
    Spinner spin3;
    String folder;
    String pcfolder;
    ArrayList<HashMap<String, String>> SongArray;
    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();
    EditText edittext_sleeptime;
    int sleeptime_hasfocus;
    DialogInterface.OnClickListener dialogClickListener2 = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    RefreshFileList(source);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    break;
            }
        }
    };
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    showsongchooser();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    scanmessage();
                    break;
            }
        }
    };
    private TimePicker tpResult;
    private long startTime;
    private long elapsedTime;
    private Button ScrollButton;
    private TextView InfoLine;
    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnRepeat;
    private ImageButton btnDownload;
    private ImageButton btnShuffle;
    private ImageButton btnBluetooth;
    private SeekBar songProgressBar;
    private Handler mHandler = new Handler();
    private TextView NowPlayingLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    private boolean isShuffle = true;
    private boolean isRepeat = false;
    private boolean isBluetooth = false;
    private boolean downloadcomplete = true;
    private boolean firstdownload = true;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private Utilities utils;
    // Internet Connection detector
    private ConnectionDetector cd;
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (sleeptime > 0) {
                elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime > sleeptime) {
                    mPlayer2.reset();
                    mPlayer2.stop();
                    onDestroy();
                    mHandler.removeCallbacksAndMessages(this);
                    PlayPhoneMusic.this.finish();
                    return;
                }
            }
            long totalDuration = mPlayer2.getDuration();
            long currentDuration = mPlayer2.getCurrentPosition();
            if (sleeptime > 0) {
                long remaining = (sleeptime - elapsedTime);
                // stuff that updates ui
                InfoLine.setText(utils.milliSecondsToTimer(remaining) + " mins until sleep");
                if (sleeptime_hasfocus == 0)
                    edittext_sleeptime.setText(utils.milliSecondsToTimer(remaining));
            }
            if (PlayLabel != "No Songs in Memory, Downloading Now.") {
                // Displaying Total Duration time
                songTotalDurationLabel.setText(""
                        + utils.milliSecondsToTimer(totalDuration));
                // Displaying time completed playing
                songCurrentDurationLabel.setText(""
                        + utils.milliSecondsToTimer(currentDuration));

                // Updating progress bar
                int progress = (int) (utils.getProgressPercentage(
                        currentDuration, totalDuration));
                songProgressBar.setProgress(progress);
            }
            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (mPlayer2 != null) {
            if (!mPlayer2.isPlaying()) {
                try {
                    mPlayer2.prepare();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block

                } catch (IOException e) {
                    // TODO Auto-generated catch block

                }
                mPlayer2.start();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.playphonemusic);
        spin = (Spinner) findViewById(R.id.spinner1);
        spin2 = (Spinner) findViewById(R.id.spinner2);
        spin3 = (Spinner) findViewById(R.id.spinner3);
        final Spinner spinner2 = (Spinner) findViewById(R.id.jumpto_spinner);
        ArrayAdapter<CharSequence> adapter3 = new ArrayAdapter<CharSequence>(
                this, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter3.add("A");
        adapter3.add("B");
        adapter3.add("C");
        adapter3.add("D");
        adapter3.add("E");
        adapter3.add("F");
        adapter3.add("G");
        adapter3.add("H");
        adapter3.add("I");
        adapter3.add("J");
        adapter3.add("K");
        adapter3.add("L");
        adapter3.add("M");
        adapter3.add("N");
        adapter3.add("O");
        adapter3.add("P");
        adapter3.add("Q");
        adapter3.add("R");
        adapter3.add("S");
        adapter3.add("T");
        adapter3.add("U");
        adapter3.add("V");
        adapter3.add("W");
        adapter3.add("X");
        adapter3.add("Y");
        adapter3.add("Z");
        spinner2.setAdapter(adapter3);
        spinner2.setOnItemSelectedListener(new MyOnItemSelectedListener5());
        ScrollButton = (Button) findViewById(R.id.scroll_button);
        InfoLine = (TextView) findViewById(R.id.infoline);
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        if (source == 2 || source == 4) {
            btnPrevious.setVisibility(View.INVISIBLE);
            btnNext.setVisibility(View.INVISIBLE);
        }
        edittext_sleeptime = (EditText) findViewById(R.id.SleepTime);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playphonemusic);
        takeKeyEvents(true);
        sleeptime_hasfocus = 0;
        source = getIntent().getIntExtra("source", 1);
        final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar_item);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#33cc33")));
        mydb = new EventDataSQLHelper(this);
        spin = (Spinner) findViewById(R.id.spinner1);
        spin2 = (Spinner) findViewById(R.id.spinner2);
        spin3 = (Spinner) findViewById(R.id.spinner3);
        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                Context.MODE_PRIVATE);
        folder = Settings.getString(PHONELOCATION, "ERROR");
        String lastchar = folder.substring(folder.length() - 1);
        if (!lastchar.equals("/")) {
            sdpath = folder + "/";
        } else {
            sdpath = folder;
        }
        if (folder.equals("ERROR")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PlayPhoneMusic.this);
            builder.setMessage("Please use \n\"Location of Phone Music\"\nfirst to tell me were your DataBase file is located.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            Intent intent = new Intent(PlayPhoneMusic.this, SettingsActivity.class);
            startActivity(intent);
            PlayPhoneMusic.this.finish();
        }

        int key = getIntent().getIntExtra("keypress", 0);
        if (key == 1) {
            forwardkeypressed();
        }
        searchterm = getIntent().getStringExtra("searchterm");
        utils = new Utilities();
        playedlist = new ArrayList<String>();
        playlistList = new ArrayList<String>();
        TextView sourcetext = (TextView) findViewById(R.id.textView1);
        if (source == 2 || source == 4) {
            String IP2 = Settings.getString(IP, "192.168.1.67");
            pcfolder = Settings.getString(PCLOCATION, "ERROR");
            itunes = "smb://" + IP2 + "/" + pcfolder + "/";
            if (pcfolder.equals("ERROR")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlayPhoneMusic.this);
                builder.setMessage("Please use \n\"Location of Network Music\"\nfirst to tell me were your songs are located.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                Intent intent = new Intent(PlayPhoneMusic.this, SettingsActivity.class);
                startActivity(intent);
                PlayPhoneMusic.this.finish();
            }
            cd = new ConnectionDetector(getApplicationContext());
            // Check if Internet present
            if (!cd.isConnectingToInternet()) {
                // Internet Connection is not present
                alert.showAlertDialog(PlayPhoneMusic.this,
                        "Internet Connection Error",
                        "Please connect to working Internet connection.\nPress OK to continue with Music on Phone.", false);
                source = 1;
                searchterm = null;
            }
        }
        if (source == 1 || source == 3)
            sourcetext.setText("Music On Phone");
        if (source == 2 || source == 4)
            sourcetext.setText("Music On Computer");
        if (source == 5)
            sourcetext.setText("Google Drive");
        final Spinner spinner2 = (Spinner) findViewById(R.id.jumpto_spinner);
        ArrayAdapter<CharSequence> adapter3 = new ArrayAdapter<CharSequence>(
                this, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter3.add("A");
        adapter3.add("B");
        adapter3.add("C");
        adapter3.add("D");
        adapter3.add("E");
        adapter3.add("F");
        adapter3.add("G");
        adapter3.add("H");
        adapter3.add("I");
        adapter3.add("J");
        adapter3.add("K");
        adapter3.add("L");
        adapter3.add("M");
        adapter3.add("N");
        adapter3.add("O");
        adapter3.add("P");
        adapter3.add("Q");
        adapter3.add("R");
        adapter3.add("S");
        adapter3.add("T");
        adapter3.add("U");
        adapter3.add("V");
        adapter3.add("W");
        adapter3.add("X");
        adapter3.add("Y");
        adapter3.add("Z");
        spinner2.setAdapter(adapter3);
        spinner2.setOnItemSelectedListener(new MyOnItemSelectedListener5());
        ScrollButton = (Button) findViewById(R.id.scroll_button);
        InfoLine = (TextView) findViewById(R.id.infoline);
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        if (source == 2 || source == 4) {
            btnPrevious.setVisibility(View.INVISIBLE);
            btnNext.setVisibility(View.INVISIBLE);
        }
        edittext_sleeptime = (EditText) findViewById(R.id.SleepTime);
        edittext_sleeptime.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String sleeptime2 = edittext_sleeptime.getText().toString();
                    try {
                        sleeptime = Integer.parseInt(sleeptime2) * 1000 * 60;
                        startTime = System.currentTimeMillis();
                        sleeptime_hasfocus = 0;
                        edittext_sleeptime.clearFocus();
                    } catch (NumberFormatException nfe) {
                        System.out.println("Could not parse " + nfe);
                    }
                }
                return false;
            }
        });
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
        btnBluetooth = (ImageButton) findViewById(R.id.btnBluetooth);
        btnBluetooth.setImageResource(R.drawable.btn_bluetooth);
        btnDownload = (ImageButton) findViewById(R.id.btnDownload);
        NowPlayingLabel = (TextView) findViewById(R.id.textView_NowPlaying);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
        songProgressBar.setOnSeekBarChangeListener(this); // Important
        mPlayer2.setOnCompletionListener(this); // Important
        songs = new ArrayList<Music>();
        SQLiteDatabase db = mydb.getWritableDatabase();
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    if (mPlayer2 != null) {
                        if (mPlayer2.isPlaying())
                            mPlayer2.pause();
                    }
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    if (mPlayer2 != null) {
                        if (!mPlayer2.isPlaying())
                            try {
                                mPlayer2.prepare();
                            } catch (IllegalStateException e) {
                                // TODO Auto-generated catch block

                            } catch (IOException e) {
                                // TODO Auto-generated catch block

                            }
                        mPlayer2.start();
                    }
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    if (mPlayer2 != null) {
                        if (mPlayer2.isPlaying())
                            mPlayer2.pause();
                    }
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        if (source == 1)// play phone music
        {
            String sql = "create table if not exists "
                    + EventDataSQLHelper.TABLE + "( "
                    + BaseColumns._ID + " integer primary key, "
                    + EventDataSQLHelper.FILE + " text, "
                    + EventDataSQLHelper.TITLE + " text, "
                    + EventDataSQLHelper.ARTIST + " text, "
                    + EventDataSQLHelper.ALBUM + " text, "
                    + EventDataSQLHelper.RATING + " integer default 0, "
                    + EventDataSQLHelper.PLAYEDCOUNT + " integer default 0);";
            db.execSQL(sql);
            Cursor cursor = db.rawQuery("select * from "
                    + EventDataSQLHelper.TABLE, null);
            if (cursor != null) {
                if (cursor.getCount() <= 0) {
                    RefreshFileList(source);
                }
                cursor.close();
                setupsong();
            } else {
                setupsong();
            }
        }
        if (source == 2) {
            String sql = "create table if not exists "
                    + EventDataSQLHelper.TABLE2 + "( "
                    + BaseColumns._ID + " integer primary key, "
                    + EventDataSQLHelper.FILE + " text, "
                    + EventDataSQLHelper.TITLE + " text, "
                    + EventDataSQLHelper.ARTIST + " text, "
                    + EventDataSQLHelper.ALBUM + " text, "
                    + EventDataSQLHelper.RATING + " integer default 0, "
                    + EventDataSQLHelper.PLAYEDCOUNT + " integer default 0);";
            db.execSQL(sql);
            Cursor cursor = db.rawQuery("select * from "
                    + EventDataSQLHelper.TABLE2, null);
            if (cursor != null) {
                if (cursor.getCount() == 0) {
                    downloadcomplete = false;
                    RefreshFileList(source);
                    while (downloadcomplete == false) {

                    }
                    cursor.close();
                }
                setupsong();
            }
        }
        if (source == 3 || source == 4) {
            setupsong();
        }
    }

    public void setupsong() {
        sortorder = "title ASC";
        DisplayData(searchterm, sortorder);
        setupSpinner(spin);
        setupSpinner2(spin2);
        setupSpinner3(spin3);
        if (!mPlayer2.isPlaying()) {
            Random rand = new Random();
            songIndex = rand.nextInt((songs.size() - 1) - 0 + 1) + 0;
            Music song = new Music();
            song = songs.get(songIndex);
            String mFileName2 = song.file;
            playsong(mFileName2);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdateTimeTask);
        mPlayer2.reset();
        PlayPhoneMusic.this.finish();
        String tempdirname = sdpath;
        tempdirname += "Temp/";
        File tempdir = new File(tempdirname);
        DeleteTempDir(tempdir);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Song Menu");
        menu.add(0, v.getId(), 0, "Delete Song");
        menu.add(0, v.getId(), 0, "Remove From DataBase");
        menu.add(0, v.getId(), 0, "Edit Tags");
        menu.add(0, v.getId(), 0, "Add To Playlist");
        if (source == 3 || source == 4) {
            menu.add(0, v.getId(), 0, "Remove From Playlist");
        }
        menu.add(0, v.getId(), 0, "Play Album");
        menu.add(0, v.getId(), 0, "Play Artist");
        if (source == 2 || source == 4)
            menu.add(0, v.getId(), 0, "Copy to Phone");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
                .getMenuInfo();
        if (item.getTitle().equals("Delete Song")) {
            DeleteSong(menuInfo.position);
        }
        if (item.getTitle().equals("Remove From DataBase")) {
            DeleteDataBase(menuInfo.position);
        }
        if (item.getTitle().equals("Edit Tags")) {
            EditTag(menuInfo.position);
        }
        if (item.getTitle().equals("Add To Playlist")) {
            AddToPlaylist(menuInfo.position);
        }
        if (item.getTitle().equals("Remove From Playlist")) {
            RemoveFromPlaylist(menuInfo.position);
        }
        if (item.getTitle().equals("Play Album")) {
            PlayAlbum(menuInfo.position);
        }
        if (item.getTitle().equals("Play Artist")) {
            PlayArtist(menuInfo.position);
        }
        if (item.getTitle().equals("Copy to Phone")) {
            CopytoPhone(menuInfo.position);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.memooptions, menu);
        MenuItem item = menu.findItem(R.id.autoedit_menu_item);
        if (source == 1 || source == 3) {
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.refresh_menu_item:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setMessage("Do you want to choose a Song,\nor Scan for new Songs?")
                        .setPositiveButton("Choose", dialogClickListener)
                        .setNegativeButton("Scan", dialogClickListener).show();
                return true;

            case R.id.search_menu_item:
                showDialog(SEARCH_DIALOG_ID);
                return true;

            case R.id.sleep_menu_item:
                showDialog(SLEEP_DIALOG_ID);
                return true;

            case R.id.editallplaylist_menu_item:
                EditAllPlaylistTags();
                return true;

            case R.id.addall2_menu_item:
                AddAllToPlaylist();
                return true;

            case R.id.autoedit_menu_item:
                new AutoEditTags().execute("");
                return true;

            case R.id.display_current_session:
                DisplayCurrentSession();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void RefreshFileList(int source) {
        if (source == 1) {
            new phonebackgroundrefresh()
                    .execute("");
        }
        if (source == 2) {
            new smbbackgroundrefresh()
                    .execute("");
        }
    }

    private void insertSongRow(String filename, String artist, String title,
                               String album, int rating) {
//        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("songs.db", null);
        SQLiteDatabase db = mydb.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EventDataSQLHelper.FILE, filename);
        values.put(EventDataSQLHelper.ARTIST, artist);
        values.put(EventDataSQLHelper.TITLE, title);
        values.put(EventDataSQLHelper.ALBUM, album);
        values.put(EventDataSQLHelper.RATING, rating);
        if (source == 1 || source == 3) {
            db.insert(EventDataSQLHelper.TABLE, null, values);
        }
        if (source == 2 || source == 4)
            db.insert(EventDataSQLHelper.TABLE2, null, values);
        values.clear();
    }

    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if (file.getName().contains(".")) {
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }

    private List<SmbFile> getsmbFiles(String parentDir, SmbConnection smbConnection) throws IOException {
        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                Context.MODE_PRIVATE);
        ArrayList<SmbFile> inFiles = new ArrayList<SmbFile>();
        List<SmbItem> files = null;
        SmbDirectory Directory = new SmbDirectory(smbConnection, parentDir);
        files = Directory.listItems();
        for (SmbItem file : files) {
            if (file.isDirectory()) {
                List<SmbFile> fileList = null;
                fileList = getsmbFiles(file.getPath(), smbConnection);
                inFiles.addAll(fileList);
            } else {
                if (file.getName().contains(".mp3")) {
                    inFiles.add((SmbFile) file);
                }
            }
        }
        return inFiles;
    }

    public void DisplayData(String search, String sortorder) {
        SQLiteDatabase db = mydb.getWritableDatabase();
        Cursor cursor = getSongs(search, sortorder, db);
        if (cursor.getCount() == 0) {
            if (source == 3) source = 1;
            if (source == 4) source = 2;
            searchterm = null;
            cursor = getSongs(null, null, db);
            Toast.makeText(getApplicationContext(),
                    "No songs match Search Term, displaying all songs.",
                    Toast.LENGTH_SHORT).show();
        }
        songs.clear();
        while (cursor.moveToNext()) {
            String mFileName2;
            String id = cursor.getString(0);
            mFileName2 = cursor.getString(1);
            String title = cursor.getString(2);
            final String artist = cursor.getString(3);
            final String album = cursor.getString(4);
            String rating = cursor.getString(6);
            if (rating.equals("m")) {
                rating = "0";
            }
            if (rating.equals("+")) {
                rating = "0";
            }
            String PC = cursor.getString(5);
            Music song = new Music();
            song.id = id;
            if (source == 2 || source == 4) {
                song.file = itunes + mFileName2;
            } else if (source == 5) {
                song.file = skydrive + mFileName2;
            } else {
                song.file = mFileName2;
            }
            song.title = title;
            song.artist = artist;
            song.album = album;
            song.rating = rating;
            String stars = "";
            if (!rating.isEmpty()) {
                stars = "";
                int in = Integer.valueOf(rating);
                for (int x = 0; x < in; x++) {
                    stars += "*";
                }
            }
            song.played = PC;
            song.display = title + " | " + "\n" + artist + "\t | " + album
                    + " | " + stars + " (" + PC + ")";
            songs.add(song);
        }
        MusicListAdaptor adaptor = new MusicListAdaptor(this, R.layout.playlist_item, songs);
        setListAdapter(adaptor);
        // selecting single ListView item
        final ListView lv = getListView();
        registerForContextMenu(lv);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ScrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                lv.setSelection(songIndex);
                if (source == 2 || source == 4) {
                    SharedPreferences Settings = getSharedPreferences(SETTINGS,
                            Context.MODE_PRIVATE);
                    int PlayIndex = Settings.getInt(LAST_PLAYED_ID, 0);
                    lv.setSelection(PlayIndex);
                }
            }
        });
        // listening to single listitem click
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                // getting listitem index
                songIndex = position;
                Music song = new Music();
                song = songs.get(songIndex);
                String mFileName2 = song.file;
                String mTitle = song.title;
                if (source == 2 || source == 4) {
                    if (downloading != null
                            && downloading.getStatus() == AsyncTask.Status.RUNNING)
                        downloading.cancel(true);
                    downloading = new DownloadTask().execute(mFileName2,
                            "false");
                    Toast.makeText(getApplicationContext(),
                            mTitle + " will be played next.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    playsong(mFileName2);
                }
            }
        });
    }

    private Cursor getSongs(String search, String sortorder, SQLiteDatabase db) {
        String search1 = null;
        String search2 = null;
        Cursor cursor = null;
        if (search != null && !search.contains("LIKE")
                && !search.contains("Rating") && !search.contains("inner join") && !search.contains("NOT IN")) {
            String[] separated = search.split("=");
            search1 = separated[0] + "=?";
            search2 = separated[1];
            if (source == 1 || source == 3) {
                cursor = db.query(EventDataSQLHelper.TABLE, null, search1,
                        new String[]{search2}, null, null, sortorder);
            }
            if (source == 2 || source == 4) {
                cursor = db.query(EventDataSQLHelper.TABLE2, null, search1,
                        new String[]{search2}, null, null, sortorder);
            }
        } else {
            if (source == 1)
                cursor = db.query(EventDataSQLHelper.TABLE, null, search, null,
                        null, null, sortorder);
            if (source == 2)
                cursor = db.query(EventDataSQLHelper.TABLE2, null, search,
                        null, null, null, sortorder);
            if (source == 3) {
                String sql = search;
                if (!sortorder.equals(null)) {
                    sql = sql + " ORDER BY " + sortorder;
                }
                cursor = db.rawQuery(sql, null);
            }
            if (source == 4) {
                String sql = search;
                if (!sortorder.equals(null)) {
                    sql = sql + " ORDER BY " + sortorder;
                }
                cursor = db.rawQuery(sql, null);
            }
        }
        return cursor;
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        if (source == 2 || source == 4) {
            SharedPreferences Settings = getSharedPreferences(SETTINGS,
                    Context.MODE_PRIVATE);
            Editor editor = Settings.edit();
            editor.putInt(LAST_PLAYED_ID, songIndex);
            editor.commit();
        }
        songIndex = songIndex + 1;
        if (isRepeat) {
            // repeat is on play same song again
            songIndex = songIndex - 1;
        } else if (isShuffle) {
            // shuffle is on - play a random song
            Random rand = new Random();
            songIndex = rand.nextInt((songs.size() - 1) - 0 + 1) + 0;
        } else if (songIndex < (songs.size() - 1)) {

        } else {
            // play first song
            songIndex = 0;
        }
        Music song = new Music();
        song = songs.get(songIndex);
        mFileName2 = song.file;
        if (source == 1 || source == 3 || downloadcomplete)
            playsong(mFileName2);
    }

    void playsong(String mFileName2) {
        if (source == 2 || source == 4) {
            if (downloading != null) {
                if (downloadcomplete) {
                    SharedPreferences Settings = getSharedPreferences(SETTINGS,
                            Context.MODE_PRIVATE);
                    downloading.cancel(true);
                    downloading = new DownloadTask().execute(mFileName2,
                            "false");
                    mFileName2 = Settings.getString(PLAY_NEXT, "ERROR");
                    PlayLabel = Settings.getString(LAST_PLAYED, "ERROR");
                }
            }
            if (downloading == null) {
                downloading = new DownloadTask().execute(mFileName2, "false");
                SharedPreferences Settings = getSharedPreferences(SETTINGS,
                        Context.MODE_PRIVATE);
                mFileName2 = Settings.getString(PLAY_NEXT, "ERROR");
                PlayLabel = Settings.getString(LAST_PLAYED, "ERROR");
            }
        }
        if (source == 1 || source == 3) {
            String tempmFileName2 = mFileName2;
            mFileName2 = sdpath + mFileName2;
        }
        mPlayer2.reset();
        mPlayer2.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer2.setDataSource(mFileName2);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block

        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            songIndex = 0;
            Music song = new Music();
            song = songs.get(songIndex);
            String mFileName3 = song.file;
            playsong(mFileName3);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            songIndex = 0;
            Music song = new Music();
            song = songs.get(songIndex);
            String mFileName3 = song.file;
            playsong(mFileName3);
        }
        try {
            mPlayer2.prepare();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block

        } catch (IOException e) {
            // TODO Auto-generated catch block
            PlayLabel = "No Songs in Memory, Downloading Now. Please Click The Forward Button when Downloading Completes.";
            NowPlayingLabel.setText(PlayLabel);
        }
        mPlayer2.start();
        playedlist.add(String.valueOf(songIndex));
        btnPlay.setImageResource(R.drawable.btn_pause);
        SQLiteDatabase db = mydb.getWritableDatabase();
        String search1 = null;
        String search2 = null;
        search1 = EventDataSQLHelper.FILE + "=?";
        String subpath = null;
        if (source == 2 || source == 4) {
            SharedPreferences Settings = getSharedPreferences(SETTINGS,
                    Context.MODE_PRIVATE);
            mFileName2 = Settings.getString(SMB_FILE, "ERROR");
            if (!mFileName2.equals("ERROR")) {
                int ituneslength = itunes.length();
                subpath = mFileName2.substring(ituneslength - 19);
            }
        }
        if (source == 1 || source == 3) {
            int phonepathlength = sdpath.length();
            subpath = mFileName2.substring(phonepathlength);
        }
        search2 = subpath;
        Cursor cursor = null;
        if (source == 2 || source == 4) {
            if (search2 != null) {
                cursor = db.query(EventDataSQLHelper.TABLE2, null, search1,
                        new String[]{search2}, null, null, sortorder);
            }
        }
        if (source == 1 || source == 3) {
            cursor = db.query(EventDataSQLHelper.TABLE, null, search1,
                    new String[]{search2}, null, null, sortorder);
        }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int playcount = cursor.getInt(5);
                playcount = playcount + 1;
                ContentValues values2 = new ContentValues();
                values2.put(EventDataSQLHelper.PLAYEDCOUNT, playcount);
                if (source == 2 || source == 4) {
                    db.update(EventDataSQLHelper.TABLE2, values2,
                            EventDataSQLHelper.FILE + "=?",
                            new String[]{subpath});
                }
                if (source == 1 || source == 3) {
                    db.update(EventDataSQLHelper.TABLE, values2,
                            EventDataSQLHelper.FILE + "=?",
                            new String[]{subpath});
                }
            }
            cursor.close();
        }
        Music song = new Music();
        song = songs.get(songIndex);
        if (source == 1 || source == 3) {
            PlayLabel = song.display;
        }
        if (PlayLabel == "ERROR") {
            PlayLabel = "No Songs in Memory, Downloading Now. Please Click The Forward Button when Downloading Completes.";
        }
        NowPlayingLabel.setText(PlayLabel);
        // set Progress bar values
        songProgressBar.setProgress(0);
        songProgressBar.setMax(100);
        // Updating progress bar
        boolean x = true;
        if (PlayLabel == "No Songs in Memory, Downloading Now.")
            x = false;
        updateProgressBar(x);
        /**
         * Play button click event plays a song and changes button to pause
         * image pauses a song and changes button to play image
         * */
        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                playbuttonclicked();
            }
        });

        /**
         * Forward button click event Forwards song specified seconds
         * */
        btnForward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mPlayer2.getCurrentPosition();
                // check if seekForward time is lesser than song duration
                if (currentPosition + seekForwardTime <= mPlayer2.getDuration()) {
                    // forward song
                    mPlayer2.seekTo(currentPosition + seekForwardTime);
                } else {
                    // forward to end position
                    mPlayer2.seekTo(mPlayer2.getDuration());
                }
            }
        });

        /**
         * Backward button click event Backward song to specified seconds
         * */
        btnBackward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mPlayer2.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if (currentPosition - seekBackwardTime >= 0) {
                    // forward song
                    mPlayer2.seekTo(currentPosition - seekBackwardTime);
                } else {
                    // backward to starting position
                    mPlayer2.seekTo(0);
                }
            }
        });

        /**
         * Next button click event Plays next song by taking currentSongIndex +
         * 1
         * */
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                forwardkeypressed();
            }
        });

        /**
         * Back button click event Plays previous song by currentSongIndex - 1
         * */
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                backkeypressed();
            }
        });

        /**
         * Button Click event for Repeat button Enables repeat flag to true
         * */
        btnRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isRepeat) {
                    isRepeat = false;
                    Toast.makeText(getApplicationContext(), "Repeat is OFF",
                            Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                } else {
                    // make repeat to true
                    isRepeat = true;
                    Toast.makeText(getApplicationContext(), "Repeat is ON",
                            Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isShuffle = false;
                    btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                }
            }
        });

        /**
         * Button Click event for Shuffle button Enables shuffle flag to true
         * */
        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isShuffle) {
                    isShuffle = false;
                    Toast.makeText(getApplicationContext(), "Shuffle is OFF",
                            Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                } else {
                    // make repeat to true
                    isShuffle = true;
                    Toast.makeText(getApplicationContext(), "Shuffle is ON",
                            Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }
            }
        });
        /**
         * Button Click event for Shuffle button Enables shuffle flag to true
         * */
        btnBluetooth.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isBluetooth) {
                    int REQUEST_BLUETOOTH = 2;
                    isBluetooth = false;
                    BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (BTAdapter.isEnabled()) {
                        BTAdapter.disable();
                    }
                    Toast.makeText(getApplicationContext(), "Bluetooth is OFF",
                            Toast.LENGTH_SHORT).show();
                    btnBluetooth.setImageResource(R.drawable.btn_bluetooth);
                } else {
                    // make repeat to true
                    int REQUEST_BLUETOOTH = 2;
                    isBluetooth = true;
                    BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (!BTAdapter.isEnabled()) {
                        Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBT, REQUEST_BLUETOOTH);
                    }
                    Toast.makeText(getApplicationContext(), "Bluetooth is ON",
                            Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    btnBluetooth.setImageResource(R.drawable.btn_bluetooth_focused);
                }
            }
        });
    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar(boolean x) {
        if (x) {
            mHandler.postDelayed(mUpdateTimeTask, 1000);
        }
    }

    /**
     *
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mPlayer2.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(),
                totalDuration);

        // forward or backward to certain seconds
        mPlayer2.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar(true);
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
                builder.setMessage("Enter Search Term\n"
                        + "(Note: This will cancel any existing Playlists.)");
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                PlayPhoneMusic.this.removeDialog(SEARCH_DIALOG_ID);
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
                                if (source == 3)
                                    source = 1;
                                if (source == 4)
                                    source = 2;
                                DisplayData(searchterm, sortorder);
                                spin.setSelection(0);
                                spin2.setSelection(0);
                                spin3.setSelection(0);
                                PlayPhoneMusic.this.removeDialog(SEARCH_DIALOG_ID);
                            }
                        });

                // Create the AlertDialog and return it
                AlertDialog searchDialog = builder.create();
                return searchDialog;

            case SLEEP_DIALOG_ID:
                LayoutInflater inflater3 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout3 = inflater3.inflate(R.layout.sleep_dialog,
                        (ViewGroup) findViewById(R.id.root));
                tpResult = (TimePicker) layout3.findViewById(R.id.timePicker1);
                final ToggleButton AlarmToggle = (ToggleButton) layout3.findViewById(R.id.toggleButton1);
                final CheckBox Mon = (CheckBox) layout3.findViewById(R.id.checkBoxMon);
                final CheckBox Tue = (CheckBox) layout3.findViewById(R.id.checkBoxTues);
                final CheckBox Wed = (CheckBox) layout3.findViewById(R.id.checkWed);
                final CheckBox Thu = (CheckBox) layout3.findViewById(R.id.checkThurs);
                final CheckBox Fri = (CheckBox) layout3.findViewById(R.id.checkBoxFrid);
                final CheckBox Sat = (CheckBox) layout3.findViewById(R.id.checkBoxSat);
                final CheckBox Sun = (CheckBox) layout3.findViewById(R.id.checkBoxSun);
                SharedPreferences Settings = getSharedPreferences(SETTINGS,
                        Context.MODE_PRIVATE);
                String alarmyesno = Settings.getString(ALARMYESNO, "no");
                String mon = Settings.getString(MONDAY, "no");
                String tue = Settings.getString(TUESDAY, "no");
                String wed = Settings.getString(WEDNESDAY, "no");
                String thu = Settings.getString(THURSDAY, "no");
                String fri = Settings.getString(FRIDAY, "no");
                String sat = Settings.getString(SATURDAY, "no");
                String sun = Settings.getString(SUNDAY, "no");
                int hour = Settings.getInt(HOUR, 6);
                int min = Settings.getInt(MINUTE, 44);
                tpResult.setCurrentHour(hour);
                tpResult.setCurrentMinute(min);
                if (alarmyesno.equals("no")) AlarmToggle.setChecked(false);
                if (alarmyesno.equals("yes")) AlarmToggle.setChecked(true);
                if (mon.equals("yes")) Mon.setChecked(true);
                if (tue.equals("yes")) Tue.setChecked(true);
                if (wed.equals("yes")) Wed.setChecked(true);
                if (thu.equals("yes")) Thu.setChecked(true);
                if (fri.equals("yes")) Fri.setChecked(true);
                if (sat.equals("yes")) Sat.setChecked(true);
                if (sun.equals("yes")) Sun.setChecked(true);
                AlarmToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                Context.MODE_PRIVATE);
                        Editor editor = Settings.edit();
                        if (arg1) {
                            editor.putString(ALARMYESNO, "yes");
                            editor.commit();
                        } else {
                            editor.putString(ALARMYESNO, "no");
                            editor.commit();
                        }
                    }
                });
                Mon.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                Context.MODE_PRIVATE);
                        Editor editor = Settings.edit();
                        if (arg1) {
                            editor.putString(MONDAY, "yes");
                            editor.commit();
                        } else {
                            editor.putString(MONDAY, "no");
                            editor.commit();
                        }
                    }
                });
                Tue.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                Context.MODE_PRIVATE);
                        Editor editor = Settings.edit();
                        if (arg1) {
                            editor.putString(TUESDAY, "yes");
                            editor.commit();
                        } else {
                            editor.putString(TUESDAY, "no");
                            editor.commit();
                        }
                    }
                });
                Wed.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                Context.MODE_PRIVATE);
                        Editor editor = Settings.edit();
                        if (arg1) {
                            editor.putString(WEDNESDAY, "yes");
                            editor.commit();
                        } else {
                            editor.putString(WEDNESDAY, "no");
                            editor.commit();
                        }
                    }
                });
                Thu.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                Context.MODE_PRIVATE);
                        Editor editor = Settings.edit();
                        if (arg1) {
                            editor.putString(THURSDAY, "yes");
                            editor.commit();
                        } else {
                            editor.putString(THURSDAY, "no");
                            editor.commit();
                        }
                    }
                });
                Fri.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                Context.MODE_PRIVATE);
                        Editor editor = Settings.edit();
                        if (arg1) {
                            editor.putString(FRIDAY, "yes");
                            editor.commit();
                        } else {
                            editor.putString(FRIDAY, "no");
                            editor.commit();
                        }
                    }
                });
                Sat.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                Context.MODE_PRIVATE);
                        Editor editor = Settings.edit();
                        if (arg1) {
                            editor.putString(SATURDAY, "yes");
                            editor.commit();
                        } else {
                            editor.putString(SATURDAY, "no");
                            editor.commit();
                        }
                    }
                });
                Sun.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                Context.MODE_PRIVATE);
                        Editor editor = Settings.edit();
                        if (arg1) {
                            editor.putString(SUNDAY, "yes");
                            editor.commit();
                        } else {
                            editor.putString(SUNDAY, "no");
                            editor.commit();
                        }
                    }
                });

                // ... other required overrides do nothing
                AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
                builder3.setView(layout3);
                // Now configure the AlertDialog
                builder3.setTitle("Set Alarm");
                builder3.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                PlayPhoneMusic.this.removeDialog(SLEEP_DIALOG_ID);
                            }
                        });
                builder3.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                int hour = tpResult.getCurrentHour();
                                int min = tpResult.getCurrentMinute();
                                SharedPreferences Settings = getSharedPreferences(
                                        SETTINGS, Context.MODE_PRIVATE);
                                Editor editor = Settings.edit();
                                editor.putInt(HOUR, hour);
                                editor.putInt(MINUTE, min);
                                editor.commit();
                                Alarm alarm = new Alarm();
                                alarm.CancelAlarm(getApplicationContext());
                                alarm.SetAlarm(getApplicationContext());
                                PlayPhoneMusic.this.removeDialog(SLEEP_DIALOG_ID);
                            }
                        });

                // Create the AlertDialog and return it
                AlertDialog sleepDialog = builder3.create();
                return sleepDialog;

            case ADDTOPLAYLIST_DIALOG_ID2:
                LayoutInflater inflater9 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout9 = inflater9.inflate(R.layout.sort_dialog,
                        (ViewGroup) findViewById(R.id.root));

                // ... other required overrides do nothing
                AlertDialog.Builder builder9 = new AlertDialog.Builder(this);
                builder9.setView(layout9);
                final Spinner spinner9 = (Spinner) layout9
                        .findViewById(R.id.sortorder_spinner);
                ArrayAdapter<String> spinnerArrayAdapter9 = new ArrayAdapter<String>(
                        this, android.R.layout.simple_spinner_dropdown_item,
                        playlistList);
                spinner9.setAdapter(spinnerArrayAdapter9);
                spinner9.setOnItemSelectedListener(new MyOnItemSelectedListener4());
                // Now configure the AlertDialog
                builder9.setTitle("Select Playlist");
                builder9.setMessage("Choose Playlist to add these songs to");
                builder9.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                PlayPhoneMusic.this
                                        .removeDialog(ADDTOPLAYLIST_DIALOG_ID2);
                            }
                        });
                builder9.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Music song = new Music();
//                                SQLiteDatabase db = SQLiteDatabase
//                                        .openOrCreateDatabase("songs.db", null);
                                SQLiteDatabase db = mydb.getWritableDatabase();
                                ContentValues values2 = new ContentValues();
                                for (int x = 0; x < songs.size(); x++) {
                                    song = songs.get(x);
                                    mName5 = song.id;
                                    values2.put("playitem", mName5);
                                    db.insert("'" + addtoplaylist + "'", null,
                                            values2);
                                }
                                PlayPhoneMusic.this
                                        .removeDialog(ADDTOPLAYLIST_DIALOG_ID2);
                            }
                        });

                // Create the AlertDialog and return it
                AlertDialog addtoplaylistDialog2 = builder9.create();
                return addtoplaylistDialog2;

            case ADDTOPLAYLIST_DIALOG_ID:
                LayoutInflater inflater7 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout7 = inflater7.inflate(R.layout.sort_dialog,
                        (ViewGroup) findViewById(R.id.root));

                // ... other required overrides do nothing
                AlertDialog.Builder builder7 = new AlertDialog.Builder(this);
                builder7.setView(layout7);
                final Spinner spinner7 = (Spinner) layout7
                        .findViewById(R.id.sortorder_spinner);
                ArrayAdapter<String> spinnerArrayAdapter7 = new ArrayAdapter<String>(
                        this, android.R.layout.simple_spinner_dropdown_item,
                        playlistList);
                spinner7.setAdapter(spinnerArrayAdapter7);
                spinner7.setOnItemSelectedListener(new MyOnItemSelectedListener4());
                // Now configure the AlertDialog
                builder7.setTitle("Select Playlist");
                builder7.setMessage("Choose Playlist to add this song to");
                builder7.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                PlayPhoneMusic.this
                                        .removeDialog(ADDTOPLAYLIST_DIALOG_ID);
                            }
                        });
                builder7.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
//                                SQLiteDatabase db = SQLiteDatabase
//                                        .openOrCreateDatabase("songs.db", null);
                                SQLiteDatabase db = mydb.getWritableDatabase();
                                ContentValues values2 = new ContentValues();
                                values2.put("playitem", mName5);
                                db.insert("'" + addtoplaylist + "'", null, values2);
                                String Message;
                                Message = mName5 + "added to playlist "
                                        + addtoplaylist;
                                Toast.makeText(getBaseContext(), Message,
                                        Toast.LENGTH_LONG).show();
                                PlayPhoneMusic.this
                                        .removeDialog(ADDTOPLAYLIST_DIALOG_ID);
                            }
                        });

                // Create the AlertDialog and return it
                AlertDialog addtoplaylistDialog = builder7.create();
                return addtoplaylistDialog;

            case EDITTAGS_DIALOG_ID:
                LayoutInflater inflater6 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout6 = inflater6.inflate(R.layout.edittags_dialog,
                        (ViewGroup) findViewById(R.id.root));
                final EditText title = (EditText) layout6
                        .findViewById(R.id.EditText_SongTitle);
                final EditText artist = (EditText) layout6
                        .findViewById(R.id.EditText_Artist);
                final EditText album = (EditText) layout6
                        .findViewById(R.id.EditText_Album);
                final EditText rating = (EditText) layout6
                        .findViewById(R.id.EditText_Rating);
                title.setText(mTitle);
                artist.setText(mArtist);
                album.setText(mAlbum);
                rating.setText(mRating);

                // ... other required overrides do nothing
                AlertDialog.Builder builder6 = new AlertDialog.Builder(this);
                builder6.setView(layout6);
                // Now configure the AlertDialog
                builder6.setTitle("Edit Tags");
                builder6.setMessage("Update the Tags to be Changed");
                builder6.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                PlayPhoneMusic.this
                                        .removeDialog(EDITTAGS_DIALOG_ID);
                            }
                        });
                builder6.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mTitle = title.getText().toString();
                                mArtist = artist.getText().toString();
                                mAlbum = album.getText().toString();
                                mRating = rating.getText().toString();
                                SQLiteDatabase db = mydb.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(EventDataSQLHelper.ARTIST, mArtist);
                                values.put(EventDataSQLHelper.TITLE, mTitle);
                                values.put(EventDataSQLHelper.ALBUM, mAlbum);
                                values.put(EventDataSQLHelper.RATING, mRating);
                                if (source == 1 || source == 3) {
                                    db.update(EventDataSQLHelper.TABLE, values,
                                            EventDataSQLHelper.FILE + "=?",
                                            new String[]{mFileName3});
                                }
                                if (source == 2 || source == 4) {
                                    db.update(EventDataSQLHelper.TABLE2, values,
                                            EventDataSQLHelper.FILE + "=?",
                                            new String[]{mFileName3.substring(19)});
                                }
                                db.close();
                                DisplayData(searchterm, sortorder);
                                final ListView lv = getListView();
                                lv.setSelection(selectedIndex);
                                if (source == 1 || source == 3) {
                                    // read metadata
                                    MusicMetadata meta = new MusicMetadata("name");
                                    meta.setAlbum(mAlbum);
                                    meta.setArtist(mArtist);
                                }
                                PlayPhoneMusic.this
                                        .removeDialog(EDITTAGS_DIALOG_ID);
                            }
                        });

                // Create the AlertDialog and return it
                AlertDialog edittagsDialog = builder6.create();
                return edittagsDialog;

            case EDITTAGS_DIALOG_ID2:
                LayoutInflater inflater8 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout8 = inflater8.inflate(R.layout.edittags_dialog,
                        (ViewGroup) findViewById(R.id.root));
                final EditText title2 = (EditText) layout8
                        .findViewById(R.id.EditText_SongTitle);
                final EditText artist2 = (EditText) layout8
                        .findViewById(R.id.EditText_Artist);
                final EditText album2 = (EditText) layout8
                        .findViewById(R.id.EditText_Album);
                final EditText rating2 = (EditText) layout8
                        .findViewById(R.id.EditText_Rating);
                final TextView hide = (TextView) layout8
                        .findViewById(R.id.textView1);
                title2.setVisibility(View.GONE);
                hide.setVisibility(View.GONE);
                artist2.setText(mArtist);
                album2.setText(mAlbum);
                rating2.setText(mRating);

                // ... other required overrides do nothing
                AlertDialog.Builder builder8 = new AlertDialog.Builder(this);
                builder8.setView(layout8);
                // Now configure the AlertDialog
                builder8.setTitle("Edit All Playlist Tags");
                builder8.setMessage("Enter the Tags to be Changed for All Songs in This Playlist");
                builder8.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                PlayPhoneMusic.this
                                        .removeDialog(EDITTAGS_DIALOG_ID2);
                            }
                        });
                builder8.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String mArtist = artist2.getText().toString();
                                String mAlbum = album2.getText().toString();
                                String mRating = rating2.getText().toString();
//                                SQLiteDatabase db = SQLiteDatabase
//                                        .openOrCreateDatabase("songs.db", null);
                                SQLiteDatabase db = mydb.getWritableDatabase();
                                for (int x = 0; x < songs.size(); x++) {
                                    Music song = new Music();
                                    song = songs.get(x);
                                    String mFileName3 = song.file;
                                    if (source == 2 || source == 4) {
                                        int ituneslength = itunes.length();
                                        mFileName3 = mFileName3.substring(ituneslength-19);
                                    }
                                    ContentValues values = new ContentValues();
                                    if (mArtist != null && !mArtist.equals("")) {
                                        values.put(EventDataSQLHelper.ARTIST,
                                                mArtist);
                                    }
                                    if (mAlbum != null && !mAlbum.equals("")) {
                                        values.put(EventDataSQLHelper.ALBUM, mAlbum);
                                    }
                                    if (!mRating.equals("") && mRating != null) {
                                        values.put(EventDataSQLHelper.RATING,
                                                mRating);
                                    }
                                    if (source == 1 || source == 3) {
                                        db.update(EventDataSQLHelper.TABLE, values,
                                                EventDataSQLHelper.FILE + "=?",
                                                new String[]{mFileName3});
                                    }
                                    if (source == 2 || source == 4) {
                                        db.update(EventDataSQLHelper.TABLE2,
                                                values, EventDataSQLHelper.FILE
                                                        + "=?",
                                                new String[]{mFileName3});
                                    }
                                    if (source == 1 || source == 3) {
                                        // read metadata
                                        MusicMetadata meta = new MusicMetadata(
                                                "name");
                                        if (mAlbum != null && !mAlbum.equals("")) {
                                            meta.setAlbum(mAlbum);
                                        }
                                        if (mAlbum != null && !mAlbum.equals("")) {
                                            meta.setArtist(mArtist);
                                        }
                                    }
                                }
                                DisplayData(searchterm, sortorder);
                                PlayPhoneMusic.this
                                        .removeDialog(EDITTAGS_DIALOG_ID);
                            }
                        });

                // Create the AlertDialog and return it
                AlertDialog edittagsDialog2 = builder8.create();
                return edittagsDialog2;

            case CHANGEIP:
                LayoutInflater IPinflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View IPlayout = IPinflater.inflate(R.layout.ip_dialog,
                        (ViewGroup) findViewById(R.id.root));
                final EditText IP3 = (EditText) IPlayout
                        .findViewById(R.id.EditText_IP);
                SharedPreferences Settings2 = getSharedPreferences(SETTINGS,
                        Context.MODE_PRIVATE);
                String IP2 = Settings2.getString(IP, "192.168.1.67");
                IP3.setText(IP2);

                // ... other required overrides do nothing
                AlertDialog.Builder IPbuilder = new AlertDialog.Builder(this);
                IPbuilder.setView(IPlayout);
                // Now configure the AlertDialog
                IPbuilder.setTitle("Change DESKTOP IP");
                IPbuilder
                        .setMessage("Cannot connect to Desktop PC. Please make sure you are logged onto the Desktop then use ES File Explorer to find the correct IP address for DESKTOP, \n\nor click CANCEL to play songs from Phone.\n");
                IPbuilder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                switchtophone();
                                PlayPhoneMusic.this.removeDialog(CHANGEIP);
                            }
                        });
                IPbuilder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String IP2 = IP3.getText().toString();
                                SharedPreferences Settings = getSharedPreferences(
                                        SETTINGS, Context.MODE_PRIVATE);
                                Editor editor = Settings.edit();
                                editor.putString(IP, IP2);
                                editor.commit();
                                itunes = "smb://"
                                        + IP2
                                        + pcfolder
                                        + "/";
                                Random rand = new Random();
                                songIndex = rand.nextInt((songs.size() - 1) - 0 + 1) + 0;
                                Music song = new Music();
                                song = songs.get(songIndex);
                                String mFileName2 = song.file;
                                playsong(mFileName2);
                                PlayPhoneMusic.this.removeDialog(SEARCH_DIALOG_ID);
                            }
                        });
                // Create the AlertDialog and return it
                AlertDialog IPDialog = IPbuilder.create();
                return IPDialog;
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
            case CHANGEIP:
                return;
            case SLEEP_DIALOG_ID:
                return;
            case ARTIST_DIALOG_ID:
                return;
            case ALBUM_DIALOG_ID:
                return;
            case EDITTAGS_DIALOG_ID:
                return;
            case ADDTOPLAYLIST_DIALOG_ID:
                return;
            case EDITTAGS_DIALOG_ID2:
                return;
            case ADDTOPLAYLIST_DIALOG_ID2:
                return;
        }
    }

    void getartists() {
        ArrayList<String> artists = new ArrayList<String>();
        Music song = new Music();
        sortorder = "title ASC";
        DisplayData(searchterm, sortorder);
        for (int x = 0; x < songs.size(); x++) {
            song = songs.get(x);
            artists.add(song.artist);
        }
        artistsList = new ArrayList<String>(new HashSet<String>(artists));
        Collections.sort(artistsList);
        artistsList.add(0, "Artist");
    }

    void getalbums() {
        ArrayList<String> albums = new ArrayList<String>();
        Music song = new Music();
        sortorder = "title ASC";
        DisplayData(searchterm, sortorder);
        for (int x = 0; x < songs.size(); x++) {
            song = songs.get(x);
            String album = song.album;
            if (album == null)
                album = "Unknown";
            albums.add(album);
        }
        albumsList = new ArrayList<String>(new HashSet<String>(albums));
        Collections.sort(albumsList);
        albumsList.add(0, "Album");
    }

    void DeleteSong(final int position) {
        Music song = new Music();
        song = songs.get(position);
        final String mFileName2 = song.file;
        CharSequence text = "Are You Sure You Want to Delete " + mFileName2;
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Confirm Delete Song");
        alertDialog.setMessage(text);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // here you can add functions
                File deletefile = new File(sdpath + mFileName2);
                deletefile.delete();
                //               SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("songs.db",
                //                       null);
                SQLiteDatabase db = mydb.getWritableDatabase();
                db.delete(EventDataSQLHelper.TABLE, EventDataSQLHelper.FILE
                        + "=?", new String[]{mFileName2});
                DisplayData(searchterm, sortorder);
                final ListView lv = getListView();
                lv.setSelection(position);
                isStorageLocationWriteable();
                if (isLocationWriteable == FALSE) {
                    CharSequence text = "Unable to delete the song from your phone.  It has been removed from the DataBase, please us a File Manager to delete it from your phone.";
                    AlertDialog alertDialog = new AlertDialog.Builder(PlayPhoneMusic.this).create();
                    alertDialog.setTitle("WARNING!!!!!!!!");
                    alertDialog.setMessage(text);
                    alertDialog.setButton2("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertDialog.show();
                }
            }
        });
        alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }

    void DeleteDataBase(final int position) {
        Music song = new Music();
        song = songs.get(position);
        final String mFileName2 = song.file;
        final String mID = song.id;
        CharSequence text = "Are You Sure You Want to Remove " + mFileName2 + " From The Data Base?";
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Confirm Delete Song");
        alertDialog.setMessage(text);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // here you can add functions
                //               SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("songs.db",
                //                       null);
                SQLiteDatabase db = mydb.getWritableDatabase();
                if (source == 2 || source == 4) {
                    db.delete(EventDataSQLHelper.TABLE2, BaseColumns._ID
                            + "=?", new String[]{mID});
                }
                if (source == 1 || source == 3) {
                    db.delete(EventDataSQLHelper.TABLE, BaseColumns._ID
                            + "=?", new String[]{mID});
                }
                DisplayData(searchterm, sortorder);
                final ListView lv = getListView();
                lv.setSelection(position);
            }
        });
        alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }

    void EditTag(int position) {
        Music song = new Music();
        song = songs.get(position);
        selectedIndex = position;
        mFileName3 = song.file;
        if (source == 2 || source == 4) {
            int ituneslength = itunes.length();
            mFileName3 = mFileName3.substring(ituneslength-19);
        }
        mTitle = song.title;
        mArtist = song.artist;
        mAlbum = song.album;
        mRating = song.rating;
        showDialog(EDITTAGS_DIALOG_ID);
    }

    void EditAllPlaylistTags() {
        showDialog(EDITTAGS_DIALOG_ID2);
    }

    void AddToPlaylist(int position) {
//        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("songs.db", null);
        SQLiteDatabase db = mydb.getWritableDatabase();
        playlistList.clear();
        Music song = new Music();
        song = songs.get(position);
        mName5 = song.id;
        Cursor cursor = null;
        String search1 = EventDataSQLHelper.SOURCE + "=?";
        String search2 = Integer.toString(source);
        cursor = db.query(EventDataSQLHelper.TABLE4, null, search1,
                new String[]{search2}, null, null, null);
        startManagingCursor(cursor);
        while (cursor.moveToNext()) {
            String mName20 = cursor.getString(0);
            playlistList.add(mName20);
        }
        Collections.sort(playlistList);
        showDialog(ADDTOPLAYLIST_DIALOG_ID);
    }

    void AddAllToPlaylist() {
        //       SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("songs.db", null);
        SQLiteDatabase db = mydb.getWritableDatabase();
        playlistList.clear();
        Cursor cursor = null;
        String search1 = EventDataSQLHelper.SOURCE + "=?";
        String search2 = Integer.toString(source);
        cursor = db.query(EventDataSQLHelper.TABLE4, null, search1,
                new String[]{search2}, null, null, null);
        startManagingCursor(cursor);
        while (cursor.moveToNext()) {
            String mName20 = cursor.getString(0);
            playlistList.add(mName20);
        }
        Collections.sort(playlistList);
        showDialog(ADDTOPLAYLIST_DIALOG_ID2);
    }

    void PlayArtist(int position) {
        Music song = new Music();
        song = songs.get(position);
        mName5 = song.artist;
        searchterm = "Artist=" + mName5;
        sortorder = null;
        if (source == 3)
            source = 1;
        if (source == 4)
            source = 2;
        DisplayData(searchterm, sortorder);
    }

    void PlayAlbum(int position) {
        Music song = new Music();
        song = songs.get(position);
        mName5 = song.album;
        searchterm = "Album=" + mName5;
        sortorder = null;
        if (source == 3)
            source = 1;
        if (source == 4)
            source = 2;
        DisplayData(searchterm, sortorder);
    }

    void RemoveFromPlaylist(int position) {
//        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("songs.db", null);
        final SQLiteDatabase db = mydb.getWritableDatabase();
        String playlist = getIntent().getStringExtra("searchterm");
        playlist = playlist.substring(playlist.indexOf("'") + 1);
        playlist = playlist.substring(0, playlist.indexOf("'"));
        final String playlist2 = playlist;
        Music song = new Music();
        song = songs.get(position);
        final String id2 = song.id;
        final String title = song.title;
        AlertDialog.Builder builder = new AlertDialog.Builder(PlayPhoneMusic.this);
        builder.setMessage("Are you sure you want to remove '" + title + "' from '" + playlist2 + "'")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String sql = "DELETE FROM '" + playlist2 + "' WHERE playitem=" + id2;
                        db.execSQL(sql);
                        DisplayData(searchterm, sortorder);
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    void CopytoPhone(int position) {
        songIndex = position;
        Music song = new Music();
        song = songs.get(songIndex);
        String mFileName2 = song.file;
        String mTitle = song.title;
        if (downloading.getStatus() == AsyncTask.Status.RUNNING) {
            downloading.cancel(true);
        }
        downloading = new DownloadTask().execute(mFileName2, "true");
        Toast.makeText(getApplicationContext(),
                mTitle + " will be copied to your phone.", Toast.LENGTH_SHORT)
                .show();
    }

    void UpDateTags(String strPCPath, String LocalFile) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        File src = new File(LocalFile);
        MusicMetadataSet src_set = null;
        try {
            src_set = new MyID3().read(src);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
        } // read metadata
        mmr.setDataSource(LocalFile);
        String rating = null;
        final String album = mmr
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        final String artist = mmr
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        final String title = mmr
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if (!LocalFile.contains(".m4a")) {
            try {
                IMusicMetadata metadata = src_set.getSimplified();
                rating = metadata.getComment();
            } catch (Exception e) {

            }
            if (rating != null) {
                if (rating.length() > 1) {
                    rating = rating.substring(rating.length() - 1);
                    if (rating.matches("-?(0|[1-9]\\d*)")) {
                        rating = rating;
                    } else {
                        rating = "0";
                    }
                }
            }
        }
        SQLiteDatabase db = mydb.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (artist != null)
            values.put(EventDataSQLHelper.ARTIST, artist);
        if (title != null)
            values.put(EventDataSQLHelper.TITLE, title);
        if (album != null)
            values.put(EventDataSQLHelper.ALBUM, album);
        if (rating != null)
            values.put(EventDataSQLHelper.RATING, rating);
        db.update(EventDataSQLHelper.TABLE2, values, EventDataSQLHelper.FILE
                + "=?", new String[]{strPCPath});
        String search1 = null;
        String search2 = null;
        search1 = EventDataSQLHelper.FILE + "=?";
        search2 = strPCPath;
        Cursor cursor = db.query(EventDataSQLHelper.TABLE, null, search1,
                new String[]{search2}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String rating2 = " ";
                rating2 = cursor.getString(6);

                if (rating2 != rating) {
                    String finalRating = rating2;
                    String finalRating1 = rating;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            CharSequence text = "The rating of this song (" + title + ") on the phone (" + finalRating + ") differs to that on the PC (" + finalRating1 + "). Do you want to update the rating on the phone?";
                            AlertDialog alertDialog = new AlertDialog.Builder(PlayPhoneMusic.this).create();
                            alertDialog.setTitle("Ratings Differ");
                            alertDialog.setMessage(text);
                            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    db.update(EventDataSQLHelper.TABLE, values, EventDataSQLHelper.FILE
                                            + "=?", new String[]{strPCPath});
                                }
                            });
                            alertDialog.setButton2("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            alertDialog.show();
                        }
                    });
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void DeleteTempDir(File fileOrDirectory) {
        String DontDelete = sdpath;
        DontDelete += "Temp/";
        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                Context.MODE_PRIVATE);
        String DontDelete2 = Settings.getString(PLAY_NEXT, "ERROR");
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                String debug = child.getName();
                debug = DontDelete + debug;
                if (!debug.equals(DontDelete2)) {
                    child.delete();
                }
                DeleteTempDir(child);
            }
    }

    void scanmessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (source == 1 || source == 3) {
            builder.setMessage("Please make sure any new songs have been added to\n" + folder + "\nin the format\nArtist/Album/SongTitle.mp3.")
                    .setPositiveButton("Yes", dialogClickListener2)
                    .setNegativeButton("No", dialogClickListener2).show();
        }
        if (source == 2 || source == 4) {
            builder.setMessage("Please make sure any new songs have been added to\n" + pcfolder + "\nin the format\nArtist/Album/SongTitle.mp3.")
                    .setPositiveButton("Yes", dialogClickListener2)
                    .setNegativeButton("No", dialogClickListener2).show();
        }
    }

    private boolean ChangeIP() {
        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                Context.MODE_PRIVATE);
        String IP2 = Settings.getString(IP, "ERROR");
        if (IP2 == "ERROR") IP2 = "192.168.1.10";
        IP2 = IP2.substring(0, 10);
        int ip6 = 0;
        IP2 = IP2 + Integer.toString(ip6);
        boolean sucess = false;
        Settings = getSharedPreferences(SETTINGS,
                Context.MODE_PRIVATE);
        String user = Settings.getString(USER, "ERROR");
        String pass = Settings.getString(PASSWORD, "ERROR");
        AuthenticationContext auth = new AuthenticationContext(user, pass.toCharArray(), "");
        try (SmbConnection smbConnection = new SmbConnection(IP2, "User2", auth)) {
            SmbFile smbFileToDownload = null;
            // Do your work
            while (!sucess) {
                int q = 0;
                final String IP3 = IP2;
                runOnUiThread(new Runnable() {
                    public void run() {
                        // stuff that updates ui
                        Toast.makeText(getApplicationContext(),
                                "Trying " + IP3,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                try {
                    String strPCPath = "smb://"
                            + IP2
                            + pcfolder;

                    smbFileToDownload = new SmbFile(smbConnection, strPCPath);
                } catch (Exception e) {

                }
                InputStream inputStream = null;
                inputStream = smbFileToDownload.getInputStream();
                if (q == 0) {
                    sucess = true;
                    Settings = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
                    Editor editor = Settings.edit();
                    editor.putString(IP, IP2);
                    editor.commit();
                    itunes = "smb://" + IP2
                            + pcfolder + "/";
                    sortorder = "title ASC";
                    DisplayData(searchterm, sortorder);
                    Random rand = new Random();
                    songIndex = rand.nextInt((songs.size() - 1) - 0 + 1) + 0;
                    Music song = new Music();
                    song = songs.get(songIndex);
                    String mFileName2 = song.file;
                    downloadcomplete = true;
                    playsong(mFileName2);
                    return true;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void DisplayCurrentSession() {
//        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("songs.db", null);
        SQLiteDatabase db = mydb.getWritableDatabase();
        final ArrayList<Music> tempSongArray = new ArrayList<Music>();
        for (String temp : playedlist) {
            String mFileName2;
            String id = temp;
            Cursor cursor = null;
            int myNum = 0;
            try {
                myNum = Integer.parseInt(temp);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }
            Music song = new Music();
            song = songs.get(myNum);
            String mFileName3 = song.file;
            String id2 = song.id;
            if (source == 1 || source == 3) {
                cursor = db.query(EventDataSQLHelper.TABLE, null,
                        "File=?", new String[]{mFileName3}, null,
                        null, null);
            }
            if (source == 2 || source == 4) {
                cursor = db.query(EventDataSQLHelper.TABLE2, null,
                        BaseColumns._ID + "=?", new String[]{id2}, null,
                        null, null);
            }
            while (cursor.moveToNext()) {
                mFileName2 = cursor.getString(1);
                String title = cursor.getString(2);
                final String artist = cursor.getString(3);
                final String album = cursor.getString(4);
                String rating = cursor.getString(6);
                String PC = cursor.getString(5);
                song.id = id;
                if (source == 2 || source == 4) {
                    song.file = itunes + mFileName2;
                } else {
                    song.file = mFileName2;
                }
                song.title = title;
                song.artist = artist;
                song.album = album;
                song.rating = rating;
                String stars = "";
                if (!rating.isEmpty()) {
                    stars = "";
                    int in = Integer.valueOf(rating);
                    for (int x = 0; x < in; x++) {
                        stars += "*";
                    }
                }
                song.played = PC;
                song.display = title + " | " + "\n" + artist + "\t | "
                        + album + " | " + stars + " (" + PC + ")";
                tempSongArray.add(song);
            }
            cursor.close();
        }
        playedlist.clear();
        MusicListAdaptor adaptor = new MusicListAdaptor(this, R.layout.playlist_item, tempSongArray);
        setListAdapter(adaptor);
        // selecting single ListView item
        final ListView lv = getListView();
        registerForContextMenu(lv);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ScrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                lv.setSelection(songIndex);
                if (source == 2 || source == 4) {
                    SharedPreferences Settings = getSharedPreferences(SETTINGS,
                            Context.MODE_PRIVATE);
                    int PlayIndex = Settings.getInt(LAST_PLAYED_ID, 0);
                    lv.setSelection(PlayIndex);
                }
            }
        });
        // listening to single listitem click
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                // getting listitem index
                Music song = new Music();
                song = tempSongArray.get(position);
                String mFileName2 = song.file;
                String mTitle = song.title;
                songIndex = Integer.parseInt(song.id);
                if (source == 2 || source == 4) {
                    if (downloading != null
                            && downloading.getStatus() == AsyncTask.Status.RUNNING)
                        downloading.cancel(true);
                    downloading = new DownloadTask().execute(mFileName2,
                            "false");
                    Toast.makeText(getApplicationContext(),
                            mTitle + " will be played next.",
                            Toast.LENGTH_SHORT).show();
                    SharedPreferences Settings = getSharedPreferences(SETTINGS,
                            Context.MODE_PRIVATE);
                } else {
                    playsong(mFileName2);
                }
            }
        });
    }

    void showsongchooser() {
        if (source == 2 || source == 4) {
            Intent player = new Intent(PlayPhoneMusic.this, FileChooser.class);
            player.putExtra(IP, itunes.substring(19, itunes.length() - 1));
            player.putExtra(SOURCE, "2");
            startActivityForResult(player, 1);
        } else {
            Intent player = new Intent(PlayPhoneMusic.this, FileChooser.class);
            player.putExtra(IP, "");
            player.putExtra(SOURCE, "1");
            startActivityForResult(player, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    String filename = data.getStringExtra("result");
                    filename = filename.replace("%2F", "/");
                    filename = filename.replace("%20", " ");
                    filename = filename.replace(sdpath, "");
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    String album = null;
                    String artist = null;
                    String title = null;
                    int rating = 0;
                    if (title == null) {
                        String subpath = filename;
                        String[] separated = subpath.split("/");
                        int length;
                        length = separated.length;
                        if (separated.length > 2) {
                            artist = separated[length - 3];
                            album = separated[length - 2];
                            title = separated[length - 1];
                            title = title.substring(0,
                                    title.length() - 4);
                            title = title.replaceAll("[-]", "");
                            title = title.replaceAll("[0-9]", "");
                            title = title.replaceAll("[.]", "");
                            title = title.trim();
                        }
                    }
                    if (title != null) {
                        insertSongRow(filename, artist, title, album,
                                rating);
                    }
                }
                DisplayData(searchterm, sortorder);
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    String filename = data.getStringExtra("result");
                    String filename2 = filename;
                    filename = filename.replace("%2F", "/");
                    filename = filename.replace("%20", " ");
                    if (filename.contains("iTunes Music")) {
                        //file on desktop
                        int tempsource = source;
                        source = 2;
                        int splitpoint = itunes.length() - 19;
                        filename = filename.substring(splitpoint);
                        String album2 = null;
                        String artist2 = null;
                        String title2 = null;
                        int rating2 = 0;
                        String subpath2 = filename;
                        String subpath3 = subpath2.substring(0, subpath2.length() - 4);
                        subpath3 = subpath3 + ".m4a";
                        String subpath4 = subpath2.substring(subpath2.length() - 4, subpath2.length());
                        String[] separated2 = subpath2.split("/");
                        artist2 = separated2[0];
                        if (separated2.length > 1) {
                            album2 = separated2[1];
                        }
                        if (separated2.length > 2) {
                            title2 = separated2[2];
                            title2 = title2.substring(0, title2.length() - 4);
                            title2 = title2.replaceAll("[-]", "");
                            title2 = title2.replaceAll("[0-9]", "");
                            title2 = title2.replaceAll("[.]", "");
                            title2 = title2.trim();
                        }
                        insertSongRow(subpath2, artist2, title2, album2, rating2);
                        if (subpath4.equals(".mp3")) {
                            SQLiteDatabase db = mydb.getWritableDatabase();
                            db.delete(EventDataSQLHelper.TABLE2, EventDataSQLHelper.FILE
                                    + "=?", new String[]{subpath3});
                            db.close();
                        }
                        source = tempsource;
                    }
                }
                DisplayData(searchterm, sortorder);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setupSpinner(Spinner spin) {
        getalbums();
        ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item,
                albumsList);
        spin.setAdapter(spinnerArrayAdapter2);
        spin.setPadding(0, 0, 0, 0);
        spin.setOnItemSelectedListener(new MyOnItemSelectedListener3());
    }

    public void setupSpinner2(Spinner spin) {
        getartists();
        ArrayAdapter<String> spinnerArrayAdapter3 = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item,
                artistsList);
        spin.setAdapter(spinnerArrayAdapter3);
        spin.setOnItemSelectedListener(new MyOnItemSelectedListener2());
    }

    public void setupSpinner3(Spinner spin) {
        ArrayAdapter<CharSequence> adapter2 = new ArrayAdapter<CharSequence>(
                this, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.add("Sort");
        adapter2.add("title DESC");
        adapter2.add("title ASC");
        adapter2.add("artist DESC");
        adapter2.add("artist ASC");
        adapter2.add("album DESC");
        adapter2.add("album ASC");
        adapter2.add("rating ASC");
        adapter2.add("rating DESC");
        adapter2.add("PlayedCount ASC");
        adapter2.add("PlayedCount DESC");
        spin.setAdapter(adapter2);
        spin.setOnItemSelectedListener(new MyOnItemSelectedListener());
    }

    public class MyOnItemSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            if (pos != 0) {
                try {
                    sortorder = parent.getItemAtPosition(pos).toString();
                    DisplayData(searchterm, sortorder);
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class MyOnItemSelectedListener5 implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            String jumpto = null;
            try {
                jumpto = parent.getItemAtPosition(pos).toString();
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
            final ListView lv = getListView();
            int lk = 0;
            for (Music s : songs) {
                String title = " ";
                title = s.title;
                if (title != null) {
                    if (!title.isEmpty()) {
                        String debug = title.substring(0, 1);
                        if (debug.equals(jumpto)) {
                            lv.setSelection(lk);
                            return;
                        }
                    }
                }
                lk++;
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class MyOnItemSelectedListener2 implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            if (pos != 0) {
                try {
                    searchterm = "Artist="
                            + parent.getItemAtPosition(pos).toString();
                    sortorder = null;
                    DisplayData(searchterm, sortorder);
                    spin.setSelection(0);
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class MyOnItemSelectedListener3 implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            if (pos != 0) {
                try {
                    searchterm = "Album="
                            + parent.getItemAtPosition(pos).toString();
                    sortorder = null;
                    DisplayData(searchterm, sortorder);
                    spin2.setSelection(0);
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class MyOnItemSelectedListener4 implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            try {
                addtoplaylist = parent.getItemAtPosition(pos).toString();
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    private class AutoEditTags extends AsyncTask<Object, String, Boolean> {
        @Override
        protected void onCancelled() {

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {

            } else {

            }
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            SQLiteDatabase db = mydb.getWritableDatabase();
            Cursor cursor = getSongs(null, null, db);
            while (cursor.moveToNext()) {
                String strPCPath = cursor.getString(1);
                int ituneslength = itunes.length();
                String subpath = strPCPath.substring(ituneslength-19);
                String existingtitle = cursor.getString(2);
                String[] separated = subpath.split("/");
                if (existingtitle == null) {
                    String artist = separated[0];
                    String album = separated[1];
                    String title = separated[2];
                    title = title.substring(0, title.length() - 4);
                    title = title.replaceAll("[-]", "");
                    title = title.replaceAll("[0-9]", "");
                    title = title.replaceAll("[.]", "");
                    title = title.trim();
                    ContentValues values = new ContentValues();
                    values.put(EventDataSQLHelper.ARTIST, artist);
                    values.put(EventDataSQLHelper.TITLE, title);
                    values.put(EventDataSQLHelper.ALBUM, album);
                    db.update(EventDataSQLHelper.TABLE2, values,
                            EventDataSQLHelper.FILE + "=?",
                            new String[]{strPCPath});
                }
            }
            return true;
        }
    }

    private class DownloadTask extends AsyncTask<String, String, String> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            ProgressDialog dialog=ProgressDialog.show(PlayPhoneMusic.this, "Downloading First Song", "Downloading First Song. May Take a Few Seconds. Pleae Wait.");
            firstdownload = false;
    }

        @Override
        protected String doInBackground(String... params) {
            boolean download = false;
            if (params[1].equals("true")) {
                download = true;
            }
            int ituneslength = itunes.length();
            String subpath=null;
            String strPCPath=null;
            if (params[0].length()>ituneslength) {
                subpath = params[0].substring(ituneslength);
                strPCPath = params[0].substring(19);
            }
            else
            {
                return "2";
            }
            int localsongindex = songIndex;
            cd = new ConnectionDetector(getApplicationContext());
            downloadcomplete = false;
            // Check if Internet present
            if (!cd.isConnectingToInternet()) {
                // Internet Connection is not present
                runOnUiThread(new Runnable() {
                public void run() {
                    alert.showAlertDialog(PlayPhoneMusic.this,
                            "Internet Connection Error",
                            "Please connect to working Internet connection.\nPress OK to continue with Music on Phone.", false);
                    // stop executing code by return
                }
                });
                return "3";
            }
            if (source == 5) {
                return "4";
            } else {
                try {
                    SmbFile smbFileToDownload = null;
                    SharedPreferences Settings = getSharedPreferences(SETTINGS,
                            Context.MODE_PRIVATE);
                    String user = Settings.getString(USER, "ERROR");
                    String pass = Settings.getString(PASSWORD, "ERROR");
                    String IP2 = Settings.getString(IP, "192.168.1.67");
                    AuthenticationContext auth = new AuthenticationContext(user, pass.toCharArray(), "");
                    SmbConnection smbConnection = null;
                    try {
                        smbConnection = new SmbConnection(IP2, "Users2", auth);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    smbFileToDownload = new SmbFile(smbConnection, strPCPath);
                    InputStream inputStream = null;
                    try {
                        inputStream = smbFileToDownload.getInputStream();
                    } catch (NullPointerException e) {
                        return "2";
                    }
                    String localFilePathString = "";
                    if (download) {
                        String[] separated = subpath.split("/");
                        String artist = separated[0];
                        String album = separated[1];
                        File folder = new File(sdpath + artist + "/" + album);
                        boolean success = true;
                        if (!folder.exists()) {
                            success = folder.mkdirs();
                        }
                        if (success) {
                            localFilePathString = isStorageLocationWriteable();
                            localFilePathString += subpath;
                        } else {
                            // Do something else on failure
                        }
                    } else {//not downloading
                        String[] separated = subpath.split("/");
                        String title = separated[2];
                        File tempfile = new File(getFilesDir() + "/MyMusicPlayer/Temp/");
                        if (!tempfile.exists()) {
                            tempfile.mkdirs();
                        }
                        DeleteTempDir(tempfile);
                        localFilePathString = tempfile.getPath() + "/" + title;
                    }
                    File localFilePath = new File(localFilePathString);
                    final String finalLocalFilePathString = localFilePathString;
                    final boolean finalDownload = download;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // stuff that updates ui
                            btnDownload.setVisibility(View.INVISIBLE);
                            btnNext.setVisibility(View.INVISIBLE);
                            if (isLocationWriteable == FALSE && finalDownload == TRUE) {
                                CharSequence text = "Unable to write to your current Music Folder (" + sdpath + ").  \nHave copied the song to '" + finalLocalFilePathString + "' instead. \nPlease use a File Manager to copy to your Music Directory";
                                AlertDialog alertDialog = new AlertDialog.Builder(PlayPhoneMusic.this).create();
                                alertDialog.setTitle("WARNING!!!!!!!!");
                                alertDialog.setMessage(text);
                                alertDialog.setButton2("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                alertDialog.show();
                            }
                        }
                    });
                    OutputStream out = null;
                    out = new FileOutputStream(localFilePath);
                    int bufferSize = 5096;
                    byte[] b = new byte[bufferSize];
                    int noOfBytes = 0;
                    while ((noOfBytes = inputStream.read(b)) != -1) {
                        out.write(b, 0, noOfBytes);
                    }
                    out.flush();
                    out.close();
                    inputStream.close();
                    smbConnection.close();
                    downloadresult = "2";
                    downloadcomplete = true;
                    if (download) {
                        SQLiteDatabase db = mydb.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        String[] separated = subpath.split("/");
                        String artist = separated[0];
                        String album = separated[1];
                        String title = separated[2];
                        title = title.substring(0, title.length() - 4);
                        title = title.replaceAll("[-]", "");
                        title = title.replaceAll("[0-9]", "");
                        title = title.replaceAll("[.]", "");
                        title = title.trim();
                        values.put(EventDataSQLHelper.FILE, subpath);
                        values.put(EventDataSQLHelper.ARTIST, artist);
                        values.put(EventDataSQLHelper.TITLE, title);
                        values.put(EventDataSQLHelper.ALBUM, album);
                        values.put(EventDataSQLHelper.RATING, 0);
                        db.insert(EventDataSQLHelper.TABLE, null, values);
                    }
                    Settings = getSharedPreferences(SETTINGS,
                            Context.MODE_PRIVATE);
                    Editor editor = Settings.edit();
                    editor.putString(PLAY_NEXT, localFilePathString);
                    editor.putString(SMB_FILE, strPCPath);
                    Music song = new Music();
                    if (localsongindex > songs.size()) localsongindex = 1;
                    song = songs.get(localsongindex);
                    PlayLabel3 = song.display;
                    editor.putString(LAST_PLAYED, PlayLabel3);
                    editor.commit();
                    SQLiteDatabase db = mydb.getWritableDatabase();
                    String search1 = null;
                    String search2 = null;
                    search1 = EventDataSQLHelper.FILE + "=?";
                    search2 = subpath;
                    Cursor cursor = db.query(EventDataSQLHelper.TABLE2, null, search1,
                            new String[]{search2}, null, null, sortorder);
                    while (cursor.moveToNext()) {
                        String rating = cursor.getString(6);
                        if (rating.equals("0") || rating == null) {
                            UpDateTags(subpath, localFilePathString);
                        }
                    }
                    cursor.close();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // stuff that updates ui
                            btnDownload.setImageResource(R.drawable.success);
                            btnDownload.setVisibility(View.VISIBLE);
                            btnNext.setVisibility(View.VISIBLE);
                            if (PlayLabel.equals("No Songs in Memory, Downloading Now. Please Click The Forward Button when Downloading Completes.")) {
                                Random rand = new Random();
                                songIndex = rand.nextInt((songs.size() - 1) - 0 + 1) + 0;
                                Music song = new Music();
                                song = songs.get(songIndex);
                                String mFileName2 = song.file;
                                playsong(mFileName2);
                                btnPlay.setImageResource(R.drawable.btn_pause);
                            }
                            btnDownload.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    Toast.makeText(getBaseContext(), PlayLabel3,
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                    return "1";
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    return "2";
                } catch (IllegalArgumentException e) {
                    return "2";
                } catch (IllegalStateException e) {
                    return "2";
                }
            }
//            return result;
        }

        @Override
        protected void onPostExecute(String result) {
//            dialog.dismiss();
            if (result.equals("2")) {
                //hasn't worked, display dialog box
                downloadresult = "2";
                final AlertDialog alertDialog = new AlertDialog.Builder(PlayPhoneMusic.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert).create();
                alertDialog.setTitle("Cannot Find Computer");
                alertDialog.setMessage("Check the PC and Wifi are on, if not turn it on and press try again." +
                        "\n\nThe IP address may have changed." +
                        "\nPress Scan IP to search for the computer (Could take a long time)." +
                        "\n\nPress 'Enter IP' to enter it manually (Can be found in ES File Explorer)" +
                        "\nor to Switch to Music on Phone." +
                        "\n\nIf you would like to edit the database, click the back button and pause playback." +
                        "\n\nWill switch to Music on Phone if nothing selected after 60 seconds.");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Try Again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        downloading.cancel(true);
                        downloading = new DownloadTask().execute(mFileName2,
                                "false");
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Scan IP", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        ChangeIP();
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Enter IP", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        showDialog(CHANGEIP);
                    }
                });
                alertDialog.show();

                // Hide after some seconds
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (alertDialog.isShowing()) {
                            switchtophone();
                            alertDialog.dismiss();
                        }
                    }
                };

                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        handler.removeCallbacks(runnable);
                    }
                });

                handler.postDelayed(runnable, 60000);

            } else if (result.equals("3")) {
                source = 1;
                btnNext = (ImageButton) findViewById(R.id.btnNext);
                btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
                btnPrevious.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
                DisplayData(searchterm, sortorder);
                if (!mPlayer2.isPlaying()) {
                    Random rand = new Random();
                    songIndex = rand.nextInt((songs.size() - 1) - 0 + 1) + 0;
                    Music song = new Music();
                    song = songs.get(songIndex);
                    TextView sourcetext = (TextView) findViewById(R.id.textView1);
                    sourcetext.setText("Music On Phone");
                    String mFileName2 = song.file;
                    playsong(mFileName2);
                }
            }
        }
    }

    private class smbbackgroundrefresh extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog dialog;

        protected void onPreExecute() {
            this.dialog = new ProgressDialog(PlayPhoneMusic.this);
            this.dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            this.dialog.setMessage("Loading songs. Please wait...");
            this.dialog.setIndeterminate(true);
            this.dialog.setCanceledOnTouchOutside(false);
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (success == false) {

            }
        }

        protected Boolean doInBackground(String... strings) {
            SQLiteDatabase db = mydb.getWritableDatabase();
            SmbFile itunesmusic = null;
            SharedPreferences Settings = getSharedPreferences(SETTINGS,
                    Context.MODE_PRIVATE);
            String user = Settings.getString(USER, "ERROR");
            String pass = Settings.getString(PASSWORD, "ERROR");
            String IP2 = Settings.getString(IP, "192.168.1.67");
            AuthenticationContext auth = new AuthenticationContext(user, pass.toCharArray(), "");
            SmbConnection smbConnection = null;
            try {
                smbConnection = new SmbConnection(IP2, "Users2", auth);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<SmbFile> fileList = null;
            try {
                fileList = getsmbFiles(itunes.substring(19, itunes.length() - 1), smbConnection);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (fileList != null) { // check if dir is not null
                int ituneslength = itunes.length();
                for (SmbFile tmpf : fileList) {
                    String filename = tmpf.getPath();
                    if (filename.contains(".mp3") || filename.contains(".m4a")
                            || filename.contains("AAC")) {
                        String subpath = filename.substring(ituneslength - 19);
                        Cursor TestCursor = getSongs(EventDataSQLHelper.FILE
                                + "=" + subpath, null, db);
                        if (TestCursor.getCount() == 0) {
                            String album = null;
                            String artist = null;
                            String title = null;
                            int rating = 0;
                            String subpath2 = subpath.substring(0, subpath.length() - 4);
                            subpath2 = subpath2 + ".m4a";
                            String subpath3 = subpath.substring(subpath.length() - 4, subpath.length());
                            String[] separated = subpath.split("/");
                            artist = separated[0];
                            if (separated.length > 1) {
                                album = separated[1];
                            }
                            if (separated.length > 2) {
                                title = separated[2];
                                title = title.substring(0, title.length() - 4);
                                title = title.replaceAll("[-]", "");
                                title = title.replaceAll("[0-9]", "");
                                title = title.replaceAll("[.]", "");
                                title = title.trim();
                            }
                            insertSongRow(subpath, artist, title, album, rating);
                            if (subpath3.equals(".mp3")) {
                                db.delete(EventDataSQLHelper.TABLE2, EventDataSQLHelper.FILE
                                        + "=?", new String[]{subpath2});
                            }
                        }
                        TestCursor.close();
                    }
                }
                downloadcomplete = true;
                db.close();
                return true;
            } else {
                runOnUiThread(new Runnable() {
                    public void run() {
                        computerfound = 0;
                        final AlertDialog alertDialog = new AlertDialog.Builder(PlayPhoneMusic.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert).create();
                        alertDialog.setTitle("Cannot Find Computer");
                        alertDialog.setMessage("Check the PC is on, if not turn it on and press try again." +
                                "\n\nThe IP address may have changed." +
                                "\nPress Scan IP to search for the computer (Could take a long time)." +
                                "\n\nPress 'Enter IP' to enter it manually (Can be found in ES File Explorer)" +
                                "\nor to Switch to Music on Phone." +
                                "\n\nIf you would like to edit the database, click the back button and pause playback." +
                                "\n\nWill switch to Music on Phone if nothing selected after 60 seconds.");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Try Again", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                downloading.cancel(true);
                                downloading = new DownloadTask().execute(mFileName2,
                                        "false");
                            }
                        });
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Scan IP", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                ChangeIP();
                            }
                        });
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Enter IP", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showDialog(CHANGEIP);
                            }
                        });
                        alertDialog.show();
                        // Hide after some seconds
                        final Handler handler = new Handler();
                        final Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                if (alertDialog.isShowing()) {
                                    switchtophone();
                                    alertDialog.dismiss();
                                }
                            }
                        };

                        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                handler.removeCallbacks(runnable);
                            }
                        });

                        handler.postDelayed(runnable, 60000);
                    }
                });
                return true;
            }
        }
    }

    private class phonebackgroundrefresh extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog dialog;

        protected void onPreExecute() {
            this.dialog = new ProgressDialog(PlayPhoneMusic.this);
            this.dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            this.dialog.setMessage("Loading songs. Please wait...");
            this.dialog.setIndeterminate(true);
            this.dialog.setCanceledOnTouchOutside(false);
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (dialog.isShowing()) {
                dialog.dismiss();
                setupsong();
            }
        }

        protected Boolean doInBackground(String... strings) {
            int searched = 0;
            int found = 0;
            String sdcard = sdpath;
            int temp = sdpath.length();
            SQLiteDatabase db = mydb.getWritableDatabase();
            List<File> fileList = getListFiles(new File(sdcard));
            if (fileList != null) { // check if dir is not null
                for (File tmpf : fileList) {
                    String filename = tmpf.toString();
                    filename = filename.substring(temp);
                    if ((filename.contains(".mp3") || filename.contains(".m4a")) && !filename.startsWith("Temp")) {

                        Cursor TestCursor = getSongs(EventDataSQLHelper.FILE
                                + "=" + filename, null, db);
                        if (TestCursor.getCount() == 0) {
                            found = found + 1;
                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(sdpath + filename);
                            String album = mmr
                                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                            String artist = mmr
                                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                            String title = mmr
                                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                            int rating = 0;
                            if (title == null) {
                                String subpath = filename;
                                String[] separated = subpath.split("/");
                                if (separated.length > 2) {
                                    artist = separated[0];
                                    album = separated[1];
                                    title = separated[2];
                                    title = title.substring(0,
                                            title.length() - 4);
                                    title = title.replaceAll("[-]", "");
                                    title = title.replaceAll("[0-9]", "");
                                    title = title.replaceAll("[.]", "");
                                    title = title.trim();
                                }
                            }
                            if (title != null) {
                                insertSongRow(filename, artist, title, album,
                                        rating);
                            }
                        }
                        TestCursor.close();
                    }
                    searched = searched + 1;
                }
            }
            db.close();
            return true;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {

            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                }
                return true;
            case 87:
                if (action == 0) {
                    if (computerfound == 0) {
                        switchtophone();
                    } else {
                        forwardkeypressed();
                    }
                }
                return true;
            case 88:
                if (action == 0) {
                    if (source == 1 || source == 3) {
                        backkeypressed();
                    }
                }
                return true;
            case 127:
                if (action == 0) {
                    playbuttonclicked();
                }
                return true;
            case 126:
                if (action == 0) {
                    playbuttonclicked();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private void forwardkeypressed() {
        if (source == 2 || source == 4) {
            SharedPreferences Settings = getSharedPreferences(SETTINGS,
                    Context.MODE_PRIVATE);
            Editor editor = Settings.edit();
            editor.putInt(LAST_PLAYED_ID, songIndex);
            editor.commit();
        }
        // check if next song is there or not
        if (isShuffle) {
            // shuffle is on - play a random song
            Random rand = new Random();
            Music song = new Music();
            songIndex = rand.nextInt((songs.size() - 1) - 0 + 1) + 0;
            song = songs.get(songIndex);
            String mFileName3 = song.file;
            playsong(mFileName3);
        } else {
            if (songIndex < (songs.size() - 1)) {
                Music song = new Music();
                songIndex = songIndex + 1;
                song = songs.get(songIndex);
                String mFileName3 = song.file;
                playsong(mFileName3);
            } else {
                // play first song
                songIndex = 0;
                Music song = new Music();
                song = songs.get(songIndex);
                String mFileName3 = song.file;
                playsong(mFileName3);
            }
        }
    }

    private void backkeypressed() {
        if (!playedlist.isEmpty()) {
            Music song = new Music();
            if ((playedlist.size() - 1) > 0) {
                playedlist.remove(playedlist.size() - 1);
            }
            int myNum = 0;
            try {
                myNum = Integer.parseInt(playedlist.get(playedlist
                        .size() - 1));
            } catch (NumberFormatException nfe) {

            }
            if ((playedlist.size() - 1) > 0)
                playedlist.remove(playedlist.size() - 1);
            song = songs.get(myNum);
            songIndex = myNum;
            String mFileName2 = song.file;
            if (source == 1 || source == 3) {
                PlayLabel = song.display;
            }
            playsong(mFileName2);
        } else {
            // play last song
            songIndex = songs.size() - 1;
            Music song = new Music();
            song = songs.get(songIndex);
            String mFileName2 = song.file;
            playsong(mFileName2);
        }
    }

    private void playbuttonclicked() {
        // check for already playing
        if (mPlayer2.isPlaying()) {
            if (mPlayer2 != null) {
                mPlayer2.pause();
                // Changing button image to play button
                btnPlay.setImageResource(R.drawable.btn_play);
            }
        } else {
            // Resume song
            if (mPlayer2 != null) {
                mPlayer2.start();
                // Changing button image to pause button
                btnPlay.setImageResource(R.drawable.btn_pause);
            }
        }
    }

    private void switchtophone() {
        source = 1;
        searchterm = null;
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        btnPrevious.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
        TextView sourcetext = (TextView) findViewById(R.id.textView1);
        sourcetext.setText("Music On Phone");
        btnDownload.setVisibility(View.INVISIBLE);
        DisplayData(searchterm, sortorder);
        if (!mPlayer2.isPlaying()) {
            Random rand = new Random();
            songIndex = rand.nextInt((songs.size() - 1) - 0 + 1) + 0;
            Music song = new Music();
            song = songs.get(songIndex);
            String mFileName2 = song.file;
            playsong(mFileName2);
        }
    }

    String isStorageLocationWriteable() {
        ArrayList<String> writeableLocations = new ArrayList<>();
        File[] Dirs = ContextCompat.getExternalFilesDirs(this, null);
        for (File file : Dirs) {
            writeableLocations.add(file.getAbsolutePath());
        }
        int first = writeableLocations.get(0).indexOf("/");
        int second = writeableLocations.get(0).indexOf("/", first + 1);
        int third = writeableLocations.get(0).indexOf("/", second + 1);
        int pos = writeableLocations.get(0).indexOf("/", third + 1);
        String loc = writeableLocations.get(0).substring(0, pos + 1);
        writeableLocations.add(loc + "Music");
        first = writeableLocations.get(1).indexOf("/");
        second = writeableLocations.get(1).indexOf("/", first + 1);
        pos = writeableLocations.get(1).indexOf("/", second + 1);
        loc = writeableLocations.get(1).substring(0, pos + 1);
        writeableLocations.add(loc + "Music");
        int found = 0;
        for (String locat : writeableLocations) {
            if (sdpath.contains(locat)) {
                found = found + 1;
            }
        }
        if (found == 0) {
            isLocationWriteable = FALSE;
            return writeableLocations.get(1);
        } else {
            isLocationWriteable = TRUE;
            return sdpath;
        }
    }

    public class Music {
        String title;
        String artist;
        String album;
        String rating;
        String played;
        String file;
        String display;
        String id;
    }

    private class MusicListAdaptor extends ArrayAdapter<Music> {

        private ArrayList<Music> songs;

        public MusicListAdaptor(Context context,
                                int textViewResourceId,
                                ArrayList<Music> items) {
            super(context, textViewResourceId, items);
            this.songs = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.playlist_item, null);
            }
            Music o = songs.get(position);
            TextView title = (TextView) v.findViewById(R.id.titletext);
            TextView artist = (TextView) v.findViewById(R.id.artisttext);
            TextView album = (TextView) v.findViewById(R.id.albumtext);
            TextView rating = (TextView) v.findViewById(R.id.ratingtext);
            TextView played = (TextView) v.findViewById(R.id.playedtext);
            artist.setVisibility(View.VISIBLE);
            album.setVisibility(View.VISIBLE);
            rating.setVisibility(View.VISIBLE);
            played.setVisibility(View.VISIBLE);
            title.setText(o.title);
            artist.setText(o.artist);
            album.setText(o.album);
            String stars = null;
            if (!o.rating.isEmpty()) {
                stars = "";
                int in = Integer.valueOf(o.rating);
                for (int x = 0; x < in; x++) {
                    stars += "*";
                }
            }
            rating.setText(stars);
            played.setText("(" + o.played + ")");

            return v;
        }
    }
}