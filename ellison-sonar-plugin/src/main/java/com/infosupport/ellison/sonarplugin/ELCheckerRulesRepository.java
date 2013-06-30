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
import com.infosupport.ellison.sonarplugin.rules.*;

import java.util.List;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;

/**
 * A repository for all the different "rules" this plugin supports.
 *
 * @author StefanZ
 * @see Rule
 */
public class ELCheckerRulesRepository extends RuleRepository {
    static final String REPOSITORY_KEY = "el-static-checker-rule-repository";
    static final ImmutableList<Class> AVAILABLE_RULES = ImmutableList.<Class>builder()
                                                                     .add(ELCheckerInfoRule.class)
                                                                     .add(ELCheckerMinorRule.class)
                                                                     .add(ELCheckerMajorRule.class)
                                                                     .add(ELCheckerCriticalRule.class)
                                                                     .add(ELCheckerBlockerRule.class)
                                                                     .build();
    private AnnotationRuleParser annotationRuleParser;

    /**
     * Constructor.
     * Note that the parameter would normally be provided by Sonar's Inversion of Control container.
     *
     * @param annotationRuleParser
     *     the parser to be used to find the rule parameters
     */
    public ELCheckerRulesRepository(AnnotationRuleParser annotationRuleParser) {
        super(REPOSITORY_KEY, "java");
        setName("Ellison");
        this.annotationRuleParser = annotationRuleParser;
    }

    /**
     * Creates a list of rules that this plugin supports.
     *
     * @return the list of rules supported by this plugin
     */
    @Override
    public List<Rule> createRules() {
        return annotationRuleParser.parse(REPOSITORY_KEY, AVAILABLE_RULES);
    }
}
