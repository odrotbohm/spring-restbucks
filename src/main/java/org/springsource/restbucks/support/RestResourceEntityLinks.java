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

import java.util.Map;
import java.util.WeakHashMap;

import lombok.RequiredArgsConstructor;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.core.AbstractEntityLinks;
import org.springframework.hateoas.mvc.BasicLinkBuilder;

/**
 * @author Oliver Gierke
 */
@RequiredArgsConstructor
public class RestResourceEntityLinks extends AbstractEntityLinks {

	private final Map<Class<?>, RepositoryLinkMetadata> cache = new WeakHashMap<Class<?>, RepositoryLinkMetadata>();

	private final RepositoryLinkMetadataFactory factory;
	private final String baseUri;

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(Class<?> delimiter) {
		return factory.supports(delimiter);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.EntityLinks#linkTo(java.lang.Class)
	 */
	@Override
	public LinkBuilder linkFor(Class<?> entity) {
		return BasicLinkBuilder.linkToCurrentMapping().slash(baseUri)
				.slash(getMetadata(entity).getCollectionResourcePath());
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.EntityLinks#linkTo(java.lang.Class, java.lang.Object[])
	 */
	@Override
	public LinkBuilder linkFor(Class<?> entity, Object... parameters) {
		return linkFor(entity);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.EntityLinks#getLinkToCollectionResource(java.lang.Class)
	 */
	@Override
	public Link linkToCollectionResource(Class<?> entity) {

		BasicLinkBuilder builder = BasicLinkBuilder.linkToCurrentMapping().slash(baseUri);
		RepositoryLinkMetadata metadata = getMetadata(entity);

		return builder.slash(metadata.getCollectionResourcePath()).withRel(metadata.getCollectionResourceRel());
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.EntityLinks#getLinkToSingleResource(java.lang.Class, java.lang.Object)
	 */
	@Override
	public Link linkToSingleResource(Class<?> entity, Object id) {

		BasicLinkBuilder builder = BasicLinkBuilder.linkToCurrentMapping().slash(baseUri);
		RepositoryLinkMetadata metadata = getMetadata(entity);

		return builder.slash(metadata.getSingleResourcePath(id)).withRel(metadata.getSingleResourceRel());
	}

	private RepositoryLinkMetadata getMetadata(Class<?> entity) {

		RepositoryLinkMetadata metadata = this.cache.get(entity);

		if (metadata != null) {
			return metadata;
		}

		metadata = factory.getMetadataFor(entity);
		cache.put(entity, metadata);
		return metadata;
	}
}
