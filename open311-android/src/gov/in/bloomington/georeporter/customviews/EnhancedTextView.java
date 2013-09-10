
package gov.in.bloomington.georeporter.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import gov.in.bloomington.georeporter.R;

public class EnhancedTextView extends TextView {
    private Context context;
    private Typeface typeface;
    private String typefaceReference;

    public EnhancedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.EnhancedTextView,
                0, 0);

        typefaceReference = a.getString(R.styleable.EnhancedTextView_font);

        setFont(typefaceReference);

    }

    public void setFont(String typeFacePath)
    {
        if (!typefaceReference.contentEquals(typeFacePath))
            typefaceReference = typeFacePath;

        if (typefaceReference != null)
        {
            typeface = Typeface.createFromAsset(this.context.getAssets(), typefaceReference);
            if (typeface != null)
            {
                this.setTypeface(typeface);
            }
        }
    }

}
