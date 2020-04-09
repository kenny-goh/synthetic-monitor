package com.gkh.syntheticmonitor.model

interface SMActionInterface {
    /**
     *
     * @param context
     */
    fun preExecuteScript(context: SMExecutionContext)

    /**
     *
     * @param context
     */
    fun expandInputParameters(context: SMExecutionContext)

    /**
     *
     * @param context
     * @throws SyntheticTestException
     */
    @Throws(Exception::class)
    fun execute(context: SMExecutionContext)

    /**
     *
     * @param context
     */
    @Throws(Exception::class)
    fun postExecuteScript(context: SMExecutionContext)

    /**
     *
     * @return
     */
    val type: String?

    /**
     *
     * @return
     */
    val name: String?
}