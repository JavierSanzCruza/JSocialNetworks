/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.auxclasses;

import java.util.List;
import org.ranksys.core.index.fast.FastItemIndex;
import org.ranksys.core.index.fast.FastUserIndex;
import org.ranksys.core.preference.fast.IdxPref;
import org.ranksys.core.preference.fast.SimpleFastPreferenceData;

/**
 * Fast preference data with public constructor.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public class FastPreferenceAdaptedData<U, I> extends SimpleFastPreferenceData<U,I>
{
    /**
     * Constructor.
     * @param numPreferences Number of preferences
     * @param uidxList List of preferences for each user
     * @param iidxList List of preferences for each item
     * @param uIndex User index
     * @param iIndex Item index
     */
    public FastPreferenceAdaptedData(int numPreferences, List<List<IdxPref>> uidxList, List<List<IdxPref>> iidxList, FastUserIndex<U> uIndex, FastItemIndex<I> iIndex)
    {
        super(numPreferences, uidxList, iidxList, uIndex, iIndex);
    }
}
