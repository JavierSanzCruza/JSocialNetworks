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
import es.uam.eps.ir.socialnetwork.graph.generator.random.BarabasiGenerator;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphWriter;
import es.uam.eps.ir.socialnetwork.utils.generator.Generators;

/**
 * Example of the generation of Barabási-Albert graphs.
 * @author Javier Sanz-Cruzado Puig
 */
public class BarabasiGraphGenerator 
{
    /**
     * Generates a Barabási-Albert random graph.
     * @param args Execution arguments. All arguments are optional, and they must be
     * indicated by their identifier (on bold).
     * <ul>
     * <li><b>-outputRoute :</b> Output route for the graph. Default value: "barabasi.txt"</li>
     * <li><b>-directed :</b> Indicates if the graph is directed or not. Default value: true</li>
     * <li><b>-initialNodes :</b> Number of initial nodes in the graph. Default value: 100</li>
     * <li><b>-numIter :</b> Number of iterations. Default value: 9900</li>
     * <li><b>-numEdgesIter :</b> Number of edges created each iteration. Default value: 100</li>
     * </ul>
     * @throws Exception If something fails when generating the graph.
     */
    public static void main(String[] args) throws Exception
    {
        int initialNodes = 100;
        int numIter = 9900;
        int numEdgesIter = 100;
        boolean directed = true;
        String outputRoute = "barabasi.txt";
        
        // Parameter reading
        if(args.length > 0)
        {
            for(int i = 0; i < args.length; ++i)
            {
                switch (args[i]) {
                    case "-outputRoute":
                        outputRoute = args[++i];
                        break;
                    case "-directed":
                        directed = args[++i].equalsIgnoreCase("true");
                        break;
                    case "-initialNodes":
                        initialNodes = new Integer(args[++i]);
                        break;
                    case "-numIter":
                        numIter = new Integer(args[++i]);
                        break;
                    case "-numEdgesIter":
                        numEdgesIter = new Integer(args[++i]);
                        break;
                    default:
                        System.err.println("Unknown command " + args[i]);
                        System.err.println("Usage: (-outputRoute route -directed true/false -initialNodes n -numIter i -numEdgesIter edges)");
                }
            }
        }
                
        // Graph generation
        BarabasiGenerator<Long> gg = new BarabasiGenerator<>();
        gg.configure(directed, initialNodes, numIter, numEdgesIter, Generators.longgen);
        Graph<Long> graph = gg.generate();
        
        // Write the graph
        TextGraphWriter gwriter = new TextGraphWriter("\t");
        if(gwriter.write(graph, outputRoute, true, false) == false)
        {
            System.err.println("ERROR: Failure while writing in " + outputRoute);
        }
    }
}
