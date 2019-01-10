/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.graphgen;

import es.uam.eps.ir.socialnetwork.adapters.Adapters;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;

import es.uam.eps.ir.socialnetwork.graph.io.TextGraphWriter;
import org.ranksys.formats.parsing.Parsers;

/**
 * Writes the complementary graph (without autoloops)
 * @author Javier Sanz-Cruzado Puig
 */
public class ComplementaryGraphGenerator 
{
    /**
     * Generates the complementary graph (removing autoloops) of a graph.
     * @param args Execution arguments:
     * <ul>
     *  <li><b>Original Graph:</b> Original graph route</li>
     *  <li><b>Complementary Graph:</b> Complementary graph output route</li>
     * </ul>
     * @throws Exception If something fails
     */
    public static void main(String[] args) throws Exception
    {
        if(args.length < 2)
        {
            System.err.println("Usage: <originalGraph> <complemenetary>");
            return;
        }

        // Read the original graph.
        TextGraphReader<Long> greader = new TextGraphReader<>(false, true, true, false, "\t", Parsers.lp);
        Graph<Long> original = greader.read(args[0], true, false);

        
        
        // Complement the graph.
        Graph<Long> complementary = original.complement();
        complementary = Adapters.removeAutoloops(complementary);
        
        TextGraphWriter gwriter = new TextGraphWriter("\t");
        if(gwriter.write(complementary, args[1], true, false) == false)
        {
            System.err.println("ERROR: Failure while writing in " + args[1]);
        }
    }
}
