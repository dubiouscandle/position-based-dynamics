package com.dubiouscandle.pointphysics.util;

import java.util.*;

public class BVH<T> {
    private Entry root;
    private int size;

    public BVH() {
        root = null;
    }

    public void rebuild() {
        List<Entry> leaves = collectLeaves();
        if (leaves.isEmpty()) {
            root = null;
            return;
        }

        Stack<RebuildTask> stack = new Stack<>();
        root = new Entry();
        stack.push(new RebuildTask(root, leaves));

        while (!stack.isEmpty()) {
            RebuildTask task = stack.pop();
            Entry node = task.node;
            List<Entry> entries = task.entries;

            if (entries.size() == 1) {
                Entry leaf = entries.get(0);
                node.setLeaf(leaf.x1, leaf.y1, leaf.x2, leaf.y2, leaf.value);
                continue;
            }

            int N = entries.size();
            float bestCost = Float.POSITIVE_INFINITY;
            int bestAxis = -1, bestSplit = -1;

            for (int axis = 0; axis < 2; axis++) {
                final int j = axis;
                entries.sort(Comparator.comparing(e -> e.getCenter(j)));

                float[] prefixArea = new float[N];
                float[] suffixArea = new float[N];
                float[] prefixBounds = new float[N * 4];
                float[] suffixBounds = new float[N * 4];

                Entry first = entries.get(0);
                prefixBounds[0] = first.x1;
                prefixBounds[1] = first.y1;
                prefixBounds[2] = first.x2;
                prefixBounds[3] = first.y2;
                prefixArea[0] = area(first.x1, first.y1, first.x2, first.y2);

                for (int i = 1; i < N; i++) {
                    Entry e = entries.get(i);
                    float[] prev = Arrays.copyOfRange(prefixBounds, (i - 1) * 4, i * 4);
                    float[] b = union(prev[0], prev[1], prev[2], prev[3], e.x1, e.y1, e.x2, e.y2);
                    System.arraycopy(b, 0, prefixBounds, i * 4, 4);
                    prefixArea[i] = area(b[0], b[1], b[2], b[3]);
                }

                Entry last = entries.get(N - 1);
                suffixBounds[(N - 1) * 4 + 0] = last.x1;
                suffixBounds[(N - 1) * 4 + 1] = last.y1;
                suffixBounds[(N - 1) * 4 + 2] = last.x2;
                suffixBounds[(N - 1) * 4 + 3] = last.y2;
                suffixArea[N - 1] = area(last.x1, last.y1, last.x2, last.y2);

                for (int i = N - 2; i >= 0; i--) {
                    Entry e = entries.get(i);
                    float[] prev = Arrays.copyOfRange(suffixBounds, (i + 1) * 4, (i + 2) * 4);
                    float[] b = union(prev[0], prev[1], prev[2], prev[3], e.x1, e.y1, e.x2, e.y2);
                    System.arraycopy(b, 0, suffixBounds, i * 4, 4);
                    suffixArea[i] = area(b[0], b[1], b[2], b[3]);
                }

                for (int i = 0; i < N - 1; i++) {
                    float cost = 1.0f + (i + 1) * prefixArea[i] + (N - i - 1) * suffixArea[i + 1];
                    if (cost < bestCost) {
                        bestCost = cost;
                        bestAxis = axis;
                        bestSplit = i + 1;
                    }
                }
            }

            final int j = bestAxis;
            entries.sort(Comparator.comparing(e -> e.getCenter(j)));

            List<Entry> leftList = new ArrayList<>(entries.subList(0, bestSplit));
            List<Entry> rightList = new ArrayList<>(entries.subList(bestSplit, entries.size()));

            Entry left = new Entry();
            Entry right = new Entry();
            node.child1 = left;
            node.child2 = right;
            left.parent = node;
            right.parent = node;

            float[] u = unionOfList(leftList);
            left.setBounds(u[0], u[1], u[2], u[3]);

            u = unionOfList(rightList);
            right.setBounds(u[0], u[1], u[2], u[3]);

            node.setBounds(Math.min(left.x1, right.x1), Math.min(left.y1, right.y1),
                           Math.max(left.x2, right.x2), Math.max(left.y2, right.y2));

            stack.push(new RebuildTask(right, rightList));
            stack.push(new RebuildTask(left, leftList));
        }

        root.parent = null;
    }

    private ArrayList<Entry> collectLeaves() {
        ArrayList<Entry> leaves = new ArrayList<>();
        if (root == null) return leaves;

        ArrayDeque<Entry> stack = new ArrayDeque<>();
        stack.add(root);

        while (!stack.isEmpty()) {
            Entry cur = stack.poll();
            if (cur.isLeaf()) {
                leaves.add(cur);
            } else {
                if (cur.child1 != null) stack.add(cur.child1);
                if (cur.child2 != null) stack.add(cur.child2);
            }
        }
        return leaves;
    }

    public void add(float x1, float y1, float x2, float y2, T e) {
        size++;
        if (root == null) {
            root = new Entry(x1, y1, x2, y2, e);
            return;
        }

        Entry cur = root;
        while (!cur.isLeaf()) {
            cur.expandToFit(x1, y1, x2, y2);
            cur = chooseNext(cur, x1, y1, x2, y2);
        }

        Entry oldLeaf = new Entry(cur.x1, cur.y1, cur.x2, cur.y2, cur.value);
        Entry newLeaf = new Entry(x1, y1, x2, y2, e);
        cur.child1 = oldLeaf;
        cur.child2 = newLeaf;
        oldLeaf.parent = cur;
        newLeaf.parent = cur;
        cur.value = null;
        cur.setBounds(Math.min(x1, oldLeaf.x1), Math.min(y1, oldLeaf.y1),
                      Math.max(x2, oldLeaf.x2), Math.max(y2, oldLeaf.y2));
    }

    private Entry chooseNext(Entry entry, float x1, float y1, float x2, float y2) {
        float cost1 = areaDelta(entry.child1, x1, y1, x2, y2);
        float cost2 = areaDelta(entry.child2, x1, y1, x2, y2);
        return cost1 < cost2 ? entry.child1 : entry.child2;
    }

    public boolean remove(float x1, float y1, float x2, float y2, T e) {
        Entry leaf = findLeaf(x1, y1, x2, y2, e);
        if (leaf == null) return false;

        Entry parent = leaf.parent;
        if (parent == null) {
            root = null;
            size--;
            return true;
        }

        Entry sibling = (parent.child1 == leaf) ? parent.child2 : parent.child1;
        Entry grand = parent.parent;

        if (grand == null) {
            root = sibling;
            sibling.parent = null;
        } else {
            if (grand.child1 == parent) {
                grand.child1 = sibling;
            } else {
                grand.child2 = sibling;
            }
            sibling.parent = grand;
            updateAncestors(grand);
        }

        size--;
        return true;
    }

    private Entry findLeaf(float x1, float y1, float x2, float y2, T e) {
        ArrayDeque<Entry> stack = new ArrayDeque<>();
        stack.add(root);

        while (!stack.isEmpty()) {
            Entry node = stack.pop();
            if (node == null) continue;
            if (node.isLeaf()) {
                if (node.value.equals(e) && node.x1 == x1 && node.y1 == y1 && node.x2 == x2 && node.y2 == y2)
                    return node;
            } else {
                if (intersects(node.child1, x1, y1, x2, y2)) stack.push(node.child1);
                if (intersects(node.child2, x1, y1, x2, y2)) stack.push(node.child2);
            }
        }
        return null;
    }

    public void query(float x1, float y1, float x2, float y2, QueryCallback<T> queryCallback) {
        if (root == null || !intersects(root, x1, y1, x2, y2)) return;

        ArrayDeque<Entry> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Entry cur = stack.pop();
            if (cur.isLeaf()) {
                if (!queryCallback.reportEntry(cur.x1, cur.y1, cur.x2, cur.y2, cur.value)) return;
            } else {
                if (intersects(cur.child1, x1, y1, x2, y2)) stack.push(cur.child1);
                if (intersects(cur.child2, x1, y1, x2, y2)) stack.push(cur.child2);
            }
        }
    }

    private void updateAncestors(Entry entry) {
        while (entry != null && !entry.isLeaf()) {
            entry.setBounds(
                Math.min(entry.child1.x1, entry.child2.x1),
                Math.min(entry.child1.y1, entry.child2.y1),
                Math.max(entry.child1.x2, entry.child2.x2),
                Math.max(entry.child1.y2, entry.child2.y2)
            );
            entry = entry.parent;
        }
    }

    private boolean intersects(Entry a, float x1, float y1, float x2, float y2) {
        return a.x2 >= x1 && a.x1 <= x2 && a.y2 >= y1 && a.y1 <= y2;
    }

    private float area(float x1, float y1, float x2, float y2) {
        return Math.abs((x2 - x1) * (y2 - y1));
    }

    private float areaDelta(Entry e, float x1, float y1, float x2, float y2) {
        float areaBefore = area(e.x1, e.y1, e.x2, e.y2);
        float[] u = union(e.x1, e.y1, e.x2, e.y2, x1, y1, x2, y2);
        float areaAfter = area(u[0], u[1], u[2], u[3]);
        return areaAfter - areaBefore;
    }

    private float[] union(float ax1, float ay1, float ax2, float ay2,
                          float bx1, float by1, float bx2, float by2) {
        return new float[]{
            Math.min(ax1, bx1), Math.min(ay1, by1),
            Math.max(ax2, bx2), Math.max(ay2, by2)
        };
    }

    private float[] unionOfList(List<Entry> list) {
        float x1 = list.get(0).x1, y1 = list.get(0).y1, x2 = list.get(0).x2, y2 = list.get(0).y2;
        for (Entry e : list) {
            x1 = Math.min(x1, e.x1);
            y1 = Math.min(y1, e.y1);
            x2 = Math.max(x2, e.x2);
            y2 = Math.max(y2, e.y2);
        }
        return new float[]{x1, y1, x2, y2};
    }

    private class RebuildTask {
        Entry node;
        List<Entry> entries;

        RebuildTask(Entry node, List<Entry> entries) {
            this.node = node;
            this.entries = entries;
        }
    }

    public interface QueryCallback<T> {
        boolean reportEntry(float x1, float y1, float x2, float y2, T e);
    }

    public class Entry {
        float x1, y1, x2, y2;
        T value;
        Entry child1, child2, parent;

        public Entry() {}

        public Entry(float x1, float y1, float x2, float y2, T value) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.value = value;
        }

        void setLeaf(float x1, float y1, float x2, float y2, T value) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.value = value;
        }

        void setBounds(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        boolean isLeaf() {
            return value != null;
        }

        float getCenter(int axis) {
            return axis == 0 ? (x1 + x2) * 0.5f : (y1 + y2) * 0.5f;
        }

        void expandToFit(float px1, float py1, float px2, float py2) {
            this.x1 = Math.min(this.x1, px1);
            this.y1 = Math.min(this.y1, py1);
            this.x2 = Math.max(this.x2, px2);
            this.y2 = Math.max(this.y2, py2);
        }
    }

    public int size() {
        return size;
    }
}
