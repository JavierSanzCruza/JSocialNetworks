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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import static java.util.stream.Collectors.toList;
import org.ranksys.formats.parsing.Parsers;

/**
 * Counts the different types of edge pairs in a couple of graphs (appears first / appears in second, appears in first / does not appear in second, ...)
 * @author Javier Sanz-Cruzado Puig
 */
public class EdgeCounter 
{
    
    public static void main(String[] args)
    {
        if(args.length < 2)
        {
            System.err.println("Usage: <trainGraph> <testGraph>");
        }
        
        TextGraphReader<Long> greader = new TextGraphReader<>(false, true, true, false, "\t", Parsers.lp);
               
        Graph<Long> train = greader.read(args[0], true, false);
        Graph<Long> test = greader.read(args[1], true, false);
        
        // The set of users
        Set<Long> users = new HashSet<>(train.getAllNodes().collect(toList()));
        users.addAll(test.getAllNodes().collect(toList()));
        
        int numUsers = users.size();
        
        System.out.println("Total number of pairs : " + numUsers*(numUsers-1));
        
        AtomicInteger pospos = new AtomicInteger();
        AtomicInteger posneg = new AtomicInteger();
        AtomicInteger negpos = new AtomicInteger();
        AtomicInteger negneg = new AtomicInteger();
        
        pospos.set(0);
        posneg.set(0);
        negpos.set(0);
        negneg.set(0);
        
        users.forEach(u -> {
            users.forEach(v -> {
                if(!u.equals(v))
                {
                    boolean trainedge = train.containsEdge(u, v);
                    boolean testedge = test.containsEdge(u, v);
                    if(trainedge && testedge)
                    {
                        pospos.incrementAndGet();
                    }
                    else if(trainedge)
                    {
                        posneg.incrementAndGet();
                    }
                    else if(testedge)
                    {
                        negpos.incrementAndGet();
                    }
                    else
                    {
                        negneg.incrementAndGet();
                    }
                }
            });
        });
        
        
        System.out.println("Pos/Pos: " + pospos.get() + " Pos/Neg: " + posneg.get() + " Neg/Pos: " + negpos.get() + " Neg/Neg: " + negneg.get());
    }
    
}
