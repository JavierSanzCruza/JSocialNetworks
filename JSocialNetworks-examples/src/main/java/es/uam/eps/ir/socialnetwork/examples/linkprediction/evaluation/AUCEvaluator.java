/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.linkprediction.evaluation;

import org.ranksys.metrics.curves.GeneralAUC;
import es.uam.eps.ir.socialnetwork.recommendation.data.GraphSimpleFastPreferenceData;
import es.uam.eps.ir.socialnetwork.adapters.Adapters;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.recommendation.FastGraphIndex;
import es.uam.eps.ir.socialnetwork.recommendation.GraphIndex;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.formats.parsing.Parsers;
import static org.ranksys.formats.parsing.Parsers.lp;
import org.ranksys.metrics.rel.BinaryRelevanceModel;
import org.ranksys.metrics.rel.IdealRelevanceModel;

/**
 * Evaluates the Area Under the ROC Curve
 * @author Javier Sanz-Cruzado Puig
 */
public class AUCEvaluator 
{
    /**
     * Evaluates the Area Under the ROC Curve (GeneralAUC) for a set of link predictions.
     * @param args Execution arguments
     * <ul>
     *  <li><b>User index:</b> User index path</li>
     *  <li><b>Train:</b> Train data path</li>
     *  <li><b>Test:</b> Test data path</li>
     *  <li><b>Recommender folder:</b> Location of the link prediction outcomes</li>
     *  <li><b>Output folder:</b> Output file.
     * </ul>
     * @throws IOException If something fails while reading or writing
     */
    public static void main(String[] args) throws IOException
    {
        if(args.length < 5)
        {
            System.err.println("Usage: <userIndex> <train> <test> <recFolder> <outputFolder>");
            return;
        }
        
        String trainDataPath = args[1];
        String testDataPath = args[2];
        String recIn = args[3];
        String output = args[4];
        Double threshold = 0.5;
        boolean directed = args[5].equalsIgnoreCase("true");

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
        IdealRelevanceModel<Long,Long> binRel = new BinaryRelevanceModel<>(false,testData,threshold);
        
        File f = new File(recIn);
        String[] recommenders = f.list();
        if(!f.isDirectory() || recommenders == null)
        {
            System.err.println("Nothing to evaluate");
        }
        
        Map<String, Double> aucs = new HashMap<>();
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
            
            long total = graph.getAllNodes().filter(u -> testGraph.getNeighbourNodesCount(u) > 0).mapToLong(u -> 
            {
                return graph.getAdjacentNodes(u).filter(v -> testGraph.getNeighbourNodesCount(v) > 0).count();
            }).sum();
            
            // Compute the ROC curve
            GeneralAUC<Long,Long> roc = new GeneralAUC<>(index, binRel,total);
            double area = roc.evaluate(ranking);
            aucs.put(recomm, area);
            long b = System.currentTimeMillis();
            System.out.println("ROC curve for " + recomm + "done (" + (b-a) + " ms.)");
        }
        
        // Write the ROC Curve
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output))))
        {
            bw.write("Recommender\tAUC\n");
            for(String recpoint : aucs.keySet())
            {
                bw.write(recpoint + "\t" + aucs.get(recpoint)+"\n");
            }
        }
    }
}
