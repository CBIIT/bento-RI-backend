package gov.nih.nci.bento_ri.model;

import com.google.gson.JsonObject;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.model.AbstractESDataFetcher;
import gov.nih.nci.bento.model.search.yaml.YamlQueryFactory;
import gov.nih.nci.bento.service.ESService;
import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.Request;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component
public class PublicESDataFetcher extends AbstractESDataFetcher {
    private static final Logger logger = LogManager.getLogger(PublicESDataFetcher.class);
    private final YamlQueryFactory yamlQueryFactory;

    public PublicESDataFetcher(ESService esService) {
        super(esService);
        yamlQueryFactory = new YamlQueryFactory(esService);
    }

    @Override
    public RuntimeWiring buildRuntimeWiring() throws IOException {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("esVersion", env -> esVersion())
                        .dataFetchers(yamlQueryFactory.createYamlQueries(Const.ES_ACCESS_TYPE.PUBLIC))
                )
                .build();
    }

    private String esVersion() throws IOException {
        Request versionRequest = new Request("GET", "/");
        JsonObject response = esService.send(versionRequest);
        return response.getAsJsonObject("version").getAsJsonPrimitive("number").getAsString();
    }

}
