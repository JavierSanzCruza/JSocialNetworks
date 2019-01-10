/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.other;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import org.ranksys.formats.parsing.Parsers;

/**
 * Checks the reciprocality of the test links
 * @author Javier Sanz-Cruzado Puig
 */
public class Reciprocality 
{
    public static void main(String[] args)
    {
        if(args.length < 2)
        {
            System.err.println("Usage: <trainGraph> <testGraph>");
        }
        
        TextGraphReader<Long> greader = new TextGraphReader<>(false, true, true, false, "\t", Parsers.lp);
        Graph<Long> graph = greader.read(args[0], false, false);
        
        
        Graph<Long> train = greader.read(args[0],true,false);
        Graph<Long> test = greader.read(args[1],true, false);
        
        long reciprocal = test.getAllNodes().mapToLong(u -> {
            return test.getAdjacentNodes(u).mapToLong(v -> {
                if(train.containsEdge(v, u))
                {
                    return 1L;
                }
                return 0L;
            }).sum();
        }).sum();
        
        long total = test.getEdgeCount();
        
        System.out.println("Reciprocal: " + reciprocal);
        System.out.println("Non Reciprocal: " + (total - reciprocal));
        
    }
}
