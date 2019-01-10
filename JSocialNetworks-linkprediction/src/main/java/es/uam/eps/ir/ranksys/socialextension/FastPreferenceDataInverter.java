/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.ranksys.socialextension;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.ranksys.core.index.fast.FastItemIndex;
import org.ranksys.core.index.fast.FastUserIndex;
import org.ranksys.core.preference.IdPref;
import org.ranksys.core.preference.fast.FastPointWisePreferenceData;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.core.preference.fast.IdxPref;
import org.ranksys.core.preference.fast.StreamsAbstractFastPreferenceData;

/**
 * Class for inverting fast preference data.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 * @param <I> Type of the items
 */
public class FastPreferenceDataInverter<U,I> extends StreamsAbstractFastPreferenceData<U, I> implements FastPointWisePreferenceData<U, I>, Serializable
{
    /**
     * Number of preferences.
     */
    private final int numPreferences;
    /**
     * User preferences
     */
    private final List<List<IdxPref>> uidxList;
    /**
     * Item preferences
     */
    private final List<List<IdxPref>> iidxList;
        
    /**
     * Builds an abstract fast preference data, starting from a given one.
     * @param users User index.
     * @param items Item index.
     * @param prefData Original preference data.
     */
    private FastPreferenceDataInverter(FastUserIndex<U> users, FastItemIndex<I> items, FastPreferenceData<U,I> prefData) {
        super(users, items);
        
        Int2ObjectMap<Set<Integer>> userPreferences = new Int2ObjectOpenHashMap<>();
        Int2ObjectMap<Set<Integer>> itemPreferences = new Int2ObjectOpenHashMap<>();
        
        users.getAllUidx().forEach(uidx -> {
            Set<Integer> set = new HashSet<>();
            prefData.getUidxPreferences(uidx).forEach(pref -> set.add(pref.v1));
            userPreferences.put(uidx, set);
        });
        
        items.getAllIidx().forEach(iidx -> {
            Set<Integer> set = new HashSet<>();
            prefData.getIidxPreferences(iidx).forEach(pref -> set.add(pref.v1));
            itemPreferences.put(iidx, set);
        });
        
        List<List<IdxPref>> userPrefList = new ArrayList<>();
        List<List<IdxPref>> itemPrefList = new ArrayList<>();
        
        users.getAllUidx().forEach(uidx -> userPrefList.add(new ArrayList<>()));
        items.getAllIidx().forEach(iidx -> itemPrefList.add(new ArrayList<>()));
        
        users.getAllUidx().sorted().forEach(uidx -> {
            items.getAllIidx().sorted().forEach(iidx -> {
                userPrefList.add(new ArrayList<>());
                boolean add = true;
                if(userPreferences.containsKey(uidx))
                {
                    if(userPreferences.get(uidx).contains(iidx))
                    {
                        add = false;
                    }
                }
                
                if(add)
                {
                    userPrefList.get(uidx).add(new IdxPref(iidx, 1.0));
                    itemPrefList.get(iidx).add(new IdxPref(uidx, 1.0));
                }
            });
        });
        
        this.uidxList = userPrefList;
        this.iidxList = itemPrefList;
        this.numPreferences = this.numUsers()*this.numItems() - prefData.numPreferences();
    }
   
    
    
    @Override
    public int numUsers(int iidx) {
        if (iidxList.get(iidx) == null) {
            return 0;
        }
        return iidxList.get(iidx).size();
    }

    @Override
    public int numItems(int uidx) {
        if (uidxList.get(uidx) == null) {
            return 0;
        }
        return uidxList.get(uidx).size();
    }

    @Override
    public Stream<IdxPref> getUidxPreferences(int uidx) {
        if (uidxList.get(uidx) == null) {
            return Stream.empty();
        } else {
            return uidxList.get(uidx).stream();
        }
    }

    @Override
    public Stream<IdxPref> getIidxPreferences(int iidx) {
        if (iidxList.get(iidx) == null) {
            return Stream.empty();
        } else {
            return iidxList.get(iidx).stream();
        }
    }

    @Override
    public int numPreferences() {
        return numPreferences;
    }

    @Override
    public IntStream getUidxWithPreferences() {
        return IntStream.range(0, numUsers())
                .filter(uidx -> uidxList.get(uidx) != null);
    }

    @Override
    public IntStream getIidxWithPreferences() {
        return IntStream.range(0, numItems())
                .filter(iidx -> iidxList.get(iidx) != null);
    }

    @Override
    public int numUsersWithPreferences() {
        return (int) uidxList.stream()
                .filter(Objects::nonNull).count();
    }

    @Override
    public int numItemsWithPreferences() {
        return (int) iidxList.stream()
                .filter(Objects::nonNull).count();
    }

    @Override
    public Optional<IdxPref> getPreference(int uidx, int iidx) {
        List<IdxPref> uList = uidxList.get(uidx);

        int low = 0;
        int high = uList.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            IdxPref p = uList.get(mid);
            int cmp = Integer.compare(p.v1, iidx);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return Optional.of(p);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<? extends IdPref<I>> getPreference(U u, I i) {
        Optional<? extends IdxPref> pref = getPreference(user2uidx(u), item2iidx(i));

        if (!pref.isPresent()) {
            return Optional.empty();
        } else {
            return Optional.of(uPrefFun.apply(pref.get()));
        }
    }
    
    /**
     * Inverts a fast preference data object.
     * @param <U> type of the users.
     * @param <I> type of the items.
     * @param userIndex user index.
     * @param itemIndex item index.
     * @param data preference data to invert.
     * @return the inverted fast preference data.
     */
    public static <U,I> FastPreferenceData<U,I> invert(FastUserIndex<U> userIndex, FastItemIndex<I> itemIndex, FastPreferenceData<U,I> data)
    {
        return new FastPreferenceDataInverter<>(userIndex, itemIndex, data);
    }
}
