# Spring Restbucks Demo 2

This branch was created to showcase the following features:

1. Open modules with `@ApplicationModule(type = Type.OPEN)`


2. Independent module testing (vertical slicing) with `@ApplicationModuleTest`

> Note: These are two independent demos.
> 
> #1 is dependent on the `legacy` module code arrangement in this branch
> 
> #2 is independent of any branch specifics and could be replicated in other branches as well (i.e. ones without any "legacy" code)


### Demo 1 Instructions:

`@ApplicationModule(type = Type.OPEN)` exposes all classes within a package hierarchy (package and all sub-packages).


This is particularly useful to enable legacy and modularized code to coexist while modernization efforts are ongoing.
Rather than completely ignoring the legacy code, this configuration enables Spring Modulith to detect and recognize the "open" packages, so that you can allow access without losing the benefit of including the package in module detection, documentation, etc.

#### Setup:

The starting state should have all the code in [legacy/package-info.java](server/src/main/java/org/springsource/restbucks/legacy/package-info.java) **commented out** (or the file deleted altogether).

#### Demo:

1. Run all tests.
Many should fail, including the Modularity tests, because they cannot access some classes in subpackage(s) of the `legacy` package. 
2. Let's assume that this code is not easily modernized, and for the time being, it is acceptable to simply expose it to other packages in the application so that the legacy and modernized modules can coexist.
Edit `legacy/package-info.java` so that it contains the following:
```java
@ApplicationModule(type = Type.OPEN)
package org.springsource.restbucks.legacy;

import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.ApplicationModule.Type;
```
3. Re-run all tests.
At this point only one should fail (continue to next demo).

### Demo 2 Instructions:

`@ApplicationModuleTest` enables testing modules in isolation.
You can test the standalone mode, the module and its direct dependencies, or the module and all of its dependencies.

#### Setup:

Make sure you have configured `package-info.java` in the `legacy` module, as per the instructions in Demo 1.

In addition, make sure [OrdersIntegrationTest.java](server/src/test/java/org/springsource/restbucks/order/OrdersIntegrationTest.java) is annotated with `@ApplicationModuleTest` but **not** `@ApplicationModuleTest(mode = BootstrapMode.DIRECT_DEPENDENCIES)` or `@ApplicationModuleTest(mode = BootstrapMode.ALL_DEPENDENCIES)`.

#### Demo:

1. Run `OrdersIntegrationTest`. It should fail.
By default, `@ApplicationModuleTest` runs in `STANDALONE` mode, which will load classes related to, in this case, the `order` module.
However, this module has dependencies that are required for the tests, as written, to pass. (In this case, though it is not germaine, the dependencies are on classes inside the `legacy ` package).
2. Change `OrdersIntegrationTest` to `@ApplicationModuleTest(mode = BootstrapMode.ALL_DEPENDENCIES)` and re-run the test. 
It should pass.
