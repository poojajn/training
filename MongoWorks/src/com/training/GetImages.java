package com.training;

import java.io.IOException;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

public class GetImages {

	public static void main(String[] args) throws IOException {
		MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB("exdb");
		String path = "/Users/tejaswini.m/Desktop/received/";
		GridFS gfs = new GridFS(db, "myimage");
		List<GridFSDBFile> list = gfs.find(new BasicDBObject());
		for(GridFSDBFile file:list) {
			file.writeTo(path + file.getFilename());
		}
		System.out.println("Restored images from mongoDB ");
		mongoClient.close();
	}

}
