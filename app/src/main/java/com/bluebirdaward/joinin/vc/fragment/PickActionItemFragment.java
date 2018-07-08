package com.bluebirdaward.joinin.vc.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluebirdaward.joinin.adapter.ActionAdapter;
import com.bluebirdaward.joinin.pojo.Category;
import com.bluebirdaward.joinin.utils.DividerItemDecoration;
import com.bluebirdaward.joinin.utils.ItemClickSupport;
import com.bluebirdaward.joinin.vc.activity.PostStatusActivity;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bluebirdaward.joinin.R;

/**
 * Created by duyvu on 4/26/16.
 */
public class PickActionItemFragment extends Fragment {

    @Bind(R.id.rvAction)
    RecyclerView rvAction;

    private ActionAdapter actionAdapter;

    private Category category = new Category();

    public static PickActionItemFragment newInstance(Category category) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("category", category);
        PickActionItemFragment fragment = new PickActionItemFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pick_action_item, container, false);
        category = getArguments().getParcelable("category");
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        actionAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.tv_cancel)
    public void cancel() {
        getActivity().getSupportFragmentManager().popBackStackImmediate();
    }

    private void setupRecyclerView() {
        actionAdapter = new ActionAdapter(category.getActions());
        rvAction.setAdapter(actionAdapter);
        rvAction.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvAction.addItemDecoration(new DividerItemDecoration(getActivity()));
        ItemClickSupport.addTo(rvAction).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                String actionItem = category.getActions().get(position).getEmoticon() + " " + category.getName() + " " + category.getActions().get(position).getName();
                ((PostStatusActivity) getActivity()).setTempAction(actionItem);
                cancel();
            }
        });
    }
}
