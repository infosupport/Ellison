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
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.Violation;

import com.infosupport.ellison.core.ELApplicationChecker;
import com.infosupport.ellison.core.api.ELError;
import com.infosupport.ellison.core.api.LocationAwareELExpression;
import com.infosupport.ellison.core.exceptions.ApplicationNotSupportedException;
import com.infosupport.ellison.core.exceptions.ArchiveFormatUnsupportedException;
import com.infosupport.ellison.core.util.Location;
import com.infosupport.ellison.sonarplugin.rules.ELCheckerCriticalRule;
import com.infosupport.ellison.sonarplugin.rules.ELCheckerMinorRule;

/**
 * The {@link Sensor} actually checking the project for Expression Language problems/errors.
 *
 * @author StefanZ
 */
public class ELCheckerSensor implements Sensor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ELCheckerSensor.class);
    private MavenProject mavenProject;
    private String savedArtifactPath;
    private ELApplicationChecker elApplicationChecker;

    /**
     * Constructor.
     * Note that the parameters will normally come from Sonar, and will be provided through Inversion of Control.
     *
     * @param mavenProject
     *     an object that reflects whatever information maven has about the project to check
     * @param settings
     *     the Sonar settings. This will be used to check if the user has manually entered an artifact to
     *     check
     * @param elApplicationChecker
     *     the {@link ELApplicationCheckerSonarBatchComponent} that will be used to do
     *     the actual analysis
     */
    public ELCheckerSensor(MavenProject mavenProject, Settings settings,
                           ELApplicationCheckerSonarBatchComponent elApplicationChecker) {
        this.mavenProject = mavenProject;
        this.elApplicationChecker = elApplicationChecker;
        savedArtifactPath = settings.getString(ELCheckerPlugin.ARTIFACT_PATH);
    }

    /**
     * The analysis of the project happens here.
     * The project's artifact is checked using {@link ELApplicationChecker#checkApplication(java.io.File)}. Any found
     * (potential) errors are saved as {@link Violation}s.
     *
     * @param project
     *     the project that is to be analyzed
     * @param context
     *     the context in which the sensor is running
     *
     * @see Sensor#analyse(org.sonar.api.resources.Project, org.sonar.api.batch.SensorContext)
     * @see ELApplicationChecker#checkApplication(java.io.File)
     */
    @Override
    public void analyse(Project project, SensorContext context) {
        File artifactFile = searchArtifactFile(mavenProject, project.getFileSystem());
        LOGGER.info("Artifact file location: {} (exists?: {})", artifactFile.toURI(), artifactFile.exists());

        Collection<ELError> foundErrors = null;

        try {
            foundErrors = elApplicationChecker.checkApplication(artifactFile);
        } catch (ApplicationNotSupportedException | ArchiveFormatUnsupportedException | IOException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("An exception occurred while trying to check project '");
            sb.append(project.getLongName());
            sb.append("'. This is the exception:\n");
            sb.append(e.getMessage());
            sb.append("\n");
            sb.append(ExceptionUtils.getFullStackTrace(e));
            Rule criticalRule = Rule.create(ELCheckerRulesRepository.REPOSITORY_KEY, ELCheckerCriticalRule.KEY);
            Violation exceptionViolation = Violation.create(criticalRule, project).setMessage(sb.toString());
            context.saveViolation(exceptionViolation);
        }

        if (foundErrors != null) {
            for (ELError foundError : foundErrors) {
                Violation violation = createViolationFromELError(project, context, foundError);
                context.saveViolation(violation);
            }
        }
    }

    /**
     * Turn an {@link ELError} into a {@link Violation}.
     *
     * @param project
     *     the project where the {@code elError} originated from
     * @param context
     *     the context of the current analysis
     * @param elError
     *     the {@code ELError} to make a {@code Violation} out of
     *
     * @return a {@code Violation} with all relevant fields set using information from {@code elError}
     */
    protected Violation createViolationFromELError(Project project, SensorContext context, ELError elError) {
        LocationAwareELExpression elExpression = elError.getElExpression();
        Location location = elExpression.getLocation();
        List<File> sourceDirs = new ArrayList<>();
        sourceDirs.addAll(project.getFileSystem().getSourceDirs());

        File webAppDir = new File(project.getFileSystem().getBasedir(), "src/main/webapp");
        if (webAppDir.exists() && webAppDir.isDirectory()) {
            sourceDirs.add(webAppDir);
        }

        File sourceFile = null;
        for (File sourceDir : sourceDirs) {
            File potentialSourceFile = new File(sourceDir, location.getUri().toString());

            if (potentialSourceFile.exists()) {
                sourceFile = potentialSourceFile;
                break;
            }
        }

        Resource sonarFile = project;
        if (sourceFile != null) {
            sonarFile = org.sonar.api.resources.File.fromIOFile(sourceFile, sourceDirs);
            context.index(sonarFile);
        }

        StringBuilder violationMessage = new StringBuilder();
        violationMessage.append("Problem found in expression: \"");
        violationMessage.append(elError.getElExpression().getExpressionString());
        violationMessage.append("\":\n\n");
        violationMessage.append(elError.getMessage());
        Rule minorRule = Rule.create(ELCheckerRulesRepository.REPOSITORY_KEY, ELCheckerMinorRule.KEY);
        Violation violation = Violation.create(minorRule, sonarFile).setMessage(violationMessage.toString())
                                       .setLineId(location.getLine());

        return violation;
    }

    /**
	 * Just run on any kind of project. This method can be extended to only return true if the plugin can handle the project.
	 * 
	 * @param project
	 *            the project for which to check if this plugin should run on it
	 * 
	 * @return always true
	 * 
	 * @see Sensor#shouldExecuteOnProject(org.sonar.api.resources.Project)
	 */
    @Override
    public boolean shouldExecuteOnProject(Project project) {
        return true;
    }

    /**
     * Look for the project's artifact.
     * This method has been taken from the Sonar <a href="http://docs.codehaus.org/display/SONAR/Artifact+Size+Plugin">
     * artifact-size-plugin</a>, whose author are Olivier Gaudin and Waleri Enns.
     * License: LGPL v3
     *
     * @param pom
     *     the object representing the project's maven configuration
     * @param fileSystem
     *     the filesystem of the project that is being checked
     *
     * @return a file pointing to the artifact path set in the settings (see
     *         {@link ELCheckerSensor#ELCheckerSensor(org.apache.maven.project.MavenProject,
     *         org.sonar.api.config.Settings, ELApplicationCheckerSonarBatchComponent)}
     *         if that has been set, otherwise the artifact as defined by the maven packaging.
     */
    protected File searchArtifactFile(MavenProject pom, ProjectFileSystem fileSystem) {
        File file = null;

        if (StringUtils.isNotEmpty(savedArtifactPath)) {
            file = buildPathFromConfig(fileSystem, savedArtifactPath);
        } else if (pom != null && pom.getBuild() != null) {
            String filename = pom.getBuild().getFinalName() + "." + pom.getPackaging();
            file = buildPathFromPom(fileSystem, filename);
        }
        return file;
    }

    private File buildPathFromConfig(ProjectFileSystem fileSystem, String artifactPath) {
        return new File(fileSystem.getBasedir(), artifactPath);
    }

    private File buildPathFromPom(ProjectFileSystem fileSystem, String artifactPath) {
        return new File(fileSystem.getBuildDir(), artifactPath);
    }
}
