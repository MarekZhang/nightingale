/**
 * Nightingale
 * Copyright (c) 2014, Paulo Roberto Massa Cereda 
 * All rights reserved.
 *
 * Redistribution and  use in source  and binary forms, with  or without
 * modification, are  permitted provided  that the  following conditions
 * are met:
 *
 * 1. Redistributions  of source  code must  retain the  above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form  must reproduce the above copyright
 * notice, this list  of conditions and the following  disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither  the name  of the  project's author nor  the names  of its
 * contributors may be used to  endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE COPYRIGHT  HOLDERS AND CONTRIBUTORS
 * "AS IS"  AND ANY  EXPRESS OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED  TO, THE  IMPLIED WARRANTIES  OF MERCHANTABILITY  AND FITNESS
 * FOR  A PARTICULAR  PURPOSE  ARE  DISCLAIMED. IN  NO  EVENT SHALL  THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE  LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT  NOT LIMITED  TO, PROCUREMENT  OF SUBSTITUTE  GOODS OR  SERVICES;
 * LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)  HOWEVER
 * CAUSED AND  ON ANY THEORY  OF LIABILITY, WHETHER IN  CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY  OUT  OF  THE USE  OF  THIS  SOFTWARE,  EVEN  IF ADVISED  OF  THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.cereda.nightingale.model;

import com.github.cereda.nightingale.utils.CommonUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implements a command model, containing a list of strings.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Command {
    
    // a list of elements which are components
    // of a command and represented as strings
    private final List<String> elements;

    /**
     * Constructor.
     * @param values An array of objects.
     */
    public Command(Object... values) {
        elements = new ArrayList<String>();
        List result = CommonUtils.flatten(Arrays.asList(values));
        for (Object value : result) {
            String element = String.valueOf(value);
            if (!CommonUtils.checkEmptyString(element)) {
                elements.add(element);
            }
        }
    }

    /**
     * Constructor.
     * @param elements A list of strings.
     */
    public Command(List<String> elements) {
        this.elements = elements;
    }

    /**
     * Gets the list of strings representing each element of a command.
     * @return A list of strings.
     */
    public List<String> getElements() {
        return elements;
    }

    /**
     * Provides a textual representation of the current command.
     * @return A string representing the current command.
     */
    @Override
    public String toString() {
        return CommonUtils.getCollectionElements(elements, "[ ", " ]", ", ");
    }
    
}
