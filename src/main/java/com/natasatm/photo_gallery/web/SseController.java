package com.natasatm.photo_gallery.web;

import com.natasatm.photo_gallery.service.GalleryService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/sse")
public class SseController {

    private final GalleryService gallery;
    private final CopyOnWriteArrayList<SseEmitter> clients = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public SseController(GalleryService gallery) {
        this.gallery = gallery;
        // keepalive ping
        scheduler.scheduleAtFixedRate(this::pingAll, 30, 30, TimeUnit.SECONDS);
    }

    @GetMapping(path="/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter em = new SseEmitter(0L); // no-timeout
        clients.add(em);

        // pošalji trenutnu verziju odmah
        send(em, "version", String.valueOf(gallery.getVersion()));

        em.onCompletion(() -> clients.remove(em));
        em.onTimeout(()   -> clients.remove(em));
        em.onError(e -> clients.remove(em));

        return em;
    }

    // Poziva Watcher kad otkrije promenu
    public void notifyVersion() {
        String v = String.valueOf(gallery.getVersion());
        for (SseEmitter em : clients) send(em, "version", v);
    }

    private void pingAll() {
        for (SseEmitter em : clients) send(em, "ping", "keepalive");
    }

    private void send(SseEmitter em, String event, String data) {
        try { em.send(SseEmitter.event().name(event).data(data)); }
        catch (IOException e) {
            clients.remove(em);
            try { em.complete(); } catch (Exception ignored) {}
        }
    }
}
