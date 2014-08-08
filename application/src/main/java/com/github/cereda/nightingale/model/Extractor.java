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

import com.github.cereda.nightingale.controller.ConfigurationController;
import com.github.cereda.nightingale.controller.LanguageController;
import com.github.cereda.nightingale.utils.DirectiveUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * It extracts directives from the provided main file.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Extractor {

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    /**
     * Extracts a list of directives from the provided main file, obtained from
     * the configuration controller.
     * @return A list of directives.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public List<Directive> extract() throws NightingaleException {
        
        // get both file and charset from the
        // configuration controller
        File file = (File) ConfigurationController.
                getInstance().
                get("execution.reference");
        Charset charset = (Charset) ConfigurationController.
                getInstance().
                get("directives.charset");

        try {
            
            // let's  try to extract the directives through
            // a helper method from the utilitary classes
            List<String> lines = FileUtils.readLines(file, charset.name());
            return DirectiveUtils.extractDirectives(lines);
            
        } catch (IOException ioexception) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_EXTRACTOR_IO_ERROR
                    ),
                    ioexception
            );
        }
    }

}
