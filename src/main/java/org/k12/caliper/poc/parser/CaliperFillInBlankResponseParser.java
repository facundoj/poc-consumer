/**
 * 
 */
package org.k12.caliper.poc.parser;

import org.imsglobal.caliper.entities.response.FillinBlankResponse;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author belen.rolandi
 *
 */
public class CaliperFillInBlankResponseParser extends AbstractCaliperParser<FillinBlankResponse> {

	@Override
	public FillinBlankResponse parseCaliperObject(JsonNode object) {
		
		return FillinBlankResponse.builder()
		        .id(this.getId(object))
		        .actor(this.getActor(object))
		        .assignable(this.getAssignable(object))
		        .attempt(this.getAttempt(object))
				.startedAtTime(this.getDateProperty(object, "startedAtTime"))
				.endedAtTime(this.getDateProperty(object, "endedAtTime"))
				.duration(this.getDuration(object))
		        .build();
	}

}
