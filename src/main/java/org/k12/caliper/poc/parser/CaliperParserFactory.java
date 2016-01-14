package org.k12.caliper.poc.parser;

import org.imsglobal.caliper.entities.EntityType;
import org.imsglobal.caliper.entities.assignable.AssignableDigitalResourceType;
import org.imsglobal.caliper.entities.response.ResponseType;
import org.imsglobal.caliper.events.EventType;

public class CaliperParserFactory {

	public static AbstractCaliperParser<?> getParser(String type) {
		if (type.equals(EventType.ASSESSMENT.getValue())) {
			return new CaliperAssessmentEventParser();
		} else if (type.equals(EntityType.PERSON.getValue())) {
			return new CaliperPersonParser();
		} else if (type.equals(AssignableDigitalResourceType.ASSESSMENT
				.getValue())) {
			return new CaliperAssessmentParser();
		} else if (type.equals(AssignableDigitalResourceType.ASSESSMENT_ITEM
				.getValue())) {
			return new CaliperAssessmentItemParser();
		} else if (type.equals(EntityType.ATTEMPT.getValue())) {
			return new CaliperAttemptParser();
		} else if (type.equals(EntityType.RESULT.getValue())) {
			return new CaliperResultParser();
		} else if (type.equals(EventType.OUTCOME.getValue())) {
			return new CaliperOutcomeEventParser();
		} else if (type.equals(EventType.ASSESSMENT_ITEM.getValue())) {
			return new CaliperAssessmentItemEventParser();
		} else if (type.equals(EntityType.LEARNING_OBJECTIVE.getValue())) {
			return new CaliperLearningObjectiveParser();
		} else if (type.equals(ResponseType.FILLINBLANK.getValue())) {
			return new CaliperFillInBlankResponseParser();
		} else if (type.equals(ResponseType.MULTIPLECHOICE.getValue())) {
			return new CaliperMultipleChoiceResponseParser();
		} else if (type.equals(ResponseType.MULTIPLERESPONSE.getValue())) {
			return new CaliperMultipleResponseParser();
		} else if (type.equals(ResponseType.SELECTTEXT.getValue())) {
			return new CaliperSelectTextResponseParser();
		} else if (type.equals(ResponseType.TRUEFALSE.getValue())) {
			return new CaliperTrueFalseResponseParser();
		}

		return null;
	}
}
