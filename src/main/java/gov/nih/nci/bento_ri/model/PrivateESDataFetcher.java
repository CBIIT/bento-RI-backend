package gov.nih.nci.bento_ri.model;

import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.model.AbstractPrivateESDataFetcher;
import gov.nih.nci.bento.model.search.MultipleRequests;
import gov.nih.nci.bento.model.search.filter.DefaultFilter;
import gov.nih.nci.bento.model.search.filter.FilterParam;
import gov.nih.nci.bento.model.search.mapper.TypeMapperImpl;
import gov.nih.nci.bento.model.search.mapper.TypeMapperService;
import gov.nih.nci.bento.model.search.query.QueryParam;
import gov.nih.nci.bento.model.search.yaml.YamlQueryFactory;
import gov.nih.nci.bento.service.ESService;
import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.client.Request;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component
public class PrivateESDataFetcher extends AbstractPrivateESDataFetcher {
    private static final Logger logger = LogManager.getLogger(PrivateESDataFetcher.class);
    private final YamlQueryFactory yamlQueryFactory;
    private final TypeMapperService typeMapper = new TypeMapperImpl();

    public PrivateESDataFetcher(ESService esService) {
        super(esService);
        yamlQueryFactory = new YamlQueryFactory(esService);
    }

    @Override
    public RuntimeWiring buildRuntimeWiring() throws IOException {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetchers(yamlQueryFactory.createYamlQueries(Const.ES_ACCESS_TYPE.PRIVATE))
                        .dataFetcher("numberOfPrograms", env -> getNodeCount(PROGRAMS_COUNT_END_POINT))
                        .dataFetcher("numberOfStudies", env -> getNodeCount(STUDIES_COUNT_END_POINT))
                        .dataFetcher("numberOfSubjects", env -> getNodeCount(SUBJECTS_COUNT_END_POINT))
                        .dataFetcher("numberOfSamples", env -> getNodeCount(SAMPLES_COUNT_END_POINT))
                        .dataFetcher("numberOfLabProcedures", env -> getNodeCount(LAB_PROCEDURE_COUNT_END_POINT))
                        .dataFetcher("numberOfFiles", env -> getNodeCount(FILES_COUNT_END_POINT))
                        .dataFetcher("idsLists", env -> idsLists())
                        .dataFetcher("programInfo", env -> programInfo())
                        .dataFetcher("programDetail", env -> {
                            Map<String, Object> args = env.getArguments();
                            return programDetail(args);
                        })
                        .dataFetcher("subjectDetail", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectDetail(args);
                        })
                        .dataFetcher("armDetail", env -> {
                            Map<String, Object> args = env.getArguments();
                            return armDetail(args);
                        })
                        .dataFetcher("samplesForSubjectId", env ->
                                samplesForSubjectId(createQueryParam(env)))
                )
                .build();
    }

    private Map<String, Object> programDetail(Map<String, Object> params) throws IOException {
        // Get filter parameter as a String
        final String PROGRAM_ID = (String) params.get("program_id");
        // Declare properties mappings
        final String[][] PROGRAMS_PROPERTIES = new String[][]{
                new String[]{"program_acronym", "program_code_kw"},
                new String[]{"program_id", "program_id_kw"},
                new String[]{"program_name", "program_name_kw"},
                new String[]{"program_full_description", "program_full_description"},
                new String[]{"institution_name", "institution_name"},
                new String[]{"program_external_url", "program_external_url"},
                new String[]{"num_subjects", "num_subjects"},
                new String[]{"num_files", "num_files"},
                new String[]{"num_samples", "num_samples"},
                new String[]{"num_lab_procedures", "num_lab_procedures"}
        };
        final String[][] STUDIES_PROPERTIES = new String[][]{
                new String[]{"study_acronym", "study_code"},
                new String[]{"study_name", "study_name"},
                new String[]{"study_full_description", "study_full_description"},
                new String[]{"study_type", "study_type"},
                new String[]{"study_info", "study_info"},
                new String[]{"num_subjects", "num_subjects"}
        };
        // Get program information use it to initialize the result
        List<Map<String, Object>> programsResultList = esService.collectPage(Map.of("program_id_kw", PROGRAM_ID),
                PROGRAMS_END_POINT, PROGRAMS_PROPERTIES);
        if (programsResultList.isEmpty()){
            return null;
        }
        Map<String, Object> programsResult = programsResultList.get(0);
        // Add study information array to the result as the property "studies"
        List<Map<String, Object>> studiesResult = esService.collectPage(Map.of("program_id_kw", PROGRAM_ID),
                STUDIES_END_POINT, STUDIES_PROPERTIES);
        programsResult.put("studies", studiesResult);
        // Add diagnoses aggregations to the result as the property "diagnoses"
        Map<String, Object> diagnosesParams = Map.of("program_id", PROGRAM_ID);
        List<Map<String, Object>> diagnosesGroupCounts = esService.getFilteredGroupCount(diagnosesParams,
                SUBJECTS_END_POINT, "diagnoses");
        programsResult.put("diagnoses", diagnosesGroupCounts);
        // Add list of unique diagnoses from aggregations to the result as the property "disease_subtypes"
        HashSet<String> diseaseSubtypes = new HashSet<>();
        diagnosesGroupCounts.forEach((Map<String, Object> groupCount) -> {
            diseaseSubtypes.add((String) groupCount.get("group"));
        });
        programsResult.put("disease_subtypes", diseaseSubtypes);
        // Return result
        return programsResult;
    }

    private Map<String, Object> subjectDetail(Map<String, Object> params) throws IOException {
        final String SUBJECT_ID = (String) params.get("subject_id");
        final String[][] SUBJECT_PROPERTIES = new String[][]{
                new String[]{"subject_id", "subject_ids"},
                new String[]{"program_acronym", "programs"},
                new String[]{"program_id", "program_id"},
                new String[]{"study_acronym", "study_acronym"},
                new String[]{"study_name", "study_name"},
                new String[]{"gender", "gender"},
                new String[]{"race", "race"},
                new String[]{"ethnicity", "ethnicity"},
                new String[]{"age_at_index", "age_at_index"},
                new String[]{"menopause_status", "meno_status"},
                new String[]{"vital_status", "vital_status"},
                new String[]{"cause_of_death", "cause_of_death"},
                new String[]{"disease_type", "disease_type"},
                new String[]{"disease_subtype", "diagnoses"},
                new String[]{"tumor_grade", "tumor_grades"},
                new String[]{"tumor_largest_dimension_diameter", "tumor_largest_dimension_diameter"},
                new String[]{"er_status", "er_status"},
                new String[]{"pr_status", "pr_status"},
                new String[]{"nuclear_grade", "nuclear_grade"},
                new String[]{"recurrence_score", "recurrence_score"},
                new String[]{"primary_surgical_procedure", "primary_surgical_procedure"},
                new String[]{"chemotherapy_regimen_group", "chemotherapy_regimen_group"},
                new String[]{"chemotherapy_regimen", "chemotherapy_regimen"},
                new String[]{"endocrine_therapy_type", "endocrine_therapy_type"},
                new String[]{"dfs_event_indicator", "dfs_event_indicator"},
                new String[]{"recurrence_free_indicator", "recurrence_free_indicator"},
                new String[]{"distant_recurrence_indicator", "distant_recurrence_indicator"},
                new String[]{"dfs_event_type", "dfs_event_type"},
                new String[]{"first_recurrence_type", "first_recurrence_type"},
                new String[]{"days_to_progression", "days_to_progression"},
                new String[]{"days_to_recurrence", "days_to_recurrence"},
                new String[]{"test_name", "test_name"},
                new String[]{"num_samples", "num_samples"},
                new String[]{"num_lab_procedures", "num_lab_procedures"}
        };
        final String[][] FILES_PROPERTIES = new String[][]{
                new String[]{"subject_id", "subject_ids"},
                new String[]{"file_name", "file_names"},
                new String[]{"file_type", "file_type"},
                new String[]{"association", "association"},
                new String[]{"file_description", "file_description"},
                new String[]{"file_format", "file_format"},
                new String[]{"file_size", "file_size"},
                new String[]{"file_id", "file_ids"},
                new String[]{"md5sum", "md5sum"}
        };
        final String[][] SAMPLES_PROPERTIES = new String[][]{
                new String[]{"sample_id", "sample_ids"},
                new String[]{"sample_anatomic_site", "sample_anatomic_site"},
                new String[]{"composition", "composition"},
                new String[]{"method_of_sample_procurement", "sample_procurement_method"},
                new String[]{"tissue_type", "tissue_type"},
                new String[]{"sample_type", "sample_type"}
        };
        Map<String, Object> filterParam = Map.of("subject_ids", SUBJECT_ID);
        // Get subject details and initialize result
        List<Map<String, Object>> subjectsResultList = esService.collectPage(filterParam, SUBJECTS_END_POINT,
                SUBJECT_PROPERTIES);
        if (subjectsResultList.isEmpty()){
            return null;
        }
        Map<String, Object> subjectsResult = subjectsResultList.get(0);
        // Add subject files array to the result
        List<Map<String, Object>> filesResultList = esService.collectPage(filterParam, FILES_END_POINT,
                FILES_PROPERTIES);
        subjectsResult.put("files", filesResultList);
        // Add subject samples array to the result
        List<Map<String, Object>> samplesResultList = esService.collectPage(filterParam, SAMPLES_END_POINT,
                SAMPLES_PROPERTIES);
        subjectsResult.put("samples", samplesResultList);
        return subjectsResult;
    }


    private Map<String, Object> armDetail(Map<String, Object> params) throws IOException {
        final String STUDY_ACRONYM = (String) params.get("study_acronym");
        final String[][] STUDY_PROPERTIES = new String[][]{
                new String[]{"study_acronym", "study_code"},
                new String[]{"study_name", "study_name"},
                new String[]{"study_type", "study_type"},
                new String[]{"study_full_description", "study_full_description"},
                new String[]{"study_info", "study_info"},
                new String[]{"num_subjects", "num_subjects"},
                new String[]{"num_files", "num_files"},
                new String[]{"num_samples", "num_samples"},
                new String[]{"num_lab_procedures", "num_laboratory_procedures"},
        };
        final String[][] FILES_PROPERTIES = new String[][]{
                new String[]{"file_name", "file_names"},
                new String[]{"file_type", "file_type"},
                new String[]{"association", "association"},
                new String[]{"file_description", "file_description"},
                new String[]{"file_format", "file_format"},
                new String[]{"file_size", "file_size"},
                new String[]{"file_id", "file_ids"},
                new String[]{"md5sum", "md5sum"}
        };
        //Get arm (study) details and initialize result
        List<Map<String, Object>> studyResultList = esService.collectPage(Map.of("study_code_kw", STUDY_ACRONYM),
                STUDIES_END_POINT, STUDY_PROPERTIES);
        if (studyResultList.isEmpty()){
            return null;
        }
        Map<String, Object> studyResult = studyResultList.get(0);
        //Add arm diagnoses group counts to result
        List<Map<String, Object>> diagnosesGroupCounts = esService.getFilteredGroupCount(Map.of("study_acronym", STUDY_ACRONYM),
                SUBJECTS_END_POINT, "diagnoses");
        studyResult.put("diagnoses", diagnosesGroupCounts);
        //Add arm files array to result
        List<Map<String, Object>> filesResultList = esService.collectPage(
                Map.of("study_acronym", STUDY_ACRONYM, "association", "study"), FILES_END_POINT,
                FILES_PROPERTIES);
        studyResult.put("files", filesResultList);
        return studyResult;
    }

    private Map<String, String[]> idsLists() throws IOException {
        /*
        Index properties map definition template:
        Map.of(
            <ES Index Endpoint>, new String[][]{
                    new String[]{<Return Label>, <ES Index property name>}
            },
        */
        Map<String, String[][]> indexProperties = Map.of(
            SUBJECT_IDS_END_POINT, new String[][]{
                    new String[]{"subjectIds", "subject_id"}
            },
            SAMPLES_END_POINT, new String[][]{
                    new String[]{"sampleIds", "sample_ids"}
            },
            FILES_END_POINT, new String[][]{
                    new String[]{"fileIds", "file_ids"},
                    new String[]{"fileNames", "file_names"}
            }
        );
        //Generic Query
        Map<String, Object> query = esService.buildListQuery();
        //Results Map
        Map<String, String[]> results = new HashMap<>();
        //Iterate through each index properties map and make a request to each endpoint then format the results as
        // String arrays
        for (String endpoint: indexProperties.keySet()){
            Request request = new Request("GET", endpoint);
            String[][] properties = indexProperties.get(endpoint);
            List<Map<String, Object>> result = esService.collectPage(request, query, properties, ESService.MAX_ES_SIZE,
                    0);
            Map<String, List<String>> indexResults = new HashMap<>();
            Arrays.asList(properties).forEach(x -> indexResults.put(x[0], new ArrayList<>()));
            for(Map<String, Object> resultElement: result){
                for(String key: indexResults.keySet()){
                    indexResults.get(key).add((String) resultElement.get(key));
                }
            }
            for(String key: indexResults.keySet()){
                results.put(key, indexResults.get(key).toArray(new String[indexResults.size()]));
            }
        }
        return results;
    }

    private List<Map<String, Object>> programInfo() throws IOException {
        /*
            Properties definition template:
            String[][] properties = new String[][]{
                new String[]{ <Return Label>, <ES Index property name>}
            };
        */
        String[][] properties = new String[][]{
            new String[]{"program_acronym", "program_code_kw"},
            new String[]{"program_id", "program_id_kw"},
            new String[]{"program_name", "program_name_kw"},
            new String[]{"start_date", "start_date"},
            new String[]{"end_date", "end_date"},
            new String[]{"pubmed_id", "pubmed_id"},
            new String[]{"num_studies", "num_studies"},
            new String[]{"num_subjects", "num_subjects"}
        };
        //Generic Query
        Map<String, Object> query = esService.buildListQuery();
        Request request = new Request("GET", PROGRAMS_END_POINT);
        return esService.collectPage(request, query, properties, ESService.MAX_ES_SIZE, 0);
    }


    private List<Map<String, Object>> samplesForSubjectId(QueryParam queryParam) throws IOException {
        logger.info("Private SamplesForSubjectId API requested");
        Set<String> returnTypes = queryParam.getReturnTypes();
        returnTypes.add(BENTO_FIELDS.SAMPLE_NESTED_FILE_INFO);

        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("samplesForSubjectId")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SAMPLES)
                                .source(new DefaultFilter(
                                        FilterParam.builder()
                                                .args(queryParam.getArgs())
                                                .selectedField("subject_id")
                                                .caseInsensitive(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getList(returnTypes)).build());

        Map<String, List<Map<String, Object>>> response = esService.elasticMultiSend(requests);
        List<Map<String, Object>> result = response.get("samplesForSubjectId");
        // Store customized sub-fields 'files'
        result.forEach(r-> {
            Object files = r.get(BENTO_FIELDS.SAMPLE_NESTED_FILE_INFO);
            r.put(BENTO_FIELDS.FILES, files);
        });
        return result;
    }

    private final static class BENTO_INDEX {
        private static final String SAMPLES = "samples";
    }

    private final static class BENTO_FIELDS {
        private static final String SAMPLE_NESTED_FILE_INFO = "file_info";
        private static final String FILES = "files";
    }
}
