package ch.unibas.cs.dbis.cineast.core.db;

import java.util.List;
import java.util.Map;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

public interface DBSelector {

	boolean open(String name);
	
	boolean close();
	/**
	 * @return a list of pairs containing an id and the distance to the query vector
	 */
	List<StringDoublePair> getNearestNeighbours(int k, float[] vector, String column, QueryConfig config);
	
	List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector, String column, QueryConfig config);
	
	List<float[]> getFeatureVectors(String fieldName, String value, String vectorName);
	
	List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String value);
	
}