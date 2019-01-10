/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.recommendation.evaluation;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.community.io.CommunitiesReader;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.generator.GraphCloneGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.GraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.grid.metrics.MetricGridReader;
import es.uam.eps.ir.socialnetwork.grid.metrics.MetricTypeIdentifiers;
import es.uam.eps.ir.socialnetwork.grid.metrics.vertex.VertexMetricSelector;
import es.uam.eps.ir.socialnetwork.metrics.VertexMetric;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.ranksys.core.util.tuples.Tuple2od;
import org.ranksys.formats.parsing.Parsers;
import org.ranksys.formats.rec.RecommendationFormat;
import org.ranksys.formats.rec.TRECRecommendationFormat;

/**
 * Class which analizes the different properties of a graph.
 * @author Javier Sanz-Cruzado Puig
 */
public class IndividualRecommendationStructuralDiversityEvaluation 
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
        if(args.length < 9)
        {
            System.err.println("Usage: <graph file> <metric grid> <recroute> <directed> <weighted> <commroute> <commfiles> <output folder> <length>");
            return;
        }
        
        // Argument reading
        String graphFile = args[0];
        String metricGrid = args[1];
        String recRoute = args[2];
        boolean multigraph = false;
        boolean directed = args[3].equalsIgnoreCase("true");
        boolean weighted = args[4].equalsIgnoreCase("true");
        String commpath = args[5];
        String[] comms = args[6].split(",");
        List<String> commFiles = Arrays.asList(comms);
        String outputFile = args[7];
        int length = Parsers.ip.parse(args[8]);
       
        // Read the graph.
        Long a = System.currentTimeMillis();
        
        TextGraphReader<Long> greader = new TextGraphReader<>(multigraph, directed, weighted, true, "\t", Parsers.lp);
        Graph<Long> graph = greader.read(graphFile, weighted, false); 
        
        Long b = System.currentTimeMillis();
        System.out.println("Graph read (" + (b-a) + " ms.)");
        
        // Read the communities
        a = System.currentTimeMillis();
        Map<String, Communities<Long>> communities = new HashMap<>();
        CommunitiesReader<Long> creader = new CommunitiesReader<>();
        
        commFiles.stream().forEach((comm) -> communities.put(comm, creader.read(commpath + comm, "\t", Parsers.lp)));
        b = System.currentTimeMillis();
        System.out.println("Communities read (" + (b-a) + " ms.");
        // Read the grid
        a = System.currentTimeMillis();
        MetricGridReader gridReader = new MetricGridReader(metricGrid);
        gridReader.readDocument();
        b = System.currentTimeMillis();
        System.out.println("Grid read (" + (b-a) + " ms.)");

        // Common distance calculator
        DistanceCalculator<Long> dc = new DistanceCalculator<>();

        // Vertex metrics
        String type = MetricTypeIdentifiers.VERTEX_METRIC;
        System.out.println("Starting vertex metrics...");
        Map<String, Supplier<VertexMetric<Long>>> vertexMetrics = new HashMap<>();
        Set<String> metricsSet = gridReader.getMetrics(type);
        
        VertexMetricSelector<Long> vertexSelector = new VertexMetricSelector<>();
        metricsSet.stream().forEach(metric -> vertexMetrics.putAll(vertexSelector.getMetrics(metric, gridReader.getGrid(metric, MetricTypeIdentifiers.VERTEX_METRIC), dc)));
        System.out.println("Identified " + vertexMetrics.size() + " metrics");
                
        File recFolder = new File(recRoute);
        if(!recFolder.exists() || !recFolder.isDirectory())
        {
            System.err.println("Nothing to evaluate!");
            return;
        }
        String[] recommenders = recFolder.list();
        
        // Configure the graph cloner
        GraphGenerator<Long> generator = new GraphCloneGenerator<>();
        RecommendationFormat<Long, Long> format = new TRECRecommendationFormat<>(Parsers.lp, Parsers.lp);
        generator.configure(graph);

        List<String> metricList = new ArrayList<>();
        metricList.addAll(vertexMetrics.keySet());
        boolean stored = false;
        
        System.out.println("\n");
        // Compute and write the values of the metrics
        
            
        // For each individual metric
        for(String recFile : recommenders)
        {
            try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile + recFile))))
            {
                System.out.println("\nStarting " + recFile);
                a = System.currentTimeMillis();
                // Map for storing average values and graph metrics
                Map<String, Map<Long, Double>> metricvalues = new HashMap<>();
                // Generate the clone of the original graph
                Graph<Long> aux = generator.generate();
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
                        aux.addEdge(u, v);
                        extraEdges.add(new Pair<>(u,v));
                    }
                });

                // Compute the metric values for the recommender
                // Compute each individual metric.
                vertexMetrics.entrySet().stream().forEach(entry -> 
                {
                    String metric = entry.getKey();
                    System.out.println("Running " + metric);
                    Long a2 = System.currentTimeMillis();
                    VertexMetric<Long> vm = entry.getValue().get();
                    Map<Long, Double> map = vm.compute(aux);
                    // Compute the average values for the metric
                    metricvalues.put(metric, map);
                    long b2 = System.currentTimeMillis();
                    // Write the metric to a file
                    System.out.println("Metric " + metric + " done (" + (b2-a2) + " ms.)");            
                });
                b = System.currentTimeMillis();
                System.out.println("Vertex metrics done (" + (b-a) + " ms.)");


                System.out.println("Recommender " + recFile + " done (" + (b-a) + " ms.)");

                // Write the complete results

                // Write the header
                bw.write("user");
                for(String metric : metricList)
                {
                    bw.write("\t" + metric);
                }
                bw.write("\n");

                List<Long> users = graph.getAllNodes().collect(Collectors.toCollection(ArrayList::new));
                
                for(Long user : users)
                {
                    bw.write(user + "");
                    for(String metric : metricList)
                    {
                       bw.write("\t" + metricvalues.get(metric).get(user));
                    }
                    bw.write("\n");
                }
                
            }
            catch(IOException | GeneratorNotConfiguredException | GeneratorBadConfiguredException ioe)
            {
                System.err.println("ERROR: Something failed while computing the metrics");
            }
        }

    }
        
}
