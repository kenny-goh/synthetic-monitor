package com.gkh.syntheticmonitor.model

interface SMActionInterface {
    /**
     * Executes Groovy script prior to execcuting the action
     * @param context
     */
    fun preExecuteScript(context: SMExecutionContext)

    /**
     * Expands the template string inside input parameters based on variable
     * defined in the context object.
     * e.g "hello $name" => "hell world"
     * @param context
     */
    fun expandInputParameters(context: SMExecutionContext)

    /**
     * Executes the action
     * @param context
     * @throws SyntheticTestException
     */
    @Throws(Exception::class)
    fun execute(context: SMExecutionContext)

    /**
     * Executes Groovy script after action execution
     * @param context
     */
    @Throws(Exception::class)
    fun postExecuteScript(context: SMExecutionContext)

    /**
     * @return type of action
     */
    val type: String?

    /**
     * @return name of action
     */
    val name: String?
}