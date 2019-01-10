/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.recommendation.evaluation;

    /*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this  file, 
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.core.preference.SimplePreferenceData;
import static org.ranksys.formats.parsing.Parsers.lp;
import org.ranksys.formats.preference.SimpleRatingPreferencesReader;
import org.ranksys.formats.rec.RecommendationFormat;
import org.ranksys.formats.rec.TRECRecommendationFormat;
import org.ranksys.metrics.RecommendationMetric;
import org.ranksys.metrics.basic.Precision;
import org.ranksys.metrics.rel.BinaryRelevanceModel;

/**
 * Computes, given a set of files, the individual values for some metrics.
 * @author Javier Sanz-Cruzado Puig
 */
public class IndividualEvaluation
{
    /**
     * Program which evaluates a series of recommendations individually for each
     * user.
     * @param args Execution arguments
     * <ul>
     *  <li><b>Test data path:</b> File containing the test data</li>
     *  <li><b>Rec. Path:</b> Route to the recommendation outcomes</li>
     *  <li><b>Output Path:</b> Folder in which to store the recommendations</li>
     *  <li><b>Cutoff:</b> Maximum length of the recommendation ranking</li>
     *  <li><b>Threshold:</b> Relevance threshold.</li>
     * </ul>
     * @throws Exception if something fails.
     */
    public static void main(String args[]) throws Exception
    {
        // Parameters
        if(args.length < 5)
        {
            System.err.println("Error: Invalid arguments ");
            for(int i = 0; i < args.length; ++i)
                System.err.print("\t" + i+":" + args[i]);
            
            System.err.println("");
            System.err.println("Usage: testDataPath recPath outputPath cutoff threshold");
            return;
        }

        String testDataPath = args[0];
        String recPath = args[1];
        String outputPath = args[2];
        
        // Read train and test data
        PreferenceData<Long, Long> testData = SimplePreferenceData.load(SimpleRatingPreferencesReader.get().read(testDataPath, lp, lp));
        
        int cutoff = new Integer(args[3]);
        double threshold = new Double(args[4]);
        
        // Relevance models definition
        BinaryRelevanceModel<Long, Long> binRel = new BinaryRelevanceModel<>(false, testData, threshold);             
        
        // Configuration of the P@k metric
        RecommendationMetric<Long, Long> metric = new Precision<>(cutoff, binRel);
        Map<String, Map<Long, Double>> values = new HashMap<>();
        Set<Long> users = new HashSet<>();
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
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath))))
        {
            String[] files = directory.list();
            bw.write("user");
            for(String file : files)
            {
                bw.write("\t" + file);
                
                Map<Long, Double> indivMetric = new HashMap<>();

                format.getReader(recPath + file).readAll().forEach(rec -> 
                {
                    double val = metric.evaluate(rec);
                    indivMetric.put(rec.getUser(), val);
                    users.add(rec.getUser());
                });

                values.put(file, indivMetric);
            }
            
            List<Long> userList = new ArrayList<>(users);
            for(Long user : userList)
            {
                if(testData.containsUser(user))
                {
                    bw.write("\n" + user);
                    for(String file : files)
                    {
                        double val = 0.0;
                        if(values.get(file).containsKey(user))
                        {
                            val = values.get(file).get(user);
                        }
                        bw.write("\t" + val);
                    }
                }
            }
        }
    }
}

