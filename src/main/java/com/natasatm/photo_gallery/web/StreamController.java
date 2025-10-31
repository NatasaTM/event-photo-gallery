package com.natasatm.photo_gallery.web;

import com.natasatm.photo_gallery.service.GalleryService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Natasa Todorov Markovic
 */
@Controller
@RequestMapping("/")
public class StreamController {

    private final GalleryService gallery;
    private final List<SseEmitter> clients = new CopyOnWriteArrayList<>();

    public StreamController(GalleryService gallery) {
        this.gallery = gallery;
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() throws IOException {
        // -1L = bez timeout-a; mozes npr. 0L ili 60_000L po zelji
        var emitter = new SseEmitter(-1L);
        clients.add(emitter);

        emitter.onCompletion(() -> clients.remove(emitter));
        emitter.onTimeout(() -> clients.remove(emitter));
        emitter.onError(ex -> clients.remove(emitter));

        // pošalji trenutnu verziju odmah
        sendVersion(emitter, gallery.getVersion());
        return emitter;
    }

    /** Pozovi kad bump-uješ verziju. Ovde bi pozivao iz watchera. */
    public void broadcastVersion(long v) {
        for (var em : clients) sendVersion(em, v);
    }

    private void sendVersion(SseEmitter em, long v) {
        try {
            SseEmitter.SseEventBuilder ev = SseEmitter.event()
                    .name("version")
                    .data(v)
                    .reconnectTime(Duration.ofSeconds(2).toMillis());
            em.send(ev);
        } catch (Exception e) {
            clients.remove(em);
            try { em.complete(); } catch (Exception ignored) {}
        }
    }
}
