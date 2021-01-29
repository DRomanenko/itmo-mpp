package msqueue;

import kotlinx.atomicfu.AtomicRef;

public class MSQueue implements Queue {
    private final AtomicRef<Node> head;
    private final AtomicRef<Node> tail;

    public MSQueue() {
        Node dummy = new Node(0, null);
        head = new AtomicRef<>(dummy);
        this.tail = new AtomicRef<>(dummy);
    }

    @Override
    public void enqueue(int x) {
        Node newTail = new Node(x, null);
        while (true) {
            Node node = tail.getValue();
            if (node.next.compareAndSet(null, newTail)) {
                tail.compareAndSet(node, newTail);
                return;
            } else {
                tail.compareAndSet(node, node.next.getValue());
            }
        }
    }
    
    @Override
    public int dequeue() {
        while (true) {
            Node curHead = head.getValue();
            while (true) {
                Node node = this.tail.getValue();
                Node nodeNext = node.next.getValue();
                if (nodeNext == null) {
                    break;
                } else {
                    this.tail.compareAndSet(node, nodeNext);
                }
            }

            if (tail.getValue() == curHead)
                return Integer.MIN_VALUE;
            Node next = curHead.next.getValue();
            if (head.compareAndSet(curHead, next))
                return next.x;
        }
    }

    @Override
    public int peek() {
        while (true) {
            Node curHead = head.getValue();
            Node next = curHead.next.getValue();
            if (head.getValue() == curHead) {
                if (next == null) {
                    return Integer.MIN_VALUE;
                } else {
                    if (tail.getValue() == curHead)
                        tail.setValue(next);
                    if (head.getValue() == curHead)
                        return next.x;
                }
            }
        }
    }

    private static class Node {
        final AtomicRef<Node> next;
        final int x;

        Node(int x, Node next) {
            this.next = new AtomicRef<>(next);
            this.x = x;
        }
    }
}