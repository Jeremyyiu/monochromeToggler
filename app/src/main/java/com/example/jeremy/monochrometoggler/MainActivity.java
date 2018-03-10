package com.example.jeremy.monochrometoggler;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.gc.materialdesign.views.Switch;

public class MainActivity extends AppCompatActivity {

    public static final String ACCESSIBILITY_DISPLAY_DALTONIZER = "accessibility_display_daltonizer";
    public static final String ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED = "accessibility_display_daltonizer_enabled";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();


        Switch monochromeSwitch = (Switch) findViewById(R.id.monochromeSwitch);
        initMonochromeSwitch(monochromeSwitch);
    }

    private void initMonochromeSwitch(final Switch monochromeSwitch) {
        monochromeToggle();

        monochromeSwitch.setOncheckListener(new Switch.OnCheckListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onCheck(Switch view, boolean check) {
                //Checks if this app can modify system settings
                boolean canWriteSettings = context.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED;

                if (canWriteSettings) {
                    if (check) {
                        toggleMonochrome(1, context.getContentResolver());
                        Toast.makeText(context, "1", Toast.LENGTH_SHORT).show();
                    } else {
                        toggleMonochrome(0, context.getContentResolver());

                        Toast.makeText(context, "0", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //If currently cant modify system settings, app will ask for permission
                    showRootWorkaroundInstructions(context);
                    monochromeSwitch.setChecked(false);
                }
            }
        });
    }

    public void showRootWorkaroundInstructions(final Context context) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("Since your phone is not rooted, you must manually grant the permission " +
                "'android.permission.WRITE_SECURE_SETTINGS' by going to adb and inputting the following command" +
                " adb -d shell pm grant com.example.jeremy.monochrometoggler android.permission.WRITE_SECURE_SETTINGS")
                .setCancelable(false)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Copy the command", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setClipboard(context, "adb -d shell pm grant com.example.jeremy.monochrometoggler android.permission.WRITE_SECURE_SETTINGS");
                        dialog.cancel();
                    }
                });
        android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * @param context
     * @param text    - Copy the text passed in the parameters onto the clipboard
     */
    private void setClipboard(Context context, String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    private boolean monochromeStatus() {
        if (Settings.Secure.getInt(getContentResolver(), ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED, 0) == 0) {
            return false;
        }
        return true;
    }

    /**
     * Checks the status of the monochrome - toggles the switch
     */
    private void monochromeToggle() {
        Switch monochromeSwitch = findViewById(R.id.monochromeSwitch);
        boolean monochromeStatus = monochromeStatus();
        monochromeSwitch.setChecked(monochromeStatus);
    }

    public static void toggleMonochrome(int value, ContentResolver contentResolver) {
        Settings.Secure.putInt(contentResolver, ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED, value);
        if (value == 0) {
            Settings.Secure.putInt(contentResolver, ACCESSIBILITY_DISPLAY_DALTONIZER, -1);
        } else if (value == 1) {
            Settings.Secure.putInt(contentResolver, ACCESSIBILITY_DISPLAY_DALTONIZER, 0);
        }
    }
}
