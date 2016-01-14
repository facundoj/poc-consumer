/**
 * 
 */
package org.k12.caliper.poc.parser;

import org.imsglobal.caliper.entities.agent.Person;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author belen.rolandi
 *
 */
public class CaliperPersonParser extends AbstractCaliperParser<Person> {

	private static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
	@Override
	public Person parseCaliperObject(JsonNode object) {
		
		DateTimeFormatter formatter = DateTimeFormat
				.forPattern(DATE_TIME_FORMAT);
		
		 String id = !object.get("@id").isNull() ? object.get("@id").asText() : null;
		 DateTime dateCreated = !object.get("dateCreated").isNull() ? formatter
					.parseDateTime(object.get("dateCreated").asText()) : null;
		 DateTime dateModified = !object.get("dateModified").isNull() ? formatter
					.parseDateTime(object.get("dateModified").asText()) : null;
 
	     return Person.builder()
	             .id(id)
	             .dateCreated(dateCreated)
	             .dateModified(dateModified)
	             .build();
	}

}
