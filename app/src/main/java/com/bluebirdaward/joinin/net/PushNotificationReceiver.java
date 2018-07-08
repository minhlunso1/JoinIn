package com.bluebirdaward.joinin.net;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.bluebirdaward.joinin.aconst.Code;
import com.bluebirdaward.joinin.pojo.DemoParticipant;
import com.bluebirdaward.joinin.utils.Log;
import com.bluebirdaward.joinin.vc.activity.MessagesListActivity;
import com.layer.atlas.provider.Participant;
import com.layer.atlas.provider.ParticipantProvider;
import com.layer.atlas.util.Util;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.Queryable;
import com.layer.sdk.query.SortDescriptor;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.bluebirdaward.joinin.BuildConfig;
import com.bluebirdaward.joinin.JoininApplication;
import com.bluebirdaward.joinin.R;

import com.bluebirdaward.joinin.vc.activity.DashboardActivity;

public class PushNotificationReceiver extends BroadcastReceiver {
    public final static int MESSAGE_ID = 1;
    private final static AtomicInteger sPendingIntentCounter = new AtomicInteger(0);
    private static Notifications sNotifications;

    public final static String ACTION_PUSH = "com.layer.sdk.PUSH";
    public final static String ACTION_CANCEL = BuildConfig.APPLICATION_ID + ".CANCEL_PUSH";

    public final static String LAYER_TEXT_KEY = "layer-push-message";
    public final static String LAYER_CONVERSATION_KEY = "layer-conversation-id";
    public final static String LAYER_MESSAGE_KEY = "layer-message-id";



    /**
     * Parses the `com.layer.sdk.PUSH` Intent.
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        setupNotification(context, intent);
        Bundle extras = intent.getExtras();
        if (extras == null) return;
        final String text = extras.getString(LAYER_TEXT_KEY, null);
        final Uri conversationId = extras.getParcelable(LAYER_CONVERSATION_KEY);
        final Uri messageId = extras.getParcelable(LAYER_MESSAGE_KEY);

        if (intent.getAction().equals(ACTION_PUSH)) {
            // New push from Layer
            if (Log.isLoggable(Log.VERBOSE)) Log.v("Received notification for: " + messageId);
            if (messageId == null) {
                if (Log.isLoggable(Log.ERROR)) Log.e("No message to notify: " + extras);
                return;
            }
            if (conversationId == null) {
                if (Log.isLoggable(Log.ERROR)) Log.e("No conversation to notify: " + extras);
                return;
            }

            if (!getNotifications(context).isEnabled()) {
                if (Log.isLoggable(Log.VERBOSE)) {
                    Log.v("Blocking notification due to global app setting");
                }
                return;
            }

            if (!getNotifications(context).isEnabled(conversationId)) {
                if (Log.isLoggable(Log.VERBOSE)) {
                    Log.v("Blocking notification due to conversation detail setting");
                }
                return;
            }

            // Try to have content ready for viewing before posting a Notification
            Util.waitForContent(JoininApplication.getLayerClient().connect(), messageId,
                    new Util.ContentAvailableCallback() {
                        @Override
                        public void onContentAvailable(LayerClient client, Queryable object) {
                            if (Log.isLoggable(Log.VERBOSE)) {
                                Log.v("Pre-fetched notification content");
                            }
                            getNotifications(context).add(context, (Message) object, text);
                        }

                        @Override
                        public void onContentFailed(LayerClient client, Uri objectId, String reason) {
                            if (Log.isLoggable(Log.ERROR)) {
                                Log.e("Failed to fetch notification content");
                            }
                        }
                    }
            );
        } else if (intent.getAction().equals(ACTION_CANCEL)) {
            // User swiped notification out
            if (Log.isLoggable(Log.VERBOSE)) {
                Log.v("Cancelling notifications for: " + conversationId);
            }
            getNotifications(context).clear(conversationId);
        } else {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("Got unknown intent action: " + intent.getAction());
            }
        }
    }

    private void setupNotification(Context context, Intent intent) {
        //Don't show a notification on boot
        if(intent.getAction() == Intent.ACTION_BOOT_COMPLETED)
            return;

        // Get notification content
        Bundle extras = intent.getExtras();
        String message = "";
        Uri conversationId = null;
        if (extras.containsKey("layer-push-message")) {
            message = extras.getString("layer-push-message");
        }
        if (extras.containsKey("layer-conversation-id")) {
            conversationId = extras.getParcelable("layer-conversation-id");
        }

        // Build the notification
        android.util.Log.d("debug", "message:"+message);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_comment_white_36dp)
                .setColor(context.getResources().getColor(R.color.colorPrimary_1))
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_SOUND);

        // Set the action to take when a user taps the notification
        Intent resultIntent = new Intent(context, DashboardActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        resultIntent.putExtra(Code.TAB_POS, 2);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        // Show the notification
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(1, mBuilder.build());
    }

    public static synchronized Notifications getNotifications(Context context) {
        if (sNotifications == null) {
            sNotifications = new Notifications(context);
        }
        return sNotifications;
    }

    /**
     * Notifications manages notifications displayed on the user's device.  Notifications are
     * grouped by Conversation, where a Conversation's notifications are rolled-up into single
     * notification summaries.
     */
    public static class Notifications {
        private static final String KEY_ALL = "all";
        private static final String KEY_POSITION = "position";
        private static final String KEY_TEXT = "text";
        private ParticipantProvider participantProvider;

        private final int MAX_MESSAGES = 5;
        // Contains black-listed conversation IDs and the global "all" key for notifications
        private final SharedPreferences mDisableds;

        // Contains positions for message IDs
        private final SharedPreferences mPositions;
        private final SharedPreferences mMessages;
        private final NotificationManager mManager;

        public Notifications(Context context) {
            mDisableds = context.getSharedPreferences("notification_disableds", Context.MODE_PRIVATE);
            mPositions = context.getSharedPreferences("notification_positions", Context.MODE_PRIVATE);
            mMessages = context.getSharedPreferences("notification_messages", Context.MODE_PRIVATE);
            mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        public boolean isEnabled() {
            return !mDisableds.contains(KEY_ALL);
        }

        public boolean isEnabled(Uri conversationId) {
            if (conversationId == null) {
                return isEnabled();
            }
            return !mDisableds.contains(conversationId.toString());
        }

        public void setEnabled(boolean enabled) {
            if (enabled) {
                mDisableds.edit().remove(KEY_ALL).apply();
            } else {
                mDisableds.edit().putBoolean(KEY_ALL, true).apply();
                mManager.cancelAll();
            }
        }

        public void setEnabled(Uri conversationId, boolean enabled) {
            if (conversationId == null) {
                return;
            }
            if (enabled) {
                mDisableds.edit().remove(conversationId.toString()).apply();
            } else {
                mDisableds.edit().putBoolean(conversationId.toString(), true).apply();
                mManager.cancel(conversationId.toString(), MESSAGE_ID);
            }
        }

        public void clear(final Conversation conversation) {
            if (conversation == null) return;
            clear(conversation.getId());
        }

        /**
         * Called when a Conversation is opened or message is marked as read
         * Clears messages map; sets position to greatest position
         *
         * @param conversationId Conversation whose notifications should be cleared
         */
        public void clear(final Uri conversationId) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (conversationId == null) return;
                    String key = conversationId.toString();
                    long maxPosition = getMaxPosition(conversationId);
                    mMessages.edit().remove(key).commit();
                    mPositions.edit().putLong(key, maxPosition).commit();
                    mManager.cancel(key, MESSAGE_ID);
                }
            }).start();
        }

        /**
         * Called when a new message arrives
         *
         * @param message Message to add
         * @param text    Notification text for added Message
         */
        protected void add(Context context, Message message, String text) {
            Conversation conversation = message.getConversation();
            getParticipants(conversation);
            String key = conversation.getId().toString();
            long currentPosition = mPositions.getLong(key, Long.MIN_VALUE);

            // Ignore older messages
            if (message.getPosition() <= currentPosition) return;

            String currentMessages = mMessages.getString(key, null);

            try {
                JSONObject messages = currentMessages == null ? new JSONObject() : new JSONObject(currentMessages);
                String messageKey = message.getId().toString();

                // Ignore if we already have this message
                if (messages.has(messageKey)) return;

                JSONObject messageEntry = new JSONObject();
                messageEntry.put(KEY_POSITION, message.getPosition());
                messageEntry.put(KEY_TEXT, text);
                messages.put(messageKey, messageEntry);

                mMessages.edit().putString(key, messages.toString()).commit();
            } catch (JSONException e) {
                if (Log.isLoggable(Log.ERROR)) {
                    Log.e(e.getMessage(), e);
                }
                return;
            }
           update(context, conversation, message);
        }

        private void getParticipants(Conversation conversation) {
            List<String> participantIds = conversation.getParticipants();
            ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
            userQuery.whereContainsAll("objectId", participantIds);
            userQuery.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    if (objects!=null) {
                        final Map<String, DemoParticipant> mParticipantMap = new HashMap<>();
                        for (int i = 0; i < objects.size(); i++) {
                            addParticipantToMap(objects.get(i), mParticipantMap);
                        }
                        participantProvider = new ParticipantProvider() {
                            @Override
                            public Map<String, Participant> getMatchingParticipants(String filter, Map<String, Participant> result) {
                                if (result == null) {
                                    result = new HashMap<String, Participant>();
                                }
                                synchronized (mParticipantMap) {
                                    // With no filter, return all Participants
                                    if (filter == null) {
                                        result.putAll(mParticipantMap);
                                        return result;
                                    }

                                    // Filter participants by substring matching first- and last- names
                                    for (DemoParticipant p : mParticipantMap.values()) {
                                        boolean matches = false;
                                        if (p.getName() != null && p.getName().toLowerCase().contains(filter))
                                            matches = true;
                                        if (matches) {
                                            result.put(p.getId(), p);
                                        } else {
                                            result.remove(p.getId());
                                        }
                                    }
                                    return result;
                                }

                            }

                            @Override
                            public Participant getParticipant(String userId) {
                                synchronized (mParticipantMap) {
                                    DemoParticipant participant = mParticipantMap.get(userId);
                                    if (participant != null) return participant;
                                    return null;
                                }
                            }

                        };
                    }
                }
            });
        }

        private void addParticipantToMap(ParseUser user, Map<String, DemoParticipant> mParticipantMap) {
            DemoParticipant participant = new DemoParticipant();
            if (user != null) {
                participant.setId(user.getObjectId());
                participant.setAvatarUrl(Uri.parse(user.getString("img_avatar_url")));
                participant.setName(user.getString("name"));
                mParticipantMap.put(user.getObjectId(), participant);
            }
        }

        private void update(Context context, Conversation conversation, Message message) {
            if (participantProvider!=null) {
                String messagesString = mMessages.getString(conversation.getId().toString(), null);
                if (messagesString == null) return;

                // Get current notification texts
                Map<Long, String> positionText = new HashMap<Long, String>();
                try {
                    JSONObject messagesJson = new JSONObject(messagesString);
                    Iterator<String> iterator = messagesJson.keys();
                    while (iterator.hasNext()) {
                        String messageId = iterator.next();
                        JSONObject messageJson = messagesJson.getJSONObject(messageId);
                        long position = messageJson.getLong(KEY_POSITION);
                        String text = messageJson.getString(KEY_TEXT);
                        positionText.put(position, text);
                    }
                } catch (JSONException e) {
                    if (Log.isLoggable(Log.ERROR)) {
                        Log.e(e.getMessage(), e);
                    }
                    return;
                }

                // Sort by message position
                List<Long> positions = new ArrayList<Long>(positionText.keySet());
                Collections.sort(positions);

                // Construct notification
                String conversationTitle = Util.getConversationTitle(JoininApplication.getLayerClient(), participantProvider, conversation);
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle().setBigContentTitle(conversationTitle);
                int i;
                if (positions.size() <= MAX_MESSAGES) {
                    i = 0;
                    inboxStyle.setSummaryText(null);
                } else {
                    i = positions.size() - MAX_MESSAGES;
                    inboxStyle.setSummaryText(context.getString(R.string.notifications_num_more, i));
                }
                while (i < positions.size()) {
                    inboxStyle.addLine(positionText.get(positions.get(i++)));
                }

                String collapsedSummary = positions.size() == 1 ? positionText.get(positions.get(0)) :
                        context.getString(R.string.notifications_new_messages, positions.size());

                // Construct notification
                // TODO: use large icon based on avatars
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notification)
                        .setContentTitle(conversationTitle)
                        .setContentText(collapsedSummary)
                        .setAutoCancel(true)
                        .setLights(context.getResources().getColor(R.color.atlas_action_bar_background), 100, 1900)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                        .setStyle(inboxStyle);

                // Intent to launch when clicked
                Intent clickIntent = new Intent(context, MessagesListActivity.class)
                        .setPackage(context.getApplicationContext().getPackageName())
                        .putExtra(LAYER_CONVERSATION_KEY, conversation.getId())
                        .putExtra(LAYER_MESSAGE_KEY, message.getId())
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent clickPendingIntent = PendingIntent.getActivity(
                        context, sPendingIntentCounter.getAndIncrement(),
                        clickIntent, PendingIntent.FLAG_ONE_SHOT);
                mBuilder.setContentIntent(clickPendingIntent);

                // Intent to launch when swiped out
                Intent cancelIntent = new Intent(ACTION_CANCEL)
                        .setPackage(context.getApplicationContext().getPackageName())
                        .putExtra(LAYER_CONVERSATION_KEY, conversation.getId())
                        .putExtra(LAYER_MESSAGE_KEY, message.getId());
                PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(
                        context, sPendingIntentCounter.getAndIncrement(),
                        cancelIntent, PendingIntent.FLAG_ONE_SHOT);
                mBuilder.setDeleteIntent(cancelPendingIntent);

                // Show the notification
                mManager.notify(conversation.getId().toString(), MESSAGE_ID, mBuilder.build());
            }
        }

        /**
         * Returns the current maximum Message position within the given Conversation, or
         * Long.MIN_VALUE if no messages are found.
         *
         * @param conversationId Conversation whose maximum Message position to return.
         * @return the current maximum Message position or Long.MIN_VALUE.
         */
        private long getMaxPosition(Uri conversationId) {
            LayerClient layerClient = JoininApplication.getLayerClient();

            Query<Message> query = Query.builder(Message.class)
                    .predicate(new Predicate(Message.Property.CONVERSATION, Predicate.Operator.EQUAL_TO, conversationId))
                    .sortDescriptor(new SortDescriptor(Message.Property.POSITION, SortDescriptor.Order.DESCENDING))
                    .limit(1)
                    .build();

            List results = layerClient.executeQueryForObjects(query);
            if (results.isEmpty()) return Long.MIN_VALUE;
            return ((Message) results.get(0)).getPosition();
        }
    }
}
