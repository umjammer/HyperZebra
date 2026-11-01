/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.github.hyperZebra;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;


/**
 * Resolves classic Mac OS file references (a bare name or a colon separated
 * path like "Myst:Myst Graphics:ALLRes") to files of an extracted HFS volume.
 * <p>
 * Extracted files keep their data fork as is and their resource fork as "&lt;name&gt;.rsrc".
 * The volume root is the folder of the first HyperCard stack opened, files are searched below it.
 */
public final class MacFiles {

    private static final Logger logger = System.getLogger(MacFiles.class.getName());

    private MacFiles() {
    }

    /** search roots, the first one is the "volume" */
    private static final Set<Path> roots = new LinkedHashSet<>();

    /** normalized file name -> files */
    private static final Map<String, List<Path>> index = new HashMap<>();

    private static final int MAX_DEPTH = 4;

    /** registers the folder of a stack file as a search root */
    public static synchronized void addRootOf(Path file) {
        Path dir = file.toAbsolutePath().normalize().getParent();
        if (dir != null && roots.add(dir)) {
            index(dir);
        }
    }

    private static void index(Path dir) {
        try (Stream<Path> s = Files.walk(dir, MAX_DEPTH)) {
            s.filter(p -> !p.equals(dir)).forEach(p -> {
                String name = p.getFileName().toString();
                if (name.endsWith(".rsrc") || name.startsWith(".")) return;
                index.computeIfAbsent(normalize(name), k -> new ArrayList<>()).add(p);
            });
        } catch (IOException e) {
            logger.log(Level.WARNING, "cannot index " + dir + ": " + e);
        }
    }

    /** for comparing names: NFC, lower case, '/' and ':' are the same char on HFS */
    static String normalize(String name) {
        String s = Normalizer.normalize(name, Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
        return s.replace('/', ':');
    }

    /** @return the last component of a mac path */
    static String leafName(String macPath) {
        String s = macPath;
        while (s.endsWith(":")) s = s.substring(0, s.length() - 1);
        int p = s.lastIndexOf(':');
        return p >= 0 ? s.substring(p + 1) : s;
    }

    /**
     * @param ref a bare name, a mac path or a host path
     * @param dirs preferred mac folders (e.g. HyperCard's "stacks" search path), nullable
     * @return the file, null if not found
     */
    public static synchronized Path find(String ref, List<String> dirs) {
        if (ref == null || ref.isEmpty()) return null;
        // host path
        try {
            Path p = Path.of(ref);
            if (Files.exists(p) && !Files.isDirectory(p)) return p;
        } catch (Exception ignore) {
        }
        String leaf = leafName(ref);
        List<Path> candidates = index.get(normalize(leaf));
        if (candidates == null || candidates.isEmpty()) {
            logger.log(Level.DEBUG, "not found: " + ref);
            return null;
        }
        if (candidates.size() == 1) return candidates.get(0);
        // prefer the one whose parent folder names match the mac path
        List<String> hints = new ArrayList<>();
        if (ref.contains(":")) hints.add(ref);
        if (dirs != null) dirs.forEach(d -> hints.add(d + (d.endsWith(":") ? "" : ":") + leaf));
        for (String hint : hints) {
            String[] comps = hint.split(":");
            Path best = null;
            int bestScore = -1;
            for (Path c : candidates) {
                int score = 0;
                Path parent = c.getParent();
                for (int i = comps.length - 2; i >= 0 && parent != null; i--) {
                    if (parent.getFileName() != null && normalize(parent.getFileName().toString()).equals(normalize(comps[i]))) {
                        score++;
                        parent = parent.getParent();
                    } else {
                        break;
                    }
                }
                if (score > bestScore) {
                    best = c;
                    bestScore = score;
                }
            }
            if (bestScore > 0) return best;
        }
        return candidates.get(0);
    }

    /** @return the resource fork file of the file, null if none */
    public static Path resourceFork(Path file) {
        Path r = file.resolveSibling(file.getFileName() + ".rsrc");
        return Files.isRegularFile(r) ? r : null;
    }

    /** @return lines of the global variable "stacks" (HyperCard's search path) */
    static List<String> stacksSearchPath() {
        List<String> list = new ArrayList<>();
        try {
            String v = TTalk.globalData != null ? TTalk.getGlobalValue("stacks") : null;
            if (v != null) {
                for (String line : v.split("[\\r\\n]")) {
                    if (!line.isBlank()) list.add(line.trim());
                }
            }
        } catch (Throwable t) {
            logger.log(Level.TRACE, t.toString());
        }
        return list;
    }
}
