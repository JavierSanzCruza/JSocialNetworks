/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
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
import java.util.LinkedList;

/**
 * Class for cloning trees.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class TreeCloneGenerator<U> implements GraphGenerator<U>
{
    /**
     * Tree to clone
     */
    private Tree<U> tree;
    /**
     * Indicates if the tree has been configured
     */
    private boolean configured = false;
    
    /**
     * Configure the tree generator
     * @param tree the tree to clone
     */
    public void configure(Tree<U> tree)
    {
        this.tree = tree;
        this.configured = true;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void configure(Object... configuration)
    {
        if(!(configuration == null) && configuration.length == 1)
        {
            Tree<U> auxGraph = (Tree<U>) configuration[0];
            this.configure(auxGraph);
        }
    }

    @Override
    public Graph<U> generate() throws GeneratorNotConfiguredException, GeneratorBadConfiguredException
    {
        if(this.configured == false)
        {
            throw new GeneratorNotConfiguredException("Graph cloner: Generator was not configured");
        }
        else if(this.tree == null)
        {
            throw new GeneratorNotConfiguredException("Graph cloner: Generator was badly configured");
        }
        
        GraphGenerator<U> emptyTreeGen = new EmptyTreeGenerator<>();
        emptyTreeGen.configure(tree.isWeighted());
        Tree<U> newTree = (Tree<U>) emptyTreeGen.generate();
        
        LinkedList<U> currentLevelUsers = new LinkedList<>();
        LinkedList<U> nextLevelUsers = new LinkedList<>();
        currentLevelUsers.add(tree.getRoot());
        
        while(!currentLevelUsers.isEmpty())
        {
            U current = currentLevelUsers.pop();
            if(tree.isRoot(current))
            {
                newTree.addRoot(current);
            }
            else
            {
                newTree.addChild(tree.getParent(current), current, tree.getParentWeight(current));
            }
            
            tree.getChildren(current).forEach(child -> {
                nextLevelUsers.add(child);
            });
            
            if(currentLevelUsers.isEmpty())
            {
                currentLevelUsers.clear();
                currentLevelUsers.addAll(nextLevelUsers);
                nextLevelUsers.clear();
            }
        }
        
        return newTree;       
    }
    
}
