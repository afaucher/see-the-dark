package com.beanfarmergames.seethedark.util;

import java.util.Comparator;

public class AgedElementComparator<T> implements Comparator<AgedElement> {

    @Override
    public int compare(AgedElement o1, AgedElement o2) {
        if (o1.getT() > o2.getT())
            return -1;
        if (o1.getT() < o2.getT())
            return 1;

        return 0;
    }

}