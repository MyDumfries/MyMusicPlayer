package com.mydumfries.mymusicplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import ch.swaechter.smbjwrapper.SmbFile;

public class MyMusicPlayerActivity extends Activity {
    static final int ALARM_DIALOG_ID = 0;
    EventDataSQLHelper mydb;
    Alarm alarm = new Alarm();
    private ConnectionDetector cd;
    AlertDialogManager alert = new AlertDialogManager();
    FileArrayAdapter adapter;
    public static final String SETTINGS = "Settings";
    public static final String PCLOCATION = "PCLocation";
    public static final String PHONELOCATION = "PhoneLocation";
    public static final String USER = "User";
    public static final String PASSWORD = "Password";
    public static final String IP = "IP";
    public static final String SOURCE = "source";
    private static final int FILE_SELECT_CODE = 0;
    private static final int NETPASS_DIALOG_ID = 1;
    SmbFile[] servers;
    //    String pathtosdcard = Environment.getExternalStorageDirectory().getPath();
    String pathtosdcard = "/storage/";
    private Button phonebutton;
    private Button pcbutton;
    private Button playlistbutton;
    private Button alarmbutton;
    private Button skydrivebutton;
    private Button settingsbutton;
    String user;
    String pass;
    List<Option> dir = new ArrayList<Option>();
    List<Option> fls = new ArrayList<Option>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setup();
    }

    public void setup() {
        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                Context.MODE_PRIVATE);
        mydb = new EventDataSQLHelper(this);
        String folder = Settings.getString(PHONELOCATION, "ERROR");
        if (folder.equals("ERROR")) {
            AlertDialog.Builder builder2 = new AlertDialog.Builder(MyMusicPlayerActivity.this);
            builder2.setMessage("First, I need to known where your Music Folder is.\nPlease choose any song from your Music Folder.\nSongs should be stored in the format Artist/Album/song.mp3.")
                    .setPositiveButton("OK", dialogClickListener).show();
        }
        alarm.SetAlarm(getApplicationContext());
        phonebutton = (Button) findViewById(R.id.phone_button);
        phonebutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences Settings = getSharedPreferences(SETTINGS,
                        Context.MODE_PRIVATE);
                String folder = Settings.getString(PHONELOCATION, "ERROR");
                if (folder.equals("ERROR")) {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(MyMusicPlayerActivity.this);
                    builder2.setMessage("Please choose any song from your Music Folder.\nSongs should be stored in the format Artist/Album/song.mp3.")
                            .setPositiveButton("OK", dialogClickListener)
                            .setNegativeButton("Close", dialogClickListener).show();
                } else {
                    Intent player = new Intent(MyMusicPlayerActivity.this, PlayPhoneMusic.class);
                    player.putExtra("source", 1);
                    startActivity(player);
                }
            }
        });
        pcbutton = (Button) findViewById(R.id.pc_button);
        pcbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pcbuttonclicked();
            }
        });
        playlistbutton = (Button) findViewById(R.id.playlist_button);
        playlistbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences Settings = getSharedPreferences(SETTINGS,
                        Context.MODE_PRIVATE);
                String folder = Settings.getString(PHONELOCATION, "ERROR");
                if (folder.equals("ERROR")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MyMusicPlayerActivity.this);
                    builder.setMessage("Please use \n\"Location of Phone Music\"\nfirst to tell me were your Music is located.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do things
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    Intent player = new Intent(MyMusicPlayerActivity.this, PlayListActivity.class);
                    startActivity(player);
                }
            }
        });
        alarmbutton = (Button) findViewById(R.id.SetAlarmButton);
        alarmbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(ALARM_DIALOG_ID);
            }
        });
        settingsbutton = (Button) findViewById(R.id.SettingsButton);
        settingsbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent player = new Intent(MyMusicPlayerActivity.this, SettingsActivity.class);
                startActivity(player);
            }
        });
        skydrivebutton = (Button) findViewById(R.id.skydrive_button);
        skydrivebutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CharSequence text = "I am working on integrating Google Drive into the app.\n" +
                        "In the meantime please click to \n" +
                        "Launch Google Play Music";
                AlertDialog alertDialog = new AlertDialog.Builder(v.getContext()).create();
                alertDialog.setTitle("Google Drive");
                alertDialog.setMessage(text);
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // here you can add functions
                        Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.music");
                        startActivity(intent);
                    }
                });
                alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.show();
            }
        });
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case ALARM_DIALOG_ID:
                final String ALARMYESNO = "AlarmYesNo";
                final TimePicker tpResult;
                final String HOUR = "Hour";
                final String MINUTE = "Minute";
                final String SATURDAY = "Saturday";
                final String SUNDAY = "Sunday";
                final String MONDAY = "Monday";
                final String TUESDAY = "Tuesday";
                final String WEDNESDAY = "Wednesday";
                final String THURSDAY = "Thursday";
                final String FRIDAY = "Friday";
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
                AlarmToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = Settings.edit();
                        if (arg1) {
                            editor.putString(ALARMYESNO, "yes");
                            editor.commit();
                        } else {
                            editor.putString(ALARMYESNO, "no");
                            editor.commit();
                        }
                    }
                });
                Mon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = Settings.edit();
                        if (arg1) {
                            editor.putString(MONDAY, "yes");
                            editor.commit();
                        } else {
                            editor.putString(MONDAY, "no");
                            editor.commit();
                        }
                    }
                });
                Tue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = Settings.edit();
                        if (arg1) {
                            editor.putString(TUESDAY, "yes");
                            editor.commit();
                        } else {
                            editor.putString(TUESDAY, "no");
                            editor.commit();
                        }
                    }
                });
                Wed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = Settings.edit();
                        if (arg1) {
                            editor.putString(WEDNESDAY, "yes");
                            editor.commit();
                        } else {
                            editor.putString(WEDNESDAY, "no");
                            editor.commit();
                        }
                    }
                });
                Thu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = Settings.edit();
                        if (arg1) {
                            editor.putString(THURSDAY, "yes");
                            editor.commit();
                        } else {
                            editor.putString(THURSDAY, "no");
                            editor.commit();
                        }
                    }
                });
                Fri.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = Settings.edit();
                        if (arg1) {
                            editor.putString(FRIDAY, "yes");
                            editor.commit();
                        } else {
                            editor.putString(FRIDAY, "no");
                            editor.commit();
                        }
                    }
                });
                Sat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = Settings.edit();
                        if (arg1) {
                            editor.putString(SATURDAY, "yes");
                            editor.commit();
                        } else {
                            editor.putString(SATURDAY, "no");
                            editor.commit();
                        }
                    }
                });
                Sun.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                        // TODO Auto-generated method stub
                        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = Settings.edit();
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

                            }
                        });
                builder3.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                int hour = tpResult.getCurrentHour();
                                int min = tpResult.getCurrentMinute();
                                SharedPreferences Settings = getSharedPreferences(
                                        SETTINGS, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = Settings.edit();
                                editor.putInt(HOUR, hour);
                                editor.putInt(MINUTE, min);
                                editor.commit();
                                Alarm alarm = new Alarm();
                                alarm.CancelAlarm(getApplicationContext());
                                alarm.SetAlarm(getApplicationContext());
                            }
                        });
                // Create the AlertDialog and return it
                AlertDialog sleepDialog = builder3.create();
                return sleepDialog;
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
                                pcbuttonclicked();
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
            case ALARM_DIALOG_ID:
                return;
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
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    finish();
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
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    finish();
                    break;
            }
        }
    };

    void showsongchooser(int source, final String path) {
        if (source == 2 || source == 4) {
            Intent player = new Intent(MyMusicPlayerActivity.this, FileChooser.class);
            player.putExtra(IP, "");
            player.putExtra(SOURCE, "2");
            startActivityForResult(player, 1);
        } else {
            Intent player = new Intent(MyMusicPlayerActivity.this, FileChooser.class);
            player.putExtra(IP, "");
            player.putExtra(SOURCE, "1");
            startActivityForResult(player, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                Intent player = new Intent(MyMusicPlayerActivity.this, PlayPhoneMusic.class);
                player.putExtra("source", 1);
                startActivity(player);
                break;
            case 1:
                Intent player2 = new Intent(MyMusicPlayerActivity.this, PlayPhoneMusic.class);
                player2.putExtra("source", 2);
                startActivity(player2);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void pcbuttonclicked() {
        SharedPreferences Settings = getSharedPreferences(SETTINGS,
                Context.MODE_PRIVATE);
        String folder = Settings.getString(PHONELOCATION, "ERROR");
        if (folder.equals("ERROR")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MyMusicPlayerActivity.this);
            builder.setMessage("Please use \n\"Location of Phone Music\"\nfirst to tell me were your DataBase file is located.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            String pcfolder = Settings.getString(PCLOCATION, "ERROR");
            user = Settings.getString(USER, "ERROR");
            pass = Settings.getString(PASSWORD, "ERROR");
            if (user == "ERROR" || pass == "ERROR") {
                showDialog(NETPASS_DIALOG_ID);
            } else if (pcfolder.equals("ERROR")) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(MyMusicPlayerActivity.this);
                builder2.setMessage("Please choose any song from your Music Folder.\nSongs should be stored in the format Artist/Album/song.mp3.\n\nWARNING!!!! - This will take some time!")
                        .setPositiveButton("OK", dialogClickListener2)
                        .setNegativeButton("Close", dialogClickListener2).show();
            } else {
                Intent player = new Intent(MyMusicPlayerActivity.this, PlayPhoneMusic.class);
                player.putExtra("source", 2);
                startActivity(player);
            }
        }
    }
}