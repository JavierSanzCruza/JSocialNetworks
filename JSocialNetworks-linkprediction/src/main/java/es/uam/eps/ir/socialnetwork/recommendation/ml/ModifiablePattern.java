/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.ml;

/**
 * Class that represents a pattern whose elements can be modified.
 * @author Javier Sanz-Cruzado Puig
 */
public class ModifiablePattern {

    private final double[] pattern;
    /**
     * Constructor
     * @param numparams number of attributes of the pattern.
     */
    public ModifiablePattern(int numparams) {
        this.pattern = new double[numparams];
        for(int i = 0; i < numparams; ++i)
            this.pattern[i] = 0.0;
    }

    /**
     * Sets the value of the i-th coordinate of the vector.
     * @param i the coordinate of the vector we want to overwrite.
     * @param nextDouble the new value.
     */
    public void setValue(int i, double nextDouble) {
        if(i >= 0 && i < pattern.length)
            this.pattern[i] = nextDouble;
    }

    /**
     * Gets the value of the i-th coordinate of the pattern.
     * @param i the coordinate we want to read.
     * @return the value of that coordinate.
     */
    public double getValue(int i) {
        if(i >= 0 && i < pattern.length)
            return this.pattern[i];
        return Double.NaN;
    }
    
}
