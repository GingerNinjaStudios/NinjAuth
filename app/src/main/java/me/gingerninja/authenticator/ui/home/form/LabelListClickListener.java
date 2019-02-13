package me.gingerninja.authenticator.ui.home.form;

import android.view.View;

import me.gingerninja.authenticator.data.db.entity.Label;

public interface LabelListClickListener {
    void onNewLabelClicked(View view);

    void onLabelSelected(Label label);
}
