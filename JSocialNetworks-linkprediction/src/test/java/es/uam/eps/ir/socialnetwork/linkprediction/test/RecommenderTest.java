package es.uam.eps.ir.socialnetwork.linkprediction.test;
/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastDirectedUnweightedGraph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.ir.BIRRecommender;
import es.uam.eps.ir.socialnetwork.recommendation.ir.BM25Recommender;
import es.uam.eps.ir.socialnetwork.recommendation.ir.ExtremeBM25Recommender;
import es.uam.eps.ir.socialnetwork.recommendation.ir.QLJMRecommender;
import es.uam.eps.ir.socialnetwork.recommendation.ir.TfIdfRecommender;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.degree.AdamicRecommender;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.degree.HubDepressedIndexRecommender;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.degree.HubPromotedIndexRecommender;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.degree.JaccardRecommender;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.degree.MostCommonNeighboursDocumentBasedRecommender;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.degree.MostCommonNeighboursRecommender;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.degree.MostCommonNeighboursTermBasedRecommender;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.degree.PreferentialAttachmentRecommender;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.degree.ResourceAllocationRecommender;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.degree.SaltonRecommender;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.degree.SorensenRecommender;
import es.uam.eps.ir.socialnetwork.recommendation.linkprediction.distance.DistanceRecommender;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ranksys.core.Recommendation;
import org.ranksys.core.index.fast.FastItemIndex;
import org.ranksys.core.index.fast.FastUserIndex;
import org.ranksys.core.index.fast.SimpleFastItemIndex;
import org.ranksys.core.index.fast.SimpleFastUserIndex;
import org.ranksys.recommenders.Recommender;

/**
 * Class that tests some recommenders.
 * @author Javier Sanz-Cruzado Puig
 */
public class RecommenderTest 
{
    
    FastUserIndex<Long> uIndex;
    FastItemIndex<Long> iIndex;
    FastGraph<Long> graph;
    
    public RecommenderTest() 
    {
        List<Long> users = new ArrayList<>();
        users.add(0L);
        users.add(1L);
        users.add(2L);
        users.add(3L);
        users.add(4L);
        
        uIndex = SimpleFastUserIndex.load(users.stream());
        iIndex = SimpleFastItemIndex.load(users.stream());
        
        graph = new FastDirectedUnweightedGraph<>();
        graph.addNode(0L);
        graph.addNode(1L);
        graph.addNode(2L);
        graph.addNode(3L);
        graph.addNode(4L);
        
        graph.addEdge(0L, 1L);
        graph.addEdge(0L, 3L);
        graph.addEdge(1L, 3L);
        graph.addEdge(2L, 0L);
        graph.addEdge(2L, 4L);
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    /**
     * Popularity algorithm
     */
    @Test
    public void popularity()
    {
        Recommender<Long, Long> rec = new PreferentialAttachmentRecommender(graph, EdgeOrientation.IN);
        Recommendation<Long, Long> res = rec.getRecommendation(2L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(3L))
            {
                assertEquals(2.0, out.v2, 0.0001);
            }
        });
        
        rec = new PreferentialAttachmentRecommender(graph, EdgeOrientation.OUT);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                assertEquals(2.0, out.v2, 0.0001);
            }
        });
    }
    
    /**
     * MCN algorithm
     */
    @Test
    public void mcn()
    {
        Recommender<Long, Long> rec = new MostCommonNeighboursRecommender(graph, EdgeOrientation.OUT, EdgeOrientation.IN);
        Recommendation<Long, Long> res = rec.getRecommendation(2L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(3L))
            {
                assertEquals(1.0, out.v2, 0.0001);
            }
        });
        
        rec = new MostCommonNeighboursRecommender(graph, EdgeOrientation.IN, EdgeOrientation.OUT);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                assertEquals(1.0, out.v2, 0.0001);
            }
        });
        
        rec = new MostCommonNeighboursTermBasedRecommender(graph, EdgeOrientation.IN, EdgeOrientation.OUT);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                assertEquals(1.0, out.v2, 0.0001);
            }
        });
        
        rec = new MostCommonNeighboursDocumentBasedRecommender(graph, EdgeOrientation.IN, EdgeOrientation.OUT);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                assertEquals(1.0, out.v2, 0.0001);
            }
        });
    }
    
    /**
     * Jaccard algorithm
     */
    @Test
    public void jaccard()
    {
        Recommender<Long, Long> rec = new JaccardRecommender(graph, EdgeOrientation.OUT, EdgeOrientation.IN);
        Recommendation<Long, Long> res = rec.getRecommendation(2L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(3L))
            {
                assertEquals(0.25, out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
        
        rec = new JaccardRecommender(graph, EdgeOrientation.IN, EdgeOrientation.OUT);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                assertEquals(0.25, out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
    }
    
    /**
     * Salton algorithm
     */
    @Test
    public void salton()
    {
        Recommender<Long, Long> rec = new SaltonRecommender(graph, EdgeOrientation.OUT, EdgeOrientation.IN);
        Recommendation<Long, Long> res = rec.getRecommendation(2L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(3L))
            {
                assertEquals(0.33333333, out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
        
        rec = new SaltonRecommender(graph, EdgeOrientation.IN, EdgeOrientation.OUT);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                assertEquals(0.33333333, out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
    }
    
    /**
     * Sorensen algorithm
     */
    @Test
    public void sorensen()
    {
        Recommender<Long, Long> rec = new SorensenRecommender(graph, EdgeOrientation.OUT, EdgeOrientation.IN);
        Recommendation<Long, Long> res = rec.getRecommendation(2L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(3L))
            {
                assertEquals(0.4, out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
        
        rec = new SorensenRecommender(graph, EdgeOrientation.IN, EdgeOrientation.OUT);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                assertEquals(0.4, out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
    }
    
    /**
     * Hub Promoted Index algorithm
     */
    @Test
    public void hpi()
    {
        Recommender<Long, Long> rec = new HubPromotedIndexRecommender(graph, EdgeOrientation.OUT, EdgeOrientation.IN);
        Recommendation<Long, Long> res = rec.getRecommendation(2L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(3L))
            {
                assertEquals(0.3333333, out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
        
        rec = new HubPromotedIndexRecommender(graph, EdgeOrientation.IN, EdgeOrientation.OUT);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                assertEquals(0.3333333, out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
    }
    
    /**
     * Hub Depressed Index algorithm
     */
    @Test
    public void hdi()
    {
        Recommender<Long, Long> rec = new HubDepressedIndexRecommender(graph, EdgeOrientation.OUT, EdgeOrientation.IN);
        Recommendation<Long, Long> res = rec.getRecommendation(2L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(3L))
            {
                assertEquals(0.3333333, out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
        
        rec = new HubDepressedIndexRecommender(graph, EdgeOrientation.IN, EdgeOrientation.OUT);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                assertEquals(0.3333333, out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
    }
    
    /**
     * Adamic/Adar algorithm
     */
    @Test
    public void adamic()
    {
        Recommender<Long, Long> rec = new AdamicRecommender(graph, EdgeOrientation.OUT, EdgeOrientation.IN, EdgeOrientation.UND);
        Recommendation<Long, Long> res = rec.getRecommendation(2L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(3L))
            {
                assertEquals(Math.log(2)/Math.log(5), out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
        
        rec = new AdamicRecommender(graph, EdgeOrientation.IN, EdgeOrientation.OUT, EdgeOrientation.UND);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                assertEquals(Math.log(2)/Math.log(5), out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
    }
    
    /**
     * Resource Allocation algorithm
     */
    @Test
    public void resalloc()
    {
        Recommender<Long, Long> rec = new ResourceAllocationRecommender(graph, EdgeOrientation.OUT, EdgeOrientation.IN, EdgeOrientation.UND);
        Recommendation<Long, Long> res = rec.getRecommendation(2L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(3L))
            {
                assertEquals(0.2, out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
        
        rec = new ResourceAllocationRecommender(graph, EdgeOrientation.IN, EdgeOrientation.OUT, EdgeOrientation.UND);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                assertEquals(0.2, out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
    }
    
    /**
     * Distance algorithm
     */
    @Test
    public void distance()
    {
        Recommender<Long, Long> rec = new DistanceRecommender(graph, EdgeOrientation.OUT);
        Recommendation<Long, Long> res = rec.getRecommendation(2L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(3L))
            {
                assertEquals(-2.0, out.v2, 1.0); // 1.0 + in the denom. for preventing NaNs
            }
        });
        
        rec = new DistanceRecommender(graph, EdgeOrientation.IN);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                assertEquals(-2.0, out.v2, 1.0); // 1.0 + in the denom. for preventing NaNs
            }
        });
    }
    
    /**
     * TF-IDF algorithm
     */
    @Test
    public void tfidf()
    {
        Recommender<Long, Long> rec = new TfIdfRecommender(graph, EdgeOrientation.OUT, EdgeOrientation.IN);
        Recommendation<Long, Long> res = rec.getRecommendation(2L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(3L))
            {
                double num = (Math.log(7)/Math.log(2) - 1)*(3 - Math.log(3)/Math.log(2));
                double normD = Math.sqrt((3 - Math.log(3)/Math.log(2))*(3 - Math.log(3)/Math.log(2)) + (Math.log(7)/Math.log(2) - 1)*(Math.log(7)/Math.log(2) - 1));
                assertEquals(num/normD, out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
        
        rec = new TfIdfRecommender(graph, EdgeOrientation.IN, EdgeOrientation.OUT);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                double num = (Math.log(7)/Math.log(2) - 1)*(3 - Math.log(3)/Math.log(2));
                double normD = Math.sqrt((Math.log(7)/Math.log(2) - 1)*(Math.log(7)/Math.log(2) - 1) + (Math.log(7)/Math.log(2) - 1)*(Math.log(7)/Math.log(2) - 1));
                assertEquals(num/normD, out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
        
        
    }
    
    
    /**
     * BIR algorithm
     */
    @Test
    public void bir()
    {
        Recommender<Long, Long> rec = new BIRRecommender(graph, EdgeOrientation.OUT, EdgeOrientation.IN);
        Recommendation<Long, Long> res = rec.getRecommendation(2L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(3L))
            {
                assertEquals(Math.log(7) - Math.log(5), out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
        
        rec = new BIRRecommender(graph, EdgeOrientation.IN, EdgeOrientation.OUT);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                assertEquals(Math.log(3), out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
    }
    
    /**
     * BM25 algorithm
     */
    @Test
    public void bm25()
    {
        Recommender<Long, Long> rec = new BM25Recommender(graph, EdgeOrientation.OUT, EdgeOrientation.IN, EdgeOrientation.IN, 0.1,1.0);
        Recommendation<Long, Long> res = rec.getRecommendation(2L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(3L))
            {
                assertEquals(2.0/2.1*(Math.log(7) - Math.log(5)), out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
        
        rec = new BM25Recommender(graph, EdgeOrientation.IN, EdgeOrientation.OUT, EdgeOrientation.OUT, 0.1,1.0);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                assertEquals(2.0/2.1*(Math.log(3)), out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
    }
    
    /**
     * ExtremeBM25 algorithm
     */
    @Test
    public void ebm25()
    {
        Recommender<Long, Long> rec = new ExtremeBM25Recommender(graph, EdgeOrientation.OUT, EdgeOrientation.IN, EdgeOrientation.IN, 0.1);
        Recommendation<Long, Long> res = rec.getRecommendation(2L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(3L))
            {
                assertEquals((Math.log(7) - Math.log(5))/1.1, out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
        
        rec = new ExtremeBM25Recommender(graph, EdgeOrientation.IN, EdgeOrientation.OUT, EdgeOrientation.OUT, 0.1);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                assertEquals((Math.log(3))/1.1, out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
    }
    
    /**
     * Query Likelihood - Jelinek Mercer smoothing algorithm
     */
    @Test
    public void qljm()
    {
        Recommender<Long, Long> rec = new QLJMRecommender(graph, EdgeOrientation.OUT, EdgeOrientation.IN, 0.5);
        Recommendation<Long, Long> res = rec.getRecommendation(2L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(3L))
            {
                assertEquals(Math.log(2.25), out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
        
        rec = new QLJMRecommender(graph, EdgeOrientation.IN, EdgeOrientation.OUT,0.5);
        res = rec.getRecommendation(3L);
        res.getItems().forEach(out -> {
            if(out.v1.equals(2L))
            {
                assertEquals(Math.log(3.5), out.v2, 0.0001); // 1.0 + in the denom. for preventing NaNs
            }
        });
    }

}
