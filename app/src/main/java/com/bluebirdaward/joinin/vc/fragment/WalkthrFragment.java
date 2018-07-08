package com.bluebirdaward.joinin.vc.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluebirdaward.joinin.R;
import com.bluebirdaward.joinin.vc.activity.WalkthrActivity;

public class WalkthrFragment extends Fragment {
    private static String IMG_ID = "imgId";
    private static String TIT_ID = "titId";
    private static String POS = "pos";

    /* Each fragment has got an R reference to the image it will display
     * an R reference to the title it will display, and an R reference to the
     * string content.
     */
    private ImageView image;
    private int imageResId;
    public TextView title;
    private int titleResId;

    private int pos;

    public static WalkthrFragment newInstance(int imageResId, int titleResId, int pos) {
        final WalkthrFragment f = new WalkthrFragment();
        final Bundle args = new Bundle();
        args.putInt(IMG_ID, imageResId);
        args.putInt(TIT_ID, titleResId);
        args.putInt(POS, pos);
        f.setArguments(args);
        return f;
    }

    // Empty constructor, required as per Fragment docs
    public WalkthrFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.imageResId = arguments.getInt(IMG_ID);
            this.titleResId = arguments.getInt(TIT_ID);
            this.pos = arguments.getInt(POS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Identify and set fields!
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_walkthr, container, false);
        image = (ImageView) rootView.findViewById(R.id.walkthr_screen_image);
        title = (TextView) rootView.findViewById(R.id.walkthr_screen_title);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Get the font
//        Typeface roboto_light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf"); // Remember to add the font to your assets folder!
//        title.setTypeface(roboto_light);
        // Populate fields with info!
        if (WalkthrActivity.class.isInstance(getActivity())) {
            title.setText(titleResId);
            image.setImageResource(imageResId);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (pos == 0) {
            Animation animation2 = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up_in);
            title.startAnimation(animation2);
        }
    }
}