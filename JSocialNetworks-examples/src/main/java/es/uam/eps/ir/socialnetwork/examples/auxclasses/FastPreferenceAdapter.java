/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.auxclasses;

import java.util.ArrayList;
import java.util.List;
import org.ranksys.core.index.fast.FastItemIndex;
import org.ranksys.core.index.fast.FastUserIndex;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.core.preference.fast.IdxPref;

/**
 * Adapts some fast preference data for fulfilling some properties.
 * @author Javier Sanz-Cruzado Puig
 */
public class FastPreferenceAdapter 
{
    /**
     * Removes all preferences from a user to itself
     * @param uIndex user index.
     * @param iIndex item index.
     * @param data the original fast preference data.
     * @return the new preference data.
     */
    public static FastPreferenceData<Long, Long> removeAutoloops(FastUserIndex<Long> uIndex, FastItemIndex<Long> iIndex, FastPreferenceData<Long, Long> data)
    {
        
        List<List<IdxPref>> uidxList = new ArrayList<>();
        List<List<IdxPref>> iidxList = new ArrayList<>();
        
        data.getAllUidx().sorted().forEach(uidx -> 
        {
            Long u = data.uidx2user(uidx);
            uidxList.add(new ArrayList<>());
            data.getUidxPreferences(uidx).filter(iidx -> !u.equals(data.iidx2item(iidx.v1))).forEach(iidx -> 
            {
                IdxPref idx = new IdxPref(iidx.v1, iidx.v2);
                uidxList.get(uidx).add(idx);
            });            
        });
        
        data.getAllIidx().sorted().forEach(iidx -> 
        {
            Long v = data.iidx2item(iidx);
            iidxList.add(new ArrayList<>());
            data.getIidxPreferences(iidx).filter(uidx -> !v.equals(data.uidx2user(uidx.v1))).forEach(uidx -> 
            {
                IdxPref idx = new IdxPref(uidx.v1, uidx.v2);
                iidxList.get(iidx).add(idx);
            });            
        });
        
        int numPreferences = uidxList.stream().mapToInt(list -> list.size()).sum();
        
        return new FastPreferenceAdaptedData<>(numPreferences, uidxList, iidxList, uIndex, iIndex);
    }
    
    
    /**
     * Adds all preferences from a user to itself
     * @param uIndex user index.
     * @param iIndex item index.
     * @param data the original fast preference data.
     * @return the new preference data.
     */
    public static FastPreferenceData<Long, Long> addAllAutoloops(FastUserIndex<Long> uIndex, FastItemIndex<Long> iIndex, FastPreferenceData<Long, Long> data)
    {       
        List<List<IdxPref>> uidxList = new ArrayList<>();
        List<List<IdxPref>> iidxList = new ArrayList<>();
        
        data.getAllUidx().sorted().forEach(uidx -> 
        {
            Long u = data.uidx2user(uidx);
            uidxList.add(new ArrayList<>());
            data.getUidxPreferences(uidx).filter(iidx -> !u.equals(data.iidx2item(iidx.v1))).forEach(iidx -> 
            {
                IdxPref idx = new IdxPref(iidx.v1, iidx.v2);
                uidxList.get(uidx).add(idx);
            });
            
            IdxPref auxIdx = new IdxPref(data.item2iidx(u), 1.0);
            uidxList.get(uidx).add(auxIdx);
        });
        
        data.getAllIidx().sorted().forEach(iidx -> 
        {
            Long v = data.iidx2item(iidx);
            iidxList.add(new ArrayList<>());
            data.getIidxPreferences(iidx).filter(uidx -> !v.equals(data.uidx2user(uidx.v1))).forEach(uidx -> 
            {
                IdxPref idx = new IdxPref(uidx.v1, uidx.v2);
                iidxList.get(iidx).add(idx);
            });
            IdxPref auxIdx = new IdxPref(data.user2uidx(v), 1.0);
            iidxList.get(iidx).add(auxIdx);
        });
        
        int numPreferences = uidxList.stream().mapToInt(list -> list.size()).sum();
        
        return new FastPreferenceAdaptedData<>(numPreferences, uidxList, iidxList, uIndex, iIndex);
    }
}
