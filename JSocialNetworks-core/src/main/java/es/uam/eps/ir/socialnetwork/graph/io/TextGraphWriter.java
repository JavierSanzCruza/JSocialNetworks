/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.io;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.multigraph.MultiGraph;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Writes a graph to a file.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class TextGraphWriter<U> implements GraphWriter<U>
{
    /**
     * The delimiter for separating fields.
     */
    private final String delimiter;
    
    /**
     * Constructor.
     * @param delimiter the delimiter for separating fields.
     */
    public TextGraphWriter(String delimiter)
    {
        this.delimiter = delimiter;
    }
    
    @Override
    public boolean write(Graph<U> graph, String file)
    {
        try
        {
            return this.write(graph, new FileOutputStream(file));
        } 
        catch (FileNotFoundException ex)
        {
            return false;
        }
    }

    @Override
    public boolean write(Graph<U> graph, OutputStream file)
    {
        return this.write(graph, file, true, false);
    }

    @Override
    public boolean write(Graph<U> graph, String file, boolean writeWeights, boolean writeTypes)
    {
        try
        {
            return this.write(graph, new FileOutputStream(file), writeWeights, writeTypes);
        } 
        catch (FileNotFoundException ex)
        {
            return false;
        }
    }

    @Override
    public boolean write(Graph<U> graph, OutputStream file, boolean writeWeights, boolean writeTypes)
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(file));
            boolean ret;
            if(graph.isMultigraph())
                ret = writeMultiGraph((MultiGraph<U>) graph, bw, writeWeights, writeTypes);
            else
                ret =writeSimpleGraph(graph, bw, writeWeights, writeTypes);
            bw.close();
            return ret;
        } 
        catch (IOException ex)
        {
            return false;
        }
    }
    
    /**
     * Writes a multigraph into a file
     * @param graph The multigraph we want to write
     * @param bw the writer which will write the graph in the file
     * @param writeWeights Indicates if weights have to be written or not.
     * @param writeTypes Indicates if types have to be written or not.
     * @return true if everything went OK, false if not.
     */
    private boolean writeMultiGraph(MultiGraph<U> graph, BufferedWriter bw, boolean writeWeights, boolean writeTypes)
    {
        boolean directed = graph.isDirected();
        
        try
        {
            if(directed)
            {
                List<U> nodes = graph.getAllNodes().collect(Collectors.toCollection(ArrayList::new));
                for(U node : nodes)
                {
                    List<U> adjacentNodes = graph.getAdjacentNodes(node).collect(Collectors.toCollection(ArrayList::new));
                    for(U v : adjacentNodes)
                    {
                        int numEdges = graph.getNumEdges(node, v);
                        List<Double> weights = graph.getEdgeWeights(node, v);
                        List<Integer> types = graph.getEdgeTypes(node, v);
                        
                        for(int i = 0; i < numEdges; ++i)
                        {                           
                            bw.write(node.toString() + delimiter + v.toString());
                            if(writeWeights) bw.write(delimiter + weights.get(i));
                            if(writeTypes) bw.write(delimiter + types.get(i));
                            bw.write("\n");
                        }
                    }
                }
            }
            else
            {
                Set<U> visited = new HashSet<>();
                List<U> nodes = graph.getAllNodes().collect(Collectors.toCollection(ArrayList::new));
                for(U node : nodes)
                {
                    List<U> adjacentNodes = graph.getAdjacentNodes(node).filter(v -> !visited.contains(v)).collect(Collectors.toCollection(ArrayList::new));
                    for(U v : adjacentNodes)
                    {
                        int numEdges = graph.getNumEdges(node, v);
                        List<Double> weights = graph.getEdgeWeights(node, v);
                        List<Integer> types = graph.getEdgeTypes(node, v);
                        
                        for(int i = 0; i < numEdges; ++i)
                        {                           
                            bw.write(node.toString() + delimiter + v.toString());
                            if(writeWeights) bw.write(delimiter + weights.get(i));
                            if(writeTypes) bw.write(delimiter + types.get(i));
                            bw.write("\n");
                        }
                    }
                    
                    visited.add(node);
                }
            }
                
        }
        catch(IOException ex)
        {
            return false;
        }
        
        return true;
    }
    
    /**
     * Writes a simple graph into a file
     * @param graph The simple graph we want to write
     * @param bw The file
     * @param writeWeights Indicates if weights have to be written or not.
     * @param writeTypes Indicates if types have to be written or not.
     * @return true if everything went OK, false if not.
     */
    private boolean writeSimpleGraph(Graph<U> graph, BufferedWriter bw, boolean writeWeights, boolean writeTypes)
    {
        boolean directed = graph.isDirected();
        
        try
        {
            if(directed)
            {
                List<U> nodes = graph.getAllNodes().collect(Collectors.toCollection(ArrayList::new));
                for(U node : nodes)
                {
                    List<U> adjacentNodes = graph.getAdjacentNodes(node).collect(Collectors.toCollection(ArrayList::new));
                    for(U v : adjacentNodes)
                    {
                        Double weight = graph.getEdgeWeight(node, v);
                        Integer type = graph.getEdgeType(node, v);
                        
                        String toWrite = node.toString() + delimiter + v.toString();
                        
                        if(writeWeights) toWrite += delimiter + weight;
                        if(writeTypes) toWrite += delimiter + type;
                        toWrite += "\n";
                        
                        bw.write(toWrite);
                        
                        if((Long) node == 394266186)
                        {
                            System.err.println("");
                        }
                    }
                }
            }
            else
            {
                Set<U> visited = new HashSet<>();
                List<U> nodes = graph.getAllNodes().collect(Collectors.toCollection(ArrayList::new));
                for(U node : nodes)
                {
                    List<U> adjacentNodes = graph.getAdjacentNodes(node).filter(v -> !visited.contains(v)).collect(Collectors.toCollection(ArrayList::new));
                    for(U v : adjacentNodes)
                    {
                        Double weight = graph.getEdgeWeight(node, v);
                        Integer type = graph.getEdgeType(node, v);
                        
                        bw.write(node.toString() + delimiter + v.toString());
                        if(writeWeights) bw.write(delimiter + weight);
                        if(writeTypes) bw.write(delimiter + type);
                        bw.write("\n");
                        
                    }
                    
                    visited.add(node);
                }
            }
                
        }
        catch(IOException ex)
        {
            return false;
        }

        return true;
    }

    
}
