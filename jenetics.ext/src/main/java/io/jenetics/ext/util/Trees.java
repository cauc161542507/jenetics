/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */
package io.jenetics.ext.util;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collectors;

import io.jenetics.util.ISeq;
import io.jenetics.util.MSeq;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since 3.9
 */
final class Trees {
	private Trees() {}


	@SuppressWarnings("unchecked")
	static <V, T extends Tree<V, T>> T self(final Tree<?, ?> tree) {
		return (T)tree;
	}

	/**
	 * Builds the parents of node up to and including the root node, where the
	 * original node is the last element in the returned array. The length of
	 * the returned array gives the node's depth in the tree.
	 *
	 * @param node the node to get the path for
	 * @param depth  an int giving the number of steps already taken towards
	 *        the root (on recursive calls), used to size the returned array
	 * @return an array of nodes giving the path from the root to the specified
	 *         node
	 */
	static <V, T extends Tree<V, T>> MSeq<T> pathToRoot(
		final T node,
		final int depth
	) {
		final MSeq<T> path;
		if (node == null) {
			path = MSeq.ofLength(depth);
		} else {
			path = pathToRoot(node.getParent().orElse(null), depth + 1);
			path.set(path.length() - depth - 1, node);
		}

		return path;
	}

	static String toInfixString(final Tree<?, ?> tree) {
		final StringBuilder out = new StringBuilder();
		toInfixString(out, tree);
		return out.toString();
	}

	private static void toInfixString(final StringBuilder out, final Tree<?, ?> tree) {
		if (!tree.isLeaf()) {
			toInfixChild(out, tree.getChild(0));
			out.append(tree.getValue());
			toInfixChild(out, tree.getChild(1));
		} else {
			out.append(tree.getValue());
		}
	}

	private static void toInfixChild(final StringBuilder out, final Tree<?, ?> child) {
		if (child.isLeaf()) {
			toInfixString(out, child);
		} else {
			out.append("(");
			toInfixString(out, child);
			out.append(")");
		}
	}

	public static String toDottyString(final String name, final Tree<?, ?> tree) {
		final StringBuilder out = new StringBuilder();
		out.append("digraph ").append(name).append(" {\n");
		dotty(out, tree);
		labels(out, tree);
		out.append("}\n");
		return out.toString();
	}

	private static void dotty(final StringBuilder out, final Tree<?, ?> node) {
		final ISeq<? extends Tree<?, ?>> nodes = node.breadthFirstStream()
			.collect(ISeq.toISeq());

		for (int i = 0; i < nodes.length(); ++i) {
			final Tree<?, ?> n = nodes.get(i);
			n.childStream().forEach(child ->
				out.append("    ")
					.append(id(n))
					.append(" -> ")
					.append(id(child))
					.append(";\n")
			);
		}
	}

	private static String id(final Tree<?, ?> node) {
		return "node_" + Math.abs(System.identityHashCode(node));
	}

	private static void labels(final StringBuilder out, final Tree<?, ?> tree) {
		tree.depthFirstStream().forEach(node -> {
			out.append("    ");
			out.append(id(node));
			out.append(" [label=\"").append(node.getValue()).append("\"];\n");
		});
	}


	static String toLispString(Tree<?, ?> tree) {
		final String value = String.valueOf(tree.getValue());
		if (tree.isLeaf()) {
			return value;
		} else {
			final String children = tree.childStream()
				.map(Trees::toLispString)
				.collect(Collectors.joining(" "));
			return "(" + value + " " + children + ")";
		}
	}

	/**
	 * Checks if the two given trees has the same structure with the same values.
	 *
	 * @param a the first tree
	 * @param b the second tree
	 * @return {@code true} if the two given trees are structurally equals,
	 *         {@code false} otherwise
	 */
	static boolean equals(final Tree<?, ?> a, final Tree<?, ?> b) {
		boolean equals = a == b;
		if (!equals && a != null && b != null) {
			equals = a.childCount() == b.childCount();
			if (equals) {
				equals = Objects.equals(a.getValue(), b.getValue());
				if (equals && a.childCount() > 0) {
					equals = equals(a.childIterator(), b.childIterator());
				}
			}
		}

		return equals;
	}

	private static boolean equals(
		final Iterator<? extends Tree<?, ?>> a,
		final Iterator<? extends Tree<?, ?>> b
	) {
		boolean equals = true;
		while (a.hasNext() && equals) {
			equals = equals(a.next(), b.next());
		}

		return equals;
	}

}
