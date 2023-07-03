package ru.yandex.service;

import ru.yandex.model.Node;
import ru.yandex.model.Task;
import ru.yandex.service.interfaces.HistoryManager;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final CustomLinkedList history;

    public InMemoryHistoryManager() {
        history = new CustomLinkedList();
    }

    @Override
    public void add(Task task) {
        if (task == null) return;
        history.linkLast(task);
    }

    @Override
    public void remove(int id) {
        Node node = history.getNodeById(id);
        if (node == null) return;
        history.removeNode(node);
    }

    @Override
    public List<Task> getHistory() {
        return history.getTasks();
    }

    static class CustomLinkedList {

        private Node tail;
        private Node head;
        private final Map<Integer, Node> linkedHashMap;

        CustomLinkedList() {
            linkedHashMap = new HashMap<>();
        }

        void linkLast(Task task) {
            Node itemNode = linkedHashMap.get(task.getId());
            if (itemNode != null) {
                removeNode(itemNode);
            }

            Node vertex = new Node(task);
            if (head == null) {
                head = vertex;
                tail = head;
            } else {
                vertex.setPrev(tail);
                tail.setNext(vertex);
                tail = vertex;
            }
            linkedHashMap.put(task.getId(), vertex);
        }

        void removeNode(Node node) {
            if (node == null) return;
            Node prevNode = node.getPrev();
            Node nextNode = node.getNext();

            if (prevNode == null && nextNode == null) {
                head = null;
                tail = null;
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

        List<Task> getTasks() {
            Node currentNode = head;
            List<Task> tasks = new ArrayList<>();
            while (currentNode != null) {
                tasks.add(currentNode.getItem());
                currentNode = currentNode.getNext();
            }
            return tasks;
        }

        Node getNodeById(int id) {
            return linkedHashMap.get(id);
        }
    }
}
