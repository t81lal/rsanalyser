package org.nullbool.api.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Map;

import org.nullbool.impl.Boot;

public class PatternParser {

	private Map<String, String[]> patterns = new Hashtable<String, String[]>();

	public PatternParser() throws IOException, URISyntaxException {
		Files.lines(Paths.get(new File(Boot.class.getResource("/pat/Patterns.txt").toURI()).getAbsolutePath())).forEach(line -> {
			String[] mainParts = line.split(" ");
			String[] secondParts = mainParts[1].split(",");
			this.patterns.put(mainParts[0], secondParts);
		});
	}

	public Map<String, String[]> getPatterns() {
		return this.patterns;
	}
}