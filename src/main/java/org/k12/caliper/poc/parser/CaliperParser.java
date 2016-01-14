package org.k12.caliper.poc.parser;

import com.fasterxml.jackson.databind.JsonNode;

public interface CaliperParser<T> {

	T parseCaliperObject(JsonNode object);

}


