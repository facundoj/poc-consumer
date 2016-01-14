/**
 * 
 */
package org.k12.caliper.poc.parser;

import java.util.concurrent.TimeUnit;

import org.imsglobal.caliper.entities.DigitalResource;
import org.imsglobal.caliper.entities.assignable.Attempt;
import org.imsglobal.caliper.entities.foaf.Agent;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author belen.rolandi
 *
 */
public abstract class AbstractCaliperParser<T> implements CaliperParser<T> {

	private static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	public abstract T parseCaliperObject(JsonNode object);

	protected DateTime formatDateTime(String datetime) {

		DateTimeFormatter formatter = DateTimeFormat
				.forPattern(DATE_TIME_FORMAT);
		return formatter.parseDateTime(datetime);
	}
	
	protected boolean isNullObject(JsonNode object) {
		return object == null || object.isNull();
	}
	
	protected String getId(JsonNode object) {
		return !this.isNullObject(object.get("@id")) ? object.get("@id")
				.asText() : null;
	}
	
	protected String getDuration(JsonNode object) {
		
		String duration = !this.isNullObject(object.get("duration")) ? object
				.get("duration").asText() : null;
		long millis = new Long(duration).longValue();
		return new Period(millis).toString();
	}
	
	protected DateTime getDateProperty(JsonNode object, String propertyName){
		return !this.isNullObject(object.get(propertyName)) ? this
				.formatDateTime(object.get(propertyName).asText()) : null;
	}
	
	protected Agent getActor(JsonNode object) {
		JsonNode actorNode = object.get("actor");
		AbstractCaliperParser<?> parser = !this.isNullObject(actorNode) ? CaliperParserFactory
				.getParser(actorNode.get("@type").asText()) : null;
		return parser != null ? (Agent) parser.parseCaliperObject(actorNode)
				: null;
	}
	
	protected DigitalResource getAssignable(JsonNode object) {
		JsonNode assignableNode = object.get("assignable");
		AbstractCaliperParser<?> parser = !assignableNode.isNull() ? CaliperParserFactory
				.getParser(assignableNode.get("@type").asText()) : null;
		return parser != null ? (DigitalResource) parser
				.parseCaliperObject(assignableNode) : null;
	}

	protected Attempt getAttempt(JsonNode object) {
		JsonNode attemptNode = object.get("attempt");
		AbstractCaliperParser<?> parser = !this.isNullObject(attemptNode) ? CaliperParserFactory
				.getParser(attemptNode.get("@type").asText()) : null;
		return parser != null ? (Attempt) parser
				.parseCaliperObject(attemptNode) : null;
	}
}
