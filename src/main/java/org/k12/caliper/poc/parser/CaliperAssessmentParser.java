/**
 * 
 */
package org.k12.caliper.poc.parser;

import org.imsglobal.caliper.entities.assessment.Assessment;
import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author belen.rolandi
 *
 */
public class CaliperAssessmentParser extends AbstractCaliperParser<Assessment> {

	
	@Override
	public Assessment parseCaliperObject(JsonNode object) {
		
		String id = !this.isNullObject(object.get("@id")) ? object.get("@id")
				.asText() : null;
		String name = !this.isNullObject(object.get("name")) ? object.get(
				"name").asText() : null;
		DateTime dateCreated = !this.isNullObject(object.get("dateCreated")) ? this
				.formatDateTime(object.get("dateCreated").asText()) : null;
		DateTime datePublished = !this
				.isNullObject(object.get("datePublished")) ? this
				.formatDateTime(object.get("datePublished").asText()) : null;
		String version = !this.isNullObject(object.get("version")) ? object
				.get("version").asText() : null;
		DateTime dateToActivate = !this.isNullObject(object
				.get("dateToActivate")) ? this.formatDateTime(object.get(
				"dateToActivate").asText()) : null;
		DateTime dateToShow = !this.isNullObject(object.get("dateToShow")) ? this
				.formatDateTime(object.get("dateToShow").asText()) : null;
		DateTime dateToStartOn = !this
				.isNullObject(object.get("dateToStartOn")) ? this
				.formatDateTime(object.get("dateToStartOn").asText()) : null;
		DateTime dateToSubmit = !this.isNullObject(object.get("dateToSubmit")) ? this
				.formatDateTime(object.get("dateToSubmit").asText()) : null;
		int maxAttempts = !this.isNullObject(object.get("maxAttempts")) ? object
				.get("maxAttempts").asInt() : 0;
		int maxSubmits = !this.isNullObject(object.get("maxSubmits")) ? object
				.get("maxSubmits").asInt() : 0;
		double maxScore = !this.isNullObject(object.get("maxScore")) ? object
				.get("maxScore").asDouble() : 0;
		DateTime dateModified = !this.isNullObject(object.get("dateModified")) ? this
				.formatDateTime(object.get("dateModified").asText()) : null;
				
        return Assessment.builder()
                .id(id)
                .name(name)
                .dateCreated(dateCreated)
                .datePublished(datePublished)
                .version(version)
                .dateToActivate(dateToActivate)
                .dateToShow(dateToShow)
                .dateToStartOn(dateToStartOn)
                .dateToSubmit(dateToSubmit)
                .maxAttempts(maxAttempts)
                .maxSubmits(maxSubmits)
                .maxScore(maxScore)
                .dateModified(dateModified)
                .build();
	}

}
