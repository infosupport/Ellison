/*
 * Ellison: EL checker
 * Copyright (C) 2013 Info Support
 * dev@sonar.codehaus.org
 * 
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.infosupport.ellison.sonarplugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.AbstractSourceImporter;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;

/**
 * Imports HTML source files into Sonar's backend, so as to be able to refer to the actual source within
 * violations.
 * The current implementation recognizes any file with one of the following extensions as HTML source files:
 * <ul>
 * <li>html</li>
 * <li>xhtml</li>
 * <li>jsp</li>
 * </ul>
 */
public class HTMLSourceImporter extends AbstractSourceImporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HTMLSourceImporter.class);
    private static final String[] HTML_EXTENSIONS = {"html", "xhtml", "jsp"};

    /**
     * Constructor.
     *
     * @param language
     *     the language of the project for which the sources need to be imported.
     *
     * @see AbstractSourceImporter#AbstractSourceImporter(org.sonar.api.resources.Language)
     */
    public HTMLSourceImporter(Language language) {
        super(language);
    }

    /**
     * This method is called to add the HTML source files to Sonar's backend. See the documentation of the superclass
     * for more information.
     *
     * @param project
     *     the project for which this method will be importing HTML source files
     * @param context
     *     the context in which this method has been called
     *
     * @see AbstractSourceImporter#analyse(org.sonar.api.resources.Project, org.sonar.api.batch.SensorContext)
     */
    @Override
    public void analyse(Project project, SensorContext context) {
        ProjectFileSystem fileSystem = project.getFileSystem();
        List<File> sourceDirs = new ArrayList<>();
        sourceDirs.addAll(fileSystem.getSourceDirs());
        File webAppDir = new File(fileSystem.getBasedir(), "src/main/webapp");
        if (webAppDir.exists()) {
            sourceDirs.add(webAppDir);
        }

        List<File> htmlFiles = new ArrayList<>();

        for (File sourceDir : sourceDirs) {
            htmlFiles.addAll(FileUtils.listFiles(sourceDir, HTML_EXTENSIONS, true));
        }

        for (File htmlFile : htmlFiles) {
            Resource htmlResource = createResource(htmlFile, sourceDirs, false);
            String htmlSource = null;
            try {
                htmlSource = FileUtils.readFileToString(htmlFile);
            } catch (IOException e) {
                LOGGER.warn("Could not read HTML file", e);
                return;
            }
            context.saveSource(htmlResource, htmlSource);
        }

        onFinished();
    }
}
