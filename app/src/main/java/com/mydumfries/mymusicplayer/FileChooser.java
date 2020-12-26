package com.mydumfries.mymusicplayer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hierynomus.smbj.auth.AuthenticationContext;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.swaechter.smbjwrapper.SmbConnection;
import ch.swaechter.smbjwrapper.SmbDirectory;
import ch.swaechter.smbjwrapper.SmbFile;
import ch.swaechter.smbjwrapper.SmbItem;

public class FileChooser extends ListActivity {
    private static final int REQUEST_PERMISSIONS = 453;
    EventDataSQLHelper mydb;
    public static final String SETTINGS = "Settings";
    public static final String USER = "User";
    public static final String PASSWORD = "Password";
    public static final String IP = "IP";
    public static final String SOURCE = "source";
    private FileArrayAdapter adapter;
    public static final String PCLOCATION = "PCLocation";
    public static final String PHONELOCATION = "PhoneLocation";
    private File f;
    private int STORAGE_PERMISSION_CODE = 23;
    String source;
    String user;
    String pass;
    String IP3;
    private static final int NETPASS_DIALOG_ID = 1;
    private ConnectionDetector cd;
    AlertDialogManager alert = new AlertDialogManager();
    List<Option> dir = new ArrayList<Option>();
    List<Option> rootdir = null;
    List<Option> fls = new ArrayList<Option>();
    SmbFile[] servers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String path = getIntent().getStringExtra(IP);
        source = getIntent().getStringExtra(SOURCE);
        mydb = new EventDataSQLHelper(this);
        try {
            fill(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int fill(final String path) throws IOException {
        File[] dirs = new File[1];
        File[] dirs2;
        if (isReadStorageAllowed()) {
            if (source.equals("1")) {
                if (path.equals("")) {
                    dirs2 = getExternalFilesDirs(Environment.DIRECTORY_MUSIC);
                    String c = dirs2[1].getPath();
                    File g = new File(c);
                    dirs[0] = g;
                    g.exists();
                } else {
                    f = new File(path);
                    dirs = f.listFiles();
                }
                List<Option> dir = new ArrayList<Option>();
                List<Option> fls = new ArrayList<Option>();
                try {
                    for (File ff : dirs) {
                        if (ff.isDirectory()) {
                            dir.add(new Option(ff.getName(), "Folder", ff.getAbsolutePath()));
                        } else {
                            fls.add(new Option(ff.getName(), "File Size: " + ff.length(), ff.getAbsolutePath()));
                        }
                    }
                } catch (Exception e) {
                }
                Collections.sort(dir);
                Collections.sort(fls);
                dir.addAll(fls);
                if (!path.equals("")) {
                    String test = f.getName();
                    if (test.equals("0")) {
                        dir.add(0, new Option("..", "Parent Directory", ""));
                    } else if (!f.getName().equalsIgnoreCase("storage")) {
                        String h = f.getParent();
                        if (h.equals("/storage")) {
                            dir.add(0, new Option("..", "Parent Directory", ""));
                        } else {
                            dir.add(0, new Option("..", "Parent Directory", f.getParent()));
                        }
                    }
                }
                adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view, dir);
                this.setListAdapter(adapter);
            } else {
//source==2 NETWORK
                SharedPreferences Settings = getSharedPreferences(SETTINGS,
                        Context.MODE_PRIVATE);
                user = Settings.getString(USER, "ERROR");
                pass = Settings.getString(PASSWORD, "ERROR");
                if (user == "ERROR" || pass == "ERROR") {
                    showDialog(NETPASS_DIALOG_ID);
                }
                cd = new ConnectionDetector(getApplicationContext());
                // Check if Internet present
                if (!cd.isConnectingToInternet()) {
                    // Internet Connection is not present
                    alert.showAlertDialog(FileChooser.this,
                            "Internet Connection Error",
                            "Please connect to working Internet connection.", false);
                } else {
                    final ProgressDialog dialog;
                    dialog = new ProgressDialog(FileChooser.this);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setMessage("Searching for Network Computers. Please wait...");
                    dialog.setIndeterminate(true);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                    new Thread(new Runnable() {
                        public void run() {
                            final AuthenticationContext auth = new AuthenticationContext(user, pass.toCharArray(), "");
                            if (path.equals("root")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (dialog.isShowing()) {
                                            dialog.dismiss();
                                        }
                                        adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view, rootdir);
                                        h.sendEmptyMessage(0);
                                    }
                                });
                            }
                            if (path.equals("")) {
                                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                WifiInfo connectionInfo = wm.getConnectionInfo();
                                int ipAddress = connectionInfo.getIpAddress();
                                String ipString = Formatter.formatIpAddress(ipAddress);
                                String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
                                boolean reachable = false;
                                InetAddress address = null;
                                String testIp = null;
                                for (int i = 0; i < 255; i++) {
                                    testIp = prefix + String.valueOf(i);
                                    try {
                                        address = InetAddress.getByName(testIp);
                                    } catch (UnknownHostException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        reachable = address.isReachable(100);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    if (reachable) {
                                        String hostName = address.getCanonicalHostName();
                                        String path3 = "smb://" + testIp;
                                        dir.add(new Option(hostName, "Folder", path3));
                                    }
                                }
                                Collections.sort(dir);
                                rootdir = new ArrayList<Option>(dir);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (dialog.isShowing()) {
                                            dialog.dismiss();
                                        }
                                        adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view, dir);
                                        h.sendEmptyMessage(0);
                                    }
                                });
                            } else if (!path.equals("root") && !path.equals("")) {
                                SharedPreferences Settings = getSharedPreferences(SETTINGS,
                                        Context.MODE_PRIVATE);
                                IP3 = Settings.getString(IP, "192.168.1.67");
                                String path4="";
                                if (!path.contains("smb://")) {
                                    path4 = path;
                                }
                                else
                                {
                                    IP3=path.substring(6);
                                }
                                dir.clear();
                                fls.clear();
                                SmbConnection smbConnection = null;
                                try {
                                     smbConnection = new SmbConnection(IP3, "Users2", auth);
//                                    smbConnection = new SmbConnection(IP3, "Root", auth);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                SmbDirectory rootDirectory;
                                if (path4.length()>0) {
                                    rootDirectory = new SmbDirectory(smbConnection, path4);
                                }else{
                                    rootDirectory = new SmbDirectory(smbConnection);
                                }
                                for (SmbDirectory smbDirectory : rootDirectory.getDirectories()) {
                                    dir.add(new Option(smbDirectory.getName(), "Folder", smbDirectory.getPath()));
                                }
                                for (SmbFile smbFile : rootDirectory.getFiles()) {
                                        fls.add(new Option(smbFile.getName(), "File Size: " + smbFile.getFileSize(), smbFile.getPath()));
                                }
                                Collections.sort(dir);
                                Collections.sort(fls);
                                dir.addAll(fls);
                                if (path.contains("smb://")) {
                                    dir.add(0, new Option("..", "Parent Directory", "root"));
                                } else if (!rootDirectory.getName().equalsIgnoreCase("smb://"))
                                    dir.add(0, new Option("..", "Parent Directory", rootDirectory.getName()));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (dialog.isShowing()) {
                                            dialog.dismiss();
                                        }
                                        adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view, dir);
                                        h.sendEmptyMessage(0);
                                    }
                                });
                            }
                        }
                    }).start();
                }
            }
        }else {
                    requestStoragePermission(source);
                }
                return 2;
            }

            final Handler h = new Handler() {
                public void handleMessage(Message msg) {
                    setListAdapter(adapter);
                }
            };

            @Override
            protected void onListItemClick (ListView l, View v,int position, long id){
                // TODO Auto-generated method stub
                super.onListItemClick(l, v, position, id);
                Option o = adapter.getItem(position);
                if (o.getData().equalsIgnoreCase("folder") || o.getData().equalsIgnoreCase("parent directory")) {
                    try {
                        fill(o.getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    onFileClick(o);
                }
            }

            private void onFileClick (Option o){
                if (source.equals("2")) {
                    String thePath = o.getPath();
                    String[] separated = thePath.split("/");
                    String PCpath=thePath;
                    int length = separated.length;
                    PCpath = PCpath.replace(separated[length - 1], "");
                    PCpath = PCpath.replace(separated[length - 2], "");
                    PCpath = PCpath.replace(separated[length - 3], "");
                    PCpath = PCpath.replace("///", "");
                    SharedPreferences Settings = getSharedPreferences(SETTINGS,
                            Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = Settings.edit();
                    editor.putString(PCLOCATION, PCpath);
                    editor.putString(IP, IP3);
                    editor.commit();
                }
                if (source.equals("1")) {
                    String filename = o.getPath();
                    filename = filename.replace("%2F", "/");
                    filename = filename.replace("%20", " ");
                    String thePath = filename;
                    String[] separated = thePath.split("/");
                    int length;
                    String PCpath = thePath;
                    length = separated.length;
                    PCpath = PCpath.replace(separated[length - 1], "");
                    PCpath = PCpath.replace(separated[length - 2], "");
                    PCpath = PCpath.replace(separated[length - 3], "");
                    PCpath = PCpath.replace("///", "");
                    String folder = PCpath;
                    SharedPreferences Settings = getSharedPreferences(SETTINGS,
                            Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = Settings.edit();
                    editor.putString(PHONELOCATION, folder);
                    editor.commit();
                }
                Intent returnIntent = new Intent(FileChooser.this, PlayPhoneMusic.class);
                returnIntent.putExtra("result", o.getPath());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
            private boolean isReadStorageAllowed () {
                int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (result == PackageManager.PERMISSION_GRANTED)
                    return true;
                //If permission is not granted returning false
                return false;
            }
            private void requestStoragePermission (String source){

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //If the user has denied the permission previously your code will come to this block
                }

                //And finally ask for the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            }

            //This method will be called when the user will tap on allow or deny
            @Override
            public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults)
            {

                //Checking the request code of our request
                if (requestCode == STORAGE_PERMISSION_CODE) {

                    //If permission is granted
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        try {
                            fill(source);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //Displaying a toast
                    } else {
                        //Displaying another toast if permission is not granted
                    }
                }
            }
}