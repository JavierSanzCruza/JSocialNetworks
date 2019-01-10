/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.community;

import es.uam.eps.ir.socialnetwork.grid.Parameters;
import es.uam.eps.ir.socialnetwork.grid.ParametersReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
 * Reads the values for a algorithms mechanism.
 * @author Javier Sanz-Cruzado Puig
 */
public class CommunityDetectionParamReader extends ParametersReader
{
    /**
     * Identifier for the name of the mechanism
     */
    private final static String NAME = "name";
    /**
     * Identifier for the parameters
     */
    private final static String PARAM = "param";
    /**
     * The set of parameters for the community detection algorithms.
     */
    private final Map<String, Parameters> communityDetectionAlgorithms;
    /**
     * The name of the file
     */
    private final String file;

    /**
     * Constructor.
     * @param file The name of the file containing the parameters for the different algorithms.
     */
    public CommunityDetectionParamReader(String file)
    {
        this.file = file;
        this.communityDetectionAlgorithms = new HashMap<>();
    }
    
    /**
     * Reads a XML document containing the parameters
     */
    public void readDocument()
    {
        try
        {
            // First of all, obtain the XML document
            File inputFile = new File(file);
            communityDetectionAlgorithms.clear();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            
            Element parent = doc.getDocumentElement();
            parent.normalize();
            
            NodeList nodeList = parent.getChildNodes();
            for(int i = 0; i < nodeList.getLength(); ++i)
            {
                Node node = nodeList.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element element = (Element) node;
                    this.readAlgorithm(element);
                }
            }
        }
        catch(ParserConfigurationException | SAXException | IOException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    
    /**
     * Reads the elements of a algorithms mechanism.
     * @param node the node containing the information for that algorithms mechanism.
     */
    public void readAlgorithm(Element node)
    {
        String name = node.getElementsByTagName(NAME).item(0).getTextContent();
        
        NodeList params = node.getElementsByTagName(PARAM);
        Parameters par;
        if(params == null || params.getLength() == 0)
        {
            par = new Parameters();
        }
        else
        {
            par = this.readParameterGrid(params);
        }
        
        this.communityDetectionAlgorithms.put(name, par);
    }

    /**
     * Gets the set of community detection algorithms previously read from the XML file.
     * @return the set of algorithms.
     */
    public Set<String> getAlgorithms()
    {
        Set<String> algorithms = this.communityDetectionAlgorithms.keySet();
        return algorithms;
    }
    
    /**
     * Gets the parameters for the given algorithm.
     * @param algorithm The community detection algorithm to search.
     * @return The parameters if it exists, an empty parameter object if not.
     */
    public Parameters getParameters(String algorithm)
    {
        return this.communityDetectionAlgorithms.getOrDefault(algorithm, new Parameters());
    }
    
    /**
     * Shows the configuration of the different algorithms.
     * @return a string containing the configuration of the different community detection algorithms.
     */
    public String printCommunityDetectionAlgorithms() 
    {
        String algorithms = "";
        for(String algorithm : this.communityDetectionAlgorithms.keySet())
        {
            algorithms += algorithm + "\n";
            Parameters values = this.communityDetectionAlgorithms.get(algorithm);
            
            algorithms += values.getBooleanValues()
                .entrySet()
                .stream()
                .map(entry -> "\t" + entry.getKey() + ": " + entry.getValue() + "\n")
                .reduce("", (x,y) -> x + y);
        
            algorithms += values.getDoubleValues()
                    .entrySet()
                    .stream()
                    .map(entry -> "\t" + entry.getKey() + ": " + entry.getValue() + "\n")
                    .reduce("", (x,y) -> x + y);

            algorithms += values.getIntegerValues()
                    .entrySet()
                    .stream()
                    .map(entry -> "\t" + entry.getKey() + ": " + entry.getValue() + "\n")
                    .reduce("", (x,y) -> x + y);

            algorithms += values.getLongValues()
                    .entrySet()
                    .stream()
                    .map(entry -> "\t" + entry.getKey() + ": " + entry.getValue() + "\n")
                    .reduce("", (x,y) -> x + y);

            algorithms += values.getStringValues()
                    .entrySet()
                    .stream()
                    .map(entry -> "\t" + entry.getKey() + ": " + entry.getValue() + "\n")
                    .reduce("", (x,y) -> x + y);

            algorithms += values.getOrientationValues()
                    .entrySet()
                    .stream()
                    .map(entry -> "\t" + entry.getKey() + ": " + entry.getValue() + "\n")
                    .reduce("", (x,y) -> x + y);
        }
        
        return algorithms;
    }
}
