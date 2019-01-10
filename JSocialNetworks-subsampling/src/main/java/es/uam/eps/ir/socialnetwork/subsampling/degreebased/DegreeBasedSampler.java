/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.subsampling.degreebased;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import es.uam.eps.ir.socialnetwork.index.Index;
import es.uam.eps.ir.socialnetwork.index.fast.FastIndex;
import es.uam.eps.ir.socialnetwork.subsampling.Sampler;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates subsamples. To select a node from the original graph, a probability
 * mixing uniform probability and proportional-to-degree probability is computed.
 * @author Javier
 * @param <V> type of the vertice
 */
public class DegreeBasedSampler<V> implements Sampler<V>
{

    /**
     * This parameter establishes the probability with which the node is sampled
     * proportionally to the degree or uniformly.
     */
    private final double lambda;
    /**
     * Vertex index.
     */
    private Index<V> idx;
    /**
     * Probability that a node is selected for the sample.
     */
    private List<Double> probabilities;
    
    /**
     * Constructor.
     * @param lambda probability of sampling the node proportionally to the degree or uniformly. 
     */
    public DegreeBasedSampler(double lambda)
    {
        this.lambda = lambda;
        this.idx = null;
        this.probabilities = null;
    }
    
    @Override
    public Graph<V> sample(Graph<V> fullGraph, double percentage) 
    {
        if(fullGraph == null || percentage < 0.0 || percentage > 1.0)
        {
            return null;
        }
            
        
        Double num = Math.ceil((fullGraph.getAllNodes().count() + 0.0) * percentage);
        return this.sample(fullGraph, num.intValue());
    }

    @Override
    public Graph<V> sample(Graph<V> fullGraph, int num) 
    {   
        try 
        {
            if(fullGraph == null || num < 0 || (num > fullGraph.getVertexCount()))
            {
                return null;
            }
            boolean directed = fullGraph.isDirected();
            boolean weighted = fullGraph.isWeighted();
            
            EmptyGraphGenerator<V> gg = new EmptyGraphGenerator<>();
            gg.configure(directed, weighted);
            Graph<V> subsample = gg.generate();
            
            generateSamplingProbabilities(fullGraph, directed);
            
            Random rng = new Random();
            // Subsample the different nodes
            while(subsample.getVertexCount() < num)
            {
                double r = rng.nextDouble();
                double value = 0.0;

                int index = -1;

                do
                {
                    value += this.probabilities.get(index+1);
                    ++index;
                }
                while(r > value && index < this.probabilities.size()-1);

                if(!subsample.containsVertex(idx.idx2object(index)))
                    subsample.addNode(idx.idx2object(index));
            }

            System.out.println("Subsample size: " + subsample.getVertexCount());
            // Add the edges
            subsample.getAllNodes().forEach(node -> {                
                fullGraph.getAdjacentNodes(node).forEach(neigh -> {
                    if(subsample.containsVertex(neigh) && !subsample.containsEdge(node, neigh))
                        subsample.addEdge(node, neigh);
                });
            });

            System.out.println("Subsample size: " + subsample.getVertexCount());

            
            // Clear the index and the probability list, as they will not be used again.
            this.idx = null;
            this.probabilities = null;
            
            // Return the subsample graph
            return subsample;
        } 
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) 
        {
            return null;
        }
    }

    /**
     * For each node in the network, generates the probability of a node to be sampled.
     * @param fullGraph The original graph.
     * @param directed indicates if the graph is directed.
     */
    private void generateSamplingProbabilities(Graph<V> fullGraph, boolean directed) 
    {
        this.probabilities = new ArrayList<>();
        this.idx = new FastIndex<>();
        if(directed)
        {
            fullGraph.getAllNodes().forEach(node -> {
                this.idx.addObject(node);
                double prob = (1-this.lambda)/(fullGraph.getEdgeCount() + 0.0);
                prob += this.lambda*(fullGraph.degree(node)+0.0)/(2 * fullGraph.getEdgeCount() + 0.0);
                this.probabilities.add(prob);
            });
        }
        else
        {
            fullGraph.getAllNodes().forEach(node -> 
            {
                this.idx.addObject(node);
                double prob = (1-this.lambda)/(fullGraph.getEdgeCount() + 0.0);
                prob += this.lambda*(fullGraph.degree(node)+0.0)/(fullGraph.getEdgeCount() + 0.0);
                this.probabilities.add(prob);
            });
        }
    }
    
}
