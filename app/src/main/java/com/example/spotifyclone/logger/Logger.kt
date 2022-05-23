/*
 * Copyright (c) 2022 All Rights Reserved, Quang Chien Pham.
 */
package com.example.spotifyclone.logger

import android.util.Log

/*
 * Define Logcat for application.
 */
internal object Logger {
    private var DEBUG = true

    private const val TRACE_METHOD = "trace"

    private const val START_LOG_METHOD = "startLogMethod"

    private const val END_LOG_METHOD = "endLogMethod"

    private const val CLASS_NAME_INDEX = 0

    private const val METHOD_NAME_INDEX = 1

    /*
     * set Debug Mode for all module.
     *
     * @param debugMode Boolean input value of debugMode.
     */
    fun setDebugMode(debugMode: Boolean) {
        DEBUG = debugMode
    }

    /*
     * Send an information log message.
     *
     * @param content The message you would like logged.
     */
    fun i(content: String) {
        if (DEBUG) {
            val msg = trace()
            if (msg != null) {
                i(msg[CLASS_NAME_INDEX], msg[METHOD_NAME_INDEX] + content)
            }
        }
    }

    private fun i(tag: String, content: String) {
        if (DEBUG) {
            Log.i(tag, content)
        }
    }

    /*
     * Send an error log message.
     *
     * @param content The message you would like logged.
     */
    fun e(content: String) {
        if (DEBUG) {
            val msg = trace()
            if (msg != null) {
                e(msg[CLASS_NAME_INDEX], msg[METHOD_NAME_INDEX] + content)
            }
        }
    }

    private fun e(tag: String, content: String) {
        if (DEBUG) {
            Log.e(tag, content)
        }
    }

    /*
     * Send an debug log message.
     *
     * @param content The message you would like logged.
     */
    fun d(content: String) {
        if (DEBUG) {
            val msg = trace()
            if (msg != null) {
                d(msg[CLASS_NAME_INDEX], "[ " + msg[METHOD_NAME_INDEX] + " ] " + content)
            }
        }
    }

    /*
     * Send an warning log message.
     *
     * @param content The message you would like logged.
     */
    fun w(content: String) {
        if (DEBUG) {
            val msg = trace()
            if (msg != null) {
                w(msg[CLASS_NAME_INDEX], "[ " + msg[METHOD_NAME_INDEX] + " ] " + content)
            }
        }
    }

    private fun d(tag: String, content: String) {
        if (DEBUG) {
            Log.d(tag, content)
        }
    }

    private fun w(tag: String, content: String) {
        if (DEBUG) {
            Log.w(tag, content)
        }
    }

    private fun trace(): Array<String>? {
        var index = 0
        val stackTraceElements = Thread.currentThread().stackTrace ?: return null
        for (i in stackTraceElements.indices) {
            val ste = stackTraceElements[i]
            if ((ste.className == Logger::class.java.name) &&
                (ste.methodName.contains(TRACE_METHOD))
            ) {
                // index for startEndMethodLog method
                index = i + 2
                if (index < stackTraceElements.size &&
                    stackTraceElements[index].methodName.contains(START_LOG_METHOD) ||
                    index < stackTraceElements.size &&
                    stackTraceElements[index].methodName.contains(END_LOG_METHOD)
                ) {
                    break
                }
                // index for d method
                index = i + 1
                break
            }
        }

        // index for method call d or startEndMethodLog method
        index++

        if ((stackTraceElements.size >= index) && (stackTraceElements[index] != null)) {
            return arrayOf(
                stackTraceElements[index].fileName,
                stackTraceElements[index].methodName + "[" + stackTraceElements[index].lineNumber + "] "
            )
        }
        return null
    }
}