/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid;

import static es.uam.eps.ir.socialnetwork.grid.BasicTypeIdentifiers.BOOLEAN_TYPE;
import static es.uam.eps.ir.socialnetwork.grid.BasicTypeIdentifiers.DOUBLE_TYPE;
import static es.uam.eps.ir.socialnetwork.grid.BasicTypeIdentifiers.INTEGER_TYPE;
import static es.uam.eps.ir.socialnetwork.grid.BasicTypeIdentifiers.LONG_TYPE;
import static es.uam.eps.ir.socialnetwork.grid.BasicTypeIdentifiers.ORIENTATION_TYPE;
import static es.uam.eps.ir.socialnetwork.grid.BasicTypeIdentifiers.STRING_TYPE;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Reads grids.
 * @author Javier Sanz-Cruzado Puig
 */
public abstract class GridReader 
{
    /**
     * Identifier for the values of the parameter
     */
    private final static String VALUES = "values";
    /**
     * Identifier for the parameter name
     */
    private final static String NAME = "name";
    /**
     * Identifier for the parameter type
     */
    private final static String TYPE = "type";
    /**
     * Identifier for an individual parameter value
     */
    private final static String VALUE = "value";
    /**
     * Identifier for a range of values
     */
    private final static String RANGE = "range";
    /**
     * Identifier for the start of an interval
     */
    private final static String START = "start";
    /**
     * Identifier for the end of an interval
     */
    private final static String END = "end";
    /**
     * Identifier for the interval step
     */
    private final static String STEP = "step";
    
    /**
     * Reads the possible values for the parameters of an algorithm.
     * @param parameters XML nodes containing the parameters information
     * @return The grid
     */
    protected Grid readParameterGrid(NodeList parameters)
    {
        Map<String, List<Double>> doubleValues = new HashMap<>();
        Map<String, List<EdgeOrientation>> orientationValues = new HashMap<>();
        Map<String, List<String>> stringValues = new HashMap<>();
        Map<String, List<Integer>> integerValues = new HashMap<>();
        Map<String, List<Boolean>> booleanValues = new HashMap<>();
        Map<String, List<Long>> longValues = new HashMap<>();
        
        for(int i = 0; i < parameters.getLength(); ++i)
        {
            Element element = (Element) parameters.item(i);
            String parameterName = element.getElementsByTagName(NAME).item(0).getTextContent();
            String type = element.getElementsByTagName(TYPE).item(0).getTextContent();
            
            switch (type) {
                case INTEGER_TYPE:
                {
                    List<Integer> grid = readIntegerGrid((Element) element.getElementsByTagName(VALUES).item(0));
                    integerValues.put(parameterName, grid);
                    break;
                }
                case DOUBLE_TYPE:
                {
                    List<Double> grid = readDoubleGrid((Element) element.getElementsByTagName(VALUES).item(0));
                    doubleValues.put(parameterName, grid);
                    break;
                }
                case STRING_TYPE:
                {
                    List<String> grid =  readStringGrid((Element) element.getElementsByTagName(VALUES).item(0));
                    stringValues.put(parameterName, grid);
                    break;
                }
                case BOOLEAN_TYPE:
                {
                    List<Boolean> grid = readBooleanGrid((Element) element.getElementsByTagName(VALUES).item(0));
                    booleanValues.put(parameterName, grid);
                    break;
                }
                case ORIENTATION_TYPE:
                {
                    List<EdgeOrientation> grid = readOrientationGrid((Element) element.getElementsByTagName(VALUES).item(0));
                    orientationValues.put(parameterName, grid);
                    break;
                }
                case LONG_TYPE:
                {
                    List<Long> grid = readLongGrid((Element) element.getElementsByTagName(VALUES).item(0));
                    longValues.put(parameterName, grid);
                    break;
                }
                default:
                {
                    System.err.println("Unidentified type " + type);
                    break;
                }
            }
        }        
        return new Grid(doubleValues, orientationValues, stringValues, integerValues, booleanValues, longValues);
        
    }
    
    
    
    /**
     * Reads integer values from a grid
     * @param element XML element containing the different possible values for an integer attribute
     * @return The list of integer values
     */
    protected List<Integer> readIntegerGrid(Element element)
    {
        List<Integer> values = new ArrayList<>();
        
        //Case 1: Values
        NodeList valuesNodes = element.getElementsByTagName(VALUE);
        if(valuesNodes.getLength() > 0)
        {
            for(int i = 0; i < valuesNodes.getLength(); ++i)
            {
                String value = valuesNodes.item(i).getTextContent();
                values.add(new Integer(value));
            }
        }
        
        //Case 2: Ranges
        NodeList rangesNodes = element.getElementsByTagName(RANGE);
        if(rangesNodes.getLength() > 0)
        {
            for(int i = 0; i < rangesNodes.getLength(); ++i)
            {
                Element range = (Element) rangesNodes.item(i);
                Integer start = new Integer(range.getElementsByTagName(START).item(0).getTextContent());
                Integer end = new Integer(range.getElementsByTagName(END).item(0).getTextContent());
                Integer step = new Integer(range.getElementsByTagName(STEP).item(0).getTextContent());
                
                for(int j = start; j <= end; j += step)
                {
                    values.add(j);
                }
            }
        }
        
        return values;
    }

   /**
     * Reads long values from a grid
     * @param element XML element containing the different possible values for a long attribute
     * @return The list of long values
     */
    protected List<Long> readLongGrid(Element element)
    {
        List<Long> values = new ArrayList<>();
        
        //Case 1: Values
        NodeList valuesNodes = element.getElementsByTagName(VALUE);
        if(valuesNodes.getLength() > 0)
        {
            for(int i = 0; i < valuesNodes.getLength(); ++i)
            {
                String value = valuesNodes.item(i).getTextContent();
                values.add(new Long(value));
            }
        }
        
        //Case 2: Ranges
        NodeList rangesNodes = element.getElementsByTagName(RANGE);
        if(rangesNodes.getLength() > 0)
        {
            for(int i = 0; i < rangesNodes.getLength(); ++i)
            {
                Element range = (Element) rangesNodes.item(i);
                Long start = new Long(range.getElementsByTagName(START).item(0).getTextContent());
                Long end = new Long(range.getElementsByTagName(END).item(0).getTextContent());
                Long step = new Long(range.getElementsByTagName(STEP).item(0).getTextContent());
                
                for(long j = start; j <= end; j += step)
                {
                    values.add(j);
                }
            }
        }
        
        return values;
    }
    
   /**
     * Reads double values from a grid
     * @param element XML element containing the different possible values for a double attribute
     * @return The list of double values
     */
    protected List<Double> readDoubleGrid(Element element)
    {
        List<Double> values = new ArrayList<>();
        
        //Case 1: Values
        NodeList valuesNodes = element.getElementsByTagName(VALUE);
        if(valuesNodes.getLength() > 0)
        {
            for(int i = 0; i < valuesNodes.getLength(); ++i)
            {
                String value = valuesNodes.item(i).getTextContent();
                values.add(new Double(value));
            }
        }
        
        //Case 2: Ranges
        NodeList rangesNodes = element.getElementsByTagName(RANGE);
        if(rangesNodes.getLength() > 0)
        {
            for(int i = 0; i < rangesNodes.getLength(); ++i)
            {
                Element range = (Element) rangesNodes.item(i);
                Double start = new Double(range.getElementsByTagName(START).item(0).getTextContent());
                Double end = new Double(range.getElementsByTagName(END).item(0).getTextContent());
                Double step = new Double(range.getElementsByTagName(STEP).item(0).getTextContent());
                
                for(double j = start; j <= end; j += step)
                {
                    values.add(j);
                }
                
                if(!values.contains(end))
                    values.add(end);
            }
            
            
        }
        
        return values;
    }

    /**
     * Reads string values from a grid
     * @param element XML element containing the different possible values for a string attribute
     * @return The list of string values
     */
    protected List<String> readStringGrid(Element element)
    {
        List<String> values = new ArrayList<>();
        
        //Case 1: Values
        NodeList valuesNodes = element.getElementsByTagName(VALUE);
        if(valuesNodes.getLength() > 0)
        {
            for(int i = 0; i < valuesNodes.getLength(); ++i)
            {
                String value = valuesNodes.item(i).getTextContent();
                values.add(value);
            }
        }
        
        return values;
    }

    /**
     * Reads boolean values from a grid
     * @param element XML element containing the different possible values for a boolean attribute
     * @return The list of boolean values
     */
    protected List<Boolean> readBooleanGrid(Element element)
    {
        List<Boolean> values = new ArrayList<>();
        
        //Case 1: Values
        NodeList valuesNodes = element.getElementsByTagName(VALUE);
        if(valuesNodes.getLength() > 0)
        {
            for(int i = 0; i < valuesNodes.getLength(); ++i)
            {
                String value = valuesNodes.item(i).getTextContent();
                values.add(value.equalsIgnoreCase("true"));
            }
        }
        
        return values;
    }
    
    /**
     * Reads edge orientation values from a grid
     * @param element XML element containing the different possible values for an orientation attribute
     * @return The list of edge orientation values
     */
    protected List<EdgeOrientation> readOrientationGrid(Element element)
    {
        List<EdgeOrientation> values = new ArrayList<>();
        
        //Case 1: Values
        NodeList valuesNodes = element.getElementsByTagName(VALUE);
        if(valuesNodes.getLength() > 0)
        {
            for(int i = 0; i < valuesNodes.getLength(); ++i)
            {
                String value = valuesNodes.item(i).getTextContent();
                values.add(EdgeOrientation.valueOf(value));
            }
        }
        
        return values;
    }
}
