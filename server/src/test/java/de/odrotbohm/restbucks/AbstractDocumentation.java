/*
 * Copyright 2015 the original author or authors.
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

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Oliver Gierke
 */
@ExtendWith(RestDocumentationExtension.class)
@Transactional
@SpringBootTest
public abstract class AbstractDocumentation {

	@Autowired WebApplicationContext context;

	protected MockMvc mockMvc;

	@BeforeEach
	protected void setUp(RestDocumentationContextProvider restDocumentation) {

		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context) //
				.apply(documentationConfiguration(restDocumentation)) //
				.build();
	}

	protected RestDocumentationResultHandler document(String name, Snippet... snippets) {
		return MockMvcRestDocumentation.document(name.concat("/{step}"), preprocessResponse(maskLinks("…")), snippets);
	}
}
