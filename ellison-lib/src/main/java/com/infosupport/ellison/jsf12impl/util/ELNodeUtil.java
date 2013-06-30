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

import com.infosupport.ellison.core.api.ELError;
import com.infosupport.ellison.core.api.LocationAwareELExpression;
import com.infosupport.ellison.core.util.Location;
import com.infosupport.ellison.core.util.Pair;
import com.infosupport.ellison.jsf12impl.apiimpl.JSFELExpression;
import com.sun.el.lang.ExpressionBuilder;
import com.sun.el.parser.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods to work with {@link com.sun.el.parser.Node} instances.
 *
 * @author StefanZ
 * @see Node
 */
public final class ELNodeUtil {
    private static final String EL_EXPRESSION_FORMAT = "#{%s}";

    private ELNodeUtil() {
    }

    /**
     * Extracts all subexpressions from a {@link Node} that can be checked statically.
     * <p/>
     * The method name is a bit of a misnomer, as it doesn't only extract value expressions, but also standalone
     * identifiers.
     * <p/>
     * Example: input expression: {@code someBean[someOtherBean.property]} list of resulting expressions:
     * <ul>
     * <li>{@code someBean} (of type {@code AstIdentifier})</li>
     * <li>{@code someOtherBean.property} (of type {@code AstValue})</li>
     * </ul>
     *
     * @param elNode
     *     the node to extract subexpressions from
     * @param location
     *     the original location of the expression
     *
     * @return a list of all {@link AstValue} and {@link AstIdentifier} subexpressions (in {@link Pair#first()},
     *         and a list of potential errors (in {@link Pair#second()}. All other subexpressions are discarded.
     *
     * @see AstValue
     * @see AstIdentifier
     */
    public static Pair<List<LocationAwareELExpression>, List<ELError>> extractCheckableExpressions(Node elNode,
                                                                                                   Location location) {
        List<LocationAwareELExpression> expressions = new ArrayList<>();
        List<ELError> errors = new ArrayList<>();

        if (elNode instanceof AstValue) {
            Pair<List<LocationAwareELExpression>, List<ELError>> foundExpressionsAndErrors
                = extractCheckableExpressions((AstValue) elNode, location);
            expressions.addAll(foundExpressionsAndErrors.first());
            errors.addAll(foundExpressionsAndErrors.second());
        } else if (elNode instanceof AstIdentifier) {
            LocationAwareELExpression jsfELExpression = new JSFELExpression(elNode, location);
            expressions.add(jsfELExpression);
        } else {
            for (int i = 0; i < elNode.jjtGetNumChildren(); i++) {
                Pair<List<LocationAwareELExpression>, List<ELError>> foundValueExpressionsAndErrors
                    = extractCheckableExpressions(elNode.jjtGetChild(i), location);
                expressions.addAll(foundValueExpressionsAndErrors.first());
                errors.addAll(foundValueExpressionsAndErrors.second());
            }
        }

        return new Pair<List<LocationAwareELExpression>, List<ELError>>(expressions, errors);
    }

    /**
     * Extracts the parts of the {@link AstValue} that can be statically checked.
     * This means that expressions containing a bracket suffix, which in turn contain a non-literal expression,
     * will be split up, and an "error" will indicate that this happened.
     * <p/>
     * Example:
     * {@code elNode} expression string: {@code bean.propA['propB'][propC].propD}. This will result in an error
     * indicating that the part {@code [propC]} means that a dynamic lookup would have to happen,
     * and the following expressions would then be returned:
     * <ul>
     * <li>{@code bean.propA['propB']}</li>
     * <li>{@code propC}</li>
     * </ul>
     * Note that this means {@code propD} will be dropped entirely.
     *
     * @param elNode
     *     the node to extract subexpressions from
     * @param location
     *     the original location of the expression
     *
     * @return a list of expressions that can be checked, and a list of found errors.
     */
    protected static Pair<List<LocationAwareELExpression>, List<ELError>> extractCheckableExpressions(AstValue elNode,
                                                                                                      Location
                                                                                                          location) {
        List<LocationAwareELExpression> expressions = new ArrayList<>();
        List<ELError> errors = new ArrayList<>();
        LocationAwareELExpression jsfELExpression = new JSFELExpression(elNode, location);
        boolean addExpression = true;

        for (int i = 0; i < elNode.jjtGetNumChildren(); i++) {
            if (elNode.jjtGetChild(i) instanceof AstBracketSuffix && (elNode.jjtGetChild(i).jjtGetNumChildren() > 1
                || !(elNode.jjtGetChild(i).jjtGetChild(0) instanceof AstString))) {
                String expressionBeforeBracket = String
                    .format(EL_EXPRESSION_FORMAT, composeNodeImage(elNode, elNode.jjtGetChild(i)));

                String expressionInsideBrackets = String
                    .format(EL_EXPRESSION_FORMAT, composeNodeImage(elNode.jjtGetChild(i).jjtGetChild(0)));

                errors.add(new ELError(String.format(
                    "This expression contains a dynamic lookup for a property, which is dependent upon a runtime "
                        + "value, and can therefore not be statically checked. Instead, expression '%s', "
                        + "and all extracted expressions in '%s' will be checked.", expressionBeforeBracket,
                    expressionInsideBrackets), jsfELExpression, ELError.ELErrorSeverity.INFO));

                jsfELExpression = new JSFELExpression(ExpressionBuilder
                    .createNode(String.format(EL_EXPRESSION_FORMAT, composeNodeImage(elNode, elNode.jjtGetChild(i)))),
                    location);
                expressions.add(jsfELExpression);

                Pair<List<LocationAwareELExpression>, List<ELError>> foundExpressionsAndErrors
                    = extractCheckableExpressions(elNode.jjtGetChild(i), location);

                expressions.addAll(foundExpressionsAndErrors.first());
                errors.addAll(foundExpressionsAndErrors.second());

                addExpression = false;
            }
        }

        if (addExpression) {
            expressions.add(jsfELExpression);
        }

        return new Pair<>(expressions, errors);
    }

    /**
     * Creates a {@code String} representation of a {@link Node}.
     * Convenience method that calls {@link #composeNodeImage(Node, Node)}.
     *
     * @param node
     *     the node to create a {@code String} representation for
     *
     * @return if {@code node.getImage() != null}, then return {@code node.getImage()}; otherwise, creates a new {@code
     *         String} representation of the expression.
     */
    public static String composeNodeImage(Node node) {
        return composeNodeImage(node, null);
    }

    /**
     * Creates a {@code String} representation of a {@link Node}.
     * <p/>
     * The idea is to get a representation that is equal to, or as close as possible to the original expression string.
     *
     * @param node
     *     the node to create a {@code String} representation for
     * @param untilNode
     *     the node at which to stop composing the image. This parameter may be {@code null},
     *     which means the entire image will be composed. If it is not {@code null},
     *     it will be compared against every child of {@code node}, and if it matches,
     *     the image composition stops there.
     *
     * @return if {@code node.getImage() != null}, then return {@code node.getImage()}; otherwise, creates a new {@code
     *         String} representation of the expression.
     */
    public static String composeNodeImage(Node node, Node untilNode) {
        String nodeImage = node.getImage();

        if (node.equals(untilNode)) {
            return "";
        }
        if (nodeImage == null) {
            if (node instanceof AstBracketSuffix) {
                nodeImage = composeNodeImage((AstBracketSuffix) node, untilNode);
            } else if (node instanceof AstFunction) {
                nodeImage = composeNodeImage((AstFunction) node, untilNode);
            } else {
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                    if (node.jjtGetChild(i) == untilNode) {
                        break;
                    }
                    stringBuilder.append(composeNodeImage(node.jjtGetChild(i), untilNode));
                }

                nodeImage = stringBuilder.toString();
            }
        } else if (node instanceof AstPropertySuffix) {
            nodeImage = "." + nodeImage;
        }

        return nodeImage;
    }

    /**
     * Creates a {@code String} representation of an {@link AstBracketSuffix} (example: {@code [property]}).
     *
     * @param node
     *     the node to create a string representation for
     *
     * @return the string representation of the node
     */
    private static String composeNodeImage(AstBracketSuffix node, Node untilNode) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append('[');

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            stringBuilder.append(composeNodeImage(node.jjtGetChild(i), untilNode));
        }

        stringBuilder.append(']');

        return stringBuilder.toString();
    }

    /**
     * Creates a {@code String} representation of an {@link AstFunction} (example:
     * {@code namespace:function(paramOne, paramTwo)}).
     *
     * @param node
     *     the node to create a string representation for
     *
     * @return the string representation of the node
     */
    private static String composeNodeImage(AstFunction node, Node untilNode) {
        StringBuilder stringBuilder = new StringBuilder();
        String functionPrefix = node.getPrefix();
        String functionLocalName = node.getLocalName();

        if (functionPrefix != null && functionPrefix.length() > 0) {
            stringBuilder.append(functionPrefix);
            stringBuilder.append(':');
        }

        stringBuilder.append(functionLocalName);
        stringBuilder.append('(');

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(composeNodeImage(node.jjtGetChild(i), untilNode));
        }

        stringBuilder.append(')');

        return stringBuilder.toString();
    }
}
