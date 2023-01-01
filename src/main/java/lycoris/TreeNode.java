/*
 * Copyright (C) 2023 The LYCORIS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package lycoris;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Diffable node of tree structure.
 */
public abstract class TreeNode<Self extends TreeNode, Context> implements Consumer<Self> {

    /** The node identifier. */
    protected final int id;

    /** The associated user context. */
    protected Context context;

    /** The children nodes. */
    private List<TreeNode> nodes;

    /**
     * <p>
     * Create node with identifier.
     * </p>
     * 
     * @param id A node identifier.
     */
    protected TreeNode(int id) {
        this.id = id;
    }

    /**
     * <p>
     * Retrieve the list of child nodes.
     * </p>
     * 
     * @return A list of child nodes.
     */
    public final List<TreeNode> nodes() {
        if (nodes == null) {
            nodes = new ArrayList<>(2);
        }
        return nodes;
    }

    /**
     * <p>
     * Insert this node to the parent node.
     * </p>
     * 
     * @param parent The contexual parent node.
     * @param index The index node.
     */
    protected abstract void addTo(Context parent, Object index);

    /**
     * <p>
     * Remove this node from the parent node.
     * </p>
     * 
     * @param parent The contexual parent node.
     */
    protected abstract void removeFrom(Context parent);

    /**
     * <p>
     * Move this node to end of the parent.
     * </p>
     * 
     * @param parent The contexual parent node.
     */
    protected abstract void moveTo(Context parent);

    /**
     * <p>
     * Replace this node with the specified node.
     * </p>
     * 
     * @param parent The contexual parent node.
     * @param newly A new node.
     */
    protected void replaceFrom(Context parent, Self newly) {
        newly.addTo(parent, this);
        removeFrom(parent);
    }

    /**
     * <p>
     * Diff against to the next state.
     * </p>
     * 
     * @param patches A list of diff patches.
     * @param next A next state.
     */
    protected void diff(List<Runnable> patches, Self next) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(Self parent) {
        parent.nodes().add(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return hashCode() == obj.hashCode();
    }

    /**
     * <p>
     * Diff against the specified state.
     * </p>
     * 
     * @param prev A prev state.
     * @param next A next state.
     * @return A list of gap closers.
     */
    public static <Node extends TreeNode<Node, Context>, Context> List<Runnable> diff(Context context, List<Node> prev, List<Node> next) {
        List<Runnable> patches = new ArrayList();
        diff(patches, context, prev, next);
        return patches;
    }

    /**
     * <p>
     * Helper method to diff list of {@link TreeNode} items. This method supports add, remove, move
     * and replace operations.
     * </p>
     * 
     * @param patches A list of diff patches.
     * @param prev A previous state.
     * @param next A next state.
     */
    protected static <Node extends TreeNode<Node, Context>, Context> void diff(List<Runnable> patches, Context context, List<Node> prev, List<Node> next) {
        int prevSize = prev.size();
        int nextSize = next.size();
        int max = prevSize + nextSize;
        int prevPosition = 0;
        int nextPosition = 0;

        for (int i = 0; i < max; i++) {
            if (prevSize <= prevPosition) {
                if (nextSize <= nextPosition) {
                    break; // all items were scanned
                } else {
                    // all prev items are scanned, but next items are remaining
                    Node nextItem = next.get(nextPosition++);
                    int index = prev.indexOf(nextItem);

                    if (index == -1) {
                        patches.add(() -> nextItem.addTo(context, null));
                    } else {
                        Node prevItem = prev.get(index);

                        /**
                         * <p>
                         * We passes the actual context from the previous node to the next node. To
                         * tell the truth, we don't want to manipulate the actual context in here.
                         * But here is the best place to pass the reference.
                         * </p>
                         */
                        nextItem.context = prevItem.context;

                        patches.add(() -> prevItem.moveTo(context));
                    }
                }
            } else {
                if (nextSize <= nextPosition) {
                    // all next items are scanned, but prev items are remaining
                    Node prevItem = prev.get(prevPosition++);
                    patches.add(() -> prevItem.removeFrom(context));
                } else {
                    // prev and next items are remaining
                    Node prevItem = prev.get(prevPosition);
                    Node nextItem = next.get(nextPosition);

                    if (prevItem.id == nextItem.id) {
                        // same item

                        /**
                         * <p>
                         * We passes the actual context from the previous node to the next node. To
                         * tell the truth, we don't want to manipulate the actual context in here.
                         * But here is the best place to pass the reference.
                         * </p>
                         */
                        nextItem.context = prevItem.context;

                        prevItem.diff(patches, nextItem);

                        prevPosition++;
                        nextPosition++;
                    } else {
                        // different item
                        int nextItemInPrev = prev.indexOf(nextItem);
                        int prevItemInNext = next.indexOf(prevItem);

                        if (nextItemInPrev == -1) {
                            if (prevItemInNext == -1) {
                                patches.add(() -> prevItem.replaceFrom(context, nextItem));
                                prevPosition++;
                            } else {
                                patches.add(() -> nextItem.addTo(context, prevItem.context));
                            }
                            nextPosition++;
                        } else {
                            if (prevItemInNext == -1) {
                                patches.add(() -> prevItem.removeFrom(context));
                            } else {
                                // both items are found in each other list
                                // hold and skip the current value
                            }
                            prevPosition++;
                        }
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Helper method to diff {@link List} items. This method supports add and remove operations.
     * </p>
     * 
     * @param patches A list of diff patches.
     * @param prev A previous state.
     * @param next A next state.
     * @param add An ADD operation.
     * @param remove A REMOVE operation.
     */
    protected static <T> void diff(List<Runnable> patches, List<T> prev, List<T> next, Consumer<T> add, Consumer<T> remove) {
        for (int i = 0, length = next.size(); i < length; i++) {
            T nextItem = next.get(i);
            int prevIndex = prev.indexOf(nextItem);

            if (prevIndex == -1) {
                patches.add(() -> add.accept(nextItem));
            }
        }

        for (int i = 0, length = prev.size(); i < length; i++) {
            T prevItem = prev.get(i);

            if (next.indexOf(prevItem) == -1) {
                patches.add(() -> remove.accept(prevItem));
            }
        }
    }
}