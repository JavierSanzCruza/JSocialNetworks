/*
 *  Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import java.util.*;

/**
 * Configuration parameters for an algorithm.
 * @author Javier Sanz-Cruzado Puig
 */
public class Parameters 
{
    /**
     * Double values for the different parameters
     */
    private final Map<String, Double> doubleValues;
    /**
     * Edge orientation values for the different parameters
     */
    private final Map<String, EdgeOrientation> orientationValues;
    /**
     * String values for the different parameters
     */
    private final Map<String, String> stringValues;
    /**
     * Integer values for the different parameters
     */
    private final Map<String, Integer> integerValues;
    /**
     * Boolean values for the different parameters.
     */
    private final Map<String, Boolean> booleanValues;
    /**
     * Long values for the parameters.
     */
    private final Map<String, Long> longValues;


    /**
     * Constructor
     * @param doubleValues Double values for the different parameters
     * @param orientationValues Edge orientation values for the different parameters
     * @param stringValues String values for the different parameters
     * @param integerValues Integer values for the different parameters
     * @param booleanValues Boolean values for the different parameters.
     * @param longValues Long values for the parameters.
     */
    public Parameters(Map<String, Double> doubleValues, Map<String, EdgeOrientation> orientationValues, Map<String, String> stringValues, Map<String, Integer> integerValues, Map<String, Boolean> booleanValues, Map<String, Long> longValues)
    {
        this.doubleValues = doubleValues;
        this.orientationValues = orientationValues;
        this.stringValues = stringValues;
        this.integerValues = integerValues;
        this.booleanValues = booleanValues;
        this.longValues = longValues;
    }

    /**
     * Empty constructor.
     */
    public Parameters()
    {
        doubleValues = new HashMap<>();
        orientationValues = new HashMap<>();
        stringValues = new HashMap<>();
        integerValues = new HashMap<>();
        booleanValues = new HashMap<>();
        longValues = new HashMap<>();
    }

    /**
     * 
     * @return Double values for the different parameters
     */
    public Map<String, Double> getDoubleValues()
    {
        return doubleValues;
    }

    /**
     * 
     * @return Edge orientation values for the different parameters
     */
    public Map<String, EdgeOrientation> getOrientationValues()
    {
        return orientationValues;
    }

    /**
     * 
     * @return String values for the different parameters
     */
    public Map<String, String> getStringValues()
    {
        return stringValues;
    }

    /**
     * 
     * @return Integer values for the different parameters
     */
    public Map<String, Integer> getIntegerValues()
    {
        return integerValues;
    }

    /**
     * 
     * @return Boolean values for the different parameters.
     */
    public Map<String, Boolean> getBooleanValues()
    {
        return booleanValues;
    }
    
    /**
     * 
     * @return Long values for the different parameters.
     */
    public Map<String, Long> getLongValues()
    {
        return longValues;
    }
    

    /**
     * Gets a double values for a single parameter.
     * @param paramName Parameter name
     * @return the values for that parameter
     */
    public Double getDoubleValue(String paramName)
    {
        return this.doubleValues.getOrDefault(paramName, null);
    }
    
    /**
     * Gets the integer values for a single parameter.
     * @param paramName Parameter name
     * @return the values for that parameter
     */
    public Integer getIntegerValue(String paramName)
    {
        return this.integerValues.getOrDefault(paramName, null);
    }
    
    /**
     * Gets the string values for a single parameter.
     * @param paramName Parameter name
     * @return the values for that parameter
     */
    public String getStringValue(String paramName)
    {
        return this.stringValues.getOrDefault(paramName, null);
    }
    
    /**
     * Gets the long values for a single parameter.
     * @param paramName Parameter name
     * @return the values for that parameter
     */
    public Long getLongValue(String paramName)
    {
        return this.longValues.getOrDefault(paramName, null);
    }
    
    /**
     * Gets the boolean values for a single parameter.
     * @param paramName Parameter name
     * @return the values for that parameter
     */
    public Boolean getBooleanValue(String paramName)
    {
        return this.booleanValues.getOrDefault(paramName, null);
    }

    /**
     * Gets the edge orientation values for a single parameter.
     * @param paramName Parameter name
     * @return the values for that parameter
     */
    public EdgeOrientation getOrientationValue(String paramName)
    {
        return this.orientationValues.getOrDefault(paramName, null);
    }
}
