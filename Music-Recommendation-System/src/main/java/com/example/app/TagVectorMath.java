package com.example.app;

import com.example.model.Tag;
import com.example.model.Track;

import java.util.*;

public final class TagVectorMath {

    private TagVectorMath() {}

    public static Map<String, Double> computeIdf(Collection<Track> pool) {
        Map<String, Integer> df = new HashMap<>();
        int N = 0;
        for (Track t : pool) {
            if (t == null || t.getTags() == null || t.getTags().isEmpty()) continue;
            N++;
            Set<String> seen = new HashSet<>();
            for (Tag tag : t.getTags()) {
                if (tag == null) continue;
                String name = tag.getName();
                if (name == null) continue;
                if (seen.add(name)) {
                    df.merge(name, 1, Integer::sum);
                }
            }
        }
        Map<String, Double> idf = new HashMap<>(df.size());
        for (var e : df.entrySet()) {
            double val = Math.log((N + 1.0) / (e.getValue() + 1.0)) + 1.0;
            idf.put(e.getKey(), val);
        }
        return idf;
    }

    public static Map<String, Double> tfidfVectorOfTrack(Track t, Map<String, Double> idf) {
        if (t == null || t.getTags() == null || t.getTags().isEmpty()) return Map.of();
        Map<String, Double> vec = new HashMap<>();
        for (Tag tag : t.getTags()) {
            if (tag == null) continue;
            String k = tag.getName();
            if (k == null) continue;
            Double idfK = idf.get(k);
            if (idfK != null) vec.put(k, idfK);
        }
        return l2Normalize(vec);
    }

    public static Map<String, Double> l2Normalize(Map<String, Double> v) {
        if (v == null || v.isEmpty()) return (v == null ? Map.of() : v);
        double norm2 = 0.0;
        for (double x : v.values()) norm2 += x * x;
        if (norm2 == 0.0) return v;
        double inv = 1.0 / Math.sqrt(norm2);
        Map<String, Double> out = new HashMap<>(v.size());
        for (var e : v.entrySet()) out.put(e.getKey(), e.getValue() * inv);
        return out;
    }

    public static Map<String, Double> addVectors(Map<String, Double> a, Map<String, Double> b) {
        if (a == null || a.isEmpty()) return (b == null ? Map.of() : new HashMap<>(b));
        if (b == null || b.isEmpty()) return new HashMap<>(a);
        Map<String, Double> out = new HashMap<>(a);
        for (var e : b.entrySet()) out.merge(e.getKey(), e.getValue(), Double::sum);
        return out;
    }

    public static double cosineSparse(Map<String, Double> a, Map<String, Double> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return 0.0;
        Map<String, Double> s = a.size() <= b.size() ? a : b;
        Map<String, Double> l = a.size() <= b.size() ? b : a;
        double dot = 0.0;
        for (var e : s.entrySet()) {
            Double bv = l.get(e.getKey());
            if (bv != null) dot += e.getValue() * bv;
        }
        return dot;
    }
}