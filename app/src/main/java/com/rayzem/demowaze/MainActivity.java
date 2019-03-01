package com.rayzem.demowaze;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static int OVERLAY_PERMISSION_REQ_CODE_CHATHEAD = 1234;
    public static int OVERLAY_PERMISSION_REQ_CODE_CHATHEAD_MSG = 5678;
    //public FloatingActionButton btnStartWaze;

    public Button btnStartWaze;
    public TextView txtView;

    private Toolbar mTopToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Always cast your custom Toolbar here, and set it as the ActionBar.


        // Get the ActionBar here to configure the way it behaves.
        /*final ActionBar ab = getSupportActionBar();
        //ab.setHomeAsUpIndicator(R.drawable.ic_menu); // set a custom icon for the default home button
        ab.setDisplayShowHomeEnabled(true); // show or hide the default home button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
        ab.setDisplayShowTitleEnabled(false); // disable the default title element here (for centered title)*/


        mTopToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);

        txtView = findViewById(R.id.txtView);
        btnStartWaze = findViewById(R.id.btnStartWaze);
        /*btnStartWaze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Starting waze app ...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                if(canDrawOverlays(MainActivity.this)) {
                    startFloatingBubbleService();
                    MainActivity.this.moveTaskToBack(true);
                    openWazeApp();
                    txtView.setText("Clicked!");

                }else {
                    requestPermission(OVERLAY_PERMISSION_REQ_CODE_CHATHEAD);
                }
            }
        });*/

        btnStartWaze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Starting waze app ...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                if(canDrawOverlays(MainActivity.this)) {
                    startFloatingBubbleService();
                    MainActivity.this.moveTaskToBack(true);
                    openWazeApp();


                }else {
                    requestPermission(OVERLAY_PERMISSION_REQ_CODE_CHATHEAD);
                }
            }
        });
    }


    private void startFloatingBubbleService() {
        startService(new Intent(MainActivity.this, FloatingBubbleService.class));
    }

    private void requestPermission(int requestCode){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, requestCode);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OVERLAY_PERMISSION_REQ_CODE_CHATHEAD) {
            if (canDrawOverlays(MainActivity.this)) {
                needPermissionDialog(requestCode);
            }else{
                startFloatingBubbleService();
            }

        }
    }

    private void needPermissionDialog(final int requestCode){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("You need to allow permission");
        builder.setPositiveButton("OK",
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        requestPermission(requestCode);
                    }
                });
        builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static boolean canDrawOverlays(Context context){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        else
            return Settings.canDrawOverlays(context);
    }


    public void openWazeApp() {
        //Format
        //https://www.waze.com/livemap?ll=-2.9001285%2C-79.0058965&from=-2.168974%2C-79.8397207&at=now

        /*String url = String.format("https://www.waze.com/livemap?ll=%s%2C%s&from=%s%2C%s&at=now");
        try{

            Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
            startActivity( intent );
        }catch ( ActivityNotFoundException ex  ){
            // If Waze is not installed, open it in Google Play:
            Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( "market://details?id=com.waze" ) );
            startActivity(intent);
        }*/


        /*Intent intent = getPackageManager().getLaunchIntentForPackage("com.waze");
        if (intent != null) {
            // We found the activity now start the activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        } else {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id=" + "com.waze"));
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        }*/

        //com.waze
        /*try {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.waze");
            // Launch Waze to look for Hawaii:
            *//*String url = "https://waze.com/ul?q=Hawaii";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);*//*
        } catch (ActivityNotFoundException ex) {
            // If Waze is not installed, open it in Google Play:
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
            startActivity(intent);
        }*/
    }
}
