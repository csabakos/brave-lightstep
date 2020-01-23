# brave-lightstep

[ ![Download](https://api.bintray.com/packages/lightstep/maven/brave-lightstep/images/download.svg) ](https://bintray.com/lightstep/maven/) [![Circle CI](https://circleci.com/gh/lightstep/brave-lightstep.svg?style=shield)](https://circleci.com/gh/lightstep/brave-lightstep) [![MIT license](http://img.shields.io/badge/license-MIT-blue.svg)](http://opensource.org/licenses/MIT)

LightStep-specific extensions for [Brave](https://github.com/openzipkin/brave).

* [Getting Started](#getting-started)
* [Features](#features)
* [Development info](#development-info)

<a name="#getting-started"></a>
<a name="#getting-started-jre"></a>

## Getting started

The library is hosted on Bintray, jcenter, and Maven Central. 
The Bintray [brave-lightstep](https://bintray.com/lightstep/maven/brave-lightstep/view) project contains 
additional installation and setup information for using the library with various build systems such as Ivy and Maven.

### Maven

```xml
<dependency>
    <groupId>com.lightstep.tracer</groupId>
    <artifactId>brave-lightstep</artifactId>
    <version>VERSION</version>
</dependency>
```

* Be sure to replace `VERSION` with the current version of the library.

### Gradle

In most cases, modifying your `build.gradle` with the below is all that is required:

```
repositories {
    mavenCentral() // OR jcenter()
}
dependencies {
    compile 'com.lightstep.tracer:brave-lightstep:VERSION'
}
```

* Be sure to replace `VERSION` with the current version of the library
* The artifact is published to both `jcenter()` and `mavenCentral()`. Use whichever you prefer.

## Features

### LightStep propagation

[LightStepPropagation.java](src/main/java/brave/propagation/LightStepPropagation.java) implements LightStep propagation. In case of HTTP headers, it uses the `ot-tracer-*` headers.

The intent of this implementation is to be able to use LightStep propagation with Brave instrumentation, and to ease the migration between LightStep and `B3` propagation.

#### Standalone usage
Simply use `LightStepPropagation.FACTORY` as your propagation factory.

#### Composite usage
Use the code from [openzipkin/brave#1060](https://github.com/openzipkin/brave/pull/1060) as follows: 
```java
CompositePropagation.newFactoryBuilder()
        .addPropagationFactory(B3Propagation.FACTORY)
        .addPropagationFactory(LightStepPropagation.FACTORY)
        .build()
```

## Development info

See [DEV.md](DEV.md) for information on contributing to this library.
