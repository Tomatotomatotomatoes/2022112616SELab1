package org.example;


import java.util.*;

public class DijkstraAlgorithm {
    public static Map<String,String> pre = new HashMap<>();

    // Dijkstra算法核心实现
    public static Map<String, Integer> dijkstra(
            Map<String, Map<String, Integer>> graph,
            String startNode) {

        // 初始化距离表
        Map<String, Integer> distances = new HashMap<>();
        for (String node : graph.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(startNode, 0); // 起点到自身距离为0

        // 优先队列（按距离排序）
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(
                Comparator.comparingInt(NodeDistance::getDistance)
        );
        pq.add(new NodeDistance(startNode, 0));

        // 已确定最短路径的节点集合
        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            String currentNode = current.getNode();

            if (visited.contains(currentNode)) continue;
            visited.add(currentNode);

            // 遍历所有邻居节点
            Map<String, Integer> neighbors = graph.get(currentNode);
            if (neighbors == null) continue;

            for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                String neighborNode = neighbor.getKey();
                int edgeWeight = neighbor.getValue();

                // 计算新距离
                int newDistance = distances.get(currentNode) + edgeWeight;

                // 更新最短距离
                if (newDistance < distances.get(neighborNode)) {
                    pre.put(neighborNode,currentNode);
                    distances.put(neighborNode, newDistance);
                    pq.add(new NodeDistance(neighborNode, newDistance));
                }
            }
        }

        return distances;
    }

    // 辅助类：节点与距离的绑定
    private static class NodeDistance {
        private final String node;
        private final int distance;

        public NodeDistance(String node, int distance) {
            this.node = node;
            this.distance = distance;
        }

        public String getNode() { return node; }
        public int getDistance() { return distance; }
    }

    // 测试代码
    public static void main(String[] args) {
        // 构建测试图（与之前代码生成的图结构一致）
        Map<String, Map<String, Integer>> graph = new HashMap<>();
        addEdge(graph, "to", "explore", 1);
        addEdge(graph, "explore", "strange", 1);
        addEdge(graph, "strange", "new", 1);
        addEdge(graph, "new", "worlds", 1);
        addEdge(graph, "worlds", "to", 1);
        addEdge(graph, "to", "seek", 1);
        addEdge(graph, "seek", "out", 1);
        addEdge(graph, "out", "new", 1);
        addEdge(graph, "new", "civilizations", 1);
        addEdge(graph, "new", "life", 1);
        addEdge(graph, "life", "and", 1);
        addEdge(graph, "and", "new", 1);
        graph.put("civilizations",new HashMap<>());


        // 运行Dijkstra算法
        String startNode = "to";
        Map<String, Integer> shortestDistances = dijkstra(graph, startNode);

        // 输出结果
        System.out.println("从起点 [" + startNode + "] 到各节点的最短距离:");
        for (Map.Entry<String, Integer> entry : shortestDistances.entrySet()) {
            System.out.printf("-> %-12s : %d\n", entry.getKey(), entry.getValue());
        }
    }

    // 辅助方法：添加边（与之前代码中的addEdge方法一致）
    private static void addEdge(
            Map<String, Map<String, Integer>> graph,
            String a, String b, int weight) {
        graph.computeIfAbsent(a, k -> new HashMap<>()).put(b, weight);
    }
}