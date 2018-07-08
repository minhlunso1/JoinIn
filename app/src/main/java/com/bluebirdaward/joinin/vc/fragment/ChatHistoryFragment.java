package com.bluebirdaward.joinin.vc.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.layer.atlas.AtlasConversationsRecyclerView;
import com.layer.atlas.adapters.AtlasConversationsAdapter;
import com.layer.atlas.util.views.SwipeableItem;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;

import com.bluebirdaward.joinin.net.PushNotificationReceiver;
import com.bluebirdaward.joinin.vc.activity.DashboardActivity;
import com.bluebirdaward.joinin.vc.activity.MessagesListActivity;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bluebirdaward.joinin.JoininApplication;
import com.bluebirdaward.joinin.R;

/**
 * Created by duyvu on 4/11/16.
 */
public class ChatHistoryFragment extends Fragment {

    private DashboardActivity activity;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
	@Bind(R.id.conversations_list)
    AtlasConversationsRecyclerView conversationsList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (DashboardActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation_list, container, false);
        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);
        toolbar.setTitle(activity.getString(R.string.Chat_history));
        toolbar.inflateMenu(R.menu.menu_dashboard2);
        toolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_logout:
                                activity.destroyAll();
                                return true;
                        }
                        return true;
                    }
                });

        setupConversationList();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupConversationList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        conversationsList.refresh();
    }

    private void setupConversationList() {
        conversationsList.init(JoininApplication.getLayerClient(), JoininApplication.getParticipantProvider(), JoininApplication.getPicasso())
                .setInitialHistoricMessagesToFetch(20)
                .setOnConversationClickListener(new AtlasConversationsAdapter.OnConversationClickListener() {
                    @Override
                    public void onConversationClick(AtlasConversationsAdapter adapter, Conversation conversation) {
                        Intent intent = new Intent(getActivity(), MessagesListActivity.class);
                        //Log.d("DEBUG", "Launching MessagesListActivity with existing conversation ID: " + conversation.getId());
                        intent.putExtra(PushNotificationReceiver.LAYER_CONVERSATION_KEY, conversation.getId());
                        startActivity(intent);
                        activity.overridePendingTransition(R.anim.enter, R.anim.exit);
                    }

                    @Override
                    public boolean onConversationLongClick(AtlasConversationsAdapter adapter, Conversation conversation) {
                        return false;
                    }
                })
                .setOnConversationSwipeListener(new SwipeableItem.OnSwipeListener<Conversation>() {
                    @Override
                    public void onSwipe(final Conversation conversation, int direction) {
                        new AlertDialog.Builder(getActivity())
                                .setMessage(R.string.alert_message_delete_conversation)
                                .setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO: simply update this one conversation
                                        conversationsList.getAdapter().notifyDataSetChanged();
                                    }
                                })
                                .setNeutralButton(R.string.alert_button_delete_my_devices, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        conversation.delete(LayerClient.DeletionMode.ALL_MY_DEVICES);
                                    }
                                })
                                .setPositiveButton(R.string.alert_button_delete_all_participants, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        conversation.delete(LayerClient.DeletionMode.ALL_PARTICIPANTS);
                                    }
                                })
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                });
    }
}