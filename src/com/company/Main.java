package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {
    // Две мапы - из частей строк в строки и из строк в части.
    // Часть строки задается строкой и положением в родительской строке.
    public static Map<Tuple<String, Integer>, List<String>> partialToOriginal = new HashMap<>();
    public static Map<String, List<Tuple<String, Integer>>> originalToPartial = new HashMap<>();
    // Сюда будем собирать группы
    public static List<Set<String>> groups = new ArrayList<>();
    // А здесь отслеживать посещенные ноды (части строк)
    public static Set<Tuple<String, Integer>> visitedParts = new HashSet<>();

    public static void readFile(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";", -1);
                if (parts.length != 3) {
                    System.out.printf("Skipping line %s, because it contains less than 3 parts\n", line);
                    continue;
                }
                boolean ok = true;
                for (int i = 0; i < parts.length; i++) {
                    String cleaned = parts[i].replace("\"", "");
                    int count = parts[i].length() - cleaned.length();
                    if (count != 2 && !cleaned.equals("")) {
                        System.out.printf("Invalid number of \"'s in string %s\n", line);
                        ok = false;
                        break;
                    } else {
                        parts[i] = cleaned;
                    }
                }

                if (!ok) continue;

                List<Tuple<String, Integer>> tuples = new ArrayList<>();
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];
                    Tuple<String, Integer> key = new Tuple<>(part, i);
                    if (!part.equals("") && !originalToPartial.containsKey(line)) { // Убираем дубликаты таким образом
                        if (!partialToOriginal.containsKey(key)) {
                            partialToOriginal.put(key, new ArrayList<>());
                        }
                        partialToOriginal.get(key).add(line);
                        tuples.add(key);

                    }
                }
                originalToPartial.put(line, tuples);
            }
        } catch (IOException e) {
            System.out.println("Error while reading a file!");
            e.printStackTrace();
        }
    }

    // Суть алгоритма - начиная с какой-то части строки обходим все строки, ее содержащую.
    // У каждой родительской строки берем части и повторяем алгоритм.
    // По сути делаем поиск компонент связности в графе.
    public static void traverseGraph(Tuple<String, Integer> node, int groupNumber) { // DFS with extra steps :)
        visitedParts.add(node);
        for (String line : partialToOriginal.get(node)) {
            groups.get(groupNumber).add(line); // Set предохраняет нас от дубликатов
            for (Tuple<String, Integer> nextNode : originalToPartial.get(line)) {
                if (!visitedParts.contains(nextNode)) {
                    traverseGraph(nextNode, groupNumber);
                }
            }
        }
    }

    public static void createGroups() {
        int groupNumber = 0;
        for (Tuple<String, Integer> part: partialToOriginal.keySet()){
            if (!visitedParts.contains(part)) {
                groups.add(new HashSet<>());
                traverseGraph(part, groupNumber);
                groupNumber++;
            }
        }
    }

    public static void printGroups() {
        Collections.sort(groups, (a, b) -> Integer.compare(b.size(), a.size()));

        int cnt = 0;
        for (Set<String> group : groups) {
            if (group.size() > 1) {
                cnt++;
            }
        }

        System.out.printf("Всего %d групп, содержащих больше одного элемента\n", cnt);
        for (int i = 0; i < groups.size(); i++) {
            System.out.printf("Группа %d\n", i + 1);
            for (String line : groups.get(i)) {
                System.out.println(line);
            }
        }
    }


    public static void processFile(String path) {
        readFile(path);
        createGroups();
        printGroups();
    }

    public static void main(String[] args) {
        processFile(args[0]);
    }
}
