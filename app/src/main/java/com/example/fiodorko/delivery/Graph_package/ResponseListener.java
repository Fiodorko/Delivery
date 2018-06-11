package com.example.fiodorko.delivery.Graph_package;

/**
 * Vracia maticu ohodnotení hrán z OSRM API do Grafu
 */
public interface ResponseListener {
    void onResponseReceive(double[][] matrix);

}
