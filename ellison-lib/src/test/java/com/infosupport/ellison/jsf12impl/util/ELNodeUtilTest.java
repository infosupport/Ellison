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
package com.infosupport.ellison.jsf12impl.util;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.infosupport.ellison.core.api.ELError;
import com.infosupport.ellison.core.api.LocationAwareELExpression;
import com.infosupport.ellison.core.util.Location;
import com.infosupport.ellison.core.util.Pair;
import com.infosupport.ellison.jsf12impl.util.ELNodeUtil;
import com.sun.el.lang.ExpressionBuilder;
import com.sun.el.parser.AstFunction;
import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.Node;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link ELNodeUtil} class.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class ELNodeUtilTest {

    /**
     * Tests whether the {@link ELNodeUtil#extractCheckableExpressions(com.sun.el.parser.Node,
     * com.infosupport.ellison.core.util.Location)} method does indeed extract all value and
     * identifier expressions.
     *
     * @throws Exception
     */
    @Test
    public void testExtractValueExpressions() throws Exception {
        String expression = "#{identifier - valueExpOne.propOne[propTwo].propThree + \"literalExp\" && " +
            "namespace:functionOne"
            + "(paramOne, paramTwo) ? functionTwo(paramThree) : (5 div 2 + someInteger) mod valueExpTwo['propFour']}";
        Node elNode = ExpressionBuilder.createNode(expression);

        Pair<List<LocationAwareELExpression>, List<ELError>> locationAwareELExpressionsAndErrors =
            ELNodeUtil.extractCheckableExpressions(elNode, mock(Location.class));
        Collection<String> extractedExpressionStrings = Collections2
            .transform(locationAwareELExpressionsAndErrors.first(), new Function<LocationAwareELExpression, String>() {
                @Nullable
                @Override
                public String apply(@Nullable LocationAwareELExpression input) {
                    return input.getExpressionString();
                }
            });

        assertThat(locationAwareELExpressionsAndErrors.first().size(), is(equalTo(8)));
        assertThat(extractedExpressionStrings.contains("identifier"), is(true));
        assertThat(extractedExpressionStrings.contains("valueExpOne.propOne"), is(true));
        assertThat(extractedExpressionStrings.contains("propTwo"), is(true));
        assertThat(extractedExpressionStrings.contains("paramOne"), is(true));
        assertThat(extractedExpressionStrings.contains("paramTwo"), is(true));
        assertThat(extractedExpressionStrings.contains("paramThree"), is(true));
        assertThat(extractedExpressionStrings.contains("someInteger"), is(true));
        assertThat(extractedExpressionStrings.contains("propThree"), is(false));
        assertThat(extractedExpressionStrings.contains("valueExpTwo['propFour']"), is(true));
    }

    @Test
    public void testComposeNodeImage_Identifier() throws Exception {
        String identifier = "identifier";
        String identifierExpression = "#{" + identifier + "}";
        Node elNode = ExpressionBuilder.createNode(identifierExpression);

        assertThat(elNode, is(instanceOf(AstIdentifier.class)));
        assertThat(ELNodeUtil.composeNodeImage(elNode), is(equalTo(identifier)));
    }

    @Test
    public void testComposeNodeImage_Function() throws Exception {
        String functionWithoutNamespace = "function(paramOne, paramTwo)";
        String functionWithNamespace = "namespace:" + functionWithoutNamespace;
        String functionWithoutNamespaceExpression = "#{" + functionWithoutNamespace + "}";
        String functionWithNamespaceExpression = "#{" + functionWithNamespace + "}";
        Node elNodeWithoutNamespace = ExpressionBuilder.createNode(functionWithoutNamespaceExpression);
        Node elNodeWithNamespace = ExpressionBuilder.createNode(functionWithNamespaceExpression);

        assertThat(elNodeWithoutNamespace, is(instanceOf(AstFunction.class)));
        assertThat(ELNodeUtil.composeNodeImage(elNodeWithoutNamespace), is(equalTo(functionWithoutNamespace)));
        assertThat(elNodeWithNamespace, is(instanceOf(AstFunction.class)));
        assertThat(ELNodeUtil.composeNodeImage(elNodeWithNamespace), is(equalTo(functionWithNamespace)));
    }

    @Test
    public void testComposeNodeImage_WithBracketSuffix() throws Exception {
        String identifierAndBracketSuffix = "identifier[bracketSuffix]";
        String identifierAndBracketSuffixExpression = "#{" + identifierAndBracketSuffix + "}";
        Node elNode = ExpressionBuilder.createNode(identifierAndBracketSuffixExpression);

        assertThat(ELNodeUtil.composeNodeImage(elNode), is(equalTo(identifierAndBracketSuffix)));
    }
}
