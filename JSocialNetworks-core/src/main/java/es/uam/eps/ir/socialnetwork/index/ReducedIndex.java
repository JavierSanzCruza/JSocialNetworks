/*
 *  Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.index;

/**
 * Index that cannot be modified
 * @author Javier Sanz-Cruzado Puig
 * @param <T> Type of the objects.
 */
public interface ReducedIndex<T>
{
    /**
     * Gets the index of a given object.
     * @param i Object to obtain
     * @return The index if the object exists, -1 if not.
     */
    public int object2idx(T i);
    /**
     * Gets the object corresponding to a certain index.
     * @param idx The index
     * @return The object correspoding to the index
     */
    public T idx2object(int idx);
}
