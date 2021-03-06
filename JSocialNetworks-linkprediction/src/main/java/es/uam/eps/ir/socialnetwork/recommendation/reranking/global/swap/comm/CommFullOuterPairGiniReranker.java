/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm;

import com.google.common.util.concurrent.AtomicDouble;
import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.utils.indexes.GiniIndex;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import org.ranksys.core.Recommendation;
import org.ranksys.core.util.tuples.Tuple2od;

/**
 * Reranks a recommendation by improving the Gini Index of the number of edges between two 
 * different communities in a community graph. Considers only those edges which
 * improve Gini Index, and go from one community to another.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class CommFullOuterPairGiniReranker<U> extends CommunityReranker<U> 
{

    /**
     * Number of edges between communities
     */
    private final List<Double> matrix;
    /**
     * Number of edges between communities.
     */
    private double sum;
    
    private double numWeak;
    
    /**
     * Constructor.
     * @param lambda Establishes the trait-off between the value of the Gini Index and the original score
     * @param cutoff The number of recommended items for each user.
     * @param norm true if the score and the Gini Index value have to be normalized.
     * @param rank true if the normalization is by ranking position, false if it is by score
     * @param graph The user graph.
     * @param communities A relation between communities and users in the graph.
     */
    public CommFullOuterPairGiniReranker(double lambda, int cutoff, boolean norm, boolean rank, Graph<U> graph, Communities<U> communities) 
    {
        super(lambda, cutoff, norm, rank,  graph, communities, false);
        
        matrix = new ArrayList<>();
        sum = 0.0;
    }

    @Override
    protected void update(Recommendation<U, U> reranked) {
    }

    @Override
    protected double novAddDelete(U u, Tuple2od<U> itemValue, Tuple2od<U> compared) 
    {
        U recomm = itemValue.v1;
        U del = compared.v1;

        Integer userComm = communities.getCommunity(u);
        Integer recommComm = communities.getCommunity(recomm);
        Integer delComm = communities.getCommunity(del);

        AtomicDouble atom = new AtomicDouble();
        atom.set(this.sum);
        GiniIndex gini = new GiniIndex();
        

        if(communityGraph.isDirected())
        {
            double toSum;
            if((userComm.equals(recommComm) && userComm.equals(delComm)) || (!userComm.equals(recommComm) && !userComm.equals(delComm)))
            {
                toSum = 0.0;
            }
            else if(!userComm.equals(recommComm) && userComm.equals(delComm))
            {
                toSum = 1.0;
            }
            else
            {
                toSum = -1.0;
            }
            
            int newIdx = userComm*communities.getNumCommunities() + recommComm - userComm - ((userComm < recommComm)? 1 : 0);
            int delIdx = userComm*communities.getNumCommunities() + delComm - userComm - ((userComm < delComm)? 1 : 0);
           
            DoubleStream stream = IntStream.range(0, matrix.size()).mapToDouble(index -> 
            {
                if(index == newIdx && !userComm.equals(recommComm))
                {
                    atom.addAndGet(1.0);
                    return matrix.get(index) + 1.0;
                }
                else if(index == delIdx && !delComm.equals(userComm))
                {
                    atom.addAndGet(-1.0);
                    return matrix.get(index) - 1.0;
                }
                else
                {
                    return matrix.get(index);
                }
            });
            double giniValue = this.numWeak + toSum + 1.0 - gini.compute(stream.boxed(), true, communities.getNumCommunities()*(communities.getNumCommunities()-1), atom.doubleValue());
            return giniValue;
        }
        else
        {
            int newIdx = 0;
            for(int i = 0; i < min(userComm, recommComm); ++i)
            {
                newIdx += communities.getNumCommunities() - i - 1;
            }
            newIdx += max(userComm, recommComm) - min(userComm, recommComm) - 1;
            
            int oldIdx = 0;
            for(int i = 0; i < min(userComm, delComm); ++i)
            {
                oldIdx += communities.getNumCommunities() - i - 1;
            }           
            
            oldIdx += max(userComm, delComm) - min(userComm, delComm) - 1;
            
            double newValue = this.matrix.get(newIdx);
            double oldValue = this.matrix.get(oldIdx);
            
            if(!userComm.equals(recommComm))
                this.matrix.set(newIdx, newValue+1);
            
            if(!userComm.equals(delComm))
                this.matrix.set(oldIdx, oldValue-1);
            
            double giniValue = 1.0 - gini.compute(matrix, true, communities.getNumCommunities()*(communities.getNumCommunities()-1)/2, this.sum);
            this.matrix.set(newIdx, newValue);
            this.matrix.set(oldIdx, oldValue);
            return giniValue;
        }
    }
    
    @Override
    protected double novAdd(U u, Tuple2od<U> itemValue, Tuple2od<U> compared) 
    {
        // TODO: complete
        return -1.0;
    }
    
    @Override
    protected double novDelete(U u, Tuple2od<U> itemValue, Tuple2od<U> compared) 
    {
        // TODO: complete
        return -1.0;
    }

    @Override
    protected void innerUpdate(U user, Tuple2od<U> updated, Tuple2od<U> old) 
    {
        U recomm = updated.v1;
        U del = old.v1;

        Integer userComm = communities.getCommunity(user);
        Integer recommComm = communities.getCommunity(recomm);
        Integer delComm = communities.getCommunity(del);
        GiniIndex gini = new GiniIndex();

        
        /// LO HE MODIFICADO AHORA: ERRATA!!!!!!
        if(communityGraph.isDirected())
        {
            double toSum;
            if((userComm.equals(recommComm) && userComm.equals(delComm)) || (!userComm.equals(recommComm) && !userComm.equals(delComm)))
            {
                toSum = 0.0;
            }
            else if(!userComm.equals(recommComm) && userComm.equals(delComm))
            {
                toSum = 1.0;
            }
            else
            {
                toSum = -1.0;
            }
            
            this.numWeak += toSum;
            int newIdx = userComm*communities.getNumCommunities() + recommComm - (userComm + 1);
            int oldIdx = userComm*communities.getNumCommunities() + delComm - (userComm + 1);
            if(!userComm.equals(recommComm))
            {
                matrix.set(newIdx, matrix.get(newIdx)+1);
                this.sum++;
            }
            if(!userComm.equals(delComm))
            {
                matrix.set(oldIdx, matrix.get(oldIdx)-1);
                this.sum--;
            }
            this.globalvalue = numWeak + 1.0 - gini.compute(matrix, true, communities.getNumCommunities()*(communities.getNumCommunities()-1), this.sum);

        }
        else
        {
            int newIdx = 0;
            for(int i = 0; i < min(userComm, recommComm); ++i)
            {
                newIdx += communities.getNumCommunities() - i - 1;
            }
            newIdx += max(userComm, recommComm) - min(userComm, recommComm) - 1;
            
            int oldIdx = 0;
            for(int i = 0; i < min(userComm, delComm); ++i)
            {
                oldIdx += communities.getNumCommunities() - i - 1;
            }
            
            if(!userComm.equals(recommComm))
                matrix.set(newIdx, matrix.get(newIdx)+1);
            
            if(!userComm.equals(delComm))
                matrix.set(oldIdx, matrix.get(oldIdx)-1);
            
            this.globalvalue = 1.0 - gini.compute(matrix, true, communities.getNumCommunities()*(communities.getNumCommunities()-1)/2, this.sum);
        }
        
    }

    @Override
    protected void computeGlobalValue() 
    {
        GiniIndex gini = new GiniIndex();

        super.computeGlobalValue();
        long vertexcount = communityGraph.getVertexCount();
        sum = communityGraph.getEdgeCount();
        this.numWeak = sum;
        if(communityGraph.isDirected())
        {
            for(int i = 0; i < vertexcount; ++i)
            {
                for(int j = 0; j < vertexcount; ++j)
                {
                    if(i != j)
                        matrix.add(communityGraph.getNumEdges(i, j)+0.0);
                }
            }
            this.globalvalue = 1.0 - gini.compute(matrix, true, communities.getNumCommunities()*(communities.getNumCommunities()-1), this.sum);
        }
        else
        {
            for(int i = 0; i < vertexcount; ++i)
                for(int j = 0; j < i; ++j)
                    matrix.add(communityGraph.getNumEdges(i, j)+0.0);
            this.globalvalue = this.numWeak + 1.0 - gini.compute(matrix, true, communities.getNumCommunities()*(communities.getNumCommunities()-1)/2, this.sum);
        }

        
        


    }
}
