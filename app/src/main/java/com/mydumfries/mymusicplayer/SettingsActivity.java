package com.mydumfries.mymusicplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends Activity {
    EventDataSQLHelper mydb;
    public static final String SETTINGS = "Settings";
    private static final int NETPASS_DIALOG_ID = 1;
    public static final String PHONELOCATION = "PhoneLocation";
    public static final String USER = "User";
    public static final String PASSWORD = "Password";
    public static final String IP = "IP";
    public static final String SOURCE = "source";
    private static final int FILE_SELECT_CODE = 0;
    String user;
    String pass;
    String sdpath;
    String folder;
    ListView listview;
    TextView help;
    TextView network;
    TextView phonelocation;
    TextView pclocation;
    TextView reset;
    TextView backupdb;
    TextView restoredb;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        mydb = new EventDataSQLHelper(this);
        listview = (ListView) findViewById(R.id.listview2);
        backupdb = (TextView) findViewById(R.id.MenuItem_BackUpDB);
        backupdb.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BackUpDataBase();
            }
        });
        restoredb = (TextView) findViewById(R.id.MenuItem_RestoreDB);
        restoredb.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                RestoreDataBase();
            }
        });
        help = (TextView) findViewById(R.id.MenuItem_Help);
        help.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent player = new Intent(SettingsActivity.this, HelpActivity.class);
                startActivity(player);
            }
        });
        network = (TextView) findViewById(R.id.MenuItem_NetworkID);
        network.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(NETPASS_DIALOG_ID);
            }
        });
        phonelocation = (TextView) findViewById(R.id.MenuItem_PhoneLocation);
        phonelocation.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(SettingsActivity.this);
                builder2.setMessage("Please choose any song from your Music Folder.\nSongs should be stored in the format Artist/Album/song.mp3.")
                        .setPositiveButton("OK", dialogClickListener)
                        .setNegativeButton("Close", dialogClickListener).show();
            }
        });
        pclocation = (TextView) findViewById(R.id.MenuItem_PCLocation);
        pclocation.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                pcbuttonclicked();
            }
        });
        reset = (TextView) findViewById(R.id.MenuItem_Reset);
        reset.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(SettingsActivity.this);
                builder2.setMessage("Are you sure you want to delete ALL SETTINGS and return to \"Factory Settings\"?")
                        .setPositiveButton("OK", dialogClickListener3)
                        .setNegativeButton("Close", dialogClickListener3).show();
            }
        });
    }

    public void Destroy(Bundle savedInstanceState) {
        finish();
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case NETPASS_DIALOG_ID:
                LayoutInflater Passinflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View Passlayout = Passinflater.inflate(R.layout.netpass_dialog,
                        (ViewGroup) findViewById(R.id.root));
                final EditText netuser = (EditText) Passlayout
                        .findViewById(R.id.EditText_USER);
                final EditText netpass = (EditText) Passlayout
                        .findViewById(R.id.EditText_PASSWORD);
                // ... other required overrides do nothing
                AlertDialog.Builder Passbuilder = new AlertDialog.Builder(this);
                Passbuilder.setView(Passlayout);
                // Now configure the AlertDialog
                Passbuilder.setTitle("Please Input Your User Name & Password");
                Passbuilder
                        .setMessage("Please enter your Windows User Name\n (if this is your e-mail address, it is usually just the part before the '@')\n and Password.");
                Passbuilder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        });
                Passbuilder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                pass = netpass.getText().toString();
                                user = netuser.getText().toString();
                                SharedPreferences Settings = getSharedPreferences(
                                        SETTINGS, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = Settings.edit();
                                editor.putString(USER, user);
                                editor.putString(PASSWORD, pass);
                                editor.commit();
                            }
                        });
                // Create the AlertDialog and return it
                AlertDialog PassDialog = Passbuilder.create();
                return PassDialog;
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
            case NETPASS_DIALOG_ID:
                return;
        }
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    showsongchooser(1, null);
                    SharedPreferences Settings = getSharedPreferences(SETTINGS,
                            Context.MODE_PRIVATE);
                    folder = Settings.getString(PHONELOCATION, "ERROR");
                    sdpath = folder;
                    if (folder.equals("ERROR")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                        builder.setMessage("Please use \n\"Location of Phone Music\"\nfirst to tell me were your DataBase file is located.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do things
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                        Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        SettingsActivity.this.finish();
                    }
                    SQLiteDatabase db = mydb.getWritableDatabase();
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
                        if (cursor.getCount() > 0) {
                            db.execSQL("delete from "
                                    + EventDataSQLHelper.TABLE);
                        }
                        cursor.close();
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    break;
            }
        }
    };

    DialogInterface.OnClickListener dialogClickListener2 = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    showsongchooser(2, "");
                    SharedPreferences Settings = getSharedPreferences(SETTINGS,
                            Context.MODE_PRIVATE);
                    folder = Settings.getString(PHONELOCATION, "ERROR");
                    sdpath = folder;
                    if (folder.equals("ERROR")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                        builder.setMessage("Please use \n\"Location of Phone Music\"\nfirst to tell me were your DataBase file is located.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do things
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                        Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        SettingsActivity.this.finish();
                    }
                    SQLiteDatabase db = mydb.getWritableDatabase();
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
                        if (cursor.getCount() > 0) {
                        }
                        cursor.close();
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    finish();
                    break;
            }
        }
    };

    DialogInterface.OnClickListener dialogClickListener3 = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    SharedPreferences Settings = getSharedPreferences(SETTINGS,
                            Context.MODE_PRIVATE);
                    String folder = Settings.getString(PHONELOCATION, "ERROR");
                    if (folder.equals("ERROR")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                        builder.setMessage("Please use \n\"Location of Phone Music\"\nfirst to tell me were your music is located.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do things
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        String pathtodb = folder + "songs.db";
                        File fdelete = new File(pathtodb);
                        if (fdelete.exists()) {
                            if (fdelete.delete()) {
                            } else {
                            }
                        }
                        pathtodb = folder + "songs.db-journal";
                        fdelete = new File(pathtodb);
                        if (fdelete.exists()) {
                            if (fdelete.delete()) {
                            } else {
                            }
                        }
                        SharedPreferences.Editor editor = Settings.edit();
                        editor.clear();
                        editor.commit();
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    break;
            }
        }
    };

    void showsongchooser(int source, final String path) {
        if (source == 2 || source == 4) {
            Intent player = new Intent(SettingsActivity.this, FileChooser.class);
            player.putExtra(IP, "");
            player.putExtra(SOURCE, "2");
            startActivityForResult(player, 1);
        } else {
            Intent player = new Intent(SettingsActivity.this, FileChooser.class);
            player.putExtra(IP, "");
            player.putExtra(SOURCE, "1");
            startActivityForResult(player, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                Toast.makeText(getBaseContext(), "Location of Music on the Phone updated.",
                        Toast.LENGTH_LONG).show();
                break;
            case 1:
                Toast.makeText(getBaseContext(), "Location of Music on the Computer updated.",
                        Toast.LENGTH_LONG).show();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void pcbuttonclicked() {
        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                Context.MODE_PRIVATE);
        user = Settings.getString(USER, "ERROR");
        pass = Settings.getString(PASSWORD, "ERROR");
        String folder = Settings.getString(PHONELOCATION, "ERROR");
        if (folder.equals("ERROR")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
            builder.setMessage("Please use \n\"Location of Phone Music\"\nfirst to tell me were your DataBase file is located.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else if (user == "ERROR" || pass == "ERROR") {
            showDialog(NETPASS_DIALOG_ID);
        } else {
            AlertDialog.Builder builder2 = new AlertDialog.Builder(SettingsActivity.this);
            builder2.setMessage("Please choose any song from your Music Folder.\nSongs should be stored in the format Artist/Album/song.mp3.")
                    .setPositiveButton("OK", dialogClickListener2)
                    .setNegativeButton("Close", dialogClickListener2).show();
        }
    }

    private void RestoreDataBase() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            String currentDBPath = "//data//"
                    + "com.mydumfries.mymusicplayer" + "//databases//"
                    + "songs.db";
            String backupDBPath = "songs.db";
            File currentDB = new File(data, currentDBPath);
            File backupDB = new File(sd, backupDBPath);
            FileChannel dst = new FileInputStream(backupDB).getChannel();
            File currentpath = new File(currentDB.getParent());
            currentpath.mkdirs();
            FileChannel src = new FileOutputStream(currentDB).getChannel();
            src.transferFrom(dst, 0, dst.size());
            src.close();
            dst.close();
            Toast.makeText(getBaseContext(), backupDB.toString(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void BackUpDataBase() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String currentDBPath = "//data//"
                        + "com.mydumfries.mymusicplayer" + "//databases//"
                        + "songs.db";
                String backupDBPath = "songs.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB)
                            .getChannel();
                    FileChannel dst = new FileOutputStream(backupDB)
                            .getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getApplicationContext(),
                            "Database Restored successfully",
                            Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG)
                    .show();
        }
    }
}