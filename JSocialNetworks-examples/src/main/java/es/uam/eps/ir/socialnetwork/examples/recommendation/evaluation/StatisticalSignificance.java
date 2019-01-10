/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.recommendation.evaluation;

import es.uam.eps.ir.socialnetwork.recommendation.data.GraphSimplePreferenceData;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.stat.inference.TTest;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.formats.parsing.Parsers;
import static org.ranksys.formats.parsing.Parsers.lp;
import org.ranksys.formats.preference.SimpleRatingPreferencesReader;
import org.ranksys.formats.rec.RecommendationFormat;
import org.ranksys.formats.rec.TRECRecommendationFormat;
import org.ranksys.metrics.RecommendationMetric;
import org.ranksys.metrics.basic.NDCG;
import org.ranksys.metrics.basic.Precision;
import org.ranksys.metrics.basic.Recall;
import org.ranksys.metrics.rel.BinaryRelevanceModel;

/**
 * Given some recommendations, computes the statistical significance between them.
 * @author Javier Sanz-Cruzado Puig
 */
public class StatisticalSignificance
{
    /**
     * Computes the p-values of different recommendations in terms of the precision, and writes
     * them to a file.
     * @param args <ul>
     *              <li><b>Test data path:</b> Route to the test data</li>
     *              <li><b>Rec. Path:</b> Path to the directory which contains all the recommendations</li>
     *              <li><b>Output:</b> File in which to store the statistical significance results</li>
     *              <li><b>Cutoff:</b> Number of items to consider in the recommendation</li>
     *              <li><b>Threshold:</b> Minimum value for relevance</li>
     *              <li><b>Num. tails:</b> Number of tails to consider</li>
     *              <li><b>Directed:</b> True if the graph is directed, false if it is not</li>
     *              <li><b>Precision:</b> p if we want to check the statistical significance of precision, r if we want recall, ndcg if we want nDCG</li>
     *             </ul>
     * @throws IOException If something goes wrong while reading files
     */
    public static void main(String[] args) throws IOException
    {
        // Parameters
        if(args.length < 7)
        {
            System.err.println("Error: Invalid arguments ");
            for(int i = 0; i < args.length; ++i)
                System.err.print("\t" + i+":" + args[i]);
            
            System.err.println("");
            System.err.println("Usage: testDataPath recPath outputPath cutoff threshold numTails directed");
            return;
        }

        String testDataPath = args[0];
        String recPath = args[1];
        String outputPath = args[2];
        int numTails = Parsers.ip.parse(args[5]);
        if(numTails < 1 || numTails > 2)
        {
            System.err.println("Error: Invalid arguments: numTails must be 1 or 2 ");
            for(int i = 0; i < args.length; ++i)
                System.err.print("\t" + i+":" + args[i]);
            
            System.err.println("");
            System.err.println("Usage: testDataPath recPath outputPath cutoff threshold numTails");
            return;
        }
        
        Boolean directed = args[6].equalsIgnoreCase("true");
        String prec = args[7];
        // Read train and test data
        PreferenceData<Long, Long> testData = GraphSimplePreferenceData.load(SimpleRatingPreferencesReader.get().read(testDataPath, lp, lp), directed, false);
        
        int cutoff = new Integer(args[3]);
        double threshold = new Double(args[4]);
        
        Map<Long, Integer> userList = new HashMap<>();
        testData.getUsersWithPreferences().forEach(user -> {
            userList.put(user, userList.size());
        });
        
        // Relevance models definition
        BinaryRelevanceModel<Long, Long> binRel = new BinaryRelevanceModel<>(false, testData, threshold);             
        
        // Configuration of the P@k metric
        RecommendationMetric<Long, Long> metric;

        if(prec.equalsIgnoreCase("p"))metric = new Precision<>(cutoff, binRel);
        else if(prec.equalsIgnoreCase("r")) metric = new Recall<>(cutoff, binRel);
        else if (prec.equalsIgnoreCase("ndcg")) metric = new NDCG<>(cutoff, new NDCG.NDCGRelevanceModel<>(false, testData, threshold));
        else return; // Invalid metric
        Set<Long> users = new HashSet<>();
        Map<String, double[]> values = new HashMap<>();

        File directory = new File(recPath);
        if(directory.isDirectory() == false || directory.list().length == 0)
        {
            System.err.println("Nothing to evaluate!");
        }
        
        // TODO: Modificar para escribir, en cada columna, el valor de la métrica para el usuario correspondiente (no aparece en alguno, = 0.0). En este
        // main solamente se contempla la precision.
        
        // FILE FORMAT:
        // "user"\"Algorithm1"\"Algorithm2"\t...\t"AlgorithmN".
        // user\tmetricAlg1\tmetricAlg2\tmetricAlg3\t...\tmetricAlgN
        RecommendationFormat<Long, Long> format = new TRECRecommendationFormat<>(lp,lp);
        String[] files = directory.list();
        
        List<String> recomms = new ArrayList<>();
        
        for(String file : files)
        {           
            values.put(file, new double[userList.size()]);
            Map<Long, Double> indiv = new HashMap<>();
            format.getReader(recPath + file).readAll().forEach(rec -> 
            {
                if(userList.containsKey(rec.getUser()))
                {
                    double val = metric.evaluate(rec);
                    indiv.put(rec.getUser(), val);
                }
            });
            
            userList.entrySet().forEach(entry -> 
            {
               if(indiv.containsKey(entry.getKey()))
               {
                   values.get(file)[entry.getValue()] = indiv.get(entry.getKey());
               }
               else
               {
                   values.get(file)[entry.getValue()] = 0.0;
               }
            });
            
            recomms.add(file);
        }
        
        recomms.sort(Comparator.naturalOrder());
       
        // Compute the p-values.
        TTest ttest = new TTest();
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath))))
        {
            for(String file : recomms)
            {
                bw.write("\t" + file);
            }
            bw.write("\n");
            for(String f1 : recomms)
            {
                bw.write(f1);
                
                for(String f2 : recomms)
                {
                    if(f1.equals(f2))
                    {
                        bw.write("\n");
                        break;
                    }
                    else
                    {
                        double value = ttest.pairedTTest(values.get(f1), values.get(f2));
                        if(Double.isNaN(value))
                            value = 1.0;
                        else if(numTails == 1)
                            value /= 2.0;
                        bw.write("\t" + value);
                    }
                }
            }
        }
    }
}
