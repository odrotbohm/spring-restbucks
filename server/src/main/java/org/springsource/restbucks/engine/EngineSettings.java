/*
 * Copyright 2022 the original author or authors.
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
package org.springsource.restbucks.engine;

import java.time.Duration;

import lombok.Value;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration settings for {@link Engine}.
 *
 * @author Oliver Drotbohm
 */
@Value
@ConfigurationProperties("restbucks.engine")
class EngineSettings {

	/**
	 * The duration for how the {@link Engine} is supposed to process the Order.
	 */
	private final Duration processingTime;

	private final boolean failRandomly;

	/**
	 * @param processingTime must not be {@literal null}.
	 */
	public EngineSettings(@DefaultValue("2s") Duration processingTime, boolean failRandomly) {
		this.processingTime = processingTime;
		this.failRandomly = failRandomly;
	}
}
