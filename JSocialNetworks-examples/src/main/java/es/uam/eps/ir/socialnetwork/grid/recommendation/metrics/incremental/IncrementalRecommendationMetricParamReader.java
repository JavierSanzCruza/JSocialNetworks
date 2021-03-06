/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.incremental;

import es.uam.eps.ir.socialnetwork.grid.Parameters;
import es.uam.eps.ir.socialnetwork.grid.ParametersReader;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Reads the values for a recommendation metric
 * @author Javier Sanz-Cruzado Puig
 */
public class IncrementalRecommendationMetricParamReader extends ParametersReader
{
    /**
     * Identifier for the name of the metric
     */
    private final static String NAME = "name";
    /**
     * Identifier for the parameters
     */
    private final static String PARAM = "param";
    /**
     * Name of the metric
     */
    private String name;
    /**
     * Parameter values for the metric
     */
    private Parameters values;
    
    /**
     * Reads the elements of a recommendation metric
     * @param node the node containing the information for that metric.
     */
    public void readMetric(Element node)
    {
        this.name = node.getElementsByTagName(NAME).item(0).getTextContent();
        
        NodeList params = node.getElementsByTagName(PARAM);
        if(params == null || params.getLength() == 0)
        {
            this.values = new Parameters();
        }
        else
        {
            this.values = this.readParameterGrid(params);
        }
    }

    /**
     * Obtains the name of the recommendation metric.
     * @return the name of the recommendation metric.
     */
    public String getName() 
    {
        return name;
    }

    /**
     * Obtains the values of the parameters of the recommendation metric.
     * @return the values of the parameters
     */
    public Parameters getParams() 
    {
        return values;
    }
    
    /**
     * Shows the configuration of a recommendation metric.
     * @return a string containing the configuration of the metric.
     */
    public String printRelevanceModel() 
    {
        String selection = "";
        
        selection += this.getName() + "\n";
        
        selection += this.values.getBooleanValues()
                .entrySet()
                .stream()
                .map(entry -> "\t\t" + entry.getKey() + ": " + entry.getValue() + "\n")
                .reduce("", (x,y) -> x + y);
        
        selection += this.values.getDoubleValues()
                .entrySet()
                .stream()
                .map(entry -> "\t\t" + entry.getKey() + ": " + entry.getValue() + "\n")
                .reduce("", (x,y) -> x + y);
        
        selection += this.values.getIntegerValues()
                .entrySet()
                .stream()
                .map(entry -> "\t\t" + entry.getKey() + ": " + entry.getValue() + "\n")
                .reduce("", (x,y) -> x + y);
        
        selection += this.values.getLongValues()
                .entrySet()
                .stream()
                .map(entry -> "\t\t" + entry.getKey() + ": " + entry.getValue() + "\n")
                .reduce("", (x,y) -> x + y);
        
        selection += this.values.getStringValues()
                .entrySet()
                .stream()
                .map(entry -> "\t\t" + entry.getKey() + ": " + entry.getValue() + "\n")
                .reduce("", (x,y) -> x + y);
        
        selection += this.values.getOrientationValues()
                .entrySet()
                .stream()
                .map(entry -> "\t\t" + entry.getKey() + ": " + entry.getValue() + "\n")
                .reduce("", (x,y) -> x + y);

        return selection;
    }
    
}
