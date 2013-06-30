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

/**
 * Utility class to hold two objects in an immutable and type-safe way.
 *
 * @param <F>
 *     type of the first item in the pair
 * @param <S>
 *     type of the second item in the pair
 *
 * @author StefanZ
 */
public class Pair<F, S> {
    private final F first;
    private final S second;

    /**
     * Constructor. Creates a pair consisting of the {@code first} and {@code second} elements.
     *
     * @param first
     *     the first element to hold in this pair
     * @param second
     *     the second element to hold in this pair
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * @return The value of the first element.
     */
    public F first() {
        return first;
    }

    /**
     * @return The value of the second element.
     */
    public S second() {
        return second;
    }
}
