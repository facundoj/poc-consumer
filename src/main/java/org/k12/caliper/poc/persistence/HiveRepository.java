/**
 * 
 */
package org.k12.caliper.poc.persistence;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

/**
 * @author belen.rolandi
 *
 */
public class HiveRepository {

	private static final String TABLE = "student_performance";
	
	public static void storeHive(JavaRDD<String> rdd, JavaSparkContext sc) {
        // sc is an existing JavaSparkContext.
        HiveContext hiveContext = getHiveContext(sc);
        
        DataFrame dataFrame = hiveContext.jsonRDD(rdd, getSchema());
        dataFrame.insertInto(TABLE);
	}
	
    private static StructType getSchema() {
        // The schema is encoded in a string
        String schemaString = "user type_event text";

        // Generate the schema based on the string of schema
        List<StructField> fields = new ArrayList<StructField>();
        for (String fieldName: schemaString.split(" ")) {
            fields.add(DataTypes.createStructField(fieldName, DataTypes.StringType, false));
        }

        return DataTypes.createStructType(fields);
    }
	
    private static HiveContext getHiveContext(JavaSparkContext sc) {
        return new org.apache.spark.sql.hive.HiveContext(sc.sc());
    }
}
