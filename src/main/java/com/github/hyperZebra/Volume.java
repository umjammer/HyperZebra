/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.github.hyperZebra;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.Serial;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;


/**
 * The master volume of movies and sounds (0.0 - 1.0), kept in the user preferences.
 */
final class Volume {

    private Volume() {
    }

    private static final Preferences prefs = Preferences.userNodeForPackage(Volume.class);

    private static final String KEY = "volume";

    private static volatile float master = Math.max(0f, Math.min(1f, prefs.getFloat(KEY, 1f)));

    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    /** @return the master volume, 0.0 - 1.0 */
    static float get() {
        return master;
    }

    static void set(float v) {
        float n = Math.max(0f, Math.min(1f, v));
        if (n == master) return;
        master = n;
        prefs.putFloat(KEY, n);
        listeners.forEach(Runnable::run);
    }

    /** the listener is called when the master volume changed */
    static void addListener(Runnable r) {
        listeners.add(r);
    }

    /** scales 16 bit signed little endian PCM by the master volume */
    static byte[] scalePcm16le(byte[] b, int off, int len) {
        float v = master;
        byte[] out = new byte[len];
        if (v >= 0.999f) {
            System.arraycopy(b, off, out, 0, len);
            return out;
        }
        for (int i = 0; i + 1 < len; i += 2) {
            int s = (short) ((b[off + i] & 0xff) | (b[off + i + 1] << 8));
            s = Math.round(s * v);
            out[i] = (byte) s;
            out[i + 1] = (byte) (s >> 8);
        }
        return out;
    }

    private static javax.swing.JDialog panel;

    /** shows a small floating panel with the volume slider under the menu bar of the card window */
    static void showPanel() {
        if (panel == null) {
            panel = new javax.swing.JDialog(PCARD.pc, ResourceBundle.getBundle("messages").getString("main.Volume"), false);
            panel.setType(java.awt.Window.Type.UTILITY);
            panel.getContentPane().add(new SliderItem());
            panel.pack();
            panel.setResizable(false);
        }
        java.awt.Rectangle r = PCARD.pc.getBounds();
        panel.setLocation(r.x + r.width - panel.getWidth() - 8, r.y + PCARD.pc.getInsets().top + 8);
        panel.setVisible(true);
        panel.toFront();
    }

    /** a slider for a menu or a panel */
    static class SliderItem extends JPanel {

        @Serial
        private static final long serialVersionUID = 1L;

        private final JSlider slider = new JSlider(0, 100);
        private final JLabel value = new JLabel();

        SliderItem() {
            super(new BorderLayout(6, 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(4, 20, 4, 12));
            add(new JLabel(ResourceBundle.getBundle("messages").getString("main.Volume")), BorderLayout.WEST);
            slider.setOpaque(false);
            slider.setFocusable(false);
            slider.setPreferredSize(new Dimension(140, slider.getPreferredSize().height));
            add(slider, BorderLayout.CENTER);
            value.setPreferredSize(new Dimension(40, value.getPreferredSize().height));
            add(value, BorderLayout.EAST);
            sync();
            slider.addChangeListener(e -> {
                set(slider.getValue() / 100f);
                value.setText(slider.getValue() + "%");
            });
        }

        @Override
        public void addNotify() {
            super.addNotify();
            sync(); // the menu popup was shown
        }

        private void sync() {
            int v = Math.round(get() * 100);
            if (slider.getValue() != v) slider.setValue(v);
            value.setText(v + "%");
        }
    }
}
