package com.bluebirdaward.joinin.vc.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.parse.ParseUser;

import com.bluebirdaward.joinin.net.UserClient;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bluebirdaward.joinin.JoininApplication;
import com.bluebirdaward.joinin.R;

/**
 * Created by Minh on 4/13/2016.
 */
public class EditProfileFragment extends Fragment {

    @Bind(R.id.edt_name)
    EditText edtName;
    @Bind(R.id.edt_intro)
    EditText edtIntro;
    @Bind(R.id.edt_interests)
    EditText edtInterests;

    ProfileFragment profileFragment;

    public static EditProfileFragment newInstance(ProfileFragment profileFragment) {
        EditProfileFragment fragment = new EditProfileFragment();
        fragment.profileFragment = profileFragment;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edtName.setText(JoininApplication.me.getName());
        edtIntro.setText(JoininApplication.me.getAboutMe());
        edtInterests.setText(JoininApplication.me.getHobby());
    }

    @OnClick(R.id.tv_cancel)
    public void cancel(){
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtIntro.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(edtInterests.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(edtName.getWindowToken(), 0);
        getActivity().getSupportFragmentManager().popBackStackImmediate();
    }

    @OnClick(R.id.img_done)
    public void update(){
        ParseUser.getCurrentUser().put("name", edtName.getText().toString());
        ParseUser.getCurrentUser().put("about", edtIntro.getText().toString());
        ParseUser.getCurrentUser().put("hobbies", edtInterests.getText().toString());
        ParseUser.getCurrentUser().saveInBackground();

        profileFragment.set1(edtName.getText().toString(), edtIntro.getText().toString(), edtInterests.getText().toString());
        if (ParseUser.getCurrentUser() != null)
            UserClient.refreshCurrentUserData();
        cancel();
    }

}
