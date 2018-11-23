package me.gingerninja.authenticator.ui.base;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import me.gingerninja.authenticator.R;

public class RoundedBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private static Drawable createBackground(Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true);
        TypedArray arr = context.obtainStyledAttributes(typedValue.data, new int[]{
                android.R.attr.colorBackground});

        int color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            color = arr.getColor(0, context.getColor(R.color.colorAccent));
        } else {
            color = arr.getColor(0, arr.getResources().getColor(R.color.colorAccent));
        }

        arr.recycle();

        int cornerRadius = context.getResources().getDimensionPixelSize(R.dimen.bottom_sheet_corner_radius);
        int topPadding = context.getResources().getDimensionPixelSize(R.dimen.bottom_sheet_top_padding);

        GradientDrawable shapeDrawable = new GradientDrawable();
        shapeDrawable.setCornerRadii(new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius, 0, 0, 0, 0});
        shapeDrawable.setColor(color);

        return new InsetDrawable(shapeDrawable, 0, topPadding, 0, 0);
    }

    protected void createRoundedView(View view) {
        view.setBackground(createBackground(view.getContext()));
    }
}
