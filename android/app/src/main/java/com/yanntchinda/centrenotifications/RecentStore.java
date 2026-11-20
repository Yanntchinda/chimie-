package com.yanntchinda.centrenotifications;

import android.app.PendingIntent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Derniers messages reçus, gardés en mémoire pour l'écran verrouillé.
 * Le plus récent est en tête. La liste est partagée entre le service d'écoute
 * (qui l'alimente) et l'écran verrouillé (qui l'affiche).
 */
final class RecentStore {

    static final class Item {
        final String key;
        final String pkg;
        final String appLabel;
        final String sender;
        final String message;
        final long when;
        final int dotRes;
        final PendingIntent open;

        Item(String key, String pkg, String appLabel, String sender, String message,
             long when, int dotRes, PendingIntent open) {
            this.key = key;
            this.pkg = pkg;
            this.appLabel = appLabel;
            this.sender = sender;
            this.message = message;
            this.when = when;
            this.dotRes = dotRes;
            this.open = open;
        }
    }

    private static final int LIMIT = 12;
    private static final List<Item> ITEMS = new ArrayList<>();

    private RecentStore() {
    }

    static synchronized void put(Item item) {
        Iterator<Item> it = ITEMS.iterator();
        while (it.hasNext()) {
            if (it.next().key.equals(item.key)) it.remove();
        }
        ITEMS.add(0, item);
        while (ITEMS.size() > LIMIT) ITEMS.remove(ITEMS.size() - 1);
    }

    static synchronized void remove(String key) {
        if (key == null) return;
        Iterator<Item> it = ITEMS.iterator();
        while (it.hasNext()) {
            if (it.next().key.equals(key)) it.remove();
        }
    }

    static synchronized void clear() {
        ITEMS.clear();
    }

    static synchronized List<Item> snapshot() {
        return new ArrayList<>(ITEMS);
    }
}
