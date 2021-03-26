/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.odrotbohm.restbucks;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.hypermedia.HypermediaDocumentation;
import org.springframework.restdocs.hypermedia.Link;
import org.springframework.restdocs.hypermedia.LinkExtractor;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.operation.ResponseCookie;
import org.springframework.restdocs.operation.preprocess.ContentModifier;
import org.springframework.restdocs.operation.preprocess.ContentModifyingOperationPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 * Helper class to ease the documentation of a certain hypermedia flow.
 *
 * @author Oliver Gierke
 */
@Value(staticConstructor = "of")
public class DocumentationFlow {

	public static DocumentationFlow NONE = new DocumentationFlow(null);

	@Nullable String name;

	/**
	 * Creates a documenting {@link ResultHandler} for a named step.
	 *
	 * @param step must not be {@literal null} or empty.
	 * @param snippets must not be {@literal null}.
	 * @return
	 */
	public ResultHandler document(String step, Snippet... snippets) {
		return document(step, true, snippets);
	}

	/**
	 * Creates a documenting {@link ResultHandler} for a named step without masking links.
	 *
	 * @param step must not be {@literal null}.
	 * @param snippets must not be {@literal null}.
	 * @return
	 */
	public ResultHandler documentUnmasked(String step, Snippet... snippets) {
		return document(step, false, snippets);
	}

	private ResultHandler document(String step, boolean maskUris, Snippet... snippets) {

		Assert.notNull(step, "Step name must not be null!");

		if (name == null) {
			return _ -> {};
		}

		var linkMasker = new ContentModifyingOperationPreprocessor(new LinkMasker("$.._links..['%s']", List.of(snippets)));

		OperationResponsePreprocessor preprocessResponse = maskUris ? preprocessResponse(linkMasker, maskLinks("…"))
				: preprocessResponse(linkMasker);

		return MockMvcRestDocumentation.document(name.concat("/").concat(step), preprocessResponse, snippets);
	}

	@RequiredArgsConstructor
	private static class LinkMasker implements ContentModifier {

		@RequiredArgsConstructor
		private static class ContentForwardingResponse implements OperationResponse {

			private final byte[] originalContent;

			@Override
			public HttpStatus getStatus() {
				return HttpStatus.OK;
			}

			@Override
			public HttpHeaders getHeaders() {
				return new HttpHeaders();
			}

			@Override
			public String getContentAsString() {
				return null;
			}

			@Override
			public byte[] getContent() {
				return originalContent;
			}

			@Override
			public Collection<ResponseCookie> getCookies() {
				return Collections.emptyList();
			}
		}

		private static final Method DESCRIPTORS_METHOD;

		static {

			DESCRIPTORS_METHOD = ReflectionUtils.findMethod(LinksSnippet.class, "getDescriptorsByRel");
			ReflectionUtils.makeAccessible(DESCRIPTORS_METHOD);
		}

		private final String jsonPathTemplate;
		private final Collection<Snippet> snippets;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.restdocs.operation.preprocess.ContentModifier#modifyContent(byte[], org.springframework.http.MediaType)
		 */
		@Override
		public byte[] modifyContent(byte[] originalContent, MediaType contentType) {

			var extractor = new ExceptionSuppressingLinkExtractor(HypermediaDocumentation.halLinks());
			var links = extractor.extractLinks(new ContentForwardingResponse(originalContent));

			if (links.isEmpty()) {
				return originalContent;
			}

			try (ByteArrayInputStream json = new ByteArrayInputStream(originalContent)) {

				DocumentContext context = JsonPath.parse(json);

				String result = snippets.stream()//
						.filter(it -> LinksSnippet.class.isInstance(it))//
						.map(it -> LinksSnippet.class.cast(it))//
						.flatMap(it -> getDocumentedLinkRelations(it))
						.flatMap(it -> links.keySet().stream().filter(rel -> !it.equals(rel)))//
						.map(it -> String.format(jsonPathTemplate, it))//
						.reduce(context, (t, u) -> t.delete(u), (_, right) -> right).jsonString();

				return result.getBytes();

			} catch (IOException o_O) {
				throw new RuntimeException(o_O);
			}
		}

		@SuppressWarnings("unchecked")
		private Stream<String> getDocumentedLinkRelations(LinksSnippet snippet) {

			Map<String, ?> descriptors = (Map<String, ?>) ReflectionUtils.invokeMethod(DESCRIPTORS_METHOD, snippet);

			return descriptors.keySet().stream();
		}
	}

	@RequiredArgsConstructor
	private static class ExceptionSuppressingLinkExtractor implements LinkExtractor {

		private final LinkExtractor delegate;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.restdocs.hypermedia.LinkExtractor#extractLinks(org.springframework.restdocs.operation.OperationResponse)
		 */
		@Override
		public Map<String, List<Link>> extractLinks(OperationResponse response) {

			try {
				return delegate.extractLinks(response);
			} catch (Exception e) {
				return Collections.emptyMap();
			}
		}
	}
}
