/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.partition.weights;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.Weight;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyMultiGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import es.uam.eps.ir.socialnetwork.partition.AbstractPartition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jooq.lambda.tuple.Tuple3;

/**
 * Class that performs a partition of the graph depending on the values of the weights.
 * Given a percentage, the top X% of the links will go to the test set, while the rest
 * will go to the training set.
 * 
 * This allows temporal partitions indicating the training and test ratio (partitioning graphs
 * with temporal marks as weights. Graphs will maintain the weights. In the case of multigraphs,
 * a same edge can appear in both training and test sets (with different threshold values).
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <V> type of the nodes of the graph.
 */ 
public class PercentageWeightsPartition<V> extends AbstractPartition<V>
{
    /**
     * Percentage of edges that will go to train (approximately).
     */
    private final double percentage;
 
    
    /**
     * Constructor.
     * @param percentage The ratio of training set.
     */
    public PercentageWeightsPartition(double percentage)
    {
        this.percentage = percentage;
    }

    @Override
    public boolean doPartition(Graph<V> graph) 
    {
        if(graph == null)
            return false;
        
        boolean directed = graph.isDirected();
        boolean weighted = graph.isWeighted();
        
        EmptyMultiGraphGenerator<V> gg = new EmptyMultiGraphGenerator<>();
        gg.configure(directed, weighted);
        
        try {
            this.train = gg.generate();
            this.test = gg.generate();
        } catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) {
            return false;
        }
        
        Comparator<Tuple3<V,V,Double>> comparator = (Tuple3<V,V,Double> x, Tuple3<V,V,Double> y) -> 
        {
            if(x.v3 < y.v3)
                return -1;
            else if(y.v3 < x.v3)
                return 1;
            return 0;
        };
        
        int num = new Double(percentage*graph.getEdgeCount()).intValue();
        
        // Add all nodes to the training and test graphs.
        graph.getAllNodes().forEach((node) -> 
        {
            this.train.addNode(node);
            this.test.addNode(node);
        });
        
        List<Tuple3<V,V,Double>> links = new ArrayList<>();
        
        graph.getAllNodes().forEach(u -> 
        {
            graph.getAdjacentNodesWeights(u).forEach(weight -> links.add(new Tuple3<>(u,weight.getIdx(), weight.getValue())));
        });
        
        links.sort(comparator);
        
        IntStream.range(0, num).forEach(i -> 
        {
            this.train.addEdge(links.get(i).v1, links.get(i).v2, links.get(i).v3);
        });
        
        IntStream.range(num, links.size()).forEach(i -> 
        {
            this.test.addEdge(links.get(i).v1, links.get(i).v2, links.get(i).v3);
        });
            
       return true;
    }
}
