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
import java.util.List;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;

/**
 * This is the "entry" point for every plugin: a subclass of {@link SonarPlugin}.
 * It is not much more than an aggregator that indicates all the functionality the plugin as a whole provides.
 *
 * @author StefanZ
 */
@Properties({
                @Property(
                    key = ELCheckerPlugin.ARTIFACT_PATH,
                    name = ELCheckerPlugin.ARTIFACT_PATH_PROP_DESCRIPTION,
                    project = true,
                    module = false,
                    global = false)
            })
public class ELCheckerPlugin extends SonarPlugin {
    static final String KEY = "el-static-checker_artifact-path";
    static final String PLUGIN_NAME = "EL static checker";
    static final List<Class> PLUGIN_EXTENSIONS = ImmutableList.<Class>builder()
                                                              .add(ELCheckerSensor.class)
                                                              .add(ELCheckerRulesRepository.class)
                                                              .add(HTMLSourceImporter.class)
                                                              .add(ELApplicationCheckerSonarBatchComponent.class)
                                                              .build();
    static final String ARTIFACT_PATH = KEY;
    static final String ARTIFACT_PATH_PROP_DESCRIPTION = "Path to the built artifact to check.";

    /**
     * Returns the extensions this plugin provides.
     * These extensions are:
     * <ul>
     * <li>{@link ELCheckerSensor}</li>
     * <li>{@link ELCheckerRulesRepository}</li>
     * <li>{@link HTMLSourceImporter}</li>
     * <li>{@link ELApplicationCheckerSonarBatchComponent}</li>
     * </ul>
     *
     * @return a list containing the above extensions
     */
    @Override
    public List getExtensions() {
        return PLUGIN_EXTENSIONS;
    }
}
