package ru.yandex.model;

public class Node {

    private Task item;
    private Node prev;
    private Node next;

    public Node(Task item) {
        this.item = item;
    }

    public Task getItem() {
        return item;
    }

    public void setItem(Task item) {
        this.item = item;
    }

    public Node getPrev() {
        return prev;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }
}
