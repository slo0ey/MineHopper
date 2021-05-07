package com.acceler8tion.minehopper;

import java.util.NoSuchElementException;

public final class Queue<T> {
    private static final class Node<T> {
        private final T val;
        private Node<T> next;

        Node(T val) {
            this.val = val;
        }
    }

    private Node<T> front = null;
    private Node<T> rear = null;

    void enqueue(T val) {
        Node<T> n = new Node<>(val);
        if(rear != null) {
            rear.next = n;
        }
        rear = n;
        if(front == null) {
            front = rear;
        }
    }

    T dequeue() {
        if(isEmpty()) throw new NoSuchElementException();
        T v = front.val;
        front = front.next;
        if(front == null) rear = null;
        return v;
    }

    T peek() {
        if(isEmpty()) throw new NoSuchElementException();
        return front.val;
    }

    boolean isEmpty() {
        return front == null;
    }

    void clear() {
        front = null;
        rear = null;
    }
}
