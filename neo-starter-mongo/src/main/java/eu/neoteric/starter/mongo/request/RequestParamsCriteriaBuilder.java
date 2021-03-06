package eu.neoteric.starter.mongo.request;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.neoteric.starter.mongo.request.processors.MongoRequestFieldProcessor;
import eu.neoteric.starter.mongo.request.processors.MongoRequestLogicalOperatorProcessor;
import eu.neoteric.starter.mongo.request.processors.MongoRequestObjectProcessor;
import eu.neoteric.starter.request.RequestObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class RequestParamsCriteriaBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(RequestParamsCriteriaBuilder.class);

    @Autowired
    private MongoRequestFieldProcessor mongoRequestFieldProcessor;

    @Autowired
    private MongoRequestLogicalOperatorProcessor mongoRequestLogicalOperatorProcessor;

    private List<MongoRequestObjectProcessor> requestObjectProcessors;

    public Criteria build(Map<RequestObject, Object> requestParams) {
        return build(Optional.empty(), requestParams, FieldMapper.of(Maps.newHashMap()));
    }

    public Criteria build(Map<RequestObject, Object> requestParams, FieldMapper fieldMapper) {
        return build(Optional.empty(), requestParams, fieldMapper);
    }

    public Criteria build(Criteria initialCriteria, Map<RequestObject, Object> requestParams) {
        return build(initialCriteria, requestParams, FieldMapper.of(Maps.newHashMap()));
    }

    public Criteria build(Criteria initialCriteria, Map<RequestObject, Object> requestParams, FieldMapper fieldMapper) {
        return build(Optional.of(initialCriteria), requestParams, fieldMapper);
    }

    private Criteria build(Optional<Criteria> initialCriteria, Map<RequestObject, Object> requestParams, FieldMapper fieldMapper) {
        List<Criteria> joinedCriteria = Lists.newArrayList();
        initialCriteria.ifPresent(criteria -> joinedCriteria.add(criteria));

        requestParams.forEach(((key, value) -> {
            if (!(value instanceof Map)) {
                throw new IllegalArgumentException("Root RequestObject expect Map as argument, but get: " + value);
            }
            List<Criteria> fieldCriteria = Stream.<MongoRequestObjectProcessor>of(mongoRequestLogicalOperatorProcessor, mongoRequestFieldProcessor)
                    .filter(mongoRequestObjectProcessor -> mongoRequestObjectProcessor.apply(key.getType()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Illegal Root type: " + key.getType()))
                    .build(key, (Map) value, fieldMapper);
            joinedCriteria.addAll(fieldCriteria);
        }));
        Criteria criteria;
        if (joinedCriteria.isEmpty()) {
            criteria = new Criteria();

        } else if (joinedCriteria.size() == 1) {
            criteria = joinedCriteria.get(0);

        } else {
            criteria = new Criteria().andOperator(joinedCriteria.stream().toArray(Criteria[]::new));
        }
        LOG.debug("Produced criteria: {}", criteria.getCriteriaObject()); // TODO nicely fails to print Criteria containing ZonedDateTime
        return criteria;
    }
}
