package com.groupeseb.reindexer;

import com.groupeseb.reindexer.processors.DCPWriter;
import com.groupeseb.reindexer.processors.DatastoreToDCPConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.annotation.AfterWrite;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class StepConfiguration {
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private MongoTemplate template;

    @Value("${api.url}")
    private String apiURL;

    @Value("${api.key}")
    private String apiKey;

    @Value("${job.batch.size}")
    private Integer batchSize;

    @Value("${selection.query}")
    private String selectionQuery;

    @Bean
    public Step readDataFromMongoStep() {
        return stepBuilderFactory.get("readFromMongo")

                .chunk(10)
                .reader(mongoReader())
                .processor((ItemProcessor) new DatastoreToDCPConverter())

                .chunk(1)
                .writer(commonAPIWriter())

                .listener(new Report(template.count(new BasicQuery(selectionQuery), "recipe")))

                .build();
    }

    public ItemReader mongoReader() {
        MongoItemReader<Map> mongoReader = new MongoItemReader<Map>();

        mongoReader.setTemplate(template);
        mongoReader.setTargetType(Map.class);
        mongoReader.setFields("{ _id: 1, fid: 1, title: 1 }");
        mongoReader.setQuery(selectionQuery);
        mongoReader.setCollection("recipe");

        mongoReader.setPageSize(batchSize);

        return mongoReader;
    }

    public ItemWriter commonAPIWriter() {
        return new DCPWriter(apiURL, apiKey);
    }

    private class Report {
        private final Long totalDocument;
        private Long processedDocument = 0L;
        private Long duration = -1L;
        private Long lastTick = 0L;
        private DescriptiveStatistics stats = new DescriptiveStatistics(batchSize * 2);

        public Report(Long totalDocument) {
            this.totalDocument = totalDocument;
        }

        @AfterWrite
        public void afterWrite(List<Object> items) throws Exception {
            tick();
            log.info("[{}/{} - ETA {} min]", processedDocument, totalDocument, getRemainingTime(totalDocument - processedDocument, TimeUnit.MINUTES));
        }

        private void tick() {
            if (lastTick != 0) {
                duration = System.currentTimeMillis() - lastTick;
                stats.addValue(duration);
            }

            lastTick = System.currentTimeMillis();
            processedDocument += 1;
        }

        private Long getRemainingTime(Long remainingElement, TimeUnit unit) {
            Long mean = Math.round(stats.getMean() * remainingElement);

            if (TimeUnit.SECONDS == unit) {
                return TimeUnit.MILLISECONDS.toSeconds(mean);
            } else if (TimeUnit.MINUTES == unit) {
                return TimeUnit.MILLISECONDS.toMinutes(mean);
            }

            return mean;
        }
    }
}
