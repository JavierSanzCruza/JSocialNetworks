/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.metrics;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Interface for node pair based metrics.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public interface PairMetric<U>
{
    /**
     * Computes the value of the metric for a single pair of nodes
     * @param graph The full graph.
     * @param orig The origin node of the edge.
     * @param dest The destiny node of the edge.
     * @return The value of the metric for that edge.
     */
    public double compute(Graph<U> graph, U orig, U dest);
    /**
     * Computes the value of the metric for all the possible pairs in the graph.
     * @param graph The full graph.
     * @return A map containing the metrics for each node.
     */
    public Map<Pair<U>, Double> compute(Graph<U> graph);
    
    /**
     * Computes the value of the metric for a selection of pairs in the graph.
     * @param graph The full graph.
     * @param pairs A stream containing the selected pairs.
     * @return A map containing the metrics for each pair in the stream that exists.
     */
    public Map<Pair<U>, Double> compute(Graph<U> graph, Stream<Pair<U>> pairs);
    
    /**
     * Computes the average value of the metric
     * @param graph The full graph.
     * @return The average value of the metric
     */
    public double averageValue(Graph<U> graph);
    
    /**
     * Computes the average value of a certain group of pairs.
     * @param graph the full graph.
     * @param pair A stream containing the selected pairs.
     * @param pairCount The number of pairs in the stream.
     * @return The average value of the metric for those pairs.
     */
    public double averageValue(Graph<U> graph, Stream<Pair<U>> pair, int pairCount);
}
