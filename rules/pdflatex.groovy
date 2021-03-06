/**
 * Nightingale
 * Copyright (c) 2014, Paulo Roberto Massa Cereda 
 * All rights reserved.
 */

import com.github.cereda.nightingale.model.Command

rule = [
    id : "pdflatex",
    name : "PDFLaTeX",
    arguments : [ "interaction", "draft", "shell", "synctex", "options" ],
    description : "The PDFLaTeX engine",
    authors : [
	"Marco Daniel",
	"Paulo Cereda"
    ],
    requires : 1.0
]

commands = [
    [
        name : "PDFLaTeX engine",
        command : {

		def file = basename(obtain('file'))
		def draft = defined('draft') ? check(true, 'draft', '--draftmode') : ''
		def shell = defined('shell') ? check(true, 'shell', '--shell-escape', '--no-shell-escape') : ''	
		def synctex = defined('synctex') ? check(true, 'synctex', '--synctex=1', '--synctex=0') : ''
		def interaction = ''
		def options = ''

		if (defined('interaction')) {
			interaction = obtain('interaction')
			if (!( interaction in [ "batchmode", "nonstopmode", "scrollmode", "errorstopmode" ] )) {
				error("I am sorry, but you provided an invalid interaction mode: ${interaction}")
			}
			else {
				interaction = "--interaction=${interaction}"
			}
		}

		if (defined('options')) {
			options = obtain('options')
			if (!(options instanceof List)) {
				error("I am sorry, but I was expecting a list when using the 'options' argument.")
			}
		}

		return new Command('pdflatex', interaction, draft, shell, synctex, options, file)

	},
        exit : { value -> return value == 0 }
    ]
]

