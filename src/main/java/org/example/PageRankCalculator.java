package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class PageRankCalculator {
    private static final double DAMPING = 0.85;
    private static final double TOLERANCE = 1e-6;
    private static final int MAX_ITERATIONS = 100;

    public static void main(String[] args) {
        // 示例图结构 (A->B, A->C, B->C, C->A)
        Map<String, Map<String, Integer>> graph = new HashMap<>();
        addEdge(graph, "A", "B", 1);
        addEdge(graph, "A", "C", 2);
        addEdge(graph, "B", "C", 1);
        addEdge(graph, "C", "A", 1);

        Map<String, Double> pageRanks = calculatePageRank(graph);
        pageRanks.forEach((node, pr) ->
                System.out.printf("%s: %.4f\n", node, pr)
        );
    }

    public static Map<String, Double> calculatePageRank(
            Map<String, Map<String, Integer>> graph) {

        // 收集所有节点（包括孤立节点）
        Set<String> allNodes = new HashSet<>();
        graph.forEach((src, edges) -> {
            allNodes.add(src);
            allNodes.addAll(edges.keySet());
        });
        int totalNodes = allNodes.size();
        if (totalNodes == 0) return Collections.emptyMap();

        // 预处理数据
        Map<String, Double> outWeights = calcOutWeights(graph, allNodes);
        Map<String, List<String>> reverseEdges = buildReverseGraph(graph);
        List<String> deadEnds = identifyDeadEnds(allNodes, outWeights);

        // 初始化PR值
        Map<String, Double> pr = allNodes.stream()
                .collect(Collectors.toMap(n -> n, n -> 1.0 / totalNodes));

        // 迭代计算
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            Map<String, Double> newPr = new HashMap<>();
            double deadEndContribution = calcDeadEndContribution(pr, deadEnds, totalNodes);

            for (String node : allNodes) {
                Map<String, Double> finalPr = pr;
                double incoming = reverseEdges.getOrDefault(node, Collections.emptyList())
                        .stream()
                        .mapToDouble(src -> {
                            double totalOut = outWeights.get(src);
                            return totalOut > 0 ?
                                    finalPr.get(src) * (graph.get(src).get(node) / totalOut) :
                                    0;
                        })
                        .sum();

                double newValue = (1 - DAMPING) / totalNodes +
                        DAMPING * (incoming + deadEndContribution);
                newPr.put(node, newValue);
            }

            if (hasConverged(pr, newPr)) break;
            pr = newPr;
        }
        return pr;
    }

    // 预处理工具方法
    private static Map<String, Double> calcOutWeights(
            Map<String, Map<String, Integer>> graph,
            Set<String> nodes) {
        return nodes.stream()
                .collect(Collectors.toMap(
                        n -> n,
                        n -> graph.getOrDefault(n, Collections.emptyMap())
                                .values().stream()
                                .mapToInt(Integer::intValue)
                                .sum()
                                * 1.0
                ));
    }

    private static Map<String, List<String>> buildReverseGraph(
            Map<String, Map<String, Integer>> graph) {
        Map<String, List<String>> reverse = new HashMap<>();
        graph.forEach((src, edges) ->
                edges.keySet().forEach(dest ->
                        reverse.computeIfAbsent(dest, k -> new ArrayList<>()).add(src)
                )
        );
        return reverse;
    }

    private static List<String> identifyDeadEnds(
            Set<String> nodes,
            Map<String, Double> outWeights) {
        return nodes.stream()
                .filter(n -> outWeights.getOrDefault(n, 0.0) == 0)
                .collect(Collectors.toList());
    }

    private static double calcDeadEndContribution(
            Map<String, Double> pr,
            List<String> deadEnds,
            int totalNodes) {
        return deadEnds.stream()
                .mapToDouble(pr::get)
                .sum() / totalNodes;
    }

    private static boolean hasConverged(
            Map<String, Double> oldPr,
            Map<String, Double> newPr) {
        return oldPr.keySet().stream()
                .mapToDouble(k -> Math.abs(oldPr.get(k) - newPr.get(k)))
                .max().orElse(Double.POSITIVE_INFINITY) < TOLERANCE;
    }

    private static void addEdge(
            Map<String, Map<String, Integer>> graph,
            String src, String dest, int weight) {
        graph.computeIfAbsent(src, k -> new HashMap<>())
                .put(dest, weight);
    }
}
