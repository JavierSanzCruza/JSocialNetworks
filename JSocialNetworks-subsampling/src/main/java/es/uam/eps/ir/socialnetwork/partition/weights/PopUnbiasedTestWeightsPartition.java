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
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import es.uam.eps.ir.socialnetwork.metrics.vertex.Degree;
import es.uam.eps.ir.socialnetwork.partition.AbstractPartition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.ranksys.core.util.tuples.Tuple2od;

/**
 * Class for splitting a graph in training and test set to prevent popularity biases.
 * Selects the same number of test ratings for each candidate item.
 * 
 * Bellogín, A., Castells, P., Cantador, I. Statistical biases in Information Retrieval Metrics for Recommender Systems. Information Retrieval Journal 20(6), 606-634 (2017)
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <V> type of the nodes of the graph.
 * 
 * 
 */ 
public class PopUnbiasedTestWeightsPartition<V> extends AbstractPartition<V>
{
    /**
     * Minimum ratio of test items per user
     */
    private final double epsilon;
    /**
     * Split ratio (R_train / R)
     */
    private final double sigma;

    
    /**
     * Constructor.
     * @param epsilon Minimum ratio of test items per user
     * @param sigma Split ratio of the training set.
     */
    public PopUnbiasedTestWeightsPartition(double epsilon, double sigma)
    {
        this.epsilon = epsilon;
        this.sigma = sigma;
    }

    @Override
    public boolean doPartition(Graph<V> graph) 
    {
        try 
        {
            Degree<V> degree = new Degree<>(EdgeOrientation.IN);

            List<Tuple2od<V>> list = new ArrayList<>();

            graph.getAllNodes().forEach(u -> 
            {
                list.add(new Tuple2od<>(u, degree.compute(graph, u)));
            });

            // Sort the items according to their popularity
            Comparator<Tuple2od<V>> comparator = (Tuple2od<V> x, Tuple2od<V> y) -> 
            {
                if(x.v2 < y.v2)
                {
                    return 1;
                }
                else if(y.v2 < x.v2)
                {
                    return -1;
                }

                return 0;

            };

            list.sort(comparator);

            // Find the number of candidate users (zeta) and the number of training ratings for each user (nu)
            int zeta = 0;

            for(int i = 1; i < list.size(); ++i)
            {
                double aux = (1.0-this.epsilon)*list.get(i-1).v2()*i/(graph.getEdgeCount()+0.0);
                if(aux > (1.0-this.sigma))
                    zeta = i;
            }

            double nu = (zeta == 0) ? 0.0 : (1-this.epsilon)*list.get(zeta-1).v2();
            
            int num = new Double(Math.floor(nu)).intValue();
            Set<V> candidates = new HashSet<>();
            for(int i = 0; i < zeta; ++i)
            {
                candidates.add(list.get(i).v1());
            }
                
            boolean directed = graph.isDirected();
            boolean weighted = graph.isWeighted();
            
            // Configure the split.
            EmptyGraphGenerator<V> gg = new EmptyGraphGenerator<>();
            gg.configure(directed, weighted);
            this.train = gg.generate();
            this.test = gg.generate();
            
            graph.getAllNodes().forEach((node)->{
                this.train.addNode(node);
                this.test.addNode(node);
            });
            
            // Classify in train / test
            for(V candidate : candidates)
            {
                List<Tuple2od<V>> links = graph.getIncidentNodesWeights(candidate)
                    .map(w -> new Tuple2od<V>(w.getIdx(), w.getValue()))
                    .sorted(comparator.reversed())
                    .collect(Collectors.toCollection(ArrayList::new));
                
                IntStream.range(0, links.size() - num).forEach(i -> 
                {
                   this.train.addEdge(links.get(i).v1, candidate, links.get(i).v2); 
                });
                
                IntStream.range(links.size() - num, links.size()).forEach(i ->
                {
                    this.test.addEdge(links.get(i).v1, candidate, links.get(i).v2);
                });
            }
            
            System.out.println("perc:" + (1.0 - sigma) + " epsilon:" + epsilon + " zeta: " + zeta + " nu:" + nu);

            return true;
        } 
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) 
        {
            return false;
        }
    } 
}
