package ca.corefacility.bioinformatics.irida.plugins;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import ca.corefacility.bioinformatics.irida.exceptions.PostProcessingException;
import ca.corefacility.bioinformatics.irida.model.sample.MetadataTemplateField;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.MetadataEntry;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.PipelineProvidedMetadataEntry;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.AnalysisOutputFile;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.type.AnalysisType;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.pipeline.results.updater.AnalysisSampleUpdater;
import ca.corefacility.bioinformatics.irida.service.sample.MetadataTemplateService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;

public class ExamplePluginUpdater implements AnalysisSampleUpdater {

	private final MetadataTemplateService metadataTemplateService;
	private final SampleService sampleService;

	/**
	 * Builds a new {@link ExamplePluginUpdater} with the given services.
	 * 
	 * @param metadataTemplateService The metadata template service.
	 * @param sampleService           The sample service.
	 */
	public ExamplePluginUpdater(MetadataTemplateService metadataTemplateService, SampleService sampleService) {
		this.metadataTemplateService = metadataTemplateService;
		this.sampleService = sampleService;
	}

	/**
	 * Code to perform the actual update of the {@link Sample}s passed in the
	 * collection.
	 * 
	 * @param samples  A collection of {@link Sample}s that were passed to this
	 *                 pipeline.
	 * @param analysis The {@link AnalysisSubmission} object corresponding to this
	 *                 analysis pipeline.
	 */
	@Override
	public void update(Collection<Sample> samples, AnalysisSubmission analysis) throws PostProcessingException {
		if (samples == null) {
			throw new IllegalArgumentException("samples is null");
		} else if (analysis == null) {
			throw new IllegalArgumentException("analysis is null");
		} else if (samples.size() != 1) {
			// In this particular pipeline, only one sample should be run at a time so I
			// verify that the collection of samples I get has only 1 sample
			throw new IllegalArgumentException(
					"samples size=" + samples.size() + " is not 1 for analysisSubmission=" + analysis.getId());
		}

		final Sample sample = samples.iterator().next();

		AnalysisOutputFile hashAnalysisFile = analysis.getAnalysis().getAnalysisOutputFile("hash.txt");
		Path hashFile = hashAnalysisFile.getFile();

		// AnalysisOutputFile readCountAnalysisFile =
		// analysis.getAnalysis().getAnalysisOutputFile("read-count.txt");
		// Path readCountFile = readCountAnalysisFile.getFile();

		try {
			Map<String, MetadataEntry> metadataEntries = new HashMap<>();

			Map<String, String> hashValues = parseHashFile(hashFile);
			for (String hashType : hashValues.keySet()) {
				final String hashValue = hashValues.get(hashType);

				PipelineProvidedMetadataEntry hashEntry = new PipelineProvidedMetadataEntry(hashValue, "text",
						analysis);

				metadataEntries.put(hashType, hashEntry);
			}

			Map<MetadataTemplateField, MetadataEntry> metadataMap = metadataTemplateService
					.getMetadataMap(metadataEntries);

			sample.mergeMetadata(metadataMap);
			sampleService.updateFields(sample.getId(), ImmutableMap.of("metadata", sample.getMetadata()));
		} catch (IOException e) {
			throw new PostProcessingException("Error parsing hash file", e);
		}
	}

	/**
	 * Parses out values from the hash file into a {@link Map} linking 'hashType' to
	 * 'hashValue'.
	 * 
	 * @param hashFile The {@link Path} to the file containing the hash values from
	 *                 the pipeline. This file should contain contents like:
	 * 
	 * <pre>
	 * #md5                                sha1
	 * d54d78010cf8eeaa76c46646846be4f2    5908a485e47f870d3f9d72ff1e55796512047f00
	 * </pre>
	 * 
	 * @return A {@link Map} linking 'hashType' to 'hashValue'.
	 * @throws IOException             If there was an error reading the file.
	 * @throws PostProcessingException If there was an error parsing the file.
	 */
	private Map<String, String> parseHashFile(Path hashFile) throws IOException, PostProcessingException {
		Map<String, String> hashTypeValues = new HashMap<>();

		BufferedReader hashReader = new BufferedReader(new FileReader(hashFile.toFile()));

		try {
			String headerLine = hashReader.readLine();

			if (!headerLine.startsWith("#")) {
				throw new PostProcessingException("Missing '#' in header of file " + hashFile);
			} else {
				// strip off '#' prefix
				headerLine = headerLine.substring(1);
			}

			String[] hashTypes = headerLine.split("\t");

			String hashValuesLine = hashReader.readLine();
			String[] hashValues = hashValuesLine.split("\t");

			if (hashTypes.length != hashValues.length) {
				throw new PostProcessingException("Unmatched fields in header and values from file " + hashFile);
			}

			for (int i = 0; i < hashTypes.length; i++) {
				hashTypeValues.put(hashTypes[i], hashValues[i]);
			}

			if (hashReader.readLine() != null) {
				throw new PostProcessingException("Too many lines in file " + hashFile);
			}
		} finally {
			hashReader.close();
		}

		return hashTypeValues;
	}

	/**
	 * The {@link AnalysisType} this {@link AnalysisSampleUpdater} corresponds to.
	 * 
	 * @return The {@link AnalysisType} this {@link AnalysisSampleUpdater}
	 *         corresponds to.
	 */
	@Override
	public AnalysisType getAnalysisType() {
		return ExamplePlugin.READ_INFO;
	}
}
