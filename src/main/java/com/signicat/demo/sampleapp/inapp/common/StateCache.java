package com.signicat.demo.sampleapp.inapp.common;

import com.signicat.demo.sampleapp.inapp.common.exception.ApplicationException;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class StateCache {

    private final Map<String, String> pendingStates;

    public StateCache() {
        pendingStates = new HashMap<>();
    }

    public String storeToStateCache(final String state) {
        final String u = UUID.randomUUID().toString();
        pendingStates.put(u, state);
        return u;
    }

    public void validateState(final String key, final String receivedState) {
        final String foundState = pendingStates.remove(key);
        if (foundState == null) {
            throw new ApplicationException("There is no cached state with given key");
        }

        if (!foundState.equals(receivedState)) {
            throw new ApplicationException("state values do not match");
        }
    }
}
