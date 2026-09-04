This folder needs to contain all the JARs needed to compile and run FearlessTour.
The jars themselves are gitignored (not committed): download each one below into this
folder before building.
For now, the list is:

## Flexmark
Flexmark is the markdown parser used by markDownTests/HtmlCreator to render the
chapters into HTML pages for ZeroToHero. FearlessTour only needs the core
renderer plus the tables extension, not the full flexmark-all bundle.

To find the latest version, open:
https://repo1.maven.org/maven2/com/vladsch/flexmark/flexmark/maven-metadata.xml
and search for <versioning>...<release>????</release>

These are the current last version direct download links:
https://repo1.maven.org/maven2/com/vladsch/flexmark/flexmark/0.64.8/flexmark-0.64.8.jar
https://repo1.maven.org/maven2/com/vladsch/flexmark/flexmark-ext-tables/0.64.8/flexmark-ext-tables-0.64.8.jar
https://repo1.maven.org/maven2/com/vladsch/flexmark/flexmark-util-ast/0.64.8/flexmark-util-ast-0.64.8.jar
https://repo1.maven.org/maven2/com/vladsch/flexmark/flexmark-util-builder/0.64.8/flexmark-util-builder-0.64.8.jar
https://repo1.maven.org/maven2/com/vladsch/flexmark/flexmark-util-collection/0.64.8/flexmark-util-collection-0.64.8.jar
https://repo1.maven.org/maven2/com/vladsch/flexmark/flexmark-util-data/0.64.8/flexmark-util-data-0.64.8.jar
https://repo1.maven.org/maven2/com/vladsch/flexmark/flexmark-util-dependency/0.64.8/flexmark-util-dependency-0.64.8.jar
https://repo1.maven.org/maven2/com/vladsch/flexmark/flexmark-util-format/0.64.8/flexmark-util-format-0.64.8.jar
https://repo1.maven.org/maven2/com/vladsch/flexmark/flexmark-util-html/0.64.8/flexmark-util-html-0.64.8.jar
https://repo1.maven.org/maven2/com/vladsch/flexmark/flexmark-util-misc/0.64.8/flexmark-util-misc-0.64.8.jar
https://repo1.maven.org/maven2/com/vladsch/flexmark/flexmark-util-options/0.64.8/flexmark-util-options-0.64.8.jar
https://repo1.maven.org/maven2/com/vladsch/flexmark/flexmark-util-sequence/0.64.8/flexmark-util-sequence-0.64.8.jar
https://repo1.maven.org/maven2/com/vladsch/flexmark/flexmark-util-visitor/0.64.8/flexmark-util-visitor-0.64.8.jar

## JetBrains Annotations
Flexmark depends on org.jetbrains:annotations at compile time.

To find the latest version, open:
https://repo1.maven.org/maven2/org/jetbrains/annotations/maven-metadata.xml
and search for <versioning>...<release>????</release>

This is the current last version direct download link:
https://repo1.maven.org/maven2/org/jetbrains/annotations/24.0.1/annotations-24.0.1.jar
