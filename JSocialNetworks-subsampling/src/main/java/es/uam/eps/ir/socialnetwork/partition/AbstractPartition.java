/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.partition;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeType;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyMultiGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.GraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.multigraph.MultiGraph;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.util.List;
import java.util.function.Predicate;

/**
 * Abstract partition.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public abstract class AbstractPartition<U> implements Partition<U> 
{
    /**
     * The training graph.
     */
    protected Graph<U> train = null;
    /**
     * The test graph.
     */
    protected Graph<U> test = null;
    
    @Override
    public Graph<U> getTrainGraph() 
    {
        return train;
    }

    @Override
    public Graph<U> getTestGraph() 
    {
        return test;
    }

    
    @Override
    public Graph<U> cleanTest() 
    {
        if(this.train == null || this.test == null)
        {
            return null;
        }

        boolean multigraph = this.test.isMultigraph();

        if(multigraph)
        {
            return cleanMultiGraph((MultiGraph<U>) this.test, false);
        }
        else
        {
            return cleanSimpleGraph(this.test, false);
        }
    }
    
    
    @Override
    public Graph<U> removeTestReciprocal()
    {
        try 
        {
            if(this.train == null || this.test == null)
            {
                return null;
            }
            
            boolean directed = this.test.isDirected();
            boolean weighted = this.test.isWeighted();
            
            if(directed == false) // There are no reciprocal links in undirected networks
                return this.test;
            
            EmptyGraphGenerator<U> gg = new EmptyGraphGenerator<>();
            gg.configure(directed,weighted);
            Graph<U> cleaned = gg.generate();
            
            test.getAllNodes().forEach(node ->
            {
                cleaned.addNode(node);
            });
            
            test.getAllNodes().forEach(u -> 
            {
                test.getAdjacentNodes(u).forEach(v -> 
                {
                    if(!train.containsVertex(u) || !train.containsVertex(v) || !train.containsEdge(v, u))
                    {
                        cleaned.addEdge(u, v, test.getEdgeWeight(u, v), test.getEdgeType(u, v));
                    }
                });
            });
            
            return cleaned;
            
        } 
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) 
        {
            return null;
        }
    }
    @Override
    public Graph<U> cleanAndRemoveTestReciprocal() 
    {
        if(this.train == null || this.test == null)
        {
            return null;
        }

        boolean multigraph = this.train.isMultigraph();

        if(multigraph)
        {
            return this.cleanMultiGraph((MultiGraph<U>) this.test, true);
        }
        else
        {
            return this.cleanSimpleGraph(this.test, true);
        }
    }

    /**
     * Cleans a multigraph, so that only users which appear in the training set appear
     * in the test set, and edges which appear in both sets only appear in the training
     * one.
     * @param multigraph the multigraph
     * @param removeTestReciprocal true if the reciprocal edges in test have to be removed.
     * @return a cleaned multigraph.
     */
    private Graph<U> cleanMultiGraph(MultiGraph<U> multigraph, boolean removeTestReciprocal) 
    {
        try 
        {
            boolean directed = this.test.isDirected();
            boolean weighted = this.test.isWeighted();

            // Generate an empty multigraph
            GraphGenerator<U> gg = new EmptyMultiGraphGenerator<>();
            gg.configure(directed,weighted);
            Graph<U> cleaned = gg.generate();
            
            this.train.getAllNodes().forEach(node -> 
            {
                cleaned.addNode(node);
            });
            
            // Include the valid edges
            multigraph.getAllNodes().forEach(u ->
            {
                if(cleaned.containsVertex(u))
                {
                    multigraph.getAdjacentNodes(u).forEach(v ->
                    {
                        // If the vertex is in the graph, the training graph does not contain the edge and the reciprocal condition is fulfilled
                        if(cleaned.containsVertex(v) && !this.train.containsEdge(u, v) && (!removeTestReciprocal || !this.train.containsEdge(v,u)))
                        {
                            List<Double> weights = multigraph.getEdgeWeights(u, v);
                            List<Integer> types = multigraph.getEdgeTypes(u, v);
                            
                            for(int i = 0; i < weights.size(); ++i)
                            {
                                cleaned.addEdge(u, v, weights.get(i), types.get(i));
                            }
                        }
                    });
                }
            });
            
            return cleaned;
        } 
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) 
        {
            return null;
        }
    }

    /**
     * Cleans a simple graph, so that only users which appear in the training set appear
     * in the test set, and edges which appear in both sets only appear in the training
     * one.
     * @param graph the graph
     * @param removeTestReciprocal true if the reciprocal edges in test have to be removed.
     * @return a cleaned graph.
     */
    private Graph<U> cleanSimpleGraph(Graph<U> graph, boolean removeTestReciprocal) 
    {
        try 
        {
            boolean directed = this.test.isDirected();
            boolean weighted = this.test.isWeighted();

            // Generate an empty multigraph
            GraphGenerator<U> gg = new EmptyGraphGenerator<>();
            gg.configure(directed,weighted);
            Graph<U> cleaned = gg.generate();
            
            // Include all nodes with degree greater than 0 in the training graph.
            this.train.getAllNodes().forEach(node -> 
            {
                    cleaned.addNode(node);
            });
            
            // Include the valid edges
            graph.getAllNodes().forEach(u ->
            {
                if(cleaned.containsVertex(u))
                {
                    graph.getAdjacentNodes(u).forEach(v ->
                    {
                        // If the vertex is in the graph, the training graph does not contain the edge and the reciprocal condition is fulfilled
                        if(cleaned.containsVertex(v) && !this.train.containsEdge(u, v) && (!removeTestReciprocal || !this.train.containsEdge(v,u)))
                        {
                            cleaned.addEdge(u, v, graph.getEdgeWeight(u, v), graph.getEdgeType(u, v));
                        }
                    });
                }
            });
            
            return cleaned;
        } 
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) 
        {
            return null;
        }
    }
    
    /**
     * Removes reciprocal edges from test graph (edges which already were on the training graph).
     * @param multigraph the multigraph.
     * @return the graph without the reciprocal links to links in training.
     */
    private Graph<U> removeReciprocalMultiGraph(MultiGraph<U> multigraph)
    {
       try 
        {
            boolean directed = true;
            boolean weighted = this.test.isWeighted();

            // Generate an empty multigraph
            GraphGenerator<U> gg = new EmptyMultiGraphGenerator<>();
            gg.configure(directed,weighted);
            Graph<U> cleaned = gg.generate();
            
            // Include all nodes with degree greater than 0 in the training graph.
            this.test.getAllNodes().forEach(node -> 
            {
                if(this.test.degree(node) > 0)
                {
                    cleaned.addNode(node);
                }
            });
            
            // Include the valid edges
            multigraph.getAllNodes().forEach(u ->
            {
                multigraph.getAdjacentNodes(u).forEach(v ->
                {
                    // If the vertex is in the graph, the training graph does not contain the edge and the reciprocal condition is fulfilled
                    if(!this.train.containsEdge(v,u))
                    {
                        List<Double> weights = multigraph.getEdgeWeights(u, v);
                        List<Integer> types = multigraph.getEdgeTypes(u, v);

                        for(int i = 0; i < weights.size(); ++i)
                        {
                            cleaned.addEdge(u, v, weights.get(i), types.get(i));
                        }
                    }
                });
            });
            
            return cleaned;
        } 
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) 
        {
            return null;
        }        
    }
    
    /**
     * Cleans a simple graph, so that only users which appear in the training set appear
     * in the test set, and edges which appear in both sets only appear in the training
     * one.
     * @param graph the graph
     * @param removeTestReciprocal true if the reciprocal edges in test have to be removed.
     * @return a cleaned graph.
     */
    private Graph<U> removeReciprocalSimpleGraph(Graph<U> graph, boolean removeTestReciprocal) 
    {
        try 
        {
            boolean directed = this.test.isDirected();
            boolean weighted = this.test.isWeighted();

            // Generate an empty multigraph
            GraphGenerator<U> gg = new EmptyGraphGenerator<>();
            gg.configure(directed,weighted);
            Graph<U> cleaned = gg.generate();
            
            // Include all nodes with degree greater than 0 in the training graph.
            this.train.getAllNodes().forEach(node -> 
            {
                if(this.train.degree(node) > 0)
                {
                    cleaned.addNode(node);
                }
            });
            
            // Include the valid edges
            graph.getAllNodes().forEach(u ->
            {
                if(cleaned.containsVertex(u))
                {
                    graph.getAdjacentNodes(u).forEach(v ->
                    {
                        // If the vertex is in the graph, the training graph does not contain the edge and the reciprocal condition is fulfilled
                        if(cleaned.containsVertex(v) && !this.train.containsEdge(u, v) && (!removeTestReciprocal || !this.train.containsEdge(v,u)))
                        {
                            cleaned.addEdge(u, v, graph.getEdgeWeight(u, v), graph.getEdgeType(u, v));
                        }
                    });
                }
            });
            
            return cleaned;
        } 
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) 
        {
            return null;
        }
    }
    
    @Override
    public Pair<Graph<U>> restrictToUsers(Predicate<U> usersSelection, boolean clean, boolean removeReciprocalsTest)
    {
        // Generate an empty multigraph
        GraphGenerator<U> gg = new EmptyGraphGenerator<>();
        gg.configure(this.train.isDirected(),this.train.isWeighted());
        
        try
        {
            Graph<U> trainCleaned = gg.generate();
            Graph<U> testCleaned = gg.generate();
            
            Graph<U> testGraph;
            if(clean && this.test.isMultigraph())
            {
                testGraph = this.cleanMultiGraph((MultiGraph<U>) test, removeReciprocalsTest);
            }
            else if(clean)
            {
                testGraph = this.cleanSimpleGraph(test, removeReciprocalsTest);
            }
            else
            {
                testGraph = test;
            }
            
            this.train.getAllNodes().forEach(u -> 
            {
                if(usersSelection.test(u))
                {
                    trainCleaned.addNode(u);
                    testCleaned.addNode(u);
                }
            });
            
            trainCleaned.getAllNodes().forEach(u -> 
            {
                this.train.getAdjacentNodesWeights(u).forEach(v -> 
                {
                    trainCleaned.addEdge(u, v.getIdx(), v.getValue(), EdgeType.getDefaultValue(), false);
                });
                
                testGraph.getAdjacentNodesWeights(u).forEach(v -> 
                {
                    testCleaned.addEdge(u, v.getIdx(), v.getValue(), EdgeType.getDefaultValue(), false);
                });
            });
            
            return new Pair<>(trainCleaned, testCleaned);
        }
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex)
        {
            return null;
        }
    }
    
}
