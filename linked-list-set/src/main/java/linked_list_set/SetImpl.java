package linked_list_set;

import kotlinx.atomicfu.AtomicRef;

public class SetImpl implements Set {
    private static class Node {
        AtomicRef<Node> next;
        int key;

        Node(int key, Node next) {
            this.next = new AtomicRef<>(next);
            this.key = key;
        }
    }

    private static class Removed extends Node {
        Removed(int key, Node next) {
            super(key, next);
        }
    }

    private boolean isRemoved(Node node) {
        return node.next.getValue() instanceof Removed;
    }

    private static class Window {
        Node cur, next;
    }

    private final AtomicRef<Node> head = new AtomicRef<>(new Node(Integer.MIN_VALUE, new Node(Integer.MAX_VALUE, null)));

    /**
     * Returns the {@link Window}, where cur.key < key <= next.key
     */
    private Window findWindow(int key) {
        while (true) {
            Window w = new Window();
            w.cur = head.getValue();
            w.next = w.cur.next.getValue();
            while (!isRemoved(w.cur) && (w.next.key < key || isRemoved(w.next))) {
                if (isRemoved(w.next)) {
                    Node nextNext = w.next.next.getValue().next.getValue();
                    w.next = w.cur.next.compareAndSet(w.next, nextNext) ? nextNext : w.cur.next.getValue();
                } else {
                    w.cur = w.next;
                    w.next = w.cur.next.getValue();
                }
            }
            if (!isRemoved(w.cur))
                return w;
        }
    }

    @Override
    public boolean add(int key) {
        while (true) {
            Window w = findWindow(key);
            if (w.next.key == key) {
                return false;
            }
            if (w.cur.next.compareAndSet(w.next, new Node(key, w.next))) {
                return true;
            }
        }
    }


    @Override
    public boolean remove(int key) {
        while (true) {
            Window w = findWindow(key + 1);
            if (w.cur.key != key) {
                return false;
            }
            if (w.cur.next.compareAndSet(w.next, new Removed(Integer.MIN_VALUE, w.next))) {
                return true;
            }
        }
    }

    @Override
    public boolean contains(int key) {
        Window w = findWindow(key);
        return w.next.key == key;
    }
}