# Fenix Framework Gradle Plugin

The Fenix Framework Gradle Plugin integrates the Fenix Framework build process with the [Gradle](http://www.gradle.org) build tool.


## Getting Started

Starting to use the Fenix Framework Gradle plugin is a straightforward process. To use it, you must simply add the plugin as a `buildscript` dependency, and apply it.

Here is a simple `build.gradle` applying the plugin to a Java project:

```groovy
apply plugin: 'java'
apply plugin: 'ff'

repositories {
    maven { url "https://fenix-ashes.ist.utl.pt/nexus/content/groups/fenix-ashes-maven-repository" }
}

buildscript {
    repositories {
        maven { url "https://fenix-ashes.ist.utl.pt/nexus/content/groups/fenix-ashes-maven-repository" }
    }
    dependencies { classpath "org.fenixedu:ff-gradle-plugin:1.0.0" }
}

dependencies {
    compile "pt.ist:fenix-framework-core-api:2.5.0"
}
```

This simple project will:

 - Scan the 'src/main/dml' folder for DML files, and add them to the project.
 - Scan the project's dependencies for Fenix Framework Projects, and add the to the project's dependencies.
 - Generate all the necessary `_Base` classes
 - Post-process all compile classes with the `@Atomic` injector

## Configuration

To configure the plugin, you must simply add a `ff` block to your `build.gradle`:

```groovy
ff {
    codeGenerator = "my.code.Generator"
}
```
 
The following properties are available:

  - `codeGenerator` The name of the Code Generator to be used to generate the _Base classes. Defaults to `pt.ist.fenixframework.dml.DefaultCodeGenerator`.
  - `parameters` A key-value map of properties to be passed to the Code Generator. Defaults to an empty map.
  - `enableTests` Whether to generate test-specific _Base classes. This enables looking up DML files under src/test/dml. Defaults to `false`.
  - `testCodeGenerator` The Code Generator used to generate test _Base classes. Defaults to `pt.ist.fenixframework.dml.DefaultCodeGenerator`.
  
## Fenix Framework versions

The plugin does not impose any version of the Fenix Framework runtime or dependencies. However, it uses a specific version of the Fenix Framework Code Generator modules. If you wish to override this behaviour and choose a specific version by adding it as a dependency in your buildscript:

```groovy
buildscript {
    repositories {
        maven { url "https://fenix-ashes.ist.utl.pt/nexus/content/groups/fenix-ashes-maven-repository" }
    }
    dependencies {
        classpath "org.fenixedu:ff-gradle-plugin:1.0.0"
        classpath "pt.ist:fenix-framework-core-dml-code-generator:your-version-here"
    }
}
```

