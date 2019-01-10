/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.metrics.distance;

import com.google.common.util.concurrent.AtomicDouble;
import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.community.detection.CommunityDetectionAlgorithm;
import es.uam.eps.ir.socialnetwork.community.detection.connectedness.StronglyConnectedComponents;
import es.uam.eps.ir.socialnetwork.graph.DirectedGraph;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.GraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Computes some of the distance based metrics: distances, number of geodesic paths between two nodes, betweenness.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 * 
 * Finding and Evaluating Community Structure in Networks. Newman, M.E.J, Girvan, M., Physical Review E 69(2): 026113, February 2004.
 * Networks: An Introduction. Newman, M.E.J., Oxford University Press, 2010.
 */
public class DistanceCalculator<U> 
{
    /**
     * Graph the betweenness metrics are built for
     */
    private Graph<U> graph;
    /**
     * Nodes betweenness map
     */
    private Map<U,Double> nodeBetweenness;
    /**
     * Edge betweenness map
     */
    private Map<U,Map<U, Double>> edgeBetweenness;
    /**
     * Distances map
     */
    private Map<U,Map<U, Double>> distances;
    /**
     * Number of minimum distance paths between two nodes
     */
    private Map<U, Map<U, Double>> geodesics;

    /**
     * Strongly connected components
     */
    private Communities<U> scc;
    
    /**
     * Constructor
     */
    public DistanceCalculator()
    {
        this.graph = null;        
    }
    
    /**
     * Computes the betweenness of a graph.
     * @param graph the graph.
     * @return true if everything went ok.
     */
    public boolean computeDistances(Graph<U> graph)
    {
        if(this.graph != null && this.graph.equals(graph))
            return true;
        
        CommunityDetectionAlgorithm<U> cda = new StronglyConnectedComponents<>();

        this.scc = cda.detectCommunities(graph);
        return this.computeDistances(graph, scc);
        
    }
    
    /**
     * Computes the DistanceCalculator metrics for the graph.
     * @param graph Graph.
     * @param scc Strongly Connected Components.
     * @return true if everything went OK, false if not.
     */
    public boolean computeDistances(Graph<U> graph, Communities<U> scc)
    {   
        if(this.graph != null && this.graph.equals(graph))
            return true;
        
        // Initialize the values for node betweenness, edge betweenness and distances
        nodeBetweenness = new HashMap<>();
        edgeBetweenness = new HashMap<>();
        distances = new HashMap<>();
        geodesics = new HashMap<>();

        long numNodes = graph.getVertexCount();
        boolean weighted = graph.isWeighted();
        
        GraphGenerator<U> gf = new EmptyGraphGenerator<>();
        gf.configure(true, weighted);
        
        
        graph.getAllNodes().forEach(node -> this.nodeBetweenness.put(node, 0.0));
        graph.getAllNodes().forEach(node -> {
            this.edgeBetweenness.put(node, new HashMap<>());
            graph.getAdjacentNodes(node).forEach(adj -> 
            {
                this.edgeBetweenness.get(node).put(adj,0.0);
            });
        });
        
        graph.getAllNodes().forEach(orig -> {
            this.distances.put(orig, new HashMap<>());
            this.geodesics.put(orig, new HashMap<>());
            graph.getAllNodes().forEach(dest -> {
                this.distances.get(orig).put(dest, Double.POSITIVE_INFINITY);
                this.geodesics.get(orig).put(dest, 0.0);
            });
        });
        
        // For each vertex in the graph: 
        Set<U> vertices = graph.getAllNodes().collect(Collectors.toCollection(HashSet::new));
        for(U u : vertices)
        {
            
            DirectedGraph<U> tree;
            try {
                tree = (DirectedGraph<U>) gf.generate();
            } catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) {
                return false;
            }
            tree.addNode(u);
            
            // STEP 1: Compute the weights and distances.
            AtomicDouble d = new AtomicDouble();
            d.set(0.0);
            
            // Weights (number of paths between the nodes and the source node, u)
            Map<U, Double> weights = new HashMap<>();
            weights.put(u, 1.0);
            
            // Distances (distances between the nodes and the source node, u)
            Map<U, Double> dist = new HashMap<>();
            dist.put(u, d.get());
            
            Queue<U> queue = new LinkedList<>();
            Queue<U> nextLevelQueue = new LinkedList<>();
            
            Map<Double, Set<U>> levels = new HashMap<>();
            levels.put(d.get(),new HashSet<>());
            levels.get(d.get()).add(u);
            levels.put(d.get() + 1.0, new HashSet<>());
            
            
            queue.add(u);
            while(!queue.isEmpty())
            {
                U current = queue.poll();
                
                graph.getAdjacentNodes(current).forEach(node -> 
                {
                    if(!dist.containsKey(node))
                    {                        
                        dist.put(node, d.get()+1.0);                       
                        weights.put(node, weights.get(current));
                        nextLevelQueue.add(node);
                        levels.get(d.get()+1.0).add(node);
                        tree.addNode(node);
                        tree.addEdge(node, current);
                    }
                    else if(dist.get(node).equals(d.get() + 1.0))
                    {
                        weights.put(node, weights.get(current) + weights.get(node));
                        tree.addEdge(node, current);
                    }
                    // else { do nothing }
                });
                
                if(queue.isEmpty())
                {
                    while(!nextLevelQueue.isEmpty())
                    {
                        queue.add(nextLevelQueue.poll());
                    }
                    levels.put(d.addAndGet(1.0)+1.0,new HashSet<>());
                }
            }
            
            // STEP 2: Compute the node and edge betweenness
            double level = d.get() - 1.0;
                        
            Map<U, Double> nodeBetw = new HashMap<>();
            Map<U, Map<U,Double>> edgeBetw = new HashMap<>();
            Map<U, Double> accumulated = new HashMap<>();
            while(level >= 0.0)
            {
                Set<U> ithLevel = levels.get(level);
                ithLevel.forEach(node -> 
                {
                    if(tree.inDegree(node) == 0) // Leaf
                    {
                        nodeBetw.put(node, 0.0);
                        edgeBetw.put(node, new HashMap<>());
                        tree.getAdjacentNodes(node).forEach(adj -> {
                            double value = weights.get(adj)/weights.get(node);
                            edgeBetw.get(node).put(adj, value);
                            if(!accumulated.containsKey(adj))
                            {
                                accumulated.put(adj, 0.0);
                            }
                            accumulated.put(adj, accumulated.get(adj) + value);
                        });
                    }
                    else // Not leaf
                    {
                        double score = tree.getIncidentNodes(node).mapToDouble(incid -> {
                            double nodeb = nodeBetw.get(incid);
                            double weightA = weights.get(node);
                            double weightB = weights.get(incid);
                            return (1+nodeb)*weightA/weightB;            
                        }).sum();
                        nodeBetw.put(node, score);
                        edgeBetw.put(node, new HashMap<>());
                        tree.getAdjacentNodes(node).forEach(adj -> 
                        {
                            double value = weights.get(adj)/weights.get(node);
                            value = value*(1.0 + accumulated.get(node));
                            edgeBetw.get(node).put(adj, value);
                            if(!accumulated.containsKey(adj))
                            {
                                accumulated.put(adj, 0.0);
                            }
                            accumulated.put(adj, accumulated.get(adj) + value);
                        });
                    }                                
                });
                level--;    
            }

            // Update the distance map and the number of geodesic paths between nodes
            dist.entrySet().stream().forEach(entry -> 
            {
                this.distances.get(u).put(entry.getKey(), entry.getValue());
                this.geodesics.get(u).put(entry.getKey(), weights.get(entry.getKey()));
            });
            
            // Update the node betweenness map
            nodeBetw.entrySet().stream().forEach(entry -> 
            {
                if(!entry.getKey().equals(u))
                {
                    this.nodeBetweenness.put(entry.getKey(), this.nodeBetweenness.get(entry.getKey()) + entry.getValue());
                }
            });
            
            // Update the edge betweenness map
            edgeBetw.entrySet().forEach(entry -> 
            {
                entry.getValue().entrySet().forEach(inner -> 
                {
                    this.edgeBetweenness.get(inner.getKey()).put(entry.getKey(), this.edgeBetweenness.get(inner.getKey()).get(entry.getKey()) + inner.getValue());
                }); 
            });
        }
        
        
        // Apply normalization
        double val = (graph.isDirected() ? 1 : 0.5)*(numNodes-2)*(numNodes-1);
        this.nodeBetweenness.keySet().stream().forEach((u) -> {
            this.nodeBetweenness.put(u, this.nodeBetweenness.get(u)/val);
        });
        
        double val2 = (graph.isDirected() ? 1 : 0.5)*numNodes*(numNodes-1);
        this.edgeBetweenness.keySet().stream().forEach((u) -> {
            this.edgeBetweenness.get(u).keySet().stream().forEach((v) -> {
                this.edgeBetweenness.get(u).put(v, this.edgeBetweenness.get(u).get(v)/val2);
            });
        });
        
        
        this.graph = graph;
        return true;
    }
    
    
    /**
     * Returns the node betweenness for each node in the network.
     * @return a map containing the node betweenness for each node.
     */
    public Map<U, Double> getNodeBetweenness()
    {
        return this.nodeBetweenness;
    }
    
    /**
     * Gets the value of node betweenness for a single node.
     * @param node the value for the node.
     * @return the node betweenness for that node.
     */
    public double getNodeBetweenness(U node)
    {
        return this.nodeBetweenness.get(node);
    }
    
    /**
     * Gets all the values of the edge betweenness
     * @return the edge betweenness value for each edge.
     */
    public Map<U, Map<U,Double>> getEdgeBetweenness()
    {
        return this.edgeBetweenness;
    }
    
    /**
     * Returns the edge betweenness of all the adjacent edges to a given node.
     * @param node The node.
     * @return a map containing the values of edge betweenness for all the adjacent links to the given node.
     */
    public Map<U,Double> getEdgeBetweenness(U node)
    {
        if(this.edgeBetweenness.containsKey(node))
            return this.edgeBetweenness.get(node);
        return new HashMap<>();
    }
    
    /**
     * Returns the edge betweenness of a single edge.
     * @param orig origin node of the edge.
     * @param dest destination node of the edge.
     * @return the betweenness if the edge exists, -1.0 if not.
     */
    public double getEdgeBetweenness(U orig, U dest)
    {
        if(this.edgeBetweenness.containsKey(orig))
            if(this.edgeBetweenness.get(orig).containsKey(dest))
                return this.edgeBetweenness.get(orig).get(dest);
        return -1.0;
    }
    
    /**
     * Returns all the distances between different pairs.
     * @return the distances between pairs.
     */
    public Map<U, Map<U, Double>> getDistances()
    {
        return this.distances;
    }
    
    /**
     * Return the distances between a node and the rest of nodes in the network.
     * @param node the node.
     * @return a map containing all the distances from the node to the rest of the network.
     */
    public Map<U, Double> getDistances(U node)
    {
        if(this.distances.containsKey(node))
        {
            return this.distances.get(node);
        }
        return new HashMap<>();
    }
    
    /**
     * Returns the distance between two nodes.
     * @param orig origin node.
     * @param dest destination node.
     * @return the distance between both nodes. if there is a path between them, +Infinity if not.
     */
    public double getDistances(U orig, U dest)
    {
        if(this.distances.containsKey(orig))
            if(this.distances.get(orig).containsKey(dest))
                return this.distances.get(orig).get(dest);
        return Double.POSITIVE_INFINITY;
    }
    
    /**
     * Returns the number of geodesic paths between different pairs.
     * @return the distances between pairs.
     */
    public Map<U, Map<U, Double>> getGeodesics()
    {
        return this.geodesics;
    }
    
    /**
     * Return the number of geodesic paths between a node and the rest of nodes in the network.
     * @param node the node.
     * @return a map containing the number of geodesic paths from the node to the rest of the network.
     */
    public Map<U, Double> getGeodesics(U node)
    {
        if(this.geodesics.containsKey(node))
        {
            return this.geodesics.get(node);
        }
        return new HashMap<>();
    }
    
    /**
     * Returns the number of geodesic paths between two nodes.
     * @param orig origin node.
     * @param dest destination node.
     * @return the number of geodesic paths between both nodes if there is a path between them, 0.0 if not.
     */
    public double getGeodesics(U orig, U dest)
    {
        if(this.geodesics.containsKey(orig))
            if(this.geodesics.get(orig).containsKey(dest))
                return this.geodesics.get(orig).get(dest);
        return 0.0;
    }
    
    
    
    /**
     * Returns the strongly connected components for the given graph.
     * @return the strongly connected components.
     */
    public Communities<U> getSCC()
    {
        return this.scc;
    }
}
