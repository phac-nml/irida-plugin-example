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

This is the main class you will need to modify for your pipeline. The class can be located in any package you wish, and can have any name you wish.  You will want to implement the two methods which are indicated as **required** in this file. You can also override the methods indicated as **optional** in the file for additional configuration.  You should have a class looking like:

```java
public class ExamplePlugin extends Plugin {

	public static final AnalysisType MY_ANALYSIS_TYPE = new AnalysisType("MY_ANALYSIS_TYPE");

	public ExamplePlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Extension
	public static class PluginInfo implements IridaPlugin {

		/*** Required ***/
		
		@Override
		public AnalysisType getAnalysisType() {
			return new AnalysisType("READ_INFO");
		}

		@Override
		public UUID getDefaultWorkflowUUID() {
			return UUID.fromString("79d90ca8-00ae-441b-b5c7-193c9e85a968");
		}
		
		/*** Optional ***/
		
		@Override
		public Optional<Color> getBackgroundColor() {
			return Optional.of(Color.decode("#dd1c77"));
		}
		
		@Override
		public Optional<Color> getTextColor() {
			return Optional.of(Color.BLACK);
		}
		
		@Override
		public Optional<AnalysisSampleUpdater> getUpdater(MetadataTemplateService metadataTemplateService,
				SampleService sampleService, IridaWorkflowsService iridaWorkflowsService) throws IridaPluginException {
			return Optional.of(new ExamplePluginUpdater(metadataTemplateService, sampleService, iridaWorkflowsService));
		}
	}
}
```

The method should be defined as:

* `getAnalysisType()`: This returns an `AnalysisType` object which stores the type of analysis as a `String` (matches the `<analysisType>READ_INFO</analysisType>` entry in the **irida_workflow.xml** file).
* `getDefaultWorkflowUUID()`: This returns the id of the workflow (matching the `<id>79d90ca8-00ae-441b-b5c7-193c9e85a968</id>` entry in the **irida_workflow.xml** file). Returning the appropriate value here is especially important if there are multiple versions of the same pipeline in this plugin (this will define the default or "latest" version).
* `getBackgroundColor()`: The background color to display in the UI (defaults to grey). This is **optional**.
* `getTextColor()`: The text color to display in the UI (defaults to black). This is **optional**.
* `getUpdater()`: Gets an instance of a class used for post-processing on pipeline results (e.g., updating the IRIDA metadata). This is **optional**. Additional documentation about this class is described below. 

### 2. Place necessary workflow files in [src/main/resources/workflows][workflows-dir]

The structure of this directory will look like:

```
workflows/
└── 0.1.0
    ├── irida_workflow_structure.ga
    ├── irida_workflow.xml
    └── messages_en.properties
```

* The directory `0.1.0` corresponds to all files for a particular version of a workflow (in this case `0.1.0`). Previous versions of the pipeline should each be kept in their own numbered directory (e.g., `0.1.0`, `0.2.0`) so that IRIDA can load up information about these workflows.
* The file `irida_workflow_structure.ga` is a [Galaxy][] workflow file which is uploaded to a Galaxy instance by IRIDA before execution.
* The file `irida_workflow.xml` contains information about this particular workflow used by IRIDA.
* The file `messages_en.properties` contains messages which will be displayed in the IRIDA UI.

#### 2.1. Creating workflow files

The necessary files that go under the `workflows/` directory can be constructed manually (see the [IRIDA Pipeline development][irida-pipeline] documentation). However, an easier approach is to construct the files automatically using the [irida-wf-ga2xml][] software and then customize the generated files.

No matter which method you use (manually constructing the workflow files, or automatically constructing them) you will need to start out by building your workflow in Galaxy. Please follow the instructions in the [IRIDA Pipelines][irida-pipeline-galaxy] documentation for how to do this. At the end, you should end up with a `workflow.ga` file exported from Galaxy, which is a JSON representation of your workflow. Once you have this file (an example would be [src/main/resources/workflows/0.1.0/irida_workflow_structure.ga][]), you can run the following to automatically generate the other necessary files:

```bash
java -jar irida-wf-ga2xml-1.0.0-SNAPSHOT-standalone.jar -n ReadInfo -t READ_INFO -W 0.1.0 -o output -i src/main/resources/workflows/0.1.0/irida_workflow_structure.ga
```

The meaning of the command-line options are as follows:

* `-n ReadInfo`: The **name** of the pipeline, stored in the **irida_workflow.xml** file under `<name>ReadInfo</name>`.
* `-t READ_INFO`: The **type** of the pipeline, stored in the **irida_workflow.xml** file under `<analysisType>READ_INFO</analysisType>`. Corresponds to the `AnalysisType` object in the Java file listed above ([ExamplePlugin.java][example-plugin-java]).
* `-W 0.1.0`:  The **version** of the pipeline, stored in the **irida_workflow.xml** file under `<version>0.1.0</version>`.
* `-o output`: The directory to store all the output files.
* `-i .../irida_workflow_structure.ga`: The input Galaxy workflow file.

Once complete, the output files will be located under **output/** and will look like:

```
output/
└── ReadInfo
    └── 0.1.0
        ├── irida_workflow_structure.ga
        ├── irida_workflow.xml
        └── messages_en.properties
```

You can directly move the `0.1.0` directory to `src/main/resources/workflows`.

#### 2.2. Updating workflow files

Once you've generated and moved your workflow files to `src/main/resources/workflows`, you can make small adjustments to them as you see fit.

##### 2.2.1. Modifying `irida_workflow.xml`

In particular, you may want to make adjustments to `irida_workflow.xml` to add/remove/change the parameters.  These are stored in the XML file like:

```xml
  <parameters>
    <parameter name="Grep1-4-invert" defaultValue="false">
      <toolParameter toolId="Grep1" parameterName="invert"/>
    </parameter>
    <parameter name="Grep1-4-pattern" defaultValue="^@">
      <toolParameter toolId="Grep1" parameterName="pattern"/>
    </parameter>
    <parameter name="wc_gnu-5-include_header" defaultValue="false">
      <toolParameter toolId="wc_gnu" parameterName="include_header"/>
    </parameter>
  </parameters>
```

When loaded up in IRIDA, these will show up like:

![pipeline-parameters.png][]

You may also wish to modify the particular output files that get saved by IRIDA. These are recorded in:

```xml
  <outputs>
    <output name="hash.txt" fileName="hash.txt"/>
    <output name="read-count.txt" fileName="read-count.txt"/>
  </outputs>
```

The `fileName` attribute should correspond to a particular name of an output file in the __*.ga__ workflow file (see [irida_workflow_stucture.ga][workflow-structure] or the [IRIDA Pipeline][irida-pipeline] documentation for more details).

##### 2.2.1. Modifying `messages_en.properties`

The file [src/main/resources/workflows/0.1.0/messages_en.properties][messages] contains the text that gets displayed by IRIDA for the pipeline. The file is in the form of `key = value` entries (the Java [.properties][properties] format).  An example of this file would be:

```properties
pipeline.parameters.modal-title.readinfo=ReadInfo Pipeline Parameters
workflow.READ_INFO.title=ReadInfo Pipeline
pipeline.h1.ReadInfo=ReadInfo Pipeline
pipeline.title.ReadInfo=Pipelines - ReadInfo
workflow.READ_INFO.description=

pipeline.parameters.readinfo.Grep1-4-invert=Grep1-4-invert
pipeline.parameters.readinfo.Grep1-4-pattern=Grep1-4-pattern

pipeline.parameters.readinfo.wc_gnu-5-include_header=wc_gnu-5-include_header
```

The entries like `workflow.READ_INFO.title=ReadInfo Pipeline` contain the text used to display the pipeline entry on the "Pipelines" page in the UI:

![example-plugin-pipeline.png][]

The entries like `pipeline.parameters.readinfo.Grep1-4-pattern=Grep1-4-pattern` contain information used to display the text when adjusting pipeline parameters:

![pipeline-parameters.png][]

The `Grep1-4-pattern` part corresponds to the **name** attribute under a `<parameter>` entry in the **irida_workflow.xml** file:

```xml
    <parameter name="Grep1-4-pattern" defaultValue="^@">
      <toolParameter toolId="Grep1" parameterName="pattern"/>
    </parameter>
```

# Dependencies

The following dependendencies are required in order to make use of this plugin.

* [IRIDA][] >= 0.23.0
* [Java][] >= 1.8 and [Maven][maven] (for building)

[maven]: https://maven.apache.org/
[IRIDA]: http://irida.ca/
[Galaxy]: https://galaxyproject.org/
[Java]: https://www.java.com/
[irida-pipeline]: https://irida.corefacility.ca/documentation/developer/tools/pipelines/
[irida-pipeline-galaxy]: https://irida.corefacility.ca/documentation/developer/tools/pipelines/#galaxy-workflow-development
[irida-wf-ga2xml]: https://github.com/phac-nml/irida-wf-ga2xml
[workflows-dir]: src/main/resources/workflows
[workflow-structure]: src/main/resources/workflows/0.1.0/irida_workflow_structure.ga
[example-plugin-java]: src/main/java/ca/corefacility/bioinformatics/irida/plugins/ExamplePlugin.java
[irida-setup]: https://irida.corefacility.ca/documentation/administrator/index.html
[properties]: https://en.wikipedia.org/wiki/.properties
[messages]: src/main/resources/workflows/0.1.0/messages_en.properties
[example-plugin-results.png]: doc/images/example-plugin-results.png
[example-plugin-pipeline.png]: doc/images/example-plugin-pipeline.png
[example-plugin-metadata.png]: doc/images/example-plugin-metadata.png
[pipeline-parameters.png]: doc/images/pipeline-parameters.png