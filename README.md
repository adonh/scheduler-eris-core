# eris-core

This is an open source project!

## Description

Eris-core is a high level Cassandra driver that builds on the [Astyanax driver](https://github.com/Netflix/astyanax).

Key features:
 * Serializers for basic Scala types
 * Tuple serializers
 * TimeUuid type
 * Type-driven ColumnFamily constructors with Serializer inference
 * Simple schema loader

## Installation

This library is published to the Sonatype OSS Maven Repository, which is configured in SBT by default. To add the dependency to your SBT build file:

```scala
libraryDependencies += "com.pagerduty" %% "eris-core" % "1.4.1"
```

## Contact

This library is primarily maintained by the Core Team at PagerDuty.

## Contributing

Contributions are welcome in the form of pull-requests based on the master branch.

We ask that your changes are consistently formatted as the rest of the code in this repository, and also that any changes are covered by unit tests.

## Changelog

See [CHANGELOG.md](./CHANGELOG.md)
