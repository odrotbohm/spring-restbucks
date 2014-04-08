/*
 * Copyright 2014 the original author or authors.
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
package org.springsource.restbucks;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * @author Oliver Gierke
 */
public class FooTests {

	@Test
	public void foo() {
		URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();

		URL[] urls = classLoader.getURLs();

		for (URL url : urls) {
			System.out.println(url);
		}

		System.out.println();
		System.out.println();

		List<Dependency> dependencies = new ArrayList<>();

		for (int i = 0; i < urls.length; i++) {

			Dependency dependency = new Dependency();
			dependency.name = urls[i].toString();
			dependency.index = i;

			dependencies.add(dependency);
		}

		Collections.sort(dependencies);

		for (Dependency dependency : dependencies) {
			System.out.println(dependency);
		}
	}

	static class Dependency implements Comparable<Dependency> {

		String name;
		int index;

		/* 
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("%s - %s", name, index);
		}

		/* 
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Dependency o) {
			return name.compareTo(o.name);
		}
	}
}
