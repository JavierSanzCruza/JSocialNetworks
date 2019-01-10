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
 * Search grid for a single algorithm.
 * @author Javier Sanz-Cruzado Puig
 */
public class Grid
{
    /**
     * Double values for the different parameters
     */
    private final Map<String, List<Double>> doubleValues;
    /**
     * Edge orientation values for the different parameters
     */
    private final Map<String, List<EdgeOrientation>> orientationValues;
    /**
     * String values for the different parameters
     */
    private final Map<String, List<String>> stringValues;
    /**
     * Integer values for the different parameters
     */
    private final Map<String, List<Integer>> integerValues;
    /**
     * Boolean values for the different parameters.
     */
    private final Map<String, List<Boolean>> booleanValues;
    /**
     * Long values for the parameters.
     */
    private final Map<String, List<Long>> longValues;

    /**
     * Constructor
     * @param doubleValues Double values for the different parameters
     * @param orientationValues Edge orientation values for the different parameters
     * @param stringValues String values for the different parameters
     * @param integerValues Integer values for the different parameters
     * @param booleanValues Boolean values for the different parameters.
     * @param longValues Long values for the parameters.
     */
    public Grid(Map<String, List<Double>> doubleValues, Map<String, List<EdgeOrientation>> orientationValues, Map<String, List<String>> stringValues, Map<String, List<Integer>> integerValues, Map<String, List<Boolean>> booleanValues, Map<String, List<Long>> longValues)
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
    public Grid()
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
    public Map<String, List<Double>> getDoubleValues()
    {
        return doubleValues;
    }

    /**
     * 
     * @return Edge orientation values for the different parameters
     */
    public Map<String, List<EdgeOrientation>> getOrientationValues()
    {
        return orientationValues;
    }

    /**
     * 
     * @return String values for the different parameters
     */
    public Map<String, List<String>> getStringValues()
    {
        return stringValues;
    }

    /**
     * 
     * @return Integer values for the different parameters
     */
    public Map<String, List<Integer>> getIntegerValues()
    {
        return integerValues;
    }

    /**
     * 
     * @return Boolean values for the different parameters.
     */
    public Map<String, List<Boolean>> getBooleanValues()
    {
        return booleanValues;
    }
    
    /**
     * 
     * @return Long values for the different parameters.
     */
    public Map<String, List<Long>> getLongValues()
    {
        return longValues;
    }
    
    /**
     * Gets a double values for a single parameter.
     * @param paramName Parameter name
     * @return the values for that parameter
     */
    public List<Double> getDoubleValues(String paramName)
    {
        return this.doubleValues.getOrDefault(paramName, new ArrayList<>());
    }
    
    /**
     * Gets the integer values for a single parameter.
     * @param paramName Parameter name
     * @return the values for that parameter
     */
    public List<Integer> getIntegerValues(String paramName)
    {
        return this.integerValues.getOrDefault(paramName, new ArrayList<>());
    }
    
    /**
     * Gets the string values for a single parameter.
     * @param paramName Parameter name
     * @return the values for that parameter
     */
    public List<String> getStringValues(String paramName)
    {
        return this.stringValues.getOrDefault(paramName, new ArrayList<>());
    }
    
    /**
     * Gets the long values for a single parameter.
     * @param paramName Parameter name
     * @return the values for that parameter
     */
    public List<Long> getLongValues(String paramName)
    {
        return this.longValues.getOrDefault(paramName, new ArrayList<>());
    }
    
    /**
     * Gets the boolean values for a single parameter.
     * @param paramName Parameter name
     * @return the values for that parameter
     */
    public List<Boolean> getBooleanValues(String paramName)
    {
        return this.booleanValues.getOrDefault(paramName, new ArrayList<>());
    }

    /**
     * Gets the edge orientation values for a single parameter.
     * @param paramName Parameter name
     * @return the values for that parameter
     */
    public List<EdgeOrientation> getOrientationValues(String paramName)
    {
        return this.orientationValues.getOrDefault(paramName, new ArrayList<>());
    }
}
