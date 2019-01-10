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
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.GraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.io.GraphReader;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.grid.metrics.edge.EdgeMetricSelector;
import es.uam.eps.ir.socialnetwork.grid.metrics.comm.global.GlobalCommunityMetricSelector;
import es.uam.eps.ir.socialnetwork.grid.metrics.graph.GraphMetricSelector;
import es.uam.eps.ir.socialnetwork.grid.metrics.comm.indiv.IndividualCommunityMetricSelector;
import es.uam.eps.ir.socialnetwork.grid.metrics.MetricGridReader;
import es.uam.eps.ir.socialnetwork.grid.metrics.MetricTypeIdentifiers;
import es.uam.eps.ir.socialnetwork.grid.metrics.pair.PairMetricSelector;
import es.uam.eps.ir.socialnetwork.grid.metrics.vertex.VertexMetricSelector;
import es.uam.eps.ir.socialnetwork.metrics.CommunityMetric;
import es.uam.eps.ir.socialnetwork.metrics.EdgeMetric;
import es.uam.eps.ir.socialnetwork.metrics.GraphMetric;
import es.uam.eps.ir.socialnetwork.metrics.IndividualCommunityMetric;
import es.uam.eps.ir.socialnetwork.metrics.PairMetric;
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
import org.ranksys.core.util.tuples.Tuple2od;
import org.ranksys.formats.parsing.Parsers;
import org.ranksys.formats.rec.RecommendationFormat;
import org.ranksys.formats.rec.TRECRecommendationFormat;

/**
 * Class which analizes the different properties of a graph.
 * @author Javier Sanz-Cruzado Puig
 */
public class RecommendationStructuralDiversityResultsEvaluation 
{
    /**
     * Program which analyzes the different properties of a graph. The graph is formed only by the recommendation links.
     * @param args Execution arguments
     * <ul>
     *  <li><b>Training graph file:</b> File containing the training graph to analyze</li>
     *  <li><b>Test graph file:</b> File containing the test graph</li>
     *  <li><b>Metric grid:</b> Grid file containing all the metrics we want to compute</li>
     *  <li><b>Recommenders route:</b> Directory which contains the recommendations to evaluate</li>
     *  <li><b>Directed:</b> true if the graph is directed, false if not</li>
     *  <li><b>Weighted:</b> true if the graph is directed, false if not</li>
     *  <li><b>Comm. Route</b> The path which contains the community files</li>
     *  <li><b>Comm. files:</b> A comma separated list of community files for the graph</li>
     *  <li><b>Output folder:</b> Folder for storing the different outcomes</li>
     *  <li><b>Max. Length:</b> Maximum length of the recommendations (each element below that position will be discarded)</li>
     *  <li><b>Only relevant:</b> true if we just want to consider relevant edges, false if we want to consider all possible edges</li>
     * </ul>
     */
    public static void main(String[] args)
    {
        if(args.length < 11)
        {
            System.err.println("Usage: <graph file> <test graph file> <metric grid> <recroute> <directed> <weighted> <commroute> <commfiles> <output folder> <length> <onlyrel>");
            return;
        }
        
        // Argument reading
        String graphFile = args[0];
        String testGraphFile = args[1];
        String metricGrid = args[2];
        String recRoute = args[3];
        boolean multigraph = false;
        boolean directed = args[4].equalsIgnoreCase("true");
        boolean weighted = args[5].equalsIgnoreCase("true");
        String commpath = args[6];
        String[] comms = args[7].split(",");
        List<String> commFiles = Arrays.asList(comms);
        String outputFile = args[8];
        int length = Parsers.ip.parse(args[9]);     
        boolean onlyrel = args[10].equals("true");
        
        // Read the communities
        Long a = System.currentTimeMillis();
        Map<String, Communities<Long>> communities = new HashMap<>();
        CommunitiesReader<Long> creader = new CommunitiesReader<>();
        
        commFiles.stream().forEach((comm) -> communities.put(comm, creader.read(commpath + comm, "\t", Parsers.lp)));
        Long b = System.currentTimeMillis();
        System.out.println("Communities read (" + (b-a) + " ms.");
        // Read the grid
        a = System.currentTimeMillis();
        MetricGridReader gridReader = new MetricGridReader(metricGrid);
        gridReader.readDocument();
        b = System.currentTimeMillis();
        System.out.println("Grid read (" + (b-a) + " ms.)");
        
        GraphReader<Long> greader = new TextGraphReader<>(multigraph, directed, weighted, false, "\t", Parsers.lp);
        Graph<Long> testGraph = greader.read(testGraphFile);

        // Common distance calculator
        DistanceCalculator<Long> dc = new DistanceCalculator<>();

        // Vertex metrics
        String type = MetricTypeIdentifiers.VERTEX_METRIC;
        System.out.println("Starting vertex metrics...");
        Map<String, Supplier<VertexMetric<Long>>> vertexMetrics = new HashMap<>();
        Set<String> metricsSet = gridReader.getMetrics(type);
        VertexMetricSelector<Long> vertexSelector = new VertexMetricSelector<>();
        // Get the different metrics
        metricsSet.stream().forEach(metric -> vertexSelector.getMetrics(metric, gridReader.getGrid(metric, MetricTypeIdentifiers.VERTEX_METRIC), dc));
        System.out.println("Identified " + vertexMetrics.size() + " metrics");
                
        // Edge metrics
        type = MetricTypeIdentifiers.EDGE_METRIC;
        System.out.println("Starting edge metrics...");
        Map<String, Supplier<EdgeMetric<Long>>> edgeMetrics = new HashMap<>();
        metricsSet = gridReader.getMetrics(type);
        EdgeMetricSelector<Long> edgeSelector = new EdgeMetricSelector<>();
        // Get the different metrics
        metricsSet.stream().forEach(metric -> edgeMetrics.putAll(edgeSelector.getMetrics(metric, gridReader.getGrid(metric, MetricTypeIdentifiers.EDGE_METRIC), dc)));
        System.out.println("Identified " + edgeMetrics.size() + " metrics");
        
        // Pair metrics
        type = MetricTypeIdentifiers.PAIR_METRIC;
        System.out.println("Starting pair metrics...");
        Map<String, Supplier<PairMetric<Long>>> pairMetrics = new HashMap<>();
        metricsSet = gridReader.getMetrics(type);
        PairMetricSelector<Long> pairSelector = new PairMetricSelector<>();
        // Get the different metrics
        metricsSet.stream().forEach(metric -> pairMetrics.putAll(pairSelector.getMetrics(metric, gridReader.getGrid(metric, MetricTypeIdentifiers.PAIR_METRIC), dc)));
        System.out.println("Identified " + pairMetrics.size() + " metrics");
               
        // Individual community metrics
        type = MetricTypeIdentifiers.INDIV_COMM_METRIC;
        System.out.println("Starting individual community metrics...");
        Map<String, Supplier<IndividualCommunityMetric<Long>>> indivcommMetrics = new HashMap<>();
        metricsSet = gridReader.getMetrics(type);
        IndividualCommunityMetricSelector<Long> indivSel = new IndividualCommunityMetricSelector<>();
        // Get the different metrics
        metricsSet.stream().forEach(metric -> indivcommMetrics.putAll(indivSel.getMetrics(metric, gridReader.getGrid(metric, MetricTypeIdentifiers.INDIV_COMM_METRIC))));
        System.out.println("Identified " + indivcommMetrics.size() + " metrics");
        
        // Global community metrics
        type = MetricTypeIdentifiers.GLOBAL_COMM_METRIC;
        System.out.println("Starting global community metrics...");
        Map<String, Supplier<CommunityMetric<Long>>> globalCommMetrics = new HashMap<>();
        metricsSet = gridReader.getMetrics(type);
        GlobalCommunityMetricSelector<Long> globalSel = new GlobalCommunityMetricSelector<>();
        metricsSet.forEach(metric ->  globalCommMetrics.putAll(globalSel.getMetrics(metric, gridReader.getGrid(metric, MetricTypeIdentifiers.GLOBAL_COMM_METRIC))));
        System.out.println("Identified " + globalCommMetrics.size() + " metrics");
       
        // Global graph metrics
        type = MetricTypeIdentifiers.GRAPH_METRIC;
        System.out.println("Starting graph metrics...");
        Map<String, Supplier<GraphMetric<Long>>> graphMetrics = new HashMap<>();
        metricsSet = gridReader.getMetrics(type);
        GraphMetricSelector<Long> graphSel = new GraphMetricSelector<>();
        metricsSet.stream().forEach(metric -> graphMetrics.putAll(graphSel.getMetrics(metric, gridReader.getGrid(metric,MetricTypeIdentifiers.GRAPH_METRIC),dc)));
        System.out.println("Identified " + graphMetrics.size() + " metrics");

        
        File recFolder = new File(recRoute);
        if(!recFolder.exists() || !recFolder.isDirectory())
        {
            System.err.println("Nothing to evaluate!");
            return;
        }
        String[] recommenders = recFolder.list();
        
        // Configure the graph cloner
        GraphGenerator<Long> generator = new EmptyGraphGenerator<>();
        RecommendationFormat<Long, Long> format = new TRECRecommendationFormat<>(Parsers.lp, Parsers.lp);
        generator.configure(directed, weighted);

        // Map for storing the different values for the different recommenders
        Map<String, Map<String,Double>> allMetricValues = new HashMap<>();
        List<String> metricList = new ArrayList<>();
        boolean stored = false;
        
        System.out.println("\n");
        // Compute and write the values of the metrics
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile))))
        {
            
            // For each individual metric
            for(String recFile : recommenders)
            {
                System.out.println("\nStarting " + recFile);
                a = System.currentTimeMillis();
                // Map for storing average values and graph metrics
                Map<String, Double> metricvalues = new HashMap<>();
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
                        // If we do not consider relevant edges or the edge is relevant
                        if(!onlyrel || testGraph.containsEdge(u, v))
                        {
                            aux.addEdge(u, v);
                            extraEdges.add(new Pair<>(u,v));
                        }
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
                    // Compute the average values for the metric
                    double average = vm.averageValue(aux);
                    metricvalues.put("Average Vertex " + metric, average);
                    long b2 = System.currentTimeMillis();
                    // Write the metric to a file
                    System.out.println("Metric " + metric + " done (" + (b2-a2) + " ms.)");            
                });
                b = System.currentTimeMillis();
                System.out.println("Vertex metrics done (" + (b-a) + " ms.)");

                // Compute each edge metric.
                edgeMetrics.entrySet().stream().forEach(entry -> 
                {
                    String metric = entry.getKey();
                    System.out.println("Running " + metric);
                    Long a2 = System.currentTimeMillis();
                    EdgeMetric<Long> em = entry.getValue().get();
                    // Compute the average values
                    double average = em.averageValue(aux, extraEdges.stream(), extraEdges.size());
                    metricvalues.put("Average Edge " + metric, average);
                    long b2 = System.currentTimeMillis();
                    System.out.println("Metric " + metric + " done (" + (b2-a2) + " ms.)");            
                });
                b = System.currentTimeMillis();
                System.out.println("Edge metrics done (" + (b-a) + " ms.)");

                // Compute each pair metric.
                pairMetrics.entrySet().stream().forEach(entry -> 
                {
                    String metric = entry.getKey();
                    System.out.println("Running " + metric);
                    Long a2 = System.currentTimeMillis();
                    PairMetric<Long> pm = entry.getValue().get();
                    // Compute the average values for the metric
                    double average = pm.averageValue(aux, extraEdges.stream(), extraEdges.size());
                    metricvalues.put("Average Pair " + metric, average);
                    long b2 = System.currentTimeMillis();
                    System.out.println("Metric " + metric + " done (" + (b2-a2) + " ms.)");            
                });
                b = System.currentTimeMillis();
                System.out.println("Pair metrics done (" + (b-a) + " ms.)");   

                // Compute each individual community metric.
                indivcommMetrics.entrySet().stream().forEach(entry -> 
                {
                    String metric = entry.getKey();
                    System.out.println("Running " + metric);
                    Long a2 = System.currentTimeMillis();
                    IndividualCommunityMetric<Long> icm = entry.getValue().get();

                    // Compute the metric values for each community detection algorithm
                    communities.entrySet().stream().forEach(comm -> 
                    {
                        double average = icm.averageValue(aux, comm.getValue());
                        metricvalues.put("Average " + metric + " " + comm.getKey(), average);
                        long b2 = System.currentTimeMillis();
                        System.out.println("Metric " + metric + " for community " + comm.getKey() + " done (" + (b2-a2) + " ms.)");            
                    });

                    Long b3 = System.currentTimeMillis();
                    System.out.println("Metric  " + metric + " done (" + (b3-a2) + " ms.)");  
                });
                b = System.currentTimeMillis();
                System.out.println("Individual community metrics done (" + (b-a) + " ms.)");

                // Compute each global community metric
                globalCommMetrics.entrySet().stream().forEach(entry -> 
                {
                    String metric = entry.getKey();
                    System.out.println("Running " + metric);

                    Long a2 = System.currentTimeMillis();
                    CommunityMetric<Long> gcm = entry.getValue().get();

                    // Compute the metric for each community detection algorithm
                    communities.entrySet().stream().forEach(comm -> 
                    {
                        double value = gcm.compute(aux, comm.getValue());
                        metricvalues.put(metric + "_" + comm.getKey(), value);
                        Long b2 = System.currentTimeMillis();
                        System.out.println("Metric " + metric + " for community " + comm.getKey() + " done (" + (b2-a2) + " ms.)");
                    });
                    Long b3 = System.currentTimeMillis();
                    System.out.println("Metric " + metric + " done (" + (b3-a2) + " ms.)");
                });
                b = System.currentTimeMillis();
                System.out.println("Global community metrics done (" + (b-a) + " ms.)");

                // Compute global metrics.
                graphMetrics.entrySet().stream().forEach(entry -> 
                {
                    String metric = entry.getKey();
                    System.out.println("Running " + metric);

                    Long a2 = System.currentTimeMillis();
                    GraphMetric<Long> gm = entry.getValue().get();
                    double value = gm.compute(aux);
                    metricvalues.put(metric, value);
                    Long b2 = System.currentTimeMillis();

                    System.out.println("Metric " + metric + " done (" + (b2-a2) + " ms.)");
                });
                b = System.currentTimeMillis();
                System.out.println("Graph metrics done (" + (b-a) + " ms.)");
                
                //Store the set of metrics
                if(stored == false)
                {
                    metricList.addAll(metricvalues.keySet());
                    stored = true;
                }
                
                allMetricValues.put(recFile, metricvalues);
                b = System.currentTimeMillis();
                System.out.println("Recommender " + recFile + " done (" + (b-a) + " ms.)");
            }
            
            // Write the complete results
            
            // Write the header
            bw.write("algorithm");
            for(String metric : metricList)
            {
                bw.write("\t" + metric);
            }
            bw.write("\n");
            
            // Write each metric
            for(String recFile : allMetricValues.keySet())
            {
                bw.write(recFile);
                Map<String, Double> metricvalues = allMetricValues.get(recFile);
                for(String metric : metricList)
                {
                    bw.write("\t" + metricvalues.get(metric));
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
