/**
 * 
 */
package org.k12.caliper.poc.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.imsglobal.caliper.entities.LearningObjective;
import org.imsglobal.caliper.entities.assessment.AssessmentItem;
import org.imsglobal.caliper.entities.schemadotorg.CreativeWork;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;

/**
 * @author belen.rolandi
 *
 */
public class CaliperAssessmentItemParser extends AbstractCaliperParser<AssessmentItem> {

	@Override
	public AssessmentItem parseCaliperObject(JsonNode object) {

		String id = !this.isNullObject(object.get("@id")) ? object.get("@id")
				.asText() : null;
		String name = !this.isNullObject(object.get("name")) ? object.get(
				"name").asText() : null;
		String version = !this.isNullObject(object.get("version")) ? object.get(
				"version").asText() : null;

		int maxAttempts = !this.isNullObject(object.get("maxAttempts")) ? object
				.get("maxAttempts").asInt() : 0;
		int maxSubmits = !this.isNullObject(object.get("maxSubmits")) ? object
				.get("maxSubmits").asInt() : 0;
		int maxScore = !this.isNullObject(object.get("maxScore")) ? object
				.get("maxScore").asInt() : 0;
			
		JsonNode isPartOfNode = object.get("isPartOf");
		AbstractCaliperParser<?> parser = !isPartOfNode.isNull() ? CaliperParserFactory.getParser(isPartOfNode.get("@type").asText()) : null;
		CreativeWork isPartOf = parser != null ? (CreativeWork) parser.parseCaliperObject(isPartOfNode) : null;
		
		ArrayNode objectivesNodes = (ArrayNode) object
				.get("alignedLearningObjective");
		Iterator<JsonNode> objectiveIt = objectivesNodes.iterator();
		List<LearningObjective> objectives = new ArrayList<LearningObjective>();

		while (objectiveIt.hasNext()) {
			JsonNode objectiveNode = objectiveIt.next();
			CaliperLearningObjectiveParser objectiveParser = new CaliperLearningObjectiveParser();
			LearningObjective objective = objectiveParser
					.parseCaliperObject(objectiveNode);
			objectives.add(objective);
		}
		
		return AssessmentItem
				.builder()
				.id(id)
				.name(name)
				.learningObjectives(
						ImmutableList.<LearningObjective> copyOf(objectives))
				.isPartOf(isPartOf).version(version).maxAttempts(maxAttempts)
				.maxSubmits(maxSubmits).maxScore(maxScore).build();
	}

}
