package org.omnirom.daynight;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import projekt.substrate.SubstratumLoader;

import static org.omnirom.daynight.ThemerConstants.BLACKLISTED_APPLICATIONS;
import static org.omnirom.daynight.ThemerConstants.ENABLE_BLACKLISTED_APPLICATIONS;
import static org.omnirom.daynight.ThemerConstants.ENFORCE_MINIMUM_SUBSTRATUM_VERSION;
import static org.omnirom.daynight.ThemerConstants.MINIMUM_SUBSTRATUM_VERSION;
import static org.omnirom.daynight.ThemerConstants.SUBSTRATUM_FILTER_CHECK;

public class OmniSubstratumLauncher extends Activity {

    private static final String OMNI_SUBSTRATUM_PACKAGE_NAME = "org.omnirom.substratum";

    private Boolean mVerified = false;

    private boolean isPackageInstalled(String package_name) {
        try {
            PackageManager pm = getPackageManager();
            pm.getPackageInfo(package_name, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPackageEnabled(String package_name) {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(package_name, 0);
            return ai.enabled;
        } catch (Exception e) {
            return false;
        }
    }

    private void beginSubstratumLaunch() {
        // If Substratum is found, then launch it with specific parameters
        if (isPackageInstalled(OMNI_SUBSTRATUM_PACKAGE_NAME)) {
            if (!isPackageEnabled(OMNI_SUBSTRATUM_PACKAGE_NAME)) {
                Toast.makeText(this, getString(R.string.toast_substratum_frozen),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // Substratum is found, launch it directly
            launchSubstratum();
        } else {
            getSubstratumFromPlayStore();
        }
    }

    private void getSubstratumFromPlayStore() {
        String playURL = "https://play.google.com/store/apps/details?id=projekt.substratum";
        Intent i = new Intent(Intent.ACTION_VIEW);
        Toast.makeText(this, getString(R.string.toast_substratum), Toast.LENGTH_SHORT).show();
        i.setData(Uri.parse(playURL));
        startActivity(i);
        finish();
    }

    private void showOutdatedSubstratumToast() {
        String parse = String.format(
                getString(R.string.outdated_substratum),
                getString(R.string.ThemeName),
                String.valueOf(MINIMUM_SUBSTRATUM_VERSION));
        Toast.makeText(this, parse, Toast.LENGTH_SHORT).show();
    }

    private void launchSubstratum() {
        if (ENABLE_BLACKLISTED_APPLICATIONS) {
            for (String blacklistedApplication : BLACKLISTED_APPLICATIONS)
                if (isPackageInstalled(blacklistedApplication)) {
                    Toast.makeText(this, R.string.unauthorized,
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
        }
        if (SUBSTRATUM_FILTER_CHECK && !mVerified) {
            Toast.makeText(this, R.string.unauthorized,
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        try {
            if (isPackageInstalled(OMNI_SUBSTRATUM_PACKAGE_NAME)) {
                Intent intent = launchOmniThemeActivity(getApplicationContext(),
                        getIntent(), getString(R.string.ThemeName), getPackageName());
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            showOutdatedSubstratumToast();
        }

        finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launch();
    }

    private void launch() {
        beginSubstratumLaunch();
    }

    private Intent launchOmniThemeActivity(Context context, Intent originalIntent, String theme_name, String theme_pid) {
        String theme_mode = originalIntent.getStringExtra("theme_mode");
        if (theme_mode == null) {
            theme_mode = "";
        }

        boolean theme_legacy = originalIntent.getBooleanExtra("theme_legacy", false);
        boolean refresh_mode = originalIntent.getBooleanExtra("refresh_mode", false);
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(ComponentName.unflattenFromString("org.omnirom.substratum/projekt.substratum.OmniActivity"));
        intent.setFlags(268435456);
        intent.addFlags(67108864);
        intent.putExtra("theme_name", theme_name);
        intent.putExtra("theme_pid", theme_pid);
        intent.putExtra("theme_legacy", theme_legacy);
        intent.putExtra("theme_mode", theme_mode);
        intent.putExtra("refresh_mode", refresh_mode);

        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(theme_pid, 128);
            String plugin = ai.metaData.getString("Substratum_Plugin");
            intent.putExtra("plugin_version", plugin);
        } catch (Exception var10) {
            ;
        }

        return intent;
    }
}