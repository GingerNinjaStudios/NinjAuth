package me.gingerninja.authenticator.util.license;

import android.content.Context;

import de.psdev.licensesdialog.licenses.License;
import me.gingerninja.authenticator.R;

public class CreativeCommonsAttribution40International extends License {
    @Override
    public String getName() {
        return "Creative Commons Attribution 4.0 International";
    }

    @Override
    public String readSummaryTextFromResources(Context context) {
        return getContent(context, R.raw.ccby_40_summary);
    }

    @Override
    public String readFullTextFromResources(Context context) {
        return getContent(context, R.raw.ccby_40_full);
    }

    @Override
    public String getVersion() {
        return "4.0";
    }

    @Override
    public String getUrl() {
        return "https://creativecommons.org/licenses/by/4.0/";
    }
}
