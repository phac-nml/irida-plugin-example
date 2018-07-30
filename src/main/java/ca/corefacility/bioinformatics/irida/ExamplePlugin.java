package ca.corefacility.bioinformatics.irida;

import ca.corefacility.bioinformatics.irida.pipeline.results.AnalysisSampleUpdater;
import ca.corefacility.bioinformatics.irida.plugins.IridaPlugin;
import ca.corefacility.bioinformatics.irida.plugins.IridaPluginException;
import ca.corefacility.bioinformatics.irida.service.sample.MetadataTemplateService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginException;
import org.pf4j.PluginWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * An example {@link IridaPlugin} implementation
 */
public class ExamplePlugin extends Plugin {

	public ExamplePlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void start() throws PluginException {
		System.out.println("Example IRIDA plugin loaded");
	}

	@Extension
	public static class PluginInfo implements IridaPlugin {

		@Override
		public Properties getMessages() throws IridaPluginException {

			ClassLoader classLoader = getClass().getClassLoader();

			Properties prop = new Properties();

			try (InputStream resourceAsStream = classLoader.getResourceAsStream("messages/messages_en.properties");) {
				prop.load(resourceAsStream);
			} catch (IOException e) {
				throw new IridaPluginException("Cannot load messages", e);
			}

			return prop;
		}

		@Override
		public Optional<AnalysisSampleUpdater> getUpdater(MetadataTemplateService metadataTemplateService,
				SampleService sampleService) {
			return Optional.empty();
		}
	}

}
