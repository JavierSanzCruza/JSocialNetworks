/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.index;

import java.util.stream.Stream;

/**
 * Interface for defining the relation of a set of objects with themselves.
 * @author Javier Sanz-Cruzado Puig
 * @param <W> Type of the weights.
 */
public interface AutoRelation<W> extends Relation<W>
{
    
    /**
     * Gets the total number of elements of the first item in the relation
     * @return The number of different elements of the first item in the relation
     */
    @Override
    public default int numSecond()
    {
        return this.numFirst();
    }
    
    /**
     * Gets the total number of elements of the second item related to a first item.
     * @param firstIdx Identifier of the first item
     * @return The second item count
     */
    @Override
    public default int numSecond(int firstIdx)
    {
        return this.numFirst(firstIdx);
    }
    
    /**
     * Gets all the elements of the second type
     * @return A stream containing all the elements.
     */
    @Override
    public default Stream<Integer> getAllSecond()
    {
        return this.getAllFirst();
    }
    

    @Override
    public default boolean addSecondItem(int secondIdx)
    {
        return addFirstItem(secondIdx);
    }
    
    /**
     * Removes an element. The identifiers of all remaining elements will
     * be reduced by 1.
     * @param idx the identifier of the element.
     * @return true if everything went OK, false otherwise.
     */
    public boolean remove(int idx);
}
