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
package com.infosupport.ellison.jsf12impl.apiimpl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URI;
import java.util.Collection;

import javax.annotation.Nullable;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.infosupport.ellison.core.api.ELError;
import com.infosupport.ellison.core.api.ELFinder;
import com.infosupport.ellison.core.api.LocationAwareELExpression;
import com.infosupport.ellison.core.archive.ApplicationArchive;
import com.infosupport.ellison.core.archive.WARApplicationArchive;
import com.infosupport.ellison.core.util.Pair;
import com.infosupport.ellison.jsf12impl.apiimpl.JSFELFinder;

/**
 * Tests {@link JSFELFinder}.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class JSFELFinderTest {
    static Collection<URI> pageList;
    static ApplicationArchive applicationArchive;

    @BeforeClass
    public static void setupClass() throws Exception {
        applicationArchive =
            new WARApplicationArchive(new File(JSFELFinderTest.class.getResource("/jsf12-helloworld.war").toURI()));
        pageList = applicationArchive.findFilesByGlobPattern(null, "*.xhtml");
    }

    /**
     * Find all expressions and syntax errors in the jsf12-helloworld.war test resource.
     * <p/>
     * Expected expressions:
     * <ul>
     * <li>
     * bla.xhtml
     * <ul>
     * <li>none</li>
     * </ul>
     * </li>
     * <li>
     * helloWorld.xhtml
     * <ul>
     * <li>{@code helloWorldBacking.name}</li>
     * <li>{@code helloWorldBacking.bladibla}</li>
     * <li>{@code helloWorldBacking.send}</li>
     * </ul>
     * </li>
     * <li>
     * page2.xhtml
     * <ul>
     * <li>{@code helloWorldBacking.name}</li>
     * <li>{@code beanFoo.trueProp}</li>
     * <li>{@code beanBar} (as part of beanBar[falseProp])</li>
     * <li>{@code falseProp} (as part of beanBar[falseProp])</li>
     * </ul>
     * </li>
     * <li>
     * template.xhtml
     * <ul>
     * <li>none</li>
     * </ul>
     * </li>
     * </ul>
     * <p/>
     * Expected errors:<ul>
     * <li>
     * bla.xhtml
     * <ul>
     * <li>Malformed XML</li>
     * </ul>
     * </li>
     * <li>
     * helloWorld.xhtml
     * <ul>
     * <li>
     * {@code faultyExpression[]}<br />
     * Fails to parse.
     * </li>
     * </ul>
     * </li>
     * <li>
     * page2.xhtml
     * <ul>
     * <li>
     * {@code beanBar[falseProp]}, because it uses a dynamic lookup (falseProp is not a constant),
     * and is therefore not typesafe
     * </li>
     * </ul>
     * </li>
     * <li>
     * template.xhtml
     * <ul>
     * <li>none</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @throws Exception
     */
    @Test
    public void testFindELExpressions() throws Exception {
        ELFinder jsfelFinder = new JSFELFinder();
        Pair<Collection<LocationAwareELExpression>, Collection<ELError>> expressionsAndErrorsPair =
            jsfelFinder.findELExpressions(pageList, applicationArchive);

        Collection<LocationAwareELExpression> foundExpressions = expressionsAndErrorsPair.first();
        Collection<ELError> foundErrors = expressionsAndErrorsPair.second();
        Collection<LocationAwareELExpression> foundExpressionsOnlyExpected =
            Collections2.filter(foundExpressions, new Predicate<LocationAwareELExpression>() {
                @Override
                public boolean apply(@Nullable LocationAwareELExpression input) {
                    String expressionString = input.getExpressionString();
                    return
                        (isInHelloWorld(input) && (expressionString.equals("helloWorldBacking.name") || expressionString
                            .equals("helloWorldBacking.send") || expressionString.equals("helloWorldBacking.bladibla")))
                            || (isInPage2(input) && (expressionString.equals("helloWorldBacking.name")
                            || expressionString.equals("beanFoo.trueProp") || expressionString.equals("beanBar")
                            || expressionString.equals("falseProp")));
                }
            });
        Collection<ELError> foundErrorsOnlyExpected = Collections2.filter(foundErrors, new Predicate<ELError>() {
            @Override
            public boolean apply(@Nullable ELError input) {
                return (isInHelloWorld(input.getElExpression()) && input.getElExpression().getExpressionString()
                                                                        .equals("#{faultyExpression[]}")) || (
                    isInBla(input.getElExpression()) && input.getMessage().startsWith("The element type")) || (
                    isInPage2(input.getElExpression()) && input.getMessage()
                                                               .startsWith("This expression contains a dynamic lookup")
                        && input.getType() == ELError.ELErrorSeverity.INFO);
            }
        });

        assertThat(foundExpressions.size(), is(equalTo(7)));
        assertThat(foundExpressionsOnlyExpected.size(), is(equalTo(7)));
        assertThat(foundExpressionsOnlyExpected.containsAll(foundExpressions), is(true));
        assertThat(foundErrors.size(), is(equalTo(3)));
        assertThat(foundErrorsOnlyExpected.size(), is(equalTo(3)));
        assertThat(foundErrorsOnlyExpected.containsAll(foundErrors), is(true));
    }

    private boolean isInHelloWorld(LocationAwareELExpression input) {
        return input.getLocation().getUri().toString().equals("helloWorld.xhtml");
    }

    private boolean isInPage2(LocationAwareELExpression input) {
        return input.getLocation().getUri().toString().equals("page2.xhtml");
    }

    private boolean isInBla(LocationAwareELExpression input) {
        return input.getLocation().getUri().toString().equals("bla.xhtml");
    }
}
