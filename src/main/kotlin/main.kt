import edu.mit.csail.sdg.alloy4.A4Reporter
import edu.mit.csail.sdg.alloy4.ErrorWarning
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod
import edu.mit.csail.sdg.alloy4viz.VizGUI

fun main(args: Array<String>) {
    val reporter = object: A4Reporter() {
        override fun warning(msg: ErrorWarning) {
            for (line in msg.message!!.lines()) {
                println("# $msg")
            }
        }
    }
    val options = A4Options()
    options.solver = A4Options.SatSolver.SAT4J

    if (args.size < 1) {
        usage()
    }

    var modeVisualize = false
    var file: String? = null
    for (arg in args) {
        if (arg == "-V") {
            modeVisualize = true
        } else if (arg[0] == '-') {
            usage()
        } else {
            file = arg
        }
    }

    if (file == null) {
        usage()
    } else {
        val world = CompUtil.parseEverything_fromFile(reporter, null, file)
        val commands = world.allCommands
        var failed = false
        println("1..${commands.indices.last + 1}")
        for (i in commands.indices) {
            val command = commands[i]
            val solution = TranslateAlloyToKodkod.execute_command(reporter, world.allReachableSigs, command, options);
            if (command.check != solution.satisfiable()) {
                println("ok ${i + 1} - $command")
            } else {
                println("not ok ${i + 1} - $command")

                if (modeVisualize) {
                    val outputXML = kotlin.io.createTempFile()
                    solution.writeXML(outputXML.path)
                    VizGUI(false, outputXML.path, null)
                }

                failed = true
            }
        }
        if (failed) {
            kotlin.system.exitProcess(2)
        }
    }
}

fun usage() {
    println("usage: alloy-run [-V] <file>")
    kotlin.system.exitProcess(1)
}
