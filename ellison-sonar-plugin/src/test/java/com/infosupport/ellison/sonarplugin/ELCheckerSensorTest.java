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

import com.google.common.collect.ImmutableList;
import com.infosupport.ellison.core.api.ELError;
import com.infosupport.ellison.core.api.LocationAwareELExpression;
import com.infosupport.ellison.core.exceptions.ArchiveFormatUnsupportedException;
import com.infosupport.ellison.core.util.Location;
import com.infosupport.ellison.core.util.LocationAwareELExpressionString;
import com.infosupport.ellison.sonarplugin.ELApplicationCheckerSonarBatchComponent;
import com.infosupport.ellison.sonarplugin.ELCheckerPlugin;
import com.infosupport.ellison.sonarplugin.ELCheckerSensor;
import com.infosupport.ellison.sonarplugin.rules.ELCheckerCriticalRule;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import junitx.util.PrivateAccessor;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.rules.Violation;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for the {@link ELCheckerSensor} class.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class ELCheckerSensorTest {
    @Mock MavenProject mavenProject;
    @Mock Project project;
    @Mock SensorContext sensorContext;
    @Mock ProjectFileSystem projectFileSystem;
    @Mock ELApplicationCheckerSonarBatchComponent elApplicationChecker;
    Settings settings;
    ELCheckerSensor elCheckerSensor;
    File baseDir;
    File buildDir;

    @Before
    public void setUp() throws Exception {
        settings = new Settings();
        baseDir = new File("basedir");
        buildDir = new File("buildDir");

        when(project.getFileSystem()).thenReturn(projectFileSystem);
        when(projectFileSystem.getBasedir()).thenReturn(baseDir);
        when(projectFileSystem.getBuildDir()).thenReturn(buildDir);
    }

    @Test
    public void testAnalyse_SavingViolations() throws Exception {
        File archiveFile = mock(File.class);
        File sourceDir = mock(File.class);
        List<File> sourceDirs = ImmutableList.<File>builder().add(sourceDir).build();
        List<ELError> elErrors = new ArrayList<>();
        ArgumentCaptor<Violation> violationArgument = ArgumentCaptor.forClass(Violation.class);

        Location firstLocation = new Location(getClass().getResource("/otherDirectory/htmlfile.html").toURI(), 1, 2);
        LocationAwareELExpression firstExpression =
            new LocationAwareELExpressionString("firstExpression", firstLocation);
        ELError firstError = new ELError("firstError", firstExpression, ELError.ELErrorSeverity.ERROR);
        elErrors.add(firstError);
        Location secondLocation = new Location(getClass().getResource("/otherDirectory/htmlfile.html").toURI(), 1, 2);
        LocationAwareELExpression secondExpression =
            new LocationAwareELExpressionString("secondExpression", secondLocation);
        ELError secondError = new ELError("secondError", secondExpression, ELError.ELErrorSeverity.ERROR);
        elErrors.add(secondError);

        PrivateAccessor.setField(sourceDir, "path", "");
        settings.setProperty(ELCheckerPlugin.ARTIFACT_PATH, "somepath");
        elCheckerSensor = new ELCheckerSensor(mavenProject, settings, elApplicationChecker);
        when(sourceDir.exists()).thenReturn(true);
        when(sourceDir.isDirectory()).thenReturn(true);
        when(projectFileSystem.getSourceDirs()).thenReturn(sourceDirs);
        when(elApplicationChecker.checkApplication(Matchers.any(File.class))).thenReturn(elErrors);

        elCheckerSensor.analyse(project, sensorContext);

        verify(sensorContext, times(elErrors.size())).saveViolation(violationArgument.capture());

        List<Violation> savedViolations = violationArgument.getAllValues();
        for (Violation savedViolation : savedViolations) {
            boolean violationCameFromError = false;
            for (ELError elError : elErrors) {
                if (savedViolation.getMessage().contains(elError.getMessage())) {
                    violationCameFromError = true;
                    break;
                }
            }

            assertThat(violationCameFromError, is(true));
        }
    }

    @Test
    public void testAnalyse_ExistingFile_NoViolations() throws Exception {
        File archiveFile = new File(getClass().getResource("/otherDirectory/htmlfile.html").toURI());
        File sourceDir = archiveFile.getParentFile();
        List<File> sourceDirs = ImmutableList.<File>builder().add(sourceDir).build();
        settings.setProperty(ELCheckerPlugin.ARTIFACT_PATH, archiveFile.getCanonicalPath());

        elCheckerSensor = new ELCheckerSensor(mavenProject, settings, elApplicationChecker);
        when(projectFileSystem.getSourceDirs()).thenReturn(sourceDirs);
        when(elApplicationChecker.checkApplication(Matchers.any(File.class))).thenReturn(null);

        elCheckerSensor.analyse(project, sensorContext);

        verify(sensorContext, never()).saveViolation(Matchers.any(Violation.class));
    }

    @Test
    public void testAnalyse_ArchiveNotSupported() throws Exception {
        File archiveFile = mock(File.class);
        File sourceDir = mock(File.class);
        List<File> sourceDirs = ImmutableList.<File>builder().add(sourceDir).build();
        ArgumentCaptor<Violation> violationArgument = ArgumentCaptor.forClass(Violation.class);
        settings.setProperty(ELCheckerPlugin.ARTIFACT_PATH, "somepath");

        elCheckerSensor = new ELCheckerSensor(mavenProject, settings, elApplicationChecker);
        when(projectFileSystem.getSourceDirs()).thenReturn(sourceDirs);
        when(elApplicationChecker.checkApplication(Matchers.any(File.class)))
            .thenThrow(ArchiveFormatUnsupportedException.class);

        elCheckerSensor.analyse(project, sensorContext);

        verify(sensorContext).saveViolation(violationArgument.capture());
        assertThat(violationArgument.getValue().getRule().getKey(), is(equalTo(ELCheckerCriticalRule.KEY)));
    }

    @Test
    public void testSearchArtifactFile_ArtifactPathSavedInSettings() throws Exception {
        settings.appendProperty(ELCheckerPlugin.ARTIFACT_PATH, "bla/artifact.packaging");

        elCheckerSensor = new ELCheckerSensor(mavenProject, settings, elApplicationChecker);

        File artifactFile = elCheckerSensor.searchArtifactFile(mavenProject, projectFileSystem);

        assertThat(artifactFile, is(notNullValue()));
        assertThat(fileIsContainedInDirectory(baseDir, artifactFile), is(true));

        verify(projectFileSystem, never()).getBuildDir();
        verify(projectFileSystem, times(1)).getBasedir();
    }

    @Test
    public void testSearchArtifactFile_ArtifactPathDeducedFromPOM() throws Exception {
        Build build = mock(Build.class);
        when(mavenProject.getBuild()).thenReturn(build);
        elCheckerSensor = new ELCheckerSensor(mavenProject, settings, elApplicationChecker);

        File artifactFile = elCheckerSensor.searchArtifactFile(mavenProject, projectFileSystem);

        assertThat(artifactFile, is(notNullValue()));
        assertThat(fileIsContainedInDirectory(buildDir, artifactFile), is(true));

        verify(mavenProject, atLeastOnce()).getBuild();
        verify(build, atLeastOnce()).getFinalName();
        verify(mavenProject, atLeastOnce()).getPackaging();
    }

    @Test
    public void testCreateViolationFromELError() throws Exception {
        String errorMessage = "errorMessage";
        LocationAwareELExpression elExpression = mock(LocationAwareELExpression.class);
        Location location = mock(Location.class);
        File htmlFile = new File(getClass().getResource("/otherDirectory/htmlfile.html").toURI());
        File sourceDir = htmlFile.getParentFile();
        File otherSourceDir = sourceDir.getParentFile();
        URI locationURI = sourceDir.toURI().relativize(htmlFile.toURI());
        ELError elError = new ELError(errorMessage, elExpression, ELError.ELErrorSeverity.INFO);
        List<File> sourceDirs = ImmutableList.<File>builder().add(sourceDir).add(otherSourceDir).build();
        elCheckerSensor = new ELCheckerSensor(mavenProject, settings, elApplicationChecker);

        when(elExpression.getLocation()).thenReturn(location);
        when(location.getLine()).thenReturn(1337);
        when(location.getUri()).thenReturn(locationURI);
        when(projectFileSystem.getSourceDirs()).thenReturn(sourceDirs);

        Violation violation = elCheckerSensor.createViolationFromELError(project, sensorContext, elError);

        assertThat(violation, is(notNullValue()));
        assertThat(violation.getMessage(), containsString(errorMessage));
        assertThat(violation.getLineId(), is(equalTo(elError.getElExpression().getLocation().getLine())));
        assertThat(violation.getResource().getKey(), is(equalTo(elExpression.getLocation().getUri().toString())));
        assertThat(violation.getRule().getKey().startsWith("el-static-checker-rule-"), is(true));
    }

    /**
     * Check whether {@code file} is contained in {@code directory}.
     * This is done by checking if any ancestor directory of {@code file} is equal to {@code directory}.
     *
     * @param directory
     *     the directory that might contain {@code file}
     * @param file
     *     the file for which we want to check if it's contained in {@code directory}
     *
     * @return {@code true} if {@code file} has any ancestor directory that is equal to {@code directory}. {@code
     *         false} otherwise.
     */
    public boolean fileIsContainedInDirectory(File directory, File file) {
        File parent = file;
        while (parent != null) {
            if (directory.equals(parent)) {
                return true;
            } else {
                parent = parent.getParentFile();
            }
        }

        return false;
    }
}
