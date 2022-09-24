package gov.nih.nci.bento_ri.model;

import gov.nih.nci.bento.model.AbstractPublicESDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component("AbstractPublicESDataFetcher")
public class PublicESDataFetcher extends AbstractPublicESDataFetcher {
    private static final Logger logger = LogManager.getLogger(PublicESDataFetcher.class);

    @Override
    public RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("publicGlobalSearch", env -> {
                            Map<String, Object> args = env.getArguments();
                            return globalSearch(args);
                        })
                )
                .build();
    }
}
