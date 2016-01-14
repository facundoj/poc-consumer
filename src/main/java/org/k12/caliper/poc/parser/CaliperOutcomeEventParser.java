/**
 * 
 */
package org.k12.caliper.poc.parser;

import org.imsglobal.caliper.entities.Entity;
import org.imsglobal.caliper.entities.Generatable;
import org.imsglobal.caliper.entities.Targetable;
import org.imsglobal.caliper.entities.foaf.Agent;
import org.imsglobal.caliper.events.OutcomeEvent;
import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author belen.rolandi
 *
 */
public class CaliperOutcomeEventParser extends AbstractCaliperParser<OutcomeEvent> {

	@Override
	public OutcomeEvent parseCaliperObject(JsonNode object) {
		
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
		
		JsonNode targetNode = object.get("target");
		parser = !this.isNullObject(targetNode) ? CaliperParserFactory.getParser(targetNode.get("@type")
				.asText()) : null;
		Targetable target = parser != null ? (Targetable) parser
				.parseCaliperObject(targetNode) : null;
		
		return OutcomeEvent.builder()
	            .actor(actor)
	            .action(action)
	            .object(eventObject)
	            .generated(generated)
	            .eventTime(eventTime)
	            .target(target)
	            .build();
}

}
