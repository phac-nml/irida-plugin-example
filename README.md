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

# Using as a template for developing a plugin

In order to use this project as a template for developing your own pluginable pipeline there are a few places you will need to change.

## 1. [src/main/java/ca/corefacility/bioinformatics/irida/plugins/ExamplePlugin.java][example-plugin-java]

This is the main class you will need to modify for your pipeline. The class can be located in any package you wish, and can have any name you wish.  You will want to implement the two methods which are indicated as **required** in this file.  At minimum you should have a class looking like:

```java
public class ExamplePlugin extends Plugin {

	public static final AnalysisType MY_ANALYSIS_TYPE = new AnalysisType("MY_ANALYSIS_TYPE");

	public ExamplePlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Extension
	public static class PluginInfo implements IridaPlugin {

		@Override
		public AnalysisType getAnalysisType() {
			return MY_ANALYSIS_TYPE;
		}

		@Override
		public UUID getDefaultWorkflowUUID() {
			return UUID.fromString("79d90ca8-00ae-441b-b5c7-193c9e85a968");
		}
	}
}
```

### 2. Place necessary workflow files in [src/main/resources/workflows][workflows-dir]

The structure of this directory will look like:

```
workflows/
└── 0.1.0
    ├── irida_workflow_structure.ga
    ├── irida_workflow.xml
    └── messages_en.properties
```

* The directory `0.1.0` corresponds to all files for a particular version of a workflow (in this case `0.1.0`). Old versions should be kept in this directory so that IRIDA can load up information about these workflows.
* The file `irida_workflow_structure.ga` is a [Galaxy][] workflow file which is uploaded to a Galaxy instance by IRIDA before execution.
* The file `irida_workflow.xml` contains information about this particular workflow used by IRIDA.
* The file `messages_en.properties` contains messages which will be displayed in the IRIDA UI.

# Dependencies

The following dependendencies are required in order to make use of this plugin.

* [IRIDA][] >= 0.23.0
* [Java][] >= 1.8 and [Maven][maven] (for building)

[maven]: https://maven.apache.org/
[IRIDA]: http://irida.ca/
[Galaxy]: https://galaxyproject.org/
[Java]: https://www.java.com/
[workflows-dir]: src/main/resources/workflows
[example-plugin-java]: src/main/java/ca/corefacility/bioinformatics/irida/plugins/ExamplePlugin.java
[irida-setup]: https://irida.corefacility.ca/documentation/administrator/index.html
[example-plugin-results.png]: doc/images/example-plugin-results.png
[example-plugin-pipeline.png]: doc/images/example-plugin-pipeline.png
[example-plugin-metadata.png]: doc/images/example-plugin-metadata.png