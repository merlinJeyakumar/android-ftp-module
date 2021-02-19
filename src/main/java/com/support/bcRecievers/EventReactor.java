package com.support.bcRecievers;

import android.content.Context;
import android.util.Log;
import android.util.Pair;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventReactor {
    private String TAG = "EventReactor";
    private static Map<String, List<Pair<Integer, EventEmission>>> eventsEmissionsMap = new HashMap<>(); //EventKey, Pair(DefaultHash, Emission)
    private static Map<Integer, List<Pair<String, Integer>>> eventsContextMap = new HashMap<>(); //ContextHash, Pair(EventKey, EmissionHash)

    public void pushEvent(String eventName, Object object) {
        if (eventsEmissionsMap.containsKey(eventName)) {
            for (Pair<Integer, EventEmission> integerEventEmissionPair : eventsEmissionsMap.get(eventName)) {
                if (object != null) {
                    integerEventEmissionPair.second.onEmit(object);
                } else {
                    integerEventEmissionPair.second.onEmit();
                }
            }
        } else {
            Log.e(TAG, "Event not found");
        }
    }

    public void subscribeEvent(Context context, String eventName, EventEmission eventEmission) {
        Log.e(TAG, "SubscribeEvent eventEmission_HashCode" + eventEmission.hashCode());
        Log.e(TAG, "SubscribeEvent contextHashCode" + context.hashCode());
        Log.e(TAG, "SubscribeEvent eventName" + eventName);

        List<Pair<Integer, EventEmission>> eventEmissionList = new ArrayList<>();
        List<Pair<String, Integer>> contextEmissionHashList = new ArrayList<>();

        if (eventsEmissionsMap.containsKey(eventName)) {
            if (isEventSubscribed(eventName, eventEmission)) {
                Log.e(TAG, "Event already subscribed");
                return;
            }
            eventEmissionList = eventsEmissionsMap.get(eventName);
        }
        eventEmissionList.add(new Pair(eventEmission.hashCode(), eventEmission));
        contextEmissionHashList.add(new Pair(eventName, eventEmission.hashCode()));

        eventsEmissionsMap.put(eventName, eventEmissionList);
        eventsContextMap.put(context.hashCode(), contextEmissionHashList);
    }

    public void unsubscribeEvent(Context context) {
        Log.e(TAG, "unsubscribeEvent " + context.hashCode());
        if (!eventsContextMap.containsKey(context.hashCode())) {
            Log.e(TAG, "No Event Registered");
            return;
        }
        for (Pair<String, Integer> stringIntegerPair : eventsContextMap.get(context.hashCode())) { //Pair(EventKey, EmissionHash)
            List<Pair<Integer, EventEmission>> eventsList = eventsEmissionsMap.get(stringIntegerPair.first);
            for (Pair<Integer, EventEmission> integerEventEmissionPair : eventsList) {
                if (integerEventEmissionPair.first.equals(stringIntegerPair.second)) {
                    eventsList.remove(integerEventEmissionPair);
                }
            }
            if (eventsList.isEmpty()) {
                eventsEmissionsMap.remove(stringIntegerPair.first);
            }
            return;
        }
        Log.e(TAG, "Unsubscribe failed");
    }

    public void unsubscribeEvent(Context context, EventEmission paraEventEmission) {
        for (Pair<String, Integer> eventKeyEmissionHashPair : eventsContextMap.get(context.hashCode())) { //Pair(EventKey,EmissionHash)
            String remEventKey = eventKeyEmissionHashPair.first;
            Integer remEmissionHash = paraEventEmission.hashCode();

            List<Pair<Integer, EventEmission>> remContextEmissionList = eventsEmissionsMap.get(remEventKey);

            for (Pair<Integer, EventEmission> integerEventEmissionPair : remContextEmissionList) { //Pair(DefaultHash, Emission)
                Integer originEmissionHash = integerEventEmissionPair.first;
                EventEmission eventEmission = integerEventEmissionPair.second;

                if (originEmissionHash.equals(remEmissionHash)) {
                    if (remContextEmissionList.remove(integerEventEmissionPair)) {
                        Log.e(TAG, "Specific event unsubscribed " + originEmissionHash);
                        return;
                    }
                }
            }
        }
        Log.e(TAG, "UnsubscribeEvent Failed");
    }

    public boolean isEventSubscribed(String eventName, EventEmission eventEmission) {
        if (!eventsEmissionsMap.containsKey(eventName)) {
            return false;
        }
        int subscEventHashCode = eventEmission.hashCode();

        for (Pair<Integer, EventEmission> integerEventEmissionPair : eventsEmissionsMap.get(eventName)) { //Pair(DefaultHash, Emission)
            if (subscEventHashCode == integerEventEmissionPair.first) {
                return true;
            }
        }
        return false;
    }
}
