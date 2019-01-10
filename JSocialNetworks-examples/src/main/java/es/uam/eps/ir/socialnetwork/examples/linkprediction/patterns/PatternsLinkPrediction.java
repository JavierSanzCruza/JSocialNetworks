/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.linkprediction.patterns;

import es.uam.eps.ir.socialnetwork.adapters.Adapters;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.linkprediction.LinkPredictor;
import es.uam.eps.ir.socialnetwork.linkprediction.MLComparators;
import es.uam.eps.ir.socialnetwork.linkprediction.PatternPostfixExLinkPredictor;
import es.uam.eps.ir.socialnetwork.linkprediction.filter.SocialLinkPredictionFastFilters;
import es.uam.eps.ir.socialnetwork.recommendation.ml.GraphPatternSet;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.ranksys.core.util.tuples.Tuple2od;
import org.ranksys.formats.parsing.Parsers;

/**
 * Generates link prediction algorithms from a given set of patterns.
 * @author Javier Sanz-Cruzado Puig
 */
public class PatternsLinkPrediction 
{
    /**
     * Computes link prediction from some pattern file.
     * @param args Execution arguments
     * <ul>
     *  <li><b>Train Data Path:</b> Location of the training graph</li>
     *  <li><b>Output Directory:</b> Directory where we want to store the predictions</li>
     *  <li><b>Test patterns:</b> The pattern file</li>
     *  <li><b>Postfix list:</b> List of postfix expressions to evaluate, using the indexes of the patterns. Expressions are comma separated, and they follow the next format:
     *  <ul><li>If two numbers are next to each other, they must be separated using a dot ("."). <i>Example:</i> 20.10+ (20 + 10)</li>
     *      <li> Operations allowed: +,-,*,/ </li></ul></li>
     *  <li><b>Directed:</b> "true" if the graph is directed, "false" if it is not</li>
     *  <li><b>Weighted:</b> "true" if the graph is weighted, "false" if it is not</li>
     * </ul>
     * @throws IOException if something fails while reading or writing from/in files
     */
    public static void main(String[] args) throws IOException
    {
        // Parameter check
        if(args.length < 6)
        {
            System.err.println("Usage: <trainDataPath> <outputDirectory> <testPatterns> <postfixList> <directed> <weighted>");
            System.err.println("postfixList contains, separated by commas, postfix expressions using the indexes of the patterns");
            System.err.println("Instead of a list, -a option can be used, to identify all the indexes of the patterns");
            return;
        }
        
        String trainDataPath = args[0];
        String outputDir = args[1];
        String testPatterns = args[2];
        String postfixList = args[3];
        boolean directed = args[4].equalsIgnoreCase("true");
        boolean weighted = args[5].equalsIgnoreCase("false");
               
        TextGraphReader<Long> greader = new TextGraphReader<>(false, directed, weighted, true, "\t", Parsers.lp);
        Graph<Long> graph = greader.read(trainDataPath, weighted, false);

        GraphPatternSet<Long> set = GraphPatternSet.read(testPatterns,"\t",false,directed, weighted);
        
        Graph<Long> dirGraph = Adapters.removeAutoloops(graph);
        int numAttrs = set.getNumAttrs();
        
        // Add all the possible parameters
        if(postfixList.equals("-a"))
        {
            postfixList = "0";
            for(int i = 1; i < numAttrs; ++i)
            {
                postfixList += "," + i;
            }
        }    
        
        // Filter
        Predicate<Pair<Long>> filter = SocialLinkPredictionFastFilters.onlyNewLinks(dirGraph).and(SocialLinkPredictionFastFilters.notSelf()).and(SocialLinkPredictionFastFilters.notReciprocal(dirGraph));
        Map<String, Supplier<LinkPredictor<Long>>> recMap = new HashMap<>();
        String[] postFixExprs = postfixList.split(",");
        
        Comparator<Tuple2od<Pair<Long>>> descComp = MLComparators.descendingComparator(graph.getAllNodes().collect(Collectors.toList()));
        Comparator<Tuple2od<Pair<Long>>> ascComp = MLComparators.ascendingComparator(graph.getAllNodes().collect(Collectors.toList()));

        for(String postfixExpr : postFixExprs)
        {
            recMap.put(postfixExpr, ()-> {
                return new PatternPostfixExLinkPredictor<>(dirGraph, descComp, set, postfixExpr);
            });
            recMap.put(postfixExpr + "-not", ()-> {
                return new PatternPostfixExLinkPredictor<>(dirGraph, ascComp, set, postfixExpr);
            });
        }
        
        // Compute the rankings and print them in a file
        recMap.entrySet().parallelStream().forEach(entry -> 
        {
            String name = entry.getKey();
            
            System.out.println("Preparing " + name);
            long a = System.currentTimeMillis();
            LinkPredictor<Long> pred = entry.getValue().get();
            long b = System.currentTimeMillis();
            System.out.println("Running " + name + " (" + (b-a) + " ms.)");
            List<Tuple2od<Pair<Long>>> res = pred.getPrediction(filter);
            b = System.currentTimeMillis();
            System.out.println("Prediction " + name + " done (" + (b-a) + " ms.)");
            try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputDir + name + ".txt"))))
            {
                for(Tuple2od<Pair<Long>> value : res)
                {
                    bw.write(value.v1.v1() + "\t" + value.v1.v2() + "\t" + value.v2 + "\n"); 
                }
                b = System.currentTimeMillis();
                System.out.println("Algorithm " + name + " finished (" + (b-a) + " ms.");
            } 
            catch (IOException ex) 
            {
                System.err.println(name + "failed at writing in file " + outputDir + name + ".txt");
            }
            
        });
    }
}
