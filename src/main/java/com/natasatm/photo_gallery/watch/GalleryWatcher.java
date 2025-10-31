package com.natasatm.photo_gallery.watch;

import com.natasatm.photo_gallery.service.GalleryService;
import com.natasatm.photo_gallery.web.SseController;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.nio.file.*;
import java.util.concurrent.*;

import static java.nio.file.StandardWatchEventKinds.*;

@Component
public class GalleryWatcher {

    private final GalleryService gallery;
    private final SseController sse;

    private final ScheduledExecutorService debounceExec = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pending;

    public GalleryWatcher(GalleryService gallery, SseController sse) {
        this.gallery = gallery;
        this.sse = sse;
    }

    @PostConstruct
    public void start() {
        Thread t = new Thread(this::run, "gallery-watch");
        t.setDaemon(true);
        t.start();
    }

    private void run() {
        Path root = gallery.getRoot();
        try (WatchService ws = root.getFileSystem().newWatchService()) {
            // Pokušaj FILE_TREE (Windows) – rekurzivno
            try {
                WatchEvent.Modifier FILE_TREE =
                        (WatchEvent.Modifier) Class.forName("com.sun.nio.file.ExtendedWatchEventModifier")
                                .getField("FILE_TREE").get(null);
                root.register(ws, new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}, FILE_TREE);
            } catch (Throwable t) {
                // Fallback: samo root
                root.register(ws, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            }

            // Dodatni polling fallback (npr. mrežni disk)
            ScheduledExecutorService poll = Executors.newSingleThreadScheduledExecutor();
            poll.scheduleAtFixedRate(this::scheduleRebuild, 30, 30, TimeUnit.SECONDS);

            while (true) {
                WatchKey key = ws.take();
                for (WatchEvent<?> ignored : key.pollEvents()) {
                    scheduleRebuild();
                }
                key.reset();
            }
        } catch (Exception e) {
            // Ako watcher padne – periodični rebuild
            ScheduledExecutorService safety = Executors.newSingleThreadScheduledExecutor();
            safety.scheduleAtFixedRate(this::doRebuildNow, 5, 5, TimeUnit.SECONDS);
        }
    }

    private void scheduleRebuild() {
        if (pending != null && !pending.isDone()) pending.cancel(false);
        pending = debounceExec.schedule(this::doRebuildNow, 1500, TimeUnit.MILLISECONDS);
    }

    private void doRebuildNow() {
        gallery.rebuildAndBump();
        sse.notifyVersion();
    }
}
