/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.recommendation.evaluation;


import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.metrics.distance.Distance;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ranksys.core.util.tuples.Tuple2od;
import org.ranksys.formats.parsing.Parsers;
import org.ranksys.formats.rec.RecommendationFormat;
import org.ranksys.formats.rec.TRECRecommendationFormat;

/**
 * Class which analizes the different properties of a graph.
 * @author Javier Sanz-Cruzado Puig
 */
public class RecommDistanceEvaluation 
{
    /**
     * Program which analyzes the different properties of a graph.
     * @param args Execution arguments
     * <ul>
     *  <li><b>Graph file:</b> File containing the graph to analyze</li>
     *  <li><b>Metric grid:</b> Grid file containing all the metrics we want to compute</li>
     *  <li><b>Recommenders route:</b> Directory which contains the recommendations to evaluate</li>
     *  <li><b>Directed:</b> true if the graph is directed, false if not</li>
     *  <li><b>Weighted:</b> true if the graph is directed, false if not</li>
     *  <li><b>Comm. Route</b> The path which contains the community files</li>
     *  <li><b>Comm. files:</b> A comma separated list of community files for the graph</li>
     *  <li><b>Output folder:</b> Folder for storing the different outcomes</li>
     *  <li><b>Max. Length:</b> Maximum length of the recommendations (each element below that position will be discarded)</li>
     * </ul>
     */
    public static void main(String[] args)
    {
        if(args.length < 6)
        {
            System.err.println("Usage: <graph file> <metric grid> <recroute> <directed> <weighted> <commroute> <commfiles> <output folder> <length>");
            return;
        }
        
        // Argument reading
        String graphFile = args[0];
        String recRoute = args[1];
        boolean multigraph = false;
        boolean directed = args[2].equalsIgnoreCase("true");
        boolean weighted = args[3].equalsIgnoreCase("true");
        String outputFile = args[4];
        int length = Parsers.ip.parse(args[5]);
       
        // Read the graph.
        Long a = System.currentTimeMillis();
        TextGraphReader<Long> greader = new TextGraphReader<>(multigraph, directed, weighted, false, "\t", Parsers.lp);
        Graph<Long> graph = greader.read(graphFile, weighted, false);         

        Long b = System.currentTimeMillis();
        System.out.println("Graph read (" + (b-a) + " ms.)");
        
        // Read the communities
        a = System.currentTimeMillis();
        // Common distance calculator
        DistanceCalculator<Long> dc = new DistanceCalculator<>();
        dc.computeDistances(graph);
        b = System.currentTimeMillis();
        System.out.println("Distances computed (" + (b-a) + " ms.");
        
        File recFolder = new File(recRoute);
        if(!recFolder.exists() || !recFolder.isDirectory())
        {
            System.err.println("Nothing to evaluate!");
            return;
        }
        String[] recommenders = recFolder.list();
        // Configure the graph cloner
        RecommendationFormat<Long, Long> format = new TRECRecommendationFormat<>(Parsers.lp, Parsers.lp);
        
        System.out.println("\n");
        // Compute and write the values of the metrics
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile))))
        {
            
            // For each individual metric
            for(String recFile : recommenders)
            {
                System.out.println("\nStarting " + recFile);
                // Generate the clone of the original graph
                List<Pair<Long>> extraEdges = new ArrayList<>();

                // Read the recommendation
                format.getReader(recRoute + recFile).readAll().forEach(rec -> 
                {
                    long u = rec.getUser();
                    List<Tuple2od<Long>> items = rec.getItems();
                    long maxLength = Math.min(items.size(), length);
                    for(int i = 0; i < maxLength; ++i)
                    {
                        long v = items.get(i).v1;
                        extraEdges.add(new Pair<>(u,v));
                    }
                });
                
                Distance<Long> distances = new Distance<>(dc);
                Map<Pair<Long>, Double> pairDist = distances.compute(graph, extraEdges.stream());
                
                double denom = pairDist.entrySet().stream().mapToDouble(entry -> 
                {
                    if(entry.getValue().isInfinite())
                        return 0.0;
                    else
                        return 1.0/(entry.getValue());
                }).sum();
                
                double distance;
                if(denom == 0)
                {
                    distance = Double.POSITIVE_INFINITY;
                }
                else
                {
                    distance = (extraEdges.size()+0.0)/denom - 2.0;
                }
                
                bw.write(recFile + "\t" + distance + "\n");
                System.out.println("Finished " + recFile);
            }
        }
        catch(IOException ioe)
        {
            System.err.println("ERROR: Something failed while computing the metrics");
        }
    }
}
