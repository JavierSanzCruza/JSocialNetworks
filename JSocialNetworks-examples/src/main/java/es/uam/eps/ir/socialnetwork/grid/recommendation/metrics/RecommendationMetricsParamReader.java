/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics;

import es.uam.eps.ir.socialnetwork.grid.ParametersReader;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.discount.DiscountParamReader;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.RecommendationMetricParamReader;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.relevance.RelevanceParamReader;
import java.util.*;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads the parameters for the metrics used to evaluate a set of recommenders
 * @author Javier Sanz-Cruzado Puig
 */
public class RecommendationMetricsParamReader extends ParametersReader
{
    /**
     * Identifier for the relevance model.
     */
    private final static String RELEVANCE = "relevance";
    /**
     * Identifier for the ranking discount model.
     */
    private final static String DISCOUNT = "discount";
    /**
     * Identifier for the metrics
     */
    private final static String METRICS = "metrics";
    /**
     * Identifier for the individual metric
     */
    private final static String METRIC = "metric";
    /**
     * Parameters for the relevance model.
     */
    private RelevanceParamReader relevanceParams;
    /**
     * Parameters for the ranking discount model.
     */
    private DiscountParamReader discountParams;
    /**
     * Parameters for the different metrics.
     */
    private final List<RecommendationMetricParamReader> recMetricParams;
    /**
     * The name of the file
     */
    private final String file;
    
    /**
     * Constructor.
     * @param file File that contains the parameters data 
     */
    public RecommendationMetricsParamReader(String file)
    {
        this.file = file;
        
        this.recMetricParams = new ArrayList<>();
    }
    
    /**
     * Reads a XML document containing a grid
     */
    public void readDocument()
    {
        relevanceParams = null;
        discountParams = null;
        

        recMetricParams.clear();
        
        try
        {
            // First of all, obtain the XML Document
            File inputFile = new File(file);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            
            Element parent = doc.getDocumentElement();
            parent.normalize();
            
            
            // Read the relevance model
            NodeList protocolList = parent.getElementsByTagName(RELEVANCE);
            Node protocol = protocolList.item(0);
            this.readRelevance((Element) protocol);
            
            // Read the discount model
            NodeList discountList = parent.getElementsByTagName(DISCOUNT);
            Node discount = discountList.item(0);
            this.readDiscount((Element) discount);
            
            // Read the metrics
            NodeList metricsList = parent.getElementsByTagName(METRICS);
            Node node = metricsList.item(0);
            this.readMetrics((Element) node);            
        } 
        catch (ParserConfigurationException | SAXException | IOException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    /**
     * Reads the grid for a single algorithm.
     * @param element The XML Element containing the algorithm information
     */
    private void readRelevance(Element element)
    {
        // Read the protocol
        RelevanceParamReader ppr = new RelevanceParamReader();
        ppr.readRelevance((Element) element);
        this.relevanceParams = ppr;
    }
    
    /**
     * Reads the grid for a single algorithm.
     * @param element The XML Element containing the algorithm information
     */
    private void readDiscount(Element element)
    {
        // Read the protocol
        DiscountParamReader dpr = new DiscountParamReader();
        dpr.readDiscount((Element) element);
        this.discountParams = dpr;
        
        
    }
    
    /**
     * Reads the configuration parameters for a list of information propagation metrics.
     * @param node The node containing the list of metrics.
     */
    private void readMetrics(Element node) 
    {
        NodeList metricsList = node.getElementsByTagName(METRIC);
        for(int i = 0; i < metricsList.getLength(); ++i)
        {
            Node metricElem = metricsList.item(i);
            RecommendationMetricParamReader mpr = new RecommendationMetricParamReader();
            mpr.readMetric((Element) metricElem);
            this.recMetricParams.add(mpr);
        }
    }
    
    /**
     * Obtains the parameters for the relevance model
     * @return the relevance model parameters.
     */
    public RelevanceParamReader getRelevanceModelParams()
    {
        return this.relevanceParams;
    }
    
    /**
     * Obtains the parameters for the ranking discount model.
     * @return the ranking discount model parameters.
     */
    public DiscountParamReader getDiscountModelParams()
    {
        return this.discountParams;
    }
    
    /**
     * Obtains the parameters for all the metrics.
     * @return a list containing the parameters for all the metrics.
     */
    public List<RecommendationMetricParamReader> getMetricsParams()
    {
        return this.recMetricParams;
    }
    
    /**
     * Obtains the parameters for a particular metric.
     * @param num the number of the metric.
     * @return the parameters for the given metric if OK, null if not.
     */
    public RecommendationMetricParamReader getMetricParams(int num)
    {
        if(num > 0 && num < this.recMetricParams.size())
            return this.recMetricParams.get(num);
        return null;
    }
    
    

}
