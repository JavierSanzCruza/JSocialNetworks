/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.partition;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.util.function.Predicate;

/**
 * Interface for partitioning a social network graph in train and test.
 * @author Javier Sanz-Cruzado Puig
 * @param <V> The type of the nodes in the network.
 */
public interface Partition<V> 
{
    /**
     * Executes the partitioning algorithm.
     * @param graph The graph to partitionate
     * @return true if everything went OK, false if not.
     */
    public boolean doPartition(Graph<V> graph);
    
    /**
     * Gets the training graph.
     * @return If training graph is generated, returns it. If not, this method 
     * returns null.
     */
    public Graph<V> getTrainGraph();
    
    /**
     * Gets the test graph.
     * @return If test graph is generated, returns it. If not, this method returns
     * null.
     */
    public Graph<V> getTestGraph();
    
    /**
     * Restrict both training and test graphs to a given set of users.
     * @param users the set of users.
     * @param clean indicates if the test has to be cleaned
     * @param removeReciprocalsTest remove reciprocal nodes from test.
     * @return the split, restricted to the corresponding set of users.
     */
    public Pair<Graph<V>> restrictToUsers(Predicate<V> users, boolean clean, boolean removeReciprocalsTest);
    
    /**
     * Removes from the test set every node with no edges in train.
     * It also removes those edges already in the training set.
     * @return The cleaned test set if ok, null if not.
     */
    public Graph<V> cleanTest();
    
    /**
     * Removes edges from test whose reciprocal is in train.
     * @return The cleaned test set if ok, null if not.
     */
    public Graph<V> removeTestReciprocal();
    
    /**
     * Cleans and removes reciprocal edges in test.
     * @return The cleaned test set, null if not.
     */
    public Graph<V> cleanAndRemoveTestReciprocal();
}
