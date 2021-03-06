package com.support.bcRecievers;

import android.content.Context;
import android.util.Pair;

import com.support.utills.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*FIXME: COMPLETE TEST REQUIRED */
public class EventReactor {
    private static String TAG = "MappedEventReactor";

    private static Map<String, Map<Pair<Integer, Integer>, EventEmission>> primaryMap = new HashMap(); //key: EventName & value: Map(Pair(ContextHashCode & EventHashCode), EventEmissionObject)

    public void pushEvent(String eventName){
        pushEvent(eventName,null);
    }

    public void pushEvent(String eventName,
                                 Object object){
        if (primaryMap.containsKey(eventName)) {
            Map<Pair<Integer, Integer>, EventEmission> primaryMapValue = primaryMap.get(eventName);

            ArrayList<EventEmission> eventEmissionList = new ArrayList(primaryMapValue.values());
            for (EventEmission eventEmission : eventEmissionList) {
                eventEmission.onEmit(object);
                eventEmission.onEmit();
            }
            if (eventEmissionList.isEmpty()) {
                Log.e(TAG, "push_event_empty_list");
            }
        }else{
            Log.e(TAG, "push_event_name_not_found");
        }
    }

    public void subscribeEvent(Context context,
                                      String eventName,
                                      EventEmission eventEmission) {
        int contextHash = context.hashCode();

        Log.e(TAG, "subscribeEvent - eventName: "+eventName+" eventEmissionHash: "+eventEmission.hashCode());

        /* Required to update Primary Map at First*/
        addOrUpdateSubscription(contextHash,
                eventName,
                eventEmission);
    }

    public void unsubscribeEvent(Context context,String eventName){
        unsubscribeEvent(context,null,null);
    }

    /*
    * Unsubscribe Event by EventName & EventEmissionHashCode
    * Unsubscribe Event by ContextHashCode
    * Unsubscribe Event by EventName
    */
    public void unsubscribeEvent(Context context,
                                        String eventName,
                                        EventEmission eventEmission) {
        int contextHash = context.hashCode();

        if (eventName != null && eventEmission != null) {
            //Check Event Subscribed
            if (primaryMap.containsKey(eventName)) {
                Log.e(TAG, "unsubscribeEvent - eventName: "+eventName+" eventEmissionHash: "+eventEmission.hashCode());

                int eventEmissionHashCode = eventEmission.hashCode();

                removeEventEmission(contextHash,
                        eventEmissionHashCode,
                        eventName);
            } else {
                Log.e(TAG, "unsubscribe_event_emission_missing_event");
            }
        }else if (eventName == null){
            Log.e(TAG, "unsubscribeEvent - contextHash: "+contextHash);

            removeContextEvents(contextHash,
                    null,
                    1);

        }else if (eventName != null){
            Log.e(TAG, "unsubscribeEvent - contextHash: "+contextHash+" eventName: "+eventName);

            removeContextEvents(contextHash,
                    eventName,
                    2);
        }else{
            throw new  IllegalStateException("invalid_unsubscribe_state");
        }
    }

    private void removeEventEmission(Integer contextHash,
                                            Integer eventEmissionHashCode,
                                            String eventName){
        Map<Pair<Integer, Integer>, EventEmission> primaryMapValue = primaryMap.get(eventName); //EventMap {Key: Pair(ContextHashCode, EventHashCode), value: EventEmissionObject}

        Pair exactPair = new Pair(contextHash, eventEmissionHashCode); //Prepare Pair(ContextHashCode, EventHashCode)

        //Check pair exist
        if (primaryMapValue.containsKey(exactPair)) {
            primaryMapValue.remove(primaryMapValue); //remove

        }else{
            Log.e(TAG, "unsubscribe_context_missing_event_pair");
        }
    }

    private static void removeContextEvents(Integer contextHash,
                                            String eventName,
                                            Integer type){
        // 1-Context 2-Events
        List<String> primaryMapKeySet = new ArrayList(primaryMap.keySet());

        for (String primaryMapKey : primaryMapKeySet) {
            if (primaryMap.containsValue(primaryMapKey)) {
                Map<Pair<Integer, Integer>, EventEmission> innerMapPrimaryMap = primaryMap.get(primaryMapKey);

                List<Pair<Integer, Integer>> arrayList = new ArrayList(innerMapPrimaryMap.values());
                for (Pair<Integer, Integer> pair : arrayList) {
                    Integer pairContextHash = pair.first;
                    Integer pairEventName = pair.second;

                    switch (type) {
                        case 1: {
                            if (contextHash.equals(pairContextHash)) {
                                innerMapPrimaryMap.remove(pair); //remove
                                break;
                            }
                            break;
                        }
                        case 2: {
                            if (eventName.equals(pairEventName)) {
                                innerMapPrimaryMap.remove(pair); //remove
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private static void addOrUpdateSubscription(int contextHash,
                                                String eventName,
                                                EventEmission eventEmission) {
        int eventEmissionHash = eventEmission.hashCode();


        Pair<Integer,Integer> primaryMapValueKeyPair = new Pair(contextHash, eventEmissionHash);
        if (primaryMap.containsKey(eventName)) {
            Map<Pair<Integer, Integer>, EventEmission> primaryMapValue = primaryMap.get(eventName); //re-use stored map

            primaryMapValue.put(primaryMapValueKeyPair,eventEmission);
        }else{
            Map map = new HashMap();
            map.put(primaryMapValueKeyPair,eventEmission); //create and put value to map

            primaryMap.put(eventName, map); //insert-map-to-primary-map
        }
    }
}
