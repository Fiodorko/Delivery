package com.example.fiodorko.delivery.Graph_package;

import android.util.Log;

import com.example.fiodorko.delivery.Delivery;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Obahuje algoritmy na hľadanie najkratšej riešením problému obchodného cestujúceho
 */
public class Algorithms {

    private static final String TAG = "Algorithms";

    private static void resetVertexSets(Graph graph) {
        for (Vertex v : graph.getVertices()) {
            v.set = v.index;
        }
    }

    private static void resetEdges(Graph graph) {
        for (Edge e : graph.getEdges()) {
            e.ab = false;
            e.ba = false;
        }
    }

    /**
     * Nájde v grafovej podobe objednávok najkratšiu cestu podľa Pažravej metódy.
     *
     * @param graph - predstavuje grafovú poddobu objednávok
     * @return Utriedený zoznam objednávok
     */
    public static ArrayList<Delivery> greedy(Graph graph) {
        ArrayList<Vertex> bestPath = new ArrayList<>();
        ArrayList<Vertex> tmp_vertices = new ArrayList<>(graph.getVertices());
        ArrayList<Delivery> tmp_deliveries = new ArrayList<>();

        Vertex actual = tmp_vertices.remove(0);
        bestPath.add(actual);

        while (bestPath.size() < graph.getVertices().size()) {
            Vertex next = null;
            double min = Double.MAX_VALUE;
            for (Vertex vertex : tmp_vertices) {
                if (graph.getDistanceMatrix()[actual.index][vertex.index] < min) {
                    next = vertex;
                    min = graph.getDistanceMatrix()[actual.index][vertex.index];
                }
            }

            bestPath.add(tmp_vertices.remove(tmp_vertices.indexOf(next)));
            assert next != null;
            tmp_deliveries.add(graph.getDeliveries().get(next.index - 1));
            actual = next;
        }

        return tmp_deliveries;
    }

    /**
     * Nájde minimálnu kostru grafu
     *
     * @param graph - grafická podoba objednávok
     * @return zoznam hrán ktoré tvoria kostru
     */
    private static LinkedList<Edge> spanningTree(Graph graph) {
        resetVertexSets(graph);
        ArrayList<Vertex> vertices = new ArrayList<>();
        LinkedList<Edge> edges = sortedEdges(graph.getEdges());
        LinkedList<Edge> sequence = new LinkedList<>();

        for (Edge edge : edges) {
            if (edge.a.set != edge.b.set) {
                sequence.add(edge);
                Log.d(TAG, "Spanning tree was extended by edge:" + edge.String());
                if (vertices.contains(edge.a) && vertices.contains(edge.b)) {
                    int tmp = edge.a.set;
                    for (Vertex vertex : vertices) {
                        if (vertex.set == tmp) {
                            vertex.set = edge.b.set;
                        }
                    }
                } else if (vertices.contains(edge.a)) {
                    vertices.add(edge.b);
                    edge.b.set = edge.a.set;
                } else if (vertices.contains(edge.b)) {
                    vertices.add(edge.a);
                    edge.a.set = edge.b.set;
                } else {
                    vertices.add(edge.a);
                    vertices.add(edge.b);
                    edge.a.set = edge.b.set;
                }


            }
        }
        Log.d(TAG, "Spanning tree size:" + sequence.size());
        graph.setSpannningTree(sequence);
        return sequence;
    }


    /**
     * Utriedi hrany podľa dĺžky od najkratšej po najdlhšiu.
     *
     * @param edges - zoznam hrán ktoré sa budú triediť
     * @return utriedený zoznam hrán
     */
    private static LinkedList<Edge> sortedEdges(LinkedList<Edge> edges) {
        Log.d(TAG, "Sorting edges for minimal spanning tree algorithm...");
        LinkedList<Edge> sorted;
        LinkedList<LinkedList<Edge>> sublists = new LinkedList<>();
        LinkedList<Edge> unsorted = new LinkedList<>(edges);
        sublists.add(unsorted);

        while (sublists.size() != edges.size()) {

            LinkedList<Edge> tmp = sublists.remove(0);
            if (tmp.size() == 1) {
                sublists.add(tmp);
            } else {
                sublists.add(new LinkedList<>(tmp.subList(0, tmp.size() / 2)));
                sublists.add(new LinkedList<>(tmp.subList(tmp.size() / 2, tmp.size())));
            }

        }

        while (sublists.size() != 1) {
            LinkedList<Edge> tmp_1 = sublists.remove(0);
            LinkedList<Edge> tmp_2 = sublists.remove(0);
            sublists.add(merge(tmp_1, tmp_2));
        }

        sorted = sublists.get(0);
        return sorted;
    }


    private static LinkedList<Edge> merge(LinkedList<Edge> tmp_1, LinkedList<Edge> tmp_2) {
        LinkedList<Edge> merged = new LinkedList<>();
        while (tmp_1.size() > 0 || tmp_2.size() > 0) {
            if (tmp_1.size() == 0) merged.add(tmp_2.remove(0));
            else if (tmp_2.size() == 0) merged.add(tmp_1.remove(0));
            else if (tmp_1.get(0).weight < tmp_2.get(0).weight) {
                merged.add(tmp_1.remove(0));
            } else {
                merged.add(tmp_2.remove(0));
            }
        }
        return merged;
    }

    /**
     * Predstavuje algoritmus zdvojenia kostry
     *
     * @param graph - grafická podoba objednávok
     * @return utriedený list objednávok
     */
    public static ArrayList<Delivery> doubleSpanningTree(Graph graph) {
        ArrayList<Delivery> deliveries = graph.getDeliveries();

        LinkedList<Edge> minimalSpanningTree;

        if (graph.getSpannningTree().isEmpty())
            minimalSpanningTree = new LinkedList<>(spanningTree(graph));
        else minimalSpanningTree = graph.getSpannningTree();

        LinkedList<Edge> doubleSpanningTree = new LinkedList<>();

        resetEdges(graph);
        resetVertexSets(graph);

        Vertex start = graph.getVertices().get(0);
        ArrayList<Vertex> vertices = new ArrayList<>();

        int vertex = start.index;
        vertices.add(start);

        Log.d(TAG, "Findind Tarry's sequence in graph...");
        while (doubleSpanningTree.size() < minimalSpanningTree.size() * 2) {
            Log.d(TAG, "Size: " + doubleSpanningTree.size());
            Edge tmp = null;
            boolean direction = false;
            for (Edge edge : minimalSpanningTree) {
                if (edge.a.index == vertex) {
                    if (!edge.ab) {
                        if (!edge.first) {
                            doubleSpanningTree.add(edge);
                            if (!vertices.contains(edge.b)) edge.first = true;
                            Log.d(TAG, "Bola pridana strana:" + edge.String() + "smer a->b");
                            edge.ab = true;
                            vertex = edge.b.index;
                            tmp = null;
                            break;
                        } else {
                            tmp = edge;
                            direction = true;
                        }
                    }
                }
                if (edge.b.index == vertex) {
                    if (!edge.ba) {
                        if (!edge.first) {
                            doubleSpanningTree.add(edge);
                            Log.d(TAG, "Bola pridana strana:" + edge.String() + "smer b->a");
                            if (!vertices.contains(edge.b)) edge.first = true;
                            edge.ba = true;
                            vertex = edge.a.index;
                            tmp = null;
                            break;
                        } else {
                            tmp = edge;
                            direction = false;
                        }
                    }
                }
            }

            if (tmp != null) {
                doubleSpanningTree.add(tmp);
                Log.d(TAG, "Bola pridana strana:" + tmp.String());
                if (direction) {
                    tmp.ab = true;
                    vertex = tmp.b.index;
                    Log.d(TAG, "Smer a->b");
                } else {
                    tmp.ba = true;
                    vertex = tmp.a.index;
                    Log.d(TAG, "Smer b->a");
                }
            }
        }

        Log.d(TAG, "Tarry's sequence completed!");
        Log.d(TAG, "Finding Hamiltonian path...");

        ArrayList<Delivery> result = new ArrayList<>();

        for (Edge edge : doubleSpanningTree) {
            int index_a = edge.a.index - 1;
            int index_b = edge.b.index - 1;
            if (index_a >= 0) {
                if (!result.contains(deliveries.get(index_a))) result.add(deliveries.get(index_a));
            }
            if (index_b >= 0) {
                if (!result.contains(deliveries.get(index_b))) result.add(deliveries.get(index_b));
            }
        }

        for (Delivery delivery : result) {
            Log.d(TAG, delivery.getId() + "!");
        }

        Log.d(TAG, "Hamiltonian path completed!");

        return result;
    }

    /**
     * Predstavuje algoritmus vkladacej heuristiky
     *
     * @param graph - grafická podoba objednávok
     * @return utriedený zoznam objednávok
     */
    public static ArrayList<Delivery> insertionHeuristic(Graph graph) {
        Log.d(TAG, "Building hamiltonian path...(Insertion Heuristic)");
        ArrayList<Delivery> deliveries = new ArrayList<>();

        ArrayList<Vertex> tmp_vertices = new ArrayList<>();

        LinkedList<Edge> cycle = new LinkedList<>();

        Edge minimalEdge = minimalEdge(graph.getEdges());

        tmp_vertices.add(minimalEdge.a);
        tmp_vertices.add(minimalEdge.b);

        cycle.add(minimalEdge);

        double minimalWeight = Double.MAX_VALUE;
        Vertex v = null;

        for (Vertex vertex : graph.getVertices()) {
            double va = graph.getDistanceMatrix()[vertex.index][minimalEdge.a.index];
            double vb = graph.getDistanceMatrix()[vertex.index][minimalEdge.b.index];

            if (!tmp_vertices.contains(vertex)) {
                if (va + vb < minimalWeight || v == null) {
                    minimalWeight = va + vb;
                    v = vertex;
                }
            }
        }

        cycle.add(graph.getEdge(v, minimalEdge.a));
        cycle.add(graph.getEdge(v, minimalEdge.b));

        tmp_vertices.add(v);

        Log.d(TAG, "Initial Cycle:");
        for (Edge edge : cycle) {
            Log.d(TAG, edge.String());
        }

        while (tmp_vertices.size() < graph.getVertices().size()) {
            double min = Double.MAX_VALUE;
            Edge e = null;
            Vertex next_v = null;

            for (Edge edge : cycle) {
                double edgeMin = Double.MAX_VALUE;
                Vertex next = null;
                for (Vertex vertex : graph.getVertices()) {
                    if (!tmp_vertices.contains(vertex)) {
                        double va = graph.getDistanceMatrix()[vertex.index][edge.a.index];
                        double vb = graph.getDistanceMatrix()[vertex.index][edge.b.index];
                        if (va + vb - edge.weight < edgeMin || next == null) {
                            edgeMin = va + vb - edge.weight;
                            next = vertex;
                        }
                    }
                }

                if (edgeMin < min) {
                    min = edgeMin;
                    e = edge;
                    next_v = next;
                }

            }

            assert e != null;
            Log.d(TAG, "Replacing edge: " + e.String() + " for: " + graph.getEdge(next_v, e.a).String() + " and " + graph.getEdge(next_v, e.b).String());

            int indexOfE = cycle.indexOf(e);

            if (cycle.size() > 1 && indexOfE < cycle.size() - 1) {
                if (cycle.get(indexOfE + 1).contains(e.a)) {
                    cycle.add(indexOfE, graph.getEdge(next_v, e.a));
                    cycle.add(indexOfE, graph.getEdge(next_v, e.b));
                } else if (cycle.get(indexOfE + 1).contains(e.b)) {
                    cycle.add(indexOfE, graph.getEdge(next_v, e.b));
                    cycle.add(indexOfE, graph.getEdge(next_v, e.a));
                }
            } else if (indexOfE - 1 > 0) {
                if (cycle.get(indexOfE - 1).contains(e.a)) {
                    cycle.add(indexOfE, graph.getEdge(next_v, e.b));
                    cycle.add(indexOfE, graph.getEdge(next_v, e.a));
                    Log.d(TAG, "Pridana hrana: " + e.String());
                } else if (cycle.get(indexOfE - 1).contains(e.b)) {
                    cycle.add(indexOfE, graph.getEdge(next_v, e.a));
                    cycle.add(indexOfE, graph.getEdge(next_v, e.b));
                    Log.d(TAG, "Pridana hrana: " + e.String());
                }
            } else {
                cycle.add(indexOfE, graph.getEdge(next_v, e.b));
                cycle.add(indexOfE, graph.getEdge(next_v, e.a));
            }

            cycle.remove(e);
            if (!tmp_vertices.contains(next_v)) {
                tmp_vertices.add(next_v);
                assert next_v != null;
                Log.d(TAG, "Pridany vrchol:" + next_v.index);
            }
        }

        while (!(cycle.get(0).contains(graph.getStart()) && cycle.get(cycle.size() - 1).contains(graph.getStart()))) {
            Edge tmp = cycle.remove(0);
            cycle.add(tmp);
            Log.d(TAG, "Removing edge: " + tmp.String() + "bola presunuta lebo neobsahuje 0");
        }

        int index = 0;

        while (cycle.size() > 1) {
            Edge tmp = cycle.remove(0);
            index = tmp.a.index == index ? tmp.b.index : tmp.a.index;
            cycle.remove(tmp);
            deliveries.add(graph.getDeliveries().get(index - 1));
            Log.d(TAG, "Removing edge: " + tmp.String() + "Remaining:" + cycle.size());
        }


        return deliveries;
    }


    private static Edge minimalEdge(LinkedList<Edge> edges) {
        Edge min = edges.get(0);
        for (Edge edge : edges) {
            if (edge.weight < min.weight) min = edge;
        }
        return min;
    }


    public static ArrayList<Delivery> permutations(Graph graph) {
        ArrayList<Delivery> result = new ArrayList<>();
        ArrayList<Vertex> tmp = new ArrayList<>();
        tmp.add(graph.getStart());


        for (Vertex v : backTrack(tmp, graph)) {
            if (v.index > 0) result.add(graph.getDeliveries().get(v.index - 1));
        }

        return result;
    }

    private static ArrayList<Vertex> backTrack(ArrayList<Vertex> cycle, Graph graph) {
        ArrayList<Vertex> result = new ArrayList<>(cycle);
        for (Vertex vertex : graph.getVertices()) {
            ArrayList<Vertex> tmp = new ArrayList<>(cycle);
            if (!cycle.contains(vertex)) {
                tmp.add(vertex);
                tmp = backTrack(tmp, graph);
                if (cycleWeight(tmp, graph) < cycleWeight(result, graph) || result.size() < tmp.size())
                    result = tmp;
            }
        }

        return result;
    }

    private static double cycleWeight(ArrayList<Vertex> vertices, Graph graph) {
        double result = 0;
        for (int i = 1; i < vertices.size(); i++) {
            result += graph.getDistanceMatrix()[vertices.get(i).index][vertices.get(i - 1).index];
        }
        return result;
    }

}
