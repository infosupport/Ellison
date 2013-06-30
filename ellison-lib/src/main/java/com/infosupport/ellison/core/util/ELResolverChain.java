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
package com.infosupport.ellison.core.util;

import com.infosupport.ellison.core.api.StaticELResolver;

import java.util.ArrayList;
import java.util.List;
import javax.el.ELContext;

/**
 * This class chains together other {@link com.infosupport.ellison.core.api.StaticELResolver}s.\
 * <p/>
 * All child resolvers will be tried to resolve a particular query, until either {@code context.isPropertyResolved()}
 * becomes true, or all chain resolvers failed to resolve the query.
 * <p/>
 * Note that child resolvers are queried in the order they were added.
 *
 * @author StefanZ
 */
public class ELResolverChain extends StaticELResolver {
    private List<StaticELResolver> elResolverList;

    /**
     * No-argument constructor.
     */
    public ELResolverChain() {
        elResolverList = new ArrayList<StaticELResolver>();
    }

    /**
     * Adds a resolver to the list of this chain's children.
     *
     * @param childResolver
     *     The resolver to add.
     */
    public void addResolver(StaticELResolver childResolver) {
        elResolverList.add(childResolver);
    }

	/**
	 * Tries all child resolvers (in order) to resolve the query. All children are tried, until one succeeds in resolving, or until
	 * no more children are left to try.
	 * 
	 * Can throw a runtime ELCheckerException when this expression can or will lead to problems at runtime. This exception contains
	 * an instanceof {@link com.infosupport.ellison.core.api.ELError} which indicates what kind of problem was
	 * found.
	 * 
	 * @param context
	 *            the context under which to try resolving
	 * @param base
	 *            the base object of the property. This resolver only handles bases that are instances of {@code Class}.
	 * @param property
	 *            the property to look for in {@code base}
	 * 
	 * @return {@code base} if the query couldn't be resolved, the class of the found property otherwise.
	 * 
	 */
    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        Object result = null;

        for (StaticELResolver staticELResolver : elResolverList) {
            result = staticELResolver.getValue(context, base, property);

            if (context.isPropertyResolved()) {
                break;
            }
        }

        if (!context.isPropertyResolved()) {
            result = base;
        }

        return result;
    }
}
