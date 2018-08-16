package com.example.lenovo.note.util;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

/**
 * Created by Lenovo on 2018/8/12.
 */

public class AnimationUtil {
    public static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();

    public static void animateOut(View view, Interpolator interpolator,
                                  ViewPropertyAnimatorListener viewPropertyAnimatorListener) {
        ViewCompat.animate(view)
                .translationY(view.getHeight() + getMarginBottom(view))
                .setInterpolator(interpolator).withLayer()
                .setListener(viewPropertyAnimatorListener).start();
    }

    public static void animateIn(View view,Interpolator interpolator,
                                 ViewPropertyAnimatorListener viewPropertyAnimatorListener) {
        view.setVisibility(View.VISIBLE);
        ViewCompat.animate(view)
                .translationY(0)
                .setInterpolator(interpolator)
                .withLayer()
                .setListener(viewPropertyAnimatorListener)
                .start();
    }

    public static int getMarginBottom(View v) {
        int marginBottom = 0;
        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }
}
