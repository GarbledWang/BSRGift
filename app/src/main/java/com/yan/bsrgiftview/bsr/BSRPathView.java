package com.yan.bsrgiftview.bsr;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yan on 2016/12/8.
 */
public class BSRPathView extends BSRPathBase {
    private View view;
    private BSREvaluator.OnValueBackListener backListener;
    private List<OnAnmEndListener> endListeners;
    private BSRGiftLayout bsrGiftLayout;
    private Interpolator interpolator;

    public void setChild(View child) {
        view = child;
    }

    public View getChild() {
        return view;
    }

    public BSRPathView() {
        super();
        endListeners = new ArrayList<>();

    }

    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    public void startBsrAnimation(OnAnmEndListener endListener, final float alphaTrigger) {
        BSREvaluator bsrEvaluator = new BSREvaluator();
        if (alphaTrigger != -1) {
            if (backListener == null) {
                backListener = new BSREvaluator.OnValueBackListener() {
                    @Override
                    public void onValueBack(float value) {
                        if (value > alphaTrigger) {
                            view.setAlpha(((1 / (1 - alphaTrigger)) * (1 - value)));
                        }
                    }
                };
            }
            bsrEvaluator.setBackListener(backListener);
        }

        endListeners.add(endListener);
        ValueAnimator anim = ValueAnimator.ofObject(bsrEvaluator, this);
        anim.setInterpolator((interpolator == null ? new AccelerateDecelerateInterpolator() : interpolator));
        anim.setDuration(during);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                screenWidth = bsrGiftLayout.getMeasuredWidth();
                screenHeight = bsrGiftLayout.getMeasuredHeight();

                float timesWidth = screenWidth / (float) view.getMeasuredWidth();
                float timesHeight = screenHeight / (float) view.getMeasuredHeight();

                view.setPivotX(xPercent * view.getWidth());
                view.setPivotY(yPercent * view.getHeight());

                float tempScale = 1;

                if (isCenterInside) {
                    if (timesWidth > timesHeight) {
                        tempScale = timesHeight * scaleInScreen;
                        view.setX(screenWidth - view.getWidth() * timesHeight * scaleInScreen / 2);
                    } else {
                        tempScale = timesWidth * scaleInScreen;
                        view.setY(screenHeight - view.getHeight() * timesWidth * scaleInScreen / 2);
                    }
                } else if (scaleInScreen != -10000) {
                    if (timesWidth > timesHeight) {
                        tempScale = timesHeight * scaleInScreen;
                    } else {
                        tempScale = timesWidth * scaleInScreen;
                    }
                }

                if (!isCenterInside) {
                    view.setX(truePointX - view.getWidth() * xPositionPercent);
                    view.setY(truePointY - view.getHeight() * yPositionPercent);
                }

                view.setScaleX(((trueScaleValue != -1) ? trueScaleValue : 1) * tempScale);
                view.setScaleY(((trueScaleValue != -1) ? trueScaleValue : 1) * tempScale);

                if (trueRotation == -10000) {
                    if (lastPoint == null) {
                        lastPoint = new PointF();
                        lastPoint.set(truePointX, truePointY);
                    }
                    float degree = getRotationPoint2Point(lastPoint.x, lastPoint.y, truePointX, truePointY);
                    view.setRotation(degree + getFirstRotation());

                    lastPoint.set(truePointX, truePointY);
                } else {
                    view.setRotation(trueRotation);
                }

                if (onAnimationStartListener != null) {
                    onAnimationStartListener.onAnimationStart();
                }
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                for (OnAnmEndListener endListener : endListeners) {
                    endListener.onAnimationEnd(BSRPathView.this);
                }
            }
        });
        anim.start();
    }

    public void addEndListeners(OnAnmEndListener endListener) {
        this.endListeners.add(endListener);
    }

    OnAnimationStartListener onAnimationStartListener;

    public void setOnAnimationStartListener(OnAnimationStartListener onAnimationStartListener) {
        this.onAnimationStartListener = onAnimationStartListener;
    }

    public void attachParent(BSRGiftLayout bsrGiftLayout) {
        this.bsrGiftLayout = bsrGiftLayout;
    }

    public interface OnAnimationStartListener {
        void onAnimationStart();
    }

}