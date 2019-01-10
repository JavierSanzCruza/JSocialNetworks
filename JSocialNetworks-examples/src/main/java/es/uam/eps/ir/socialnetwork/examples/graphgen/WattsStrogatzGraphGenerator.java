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
import es.uam.eps.ir.socialnetwork.graph.generator.GraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.random.WattsStrogatzGenerator;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphWriter;
import es.uam.eps.ir.socialnetwork.utils.generator.Generators;

/**
 * Example of the generation of Watts-Strogatz graphs.
 * @author Javier Sanz-Cruzado Puig
 */
public class WattsStrogatzGraphGenerator 
{
    /**
     * Generates Erdös-Rènyi random graphs (with probability of rewiring from 0.0 to 1.0 (step equal to 0.1).
     * @param args Execution arguments. All arguments are optional, and they must be
     * indicated by their identifier (on bold).
     * <ul>
     * <li><b>-outputRoute :</b> Output route for the graph. Default value: "barabasi.txt"</li>
     * <li><b>-directed :</b> Indicates if the graph is directed or not. Default value: true</li>
     * <li><b>-numNodes :</b> Number of nodes in the graph. Default value: 10000</li>
     * <li><b>-k :</b> Initial number of links on each direction in the original ring. Default value: 15</li>
     * </ul>
     * @throws Exception If something fails when generating the graph.
     */
    public static void main(String[] args) throws Exception
    {
        int numNodes = 10000;
        int k = 15;
        boolean directed = true;
        String outputRoute = "";
        
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
                    case "-numNodes":
                        numNodes = new Integer(args[++i]);
                        break;
                    case "-k":
                        k = new Integer(args[++i]);
                        break;
                }
            }
        }
        else
        {
            System.err.println("Invalid arguments. Usage: -outputRoute route -directed true/false -numNodes n -k k)");
            return;
        }
        
        // Graph generator
        double[] betas = {0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
        Object[] genargs = {directed, numNodes, k, 0.0, Generators.longgen};        
        GraphGenerator<Long> gg = new WattsStrogatzGenerator<>();
        
        for(double b : betas)
        {
            // generate
            genargs[3] = b;
            gg.configure(genargs);
            Graph<Long> graph = gg.generate();
            
            // write
            TextGraphWriter gwriter = new TextGraphWriter("\t");
            if(gwriter.write(graph, outputRoute + "/ws-" + b + ".csv", true, false) == false)
            {
                System.err.println("ERROR: Failure while writing in " + outputRoute);
            }
            
            System.out.println("Generated graph " + b);
        }
        
    }
}
