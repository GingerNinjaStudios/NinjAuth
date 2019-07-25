package me.gingerninja.authenticator.data;

import androidx.annotation.DrawableRes;

import me.gingerninja.authenticator.R;

public class LabelIconLinker {
    public static final String[] ICONS = {"google", "github", "gitlab", "bitbucket", "aws", "heart", "school", "work", "money", "face_happy", "face_sad", "thumbs_up", "thumbs_down", "time", "puzzle", "lock", "palette"};

    @DrawableRes
    public static int getIconResourceId(@androidx.annotation.Nullable String icon) {
        if (icon == null) {
            return 0;
        }

        switch (icon) {
            case "google":
                return R.drawable.label_icon_google;
            case "github":
                return R.drawable.label_icon_github;
            case "gitlab":
                return R.drawable.label_icon_gitlab;
            case "bitbucket":
                return R.drawable.label_icon_bitbucket;
            case "aws":
                return R.drawable.label_icon_aws;
            case "heart":
                return R.drawable.label_icon_heart;
            case "school":
                return R.drawable.label_icon_school;
            case "work":
                return R.drawable.label_icon_work;
            case "money":
                return R.drawable.label_icon_money;
            case "face_happy":
                return R.drawable.label_icon_face_happy;
            case "face_sad":
                return R.drawable.label_icon_face_sad;
            case "thumbs_up":
                return R.drawable.label_icon_thumbs_up;
            case "thumbs_down":
                return R.drawable.label_icon_thumbs_down;
            case "time":
                return R.drawable.label_icon_time;
            case "palette":
                return R.drawable.ic_palette_24dp;
            case "lock":
                return R.drawable.ic_lock_24dp;
            case "puzzle":
                return R.drawable.ic_extension_24dp;
            default:
                return 0;
        }
    }
}
