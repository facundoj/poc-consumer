/**
 * 
 */
package org.k12.caliper.poc.parser;

import org.imsglobal.caliper.entities.response.SelectTextResponse;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author belen.rolandi
 *
 */
public class CaliperSelectTextResponseParser extends AbstractCaliperParser<SelectTextResponse> {

	@Override
	public SelectTextResponse parseCaliperObject(JsonNode object) {
		return SelectTextResponse.builder().id(this.getId(object))
				.actor(this.getActor(object))
				.assignable(this.getAssignable(object))
				.attempt(this.getAttempt(object))
				.startedAtTime(this.getDateProperty(object, "startedAtTime"))
				.endedAtTime(this.getDateProperty(object, "endedAtTime"))
				.duration(this.getDuration(object))
				.build();
	}

}
