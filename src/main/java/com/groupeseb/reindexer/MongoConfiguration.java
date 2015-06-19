package com.groupeseb.reindexer;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.net.UnknownHostException;

@Configuration
public class MongoConfiguration {
    @Value("${mongo.database.name}")
    private String databaseName;

    @Value("${mongo.database.host}")
    private String databaseHost;

    @Value("${mongo.database.port}")
    private Integer databasePort;

    public Mongo mongo() throws UnknownHostException {
        return new MongoClient(databaseHost, databasePort);
    }

    @Bean
    public MongoTemplate mongoTemplate() throws UnknownHostException {
        return new MongoTemplate(mongo(), databaseName);
    }
}
