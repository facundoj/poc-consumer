/**
 * 
 */
package org.k12.caliper.poc.parser;

import org.imsglobal.caliper.entities.DigitalResource;
import org.imsglobal.caliper.entities.foaf.Agent;
import org.imsglobal.caliper.entities.outcome.Result;
import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author belen.rolandi
 *
 */
public class CaliperResultParser extends AbstractCaliperParser<Result> {

	@Override
	public Result parseCaliperObject(JsonNode object) {

		String id = !object.get("@id").isNull() ? object.get("@id").asText() : null;
		DateTime dateCreated = !this.isNullObject(object.get("dateCreated")) ? this
				.formatDateTime(object.get("dateCreated").asText()) : null;
		double normalScore = !this.isNullObject(object.get("normalScore")) ? object
				.get("normalScore").asDouble() : 0;
		double penaltyScore = !this.isNullObject(object.get("penaltyScore")) ? object
				.get("penaltyScore").asDouble() : 0;
		double extraCreditScore = !this.isNullObject(object.get("extraCreditScore")) ? object
				.get("extraCreditScore").asDouble() : 0;
		double totalScore = !this.isNullObject(object.get("totalScore")) ? object
				.get("totalScore").asDouble() : 0;
		double curvedTotalScore = !this.isNullObject(object.get("curvedTotalScore")) ? object
				.get("curvedTotalScore").asDouble() : 0;
		double curveFactor = !this.isNullObject(object.get("curveFactor")) ? object
				.get("curveFactor").asDouble() : 0;
		
		AbstractCaliperParser<?> parser;
				
		JsonNode actorNode = object.get("actor");
		parser = !this.isNullObject(actorNode) ? CaliperParserFactory
				.getParser(actorNode.get("@type").asText()) : null;
		Agent actor = parser != null ? (Agent) parser
				.parseCaliperObject(actorNode) : null;

		JsonNode assignableNode = object.get("assignable");
		parser = !this.isNullObject(assignableNode) ? CaliperParserFactory.getParser(assignableNode.get("@type")
				.asText()) : null;
		DigitalResource assignable = parser != null ? (DigitalResource) parser
				.parseCaliperObject(assignableNode) : null;
				
        // Build result
        return Result.builder()
            .id(id)
            .assignable(assignable)
            .actor(actor)
            .dateCreated(dateCreated)
            .normalScore(normalScore)
            .penaltyScore(penaltyScore)
            .extraCreditScore(extraCreditScore)
            .totalScore(totalScore)
            .curvedTotalScore(curvedTotalScore)
            .curveFactor(curveFactor)
            .build();
	}

}
