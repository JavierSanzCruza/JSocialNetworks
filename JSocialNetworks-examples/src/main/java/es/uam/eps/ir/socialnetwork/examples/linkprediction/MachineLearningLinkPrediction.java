/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.linkprediction;

import es.uam.eps.ir.socialnetwork.adapters.Adapters;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.linkprediction.LinkPredictor;
import es.uam.eps.ir.socialnetwork.linkprediction.MLComparators;
import es.uam.eps.ir.socialnetwork.linkprediction.SupervisedLinkPredictor;
import es.uam.eps.ir.socialnetwork.linkprediction.filter.SocialLinkPredictionFastFilters;
import es.uam.eps.ir.socialnetwork.recommendation.ml.io.WekaPatternReader;

import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.io.BufferedWriter;
import java.io.FileOutputStream;


import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.function.Predicate;
import static java.util.stream.Collectors.toList;
import org.ranksys.core.util.tuples.Tuple2od;
import org.ranksys.formats.parsing.Parsers;
import static org.ranksys.formats.parsing.Parsers.lp;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.DecisionStump;

/**
 * Program for predicting new links using Machine Learning techniques.
 * @author Javier Sanz-Cruzado Puig
 */
public class MachineLearningLinkPrediction 
{
    /**
     * Predicts new links in a network using machine learning algorithms.
     * @param args Execution arguments
     * <ul>
     *  <li><b>Train data path:</b> Training graph location</li>
     *  <li><b>Output directory:</b> Route where to store predictions</li>
     *  <li><b>Rec:</b> Classifier. Available: <i>naivebayes, logistic</i> and <i>randomforest</i></li>
     *  <li><b>Inversed:</b> Indicates if the ranking has to be inversed or not</li>
     *  <li><b>PatternConf:</b> File containing the information about the attributes</li>
     *  <li><b>Train patterns:</b> Training patterns file</li>
     *  <li><b>Test patterns:</b> Test patterns file</li>
     *  <li><b>Directed:</b> "True" if the graph is directed, "false" if it is not</li>
     *  <li><b>Weighted:</b> "True" if the graph is weighted, "false" if it is not</li>
     * </ul>
     * @throws IOException if something fails while reading or writing files.
     */
    public static void main(String[] args) throws IOException
    {
        if(args.length < 10)
        {
            System.err.println("Usage: <trainDataPath> <outputDirectory> <rec> <inversed> <patternConf> <trainPatterns> <testPatterns> <directed> <weighted>");
            System.err.println("Available modes: naivebayes, logistic");
            return;
        }
        
        String trainDataPath = args[0];
        String outputDir = args[1];
        String recomm = args[2];
        boolean inversed = args[3].equals("true");
        String patternConf = args[4];
        String trainPatterns = args[5];
        String testPatterns = args[6];
        boolean directed = args[7].equals("true");
        boolean weighted = args[8].equals("true");
        
        
        // Load the graph
        
        TextGraphReader<Long> greader = new TextGraphReader<>(false, directed, weighted, true, "\t", Parsers.lp);
        Graph<Long> graph = greader.read(trainDataPath, weighted, false);
        
        FastGraph<Long> dirGraph = (FastGraph<Long>) Adapters.removeAutoloops(graph);
        
        WekaPatternReader<Long> reader = new WekaPatternReader<>();
        long timeA = System.currentTimeMillis();
        long timeB;
        if(reader.readAttributes(patternConf))
        {
            if(reader.readTrain(trainPatterns))
            {
                timeB = System.currentTimeMillis();
                System.out.println("Patterns read (" + (timeB-timeA) + " ms.)");
                timeA = System.currentTimeMillis();
                if(!reader.readTest(testPatterns, lp))
                {
                    System.err.println("Test patterns could not be read");
                    return;
                }
                timeB = System.currentTimeMillis();
                System.out.println("Test patterns read (" + (timeB - timeA) + " ms.)");
            }
            else
            {
               System.err.println("Train patterns could not be read");
               return;
            }
        }
        else
        {
            System.err.println("Pattern info could not be read");
            return;
        }

        Classifier classifier;
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
        
        // Filters. Now, it filters all the users that do not appear in train, it doesn't let the recommenders to recommend a user to himself, and takes only the neighbours
        Predicate<Pair<Long>> filter = SocialLinkPredictionFastFilters.onlyNewLinks(dirGraph).and(SocialLinkPredictionFastFilters.notSelf()).and(SocialLinkPredictionFastFilters.notReciprocal(dirGraph));
        
        System.out.println("Preparing " + recomm);
        long a = System.currentTimeMillis();
        LinkPredictor<Long> pred;
        if(!inversed)
            pred = new SupervisedLinkPredictor(dirGraph, MLComparators.descendingComparator(graph.getAllNodes().collect(toList())), classifier, reader.getTrainSet(), reader.getTestSet(), reader.getInstanceIndexer(), reader.getFVAttributes());
        else
            pred = new SupervisedLinkPredictor(dirGraph, MLComparators.ascendingComparator(graph.getAllNodes().collect(toList())), classifier, reader.getTrainSet(), reader.getTestSet(), reader.getInstanceIndexer(), reader.getFVAttributes());
        long b = System.currentTimeMillis();
        System.out.println("Running " + recomm + " (" + (b-a) + " ms.)");
        a = System.currentTimeMillis();
        List<Tuple2od<Pair<Long>>> res = pred.getPrediction(filter);
        b = System.currentTimeMillis();
        System.out.println("Prediction " + recomm + " done (" + (b-a) + " ms.)");
        a = System.currentTimeMillis();
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputDir + recomm + ".txt"))))
        {
            for(Tuple2od<Pair<Long>> value : res)
            {
                bw.write(value.v1.v1() + "\t" + value.v1.v2() + "\t" + value.v2 + "\n");
            }
        } 
        catch (IOException ex)
        {
            System.err.println(recomm + " failed at writing in file " + outputDir + recomm + ".txt");
        }
        b = System.currentTimeMillis();
        System.out.println("Algorithm " + recomm + "finished (" + (b-a) + " ms.");
    }    
}
