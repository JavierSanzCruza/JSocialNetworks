/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.utils.datatypes;

import java.util.Objects;

/**
 * Class that represents a pair of objects of different type.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Objects of the first type.
 * @param <I> Objects of the second type.
 */
public class Tuple2oo<U,I>
{
    /**
     * First object
     */
    private final U first;
    /**
     * Second object
     */
    private final I second;
    
    /**
     * Constructor.
     * @param first First object.
     * @param second Second object.
     */
    public Tuple2oo (U first, I second)       
    {
        this.first = first;
        this.second = second;
    }
    
    /**
     * Gets the first object of the pair.
     * @return the first object of the pair.
     */
    public U v1()
    {
        return first;
    }
    
    /**
     * Gets the second object of the pair.
     * @return the second object of the pair.
     */
    public I v2()
    {
        return second;
    }
    
    @Override
    public boolean equals(Object u)
    {
        if(u.getClass().equals(this.getClass()))
        {
            Tuple2oo<?,?> pair = (Tuple2oo<?,?>) u;
            return first.equals(pair.first) && second.equals(pair.second);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.first);
        hash = 83 * hash + Objects.hashCode(this.second);
        return hash;
    }
}


