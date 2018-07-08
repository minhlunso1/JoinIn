package com.bluebirdaward.joinin.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.bluebirdaward.joinin.net.UserClient;
import com.bluebirdaward.joinin.utils.Util;

/**
 * Created by Minh on 4/22/2016.
 */
public class ExplosionM extends View {

    private List<ExplosionAnimator> mExplosions = new ArrayList<>();
    private int[] mExpandInset = new int[2];

    public ExplosionM(Context context) {
        super(context);
        init();
    }

    public ExplosionM(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExplosionM(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Arrays.fill(mExpandInset, Util.dp2Px(32));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (ExplosionAnimator explosion : mExplosions) {
            explosion.draw(canvas);
        }
    }

    public void expandExplosionBound(int dx, int dy) {
        mExpandInset[0] = dx;
        mExpandInset[1] = dy;
    }

    public void explode(Bitmap bitmap, Rect bound, long startDelay, long duration) {
        final ExplosionAnimator explosion = new ExplosionAnimator(this, bitmap, bound);
        explosion.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mExplosions.remove(animation);
            }
        });
        explosion.setStartDelay(startDelay);
        explosion.setDuration(duration);
        mExplosions.add(explosion);
        explosion.start();
    }

    public void explode(final View view, boolean disappear) {
        Rect r = new Rect();
        view.getGlobalVisibleRect(r);
        int[] location = new int[2];
        getLocationOnScreen(location);
        r.offset(-location[0], -location[1]);
        r.inset(-mExpandInset[0], -mExpandInset[1]);
        int startDelay = 100;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(150);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            Random random = new Random();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setTranslationX((random.nextFloat() - 0.5f) * view.getWidth() * 0.05f);
                view.setTranslationY((random.nextFloat() - 0.5f) * view.getHeight() * 0.05f);

            }
        });
        animator.start();
        if (disappear)
            view.animate().setDuration(150).setStartDelay(startDelay).scaleX(0f).scaleY(0f).alpha(0f).start();
        else {
            view.animate().setDuration(100).setStartDelay(startDelay).scaleX(1f).scaleY(1f).alpha(0f).start();
            view.animate().cancel();
        }
        explode(Util.createBitmapFromView(view), r, startDelay, ExplosionAnimator.DEFAULT_DURATION);
    }

    public void explodeWithLogout(final View view, final View view2, final View view3) {
        explodeWithM1(view, view2, view3);
    }

    private void explodeWithM1(final View view, final View view2, final View view3) {
        explode(view2, true);
        Rect r = new Rect();
        view.getGlobalVisibleRect(r);
        int[] location = new int[2];
        getLocationOnScreen(location);
        r.offset(-location[0], -location[1]);
        r.inset(-mExpandInset[0], -mExpandInset[1]);
        int startDelay = 100;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(150);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            Random random = new Random();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setTranslationX((random.nextFloat() - 0.5f) * view.getWidth() * 0.05f);
                view.setTranslationY((random.nextFloat() - 0.5f) * view.getHeight() * 0.05f);

            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                explodeWithM2(view3);
            }
        });
        animator.start();
        view.animate().setDuration(150).setStartDelay(startDelay).scaleX(0f).scaleY(0f).alpha(0f).start();
        explode(Util.createBitmapFromView(view), r, startDelay, ExplosionAnimator.DEFAULT_DURATION);
    }

    private void explodeWithM2(final View view3) {
        Rect r = new Rect();
        view3.getGlobalVisibleRect(r);
        int[] location = new int[2];
        getLocationOnScreen(location);
        r.offset(-location[0], -location[1]);
        r.inset(-mExpandInset[0], -mExpandInset[1]);
        int startDelay = 100;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(150);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            Random random = new Random();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view3.setTranslationX((random.nextFloat() - 0.5f) * view3.getWidth() * 0.05f);
                view3.setTranslationY((random.nextFloat() - 0.5f) * view3.getHeight() * 0.05f);

            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                UserClient.logOut(getContext());
            }
        });
        animator.start();
        view3.animate().setDuration(150).setStartDelay(startDelay).scaleX(0f).scaleY(0f).alpha(0f).start();
        explode(Util.createBitmapFromView(view3), r, startDelay, ExplosionAnimator.DEFAULT_DURATION);
    }

    public void clear() {
        mExplosions.clear();
        invalidate();
    }

    public static ExplosionM attach2Window(Activity activity) {
        ViewGroup rootView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        ExplosionM explosionField = new ExplosionM(activity);
        rootView.addView(explosionField, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return explosionField;
    }

}
