/**
 * 
 */
package org.k12.caliper.poc.parser;

import org.imsglobal.caliper.entities.LearningObjective;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author belen.rolandi
 *
 */
public class CaliperLearningObjectiveParser extends AbstractCaliperParser<LearningObjective> {

	@Override
	public LearningObjective parseCaliperObject(JsonNode object) {
		
		String id = !object.get("@id").isNull() ? object.get("@id").asText() : null;
		String name = !object.get("name").isNull() ? object.get("name").asText() : null;
		
		return LearningObjective.builder()
				.id(id)
				.name(name)
				.build();
	}

}
