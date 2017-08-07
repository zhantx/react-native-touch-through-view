package com.rome2rio.android.reactnativetouchthroughview;

import com.facebook.react.views.view.ReactViewGroup;
import com.wix.interactable.Interactable;
import com.wix.interactable.InteractableView;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Rect;
import android.view.ViewGroup;

public class TouchThroughWrapper extends ReactViewGroup {
    public TouchThroughWrapper(Context context) {
        super(context);
    }

    private boolean isChildIsInteractable = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Recursively find out if an absolute x/y position is hitting a child view and stop event
        // propagation if a hit is found.
        final int actionMasked = event.getAction() & MotionEvent.ACTION_MASK;
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            isChildIsInteractable = false;
        }

        return this.isTouchingTouchThroughView(this, Math.round(event.getX()), Math.round(event.getY()), event);
    }

    private boolean isTouchingTouchThroughView(ViewGroup viewgroup, int x, int y, MotionEvent event) {
        boolean isTouchingTouchThroughView = false;

        for(int i = 0; i < viewgroup.getChildCount(); i++) {
            View child = viewgroup.getChildAt(i);

            boolean isViewGroup = child instanceof ViewGroup;
            boolean isTouchThroughView = child instanceof TouchThroughView;

            if (isTouchThroughView) {
                int[] location = new int[2];
                int[] thisLocation = new int[2];

                child.getLocationOnScreen(location);
                this.getLocationOnScreen(thisLocation);

                int childX = location[0] - thisLocation[0];
                int childY = location[1] - thisLocation[1];

                Rect bounds = new Rect(childX, childY, childX + child.getWidth(), childY + child.getHeight());

                isTouchingTouchThroughView = bounds.contains(x, y);
            }
            else if (isViewGroup) {
                isTouchingTouchThroughView = this.isTouchingTouchThroughView((ViewGroup) child, x, y, event);
            }

            final int actionMasked = event.getAction() & MotionEvent.ACTION_MASK;
            if (actionMasked == MotionEvent.ACTION_DOWN) {
                this.isChildIsInteractable = (this.isChildIsInteractable || child instanceof InteractableView) && !isTouchingTouchThroughView;
            }

            if (isTouchingTouchThroughView) {
                break;
            }
        }

        if (this.isChildIsInteractable) {
            return false;
        }

        return isTouchingTouchThroughView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Pass through touch events to layer behind.
        return false;
    }
}
