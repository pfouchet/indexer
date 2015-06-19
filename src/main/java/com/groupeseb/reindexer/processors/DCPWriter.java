package com.groupeseb.reindexer.processors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.http.Body;
import retrofit.http.PUT;

import java.util.List;

@Slf4j
public class DCPWriter implements ItemWriter<DCPIdentifier> {
    private final DCP dcp;

    public DCPWriter(String apiURL, final String apiKey) {
        dcp = new RestAdapter.Builder()
                .setEndpoint(apiURL)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade requestFacade) {
                        requestFacade.addHeader("Accept", "application/json");
                        requestFacade.addHeader("ApiKey", apiKey);
                    }
                })
                .build().create(DCP.class);
    }

    @Override
    public void write(List<? extends DCPIdentifier> items) throws Exception {
        try {
            dcp.reindex(items);
        } catch (RetrofitError e) {
            log.error("Failed to index {} : {}", items.get(0), e);
        }

        log.info("{} reindexed.", items.get(0));
    }

    public interface DCP {
        @PUT("/admin/recipes")
        Object reindex(@Body List<? extends DCPIdentifier> recipes);
    }
}
