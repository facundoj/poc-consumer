/**
 * 
 */
package org.k12.caliper.poc.parser;

import org.imsglobal.caliper.entities.DigitalResource;
import org.imsglobal.caliper.entities.assignable.Attempt;
import org.imsglobal.caliper.entities.foaf.Agent;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author belen.rolandi
 *
 */
public class CaliperAttemptParser extends AbstractCaliperParser<Attempt> {

	private static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
	@Override
	public Attempt parseCaliperObject(JsonNode object) {

		DateTimeFormatter formatter = DateTimeFormat
				.forPattern(DATE_TIME_FORMAT);
		
		String id = !object.get("@id").isNull() ? object.get("@id").asText() : null;
		DateTime dateCreated = !object.get("dateCreated").isNull() ? formatter
				.parseDateTime(object.get("dateCreated").asText()) : null;
		DateTime dateStarted = !object.get("startedAtTime").isNull() ? formatter
				.parseDateTime(object.get("startedAtTime").asText()) : null;
		DateTime dateEnded = !object.get("endedAtTime").isNull() ? formatter
				.parseDateTime(object.get("endedAtTime").asText()) : null;
		int count = !object.get("count").isNull() ? object.get("count").asInt() : 1;
		String duration = !object.get("duration").isNull() ? object.get("duration").asText() : null;
		
		CaliperParser<?> parser;
		
		JsonNode actorNode = object.get("actor");
		parser = !actorNode.isNull() ? CaliperParserFactory.getParser(actorNode.get("@type").asText()) : null;
		Agent actor = parser != null ? (Agent) parser.parseCaliperObject(actorNode) : null;
		
		JsonNode assignableNode = object.get("assignable");
		parser = !assignableNode.isNull() ? CaliperParserFactory.getParser(assignableNode.get("@type").asText()) : null;
		DigitalResource assignable = parser != null ? (DigitalResource) parser.parseCaliperObject(assignableNode) : null;
		
		return Attempt.builder()
	            .id(id)
	            .assignable(assignable)
	            .actor(actor)
	            .count(count)
	            .dateCreated(dateCreated)
	            .startedAtTime(dateStarted)
	            .endedAtTime(dateEnded)
	            .duration(duration)
	            .build();
	}

}
