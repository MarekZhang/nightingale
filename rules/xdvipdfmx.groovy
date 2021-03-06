/**
 * Nightingale
 * Copyright (c) 2014, Paulo Roberto Massa Cereda 
 * All rights reserved.
 */

import com.github.cereda.nightingale.model.Command

rule = [
    id : "xdvipdfmx",
    name : "XDVIPDFMX",
    arguments : [ "options", "output" ],
    description : "XDVIPDFMX",
    authors : [
	"Marco Daniel",
	"Paulo Cereda"
    ],
    requires : 1.0
]

commands = [
    [
        name : "XDVIPDFMX",
        command : {

		ensure('output', basename(obtain('file')))

		def file = "${basename(obtain('file'))}.dvi"
		def output = "${obtain('output')}.ps"
		def options = ''

		if (defined('options')) {
			options = obtain('options')
			if (!(options instanceof List)) {
				error("I am sorry, but I was expecting a list when using the 'options' argument.")
			}
		}

		return new Command('xdvipdfmx', file, '-o', output, options)

	},
        exit : { value -> return value == 0 }
    ]
]

