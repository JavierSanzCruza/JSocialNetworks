/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.edges;

/**
 * Indicates the orientation of the edges to take.
 * @author Sofia Marina Pepa
 */
public enum EdgeOrientation {

    
    OUT, IN, UND, MUTUAL;

    /**
     * Given an edge orientation, returns the opposite value.
     * @return the opposite orientation.
     */
    public EdgeOrientation invertSelection() 
    {
        if (null == this) 
        {
            return this;
        } 
        else switch (this)
        {
            case OUT:
                return IN;
            case IN:
                return OUT;
            default:
                return this;
        }
    }
    
    /**
     * Selection for the complementary graph
     * @return the selection for the complementary graph.
     */
    public EdgeOrientation complementarySelection()
    {
        if(null == this)
        {
            return this;
        }
        else switch(this)
        {
            case UND:
                return MUTUAL;
            case MUTUAL:
                return UND;
            default:
                return this;
        }
    }
}
