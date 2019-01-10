/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.linkprediction.patterns;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.community.io.CommunitiesReader;
import es.uam.eps.ir.socialnetwork.metrics.distance.Closeness;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import es.uam.eps.ir.socialnetwork.metrics.distance.NodeBetweenness;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.metrics.pair.Embededness;
import es.uam.eps.ir.socialnetwork.metrics.pair.NeighbourOverlap;
import es.uam.eps.ir.socialnetwork.metrics.vertex.Degree;
import es.uam.eps.ir.socialnetwork.metrics.vertex.LocalClusteringCoefficient;
import es.uam.eps.ir.socialnetwork.metrics.vertex.PageRank;
import es.uam.eps.ir.socialnetwork.recommendation.ml.MachineLearningRecommender;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import org.openide.util.Exceptions;
import org.ranksys.formats.parsing.Parsers;

/**
 * Class that generates patterns for each existent node in a given
 * @author Javier Sanz-Cruzado Puig
 */
public class TestPatternGenerator 
{
    /**
     * Program that generates test patterns for the both the new and disappearing prediction problems.
     * @param args Execution arguments
     * <ul>
     *  <li><b>Graph File:</b> Train graph (t_1)</li>
     *  <li><b>Class File:</b> Test graph (t_2)</li>
     *  <li><b>Louvain comm.:</b> File containing Louvain communities (for the train graph)</li>
     *  <li><b>Lead. Vector comm.:</b> File containing Leading Eigenvector communities (for the train graph)</li>
     *  <li><b>Infomap comm.:</b> File containing Infomap communities (for the train graph)</li>
     *  <li><b>Output path:</b> Folder in which to store the patterns for both problems</li>
     *  <li><b>Directed:</b> "true" if the graph is directed, "false" if it is not</li>
     * </ul>
     * @throws IOException if something fails while reading/writing files.
     */
    public static void main(String[] args) throws IOException
    {
        if(args.length < 6)
        {
            System.err.println("Usage: <trainGraph> <testGraph> <blondelComm> <newmanComm> <infomapComm> <outputPath> <directed>");
            return;
        }
        
        String input = args[0];
        String classes = args[1];
        String inputLouvain = args[2];
        String inputLeadVector = args[3];
        String inputInfomap = args[4];
        String output = args[5];
        boolean directed = args[6].equalsIgnoreCase("true");
        
        long a = System.currentTimeMillis();
        // Read the graph
        TextGraphReader<Long> greader = new TextGraphReader<>(false, directed, true, false, "\t", Parsers.lp);
        Graph<Long> graph = greader.read(input, true, false);
        
        // Read the classes: if an edge appears in the graph, the class is negative.
        Graph<Long> classgraph = greader.read(classes, true, false);
        
        long b = System.currentTimeMillis();
        
        System.out.println("Graphs read (" + (b-a) + " ms.");
        // Compute metrics:

        if(directed)
        {
            DistanceCalculator distcalc = new DistanceCalculator();
            distcalc.computeDistances(graph);
            b = System.currentTimeMillis();
            System.out.println("Distance metrics prepared " + (b-a) + " ms.");

            PageRank<Long> pr = new PageRank<>(0.15);
            Map<Long, Double> pageranks = pr.compute(graph);
            b = System.currentTimeMillis();
            System.out.println("PageRanks prepared (" + (b-a) + " ms.)");


            // Prepare other metrics for their executionj

            Degree<Long> inDegree = new Degree<>(EdgeOrientation.IN);
            Degree<Long> outDegree = new Degree<>(EdgeOrientation.OUT);
            NodeBetweenness<Long> betweenness = new NodeBetweenness(distcalc);
            Embededness<Long> embeddedness = new Embededness(EdgeOrientation.OUT, EdgeOrientation.IN);
            Closeness<Long> closeness = new Closeness<>(distcalc);
            LocalClusteringCoefficient<Long> clustcoef = new LocalClusteringCoefficient<>();
            NeighbourOverlap<Long> foaf = new NeighbourOverlap<>();
            Map<Long, Double> inDegrees = inDegree.compute(graph);
            Map<Long, Double> outDegrees = outDegree.compute(graph);
            Map<Long, Double> closenesses = closeness.compute(graph);
            Map<Long, Double> betweennesses = betweenness.compute(graph);
            Map<Long, Double> clustcoefs = clustcoef.compute(graph);

            // Communities
            CommunitiesReader<Long> reader = new CommunitiesReader<>();
            Communities<Long> louvain = reader.read(inputLouvain, "\t", Parsers.lp);
            Communities<Long> leadVector = reader.read(inputLeadVector, "\t", Parsers.lp);
            Communities<Long> infomap = reader.read(inputInfomap, "\t", Parsers.lp);
            b = System.currentTimeMillis();        
            System.out.println("Metrics prepared and initialized (" + (b-a) + " ms. )");

            try(BufferedWriter bw_lp = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output + "-classiclp")));
                BufferedWriter bw_dlp = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output + "-dissaplp"))))
            {
                // Attribute names
                bw_lp.write("inDegreeA\tinDegreeB\toutDegreeA\toutDegreeB\tbetwA\tbetwB\tembeddedness\tfoaf\tcloseA\tcloseB\tpageRankA\tpageRankB\tclustcoefA\tclustcoefB\tblondel\tinfomap\tnewman\tclass\n");
                bw_dlp.write("inDegreeA\tinDegreeB\toutDegreeA\toutDegreeB\tbetwA\tbetwB\tembeddedness\tfoaf\tcloseA\tcloseB\tpageRankA\tpageRankB\tclustcoefA\tclustcoefB\tblondel\tinfomap\tnewman\tclass\n");

                // Attribute types
                bw_lp.write("continuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tnominal\tnominal\tnominal\tclass\n");
                bw_dlp.write("continuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tnominal\tnominal\tnominal\tclass\n");


                graph.getAllNodes().forEach(u -> {
                    graph.getAllNodes().forEach(v -> {
                        try 
                        {
                            boolean contains = graph.containsEdge(u, v);
                            BufferedWriter bw = (contains ? bw_dlp : bw_lp);
                            bw.write("" + u);
                            bw.write("\t" + v);
                            bw.write("\t" + inDegrees.get(u));
                            bw.write("\t" + inDegrees.get(v));
                            bw.write("\t" + outDegrees.get(u));
                            bw.write("\t" + outDegrees.get(v));
                            bw.write("\t" + betweennesses.get(u));
                            bw.write("\t" + betweennesses.get(v));
                            bw.write("\t" + embeddedness.compute(graph,u,v));
                            bw.write("\t" + foaf.compute(graph,u,v));
                            bw.write("\t" + closenesses.get(u));
                            bw.write("\t" + closenesses.get(v));
                            bw.write("\t" + pageranks.get(u));
                            bw.write("\t" + pageranks.get(v));
                            bw.write("\t" + clustcoefs.get(u));
                            bw.write("\t" + clustcoefs.get(v));
                            bw.write("\t" + ((louvain.getCommunity(u) == louvain.getCommunity(v)) ? 1 : 0));
                            bw.write("\t" + ((leadVector.getCommunity(u) == leadVector.getCommunity(v)) ? 1 : 0));
                            bw.write("\t" + ((infomap.getCommunity(u) == infomap.getCommunity(v)) ? 1 : 0));
                            if(contains)
                                bw.write("\t" + (classgraph.containsEdge(u, v) ? MachineLearningRecommender.NEGATIVECLASS : MachineLearningRecommender.POSITIVECLASS) + "\n");
                            else
                                bw.write("\t" + (classgraph.containsEdge(u, v) ? MachineLearningRecommender.POSITIVECLASS : MachineLearningRecommender.NEGATIVECLASS) + "\n");
                        } 
                        catch (IOException ex) 
                        {
                            Exceptions.printStackTrace(ex);
                        }

                    });
                });
            }
        }
        else
        {
            DistanceCalculator distcalc = new DistanceCalculator();
            distcalc.computeDistances(graph);
            b = System.currentTimeMillis();
            System.out.println("Distance metrics prepared " + (b-a) + " ms.");

            PageRank<Long> pr = new PageRank<>(0.15);
            Map<Long, Double> pageranks = pr.compute(graph);
            b = System.currentTimeMillis();
            System.out.println("PageRanks prepared (" + (b-a) + " ms.)");


            // Prepare other metrics for their executionj

            Degree<Long> outDegree = new Degree<>(EdgeOrientation.OUT);
            NodeBetweenness<Long> betweenness = new NodeBetweenness(distcalc);
            Embededness<Long> embeddedness = new Embededness(EdgeOrientation.OUT, EdgeOrientation.IN);
            Closeness<Long> closeness = new Closeness<>(distcalc);
            LocalClusteringCoefficient<Long> clustcoef = new LocalClusteringCoefficient<>();
            NeighbourOverlap<Long> foaf = new NeighbourOverlap<>();
            Map<Long, Double> outDegrees = outDegree.compute(graph);
            Map<Long, Double> closenesses = closeness.compute(graph);
            Map<Long, Double> betweennesses = betweenness.compute(graph);
            Map<Long, Double> clustcoefs = clustcoef.compute(graph);

            // Communities
            CommunitiesReader<Long> reader = new CommunitiesReader<>();
            Communities<Long> louvain = reader.read(inputLouvain, "\t", Parsers.lp);
            Communities<Long> leadVector = reader.read(inputLeadVector, "\t", Parsers.lp);
            Communities<Long> infomap = reader.read(inputInfomap, "\t", Parsers.lp);
            b = System.currentTimeMillis();        
            System.out.println("Metrics prepared and initialized (" + (b-a) + " ms. )");

            try(BufferedWriter bw_lp = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output + "-classiclp")));
                BufferedWriter bw_dlp = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output + "-dissaplp"))))
            {
                // Attribute names
                bw_lp.write("degreeA\tdegreeB\tbetwA\tbetwB\tembeddedness\tfoaf\tcloseA\tcloseB\tpageRankA\tpageRankB\tclustcoefA\tclustcoefB\tblondel\tinfomap\tnewman\tclass\n");
                bw_dlp.write("degreeA\tdegreeB\tbetwA\tbetwB\tembeddedness\tfoaf\tcloseA\tcloseB\tpageRankA\tpageRankB\tclustcoefA\tclustcoefB\tblondel\tinfomap\tnewman\tclass\n");

                // Attribute types
                bw_lp.write("continuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tnominal\tnominal\tnominal\tclass\n");
                bw_dlp.write("continuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tcontinuous\tnominal\tnominal\tnominal\tclass\n");


                graph.getAllNodes().forEach(u -> {
                    graph.getAllNodes().forEach(v -> {
                        try 
                        {
                            boolean contains = graph.containsEdge(u, v);
                            BufferedWriter bw = (contains ? bw_dlp : bw_lp);
                            bw.write("" + u);
                            bw.write("\t" + v);
                            bw.write("\t" + outDegrees.get(u));
                            bw.write("\t" + outDegrees.get(v));
                            bw.write("\t" + betweennesses.get(u));
                            bw.write("\t" + betweennesses.get(v));
                            bw.write("\t" + embeddedness.compute(graph,u,v));
                            bw.write("\t" + foaf.compute(graph,u,v));
                            bw.write("\t" + closenesses.get(u));
                            bw.write("\t" + closenesses.get(v));
                            bw.write("\t" + pageranks.get(u));
                            bw.write("\t" + pageranks.get(v));
                            bw.write("\t" + clustcoefs.get(u));
                            bw.write("\t" + clustcoefs.get(v));
                            bw.write("\t" + ((louvain.getCommunity(u) == louvain.getCommunity(v)) ? "1" : "0"));
                            bw.write("\t" + ((leadVector.getCommunity(u) == leadVector.getCommunity(v)) ? "1" : "0"));
                            bw.write("\t" + ((infomap.getCommunity(u) == infomap.getCommunity(v)) ? "1" : "0"));
                            if(contains)
                                bw.write("\t" + (classgraph.containsEdge(u, v) ? MachineLearningRecommender.NEGATIVECLASS : MachineLearningRecommender.POSITIVECLASS) + "\n");
                            else
                                bw.write("\t" + (classgraph.containsEdge(u, v) ? MachineLearningRecommender.POSITIVECLASS : MachineLearningRecommender.NEGATIVECLASS) + "\n");
                        } 
                        catch (IOException ex) 
                        {
                            Exceptions.printStackTrace(ex);
                        }

                    });
                });
            }
        }
        b = System.currentTimeMillis();       
        System.out.println("Dataset finished (" + (b-a) + " ms. )");

    }
}
