/*
 * Copyright 2012 the original author or authors.
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
package org.springsource.restbucks.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.repository.annotation.RestResource;
import org.springframework.plugin.core.Plugin;
import org.springframework.util.StringUtils;

/**
 * @author Oliver Gierke
 */
public class RepositoryLinkMetadataFactory implements Plugin<Class<?>> {

	private final Map<Class<?>, RepositoryInformation> infos;

	public RepositoryLinkMetadataFactory(Repositories repositories) {

		Map<Class<?>, RepositoryInformation> info = new HashMap<Class<?>, RepositoryInformation>();

		for (Class<?> domainClass : repositories) {
			info.put(domainClass, repositories.getRepositoryInformationFor(domainClass));
		}

		this.infos = Collections.unmodifiableMap(info);
	}

	public RepositoryLinkMetadata getMetadataFor(Class<?> domainClass) {

		RepositoryInformation information = infos.get(domainClass);
		return new RepositoryInterfaceBasedRepositoryLinkMetadata(information.getRepositoryInterface());
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(Class<?> delimiter) {
		return infos.containsKey(delimiter);
	}

	private static class RepositoryInterfaceBasedRepositoryLinkMetadata implements RepositoryLinkMetadata {

		private final Class<?> repositoryInterface;

		private String collectionResourcePath;
		private String collectionResourceRel;

		public RepositoryInterfaceBasedRepositoryLinkMetadata(Class<?> repositoryInterface) {
			this.repositoryInterface = repositoryInterface;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springsource.restbucks.support.RepositoryLinkMetadata#getCollectionResourcePath()
		 */
		@Override
		public String getCollectionResourcePath() {

			if (collectionResourcePath == null) {
				RestResource annotation = repositoryInterface.getAnnotation(RestResource.class);
				if (annotation == null) {
					String interfaceName = repositoryInterface.getSimpleName();
					this.collectionResourcePath = interfaceName.substring(0, interfaceName.indexOf("Repository"));
				} else {
					this.collectionResourcePath = annotation.path();
				}
			}

			return this.collectionResourcePath;
		}

		/* (non-Javadoc)
		 * @see org.springsource.restbucks.support.RepositoryLinkMetadata#getSingleResourcePath(java.lang.Object)
		 */
		@Override
		public String getSingleResourcePath(Object id) {
			return String.format("%s/%s", getCollectionResourcePath(), id);
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springsource.restbucks.support.RepositoryLinkMetadata#getCollectionResourceRel()
		 */
		@Override
		public String getCollectionResourceRel() {

			if (collectionResourceRel == null) {
				RestResource annotation = repositoryInterface.getAnnotation(RestResource.class);
				if (annotation == null) {
					this.collectionResourceRel = getCollectionResourcePath();
				} else {
					this.collectionResourceRel = annotation.rel();
				}
			}

			return this.collectionResourceRel;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springsource.restbucks.support.RepositoryLinkMetadata#getSingleResourceRel()
		 */
		@Override
		public String getSingleResourceRel() {
			return String.format("%s.%s", getCollectionResourceRel(), StringUtils.capitalize(getCollectionResourceRel()));
		}

	}
}
