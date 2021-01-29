package stack;

import kotlinx.atomicfu.AtomicRef;

public class StackImpl implements Stack {
    private static class Node {
        final AtomicRef<Node> next;
        final int x;

        Node(int x, Node next) {
            this.next = new AtomicRef<>(next);
            this.x = x;
        }
    }

    // head pointer
    private final AtomicRef<Node> head = new AtomicRef<>(null);

    @Override
    public void push(int x) {
        while (true) {
            Node node  = new Node(x, head.getValue());
            if (head.compareAndSet(node.next.getValue(), node))
                break;
        }
    }

    @Override
    public int pop() {
        while (true) {
            Node node = head.getValue();
            if (node == null)
                return Integer.MIN_VALUE;
            if (head.compareAndSet(node, node.next.getValue()))
                return node.x;
        }
    }
}
