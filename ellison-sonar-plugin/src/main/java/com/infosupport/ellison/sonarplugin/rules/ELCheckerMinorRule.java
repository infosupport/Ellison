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
package com.infosupport.ellison.sonarplugin.rules;

import org.sonar.check.Priority;
import org.sonar.check.Rule;

/**
 * Rule with a <em>Minor</em> severity indicating an Expression Language error or problem.
 */
@Rule(
    key = ELCheckerMinorRule.KEY,
    name = ELCheckerMinorRule.NAME,
    description = ELCheckerRule.RULE_DESCRIPTION,
    priority = Priority.MINOR)
public abstract class ELCheckerMinorRule extends ELCheckerRule {
    /**
     * The key for this rule.
     * Use this value whenever you need to create a reference to this rule. See the following example:
     * <code>Rule rule = Rule.create(someRulesRepositoryKey, ELCheckerMinorRule.KEY);</code>
     */
    public static final String KEY = "el-static-checker-rule-minor";
    static final String NAME = "Expression Language error/problem (minor severity)";
}
