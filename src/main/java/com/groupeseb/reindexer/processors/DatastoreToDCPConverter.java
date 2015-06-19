package com.groupeseb.reindexer.processors;


import org.springframework.batch.item.ItemProcessor;

import java.util.Map;

public class DatastoreToDCPConverter implements ItemProcessor<Map, DCPIdentifier> {
    @Override
    public DCPIdentifier process(Map item) throws Exception {
        DCPIdentifier identifier = new DCPIdentifier();

        Map<String, Object> fid = ((Map) item.get("fid"));
        identifier.setFunctionalId((String) fid.get("functionalId"));
        identifier.setVersion((String) fid.get("version"));
        identifier.setSourceSystem((String) ((Map) fid.get("sourceSystem")).get("_id"));

        return identifier;
    }
}
