package org.vitrivr.cineast.core.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

public interface DBSelector {

  boolean open(String name);

  boolean close();

  /**
   * Finds the {@code k}-nearest neighbours of the given {@code vector} in {@code column} using the
   * provided distance function in {@code config}. {@code ScoreElementClass} defines the specific
   * type of {@link DistanceElement} to be created internally and returned by this method.
   *
   * @param k maximum number of results
   * @param vector query vector
   * @param column feature column to do the search
   * @param distanceElementClass class of the {@link DistanceElement} type
   * @param config query config
   * @param <T> type of the {@link DistanceElement}
   * @return a list of elements with their distance
   */
  <T extends DistanceElement> List<T> getNearestNeighbours(int k, float[] vector, String column,
      Class<T> distanceElementClass, ReadableQueryConfig config);

  /**
   * Performs a batched kNN-search with multiple query vectors. That is, the storage engine is tasked to perform the kNN search for each vector
   * in the provided list and returns the union of the results for every query.
   *
   * @param k The number k vectors to return per query.
   * @param vectors The list of vectors to use.
   * @param column The column to perform the kNN search on.
   * @param distanceElementClass class of the {@link DistanceElement} type
   * @param configs The query configuration, which may contain distance definitions or query-hints.
   * @param <T> The type T of the resulting <T> type of the {@link DistanceElement}.
   * @return List of results.
   */
  <T extends DistanceElement> List<T> getBatchedNearestNeighbours(int k, List<float[]> vectors, String column, Class<T> distanceElementClass, List<ReadableQueryConfig> configs);

  /**
   * Performs a combined kNN-search with multiple query vectors. That is, the storage engine is tasked to perform the kNN search for each vector and then
   * merge the partial result sets pairwise using the desired MergeOperation.
   *
   * @param k The number k vectors to return per query.
   * @param vectors The list of vectors to use.
   * @param column The column to perform the kNN search on.
   * @param distanceElementClass class of the {@link DistanceElement} type
   * @param configs The query configuration, which may contain distance definitions or query-hints.
   * @param merge
   * @param options
   * @param <T> The type T of the resulting <T> type of the {@link DistanceElement}.
   * @return List of results.
   */
  <T extends DistanceElement> List<T> getCombinedNearestNeighbours(int k, List<float[]> vectors, String column, Class<T> distanceElementClass, List<ReadableQueryConfig> configs, MergeOperation merge, Map<String,String> options);

  /**
   * Performs a combined kNN-search with multiple query vectors. That is, the storage engine is tasked to perform the kNN search for each vector and then
   * merge the partial result sets pairwise using the desired MergeOperation.
   *
   * @param k The number k vectors to return per query.
   * @param vectors The list of vectors to use.
   * @param column The column to perform the kNN search on.
   * @param distanceElementClass class of the {@link DistanceElement} type
   * @param configs The query configuration, which may contain distance definitions or query-hints.
   * @param merge
   * @param <T> The type T of the resulting <T> type of the {@link DistanceElement}.
   * @return List of results.
   */
  default <T extends DistanceElement> List<T> getCombinedNearestNeighbours(int k, List<float[]> vectors, String column, Class<T> distanceElementClass, List<ReadableQueryConfig> configs, MergeOperation merge) {
    return this.getCombinedNearestNeighbours(k, vectors, column, distanceElementClass, configs,merge, new HashMap<>(0));
  }

  List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector, String column, ReadableQueryConfig config);

  List<float[]> getFeatureVectors(String fieldName, String value, String vectorName);

  List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String value);

  List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String... values);

  List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, Iterable<String> values);

  /**
   * SELECT column from the table. Be careful with large entities
   */
  List<PrimitiveTypeProvider> getAll(String column);

  /**
   * SELECT * from
   */
  List<Map<String, PrimitiveTypeProvider>> getAll();

  boolean existsEntity(String name);

  /**
   * Get first k rows
   */
  List<Map<String, PrimitiveTypeProvider>> preview(int k);
}
