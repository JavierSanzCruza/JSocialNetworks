
/* 
 * Copyright (C) 2015 Information Retrieval Group at Universidad Autónoma
 * de Madrid, http://ir.ii.uam.es
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.recommendation.evaluation;

import es.uam.eps.ir.ranksys.socialextension.LuceneFeaturesReader;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.GraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.io.GraphReader;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.RecommendationMetricsParamReader;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.discount.DiscountSelector;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.RecommendationMetricParamReader;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.RecommendationMetricSelector;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.relevance.RelevanceSelector;
import es.uam.eps.ir.socialnetwork.recommendation.FastGraphIndex;
import es.uam.eps.ir.socialnetwork.recommendation.GraphIndex;
import es.uam.eps.ir.socialnetwork.recommendation.data.GraphSimplePreferenceData;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.openide.util.Exceptions;
import org.ranksys.core.feature.FeatureData;
import org.ranksys.core.feature.SimpleFeatureData;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.formats.feature.SimpleFeaturesReader;
import org.ranksys.formats.parsing.Parsers;
import static org.ranksys.formats.parsing.Parsers.lp;
import static org.ranksys.formats.parsing.Parsers.sp;
import org.ranksys.formats.rec.RecommendationFormat;
import org.ranksys.formats.rec.TRECRecommendationFormat;
import org.ranksys.metrics.SystemMetric;
import org.ranksys.metrics.rank.RankingDiscountModel;
import org.ranksys.metrics.rel.RelevanceModel;



/**
 * Program for evaluating the accuracy of a recommendation, in terms of precision and recall.
 *
 * @author Javier Sanz-Cruzado Puig
 */
public class RecommenderEvaluation 
{
    /**
     * Evaluates a set of recommenders
     * @param args Execution arguments:
     * <ul>
     *  <li><b>trainPath:</b>Path of a file containing the training data</li>
     *  <li><b>testPath:</b> Path of a file containing the test data</li>
     *  <li><b>recs:</b> Directory containing the recommendations</li>
     *  <li><b>features</b> Node features</li>
     *  <li><b>indexMode:</b> true if features are stored in a text index, false if they are stored in a file</li>
     *  <li><b>output:</b> Output filename</li>
     *  <li><b>cutoff:</b> Maximum length of the ranking to consider</li>
     *  <li><b>directed:</b> true if the graph is directed, false if not.</li>
     *  <li><b>grid:</b> Grid containing all the metrics to execute.</li>
     * </ul>
     * @throws Exception if something fails at execution
     */
    public static void main(String[] args) throws Exception 
    {
        if(args.length < 10)
        {
            System.err.println("Usage: <trainPath> <testPath> <rec directory> <feature file> <indexMode> <output file> <cutoff> <directed> <metrics> <onlyTest>");
            return;
        }
       
        String trainDataPath = args[0];
        String testDataPath = args[1];
        String recIn = args[2];
        String features = args[3];
        boolean index = args[4].equalsIgnoreCase("true");
        String output = args[5];
        int cutoff = new Integer(args[6]);
        boolean directed = args[7].equalsIgnoreCase("true");
        String metricsGrid = args[8];
        boolean onlyTest = args[9].equalsIgnoreCase("true");
                
        GraphReader<Long> greader = new TextGraphReader<>(false, directed, true, true, "\t", Parsers.lp);
        Graph<Long> auxtrain = greader.read(trainDataPath, true, false);
        Graph<Long> auxtest = greader.read(testDataPath, true, false);
        
        GraphGenerator<Long> ggen = new EmptyGraphGenerator<>();
        ggen.configure(directed, true);
        FastGraph<Long> trainGraph = (FastGraph<Long>)ggen.generate();
        FastGraph<Long> testGraph = (FastGraph<Long>)ggen.generate();
        
        auxtrain.getAllNodes().forEach(u -> 
        {
           trainGraph.addNode(u);
           testGraph.addNode(u);
        });
        
        auxtest.getAllNodes().forEach(u -> 
        {
           trainGraph.addNode(u);
           testGraph.addNode(u);
        });
        
        auxtrain.getAllNodes().forEach(u -> 
        {
            auxtrain.getAdjacentNodesWeights(u).forEach(v -> trainGraph.addEdge(u, v.getIdx(), v.getValue()));
        });
        
        auxtest.getAllNodes().forEach(u -> 
        {
            auxtest.getAdjacentNodesWeights(u).forEach(v -> testGraph.addEdge(u, v.getIdx(), v.getValue()));
        });
        
        GraphIndex<Long> gindex = new FastGraphIndex<>(trainGraph);
        
        // Load the features
        FeatureData<Long,String,Double> featureData;
        if(index)
        {
            featureData = SimpleFeatureData.load(LuceneFeaturesReader.load(features, trainGraph));
        }
        else
        {
            featureData = SimpleFeatureData.load(SimpleFeaturesReader.get().read(features, lp, sp));
        }

        // Load the train and test data
        PreferenceData<Long,Long> trainData  = GraphSimplePreferenceData.load(trainGraph);
        PreferenceData<Long,Long> testData = GraphSimplePreferenceData.load(testGraph);
        
        // Read the grid containing the information about the metrics to compute.
        RecommendationMetricsParamReader recParamReader = new RecommendationMetricsParamReader(metricsGrid);
        recParamReader.readDocument();
        
        RelevanceSelector<Long, Long> relSelector = new RelevanceSelector<>();
        RelevanceModel<Long,Long> relModel = relSelector.select(recParamReader.getRelevanceModelParams(),trainData, testData).v2();

        DiscountSelector<Long, Long> discSelector = new DiscountSelector<>();
        RankingDiscountModel discModel = discSelector.select(recParamReader.getDiscountModelParams()).v2();
        
        Map<String, SystemMetric<Long, Long>> sysMetrics = new HashMap<>();
        
        RecommendationMetricSelector<Long,Long,String> metricSelector = new RecommendationMetricSelector<>();
        for(RecommendationMetricParamReader metrParam : recParamReader.getMetricsParams())
        {
            Tuple2oo<String, SystemMetric<Long,Long>> metric = metricSelector.select(metrParam, trainData, testData, featureData, relModel, discModel, cutoff);
            sysMetrics.put(metric.v1(), metric.v2());
        }
        
        // Compute the metrics.
        Map<String, Map<String, Double>> metrics = new HashMap<>();
        
        File f = new File(recIn);
        String[] recommenders = f.list();
        if(!f.isDirectory() || recommenders == null)
        {
            System.err.println("Nothing to evaluate");
            return;
        }
                
        RecommendationFormat<Long, Long> format = new TRECRecommendationFormat<>(lp,lp);
        
        double count = (onlyTest) ? testData.numUsersWithPreferences() + 0.0 : gindex.numUsers() + 0.0;
        
        Set<Long> usersWithPreferences = testData.getUsersWithPreferences().collect(Collectors.toCollection(HashSet::new));
        
        for(String recomm : recommenders)
        {
            sysMetrics.values().forEach(metric -> metric.reset());
            double totalcount;
            if(onlyTest)
            {
                totalcount = format.getReader(recIn + recomm).readAll().filter(rec -> usersWithPreferences.contains(rec.getUser())).mapToDouble(rec -> 
                {
                    sysMetrics.values().forEach(metric -> metric.add(rec));
                    return 1.0;
                }).sum();
            }
            else
            {
                totalcount = format.getReader(recIn + recomm).readAll().mapToDouble(rec -> 
                {
                    sysMetrics.values().forEach(metric -> metric.add(rec));
                    return 1.0;
                }).sum();
                format.getReader(recIn + recomm).readAll().forEach(rec -> sysMetrics.values().forEach(metric -> metric.add(rec)));
            }
            metrics.put(recomm, new HashMap<>());
            sysMetrics.forEach((name, metric) -> metrics.get(recomm).put(name, metric.evaluate()*totalcount/count));
        }
        
        List<String> metricsNames = new ArrayList<>();
        for(String metricName : sysMetrics.keySet())
        {
            metricsNames.add(metricName);
        }
        
        // Write the values of the metrics.
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output))))
        {
            bw.write("Recommender");
            for(String metr : metricsNames)
            {
                bw.write("\t" + metr);
            }
            bw.write("\n");
            
            metrics.entrySet().forEach(entry -> {
                try {
                    bw.write(entry.getKey());
                    for(String metr : metricsNames)
                    {
                        bw.write("\t" + entry.getValue().get(metr));
                    }
                    bw.write("\n");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        }
    }
}
