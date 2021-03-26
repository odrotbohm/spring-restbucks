/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.odrotbohm.restbucks.payment.web;

import net.minidev.json.JSONArray;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.restdocs.hypermedia.Link;
import org.springframework.restdocs.hypermedia.LinkExtractor;
import org.springframework.restdocs.operation.OperationResponse;

import com.jayway.jsonpath.JsonPath;

/**
 * @author Oliver Drotbohm
 */
enum JsonPathHalLinkExtractor implements LinkExtractor {

	INSTANCE;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.restdocs.hypermedia.LinkExtractor#extractLinks(org.springframework.restdocs.operation.OperationResponse)
	 */
	@Override
	public Map<String, List<Link>> extractLinks(OperationResponse response) throws IOException {

		var document = JsonPath.parse(response.getContentAsString());
		Map<String, Object> rawLinks = document.read("$._links");
		var result = new HashMap<String, List<org.springframework.restdocs.hypermedia.Link>>();

		for (Entry<String, Object> entry : rawLinks.entrySet()) {
			result.put(entry.getKey(), extractLinks(entry.getKey(), entry.getValue()));
		}

		return result;
	}

	private static List<org.springframework.restdocs.hypermedia.Link> extractLinks(String key, Object source) {

		List<?> collection = JSONArray.class.isInstance(source)
				? (JSONArray) source
				: List.of((Map<String, Object>) source);

		return collection.stream()
				.map(Map.class::cast)
				.map(it -> new org.springframework.restdocs.hypermedia.Link(key, (String) it.get("href"),
						(String) it.get("title")))
				.toList();
	}
}
