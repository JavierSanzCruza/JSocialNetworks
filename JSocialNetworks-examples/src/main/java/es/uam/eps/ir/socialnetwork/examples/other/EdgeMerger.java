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
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.GraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphWriter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.ranksys.formats.parsing.Parsers;

/**
 * Given a multigraph network in a file, condenses it to a simple graph without
 * weights.
 * @author Javier Sanz-Cruzado Puig
 */
public class EdgeMerger 
{
    public static void main(String[] args) throws Exception
    {
        if(args.length < 3)
        {
            System.err.println("ERROR: Invalid arguments");
            System.err.println("Usage: <original> <cleaned> <directed>");
            return;
        }
        
        String original = args[0];
        String cleaned = args[1];
        boolean directed = args[2].equalsIgnoreCase("true");
        
        // Generate an empty graph.
        GraphGenerator<Long> ggen = new EmptyGraphGenerator<>();
        ggen.configure(directed, false);
        Graph<Long> graph = ggen.generate();
        
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(original))))
        {
            String line;
            while((line = br.readLine()) != null)
            {
                String[] split = line.split("\t");
                long u = Parsers.lp.parse(split[0]);
                long v = Parsers.lp.parse(split[1]);
                
                if(!graph.containsVertex(u)) graph.addNode(u);
                if(!graph.containsVertex(v)) graph.addNode(v);
                if(!graph.containsEdge(u, v)) graph.addEdge(u, v);
            }
        }
        
        TextGraphWriter<Long> gwriter = new TextGraphWriter("\t");
        gwriter.write(graph, cleaned, true, false);
        
    }
}
