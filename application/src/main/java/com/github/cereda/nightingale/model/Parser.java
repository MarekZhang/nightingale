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
import com.github.cereda.nightingale.controller.LoggingController;
import com.github.cereda.nightingale.utils.CommonUtils;
import com.github.cereda.nightingale.utils.DisplayUtils;
import java.util.Locale;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Implements the command line parser.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Parser {

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    // command line arguments to be
    // processed by this parser
    private final String[] arguments;
    
    // command line options, it will
    // group each option available
    // in nightingale
    private Options options;

    // each option available in
    // nightingale
    private Option version;
    private Option help;
    private Option log;
    private Option verbose;
    private Option dryrun;
    private Option timeout;
    private Option language;
    private Option loops;

    /**
     * Constructor.
     * @param arguments Array of strings representing the command line
     * arguments.
     */
    public Parser(String[] arguments) {
        this.arguments = arguments;
    }

    /**
     * Parses the command line arguments.
     * @return A boolean value indicating if the parsing should allow the
     * application to look for directives in the provided main file.
     * @throws NightingaleException Something wrong happened, to be caught in
     * the higher levels.
     */
    public boolean parse() throws NightingaleException {

        // create new instances of the
        // command line options, including
        // the ones that require arguments
        version = new Option("V", "version", false, "");
        help = new Option("h", "help", false, "");
        log = new Option("l", "log", false, "");
        verbose = new Option("v", "verbose", false, "");
        dryrun = new Option("n", "dry-run", false, "");
        timeout = new Option("t", "timeout", true, "");
        timeout.setArgName("number");
        language = new Option("L", "language", true, "");
        language.setArgName("code");
        loops = new Option("m", "max-loops", true, "");
        loops.setArgName("number");

        // add all options to the options
        // group, so they are recognized
        // by the command line parser
        options = new Options();
        options.addOption(version);
        options.addOption(help);
        options.addOption(log);
        options.addOption(verbose);
        options.addOption(dryrun);
        options.addOption(timeout);
        options.addOption(language);
        options.addOption(loops);

        // update all descriptions based
        // on the localized messages
        updateDescriptions();

        // a new basic command line
        // parser is created and the
        // arguments are parsed
        CommandLineParser parser = new BasicParser();
        
        try {
            
            CommandLine line = parser.parse(options, arguments);
            String reference;

            // there is a language option, get
            // the argument and validate it
            if (line.hasOption("language")) {
                ConfigurationController.
                        getInstance().
                        put("execution.language",
                                new Language(line.getOptionValue("language")));
                Locale locale =
                        ((Language) ConfigurationController.
                                getInstance().
                                get("execution.language")).getLocale();
                messages.setLocale(locale);
                updateDescriptions();
            }

            // there is a help option, 
            // print info and return false,
            // so the application should
            // gracefully exit
            if (line.hasOption("help")) {
                printVersion();
                printUsage();
                return false;
            }

            // there is a version option,
            // print info and return false,
            // so the application should
            // gracefully exit
            if (line.hasOption("version")) {
                printVersion();
                printNotes();
                return false;
            }

            // nightingale expects only one file
            // to be processed at a time, so if
            // there are no files or more than
            // one file, print info and return
            // false, so the application should
            // gracefully exit
            if (line.getArgs().length != 1) {
                printVersion();
                printUsage();
                return false;
            } else {
                reference = line.getArgs()[0];
            }

            // there is a timeout option, get
            // the argument and validate it
            if (line.hasOption("timeout")) {
                try {
                    long value = Long.parseLong(line.getOptionValue("timeout"));
                    if (value <= 0) {
                        throw new NightingaleException(
                                messages.getMessage(
                                        Messages.ERROR_PARSER_TIMEOUT_INVALID_RANGE
                                )
                        );
                    } else {
                        ConfigurationController.
                                getInstance().
                                put("execution.timeout", true);
                        ConfigurationController.
                                getInstance().
                                put("execution.timeout.value", value);
                    }
                } catch (NumberFormatException nfexception) {
                    throw new NightingaleException(
                            messages.getMessage(
                                    Messages.ERROR_PARSER_TIMEOUT_NAN
                            )
                    );
                }
            }

            // there is an option for the
            // maximum number of loops, get
            // the argument and validate it
            if (line.hasOption("max-loops")) {
                try {
                    long value = Long.parseLong(line.getOptionValue("max-loops"));
                    if (value <= 0) {
                        throw new NightingaleException(
                                messages.getMessage(
                                        Messages.ERROR_PARSER_LOOPS_INVALID_RANGE
                                )
                        );
                    } else {
                        ConfigurationController.
                                getInstance().
                                put("execution.loops", value);
                    }
                } catch (NumberFormatException nfexception) {
                    throw new NightingaleException(
                            messages.getMessage(
                                    Messages.ERROR_PARSER_LOOPS_NAN
                            )
                    );
                }
            }

            // there is a verbose option,
            // set the settings accordingly
            if (line.hasOption("verbose")) {
                ConfigurationController.
                        getInstance().
                        put("execution.verbose", true);
            }

            // there is a dry-run option,
            // set the settings accordingly;
            // note that dry-run always sets
            // the execution to not halt when
            // an error occurs
            if (line.hasOption("dry-run")) {
                ConfigurationController.
                        getInstance().put("execution.dryrun", true);
                ConfigurationController.
                        getInstance().
                        put("execution.errors.halt", false);
            }

            // there is a verbose option,
            // set the settings accordingly
            if (line.hasOption("log")) {
                ConfigurationController.
                        getInstance().
                        put("execution.logging", true);
            }

            // time to do a file lookup based on
            // the string reference; this method
            // might raise an exception if the file
            // could not be found
            CommonUtils.discoverFile(reference);
            LoggingController.enableLogging((Boolean) ConfigurationController.
                    getInstance().
                    get("execution.logging")
            );
            ConfigurationController.
                    getInstance().
                    put("display.time", true);
            
            // everything is good, so the application
            // can proceed on the analysis
            return true;

        } catch (ParseException pexception) {
            printVersion();
            printUsage();
            return false;
        }
    }

    /**
     * Prints the application usage.
     */
    private void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        StringBuilder builder = new StringBuilder();
        builder.append("nightingale [file [--dry-run] [--log] ");
        builder.append("[--verbose] [--timeout N] [--max-loops N] ");
        builder.append("[--language L] | --help | --version]");
        formatter.printHelp(builder.toString(), options);
    }

    /**
     * Prints the application version.
     */
    private void printVersion() {
        String year = (String) ConfigurationController.
                getInstance().
                get("application.copyright.year");
        StringBuilder builder = new StringBuilder();
        builder.append("nightingale ");
        builder.append(CommonUtils.getVersionString());
        builder.append("\n");
        builder.append("Copyright (c) ").append(year).append(", ");
        builder.append("Paulo Roberto Massa Cereda");
        builder.append("\n");
        builder.append(
                messages.getMessage(
                        Messages.INFO_PARSER_ALL_RIGHTS_RESERVED
                )
        );
        builder.append("\n");
        System.out.println(builder.toString());
    }

    /**
     * Print the application notes.
     */
    private void printNotes() {
        DisplayUtils.wrapText(messages.getMessage(Messages.INFO_PARSER_NOTES));
    }

    /**
     * Updates all the descriptions in order to make them reflect the current
     * language setting.
     */
    private void updateDescriptions() {
        version.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_VERSION_DESCRIPTION
                )
        );
        help.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_HELP_DESCRIPTION
                )
        );
        log.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_LOG_DESCRIPTION
                )
        );
        verbose.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_VERBOSE_MODE_DESCRIPTION
                )
        );
        dryrun.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_DRYRUN_MODE_DESCRIPTION
                )
        );
        timeout.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_TIMEOUT_DESCRIPTION
                )
        );
        language.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_LANGUAGE_DESCRIPTION
                )
        );
        loops.setDescription(
                messages.getMessage(
                        Messages.INFO_PARSER_LOOPS_DESCRIPTION
                )
        );
    }

}
