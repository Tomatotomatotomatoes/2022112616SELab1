package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import org.example.DijkstraAlgorithm;

public class TextToGraphConverter {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java TextToGraphConverter <file-path>");
            return;
        }

        Path filePath = Path.of(args[0]);
        Map<String, Map<String, Integer>> graph = new HashMap<>();
        String previousWord = null;

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String processedLine = processLine(line);
                if (processedLine.isEmpty()) continue;

                String[] words = processedLine.split("\\s+");
                if (words.length == 0) continue;

                // 处理跨行相邻单词对
                if (previousWord != null) {
                    addEdge(graph, previousWord, words[0]);
                }

                // 处理当前行内部的相邻单词对
                for (int i = 0; i < words.length - 1; i++) {
                    addEdge(graph, words[i], words[i + 1]);
                }

                previousWord = words[words.length - 1];
            }
            graph.put(previousWord,new HashMap<>());
        }

        // 输出图结构
        printGraph(graph);
        visualizeGraph(graph);

        //查询桥接词


        System.out.println("which function to test?\n1.query bridge word\t2.generate new string\t3.calculate shortest path\t4.page rank\t5.random walk");
        Scanner scanner = new Scanner(System.in);
        int flg = Integer.parseInt(scanner.nextLine());
        if(flg==1) {
            System.out.println("please input:");
            String word1 = scanner.nextLine();
            String word2 = scanner.nextLine();

            String res = queryBridgeWords(word1, word2, graph, true);
        }else if(flg==2){
            //根据桥接词生成新文本
            String inputString = scanner.nextLine();
            String newText = generateNewText(inputString,graph);
        }else if(flg==3){
            //查询最短路径
            String source = scanner.nextLine();
            String end = scanner.nextLine();
            String path = calcShortestPath(source,end,graph);

        }else if(flg==4){
            String pr = scanner.nextLine();
            Double res = calPageRank(pr,graph);
        }else if(flg==5){
            String path = randomWalk(graph);
            System.out.println(path);
        }







    }

    // 处理单行文本：过滤非字母字符并转换为小写
    private static String processLine(String line) {
        StringBuilder sb = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (Character.isLetter(c)) {
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(' ');
            }
        }
        return sb.toString().replaceAll("\\s+", " ").trim();
    }

    // 更新图中的边权重
    private static void addEdge(Map<String, Map<String, Integer>> graph, String a, String b) {
        graph.computeIfAbsent(a, k -> new HashMap<>())
                .merge(b, 1, Integer::sum);
    }

    // 打印图结构（按需扩展）
    private static void printGraph(Map<String, Map<String, Integer>> graph) {
        for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
            String source = entry.getKey();
            for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
                String target = edge.getKey();
                int weight = edge.getValue();
                System.out.printf("[%s -> %s] : %d\n", source, target, weight);
            }
        }
    }
    public  static String queryBridgeWords(String word1, String word2,Map<String, Map<String, Integer>> graph,boolean flag){
        if(!graph.containsKey(word1)|| !graph.containsKey(word2)){
            return String.format("No %s or %s in the graph",word1,word2);//No word1 or word2 in the graph
        }else{
            ArrayList<String> result = new ArrayList<>();
            Map<String,Integer> wd1 =  graph.get(word1);;
            for(Map.Entry<String,Integer> edge:wd1.entrySet()){
                String edg = edge.getKey();
                Map<String,Integer> edg1 = graph.get(edg);
                if(edg1.containsKey(word2)){
                    result.add(edg);
                }
            }
            if(!result.isEmpty()){
                StringBuilder res = new StringBuilder(String.format("The bridge words from %s to %s are:", word1, word2));
                for(String bridgeword:result){
                    res.append(bridgeword);
                    res.append(",");

                }
                return res.toString();
            }else{
                return String.format("No bridge words from %s to %s!",word1,word2);
            }
        }

    }

    public  static ArrayList<String> queryBridgeWords(String word1, String word2,Map<String, Map<String, Integer>> graph) {


        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return new ArrayList<String>();
        } else {
            ArrayList<String> result = new ArrayList<>();

            Map<String, Integer> wd1 = graph.get(word1);

            for (Map.Entry<String, Integer> edge : wd1.entrySet()) {
                String edg = edge.getKey();
                Map<String, Integer> edg1 = graph.get(edg);
                if (edg1.containsKey(word2)) {
                    result.add(edg);
                }
            }
            return result;

        }
    }


    public static String generateNewText(String inputText,Map<String, Map<String, Integer>> graph){
        String[] words = inputText.split("\\s");

        ArrayList<node> Res = new ArrayList<>();
        for(int i = 0;i<words.length-1;i++){
            String wd1 = words[i];
            String wd2 = words[i+1];
            ArrayList<String> res = queryBridgeWords(wd1,wd2,graph);
            if(res.isEmpty()){
                //do nothing
            }else if(res.size()==1){
                Res.add(new node(res.get(0),i));
            }else{
                //生成随机数0~res.length-1
                int idx = (int) (Math.random() * (res.size() - 1));
                Res.add(new node(res.get(idx),i));
            }
        }

        //得到了map以后，进行字符串插入
        int idx_s = 0;
        int idx_w = 0;

        ArrayList<String> Words = new ArrayList<>(Arrays.asList(words));
        for(int index = 0;index< words.length-1;index++){
            if (Res.get(idx_s).idx == index) {

                Words.add(idx_w+1,Res.get(idx_s).vec);
                idx_s+=1;
                idx_w+=1;
            }else{

            }
            idx_w+=1;

        }
        //Words包含更新字符串的单词
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0;i< Words.size()-1;i++){
            stringBuilder.append(Words.get(i));
            stringBuilder.append(" ");
        }
        stringBuilder.append(Words.get(Words.size()-1));
        return stringBuilder.toString();


    }
    private static void visualizeGraph(Map<String, Map<String, Integer>> graph) {
        System.setProperty("org.graphstream.ui", "swing");
        Graph graphStream = new SingleGraph("Word Graph");

        // 添加节点和边
        for (String source : graph.keySet()) {
            graphStream.addNode(source).setAttribute("ui.label", source);

        }

        for(String source : graph.keySet()){
            for (Map.Entry<String, Integer> edge : graph.get(source).entrySet()) {
                String target = edge.getKey();
                int weight = edge.getValue();
                String edgeId = source + "-" + target;
                graphStream.addEdge(edgeId, source, target, true)
                        .setAttribute("ui.label", weight);
            }
        }

        // 设置样式
        graphStream.setAttribute("ui.stylesheet",
                "node { fill-color: #a0d8ef; size: 20px; text-alignment: under; }" +
                        "edge { fill-color: #777; }");

        // 显示窗口
        Viewer viewer = graphStream.display();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
    }

    public static String calcShortestPath(String word1, String word2,Map<String, Map<String, Integer>> graph){
        Map<String, Integer> result = DijkstraAlgorithm.dijkstra(graph,word1);
        ArrayList<String> path_node = new ArrayList<>();
        String cur = word2;
        while(!cur.equals(word1)) {
            path_node.add(cur);
            cur = DijkstraAlgorithm.pre.get(cur);

        }
        //path_node.add(cur);
        path_node.add(word1);
        //反向构造序列
        Collections.reverse(path_node);
        StringBuilder stringBuilder = new StringBuilder();
        path_node.forEach((e)->{
            stringBuilder.append(e);
            stringBuilder.append("->");
        });
        stringBuilder.delete(stringBuilder.length()-2,stringBuilder.length());
        return stringBuilder.toString();
    }
    public static Double calPageRank(String word,Map<String, Map<String, Integer>> graph){
        //初始化pr值
        Map<String,Double> PR = new HashMap<>();
        for(String node: graph.keySet()){
            PR.put(node,0.85);
        }
        Double d = 0.85; //阻尼因子
        //初始化B_u
        Map<String,Set<String>> B = new HashMap<>();
        for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
            String source = entry.getKey();
            for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
                String target = edge.getKey();
                B.computeIfAbsent(target,k -> new HashSet<>()).add(source);
                //System.out.printf("[%s -> %s] : %d\n", source, target, weight);
            }
        }
        //初始化出度矩阵
        Map<String,Integer> out = new HashMap<>();
        for(String node:graph.keySet()){
            out.put(node,graph.get(node).size());
        }
        //计算pr值
        int N = graph.size();
        for(String node:PR.keySet()){
            Double tmp = 0.0;
            for(String point_to:B.get(node)){
                tmp+=PR.get(point_to)/out.get(point_to);
            }
            tmp*=d;
            tmp+=(1-d)/N;
            PR.put(node,tmp);

        }
        return PR.get(word);


    }
    public static String randomWalk(Map<String, Map<String, Integer>> graph)
    {
        System.out.println("start randomWalk");
        Random random = new Random();
        ArrayList<String> novelist = new ArrayList<>(graph.keySet());
        String source = novelist.get(random.nextInt(novelist.size()));
        String tmp = source;
        String tmp_to = null;
        Set<edge> visited = new HashSet<>();

        ArrayList<String> to_list = new ArrayList<>(graph.get(tmp).keySet());
        if(to_list.isEmpty()){
            System.out.println("节点无出度");
            return source;
        }
        tmp_to = to_list.get(random.nextInt(to_list.size()));
        edge cur = new edge(tmp,tmp_to);
        System.out.println("continue?");
        Scanner scanner = new Scanner(System.in);
        String is_contin = scanner.nextLine();
        StringBuilder stringBuilder = new StringBuilder();
        if(is_contin.equals("y")){
            //visited.add(cur);
            stringBuilder.append(String.format("%s->%s",cur.from,cur.to));
        }else{
            return source;
        }

        do{
            visited.add(cur);
            tmp = tmp_to;
            to_list = new ArrayList<>(graph.get(tmp).keySet());
            tmp_to = to_list.get(random.nextInt(to_list.size()));
            System.out.println("continue?");
            is_contin = scanner.nextLine();
            if(is_contin.equals("y")){
                cur = new edge(tmp,tmp_to);

                stringBuilder.append(String.format("->%s",cur.to));
            }else{
                break;
            }



        }while((!visited.contains(cur))&&(!graph.get(tmp_to).isEmpty()));

        return stringBuilder.toString();

    }


}