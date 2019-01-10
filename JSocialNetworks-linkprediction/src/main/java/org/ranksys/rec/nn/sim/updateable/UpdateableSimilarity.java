/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.rec.nn.sim.updateable;

import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import java.util.List;
import org.ranksys.recommenders.nn.sim.Similarity;

/**
 * Generic updateable similarity for fast data. This is the interface that is 
 * under the hood of user and item similarities. It does not need to be
 * symmetric
 * 
 * @author Javier Sanz-Cruzado (javier.sanz-cruzado@uam.es)
 */
public interface UpdateableSimilarity extends Similarity
{
    /**
     * Updates the similarities between user/items after adding/modifying a
     * rating.
     * @param idx1 index of user
     * @param idx2 index of item
     * @param val rating value
     * @return A pair. Contains a) a list of users which have been fully updated
     * b) a list of other updated similarities.
     */
    public Tuple2oo<List<Integer>,List<Pair<Integer>>> updateAdd(int idx1, int idx2, double val);
    
    /**
     * Updates the similarities between users/items after removing a rating.
     * @param idx1 index of user
     * @param idx2 index of item
     */
    public void updateDel(int idx1, int idx2);
    
    /**
     * Updates the similarities between users/items after adding a new element.
     */
    public void updateAddElement();
}
