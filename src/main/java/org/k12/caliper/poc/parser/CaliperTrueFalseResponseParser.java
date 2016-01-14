/**
 * 
 */
package org.k12.caliper.poc.parser;

import org.imsglobal.caliper.entities.response.TrueFalseResponse;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author belen.rolandi
 *
 */
public class CaliperTrueFalseResponseParser extends AbstractCaliperParser<TrueFalseResponse> {


	@Override
	public TrueFalseResponse parseCaliperObject(JsonNode object) {

		return TrueFalseResponse.builder().id(this.getId(object))
				.actor(this.getActor(object))
				.assignable(this.getAssignable(object))
				.attempt(this.getAttempt(object))
				.startedAtTime(this.getDateProperty(object, "startedAtTime"))
				.endedAtTime(this.getDateProperty(object, "endedAtTime"))
				.duration(this.getDuration(object))
				.build();
	}

}
