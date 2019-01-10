/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.linkprediction;

import es.uam.eps.ir.socialnetwork.recommendation.data.GraphSimpleFastPreferenceData;
import es.uam.eps.ir.socialnetwork.adapters.Adapters;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridSelector;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridReader;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.linkprediction.LinkPredictor;
import es.uam.eps.ir.socialnetwork.linkprediction.MLComparators;
import es.uam.eps.ir.socialnetwork.linkprediction.RecommendationLinkPredictor;
import es.uam.eps.ir.socialnetwork.linkprediction.filter.SocialLinkPredictionFastFilters;
import es.uam.eps.ir.socialnetwork.recommendation.FastGraphIndex;
import es.uam.eps.ir.socialnetwork.recommendation.GraphIndex;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import static java.util.stream.Collectors.toList;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.core.util.tuples.Tuple2od;
import org.ranksys.formats.parsing.Parsers;
import org.ranksys.recommenders.Recommender;

/**
 * Program for applying recommendation algorithms for link prediction
 * @author Javier Sanz-Cruzado Puig
 */
public class LinkPrediction 
{
    /**
     * Computes the recommendations for identifying disappearing links
     * @param args <ul>
     * <li><b>Train:</b> Training data </li>
     * <li><b>Grid:</b> Location of the XML grid file</li>
     * <li><b>Output:</b> Directory for storing the recommendations</li>
     * <li><b>Directed:</b> "true" if the graph is directed, "false" if not</li>
     * <li><b>Weighted:</b> "true" if the graph is directed, "false" if not</li>
     * <li><b>Invert:</b> inverts the rankings</li>
     * </ul>
     */
    public static void main(String[] args)
    {
        if(args.length < 7)
        {
            System.err.println("Invalid arguments.");
            System.err.println("Usage:");
            System.err.println("\ttrain: Training data");
            System.err.println("\tgrid: Location of the XML grid file");
            System.err.println("\toutput: Directory for storing the recommendations");
            System.err.println("\tdirected: true if the graph is directed, false if not");
            System.err.println("\tweighted: true if the graph is weighted, false if not");
            System.err.println("\tinvert: inverts the ranking if true");
            return;
        }
        
        String trainDataPath = args[0];
        String testDataPath = args[1];
        String gridPath = args[2];
        String outputPath = args[3];
        boolean directed = args[4].equalsIgnoreCase("true");
        boolean weighted = args[5].equalsIgnoreCase("true");
        boolean invert = args[6].equalsIgnoreCase("true");
        
        TextGraphReader<Long> greader = new TextGraphReader<>(false, directed, weighted, false, "\t", Parsers.lp);
        Graph<Long> auxgraph = greader.read(trainDataPath, weighted, false);
        Graph<Long> testGraph = greader.read(testDataPath, weighted, false);
        
        FastGraph<Long> trainGraph = (FastGraph<Long>) Adapters.removeAutoloops(auxgraph);
        FastGraph<Long> complgraph = (FastGraph<Long>) Adapters.addAllAutoloops(trainGraph);
        
        GraphIndex<Long> index = new FastGraphIndex<>(trainGraph);
        // Read the training and test data
        FastPreferenceData<Long, Long> trainData = GraphSimpleFastPreferenceData.load(trainGraph);

        // Read the XML containing the parameter grid for each algorithm
        AlgorithmGridReader gridreader = new AlgorithmGridReader(gridPath);
        gridreader.readDocument();
        Map<String, Supplier<Recommender<Long,Long>>> recMap = new HashMap<>();
        // Get the different recommenders to execute
        gridreader.getAlgorithms().forEach(algorithm -> 
        {
            AlgorithmGridSelector<Long> ags = new AlgorithmGridSelector<>();
            if(algorithm.startsWith("Complementary"))
            {
                recMap.putAll(ags.getRecommenders(algorithm, gridreader.getGrid(algorithm), complgraph, trainData));
            }
            else
            {
                recMap.putAll(ags.getRecommenders(algorithm, gridreader.getGrid(algorithm), trainGraph, trainData));
            }
        });
        
        // Select the set of users to be recommended, the format, and the filters to apply to the recommendation
        Predicate<Pair<Long>> filter = SocialLinkPredictionFastFilters.onlyNewLinks(trainGraph).and(SocialLinkPredictionFastFilters.notSelf());
        recMap.entrySet().parallelStream().forEach(entry -> 
        {
            String name = entry.getKey();
            System.out.println("Preparing " + name);
            Supplier<Recommender<Long,Long>> recomm = entry.getValue();
            
            long a = System.currentTimeMillis();
            Recommender<Long, Long> rec = recomm.get();
            LinkPredictor<Long> pred;
            if(invert)
                pred = new RecommendationLinkPredictor<>(trainGraph, MLComparators.descendingComparator(trainGraph.getAllNodes().collect(toList())),rec);
            else
                pred = new RecommendationLinkPredictor<>(trainGraph, MLComparators.ascendingComparator(trainGraph.getAllNodes().collect(toList())),rec);
            long b = System.currentTimeMillis();
            System.out.println("Prepared " + name + " (" + (b-a) + " ms.)");
            a = System.currentTimeMillis();
            List<Tuple2od<Pair<Long>>> res = pred.getPrediction(filter);
            b = System.currentTimeMillis();
            System.out.println("Prediction " + name + " done (" + (b-a) + " ms.");
            try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath + name + ".txt"))))
            {
                for(Tuple2od<Pair<Long>> value : res)
                {
                    bw.write(value.v1.v1() + "\t" + value.v1.v2() + "\t" + value.v2 + "\n");
                }
            } 
            catch (IOException ex)
            {
                System.err.println(name + " failed at writing in file " + outputPath + name + ".txt");
            }
            b = System.currentTimeMillis();
            System.out.println("Algorithm " + name + " finished (" + (b-a) + " ms.");
        });
    }
}
