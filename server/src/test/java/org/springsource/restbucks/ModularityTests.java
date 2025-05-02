/*
 * Copyright 2022-2024 the original author or authors.
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
package org.springsource.restbucks;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesArchitectureRules.VerificationDepth;
import org.jmolecules.stereotype.api.Stereotype;
import org.jmolecules.stereotype.api.StereotypeFactory;
import org.jmolecules.stereotype.api.Stereotypes;
import org.jmolecules.stereotype.catalog.StereotypeCatalog;
import org.jmolecules.stereotype.catalog.StereotypeGroup;
import org.jmolecules.stereotype.catalog.support.CatalogSource;
import org.jmolecules.stereotype.catalog.support.JsonPathStereotypeCatalog;
import org.jmolecules.stereotype.reflection.ArchUnitStereotypeFactory;
import org.jmolecules.stereotype.reflection.ArchUnitStructureProvider;
import org.jmolecules.stereotype.reflection.ReflectionStereotypeFactory;
import org.jmolecules.stereotype.tooling.AsciiArtNodeHandler;
import org.jmolecules.stereotype.tooling.Grouped;
import org.jmolecules.stereotype.tooling.LabelProvider;
import org.jmolecules.stereotype.tooling.LabelUtils;
import org.jmolecules.stereotype.tooling.ProjectTree;
import org.jmolecules.stereotype.tooling.SpringLabelUtils;
import org.jmolecules.stereotype.tooling.StructureProvider;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.FormattableType;
import org.springframework.modulith.core.JavaPackage;
import org.springframework.modulith.core.NamedInterface;
import org.springframework.modulith.core.VerificationOptions;
import org.springframework.modulith.docs.Documenter;
import org.springframework.util.Assert;
import org.springsource.restbucks.ModularityTests.ApplicationModulesStructureProvider.NamedInterfacesGroupingProvider;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

/**
 * Verifying modulithic structure and creating documentation for {@link ApplicationModules}.
 *
 * @author Oliver Drotbohm
 */
class ModularityTests {

	ApplicationModules modules = ApplicationModules.of(Restbucks.class);

	@Test
	void verifiesArchitecture() {

		// System.out.println(modules);

		var hexagonal = JMoleculesArchitectureRules.ensureHexagonal(VerificationDepth.LENIENT);
		var options = VerificationOptions.defaults().withAdditionalVerifications(hexagonal);

		modules.verify(options);
	}

	@Test
	void createDocumentation() {
		new Documenter(modules).writeDocumentation();
	}

	@Test
	void renderSpringBootProject() {

		var source = CatalogSource.ofClassLoader(ModularityTests.class.getClassLoader());
		var catalog = new JsonPathStereotypeCatalog(source);
		var factory = new ReflectionStereotypeFactory(catalog);

		System.out.println(catalog);

		var structure = ArchUnitStructureProvider.asSinglePackage();
		var labelProvider = structure.getLabelProvider()
				.withTypeLabel(it -> LabelUtils.abbreviate(it.getName(), "org.springsource.restbucks"))
				.withMethodLabel((m, t) -> SpringLabelUtils.requestMappings(m.reflect(), t == null ? null : t.reflect()));

		var handler = new AsciiArtNodeHandler<>(labelProvider);

		var tree = new ProjectTree<>(new ArchUnitStereotypeFactory(factory), catalog, handler)
				.withStructureProvider(structure)
				.withGrouper("org.jmolecules.architecture")
				.withGrouper("org.jmolecules.ddd", "org.jmolecules.event", "spring", "jpa", "java");

		var classes = new ClassFileImporter()
				.withImportOption(new ImportOption.DoNotIncludeTests())
				.importPackages("org.springsource.restbucks");

		tree.process(classes.get(Restbucks.class).getPackage());

		// System.out.println(jsonHandler.toString());
		System.out.println(handler.getWriter().toString());
	}

	@Test
	void renderSpringModulithProject() {

		var source = CatalogSource.ofClassLoader(ModularityTests.class.getClassLoader());
		var catalog = new JsonPathStereotypeCatalog(source);
		var factory = new ReflectionStereotypeFactory(catalog);
		var adapter = new StereotypeFactoryAdapter(factory);

		// var structure = new DefaultApplicationModulesStructureProvider(modules, catalog);
		var structure = new NamedInterfacesGroupingProvider(modules, catalog);
		var handler = new AsciiArtNodeHandler<>(structure);

		// var jsonHandler = new HierarchicalNodeHandler<>(labelProvider,
		// (node, ni) -> node.withAttribute(HierarchicalNodeHandler.TEXT, labelProvider.getCustomLabel(ni))
		// .withAttribute("icon", "fa-named-interface"));

		var tree = new ProjectTree<>(adapter, catalog, handler)
				.withStructureProvider(structure)
				.withGrouper("org.jmolecules.architecture")
				.withGrouper("org.jmolecules.event", "spring", "java");

		tree.process(modules);

		// System.out.println(jsonHandler.toString());
		System.out.println(handler.getWriter().toString());
	}

	class StereotypeFactoryAdapter implements StereotypeFactory<JavaPackage, Class<?>, Method> {

		private final ReflectionStereotypeFactory delegate;

		/**
		 * Creates a new {@link StereotypeFactoryAdapter} for the given {@link ReflectionStereotypeFactory}.
		 *
		 * @param delegate must not be {@literal null}.
		 */
		StereotypeFactoryAdapter(ReflectionStereotypeFactory delegate) {

			Assert.notNull(delegate, "ReflectionStereotypeFactory must not be null!");

			this.delegate = delegate;
		}

		/*
		 * (non-Javadoc)
		 * @see org.jmolecules.stereotype.api.StereotypeFactory#fromPackage(java.lang.Object)
		 */
		@Override
		public Stereotypes fromPackage(JavaPackage pkg) {
			return new Stereotypes(Collections.emptySet());
		}

		/*
		 * (non-Javadoc)
		 * @see org.jmolecules.stereotype.api.StereotypeFactory#fromType(java.lang.Object)
		 */
		@Override
		public Stereotypes fromType(Class<?> type) {
			return delegate.fromType(type);
		}

		/*
		 * (non-Javadoc)
		 * @see org.jmolecules.stereotype.api.StereotypeFactory#fromMethod(java.lang.Object)
		 */
		@Override
		public Stereotypes fromMethod(Method method) {
			return delegate.fromMethod(method);
		}
	}

	static class NamedInterfaceNode {

		private static final NamedInterfaceNode INTERNAL = new NamedInterfaceNode(null);

		private final @Nullable NamedInterface namedInterface;

		/**
		 * Creates a new {@link NamedInterfaceNode} for the given {@link NamedInterface}.
		 *
		 * @param namedInterface can be {@literal null}.
		 */
		NamedInterfaceNode(@Nullable NamedInterface namedInterface) {
			this.namedInterface = namedInterface;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {

			if (namedInterface == null) {
				return "Internal";
			}

			return namedInterface.isUnnamed() ? "API" : namedInterface.getName();
		}

		static Collector<NamedInterface, ?, Map<NamedInterfaceNode, Collection<Class<?>>>> collector() {

			return Collectors.toMap(it -> new NamedInterfaceNode(it),
					it -> getClasses(it.asJavaClasses()),
					(l, r) -> r,
					LinkedHashMap::new);
		}

		static Collection<Class<?>> getClasses(Stream<JavaClass> namedInterface) {

			Supplier<Collection<Class<?>>> factory = () -> new ArrayList<>();

			return namedInterface.filter(it -> !it.getSimpleName().equals("package-info"))
					.map(JavaClass::reflect)
					.collect(Collectors.toCollection(factory));
		}
	}

	@RequiredArgsConstructor
	static abstract class ApplicationModulesStructureProvider
			implements StructureProvider<ApplicationModules, JavaPackage, Class<?>, Method>,
			LabelProvider<ApplicationModules, JavaPackage, Class<?>, Method, NamedInterfaceNode> {

		protected final ApplicationModules modules;
		private final StereotypeCatalog catalog;

		/*
		 * (non-Javadoc)
		 * @see org.jmolecules.stereotype.tooling.StructureProvider#extractPackages(java.lang.Object)
		 */
		@Override
		public Collection<JavaPackage> extractPackages(ApplicationModules application) {
			return application.stream()
					.map(ApplicationModule::getBasePackage)
					.toList();
		}

		/*
		 * (non-Javadoc)
		 * @see org.jmolecules.stereotype.tooling.StructureProvider#extractMethods(java.lang.Object)
		 */
		@Override
		public Collection<Method> extractMethods(Class<?> type) {
			return List.of(type.getDeclaredMethods());
		}

		/*
		 * (non-Javadoc)
		 * @see org.jmolecules.stereotype.tooling.LabelProvider#getApplicationLabel(java.lang.Object)
		 */
		@Override
		public String getApplicationLabel(ApplicationModules application) {
			return modules.getSystemName().orElse("Application");
		}

		/*
		 * (non-Javadoc)
		 * @see org.jmolecules.stereotype.tooling.LabelProvider#getPackageLabel(java.lang.Object)
		 */
		@Override
		public String getPackageLabel(JavaPackage pkg) {

			var name = pkg.getName();

			return modules.getModuleForPackage(name)
					.map(ApplicationModule::getDisplayName)
					.orElse(name);
		}

		/*
		 * (non-Javadoc)
		 * @see org.jmolecules.stereotype.tooling.LabelProvider#getTypeLabel(java.lang.Object)
		 */
		@Override
		public String getTypeLabel(Class<?> type) {

			return modules.getModuleByType(type)
					.map(it -> FormattableType.of(type).getAbbreviatedFullName(it))
					.orElseGet(() -> type.getName());
		}

		/*
		 * (non-Javadoc)
		 * @see org.jmolecules.stereotype.tooling.LabelProvider#getMethodLabel(java.lang.Object)
		 */
		@Override
		public String getMethodLabel(Method method, Class<?> contextual) {
			return SpringLabelUtils.requestMappings(method, contextual);
		}

		/*
		 * (non-Javadoc)
		 * @see org.jmolecules.stereotype.tooling.LabelProvider#getCustomLabel(java.lang.Object)
		 */
		@Override
		public String getCustomLabel(NamedInterfaceNode ni) {
			return ni.toString();
		}

		/*
		 * (non-Javadoc)
		 * @see org.jmolecules.stereotype.tooling.LabelProvider#getSterotypeLabel(org.jmolecules.stereotype.api.Stereotype)
		 */
		@Override
		public String getSterotypeLabel(Stereotype stereotype) {

			var groups = catalog.getGroupsFor(stereotype);

			return stereotype.getDisplayName() + (groups.isEmpty() ? ""
					: " " + groups.stream().map(StereotypeGroup::getDisplayName)
							.collect(Collectors.joining(", ", "(", ")")));
		}

		static class DefaultApplicationModulesStructureProvider extends ApplicationModulesStructureProvider
				implements SimpleStructureProvider<ApplicationModules, JavaPackage, Class<?>, Method> {

			DefaultApplicationModulesStructureProvider(ApplicationModules modules, StereotypeCatalog catalog) {
				super(modules, catalog);
			}

			@Override
			public Collection<Class<?>> extractTypes(JavaPackage pkg) {
				return modules.getModuleForPackage(pkg.getName()).get().getBasePackage().stream()
						.<Class<?>> map(JavaClass::reflect).toList();
			}

			/*
			 * (non-Javadoc)
			 * @see org.jmolecules.stereotype.tooling.LabelProvider#getTypeLabel(java.lang.Object)
			 */
			@Override
			public String getTypeLabel(Class<?> type) {

				return super.getTypeLabel(type) + modules.getModuleByType(type)
						.map(it -> it.isExposed(type) ? " (API)" : " (internal)")
						.orElse("");
			}
		}

		static class NamedInterfacesGroupingProvider extends ApplicationModulesStructureProvider
				implements GroupingStructureProvider<ApplicationModules, JavaPackage, Class<?>, Method, NamedInterfaceNode> {

			NamedInterfacesGroupingProvider(ApplicationModules modules, StereotypeCatalog catalog) {
				super(modules, catalog);
			}

			/*
			 * (non-Javadoc)
			 * @see org.jmolecules.stereotype.tooling.StructureProvider.TypeGroupingStructureProvider#groupTypes(java.lang.Object)
			 */
			@Override
			public Grouped<NamedInterfaceNode, Class<?>> groupTypes(JavaPackage pkg) {

				var module = modules.getModuleForPackage(pkg.getName()).get();
				var interfaces = module.getNamedInterfaces().stream().collect(NamedInterfaceNode.collector());

				interfaces.put(NamedInterfaceNode.INTERNAL,
						NamedInterfaceNode.getClasses(module.getInternalTypes().stream()));

				return new Grouped<NamedInterfaceNode, Class<?>>(interfaces);
			}
		}
	}
}
