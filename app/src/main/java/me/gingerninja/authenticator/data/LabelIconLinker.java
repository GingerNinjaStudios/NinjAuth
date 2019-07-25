package me.gingerninja.authenticator.data;

import androidx.annotation.DrawableRes;

import me.gingerninja.authenticator.R;

public class LabelIconLinker {
    public static final String[] ICONS = {"google", "facebook", "twitter", "tumblr", "twitch",
            "dropbox", "github", "gitlab", "bitbucket", "aws", "slack", "discord", "sentry", "face",
            "person", "group", "heart", "school", "work", "hot", "cloud", "money", "shopping_cart",
            "ubisoft", "video_game", "face_happy", "face_sad", "thumbs_up", "thumbs_down", "time",
            "puzzle", "lock", "palette"};

    @DrawableRes
    public static int getIconResourceId(@androidx.annotation.Nullable String icon) {
        if (icon == null) {
            return 0;
        }

        switch (icon) {
            case "google":
                return R.drawable.label_icon_google;
            case "facebook":
                return R.drawable.label_icon_facebook;
            case "twitter":
                return R.drawable.label_icon_twitter;
            case "tumblr":
                return R.drawable.label_icon_tumblr;
            case "twitch":
                return R.drawable.label_icon_twitch;
            case "dropbox":
                return R.drawable.label_icon_dropbox;
            case "slack":
                return R.drawable.label_icon_slack;
            case "discord":
                return R.drawable.label_icon_discord;
            case "sentry":
                return R.drawable.label_icon_sentry;
            case "github":
                return R.drawable.label_icon_github;
            case "gitlab":
                return R.drawable.label_icon_gitlab;
            case "bitbucket":
                return R.drawable.label_icon_bitbucket;
            case "aws":
                return R.drawable.label_icon_aws;
            case "face":
                return R.drawable.label_icon_face;
            case "person":
                return R.drawable.label_icon_person;
            case "group":
                return R.drawable.label_icon_group;
            case "heart":
                return R.drawable.label_icon_heart;
            case "school":
                return R.drawable.label_icon_school;
            case "work":
                return R.drawable.label_icon_work;
            case "hot":
                return R.drawable.label_icon_hot;
            case "cloud":
                return R.drawable.label_icon_cloud;
            case "money":
                return R.drawable.label_icon_money;
            case "shopping_cart":
                return R.drawable.label_icon_shopping_cart;
            case "ubisoft":
                return R.drawable.label_icon_ubisoft;
            case "video_game":
                return R.drawable.label_icon_video_game;
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
