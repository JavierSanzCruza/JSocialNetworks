/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.ir.socialnetwork.grid.metrics.vertex;

import es.uam.eps.ir.socialnetwork.grid.Grid;
import static es.uam.eps.ir.socialnetwork.grid.metrics.vertex.VertexMetricIdentifiers.*;
import es.uam.eps.ir.socialnetwork.metrics.GraphMetric;
import java.util.*;
import java.util.function.Supplier;

import es.uam.eps.ir.socialnetwork.metrics.VertexMetric;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import es.uam.eps.ir.socialnetwork.metrics.graph.aggregate.AggregateVertexMetric;
import java.util.function.Function;


/**
 * Class that translates from a grid to the different contact recommendation algorithns.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class VertexMetricSelector<U>
{   
     /**
     * Obtains the grid configurator for a metric.
     * @param metric the name of the metric.
     * @return the grid configurator for the metric if it exists, null otherwise.
     */
    public VertexMetricGridSearch<U> getGridSearch(String metric)
    {
        VertexMetricGridSearch<U> gridsearch;
        switch(metric)
        {
            case CLOSENESS:
                gridsearch = new ClosenessGridSearch();
                break;
            case BETWEENNESS:
                gridsearch = new NodeBetweennessGridSearch();
                break;
            case ECCENTRICITY:
                gridsearch = new EccentricityGridSearch();
                break;
            case DEGREE:
                gridsearch = new DegreeGridSearch();
                break;
            case HITS:
                gridsearch = new HITSGridSearch();
                break;
            case INVDEGREE:
                gridsearch = new InverseDegreeGridSearch();
                break;
            case LOCALCLUSTCOEF:
                gridsearch = new LocalClusteringCoefficientGridSearch();
                break;
            case LOCALRECIPRATE:
                gridsearch = new LocalReciprocityRateGridSearch();
                break;
            case PAGERANK:
                gridsearch = new PageRankGridSearch();
                break;
            // Default behavior
            default:
                gridsearch = null;
        }
        
        return gridsearch;
    }
        
    /**
     * Obtains the different variants of a given vertex metric depending on the 
     * parameters selected in a grid.
     * @param metric the name of the metric.
     * @param grid the grid containing the different parameters.
     * @param distCalc a distance calculator.
     * @return a map containing the different metric suppliers.
     */
    public Map<String, Supplier<VertexMetric<U>>> getMetrics(String metric, Grid grid, DistanceCalculator<U> distCalc)
    {
        VertexMetricGridSearch<U> gridsearch = this.getGridSearch(metric);
        if(gridsearch != null)
            return gridsearch.grid(grid, distCalc);
        return new HashMap<>();
    }
    
    /**
     * Obtains the different variants of a given vertex metric depending on the 
     * parameters selected in a grid.
     * @param metric the name of the metric.
     * @param grid the grid containing the different parameters.
     * @return a map containing the different metric suppliers, which work given a distance calculator
     */
    public Map<String, Function<DistanceCalculator<U>,VertexMetric<U>>> getMetrics(String metric, Grid grid)
    {
        VertexMetricGridSearch<U> gridsearch = this.getGridSearch(metric);
        if(gridsearch != null)
            return gridsearch.grid(grid);
        return new HashMap<>();
    }
    
    /**
     * Obtains the aggregate variants of a given vertex metric, given the parameters selected
     * in a grid.
     * @param metric the name of the metric.
     * @param grid the grid containing the different parameters.
     * @param distCalc a distance calculator.
     * @return a map, indexed by metric name, containing the different variants of the metric selected in the grid.
     */
    public Map<String, Supplier<GraphMetric<U>>> getGraphMetrics(String metric, Grid grid, DistanceCalculator<U> distCalc)
    {
        VertexMetricGridSearch<U> gridsearch = this.getGridSearch(metric);
        if(gridsearch != null)
        {
            Map<String, Supplier<VertexMetric<U>>> metrics = gridsearch.grid(grid, distCalc);
            Map<String, Supplier<GraphMetric<U>>> graphMetrics = new HashMap<>();
            metrics.entrySet().stream().forEach(entry -> 
            {
                String name = entry.getKey();
                graphMetrics.put(name, () -> new AggregateVertexMetric<>(entry.getValue().get()));
            });
            return graphMetrics;
        }
        return new HashMap<>();
    }
    
    /**
     * Obtains the aggregate variants of a given vertex metric, given the parameters selected
     * in a grid.
     * @param metric the name of the metric.
     * @param grid the grid containing the different parameters.
     * @return a map, indexed by metric name, containing the different variants of the metric selected in the grid.
     */
    public Map<String, Function<DistanceCalculator<U>,GraphMetric<U>>> getGraphMetrics(String metric, Grid grid)
    {
        VertexMetricGridSearch<U> gridsearch = this.getGridSearch(metric);
        if(gridsearch != null)
        {
            Map<String, Function<DistanceCalculator<U>,VertexMetric<U>>> metrics = gridsearch.grid(grid);
            Map<String, Function<DistanceCalculator<U>,GraphMetric<U>>> graphMetrics = new HashMap<>();
            metrics.entrySet().stream().forEach(entry -> 
            {
                String name = entry.getKey();
                graphMetrics.put(name, (distCalc) -> new AggregateVertexMetric<>(entry.getValue().apply(distCalc)));
            });
            return graphMetrics;
        }
        return new HashMap<>();
    }
}
