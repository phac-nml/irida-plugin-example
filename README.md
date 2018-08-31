# IRIDA Example Pipeline Plugin

This project contains an example pipeline implemented as a plugin for the [IRIDA][] bioinformatics analysis system. This can be used as a template for implementing your own pipelines within IRIDA. Please see the pipeline documentation at <https://irida.corefacility.ca/documentation/developer/tools/pipelines/> for more details.

# Building/Packaging

Building and packaging this code is accomplished using [Apache Maven][maven]. However, you will first need to install [IRIDA][] to your local Maven repository.

## Installing IRIDA to local Maven repository

To install IRIDA to your local Maven repository please do the following:

1. Clone the IRIDA project

```bash
git clone https://github.com/phac-nml/irida.git
cd irida
```

2. Install IRIDA to local repository

```bash
mvn clean install -DskipTests
```

## Building the plugin

Once you've installed IRIDA as a dependency, you can proceed to building this plugin. Please run the following commands:

```bash
cd irida-plugin-example

mvn clean package
```

Once complete, you should end up with a file `target/example-plugin-1.0-SNAPSHOT.jar` which can be installed as a plugin to IRIDA.

If you have previously [setup IRIDA][irida-setup] before you may copy this JAR file to `/etc/irida/plugins` and restart IRIDA.  The plugin should now show up in the **Analyses > Pipelines** section of IRIDA.

![example-plugin-pipeline.png][]  

You should be able to run a pipeline with this plugin and get analysis results.

![example-plugin-results.png][]

And, you should be able to save and view these results in the IRIDA metadata table.

![example-plugin-metadata.png][]

# Dependencies

The following dependendencies are required in order to make use of this plugin.

* [IRIDA][] >= 0.23.0
* [Java][] >= 1.8 and [Maven][maven] (for building)

[maven]: https://maven.apache.org/
[IRIDA]: http://irida.ca/
[Java]: https://www.java.com/
[irida-setup]: https://irida.corefacility.ca/documentation/administrator/index.html
[example-plugin-results.png]: doc/images/example-plugin-results.png
[example-plugin-pipeline.png]: doc/images/example-plugin-pipeline.png
[example-plugin-metadata.png]: doc/images/example-plugin-metadata.png