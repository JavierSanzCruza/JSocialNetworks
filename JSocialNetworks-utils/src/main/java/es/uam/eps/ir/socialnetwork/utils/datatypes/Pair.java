/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.utils.datatypes;

/**
 * Class that represents a pair of objects of the same type.
 * @author Javier Sanz-Cruzado Puig.
 * @param <U> type of the objects
 */
public class Pair<U> extends Tuple2oo<U,U>
{
    /**
     * Constructor
     * @param first First element.
     * @param second Second element.
     */
    public Pair (U first, U second)       
    {
        super(first, second);
    }
}
