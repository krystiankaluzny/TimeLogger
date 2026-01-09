package org.obywatelgcc.timelogger.categories.model.calculation

class BooleanCalculation<Context>(
    root: BooleanNode<Context>
) : CalculationTree<Boolean, Context>(root) {

    interface BooleanOperation<Context> : Operation<Boolean, Context>

    class BooleanNode<Context>(
        left: BooleanNode<Context>,
        right: BooleanNode<Context>,
        operation: BooleanOperation<Context>,
    ) : Node<Boolean, Context>(left, right, operation)

    class AlwaysTrueOperation<Context> : BooleanOperation<Context>{
        override fun apply(ctx: Context, left: Boolean?, right: Boolean?): Boolean? {
            return true
        }
    }

    class AlwaysFalseOperation<Context> : BooleanOperation<Context>{
        override fun apply(ctx: Context, left: Boolean?, right: Boolean?): Boolean? {
            return false
        }
    }

    class AndOperation<Context> : BooleanOperation<Context>{
        override fun apply(ctx: Context, left: Boolean?, right: Boolean?): Boolean? {
            return left!! && right!!
        }
    }

    class OrOperation<Context> : BooleanOperation<Context>{
        override fun apply(ctx: Context, left: Boolean?, right: Boolean?): Boolean? {
            return left!! || right!!
        }
    }

    class NotOperation<Context> : BooleanOperation<Context>{
        override fun apply(ctx: Context, left: Boolean?, right: Boolean?): Boolean? {
            return !left!!
        }
    }

    class EqualsOperation<Context> : BooleanOperation<Context>{
        override fun apply(ctx: Context, left: Boolean?, right: Boolean?): Boolean? {
            return left == right
        }
    }

    class NotEqualsOperation<Context> : BooleanOperation<Context>{
        override fun apply(ctx: Context, left: Boolean?, right: Boolean?): Boolean? {
            return left != right
        }
    }
}



