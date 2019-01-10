/* 
 * Copyright (C) 2015 Information Retrieval Group at Universidad Autónoma
 * de Madrid, http://ir.ii.uam.es
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import static java.lang.Math.min;
import java.util.ArrayList;
import static java.util.Comparator.comparingDouble;
import java.util.List;
import java.util.function.IntPredicate;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;
import org.ranksys.core.fast.FastRecommendation;
import org.ranksys.core.index.fast.FastItemIndex;
import org.ranksys.core.index.fast.FastUserIndex;
import org.ranksys.core.util.topn.IntDoubleTopN;
import org.ranksys.core.util.tuples.Tuple2id;
import static org.ranksys.core.util.tuples.Tuples.tuple;
import org.ranksys.recommenders.fast.AbstractFastRecommender;
import org.ranksys.recommenders.mf.Factorization;

/**
 * Matrix factorization recommender. Scores are calculated as the inner product of user and item vectors.
 * Returns the outcome on inverted order.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @author Saúl Vargas (saul.vargas@uam.es)
 *
 * @param <U> type of the users
 * @param <I> type of the items
 */
public class InverseMFRecommender<U, I> extends AbstractFastRecommender<U, I> {

    /**
     * Matrix factorization results.
     */
    private final Factorization<U, I> factorization;

    /**
     * Constructor.
     *
     * @param uIndex fast user index
     * @param iIndex fast item index
     * @param factorization matrix factorization
     */
    public InverseMFRecommender(FastUserIndex<U> uIndex, FastItemIndex<I> iIndex, Factorization<U, I> factorization) {
        super(uIndex, iIndex);
        this.factorization = factorization;
    }

    @Override
    public FastRecommendation getRecommendation(int uidx, int maxLength, IntPredicate filter) {
        DoubleMatrix1D pu;

        pu = factorization.getUserVector(uidx2user(uidx));
        if (pu == null) {
            return new FastRecommendation(uidx, new ArrayList<>());
        }

        IntDoubleTopN topN = new IntDoubleTopN(min(maxLength, factorization.numItems()));

        DoubleMatrix1D r = factorization.getItemMatrix().zMult(pu, null);
        for (int iidx = 0; iidx < r.size(); iidx++) {
            if (filter.test(iidx)) {
                topN.add(iidx, r.getQuick(iidx));
            }
        }

        topN.sort();

        List<Tuple2id> items = topN.reverseStream()
                .collect(toList());

        return new FastRecommendation(uidx, items);
    }

    @Override
    public FastRecommendation getRecommendation(int uidx, IntStream candidates) {
        DoubleMatrix1D pu;

        pu = factorization.getUserVector(uidx2user(uidx));
        if (pu == null) {
            return new FastRecommendation(uidx, new ArrayList<>());
        }
        
        DenseDoubleMatrix2D q = factorization.getItemMatrix();
        
        List<Tuple2id> items = candidates
                .mapToObj(iidx -> tuple(iidx, q.viewRow(iidx).zDotProduct(pu)))
                .sorted(comparingDouble(Tuple2id::v2))
                .collect(toList());
        
        return new FastRecommendation(uidx, items);
    }

}