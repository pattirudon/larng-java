package jp.co.pattirudon.larng.gui;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

import javax.swing.table.DefaultTableModel;

import jp.co.pattirudon.larng.Sampler;
import jp.co.pattirudon.larng.pokemon.Gender;
import jp.co.pattirudon.larng.pokemon.Pokemon;
import jp.co.pattirudon.larng.random.Xoroshiro;

public class GenerateTableLogic implements Runnable {

    private DefaultTableModel model;
    private String seedStr;
    private String shinyRollsStr;
    private int genderRatioInd;
    private boolean showPath;
    private String pathsStr;
    private boolean showMult;
    private String numSpawnsStr;
    private String maxDepthStr;
    private boolean shinyOnly;
    public static final int MAX_ROW_COUNT = 10_000;

    public GenerateTableLogic(DefaultTableModel model) {
        this.model = model;
    }

    private void updateTableShowingPath(long seed, int[] paths, int numShinyRolls, Gender.Ratio genderRatio) {
        /**
         * [1] -> group, [2] -> spawner [3] -> fixed
         */
        Xoroshiro[] rngs = { new Xoroshiro(seed), null, null };
        long[] seeds = { seed, 0L, 0L };
        int[] subpath = new int[paths.length];
        for (int i = 0; i < paths.length; i++) {
            int numPokeOnPhase = paths[i];
            for (int n = 1; n <= numPokeOnPhase; n++) {
                seeds[1] = rngs[0].nextLong();
                rngs[1] = new Xoroshiro(seeds[1]);
                rngs[0].nextLong();
                int numGuaranteedIVs = 0;
                rngs[1].nextLong();
                seeds[2] = rngs[1].nextLong();
                rngs[2] = new Xoroshiro(seeds[2]);
                Pokemon pokemon = Sampler.getPokemon(rngs[2], numShinyRolls, numGuaranteedIVs, 2, genderRatio);
                subpath[i] = n;
                if (!shinyOnly || pokemon.isShiny())
                    insert(Arrays.copyOf(subpath, i + 1), pokemon, seeds[0], seeds[1], seeds[2]);
            }
            seeds[0] = rngs[0].nextLong();
            rngs[0] = new Xoroshiro(seeds[0]);
        }
    }

    private void updateTableShowingMultiSpawner(long seed, int numShinyRolls, Gender.Ratio genderRatio, int numSpawns,
            int maxDepth, boolean shinyOnly) {
        Xoroshiro[] rngs = { null, null, null };
        long[] seeds = { seed, 0L, 0L };
        Node root = new Node(null, 0, seed);
        Deque<Node> queue = new LinkedList<>();
        queue.add(root);
        while (queue.size() > 0) {
            Node v = queue.pollFirst();
            if (v.getDepth() >= maxDepth)
                continue;
            seeds[0] = v.getSeed();
            rngs[0] = new Xoroshiro(seeds[0]);
            for (int weight = 1; weight <= numSpawns; weight++) {
                seeds[1] = rngs[0].nextLong();
                rngs[1] = new Xoroshiro(seeds[1]);
                rngs[0].nextLong();
                int numGuaranteedIVs = 0;
                rngs[1].nextLong();
                seeds[2] = rngs[1].nextLong();
                rngs[2] = new Xoroshiro(seeds[2]);
                Pokemon pokemon = Sampler.getPokemon(rngs[2], numShinyRolls, numGuaranteedIVs, 2, genderRatio);
                Node u = new Node(v, weight, rngs[0].clone().nextLong());
                if (!shinyOnly || pokemon.isShiny())
                    insert(u.getPath(), pokemon, seeds[0], seeds[1], seeds[2]);
                queue.addLast(u);
            }
        }
    }

    private void checkInputs(int numSpawns, int maxDepth) throws IllegalArgumentException {
        if (shinyOnly)
            return;
        if (numSpawns == 1 && maxDepth > MAX_ROW_COUNT)
            throw new IllegalArgumentException("too large table shall result");
        if ((maxDepth + 1) * Math.log(numSpawns) > Math.log(MAX_ROW_COUNT * (numSpawns - 1) + 1))
            throw new IllegalArgumentException("too large table shall result");
    }

    private void insert(int[] path, Pokemon pokemon, long groupSeed, long spawnerSeed, long fixedSeed) {
        model.addRow(new Object[] { String.valueOf(model.getRowCount()), ArrayFormatter.toString(path),
                pokemon.nature.name(), pokemon.gender.gender.name(), String.valueOf(pokemon.ability),
                String.valueOf(pokemon.ivs[0]), String.valueOf(pokemon.ivs[1]), String.valueOf(pokemon.ivs[2]),
                String.valueOf(pokemon.ivs[3]), String.valueOf(pokemon.ivs[4]), String.valueOf(pokemon.ivs[5]),
                String.valueOf(pokemon.isShiny()), String.format("%016x", groupSeed),
                String.format("%016x", spawnerSeed), String.format("%016x", fixedSeed),
                String.format("%08x", pokemon.ec), String.format("%08x", pokemon.tid),
                String.format("%08x", pokemon.pid), });
    }

    private void truncate() {
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
    }

    @Override
    public void run() {
        try {
            long seed = Long.parseUnsignedLong(seedStr, 16);
            int shinyRolls = Integer.parseInt(shinyRollsStr);
            Gender.Ratio genderRatio = Gender.Ratio.values()[genderRatioInd];
            if (showPath) {
                int[] paths = Arrays.stream(pathsStr.split("\\s*,\\s*")).mapToInt(Integer::parseInt).toArray();
                truncate();
                updateTableShowingPath(seed, paths, shinyRolls, genderRatio);
            } else if (showMult) {
                int numSpawns = Integer.parseInt(numSpawnsStr);
                int maxDepth = Integer.parseInt(maxDepthStr);
                checkInputs(numSpawns, maxDepth);
                truncate();
                updateTableShowingMultiSpawner(seed, shinyRolls, genderRatio, numSpawns, maxDepth, shinyOnly);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Node {
        int depth;
        int weight;
        int sum;
        Node parent;
        int[] path;
        long seed;

        public Node(Node parent, int weight, long seed) {
            this.parent = parent;
            if (isRoot()) {
                this.depth = 0;
                this.path = new int[] {};
            } else {
                this.depth = 1 + getParent().getDepth();
                this.path = Arrays.copyOf(parent.getPath(), depth);
                this.path[depth - 1] = weight;
            }
            this.weight = weight;
            this.seed = seed;
            this.sum = isRoot() ? getWeight() : getWeight() + getParent().getSum();
        }

        public boolean isRoot() {
            return getParent() == null;
        }

        public int getWeight() {
            return weight;
        }

        public int getSum() {
            return sum;
        }

        public Node getParent() {
            return parent;
        }

        public long getSeed() {
            return seed;
        }

        @Override
        public String toString() {
            return (isRoot() ? "" : getParent().toString() + "|") + getWeight();
        }

        public int getDepth() {
            return depth;
        }

        public int[] getPath() {
            return path;
        }
    }

    public void setSeedStr(String seedStr) {
        this.seedStr = seedStr;
    }

    public void setShinyRollsStr(String shinyRollsStr) {
        this.shinyRollsStr = shinyRollsStr;
    }

    public void setGenderRatioInd(int genderRatioInd) {
        this.genderRatioInd = genderRatioInd;
    }

    public void setShowPath(boolean showPath) {
        this.showPath = showPath;
    }

    public void setPathsStr(String pathsStr) {
        this.pathsStr = pathsStr;
    }

    public void setModel(DefaultTableModel model) {
        this.model = model;
    }

    public void setShowMult(boolean showMult) {
        this.showMult = showMult;
    }

    public void setNumSpawnsStr(String numSpawnsStr) {
        this.numSpawnsStr = numSpawnsStr;
    }

    public void setMaxDepthStr(String maxDepthStr) {
        this.maxDepthStr = maxDepthStr;
    }

    public void setShinyOnly(boolean shinyOnly) {
        this.shinyOnly = shinyOnly;
    }

}
