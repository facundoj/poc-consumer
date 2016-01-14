/**
 * 
 */
package org.k12.caliper.poc.parser;

import org.imsglobal.caliper.entities.Entity;
import org.imsglobal.caliper.entities.Generatable;
import org.imsglobal.caliper.entities.foaf.Agent;
import org.imsglobal.caliper.events.AssessmentItemEvent;
import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author belen.rolandi
 *
 */
public class CaliperAssessmentItemEventParser extends AbstractCaliperParser<AssessmentItemEvent> {

	@Override
	public AssessmentItemEvent parseCaliperObject(JsonNode object) {
	
		String action = !this.isNullObject(object.get("action")) ? object.get("action").asText() : null;
		DateTime eventTime = !this.isNullObject(object.get("eventTime")) ? this
				.formatDateTime(object.get("eventTime").asText()) : null;

		AbstractCaliperParser<?> parser;
				
		JsonNode actorNode = object.get("actor");
		parser = !this.isNullObject(actorNode) ? CaliperParserFactory
				.getParser(actorNode.get("@type").asText()) : null;
		Agent actor = parser != null ? (Agent) parser
				.parseCaliperObject(actorNode) : null;

		JsonNode eventObjectNode = object.get("object");
		parser = !this.isNullObject(eventObjectNode) ? CaliperParserFactory.getParser(eventObjectNode.get("@type")
				.asText()) : null;
		Entity eventObject = parser != null ? (Entity) parser
				.parseCaliperObject(eventObjectNode) : null;
				
		JsonNode generatedNode = object.get("generated");
		parser = !this.isNullObject(generatedNode) ? CaliperParserFactory.getParser(generatedNode.get("@type")
				.asText()) : null;
		Generatable generated = parser != null ? (Generatable) parser
				.parseCaliperObject(generatedNode) : null;
		
		return AssessmentItemEvent.builder()
	            .actor(actor)
	            .action(action)
	            .object(eventObject)
	            .generated(generated)
	            .eventTime(eventTime)
	            .build();
	}

}
