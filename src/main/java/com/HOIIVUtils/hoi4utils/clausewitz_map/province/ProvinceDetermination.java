package com.HOIIVUtils.hoi4utils.clausewitz_map.province;

import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.Heightmap;
import com.HOIIVUtils.hoi4utils.clausewitz_map.gen.MapPoint;
import com.HOIIVUtils.hoi4utils.clausewitz_map.seed.BorderMap;
import com.HOIIVUtils.hoi4utils.clausewitz_map.seed.BorderMapping;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.concurrent.AsSynchronizedGraph;

import java.io.Serial;
import java.util.concurrent.RecursiveAction;

public interface ProvinceDetermination<P extends MapPoint> {
    void generate(BorderMapping<P> stateMapList, BorderMap stateBorderMap);

    /**
     * Province connectivity determination (->graph) using {@link RecursiveAction} for multithreading efficiency.
     *
     * @see RecursiveAction
     */
    class ForkProvinceConnectivityDetermination extends RecursiveAction {

        /** Auto-generated serialVersionUID */
        @Serial
        private static final long serialVersionUID = 9171676481286895487L;
        protected static int splitThreshold = 16;       // was 8
        private final int startY;
        private final int endY;
        private final int dy;
        private final ProvinceMapPointsList mapPoints;
        private static final Graph<MapPoint, DefaultEdge> mpGraph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        private static final Graph<MapPoint, DefaultEdge> sync_mpGraph = new AsSynchronizedGraph<>(mpGraph);
        private final Heightmap heightmap;

        /**
         * constructor (y set as 0 to height). Recommended constructor for initial initialization.
         */
        public ForkProvinceConnectivityDetermination(ProvinceMapPointsList mapPoints, Heightmap heightmap) {
            this(mapPoints, heightmap, 0, mapPoints.height());
        }

        /**
         * constructor
         * // todo pass in prev fork color determination instead of province map, heightmap?
         */
        public ForkProvinceConnectivityDetermination(ProvinceMapPointsList mapPoints, Heightmap heightmap, int startY, int endY) {
            this.mapPoints = mapPoints;
            this.heightmap = heightmap;
            this.startY = startY;
            this.endY = endY;
            dy = endY - startY;
        }

        @Override
        protected void compute() {
            if (dy <= splitThreshold) {
                computeDirectly();
                return;
            }

            int split = dy / 2;

            invokeAll(new ForkProvinceConnectivityDetermination(mapPoints, heightmap, startY, startY + split),
                    new ForkProvinceConnectivityDetermination(mapPoints, heightmap, startY + split, endY));
        }

        protected void computeDirectly() {
            try {
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < heightmap.width(); x++) {
                        int currentType = mapPoints.get(x, y).type();
                        connectNeighbors(x, y, currentType);
                    }
                }
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }
        }

        private void connectNeighbors(int x, int y, int currentType) {
            // Left neighbor
            connectIfApplicable(x, y, x - 1, y, currentType);
            // Right neighbor
            connectIfApplicable(x, y,x + 1, y, currentType);
            // Up neighbor
            connectIfApplicable(x, y, x, y - 1, currentType);
            // Down neighbor
            connectIfApplicable(x, y, x, y + 1, currentType);
        }

        private void connectIfApplicable(int x, int y, int nx, int ny, int currentType) {
            if (isValidCoordinate(nx, ny) && mapPoints.get(nx, ny).type() == currentType) {
                MapPoint p = mapPoints.get(x, y);
                MapPoint np = mapPoints.get(nx, ny);
                if (!sync_mpGraph.containsVertex(p)) {
                    sync_mpGraph.addVertex(mapPoints.get(x, y));
                }
                if (!sync_mpGraph.containsVertex(np)) {
                    sync_mpGraph.addVertex(mapPoints.get(nx, ny));
                }
                sync_mpGraph.addEdge(p, np);
                // todo optimize prev ?
            }
        }

        private boolean isValidCoordinate(int x, int y) {
            return x >= 0 && x < mapPoints.width() && y >= 0 && y < mapPoints.height();
        }

    }
}
