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
import com.infosupport.ellison.sonarplugin.HTMLSourceImporter;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import junitx.util.PrivateAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the {@link HTMLSourceImporter} class.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class HTMLSourceImporterTest {
    HTMLSourceImporter htmlSourceImporter;
    @Mock Project project;
    @Mock SensorContext sensorContext;
    @Mock Language language;
    @Mock ProjectFileSystem projectFileSystem;
    @Mock File sourceDir = mock(File.class);
    @Mock File mockedFile = mock(File.class);
    List<File> sourceDirs;

    @Before
    public void setUp() throws Exception {
        htmlSourceImporter = new HTMLSourceImporter(language);

        PrivateAccessor.setField(sourceDir, "path", "directory");
        sourceDirs = ImmutableList.<File>builder().add(sourceDir).build();
        when(project.getFileSystem()).thenReturn(projectFileSystem);
        when(projectFileSystem.getSourceDirs()).thenReturn(sourceDirs);
        when(sourceDir.isDirectory()).thenReturn(true);
    }

    @Test
    public void analyse_IOException() throws Exception {
        when(sourceDir.listFiles(any(FileFilter.class))).thenReturn(new File[]{mockedFile});
        when(mockedFile.getPath()).thenReturn("file.html");
        when(mockedFile.getAbsolutePath()).thenReturn("file.html");
        when(mockedFile.exists()).thenReturn(false);

        htmlSourceImporter.analyse(project, sensorContext);

        verify(sensorContext, never()).saveSource(any(Resource.class), any(String.class));
    }

    @Test
    public void analyse_SaveSources() throws Exception {
        File htmlSourceFile = new File(getClass().getResource("/otherDirectory/htmlfile.html").toURI());
        File xhtmlSourceFile = new File(getClass().getResource("/otherDirectory/xhtmlfile.xhtml").toURI());
        File jspSourceFile = new File(getClass().getResource("/otherDirectory/jspfile.jsp").toURI());
        when(sourceDir.listFiles(any(FileFilter.class))).thenReturn(new File[]{
            htmlSourceFile, xhtmlSourceFile, jspSourceFile
        });

        htmlSourceImporter.analyse(project, sensorContext);

        verify(sensorContext, times(1)).saveSource(any(Resource.class), startsWith("html"));
        verify(sensorContext, times(1)).saveSource(any(Resource.class), startsWith("xhtml"));
        verify(sensorContext, times(1)).saveSource(any(Resource.class), startsWith("jsp"));
    }
}
