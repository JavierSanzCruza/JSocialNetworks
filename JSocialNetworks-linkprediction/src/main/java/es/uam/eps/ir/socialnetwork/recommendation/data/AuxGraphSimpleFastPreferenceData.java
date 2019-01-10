/* 
 * Copyright (C) 2017 Information Retrieval Group at Universidad Autónoma
 * de Madrid, http://ir.ii.uam.es
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.data;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.FastGraphIndex;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.ranksys.core.preference.IdPref;
import org.ranksys.core.preference.fast.FastPointWisePreferenceData;
import org.ranksys.core.preference.fast.IdxPref;
import org.ranksys.core.preference.fast.StreamsAbstractFastPreferenceData;

/**
 * Simple implementation of FastPreferenceData backed by nested lists.
 *
 * @param <U> type of the users
 * 
 * @author Javier Sanz-Cruzado Puig
 * @author Saúl Vargas (saul.vargas@uam.es)
 */
public class AuxGraphSimpleFastPreferenceData<U> extends StreamsAbstractFastPreferenceData<U, U> implements FastPointWisePreferenceData<U, U>, Serializable 
{
    /**
     * The number of preferences.
     */
    private final int numPreferences;
    /**
     * A fast graph.
     */
    FastGraph<U> graph;

    /**
     * A function that transforms objects from IdxPref to IdPref<U>
     */
    Function<IdxPref, IdPref<U>> f = p -> new IdPref<>(graph.getIndex().idx2object(p.v1),p.v2);
    
    /**
     * Constructor with default IdxPref to IdPref converter.
     *
     * @param numPreferences number of total preferences.
     * @param fastGraph the graph.

     */
    protected AuxGraphSimpleFastPreferenceData(int numPreferences, FastGraph<U> fastGraph) {
        this(numPreferences, fastGraph, new FastGraphIndex<>(fastGraph),
                (Function<IdxPref, IdPref<U>> & Serializable) p -> new IdPref<>(fastGraph.getIndex().idx2object(p.v1),p.v2),
                (Function<IdxPref, IdPref<U>> & Serializable) p -> new IdPref<>(fastGraph.getIndex().idx2object(p.v1),p.v2));
    }

    /**
     * Constructor with custom IdxPref to IdPref converter.
     *
     * @param numPreferences number of total preferences
     * @param fastGraph the graph
     * @param index the graph index.

     * @param uPrefFun user IdxPref to IdPref converter
     * @param iPrefFun item IdxPref to IdPref converter
     */
    protected AuxGraphSimpleFastPreferenceData(int numPreferences, FastGraph<U> fastGraph, FastGraphIndex<U> index,
            Function<IdxPref, IdPref<U>> uPrefFun, Function<IdxPref, IdPref<U>> iPrefFun) {
        super(index, index, uPrefFun, iPrefFun);
        this.numPreferences = numPreferences;
        this.graph = fastGraph;
    }

    @Override
    public int numUsers(int iidx) 
    {
        return this.graph.getNeighborhood(iidx, EdgeOrientation.OUT).reduce(0,(a,b) -> a + 1 );
    }

    @Override
    public int numItems(int uidx) 
    {
        return this.graph.getNeighborhood(uidx, EdgeOrientation.IN).reduce(0,(a,b) -> a + 1 );
    }

    @Override
    public Stream<IdxPref> getUidxPreferences(int uidx) 
    {
        return this.graph.getNeighborhoodWeights(uidx, EdgeOrientation.OUT);
    }

    @Override
    public Stream<IdxPref> getIidxPreferences(int iidx) 
    {
        return this.graph.getNeighborhoodWeights(iidx, EdgeOrientation.IN);
    }

    @Override
    public int numPreferences() 
    {
        return numPreferences;
    }

    @Override
    public IntStream getUidxWithPreferences() 
    {
        return this.getAllUidx().filter(uidx -> this.graph.getNeighborhood(uidx, EdgeOrientation.OUT).count() > 0);
    }

    @Override
    public IntStream getIidxWithPreferences() {
        return this.getAllIidx().filter(iidx -> this.graph.getNeighborhood(iidx, EdgeOrientation.IN).count() > 0);

    }

    @Override
    public int numUsersWithPreferences() 
    {
        return this.getUidxWithPreferences().reduce(0, (a,b) -> a + 1);
    }

    @Override
    public int numItemsWithPreferences() 
    {
        return this.getIidxWithPreferences().reduce(0, (a,b) -> a + 1);
    }

    @Override
    public Optional<IdxPref> getPreference(int uidx, int iidx) 
    {
        double edgew = this.graph.getEdgeWeight(uidx, iidx);
        if(edgew == 0.0) return Optional.empty();
        return Optional.of(new IdxPref(iidx,edgew));
    }

    @Override
    public Optional<? extends IdPref<U>> getPreference(U u, U i) {
        Optional<? extends IdxPref> pref = getPreference(user2uidx(u), item2iidx(i));

        if (!pref.isPresent()) {
            return Optional.empty();
        } else {
            return Optional.of(uPrefFun.apply(pref.get()));
        }
    }

    /**
     * Loads the preferences from a file.
     * @param <U> Type of the users.
     * @param graph the graph.
     * @return the corresponding preference data.
     */
    public static <U> AuxGraphSimpleFastPreferenceData<U> load(FastGraph<U> graph)
    {
        int edgeCount = (int) graph.getEdgeCount();
        return new AuxGraphSimpleFastPreferenceData<>(edgeCount, graph);
    }
}