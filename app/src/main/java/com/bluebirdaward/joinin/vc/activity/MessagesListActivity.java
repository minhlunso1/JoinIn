package com.bluebirdaward.joinin.vc.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.layer.atlas.AtlasHistoricMessagesFetchLayout;
import com.layer.atlas.AtlasMessageComposer;
import com.layer.atlas.AtlasMessagesRecyclerView;
import com.layer.atlas.AtlasTypingIndicator;
import com.layer.atlas.messagetypes.generic.GenericCellFactory;
import com.layer.atlas.messagetypes.location.LocationCellFactory;
import com.layer.atlas.messagetypes.location.LocationSender;
import com.layer.atlas.messagetypes.singlepartimage.SinglePartImageCellFactory;
import com.layer.atlas.messagetypes.text.TextCellFactory;
import com.layer.atlas.messagetypes.text.TextSender;
import com.layer.atlas.messagetypes.threepartimage.CameraSender;
import com.layer.atlas.messagetypes.threepartimage.GallerySender;
import com.layer.atlas.messagetypes.threepartimage.ThreePartImageCellFactory;
import com.layer.atlas.typingindicators.AvatarTypingIndicatorFactory;
import com.layer.atlas.util.Util;
import com.layer.atlas.util.views.SwipeableItem;
import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerConversationException;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.ConversationOptions;
import com.layer.sdk.messaging.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.bluebirdaward.joinin.net.PushNotificationReceiver;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bluebirdaward.joinin.JoininApplication;
import com.bluebirdaward.joinin.R;
import com.bluebirdaward.joinin.pojo.DemoParticipant;

public class MessagesListActivity extends BaseActivity {
    @Bind(R.id.messages_list)
    AtlasMessagesRecyclerView mMessagesList;
    @Bind(R.id.historic_sync_layout)
    AtlasHistoricMessagesFetchLayout mHistoricFetchLayout;
    @Bind(R.id.message_composer)
    AtlasMessageComposer mMessageComposer;
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private UiState mState;
    private Conversation mConversation;
    private AtlasTypingIndicator mTypingIndicator;
    private final Map<String, DemoParticipant> mParticipantMap = new HashMap<>();

    private enum UiState {
        ADDRESS,
        ADDRESS_COMPOSER,
        ADDRESS_CONVERSATION_COMPOSER,
        CONVERSATION_COMPOSER
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        ButterKnife.bind(this);

        fetchIntents();
        setupLayoutElements();
        setConversation(mConversation, mConversation != null);
    }

    private void fetchIntents() {
        // Get or create Conversation from Intent extras
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(PushNotificationReceiver.LAYER_CONVERSATION_KEY)) {
                Uri conversationId = intent.getParcelableExtra(PushNotificationReceiver.LAYER_CONVERSATION_KEY);
                mConversation = JoininApplication.getLayerClient().getConversation(conversationId);
            } else if (intent.hasExtra("participantIds")) {
                ArrayList<String> participantIds = intent.getStringArrayListExtra("participantIds");
                try {
                    mConversation = JoininApplication.getLayerClient()
                            .newConversation(new ConversationOptions().distinct(true), participantIds);
                } catch (LayerConversationException e) {
                    mConversation = e.getConversation();
                }
            }
        }
    }

    private void setupLayoutElements() {
        setupToolbar();
        mHistoricFetchLayout.init(JoininApplication.getLayerClient())
                .setHistoricMessagesPerFetch(20);

        mMessagesList.init(JoininApplication.getLayerClient(), JoininApplication.getParticipantProvider(), JoininApplication.getPicasso())
                .addCellFactories(
                        new TextCellFactory(),
                        new ThreePartImageCellFactory(this, JoininApplication.getLayerClient(), JoininApplication.getPicasso()),
                        new LocationCellFactory(this, JoininApplication.getPicasso()),
                        new SinglePartImageCellFactory(this, JoininApplication.getLayerClient(), JoininApplication.getPicasso()),
                        new GenericCellFactory())
                .setOnMessageSwipeListener(new SwipeableItem.OnSwipeListener<Message>() {
                    @Override
                    public void onSwipe(final Message message, int direction) {
                        if (message.getSender().getUserId().equals(JoininApplication.me.getId()))
                            showDeleteDialogMe(message);
                        else
                            showDeleteDialogThem(message);
                    }
                });

        mTypingIndicator = new AtlasTypingIndicator(this)
                .init(JoininApplication.getLayerClient())
                .setTypingIndicatorFactory(new AvatarTypingIndicatorFactory(JoininApplication.getParticipantProvider(), JoininApplication.getPicasso()))
                .setTypingActivityListener(new AtlasTypingIndicator.TypingActivityListener() {
                    @Override
                    public void onTypingActivityChange(AtlasTypingIndicator typingIndicator, boolean active) {
                        mMessagesList.setFooterView(active ? typingIndicator : null);
                    }
                });

        mMessageComposer.init(JoininApplication.getLayerClient(), JoininApplication.getParticipantProvider())
                .setTextSender(new TextSender())
                .addAttachmentSenders(
                        new CameraSender(R.string.attachment_menu_camera, R.drawable.ic_photo_camera_white_24dp, this),
                        new GallerySender(R.string.attachment_menu_gallery, R.drawable.ic_photo_white_24dp, this),
                        new LocationSender(R.string.attachment_menu_location, R.drawable.ic_place_white_24dp, this))
                .setOnMessageEditTextFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            setUiState(UiState.CONVERSATION_COMPOSER);
                            setTitle(true);
                        }
                    }
                });

    }

    private void showDeleteDialogThem(final Message message) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.alert_message_delete_message)
                .setNeutralButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.alert_button_delete_my_devices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        message.delete(LayerClient.DeletionMode.ALL_MY_DEVICES);
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mMessagesList.getAdapter().notifyDataSetChanged();
                    }
                })
                .show();
    }

    private void showDeleteDialogMe(final Message message) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.alert_message_delete_message)
                .setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.alert_button_delete_my_devices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        message.delete(LayerClient.DeletionMode.ALL_MY_DEVICES);
                    }
                })
                .setNegativeButton(R.string.alert_button_delete_all_participants, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        message.delete(LayerClient.DeletionMode.ALL_PARTICIPANTS);
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mMessagesList.getAdapter().notifyDataSetChanged();
                    }
                })
                .show();
    }

    @Override
    protected void onResume() {
        // Clear any notifications for this conversation
        PushNotificationReceiver.getNotifications(this).clear(mConversation);
        super.onResume();
        setTitle(mConversation != null);
    }

    @Override
    protected void onPause() {
        // Update the notification position to the latest seen
        PushNotificationReceiver.getNotifications(this).clear(mConversation);
        super.onPause();
    }

    public void setTitle(boolean useConversation) {
        if (!useConversation) {
            setTitle(R.string.title_select_conversation);
        } else {
            setTitle(Util.getConversationTitle(JoininApplication.getLayerClient(), JoininApplication.getParticipantProvider(), mConversation));
        }
    }

    private void setUiState(UiState state) {
        if (mState == state) return;
        mState = state;
        switch (state) {
            case ADDRESS:
//                mAddressBar.setVisibility(View.VISIBLE);
//                mAddressBar.setSuggestionsVisibility(View.VISIBLE);
                mHistoricFetchLayout.setVisibility(View.GONE);
                mMessageComposer.setVisibility(View.GONE);
                break;

            case ADDRESS_COMPOSER:
//                mAddressBar.setVisibility(View.VISIBLE);
//                mAddressBar.setSuggestionsVisibility(View.VISIBLE);
                mHistoricFetchLayout.setVisibility(View.GONE);
                mMessageComposer.setVisibility(View.VISIBLE);
                break;

            case ADDRESS_CONVERSATION_COMPOSER:
//                mAddressBar.setVisibility(View.VISIBLE);
//                mAddressBar.setSuggestionsVisibility(View.GONE);
                mHistoricFetchLayout.setVisibility(View.VISIBLE);
                mMessageComposer.setVisibility(View.VISIBLE);
                break;

            case CONVERSATION_COMPOSER:
//                mAddressBar.setVisibility(View.GONE);
//                mAddressBar.setSuggestionsVisibility(View.GONE);
                mHistoricFetchLayout.setVisibility(View.VISIBLE);
                mMessageComposer.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setConversation(Conversation conversation, boolean hideLauncher) {
        mConversation = conversation;
        mHistoricFetchLayout.setConversation(conversation);
        mMessagesList.setConversation(conversation);
        mTypingIndicator.setConversation(conversation);
        mMessageComposer.setConversation(conversation);
        // UI state
        if (conversation == null) {
            setUiState(UiState.ADDRESS);
            return;
        }

        if (hideLauncher) {
            setUiState(UiState.CONVERSATION_COMPOSER);
            return;
        }

        if (conversation.getHistoricSyncStatus() == Conversation.HistoricSyncStatus.INVALID) {
            // New "temporary" conversation
            setUiState(UiState.ADDRESS_COMPOSER);
        } else {
            setUiState(UiState.ADDRESS_CONVERSATION_COMPOSER);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_setting) {
            Intent i = new Intent(MessagesListActivity.this, MessageSettingActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.enter, R.anim.exit);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mMessageComposer.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mMessageComposer.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
    }
}
