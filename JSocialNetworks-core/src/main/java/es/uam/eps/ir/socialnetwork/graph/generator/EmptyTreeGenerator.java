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
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.tree.Tree;
import es.uam.eps.ir.socialnetwork.graph.tree.fast.FastUnweightedTree;
import es.uam.eps.ir.socialnetwork.graph.tree.fast.FastWeightedTree;

/**
 * Empty tree generator.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class EmptyTreeGenerator<U> implements GraphGenerator<U>
{
    /**
     * Indicates if the graph generator has been configured.
     */
    private boolean configured = false;
    /**
     * Indicates if the tree has weights.
     */
    private boolean weighted;
   
    @SuppressWarnings("unchecked")   
    @Override
    public void configure(Object... configuration) 
    {
        if(!(configuration == null) && configuration.length == 1)
        {
            boolean auxWeighted = (boolean) configuration[0];
            
            
            this.configure(auxWeighted);
        }
        else
        {
            configured = false;
        }
        
    }
    
    /**
     * Configures the tree generator
     * @param weighted if the tree is going to be weighted.
     */
    public void configure(boolean weighted)
    {
        this.weighted = weighted;
        this.configured = true;
    }
    
    @Override
    public Graph<U> generate() throws GeneratorNotConfiguredException, GeneratorBadConfiguredException
    {
        if(configured == false)
            throw new GeneratorNotConfiguredException("Empty graph: The generator was not configured");
        
        Tree<U> graph;
        
        if(weighted)
            graph = new FastWeightedTree<>();
        else
            graph = new FastUnweightedTree<>();
        
        
        return graph; 
    }
}
