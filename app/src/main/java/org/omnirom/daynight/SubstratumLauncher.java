package org.omnirom.daynight;

import android.app.Activity;
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

public class SubstratumLauncher extends Activity {

    private static final String SUBSTRATUM_PACKAGE_NAME = "projekt.substratum";
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
        if (isPackageInstalled(SUBSTRATUM_PACKAGE_NAME)) {
            if (!isPackageEnabled(SUBSTRATUM_PACKAGE_NAME)) {
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
        if (ENFORCE_MINIMUM_SUBSTRATUM_VERSION) {
            try {
                PackageInfo packageInfo = getApplicationContext()
                        .getPackageManager().getPackageInfo(SUBSTRATUM_PACKAGE_NAME, 0);
                if (packageInfo.versionCode >= MINIMUM_SUBSTRATUM_VERSION) {
                    Intent intent = SubstratumLoader.launchThemeActivity(getApplicationContext(),
                            getIntent(), getString(R.string.ThemeName), getPackageName());
                    startActivity(intent);
                    finish();
                } else {
                    showOutdatedSubstratumToast();
                }
            } catch (Exception e) {
                showOutdatedSubstratumToast();
            }
            finish();
        } else {
            Intent intent = SubstratumLoader.launchThemeActivity(getApplicationContext(),
                    getIntent(), getString(R.string.ThemeName), getPackageName());
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launch();
    }

    private void launch() {
        beginSubstratumLaunch();
    }
}