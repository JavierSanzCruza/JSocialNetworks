/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.graphgen;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.generator.CompleteGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphWriter;
import es.uam.eps.ir.socialnetwork.utils.generator.Generators;

/**
 * Example of the generation of Erdös graphs.
 * @author Javier Sanz-Cruzado Puig
 */
public class CompleteGraphGeneratorExample 
{
    /**
     * Generates a Erdös-Rènyi random graph.
     * @param args Execution arguments. All arguments are optional, and they must be
     * indicated by their identifier (on bold).
     * <ul>
     * <li><b>-outputRoute :</b> Output route for the graph. Default value: "barabasi.txt"</li>
     * <li><b>-directed :</b> Indicates if the graph is directed or not. Default value: true</li>
     * <li><b>-numNodes :</b> Number of nodes in the graph. Default value: 1000</li>
     * <li><b>-prob :</b> Probability of creating a link between two nodes. Default value: 0.1</li>
     * </ul>
     * @throws Exception If something fails when generating the graph.
     */
    public static void main(String[] args) throws Exception
    {
        int numNodes = 1000;
        boolean directed = true;
        String outputRoute = "complete.txt";
        
        // Parameter reading
        if(args.length > 0)
        {
            for(int i = 0; i < args.length; ++i)
            {
                if(args[i].equals("-outputRoute"))
                {
                    outputRoute = args[++i];
                }
                else if(args[i].equals("-directed"))
                {
                    directed = args[++i].equalsIgnoreCase("true");
                }
                else if(args[i].equals("-numNodes"))
                {
                   numNodes = new Integer(args[++i]);
                }
            }
        }
        else
        {
            System.err.println("Invalid arguments. Usage: -outputRoute route -directed true/false -numNodes n -prob p)");
            return;
        }
                
        // Generating
        CompleteGraphGenerator<Long> gg = new CompleteGraphGenerator<>();
        gg.configure(directed, numNodes, Generators.longgen);
        Graph<Long> graph = gg.generate();
        
        TextGraphWriter gwriter = new TextGraphWriter("\t");
        if(gwriter.write(graph, outputRoute, true, false) == false)
        {
            System.err.println("ERROR: Failure while writing in " + outputRoute);
        }
        // Write the graph
        
    }
}
