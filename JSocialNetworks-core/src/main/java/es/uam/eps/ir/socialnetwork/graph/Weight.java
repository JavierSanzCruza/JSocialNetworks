/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph;


/**
 * Class for expressing weights
 * @author Javier Sanz-Cruzado Puig
 * @param <I> The type of the identifier.
 * @param <W> The type of the weight.
 */
public class Weight<I, W>
{
    /**
     * Identifier of the weight
     */
    private final I idx;
    /**
     * Value of the weight
     */
    private final W weight;
    
    /**
     * Constructor.
     * @param idx identifier.
     * @param weight value.
     */
    public Weight(I idx, W weight)
    {
        this.idx = idx;
        this.weight = weight;
    }
    
    /**
     * Gets the identifier of the weight.
     * @return the identifier of the weight.
     */
    public I getIdx() { return this.idx; }
    
    /**
     * Gets the value of the weight.
     * @return the value of the weight.
     */
    public W getValue() { return this.weight; }
    
}
