package com.training.sparkworks;

import static org.apache.spark.sql.functions.avg;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.max;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import com.training.commons.SparkConnection;

public class SparkSQLDemo {
	public static void main(String[] args) {
		Logger.getLogger("org").setLevel(Level.ERROR);
		//optional
		Logger.getLogger("akka").setLevel(Level.ERROR);
		JavaSparkContext sparkContext = SparkConnection.getContext();
		SparkSession spSession = SparkConnection.getSession();
		Dataset<Row>  empDataFields = spSession.read().json("./data/customerData.json");
		empDataFields.show();
		//empDataFields.printSchema();
		//data queries
		//System.out.println("SELECT Demo");
		//empDataFields.select(col("name"), col("salary")).show();
		//data queries on selection (condition)
		//System.out.println("Select Demo with condition");
		//empDataFields.filter(col("gender").equalTo("male")).show();
		
		System.out.println("Aggregate - group by gender");
		//empDataFields.groupBy(col("gender")).count().show();
		
		 //group by dept id, average salary, max age
		Dataset<Row> summaryData = empDataFields.groupBy(col("deptid")).
				agg(
						avg(empDataFields.col("salary")),
						max(empDataFields.col("age"))
						);
		
		//summaryData.show();
		
		//---------------try with Bean class--------------------
		
		Department dept1 = new Department(100, "Developement");
		Department dept2 = new Department(200, "Testing");
		List<Department> deptList = new ArrayList<Department>();
		deptList.add(dept1);
		deptList.add(dept2);
		Dataset<Row> deptDataFields = spSession.createDataFrame(deptList, Department.class);
		System.out.println("Department contents are: ");
		deptDataFields.show();
		
		System.out.println("Join Employee with dept: ");
		Dataset<Row> empDeptJoin = empDataFields.join(deptDataFields, col("deptId").equalTo(col("departmentId")));
		
		empDeptJoin.show();
		
		System.out.println("--------join with aggregation(Dept and Employee)");
		
		empDataFields.filter(col("age").gt(30)).join(deptDataFields, col("deptid").equalTo(col("departmentID")))
		.groupBy(col("deptid"))
		.agg(
			avg(empDataFields.col("salary")),
			max(empDataFields.col("age"))
				).show();
		
		
		//to load the data from CSV
		
		Dataset<Row> autoData = spSession.read().option("header", "true").csv("data/auto-data.csv");
		autoData.show(5);
		
		//creating RDD with row objects
		Row row1 = RowFactory.create(1, "India", "Bengaluru");
		Row row2 = RowFactory.create(2,"USA","Reston");
		Row row3 = RowFactory.create(3,"UK","Steevenscreek");
		
		List<Row> rList = new ArrayList<Row>();
		rList.add(row1);
		rList.add(row2);
		rList.add(row3);
		JavaRDD<Row> rowRDD = sparkContext.parallelize(rList);
		StructType schema = DataTypes.createStructType(new StructField[] {
				DataTypes.createStructField("id", DataTypes.IntegerType, false),
				DataTypes.createStructField("country", DataTypes.StringType, false),
				DataTypes.createStructField("city", DataTypes.StringType, false)
				});
		
		Dataset<Row> tempDataFields = spSession.createDataFrame(rowRDD, schema);
		tempDataFields.show();
		
		//working with csv data, with sql statements
		//provided the data is kept in table like format
		//the persistance will be only till the end of program
		//meaning temporary
		
		autoData.createOrReplaceTempView("autos");
		System.out.println("Temp table contents: ");
		
		System.out.println("Showing all fields with hp greater than 200");
		spSession.sql("select * from autos where hp > 200").show();
		
		//to find make, maximum RPM from autos group by make
		System.out.println("***********to find make, maximum RPM from autos*******");
		//order by 2 ==> 2 stands for column number, here 2 is max(rpm) column
		spSession.sql("select make, max(rpm) from autos group by make order by 2 DESC").show();;
		
		
		//convert DataFrame to JavaRDD
		JavaRDD<Row> autoRDD = autoData.rdd().toJavaRDD();
		
		//reading the data from mysql DB
		//db(exdb), table(employee)
		Map<String, String> jdbcConnectParams = new HashMap<String, String>();
		jdbcConnectParams.put("url", "jdbc:mysql://localhost:3306/exdb");
		jdbcConnectParams.put("driver", "com.mysql.jdbc.Driver");
		jdbcConnectParams.put("dbtable", "employee");
		jdbcConnectParams.put("user", "root");
		jdbcConnectParams.put("password", "root@123");
		
		System.out.println("Create a datafram from a DB Table");
		Dataset<Row> sqlDataFields = spSession.read().format("jdbc").options(jdbcConnectParams).load();
		sqlDataFields.show();
		
		
		
		}
}
