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

import java.lang.reflect.Method;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosupport.ellison.core.api.ELError;
import com.infosupport.ellison.core.api.LocationAwareELExpression;
import com.infosupport.ellison.core.exceptions.ELCheckerException;
import com.infosupport.ellison.core.util.Location;
import com.infosupport.ellison.jsf12impl.util.ELNodeUtil;
import com.sun.el.lang.EvaluationContext;
import com.sun.el.parser.Node;

/**
 * JSF 1.2 specific implementation of {@link LocationAwareELExpression}.
 *
 * @author StefanZ
 */
public class JSFELExpression extends LocationAwareELExpression {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSFELExpression.class);

    private Node expressionNode;

    /**
     * Constructor.
     *
     * @param expressionNode
     *     the {@link Node} representing an expression
     * @param location
     *     the location of the expression
     */
    public JSFELExpression(Node expressionNode, Location location) {
        super(location);

        this.expressionNode = expressionNode;
    }

    /**
	 * Resolve the value the expression refers to. In addition to what the interface prescribes, this implementation adds the
	 * current expression to the {@link ELContext} it passes to {@link Node#getValue(com.sun.el.lang.EvaluationContext)}. This can
	 * then be used from within {@link com.infosupport.ellison.core.api.StaticELResolver} implementations whenever
	 * they need to throw an {@link ELException} to indicate some kind of failure to resolve.
	 * 
	 * Can throw runtime ELCheckerException when this expression can or will lead to problems at runtime.
	 * 
	 * @param resolver
	 *            the resolver to use to resolve the value of the expression
	 * 
	 * @return whatever value this expression resolves to.
	 * 
	 */
    @Override
    public Object getValue(ELResolver resolver) {
        Object resultValue = null;
        ELContext elContext = new JSFELContext(resolver);
        elContext.putContext(JSFELExpression.class, this);

        try {
            resultValue = expressionNode.getValue(new EvaluationContext(elContext, JSFELContext.FUNCTION_MAPPER,
                    JSFELContext.VARIABLE_MAPPER));
        } catch (ELException e) {
            LOGGER.error("Resolving the value of the EL expression failed.", e);
            throw new ELCheckerException(e.getMessage(), this, ELError.ELErrorSeverity.ERROR);
        }

        if (!elContext.isPropertyResolved()) {
            throw new ELCheckerException("Expression could not be resolved.", this, ELError.ELErrorSeverity.ERROR);
        }

        return resultValue;
    }

    @Override
    public String getExpressionString() {
        return ELNodeUtil.composeNodeImage(expressionNode);
    }

    /**
     * This context is to be used when resolving expressions. Simply create an instance,
     * passing in the resolver to be used. It has a {@link FunctionMapper} and a {@link VariableMapper},
     * which both simply fail to resolve functions (i.e. {@code #{namespace:functionName(parameterOne,
     * parameterTwo)}}) and variables.
     */
    public static class JSFELContext extends ELContext {
        static final FunctionMapper FUNCTION_MAPPER = new NoOpFunctionMapper();
        static final VariableMapper VARIABLE_MAPPER = new NoOpVariableMapper();
        private ELResolver elResolver;

        /**
         * Constructor.
         *
         * @param elResolver
         *     the top-level resolver to use in this context. It may be a simple resolver, or a composite one.
         */
        public JSFELContext(ELResolver elResolver) {
            this.elResolver = elResolver;
        }

        @Override
        public ELResolver getELResolver() {
            return elResolver;
        }

        @Override
        public FunctionMapper getFunctionMapper() {
            return FUNCTION_MAPPER;
        }

        @Override
        public VariableMapper getVariableMapper() {
            return VARIABLE_MAPPER;
        }
    }

    private static class NoOpFunctionMapper extends FunctionMapper {
        @Override
        public Method resolveFunction(String prefix, String localName) {
            return null;
        }
    }

    private static class NoOpVariableMapper extends VariableMapper {

        @Override
        public ValueExpression resolveVariable(String variable) {
            return null;
        }

        @Override
        public ValueExpression setVariable(String variable, ValueExpression expression) {
            return null;
        }
    }
}

