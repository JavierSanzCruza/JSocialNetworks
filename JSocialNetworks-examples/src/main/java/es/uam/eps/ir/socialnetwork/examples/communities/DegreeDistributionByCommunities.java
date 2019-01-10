/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.communities;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.community.io.CommunitiesReader;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.metrics.vertex.Degree;
import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.doubles.Double2IntOpenHashMap;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.ranksys.formats.parsing.Parsers;

/**
 * Obtains the degree distribution for each community.
 * @author Javier Sanz-Cruzado Puig
 */
public class DegreeDistributionByCommunities 
{
    /**
     * Finds the degree distribution by communities for a given graph.
     * @param args Execution arguments <ul>
     * <li><b>Graph route:</b> The route of the graph</li>
     * <li><b>Comm route:</b> Route of the communities file</li>
     * <li><b>Directed:</b> Indicates if the graph is directed</li>
     * <li><b>Weighted:</b> Indicates if the graph is weighted</li>
     * <li><b>Direction:</b> The degree type to measure</li>
     * <li><b>Output:</b> The output file</li>
     * </ul>
     */
    public static void main(String[] args)
    {
        if(args.length < 5)
        {
            System.err.println("Usage: <graph> <communities> <directed> <weighted> <direction> <output>");
            return;
        }
        
        String graphRoute = args[0];
        String commRoute = args[1];
        boolean directed = args[2].equalsIgnoreCase("true");
        boolean weighted = args[3].equalsIgnoreCase("true");
        EdgeOrientation orientation = EdgeOrientation.valueOf(args[4]);
        String output = args[5];
        
        TextGraphReader<Long> greader = new TextGraphReader<>(false, directed, weighted, false, "\t", Parsers.lp);
        Graph<Long> graph = greader.read(graphRoute, weighted, false);
        
        CommunitiesReader<Long> creader = new CommunitiesReader<>();
        Communities<Long> comm = creader.read(commRoute, "\t", Parsers.lp);
        
        Degree<Long> degree = new Degree<>(orientation);
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output))))
        {
            for(int i = 0; i < comm.getNumCommunities(); ++i)
            {
                Double2IntMap degrees = new Double2IntOpenHashMap();
                degrees.defaultReturnValue(0);
                double maxdeg = 0;
                comm.getUsers(i).forEach(us -> 
                {
                    double deg = degree.compute(graph, us);
                    
                    if(!degrees.containsKey(deg))
                    {
                        
                        degrees.put(deg, 0);
                    }
                    degrees.put(deg, degrees.get(deg) + 1);
                });
                
                for(double d : degrees.keySet())
                {
                    if(d > maxdeg)
                        maxdeg = d;
                }
                
                for(double d = 0.0; d <= maxdeg; ++d)
                {
                    bw.write(i + "\t" + d + "\t" + degrees.get(d) + "\n");
                }
                
            }
        }
        catch(IOException ioe)
        {
            
        }
        
    }
}
