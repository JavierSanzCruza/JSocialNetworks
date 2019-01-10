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
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import org.openide.util.Exceptions;

/**
 * Class which partitions a graph using a certain set of edges as training, and the rest as test.
 * @author Javier Sanz-Cruzado Puig
 */
public class SubstractEdgesPartition<U> extends AbstractPartition<U> 
{
    private final Graph<U> edgesToSubstract;
    
    public SubstractEdgesPartition(Graph<U> edgesToSubstract)
    {
        this.edgesToSubstract = edgesToSubstract;
    }
    
    @Override
    public boolean doPartition(Graph<U> graph) 
    {
        try 
        {
            boolean directed = graph.isDirected();
            boolean weighted = graph.isWeighted();
            
            EmptyGraphGenerator<U> gg = new EmptyGraphGenerator<>();
            
            gg.configure(directed, weighted);
            this.train = gg.generate();
            this.test = gg.generate();
            
            edgesToSubstract.getAllNodes().forEach(node -> 
            {
                this.train.addNode(node);
                this.test.addNode(node);
            });
            
            graph.getAllNodes().forEach(u -> 
            {
                graph.getAdjacentNodesWeights(u).forEach(vW -> 
                {
                   U v = vW.getIdx();
                   if(train.containsVertex(u) && train.containsVertex(v) && !edgesToSubstract.containsEdge(u, v))
                   {
                       this.test.addEdge(u, v, vW.getValue());
                   }
                   else if(train.containsVertex(u) && train.containsVertex(v))
                   {
                       this.train.addEdge(u, v, vW.getValue());
                   }
                });
            });
            
            return true;
            
        } 
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) 
        {
            return false;
        }
    }
    
}
