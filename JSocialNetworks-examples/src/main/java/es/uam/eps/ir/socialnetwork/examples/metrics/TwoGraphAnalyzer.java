/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.metrics;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.community.io.CommunitiesReader;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.grid.metrics.comm.global.GlobalCommunityMetricSelector;
import es.uam.eps.ir.socialnetwork.grid.metrics.graph.GraphMetricSelector;
import es.uam.eps.ir.socialnetwork.grid.metrics.comm.indiv.IndividualCommunityMetricSelector;
import es.uam.eps.ir.socialnetwork.grid.metrics.MetricGridReader;
import es.uam.eps.ir.socialnetwork.grid.metrics.MetricTypeIdentifiers;
import es.uam.eps.ir.socialnetwork.grid.metrics.pair.PairMetricSelector;
import es.uam.eps.ir.socialnetwork.metrics.CommunityMetric;
import es.uam.eps.ir.socialnetwork.metrics.GraphMetric;
import es.uam.eps.ir.socialnetwork.metrics.IndividualCommunityMetric;
import es.uam.eps.ir.socialnetwork.metrics.PairMetric;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import org.ranksys.formats.parsing.Parsers;

/**
 * Class which analizes the different properties of a graph.
 * @author Javier Sanz-Cruzado Puig
 */
public class TwoGraphAnalyzer 
{
    /**
     * Program which analyzes pair metrics for the new edges of a graph.
     * @param args Execution arguments
     * <ul>
     *  <li><b>Graph file:</b> File containing the training graph to analyze</li>
     *  <li><b>Test file:</b> File containing the test graph to analyze</li>
     *  <li><b>Metric grid:</b> Grid file containing all the metrics we want to compute</li>
     *  <li><b>Multigraph:</b> true if the graph is a multigraph, false if not</li>
     *  <li><b>Directed:</b> true if the graph is directed, false if not</li>
     *  <li><b>Weighted:</b> true if the graph is directed, false if not</li>
     *  <li><b>Comm. Route</b> The path which contains the community files</li>
     *  <li><b>Comm. files:</b> A comma separated list of community files for the graph</li>
     *  <li><b>Output folder:</b> Folder for storing the different outcomes</li>
     * </ul>
     */
    public static void main(String[] args)
    {
        if(args.length < 8)
        {
            System.err.println("Usage: <graph file> <test graph file> <metric grid> <commroute> <commfiles> <multigraph> <directed> <weighted> <output folder>");
            return;
        }
        
        // Argument reading
        String graphFile = args[0];
        String testGraphFile = args[1];
        String metricGrid = args[2];
        boolean multigraph = args[3].equalsIgnoreCase("true");
        boolean directed = args[4].equalsIgnoreCase("true");
        boolean weighted = args[5].equalsIgnoreCase("true");
        String commpath = args[6];
        String[] comms = args[7].split(",");
        List<String> commFiles = Arrays.asList(comms);
        String output = args[8];
       
        // Read the graph.
        Long a = System.currentTimeMillis();
        
        TextGraphReader<Long> greader = new TextGraphReader<>(multigraph, directed, weighted, false, "\t", Parsers.lp);
        Graph<Long> graph = greader.read(graphFile, weighted, false);
        Long b = System.currentTimeMillis();
        System.out.println("Graph read (" + (b-a) + " ms.)");
        
        a = System.currentTimeMillis();
        Graph<Long> testGraph = greader.read(testGraphFile, weighted, false);
        b = System.currentTimeMillis();
        System.out.println("Test graph read (" + (b-a) + " ms.)");
        
        // Read the communities
        a = System.currentTimeMillis();
        Map<String, Communities<Long>> communities = new HashMap<>();
        CommunitiesReader<Long> creader = new CommunitiesReader<>();
        
        commFiles.stream().forEach((comm) -> communities.put(comm, creader.read(commpath + comm, "\t", Parsers.lp)));
        b = System.currentTimeMillis();
        System.out.println("Communities read (" + (b-a) + " ms.");
        
        // Find the list of pairs
        List<Pair<Long>> pairs = new ArrayList<>();
        testGraph.getAllNodes().forEach(u -> {
            if(graph.containsVertex(u))
            {
                testGraph.getAdjacentNodes(u).forEach(v -> {
                    if(graph.containsVertex(v) && !graph.containsEdge(u,v))
                    {
                        pairs.add(new Pair<>(u,v));
                    }
                });
            }
        });
        int pairsSize = pairs.size();

        // Read the grid
        a = System.currentTimeMillis();
        MetricGridReader gridReader = new MetricGridReader(metricGrid);
        gridReader.readDocument();
        b = System.currentTimeMillis();
        System.out.println("Grid read (" + (b-a) + " ms.)");

        // Common distance calculator
        DistanceCalculator<Long> dc = new DistanceCalculator<>();
        // Map for storing average values and graph metrics
        Map<String, Double> metricvalues = new HashMap<>();
        
        // Pair metrics
        String type = MetricTypeIdentifiers.PAIR_METRIC;
        System.out.println("Starting pair metrics...");
        a = System.currentTimeMillis();
        Map<String, Supplier<PairMetric<Long>>> pairMetrics = new HashMap<>();
        Set<String> metricsSet = gridReader.getMetrics(type);
        
        PairMetricSelector<Long> pairSel = new PairMetricSelector<>();
        // Get the different metrics
        metricsSet.stream().forEach(metric -> pairMetrics.putAll(pairSel.getMetrics(metric, gridReader.getGrid(metric, MetricTypeIdentifiers.PAIR_METRIC), dc)));
        System.out.println("Identified " + pairMetrics.size() + " metrics");
                        
        // Compute each individual metric.
        pairMetrics.entrySet().stream().forEach(entry -> 
        {
            String metric = entry.getKey();
            System.out.println("Running " + metric);
            Long a2 = System.currentTimeMillis();
            PairMetric<Long> pm = entry.getValue().get();
            // Compute the average values for the metric
            double average = pm.averageValue(graph, pairs.stream(), pairsSize);
            metricvalues.put(metric, average);
            Long b2 = System.currentTimeMillis();
            System.out.println("Computed average " + metric + " (" + (b2-a2) + " ms.");           
        });
        b = System.currentTimeMillis();
        System.out.println("Pair metrics done (" + (b-a) + " ms.)");
        
        // Individual community metrics
        type = MetricTypeIdentifiers.INDIV_COMM_METRIC;
        System.out.println("Starting individual community metrics...");
        a = System.currentTimeMillis();
        Map<String, Supplier<IndividualCommunityMetric<Long>>> indivcommMetrics = new HashMap<>();
        metricsSet = gridReader.getMetrics(type);
        
        IndividualCommunityMetricSelector<Long> indivSel = new IndividualCommunityMetricSelector<>();
        // Get the different metrics
        metricsSet.stream().forEach(metric -> indivcommMetrics.putAll(indivSel.getMetrics(metric, gridReader.getGrid(metric, MetricTypeIdentifiers.INDIV_COMM_METRIC))));
        System.out.println("Identified " + indivcommMetrics.size() + " metrics");
        
        // Create the individual folder for the vertex metrics.
        if(indivcommMetrics.size() > 0)
        {
            File file = new File(output + type);
            if(!file.exists())
            {
                file.mkdirs();
            }
        }
        
        // Compute each individual metric.
        indivcommMetrics.entrySet().stream().forEach(entry -> 
        {
            String metric = entry.getKey();
            System.out.println("Running " + metric);
            Long a2 = System.currentTimeMillis();
            IndividualCommunityMetric<Long> icm = entry.getValue().get();
            
            // Compute the metric values for each community detection algorithm
            communities.entrySet().stream().forEach(comm -> 
            {
                Map<Integer, Double> values = icm.compute(graph, comm.getValue());
                Long b2 = System.currentTimeMillis();
                System.out.println("Computed " + metric + " for communities " + comm.getKey() + " (" + (b2-a2) + " ms.)");
                
                double average = values.values().stream().mapToDouble(val -> val).average().getAsDouble();
                metricvalues.put("Average " + metric + " " + comm.getKey(), average);
                b2 = System.currentTimeMillis();
                System.out.println("Computed average " + metric + " for community " + comm.getKey() + " (" + (b2-a2) + " ms.)");

                TwoGraphAnalyzer.printIndividualCommMetric(output + MetricTypeIdentifiers.INDIV_COMM_METRIC + "/" + metric + "_" + comm.getKey(), values);
                b2 = System.currentTimeMillis();
                System.out.println("Metric " + metric + " for community " + comm.getKey() + " done (" + (b2-a2) + " ms.)");            
            });
            
            Long b3 = System.currentTimeMillis();
            System.out.println("Metric  " + metric + " done (" + (b3-a2) + " ms.)");  
        });
        b = System.currentTimeMillis();
        System.out.println("Individual community metrics done (" + (b-a) + " ms.)");
        
        
        // Global community metrics
        type = MetricTypeIdentifiers.GLOBAL_COMM_METRIC;
        System.out.println("Starting global community metrics...");
        a = System.currentTimeMillis();
        Map<String, Supplier<CommunityMetric<Long>>> globalCommMetrics = new HashMap<>();
        metricsSet = gridReader.getMetrics(type);
        GlobalCommunityMetricSelector<Long> globalSel = new GlobalCommunityMetricSelector<>();
        metricsSet.stream().forEach(metric -> globalCommMetrics.putAll(globalSel.getMetrics(metric, gridReader.getGrid(metric, MetricTypeIdentifiers.GLOBAL_COMM_METRIC))));
        System.out.println("Identified " + globalCommMetrics.size() + " metrics");

        // Compute each individual metric
        globalCommMetrics.entrySet().stream().forEach(entry -> 
        {
            String metric = entry.getKey();
            System.out.println("Running " + metric);

            Long a2 = System.currentTimeMillis();
            CommunityMetric<Long> gcm = entry.getValue().get();
            
            // Compute the metric for each community detection algorithm
            communities.entrySet().stream().forEach(comm -> 
            {
                double value = gcm.compute(graph, comm.getValue());
                metricvalues.put(metric + "_" + comm.getKey(), value);
                Long b2 = System.currentTimeMillis();
                System.out.println("Metric " + metric + " for community " + comm.getKey() + " done (" + (b2-a2) + " ms.)");
            });
            Long b3 = System.currentTimeMillis();
            System.out.println("Metric " + metric + " done (" + (b3-a2) + " ms.)");
        });
        b = System.currentTimeMillis();
        System.out.println("Global community metrics done (" + (b-a) + " ms.)");
        
        // Global graph metrics
        type = MetricTypeIdentifiers.GRAPH_METRIC;
        System.out.println("Starting graph metrics...");
        a = System.currentTimeMillis();
        Map<String, Supplier<GraphMetric<Long>>> graphMetrics = new HashMap<>();
        metricsSet = gridReader.getMetrics(type);
        GraphMetricSelector<Long> graphSel = new GraphMetricSelector<>();
        
        metricsSet.stream().forEach(metric -> graphMetrics.putAll(graphSel.getMetrics(metric, gridReader.getGrid(metric, MetricTypeIdentifiers.GRAPH_METRIC),dc)));
        System.out.println("Identified " + graphMetrics.size() + " metrics");

        graphMetrics.entrySet().stream().forEach(entry -> 
        {
            String metric = entry.getKey();
            System.out.println("Running " + metric);

            Long a2 = System.currentTimeMillis();
            GraphMetric<Long> gm = entry.getValue().get();
            double value = gm.compute(graph);
            metricvalues.put(metric, value);
            Long b2 = System.currentTimeMillis();
            
            System.out.println("Metric " + metric + " done (" + (b2-a2) + " ms.)");
        });
        b = System.currentTimeMillis();
        System.out.println("Graph metrics done (" + (b-a) + " ms.)");
        
        TwoGraphAnalyzer.printGlobalMetrics(output + "global.txt", metricvalues);
        
        
    }

    /**
     * Prints the different values for an individual metric in a file. Values are ordered
     * by node identifier.
     * @param file The route of the file.
     * @param values The values of the vertex metric.
     */
    private static void printIndividualMetric(String file, Map<Long, Double> values) 
    {
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))))
        {
            bw.write("node\tmetric\n");
            List<Long> nodes = new ArrayList<>(values.keySet());
            nodes.sort(Comparator.naturalOrder());
            
            for(long node : nodes)
            {    
                bw.write(node + "\t" + values.get(node) + "\n");
            }
        }
        catch(IOException ioe)
        {
            System.err.println("ERROR: The file " + file + " could not be written");
        }
    }
    
    /**
     * Prints the different values for an individual community metric in a file. Values are ordered
     * by node identifier.
     * @param file The route of the file.
     * @param values The values of the vertex metric.
     */
    private static void printIndividualCommMetric(String file, Map<Integer, Double> values) 
    {
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))))
        {
            bw.write("comm\tmetric\n");
            List<Integer> nodes = new ArrayList<>(values.keySet());
            nodes.sort(Comparator.naturalOrder());
            
            for(int node : nodes)
            {    
                bw.write(node + "\t" + values.get(node) + "\n");
            }
        }
        catch(IOException ioe)
        {
            System.err.println("ERROR: The file " + file + " could not be written");
        }
    }

    /**
     * Prints the different values for a pair/edge metric in a file. Values are ordered
     * by node identifier.
     * @param file The route of the file.
     * @param values The values of the vertex metric.
     */
    private static void printPairMetric(String file, Map<Pair<Long>, Double> values) 
    {
        // Comparator for ordering the pair of nodes.
        Comparator<Pair<Long>> comparator = (Pair<Long> p1, Pair<Long> p2) -> 
        {
            if(Objects.equals(p1.v1(), p2.v1()))
            {
                if(p1.v2() < p2.v2())
                    return -1;
                else if(p1.v2() > p2.v2())
                    return 1;
                else
                    return 0;
            }
            else
            {
                if(p1.v1() > p2.v1())
                    return 1;
                else
                    return -1;
            }
        };
        
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))))
        {
            bw.write("nodeA\tnodeB\tmetric\n");
            List<Pair<Long>> nodes = new ArrayList<>(values.keySet());
            nodes.sort(comparator);
            
            for(Pair<Long> node : nodes)
            {    
                bw.write(node.v1() + "\t" + node.v2() + "\t" + values.get(node) + "\n");
            }
        }
        catch(IOException ioe)
        {
            System.err.println("ERROR: The file " + file + " could not be written");
        }
    }

    /**
     * Prints the global metric values into a file.
     * @param file The route to the file.
     * @param values The metric values.
     */
    private static void printGlobalMetrics(String file, Map<String, Double> values) 
    {
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))))
        {
            bw.write("metric\tvalue\n");
            for(String metric : values.keySet())
            {
                bw.write(metric + "\t" + values.get(metric) + "\n");
            }
        } 
        catch (IOException ex) 
        {
            System.err.println("ERROR: The file " + file + " could not be written");
        }
    }
}
