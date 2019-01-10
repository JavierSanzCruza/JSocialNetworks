/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.recommendation;

import es.uam.eps.ir.socialnetwork.adapters.Adapters;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.recommendation.FastGraphIndex;
import es.uam.eps.ir.socialnetwork.recommendation.GraphIndex;

import es.uam.eps.ir.socialnetwork.recommendation.filter.SocialFastFilters;
import es.uam.eps.ir.socialnetwork.recommendation.ml.MachineLearningWekaRecommender;
import es.uam.eps.ir.socialnetwork.recommendation.ml.io.WekaPatternReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.ranksys.evaluation.runner.RecommenderRunner;
import org.ranksys.evaluation.runner.fast.FastFilterRecommenderRunner;
import org.ranksys.evaluation.runner.fast.FastFilters;
import org.ranksys.formats.parsing.Parsers;
import static org.ranksys.formats.parsing.Parsers.lp;
import org.ranksys.formats.rec.RecommendationFormat;
import org.ranksys.formats.rec.TRECRecommendationFormat;
import org.ranksys.recommenders.Recommender;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.DecisionStump;

/**
 * Examples of recommenders based on supervised machine learning.
 * @author Javier Sanz-Cruzado Puig
 */
public class MachineLearningRecommender 
{
    /**
     * Program which computes recommendations using machine learning techniques.
     * @param args Execution arguments
     * <ul>
     *  <li><b>User index path:</b> File containing the user index.</li>
     *  <li><b>Train data path:</b> Training graph file</li>
     *  <li><b>Test data path:</b> Test graph file</li>
     *  <li><b>Train patterns:</b> Training patterns file</li>
     *  <li><b>Test patterns:</b> Test patterns file</li>
     *  <li><b>Output path:</b> A folder to store the outcomes of the recommenders</li>
     *  <li><b>Rec.:</b> A list of machine learning algorithms. Available: <i>Naive Bayes (naivebayes)</i> and <i>Logistic Regression (logistic)</i></li>
     * </ul>
     * @throws IOException if something fails while reading/writing files
     */
    public static void main(String[] args) throws IOException
    {
        if(args.length < 5)
        {
            System.err.println("Usage: <trainDataPath> <trainPatterns> <testPatterns> <output> <rec>");
            System.err.println("Available recommenders: naivebayes, logistic");
            return;
        }
        
        String trainDataPath = args[0];
        String trainPatterns = args[1];
        String testPatterns = args[2];
        String outputDir = args[3];
        String recomm = args[4];
               
        final long a = System.currentTimeMillis();
        
        WekaPatternReader<Long> reader = new WekaPatternReader<>();
        
        if(reader.readTrain(trainPatterns))
        {
            if(!reader.readTest(testPatterns, lp))
            {
                System.err.println("ERROR: could not read test patterns");
                return;
            }
        }
        else
        {
            System.err.println("ERROR: could not read train patterns");
            return;
        }
        
        
        long b = System.currentTimeMillis();
        
        System.out.println("Patterns read (" + (b-a) + " ms.)");
        
                
        TextGraphReader<Long> greader = new TextGraphReader<>(false, true, false, false, "\t", Parsers.lp);
        Graph<Long> auxgraph = greader.read(trainDataPath, false, false);
        
        FastGraph<Long> graph = (FastGraph<Long>) Adapters.removeAutoloops(auxgraph);
                        
        GraphIndex<Long> index = new FastGraphIndex<>(graph);
        
        // Load the graph
        b = System.currentTimeMillis();
        System.out.println("Graph read (" + (b-a) + " ms.)");
        
        
        
        weka.classifiers.Classifier classifier;
        Map<String, Supplier<Recommender<Long,Long>>> recMap = new HashMap<>();

        switch(recomm.toLowerCase())
        {
            case "naivebayes":
                classifier = new NaiveBayes();
                break;
            case "logistic":
                classifier = new Logistic();
                break;
            case "randomforest":
                AdaBoostM1 ada = new AdaBoostM1();
                ada.setClassifier(new DecisionStump());
                ada.setNumIterations(1000);
                classifier = ada;
                break;
            default:
                System.err.println("Unknown classifier. Available: naivebayes, logistic");
                return;
        }
        
        
        recMap.put(recomm, () -> {
            return new MachineLearningWekaRecommender<>(graph, classifier, reader.getTrainSet(), reader.getTestSet(), reader.getInstanceIndexer() , reader.getFVAttributes()); 
        });
        
        b = System.currentTimeMillis();
        System.out.println("Classifier training done (" + (b-a) + " ms.)");
        
        // Makes recommendations only for the users in this set
        Set<Long> targetUsers = index.getAllUsers().collect(Collectors.toCollection(HashSet::new));
        // Selects the TREC Recommendation format
        RecommendationFormat<Long,Long> format = new TRECRecommendationFormat<>(lp,lp);
        // Filters. Now, it filters all the users that do not appear in train, it doesn't let the recommenders to recommend a user to himself, and takes only the neighbours
        Function<Long, IntPredicate> filter = FastFilters.and(FastFilters.notSelf(index),SocialFastFilters.notReciprocal(graph,index));
        
        // Maximum length of the recommendationa
        int maxLength = 10;
        // Runner
        RecommenderRunner<Long,Long> runner = new FastFilterRecommenderRunner<>(index, index, targetUsers.stream(), filter, maxLength);
        
        recMap.entrySet().parallelStream().forEach(entry -> 
        {
            String name = entry.getKey();
            
            System.out.println("Preparing " + name);
            Supplier<Recommender<Long,Long>> recommender = entry.getValue();
            
            // Configuring the recommender
            Recommender<Long,Long> rec = recommender.get();
            long c = System.currentTimeMillis();
            System.out.println("Prepared " + name + " (" + (c-a) + " ms.)");
            
            // Executing the recommendation
            try(RecommendationFormat.Writer<Long, Long> writer = format.getWriter(outputDir + name + ".txt")) 
            {
                System.out.println("Running " + name);

                runner.run(rec, writer);
                c = System.currentTimeMillis();
                System.out.println("Done " + name + " (" + (c-a) + "ms.)");
            } 
            catch (IOException ex) // An error happens
            {
                System.err.println("Algorithm " + name + " failed");
            }
        });
    }    
}
