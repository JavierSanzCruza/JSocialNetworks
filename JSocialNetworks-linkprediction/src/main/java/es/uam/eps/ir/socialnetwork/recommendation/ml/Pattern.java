/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.ml;

import java.util.List;

/**
 * Machine learning individual pattern.
 * @author Javier Sanz-Cruzado Puig
 */
public class Pattern
{
    private final int numAttrs;
    /**
     * Attributes values.
     */
    private final List<Double> values;
    /**
     * Class which the pattern belongs to.
     */
    private final int category;
            
    /**
     * Value to apply when the class is unknown or undefined.
     */
    public final static int ERROR_CLASS = -1;
    
    /**
     * Constructor.
     * @param values List that contains the values of the attributes.
     * @param category Class which the pattern belongs to..
     */
    protected Pattern(List<Double> values, int category)
    {
        this.values = values;
        this.category = category;
        this.numAttrs = values.size();
    }

    /**
     * Constructor. This is useful if you do not want to assign a class to the pattern.
     * @param values List that contains the values of the attributes.
     */
    protected Pattern(List<Double> values)
    {
        this(values, ERROR_CLASS);
    }

    
    /**
     * Gets the list of values for the different attributes in the pattern.
     * @return the list of attribute values.
     */
    public List<Double> getValues() {
        return values;
    }
    
    /**
     * Gets the value for a certain attribute.
     * @param attrId Index of the attribute
     * @return the value of the attribute.
     */
    public double getValue(int attrId)
    {
        if(attrId >= 0 && attrId < this.numAttrs)
        {
            return this.values.get(attrId);
        }
        else
        {
            return Double.NaN;
        }
    }

    /**
     * Gets the class of the pattern.
     * @return the class of the pattern.
     */
    public int getCategory() 
    {
        return category;
    }
}
