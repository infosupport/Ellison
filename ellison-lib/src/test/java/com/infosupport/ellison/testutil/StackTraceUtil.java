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
package com.infosupport.ellison.testutil;

import java.lang.reflect.Method;

/**
 * Utility functions to work with stacktraces.
 *
 * @author StefanZ
 * @see StackTraceElement
 */
public final class StackTraceUtil {
    /**
     * Private constructor, as this is a utility class.
     */
    private StackTraceUtil() {
    }

    /**
     * Checks that a particular caller is part of the given stacktrace.
     *
     * @param stackTrace
     *     The stacktrace to check for the caller.
     * @param clazz
     *     the calling class to check for (must not be null)
     * @param method
     *     the calling method to check for (may be null)
     *
     * @return if {@code method == null}, then return {@code true} if any method from {@code clazz} exists in the
     *         stacktrace. Otherwise, only return {@code true} if the actual method call exists in the stacktrace.
     *         {@code false} otherwise.
     */
    public static boolean hasCaller(StackTraceElement[] stackTrace, Class clazz, Method method) {
        for (int i = 0; i < stackTrace.length; i++) {
            if (stackTrace[i].getClassName().equals(clazz.getName())) {
                if (method == null || stackTrace[i].getMethodName().equals(method.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Convenience method for {@code hasCaller(stackTrace, clazz, null)}.
     *
     * @param stackTrace
     *     The stacktrace to check for the caller.
     * @param clazz
     *     the calling class to check for (must not be null)
     *
     * @return {@code true} if any method from {@code clazz} exists in the stacktrace, {@code false}
     *
     * @see #hasCaller(StackTraceElement[], Class, java.lang.reflect.Method)
     */
    public static boolean hasCaller(StackTraceElement[] stackTrace, Class clazz) {
        return hasCaller(stackTrace, clazz, null);
    }
}
