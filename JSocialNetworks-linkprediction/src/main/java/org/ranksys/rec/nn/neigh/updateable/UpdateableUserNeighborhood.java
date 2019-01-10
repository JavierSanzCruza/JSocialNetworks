/* 
 * Copyright (C) 2015 Information Retrieval Group at Universidad Autónoma
 * de Madrid, http://ir.ii.uam.es
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.rec.nn.neigh.updateable;

import org.ranksys.core.util.tuples.Tuple2id;
import org.ranksys.core.util.tuples.Tuple2od;

import java.util.stream.Stream;
import org.ranksys.core.index.fast.updateable.FastUpdateableUserIndex;

/**
 * Updateable version of a user neighborhood. Wraps a generic neighborhood and a fast user index.
 *
 * @author Javier Sanz-Cruzado (javier.sanz-cruzado@uam.es)
 * @author Saúl Vargas (saul.vargas@uam.es)
 * 
 * @param <U> type of the users
 */
public class UpdateableUserNeighborhood<U> implements FastUpdateableUserIndex<U> {

    /**
     * Fast user index.
     */
    protected final FastUpdateableUserIndex<U> uIndex;

    /**
     * Generic fast neighborhood.
     */
    protected final UpdateableNeighborhood neighborhood;

    /**
     * Constructor
     *
     * @param uIndex fast user index
     * @param neighborhood generic fast neighborhood
     */
    public UpdateableUserNeighborhood(FastUpdateableUserIndex<U> uIndex, UpdateableNeighborhood neighborhood) 
    {
        this.uIndex = uIndex;
        this.neighborhood = neighborhood;
    }

    
    
    public UpdateableNeighborhood neighborhood() {
        return neighborhood;
    }

    @Override
    public int numUsers() {
        return uIndex.numUsers();
    }

    @Override
    public int user2uidx(U u) {
        return uIndex.user2uidx(u);
    }

    @Override
    public U uidx2user(int uidx) {
        return uIndex.uidx2user(uidx);
    }

    /**
     * Returns the neighborhood of a user/index.
     *
     * @param idx user/index whose neighborhood is calculated
     * @return stream of user/item-similarity pairs.
     */
    public Stream<Tuple2id> getNeighbors(int idx) {
        return neighborhood.getNeighbors(idx);
    }

    /**
     * Returns a stream of user neighbors
     *
     * @param u user whose neighborhood is returned
     * @return a stream of user-score pairs
     */
    public Stream<Tuple2od<U>> getNeighbors(U u) {
        return getNeighbors(user2uidx(u))
                .map(this::uidx2user);
    }

    @Override
    public int addUser(U u) 
    {
        int numUsers = this.uIndex.numUsers();
        int idx = this.uIndex.addUser(u);
        if(idx == numUsers)
            neighborhood.updateAddElement();
        return idx;
    }

    @Override
    public boolean containsUser(U u) 
    {
        return uIndex.containsUser(u);
    }

    @Override
    public Stream<U> getAllUsers() 
    {
        return uIndex.getAllUsers();
    }
    
    public void updateAdd(int uidx, int iidx, double val)
    {
        this.neighborhood.updateAdd(uidx, iidx, val);
    }
    
    public void updateDelete(int uidx, int iidx)
    {
        this.neighborhood.updateDelete(uidx, iidx);
    }
}