package com.example.lenovo.note.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.AttributeSet;
import android.view.View;

import com.example.lenovo.note.util.AnimationUtil;

/**
 * Created by Lenovo on 2018/8/12.
 */

public class ScrollAwareFABBehavior extends FloatingActionButton.Behavior {
    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollAwareFABBehavior() {
        super();
    }

    private boolean mIsAnimatingOut = false;

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout,
                                       FloatingActionButton child,
                                       View directTargetChild,
                                       View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
                || super.onStartNestedScroll(coordinatorLayout, child,
                directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child,
                               View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target,
                dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyConsumed > 0 && !this.mIsAnimatingOut && child.getVisibility() == View.VISIBLE) {
            AnimationUtil.animateOut(child,AnimationUtil.INTERPOLATOR,new ViewPropertyAnimatorListener() {
                public void onAnimationStart(View view) {
                    ScrollAwareFABBehavior.this.mIsAnimatingOut = true;
                }

                public void onAnimationCancel(View view) {
                    ScrollAwareFABBehavior.this.mIsAnimatingOut = false;
                }

                public void onAnimationEnd(View view) {
                    ScrollAwareFABBehavior.this.mIsAnimatingOut = false;
                    view.setVisibility(View.INVISIBLE);
                }
            });
        } else if (dyConsumed < 0 && child.getVisibility() == View.INVISIBLE) {
            AnimationUtil.animateIn(child,AnimationUtil.INTERPOLATOR,null);
        }
    }
}
