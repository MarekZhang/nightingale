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
package com.github.cereda.nightingale.utils;

import com.github.cereda.nightingale.controller.ConfigurationController;
import com.github.cereda.nightingale.controller.LanguageController;
import com.github.cereda.nightingale.model.NightingaleException;
import com.github.cereda.nightingale.model.Conditional;
import com.github.cereda.nightingale.model.Directive;
import com.github.cereda.nightingale.model.Messages;
import com.github.cereda.nightingale.model.Pair;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Implements directive utilitary methods.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class DirectiveUtils {

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();
    
    // get the logger context from a factory
    private static final Logger logger =
            LoggerFactory.getLogger(DirectiveUtils.class);

    /**
     * Validates the list of directives, returning a new list.
     * @param directives The list of directives.
     * @return A new list of directives.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public static List<Directive> validate(List<Directive> directives)
            throws NightingaleException {
        ArrayList<Directive> result = new ArrayList<Directive>();
        for (Directive directive : directives) {
            Map<String, Object> parameters = directive.getParameters();

            // checks if the 'file' keyword was
            // used; if so, throw an exception,
            // since this is forbidden
            if (parameters.containsKey("file")) {
                throw new NightingaleException(
                        messages.getMessage(
                                Messages.ERROR_VALIDATE_FILE_IS_RESERVED,
                                CommonUtils.getCollectionElements(
                                        directive.getLineNumbers(),
                                        "(",
                                        ")",
                                        ", "
                                )
                        )
                );
            }

            // the 'files' key is allowed, but the user
            // must follow some guidelines: it must be
            // a non-empty list; each element will be
            // converted to a file and used as 'file'
            // element for a cloned directive in the
            // resulting list
            if (parameters.containsKey("files")) {
                Object holder = parameters.get("files");
                if (holder instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> files = (List<Object>) holder;
                    parameters.remove("files");

                    if (files.isEmpty()) {
                        throw new NightingaleException(
                                messages.getMessage(
                                        Messages.ERROR_VALIDATE_EMPTY_FILES_LIST,
                                        CommonUtils.getCollectionElements(
                                                directive.getLineNumbers(),
                                                "(",
                                                ")",
                                                ", "
                                        )
                                )
                        );
                    }

                    for (Object file : files) {

                        Map<String, Object> map = new HashMap<String, Object>();
                        for (String key : parameters.keySet()) {
                            map.put(key, parameters.get(key));
                        }
                        File representation = CommonUtils.
                                getCanonicalFile(String.valueOf(file));
                        map.put("file", representation);

                        Directive addition = new Directive();

                        Conditional conditional = new Conditional();
                        conditional.setCondition(directive.
                                getConditional().
                                getCondition()
                        );
                        conditional.setType(directive.
                                getConditional().
                                getType()
                        );

                        addition.setIdentifier(directive.getIdentifier());
                        addition.setConditional(conditional);
                        addition.setParameters(map);
                        addition.setLineNumbers(directive.getLineNumbers());

                        result.add(addition);

                    }
                } else {
                    throw new NightingaleException(
                            messages.getMessage(
                                    Messages.ERROR_VALIDATE_FILES_IS_NOT_A_LIST,
                                    CommonUtils.getCollectionElements(
                                            directive.getLineNumbers(),
                                            "(",
                                            ")",
                                            ", "
                                    )
                            )
                    );
                }
            } else {
                parameters.put("file", ConfigurationController.
                        getInstance().
                        get("execution.reference")
                );
                directive.setParameters(parameters);

                result.add(directive);
            }
        }

        logger.info(
                messages.getMessage(Messages.LOG_INFO_VALIDATED_DIRECTIVES)
        );
        logger.info(
                DisplayUtils.displayOutputSeparator(
                        messages.getMessage(
                                Messages.LOG_INFO_DIRECTIVES_BLOCK
                        )
                )
        );
        for (Directive directive : result) {
            logger.info(directive.toString());
        }
        logger.info(DisplayUtils.displaySeparator());

        return result;
    }

    /**
     * Extracts a list of directives from a list of strings.
     * @param lines List of strings.
     * @return A list of directives.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public static List<Directive> extractDirectives(List<String> lines)
            throws NightingaleException {

        String regex = (String) ConfigurationController.
                getInstance().
                get("execution.file.pattern");
        regex = regex.concat((String) ConfigurationController.
                getInstance().
                get("application.pattern"));
        Pattern pattern = Pattern.compile(regex);
        List<Pair<Integer, String>> pairs =
                new ArrayList<Pair<Integer, String>>();
        Matcher matcher;
        for (int i = 0; i < lines.size(); i++) {
            matcher = pattern.matcher(lines.get(i));
            if (matcher.find()) {
                String line = lines.get(i).substring(
                        matcher.end(),
                        lines.get(i).length());
                Pair<Integer, String> pair =
                        new Pair<Integer, String>(i + 1, line);
                pairs.add(pair);

                logger.info(
                        messages.getMessage(
                                Messages.LOG_INFO_POTENTIAL_PATTERN_FOUND,
                                (i + 1),
                                line.trim()
                        )
                );

            }
        }

        if (pairs.isEmpty()) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_VALIDATE_NO_DIRECTIVES_FOUND
                    )
            );
        }

        List<DirectiveAssembler> assemblers =
                new ArrayList<DirectiveAssembler>();
        DirectiveAssembler assembler = new DirectiveAssembler();
        regex = (String) ConfigurationController.
                getInstance().
                get("directives.linebreak.pattern");
        pattern = Pattern.compile(regex);
        for (Pair<Integer, String> pair : pairs) {
            matcher = pattern.matcher(pair.getSecondElement());
            if (matcher.find()) {
                if (!assembler.isAppendAllowed()) {
                    throw new NightingaleException(
                            messages.getMessage(
                                    Messages.ERROR_VALIDATE_ORPHAN_LINEBREAK,
                                    pair.getFirstElement()
                            )
                    );
                } else {
                    assembler.addLineNumber(pair.getFirstElement());
                    assembler.appendLine(matcher.group(1));
                }
            } else {
                if (assembler.isAppendAllowed()) {
                    assemblers.add(assembler);
                }
                assembler = new DirectiveAssembler();
                assembler.addLineNumber(pair.getFirstElement());
                assembler.appendLine(pair.getSecondElement());
            }
        }
        if (assembler.isAppendAllowed()) {
            assemblers.add(assembler);
        }

        List<Directive> directives = new ArrayList<Directive>();
        for (DirectiveAssembler current : assemblers) {
            directives.add(generateDirective(current));
        }

        return directives;

    }

    /**
     * Generates a directive from a directive assembler.
     * @param assembler The directive assembler.
     * @return The corresponding directive.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public static Directive generateDirective(DirectiveAssembler assembler)
            throws NightingaleException {
        String regex = (String) ConfigurationController.
                getInstance().
                get("directives.pattern");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(assembler.getText());
        if (matcher.find()) {
            Directive directive = new Directive();
            directive.setIdentifier(matcher.group(1));
            directive.setParameters(
                    getParameters(matcher.group(3),
                            assembler.getLineNumbers()
                    )
            );
            Conditional conditional = new Conditional();
            conditional.setType(getType(matcher.group(5)));
            conditional.setCondition(getCondition(matcher.group(6)));
            directive.setConditional(conditional);
            directive.setLineNumbers(assembler.getLineNumbers());

            logger.info(
                    messages.getMessage(
                            Messages.LOG_INFO_POTENTIAL_DIRECTIVE_FOUND,
                            directive
                    )
            );

            return directive;

        } else {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_VALIDATE_INVALID_DIRECTIVE_FORMAT,
                            CommonUtils.getCollectionElements(
                                    assembler.getLineNumbers(),
                                    "(",
                                    ")",
                                    ", "
                            )
                    )
            );
        }

    }

    /**
     * Gets the conditional type based on the input string.
     * @param text The input string.
     * @return The conditional type.
     */
    private static Conditional.ConditionalType getType(String text) {
        if (text == null) {
            return Conditional.ConditionalType.NONE;
        } else {
            if (text.equals("if")) {
                return Conditional.ConditionalType.IF;
            } else {
                if (text.equals("while")) {
                    return Conditional.ConditionalType.WHILE;
                } else {
                    if (text.equals("until")) {
                        return Conditional.ConditionalType.UNTIL;
                    } else {
                        return Conditional.ConditionalType.UNLESS;
                    }
                }
            }
        }
    }

    /**
     * Gets the condition from the input string.
     * @param text The input string.
     * @return A string representing the condition.
     */
    private static String getCondition(String text) {
        return text == null ? "" : text;
    }

    /**
     * Gets the parameters from the input string.
     * @param text The input string.
     * @param numbers The list of line numbers.
     * @return A map containing the directive parameters.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    private static Map<String, Object> getParameters(String text,
            List<Integer> numbers) throws NightingaleException {
        if (text == null) {
            return new HashMap<String, Object>();
        }

        Yaml yaml = new Yaml(
                new Constructor(),
                new Representer(),
                new DumperOptions(),
                new DirectiveResolver()
        );
        try {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> map = yaml.loadAs(text, HashMap.class);
            return map;
        } catch (MarkedYAMLException exception) {
            throw new NightingaleException(
                    messages.getMessage(
                            Messages.ERROR_VALIDATE_YAML_EXCEPTION,
                            CommonUtils.getCollectionElements(
                                    numbers,
                                    "(",
                                    ")",
                                    ", "
                            )
                    ),
                    exception
            );
        }
    }

}
