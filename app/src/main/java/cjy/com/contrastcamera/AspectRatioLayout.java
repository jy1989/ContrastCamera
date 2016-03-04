package cjy.com.contrastcamera;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.orhanobut.logger.Logger;

/**
 * Created by jhernandez on 20/08/2015.
 * <p/>
 * Vista que mantiene aspecto dado sin deformar (ancho / alto)
 * Usado en la cámara personalizada de la app
 */
public class AspectRatioLayout extends FrameLayout {
    private double mTargetAspect = -1.0;        // initially use default window size

    public AspectRatioLayout(Context context) {
        super(context);
    }

    public AspectRatioLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Sets the desired aspect ratio.  The value is <code>width / height</code>.
     */
    public void setAspectRatio(double aspectRatio) {
        if (aspectRatio < 0) {
            throw new IllegalArgumentException();
        }
        Logger.d("Setting aspect ratio to " + aspectRatio + " (was " + mTargetAspect + ")");
        if (mTargetAspect != aspectRatio) {
            mTargetAspect = aspectRatio;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Logger.e("onMeasure target=" + mTargetAspect +
        //        " width=[" + MeasureSpec.toString(widthMeasureSpec) +
        //       "] height=[" + MeasureSpec.toString(heightMeasureSpec) + "]");

        // Target aspect ratio will be < 0 if it hasn't been set yet.  In that case,
        // we just use whatever we've been handed.
        if (mTargetAspect > 0) {
            int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
            int initialHeight = MeasureSpec.getSize(heightMeasureSpec);

            // factor the padding out
            int horizPadding = getPaddingLeft() + getPaddingRight();
            int vertPadding = getPaddingTop() + getPaddingBottom();
            initialWidth -= horizPadding;
            initialHeight -= vertPadding;

            double viewAspectRatio = (double) initialWidth / initialHeight;
            double aspectDiff = mTargetAspect / viewAspectRatio - 1;

            if (Math.abs(aspectDiff) < 0.01) {
                // We're very close already.  We don't want to risk switching from e.g. non-scaled
                // 1280x720 to scaled 1280x719 because of some floating-point round-off error,
                // so if we're really close just leave it alone.
                // Logger.e("aspect ratio is good (target=" + mTargetAspect +
                //        ", view=" + initialWidth + "x" + initialHeight + ")");
            } else {
                if (aspectDiff > 0) {
                    // limited by narrow width; restrict height
                    initialHeight = (int) (initialWidth / mTargetAspect);
                } else {
                    // limited by short height; restrict width
                    initialWidth = (int) (initialHeight * mTargetAspect);
                }
                //Logger.e("new size=" + initialWidth + "x" + initialHeight + " + padding " +
                //         horizPadding + "x" + vertPadding);
                initialWidth += horizPadding;
                initialHeight += vertPadding;
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY);
            }
        }

        //if (Utils.DEBUG) Log.d(TAG, "set width=[" + MeasureSpec.toString(widthMeasureSpec) +
        //        "] height=[" + View.MeasureSpec.toString(heightMeasureSpec) + "]");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}