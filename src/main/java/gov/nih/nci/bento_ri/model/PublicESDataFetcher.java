package gov.nih.nci.bento_ri.model;

import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.model.AbstractPublicESDataFetcher;
import gov.nih.nci.bento.model.search.yaml.YamlQueryFactory;
import gov.nih.nci.bento.service.ESService;
import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component
public class PublicESDataFetcher extends AbstractPublicESDataFetcher {
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
                        .dataFetcher("numberOfPrograms", env -> getNodeCount(PROGRAMS_COUNT_END_POINT))
                        .dataFetcher("numberOfStudies", env -> getNodeCount(STUDIES_COUNT_END_POINT))
                        .dataFetcher("numberOfSubjects", env -> getNodeCount(SUBJECTS_COUNT_END_POINT))
                        .dataFetcher("numberOfSamples", env -> getNodeCount(SAMPLES_COUNT_END_POINT))
                        .dataFetcher("numberOfLabProcedures", env -> getNodeCount(LAB_PROCEDURE_COUNT_END_POINT))
                        .dataFetcher("numberOfFiles", env -> getNodeCount(FILES_COUNT_END_POINT))
                )
                .build();
    }

}
