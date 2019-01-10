/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.generator;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.multigraph.MultiGraph;
import es.uam.eps.ir.socialnetwork.graph.multigraph.fast.FastDirectedUnweightedMultiGraph;
import es.uam.eps.ir.socialnetwork.graph.multigraph.fast.FastDirectedWeightedMultiGraph;
import es.uam.eps.ir.socialnetwork.graph.multigraph.fast.FastUndirectedUnweightedMultiGraph;
import es.uam.eps.ir.socialnetwork.graph.multigraph.fast.FastUndirectedWeightedMultiGraph;
import es.uam.eps.ir.socialnetwork.utils.generator.Generator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generator for multigraphs.
 * @author Javier Sanz-Cruzado Puig
 * @param <V> Type of the graphs.
 */
@Deprecated
public class FastMultiGraphGenerator<V> implements MultiGraphGenerator<V>
{

    @Override
    public Graph<V> generateEmptyGraph(boolean directed, boolean weighted)
    {
        MultiGraph<V> graph;
        if(directed)
            if(weighted)
                graph = new FastDirectedWeightedMultiGraph<>();
            else
                graph = new FastDirectedUnweightedMultiGraph<>();
        else
            if(weighted)
                graph = new FastUndirectedWeightedMultiGraph<>();
            else
                graph = new FastUndirectedUnweightedMultiGraph<>();
        
        return graph;    
    }

    @Override
    public Graph<V> generateRandomGraph(boolean directed, int numNodes, double prob, Generator<V> generator) {
        MultiGraph<V> graph = (MultiGraph<V>) this.generateEmptyGraph(directed, false);
        Random rand = new Random();
        for(int i = 0; i < numNodes; ++i)
        {
            graph.addNode(generator.generate());
        }
        
        List<V> nodes = graph.getAllNodes().collect(Collectors.toCollection(ArrayList::new));
        double numEdges = Math.ceil(prob*numNodes*(numNodes-1));
        
        for(int i = 0; i < numEdges; ++i)
        {
            V node1 = nodes.get(rand.nextInt(nodes.size()));
            V node2 = nodes.get(rand.nextInt(nodes.size()));

            if(!graph.containsEdge(node1, node2))
            {
                graph.addEdge(node1, node2);
            }
            else
            {
                --i;
            }
        }        
        return graph;
    }

    @Override
    public Graph<V> generatePreferentialAttachmentGraph(boolean directed, int initialNodes, int numIter, int numEdgesIter, Generator<V> generator) 
    {
        if(numEdgesIter > initialNodes)
            return null;
        
        MultiGraph<V> graph = (MultiGraph<V>) this.generateEmptyGraph(directed, false);
        Random rand = new Random();
        Map<V, Integer> inDegrees = new HashMap<>();
        //Generate the initial nodes for the random graph.
        for(int i = 0; i < initialNodes; ++i)
        {
            V node = generator.generate();
            graph.addNode(node);
            inDegrees.put(node, initialNodes-1);
        }
       
        // Create the connected component of the graph
        if(directed)
        {
            graph.getAllNodes().forEach((orig)->{
                graph.getAllNodes().forEach((dest)->{
                    if(!orig.equals(dest))
                        graph.addEdge(orig, dest);
                });
            });
        }
        else
        {
            List<V> visited = new ArrayList<>();
            graph.getAllNodes().forEach((orig)->{
                graph.getAllNodes().forEach((dest)->{
                    if(!orig.equals(dest) && !visited.contains(dest))
                    {
                        graph.addEdge(orig, dest);
                    }
                });
                visited.add(orig);
            });
        }
        
        
        
        for(int i = 0; i < numIter; i++)
        {
            Set<V> newLinks = new HashSet<>();
            long numEdges = graph.getEdgeCount();
            // Select the new edges
            while(newLinks.size() < numEdgesIter)
            {    
                double aux = 0.0;
                double rnd = rand.nextDouble();
                for(Map.Entry<V,Integer> entry : inDegrees.entrySet())
                {
                    aux += (entry.getValue()+0.0)/(graph.getEdgeCount()+0.0);
                    if(aux > rnd)
                    {
                        newLinks.add(entry.getKey());
                        break;
                    }
                }
            }
            
            V newNode = generator.generate();
            graph.addNode(newNode);
            if(!directed)
                inDegrees.put(newNode, numEdgesIter);
            
            newLinks.stream().forEach((node)->{
                graph.addEdge(newNode, node);
                inDegrees.put(node, inDegrees.get(node) + 1);
            });            
        }
        
        
        
        
        return graph;
        
        
        
        
        
    }
    
}
