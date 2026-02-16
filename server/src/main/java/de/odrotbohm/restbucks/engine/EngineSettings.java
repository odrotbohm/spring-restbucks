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
package de.odrotbohm.restbucks.engine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration settings for {@link Engine}.
 *
 * @author Oliver Drotbohm
 */
@Value
@Getter(value = AccessLevel.NONE)
@ConfigurationProperties("restbucks.engine")
class EngineSettings {

	/**
	 * The duration for how the {@link Engine} is supposed to process the Order.
	 */
	private final Duration processingTime;
	private final Duration maxProcessingTime;

	private final @Getter boolean failRandomly;

	/**
	 * @param processingTime must not be {@literal null}.
	 */
	public EngineSettings(Duration processingTime, Duration maxProcessingTime, boolean failRandomly) {

		this.processingTime = processingTime;
		this.maxProcessingTime = maxProcessingTime;
		this.failRandomly = failRandomly;
	}

	Duration getProcessingTime() {

		if (processingTime != null) {
			return processingTime;
		}

		if (maxProcessingTime != null) {

			long maxMs = maxProcessingTime.toMillis();
			long randomMs = maxMs > 0 ? ThreadLocalRandom.current().nextLong(0, maxMs) : 0;

			return Duration.ofMillis(randomMs);
		}

		return Duration.ofSeconds(2);
	}
}
