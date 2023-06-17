package ru.yandex.service;

import ru.yandex.model.Node;
import ru.yandex.model.Task;
import ru.yandex.service.interfaces.HistoryManager;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final CustomLinkedList history;
    private final Map<Integer, Node> linkedHashMap;

    public InMemoryHistoryManager() {
        history = new CustomLinkedList();
        linkedHashMap = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        if (linkedHashMap.get(task.getId()) != null) {
            remove(task.getId());
        }
        linkedHashMap.put(task.getId(), history.linkLast(task));
    }

    @Override
    public void remove(int id) {
        history.removeNode(linkedHashMap.get(id));
    }

    @Override
    public List<Task> getHistory() {
        return history.getTasks();
    }

    static class CustomLinkedList {

        private Node tail;
        private Node head;

        public Node linkLast(Task item) {
            if (item == null) return new Node(null);
            Node vertex = new Node(item);
            if (head == null) {
                head = vertex;
                tail = head;
            } else {
                vertex.setPrev(tail);
                tail.setNext(vertex);
                tail = vertex;
            }
            return vertex;
        }

        public void removeNode(Node node) {
            if (node == null) return;
            Node prevNode = node.getPrev();
            Node nextNode = node.getNext();

            if (prevNode == null && nextNode == null) {
                head = null;
                tail = null;
                node.setItem(null);
                return;
            }

            if (prevNode == null) {
                head = nextNode;
                head.setPrev(null);
                return;
            }
            if (nextNode == null) {
                tail = prevNode;
                tail.setNext(null);
                return;
            }

            prevNode.setNext(nextNode);
            nextNode.setPrev(prevNode);
        }

        public List<Task> getTasks() {
            Node currentNode = head;
            List<Task> tasks = new ArrayList<>();
            while (currentNode != null) {
                tasks.add(currentNode.getItem());
                currentNode = currentNode.getNext();
            }
            return tasks;
        }
    }
}
