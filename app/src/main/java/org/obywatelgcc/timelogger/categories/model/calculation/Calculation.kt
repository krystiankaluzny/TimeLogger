package org.obywatelgcc.timelogger.categories.model.calculation


open class CalculationTree<T, Context>(
    val root: Node<T, Context>
) {
    fun calculate(context: Context): T? {
        root.run(context)
        return root.result
    }

    interface Operation<T, Context> {
        fun apply(ctx: Context, left: T?, right: T?): T?
    }

    open class Node<T, Context>(
        val left: Node<T, Context>,
        val right: Node<T, Context>,
        val operation: Operation<T, Context>,
        var result: T? = null,
    ) {
        fun run(context: Context) {
            left.run(context)
            right.run(context)
            result = operation.apply(context, left.result, right.result)
        }
    }
}



