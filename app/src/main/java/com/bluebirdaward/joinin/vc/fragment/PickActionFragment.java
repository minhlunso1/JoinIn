package com.bluebirdaward.joinin.vc.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.bluebirdaward.joinin.net.CategoryClient;
import com.parse.ParseObject;

import java.util.ArrayList;

import com.bluebirdaward.joinin.utils.ItemClickSupport;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bluebirdaward.joinin.R;
import com.bluebirdaward.joinin.adapter.CategoryAdapter;
import com.bluebirdaward.joinin.pojo.Category;
import com.bluebirdaward.joinin.utils.DividerItemDecoration;
import com.bluebirdaward.joinin.vc.activity.PostStatusActivity;

/**
 * Created by duyvu on 4/26/16.
 */
public class PickActionFragment extends Fragment {

    @Bind(R.id.edt_action)
    EditText tvAction;
    @Bind(R.id.rvCategory)
    RecyclerView rvCategory;

    private String action = "";
    private ArrayList<Category> categories = new ArrayList<>();
    private CategoryAdapter categoryAdapter;

    public static PickActionFragment newInstance(String action) {
        Bundle bundle = new Bundle();
        bundle.putString("action", action);
        PickActionFragment fragment = new PickActionFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!((PostStatusActivity) getActivity()).getTempAction().equals(""))
            tvAction.setText(((PostStatusActivity) getActivity()).getTempAction());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pick_action, container, false);
        action = getArguments().getString("action");
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvAction.setText(action);
        setupRecyclerView();
        fetchAllActions();
    }

    private void fetchAllActions() {
        if (categories.size() == 0) {
            CategoryClient.fetchAllCategories(new CategoryClient.OnCategoriesFetchedListener() {
                @Override
                public void onSuccess(ArrayList<ParseObject> categoriesObjects) {
                    if (categoriesObjects != null && categoriesObjects.size() > 0)
                        categories.addAll(Category.fromParseObjects(categoriesObjects));
                    categoryAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @OnClick(R.id.tv_cancel)
    public void cancel() {
        ((PostStatusActivity) getActivity()).setTempAction("");
        getActivity().getSupportFragmentManager().popBackStackImmediate();
    }

    @OnClick(R.id.img_done)
    public void done() {
        ((PostStatusActivity) getActivity()).setAction(tvAction.getText().toString());
        ((PostStatusActivity) getActivity()).updateStatusInfoUI();
        cancel();
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter(categories);
        rvCategory.setAdapter(categoryAdapter);
        rvCategory.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvCategory.addItemDecoration(new DividerItemDecoration(getActivity()));
        ItemClickSupport.addTo(rvCategory).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                launchPickActionItemFragment(position);
            }
        });
    }

    public void launchPickActionItemFragment(int position) {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(tvAction.getWindowToken(), 0);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        fragmentTransaction.replace(android.R.id.content, PickActionItemFragment.newInstance(categories.get(position)));
        fragmentTransaction.addToBackStack("action_item_fragment");
        fragmentTransaction.commit();
    }
}
