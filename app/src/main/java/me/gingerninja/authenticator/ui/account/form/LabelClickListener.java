package me.gingerninja.authenticator.ui.account.form;

import android.view.View;

import me.gingerninja.authenticator.data.db.entity.Label;

public interface LabelClickListener {
    void onLabelAddClicked(View view);

    void onLabelRemoved(Label label);
}