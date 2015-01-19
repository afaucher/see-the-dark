package com.beanfarmergames.seethedark.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AgingList<T> {
    private final float maxAgeSeconds;
    private final List<AgedElement<T>> agedList = new ArrayList<AgedElement<T>>();

    public AgingList(float maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;

    }

    public Collection<T> getElementCollection() {
        Collection<T> c = new ArrayList<T>();
        for (AgedElement<T> e : agedList) {
            c.add(e.getE());
        }
        return c;
    }
    
    public List<AgedElement<T>> getScaledAgedCollection(float t) {
        List<AgedElement<T>> c = new ArrayList<AgedElement<T>>(agedList.size());
        for (AgedElement<T> e : agedList) {
            float scaledAge = Math.min(1.0f,(t - e.getT()) / maxAgeSeconds);
            scaledAge = Math.max(0.0f, scaledAge);
            c.add(new AgedElement<T>(scaledAge, e.getE()));
        }
        return c;
    }

    public void appendCollection(float t, Collection<T> c) {
        for (T e : c) {
            agedList.add(new AgedElement<T>(t, e));
        }
    }

    public void purge(float t) {

        Collection<AgedElement<T>> toRemove = new ArrayList<AgedElement<T>>();
        float minimumAge = t - maxAgeSeconds;

        for (AgedElement<T> e : agedList) {
            if (e.getT() < minimumAge) {
                toRemove.add(e);
            }
        }
        agedList.removeAll(toRemove);
    }

}
