package me.gingerninja.authenticator.ui.base;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.RoundedCornerTreatment;
import com.google.android.material.shape.ShapeAppearanceModel;

import me.gingerninja.authenticator.R;

public class RoundedBottomSheetDialogFragment extends BottomSheetDialogFragment {
    protected MaterialShapeDrawable shapeDrawable;

    private Drawable createBackground(Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.colorSurface, typedValue, true);
        TypedArray arr = context.obtainStyledAttributes(typedValue.data, new int[]{
                R.attr.colorSurface});

        int color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            color = arr.getColor(0, context.getColor(R.color.colorAccent));
        } else {
            color = arr.getColor(0, arr.getResources().getColor(R.color.colorAccent));
        }

        arr.recycle();

        /*int cornerRadius = context.getResources().getDimensionPixelSize(R.dimen.bottom_sheet_corner_radius);
        int topPadding = context.getResources().getDimensionPixelSize(R.dimen.bottom_sheet_top_padding);

        GradientDrawable shapeDrawable = new GradientDrawable();
        shapeDrawable.setCornerRadii(new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius, 0, 0, 0, 0});
        shapeDrawable.setColor(color);*/

        shapeDrawable = new MaterialShapeDrawable();
        shapeDrawable.setFillColor(ColorStateList.valueOf(color));

        ShapeAppearanceModel shapeModel = shapeDrawable.getShapeAppearanceModel();
        shapeModel.setBottomLeftCorner(new NoCornerTreatment());
        shapeModel.setBottomRightCorner(new NoCornerTreatment());

        shapeDrawable.setCornerRadius(context.getResources().getDimensionPixelSize(R.dimen.bottom_sheet_corner_radius));

        return shapeDrawable;

        //return new InsetDrawable(shapeDrawable, 0, topPadding, 0, 0);
    }

    protected void createRoundedView(View view) {
        view.setBackground(createBackground(view.getContext()));
    }

    private static class NoCornerTreatment extends RoundedCornerTreatment {
        /**
         * Instantiates a no-corner treatment.
         */
        private NoCornerTreatment() {
            super(0);
        }

        @Override
        public void setCornerSize(float cornerSize) {
            // do nothing, the corner size is always 0
        }
    }
}
