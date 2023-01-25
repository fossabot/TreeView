package io.github.dingyi222666.view.treeview

import androidx.annotation.CallSuper


/**
 * Abstract interface for tree structure.
 *
 * You can implement your own tree structure.
 */
interface AbstractTree<T : Any> : TreeVisitable<T>, TreeIdGenerator {

    /**
     * Node generator.
     *
     * The object is required for each tree structure so that the node data can be retrieved.
     *
     * @see [TreeNodeGenerator]
     */
    var generator: TreeNodeGenerator<T>


    /**
     * Root node.
     *
     * All data for the tree structure is obtained based on the root node and the node generator.
     */
    var rootNode: TreeNode<T>

    /**
     * Create the root node.
     *
     * This method is called first when the init tree is created.
     *
     * In this method you need to call the [createRootNodeUseGenerator] method first
     * and then create an empty root node if the [TreeNodeGenerator] does not return a root node.
     *
     * @see [createRootNodeUseGenerator]
     * @see [TreeNodeGenerator]
     */
    fun createRootNode(): TreeNode<*>

    /**
     * Use [TreeNodeGenerator] to create the root node.
     * This method will actually call [TreeNodeGenerator.createRootNode]
     *
     * @see [TreeNodeGenerator.createRootNode]
     */
    fun createRootNodeUseGenerator(): TreeNode<T>? {
        return generator.createRootNode()
    }

    /**
     * Initializing the tree.
     *
     * Subclass overrides can do something when that method is called.
     *
     * Note: Before initializing the tree, make sure that the node generator is set up.
     */
    @CallSuper
    fun initTree() {
        createRootNode()
    }

    /**
     * Get the list of children of the current node.
     *
     * This method returns a list of the ids of the child nodes,
     * you may need to do further operations to get the list of child nodes
     *
     * @param currentNode current node
     * @return List of id of child nodes
     *
     * @see [TreeNodeGenerator]
     */
    suspend fun getChildNodes(currentNode: TreeNode<*>): Set<Int>

    /**
     * Get the child list of the current node pointed to by the id.
     *
     * This method returns a list of the ids of the child nodes,
     * you may need to do further operations to get the list of child nodes
     *
     * @param currentNodeId Need to get the id of a node in the child node list
     * @return List of id of child nodes
     *
     * @see [TreeNodeGenerator]
     */
    suspend fun getChildNodes(currentNodeId: Int): Set<Int>

    /**
     * Refresh the current node.
     *
     * Refresh the node, this will update the list of children of the current node.
     *
     * Note: Refreshing a node does not update all the children under the node, the method will only update one level (the children under the node). You can call this method repeatedly if you need to update all the child nodes
     *
     * @see [TreeNodeGenerator]
     */
    suspend fun refresh(node: TreeNode<T>): TreeNode<T>

    /**
     * Refresh the current node and it‘s child.
     *
     * Refreshing the current node and also refreshes its children.
     *
     * @param [withExpandable] Whether to refresh only the expanded child nodes, otherwise all will be refreshed.
     *
     * @see [TreeNodeGenerator]
     */
    suspend fun refreshWithChild(node: TreeNode<T>, withExpandable: Boolean = true): TreeNode<T>


    /**
     * Get the list of node from the given list of id
     */
    fun getNodes(nodeIdList: Set<Int>): List<TreeNode<T>>

    /**
     * Get node pointed to from id
     */
    fun getNode(id: Int): TreeNode<T>

}


/**
 * Convert the node data in a tree structure into an ordered list.
 *
 * @param withExpandable Whether to add collapsed nodes
 * @param fastVisit Quick visit to the tree structure or not
 *
 * @see AbstractTree
 * @see TreeVisitor
 * @see TreeVisitable
 */
suspend fun <T : Any> AbstractTree<T>.toSortedList(
    withExpandable: Boolean = true, fastVisit: Boolean = true
): List<TreeNode<T>> {
    val result = mutableListOf<TreeNode<T>>()

    val visitor = object : TreeVisitor<T> {
        override fun visitChildNode(node: TreeNode<T>): Boolean {
            if (node.depth >= 0) {
                result.add(node)
            }
            return if (withExpandable) {
                node.expand
            } else {
                true
            }
        }

        override fun visitLeafNode(node: TreeNode<T>) {
            if (node.depth >= 0) {
                result.add(node)
            }
        }

    }

    visit(visitor, fastVisit)

    return result
}