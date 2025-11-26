# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 3.0.0 

### Added 
- Maven wrapper 3.3.4

### Changed
- Migrated application to spring boot 3.5.8
- Upgrade dependencies: MongoDB 4 -> MongoDB 8
- Updated README
- Server now listens on `/` not `/gazetteer`
- Building with Java 25 instead of 8
- Updated geotools 14.5 -> 34.0
- Migrated `xml` configuration to Java `@Configuration` 
- Migrated javax to jakartax
- Replaced `xyt.test` domain in testdata with `example.com`
- `CHANGELOG.md`

### Security 

[unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v3.0.0...HEAD
[3.0.0]: https://github.com/olivierlacan/keep-a-changelog/releases/tag/v3.0.0
