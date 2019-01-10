/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.linkprediction.evaluation;

import es.uam.eps.ir.socialnetwork.recommendation.data.GraphSimpleFastPreferenceData;
import org.ranksys.metrics.curves.GeneralROCCurve;
import es.uam.eps.ir.socialnetwork.adapters.Adapters;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.recommendation.FastGraphIndex;
import es.uam.eps.ir.socialnetwork.recommendation.GraphIndex;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.formats.parsing.Parsers;
import static org.ranksys.formats.parsing.Parsers.lp;
import org.ranksys.metrics.rel.BinaryRelevanceModel;

/**
 * Example of a ROC curve generator for some link prediction outcome.
 * @author Javier Sanz-Cruzado Puig
 */
public class ROCCurveGenerator 
{
    /**
     * Evaluates the Area Under the ROC Curve (AUC) for a set of link predictions.
     * @param args Execution arguments
     * <ul>
     *  <li><b>Train:</b> Train data path</li>
     *  <li><b>Test:</b> Test data path</li>
     *  <li><b>Recommender folder:</b> Location of the link prediction outcomes</li>
     *  <li><b>Output folder:</b> Output folder for storing the different ROC curves</li>
     *  <li><b>Threshold:</b> Relevance threshold</li>
     * </ul>
     * @throws IOException If something fails while reading or writing
     */
    public static void main(String[] args) throws IOException
    {
        if(args.length < 5)
        {
            System.err.println("Usage: <train> <test> <recFolder> <outputFolder> <threshold>");
            return;
        }
        
        String trainDataPath = args[0];
        String testDataPath = args[1];
        String recIn = args[2];
        String output = args[3];
        Double threshold = 0.5;
        boolean directed = args[4].equalsIgnoreCase("true");
        
        // Load the graph
        TextGraphReader<Long> greader = new TextGraphReader<>(false, directed, false, false, "\t", Parsers.lp);
        Graph<Long> auxgraph = greader.read(trainDataPath, false, false);
        FastGraph<Long> graph = (FastGraph<Long>) Adapters.removeAutoloops(auxgraph);
        auxgraph = greader.read(testDataPath, false, false);
        FastGraph<Long> testGraph = (FastGraph<Long>) Adapters.onlyTrainUsers(auxgraph, graph);
        
        GraphIndex<Long> index = new FastGraphIndex<>(graph);
        // Load the train and test data
        PreferenceData<Long,Long> testData = GraphSimpleFastPreferenceData.load(testGraph);
        // Binary relevance
        BinaryRelevanceModel<Long, Long> binRel = new BinaryRelevanceModel<>(false, testData, threshold);
        
        File f = new File(recIn);
        String[] recommenders = f.list();
        if(!f.isDirectory() || recommenders == null)
        {
            System.err.println("Nothing to evaluate");
        }
        
        for(String recomm : recommenders)
        {
            long a = System.currentTimeMillis();
            List<Tuple2oo<Long,Long>> ranking = new ArrayList<>();

            // Read the ranking
            try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(recIn + recomm))))
            {
                String line;
                while((line = br.readLine()) != null)
                {
                    String[] split = line.split("\t");
                    Long u = lp.parse(split[0]);
                    Long v = lp.parse(split[1]);
                    ranking.add(new Tuple2oo<>(u,v));
                }
            }
            
            // Compute the ROC curve
            GeneralROCCurve<Long,Long> roc = new GeneralROCCurve<>(index, binRel, graph.getEdgeCount());
            List<Pair<Double>> curve = roc.getCurve(ranking);
            
            // Write the ROC Curve
            try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output + recomm))))
            {
                for(Pair<Double> point : curve)
                {
                    bw.write(point.v1() + "\t" + point.v2() + "\n");
                }
            }
            
            long b = System.currentTimeMillis();
            System.out.println("ROC curve for " + recomm + "done (" + (b-a) + " ms.)");
        }
    }
}
