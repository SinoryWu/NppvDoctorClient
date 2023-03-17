package com.hzdq.nppvdoctorclient.util;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * Time:2023/3/16
 * Author:Sinory
 * Description:view出现消失渐变动画
 */
public class AnimationUtil {
    public static void fadeIn(final View view) {
        if (view.getVisibility() == View.VISIBLE) {
            return;
        }
        Animation animation = new AlphaAnimation(0F, 1F);
        animation.setDuration(300);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }

    public static void fadeOut(final View view) {
        if (view.getVisibility() != View.VISIBLE) {
            return;
        }

        // Since the button is still clickable before fade-out animation
        // ends, we disable the button first to block click.
        view.setEnabled(false);
        Animation animation = new AlphaAnimation(1F, 0F);
        animation.setDuration(300);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation);
    }
}
