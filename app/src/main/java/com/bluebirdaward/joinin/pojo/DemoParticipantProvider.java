package com.bluebirdaward.joinin.pojo;

import android.content.Context;
import android.net.Uri;

import com.bluebirdaward.joinin.net.UserClient;
import com.bluebirdaward.joinin.utils.Log;
import com.layer.atlas.provider.Participant;
import com.layer.atlas.provider.ParticipantProvider;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


public class DemoParticipantProvider implements ParticipantProvider {
    private final Context mContext;
    private String mLayerAppIdLastPathSegment;
    private final Queue<ParticipantListener> mParticipantListeners = new ConcurrentLinkedQueue<>();
    private final Map<String, DemoParticipant> mParticipantMap = new HashMap<>();
    private final AtomicBoolean mFetching = new AtomicBoolean(false);

    public DemoParticipantProvider(Context context) {
        mContext = context.getApplicationContext();
    }

    public DemoParticipantProvider setLayerAppId(String layerAppId) {
        if (layerAppId.contains("/")) {
            mLayerAppIdLastPathSegment = Uri.parse(layerAppId).getLastPathSegment();
        } else {
            mLayerAppIdLastPathSegment = layerAppId;
        }
        load();
        fetchParticipants();
        return this;
    }


    //==============================================================================================
    // Atlas ParticipantProvider
    //==============================================================================================

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
            fetchParticipants();
            return null;
        }
    }

    /**
     * Adds the provided Participants to this ParticipantProvider, saves the participants, and
     * returns the list of added participant IDs.
     */
    private DemoParticipantProvider setParticipants(Collection<DemoParticipant> participants) {
        List<String> newParticipantIds = new ArrayList<>(participants.size());
        synchronized (mParticipantMap) {
            for (DemoParticipant participant : participants) {
                String participantId = participant.getId();
                if (!mParticipantMap.containsKey(participantId))
                    newParticipantIds.add(participantId);
                mParticipantMap.put(participantId, participant);
            }
            save();
        }
        alertParticipantsUpdated(newParticipantIds);
        return this;
    }


    //==============================================================================================
    // Persistence
    //==============================================================================================

    /*
     * Loads additional participants from SharedPreferences
     */
    private boolean load() {
        synchronized (mParticipantMap) {
            String jsonString = mContext.getSharedPreferences("participants", Context.MODE_PRIVATE).getString("json", null);
            if (jsonString == null) return false;

            try {
                for (DemoParticipant participant : participantsFromJson(new JSONArray(jsonString))) {
                    mParticipantMap.put(participant.getId(), participant);
                }
                return true;
            } catch (JSONException e) {
                if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * Saves the current map of participants to SharedPreferences
     */
    private boolean save() {
        synchronized (mParticipantMap) {
            try {
                mContext.getSharedPreferences("participants", Context.MODE_PRIVATE).edit()
                        .putString("json", participantsToJson(mParticipantMap.values()).toString())
                        .commit();
                return true;
            } catch (JSONException e) {
                if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
            }
        }
        return false;
    }


    //==============================================================================================
    // Network operations
    //==============================================================================================
    private DemoParticipantProvider fetchParticipants() {
        if (!mFetching.compareAndSet(false, true)) return this;
        UserClient.fetchAllUsers(new UserClient.OnAllUsersFetchedListener() {
            @Override
            public void onAllUsersFetched(ArrayList<ParseUser> fetchedUsers) {
                if (fetchedUsers.size() > 0) {
                    ArrayList<DemoParticipant> demoParticipants = new ArrayList<>();
                    demoParticipants = participantsFromParseUsers(fetchedUsers);
                    setParticipants(demoParticipants);
                }
            }
        });
        return this;
    }

    //==============================================================================================
    // Utils
    //==============================================================================================

    private ArrayList<DemoParticipant> participantsFromParseUsers(ArrayList<ParseUser> parseUsers) {
        ArrayList<DemoParticipant> demoParticipants = new ArrayList<>();
        for (int i = 0; i < parseUsers.size(); i++) {
            DemoParticipant demoParticipant = new DemoParticipant();
            if (parseUsers.get(i).has("img_avatar_url"))
                demoParticipant.setAvatarUrl(Uri.parse(parseUsers.get(i).getString("img_avatar_url")));
            demoParticipant.setId(parseUsers.get(i).getObjectId());
            demoParticipant.setName(parseUsers.get(i).getString("name"));
            demoParticipants.add(demoParticipant);
        }
        return demoParticipants;
    }

    private ArrayList<DemoParticipant> participantsFromUsers(ArrayList<User> users) {
        ArrayList<DemoParticipant> demoParticipants = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            DemoParticipant demoParticipant = new DemoParticipant();
            if (users.get(i).getImgAva().length() > 0)
                demoParticipant.setAvatarUrl(Uri.parse(users.get(i).getImgAva()));
            demoParticipant.setId(users.get(i).getId());
            demoParticipant.setName(users.get(i).getName());
            demoParticipants.add(demoParticipant);
        }
        return demoParticipants;
    }

    private static List<DemoParticipant> participantsFromJson(JSONArray participantArray) throws JSONException {
        List<DemoParticipant> participants = new ArrayList<>(participantArray.length());
        for (int i = 0; i < participantArray.length(); i++) {
            JSONObject participantObject = participantArray.getJSONObject(i);
            DemoParticipant participant = new DemoParticipant();
            participant.setId(participantObject.optString("id"));
            participant.setName(participantObject.optString("name"));
            if (participantObject.has("img_avatar_url"))
                participant.setAvatarUrl(Uri.parse(participantObject.optString("img_avatar_url")));
            participants.add(participant);
        }
        return participants;
    }

    private static JSONArray participantsToJson(Collection<DemoParticipant> participants) throws JSONException {
        JSONArray participantsArray = new JSONArray();
        for (DemoParticipant participant : participants) {
            JSONObject participantObject = new JSONObject();
            participantObject.put("id", participant.getId());
            participantObject.put("name", participant.getName());
            if (participant.getAvatarUrl() != null)
                participantObject.put("img_avatar_url", participant.getAvatarUrl());
            participantsArray.put(participantObject);
        }
        return participantsArray;
    }

    private DemoParticipantProvider registerParticipantListener(ParticipantListener participantListener) {
        if (!mParticipantListeners.contains(participantListener)) {
            mParticipantListeners.add(participantListener);
        }
        return this;
    }

    private DemoParticipantProvider unregisterParticipantListener(ParticipantListener participantListener) {
        mParticipantListeners.remove(participantListener);
        return this;
    }

    private void alertParticipantsUpdated(Collection<String> updatedParticipantIds) {
        for (ParticipantListener listener : mParticipantListeners) {
            listener.onParticipantsUpdated(this, updatedParticipantIds);
        }
    }


    //==============================================================================================
    // Callbacks
    //==============================================================================================

    public interface ParticipantListener {
        void onParticipantsUpdated(DemoParticipantProvider provider, Collection<String> updatedParticipantIds);
    }
}