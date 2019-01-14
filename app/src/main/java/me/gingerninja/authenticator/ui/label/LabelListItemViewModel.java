package me.gingerninja.authenticator.ui.label;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import me.gingerninja.authenticator.data.db.entity.Label;

public class LabelListItemViewModel {
    @NonNull
    private final Label label;

    public LabelListItemViewModel(@NonNull Label label) {
        this.label = label;
    }

    public String getName() {
        return label.getName();
    }

    @ColorInt
    public int getColor() {
        return label.getColor();
    }
}
