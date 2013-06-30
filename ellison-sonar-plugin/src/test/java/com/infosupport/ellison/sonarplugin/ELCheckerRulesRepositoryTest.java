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

import com.google.common.collect.ImmutableMap;
import com.infosupport.ellison.sonarplugin.ELCheckerRulesRepository;
import com.infosupport.ellison.sonarplugin.rules.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test the {@link ELCheckerRulesRepository} class.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class ELCheckerRulesRepositoryTest {
    static final Map<String, RulePriority> RULEKEY_TO_PRIORITY_MAP = ImmutableMap.<String, RulePriority>builder()
                                                                                 .put(ELCheckerInfoRule.KEY,
                                                                                      RulePriority.INFO)
                                                                                 .put(ELCheckerMinorRule.KEY,
                                                                                      RulePriority.MINOR)
                                                                                 .put(ELCheckerMajorRule.KEY,
                                                                                      RulePriority.MAJOR)
                                                                                 .put(ELCheckerBlockerRule.KEY,
                                                                                      RulePriority.BLOCKER)
                                                                                 .put(ELCheckerCriticalRule.KEY,
                                                                                      RulePriority.CRITICAL)
                                                                                 .build();
    ELCheckerRulesRepository rulesRepository;

    @Before
    public void setUp() throws Exception {
        rulesRepository = new ELCheckerRulesRepository(new AnnotationRuleParser());
    }

    /**
     * Make sure that a rule is created for each rule "implementation".
     */
    @Test
    public void testCreateRules() throws Exception {
        List<Rule> createdRules = rulesRepository.createRules();
        Set<String> ruleKeys = RULEKEY_TO_PRIORITY_MAP.keySet();

        assertThat(createdRules.size(), is(ELCheckerRulesRepository.AVAILABLE_RULES.size()));
        for (Rule createdRule : createdRules) {
            assertThat(ruleKeys.contains(createdRule.getKey()), is(true));
            assertThat(RULEKEY_TO_PRIORITY_MAP.get(createdRule.getKey()), is(equalTo(createdRule.getSeverity())));
        }
    }
}
