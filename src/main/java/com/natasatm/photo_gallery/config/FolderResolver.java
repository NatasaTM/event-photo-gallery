// com.natasatm.photo_gallery.config.FolderResolver.java
package com.natasatm.photo_gallery.config;

import com.natasatm.photo_gallery.config.GalleryRoots.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

@Configuration
public class FolderResolver {
    private static final Logger log = LoggerFactory.getLogger(FolderResolver.class);

    // === Properties / flags ===
    @Value("${gallery.mode:auto}")           // auto|single|multi
    private String propMode;

    // single-root hint
    @Value("${gallery.single.event-root:}")
    private String propSingleEventRoot;

    // multi-root hints
    @Value("${gallery.multi.preview-root:}")
    private String propMultiPreviewRoot;
    @Value("${gallery.multi.original-root:}")
    private String propMultiOriginalRoot;
    @Value("${gallery.multi.orders-root:}")
    private String propMultiOrdersRoot;

    // UX flags
    @Value("${gallery.choose-on-start:true}")
    private boolean chooseOnStart;
    @Value("${gallery.remember-choice:true}")
    private boolean rememberChoice;
    @Value("${gallery.remember-file:.gallery-config}")
    private String rememberFileName;
    @Value("${gallery.force-choose:false}")
    private boolean forceChoose;

    private volatile GalleryRoots resolved; // cache

    // === Public API ===
    public synchronized GalleryRoots getRoots() {
        if (resolved != null) return resolved;

        // 1) ENV (najviši prioritet)
        GalleryRoots env = fromEnv();
        if (env != null) return resolved = env;

        // 2) Remembered (osim ako je forceChoose)
        if (!forceChoose) {
            GalleryRoots remembered = readRemembered();
            if (remembered != null) {
                log.info("Koristim zapamćenu konfiguraciju: {}", remembered);
                return resolved = remembered;
            }
        } else {
            log.info("force-choose=true, ignorišem zapamćenu konfiguraciju");
        }

        // 3) Spring properties hint
        GalleryRoots fromProps = fromProperties();
        if (fromProps != null) {
            maybeRemember(fromProps);
            return resolved = fromProps;
        }

        // 4) Interaktivni izbor
        if (chooseOnStart) {
            GalleryRoots chosen = chooseInteractively();
            if (chosen != null) {
                maybeRemember(chosen);
                return resolved = chosen;
            }
        }

        // 5) Fallback (single root u ./event)
        Path event = normalizeAndEnsure("./event");
        Path preview = ensureDir(event.resolve("preview"));
        Path originals = ensureDir(event.resolve("originals"));
        Path orders = ensureDir(event.resolve("orders"));
        GalleryRoots fb = new GalleryRoots(preview, originals, orders, Mode.SINGLE);
        log.info("Koristim fallback: {}", fb);
        return resolved = fb;
    }

    // Zadrži API kompatibilnost (WebConfig i dalje može da traži samo preview)
    public Path getRoot() { return getRoots().previewRoot(); }

    // === Beans za injektovanje gde treba ===
    @Bean public Path galleryRoot()   { return getRoots().previewRoot(); }
    @Bean public Path originalsRoot() { return getRoots().originalRoot(); }
    @Bean public Path ordersRoot()    { return getRoots().ordersRoot();  }

    // === ENV ===
    private GalleryRoots fromEnv() {
        String mode = System.getenv("GALLERY_MODE"); // SINGLE|MULTI (case-insensitive)
        if (mode == null && System.getenv("GALLERY_EVENT_ROOT")==null
                && System.getenv("GALLERY_PREVIEW_ROOT")==null
                && System.getenv("GALLERY_ORIGINAL_ROOT")==null
                && System.getenv("GALLERY_ORDERS_ROOT")==null) return null;

        Mode m = parseMode(mode, Mode.SINGLE);
        if (m == Mode.SINGLE) {
            String er = env("GALLERY_EVENT_ROOT");
            if (blank(er)) return null;
            Path event = normalizeAndEnsure(er);
            Path preview = ensureDir(event.resolve("preview"));
            Path originals = ensureDir(event.resolve("originals"));
            Path orders = ensureWritableDir(event.resolve("orders"));
            GalleryRoots roots = new GalleryRoots(preview, originals, orders, Mode.SINGLE);
            validate(roots);
            return roots;
        } else {
            String pr = env("GALLERY_PREVIEW_ROOT");
            String or = env("GALLERY_ORIGINAL_ROOT");
            String od = env("GALLERY_ORDERS_ROOT");
            if (blank(pr) || blank(or) || blank(od)) return null;
            GalleryRoots roots = new GalleryRoots(
                    normalizeAndEnsure(pr),
                    normalizeAndEnsure(or),
                    ensureWritableDir(Paths.get(od)),
                    Mode.MULTI
            );
            validate(roots);
            return roots;
        }
    }

    // === Properties ===
    private GalleryRoots fromProperties() {
        Mode desired = switch (propMode.toLowerCase()) {
            case "single" -> Mode.SINGLE;
            case "multi"  -> Mode.MULTI;
            default       -> null; // auto
        };

        if (desired == Mode.SINGLE) {
            if (blank(propSingleEventRoot)) return null;
            Path event = normalizeAndEnsure(propSingleEventRoot);
            GalleryRoots roots = new GalleryRoots(
                    ensureDir(event.resolve("preview")),
                    ensureDir(event.resolve("originals")),
                    ensureWritableDir(event.resolve("orders")),
                    Mode.SINGLE
            );
            validate(roots);
            return roots;
        }
        if (desired == Mode.MULTI) {
            if (blank(propMultiPreviewRoot) || blank(propMultiOriginalRoot) || blank(propMultiOrdersRoot)) return null;
            GalleryRoots roots = new GalleryRoots(
                    normalizeAndEnsure(propMultiPreviewRoot),
                    normalizeAndEnsure(propMultiOriginalRoot),
                    ensureWritableDir(Paths.get(propMultiOrdersRoot)),
                    Mode.MULTI
            );
            validate(roots);
            return roots;
        }

        // auto → pokušaj ništa
        return null;
    }

    // === Remember file ===
    private GalleryRoots readRemembered() {
        Path f = rememberFilePath();
        if (!Files.isRegularFile(f)) return null;
        try {
            Properties p = new Properties();
            try (var r = Files.newBufferedReader(f)) { p.load(r); }
            Mode m = parseMode(p.getProperty("mode"), Mode.SINGLE);

            if (m == Mode.SINGLE) {
                String root = p.getProperty("eventRoot");
                if (blank(root)) return null;
                Path event = normalizeAndEnsure(root);
                Path preview   = ensureDir(event.resolve("preview"));
                Path originals = ensureDir(event.resolve("originals"));
                Path orders    = ensureWritableDir(event.resolve("orders"));

                // ✅ hard-validacija: sve mora postojati + orders writable
                if (!Files.isDirectory(preview) || !Files.isDirectory(originals) || !canWrite(orders)) {
                    log.warn("Remembered SINGLE config ne važi: {}", event);
                    return null;
                }
                return new GalleryRoots(preview, originals, orders, Mode.SINGLE);
            } else {
                String pr = p.getProperty("previewRoot");
                String or = p.getProperty("originalRoot");
                String od = p.getProperty("ordersRoot");
                if (blank(pr) || blank(or) || blank(od)) return null;

                Path preview   = normalizeAndEnsure(pr);
                Path originals = normalizeAndEnsure(or);
                Path orders    = ensureWritableDir(Paths.get(od));

                if (!Files.isDirectory(preview) || !Files.isDirectory(originals) || !canWrite(orders)) {
                    log.warn("Remembered MULTI config ne važi");
                    return null;
                }
                return new GalleryRoots(preview, originals, orders, Mode.MULTI);
            }
        } catch (Exception e) {
            log.warn("Greška pri čitanju {}: {}", f, e.toString());
            return null; // forsira novi izbor
        }
    }


    private void maybeRemember(GalleryRoots r) {
        if (!rememberChoice || r == null) return;
        Path f = rememberFilePath();
        try {
            Properties p = new Properties();
            p.setProperty("mode", r.mode().name());
            if (r.mode() == Mode.SINGLE) {
                // eventRoot = parent(previewRoot)
                Path eventRoot = r.previewRoot().getParent();
                if (eventRoot == null) eventRoot = r.previewRoot();
                p.setProperty("eventRoot", eventRoot.toString());
            } else {
                p.setProperty("previewRoot",  r.previewRoot().toString());
                p.setProperty("originalRoot", r.originalRoot().toString());
                p.setProperty("ordersRoot",   r.ordersRoot().toString());
            }
            try (var w = Files.newBufferedWriter(f)) {
                p.store(w, null); // ispravno escape-uje backslash za Windows putanje
            }
            log.info("Zapamćena galerijska konfiguracija u {}", f);
        } catch (Exception e) {
            log.warn("Ne mogu da zapamtim konfiguraciju: {}", e.toString());
        }
    }

    public void clearRemembered() {
        try {
            Files.deleteIfExists(rememberFilePath());
            log.info("Obrisan zapamćeni .gallery-config");
        } catch (IOException e) {
            log.warn("Ne mogu da obrišem {}: {}", rememberFilePath(), e.toString());
        }
    }



    private String toPropertiesString(Properties p) throws IOException {
        var sb = new StringBuilder();
        for (Map.Entry<Object,Object> e : p.entrySet().stream().collect(HashMap::new, (m,x)->m.put(x.getKey(),x.getValue()), Map::putAll).entrySet()) {
            sb.append(e.getKey()).append('=').append(e.getValue()).append('\n');
        }
        return sb.toString();
    }

    private Path rememberFilePath() {
        Path wd = Paths.get("").toAbsolutePath();
        Path inWd = wd.resolve(rememberFileName);
        return canWrite(wd) ? inWd : Paths.get(System.getProperty("user.home")).resolve(rememberFileName);
    }

    // === Dialog (Single ili Multi) ===
    private GalleryRoots chooseInteractively() {
        Mode m = askMode();
        if (m == null) return null;

        if (m == Mode.SINGLE) {
            Path event = chooseDir("Izaberite GLAVNI folder za event");
            if (event == null) return null;
            Path preview = ensureDir(event.resolve("preview"));
            Path originals = ensureDir(event.resolve("originals"));
            Path orders = ensureWritableDir(event.resolve("orders"));
            GalleryRoots r = new GalleryRoots(preview, originals, orders, Mode.SINGLE);
            validate(r);
            return r;
        } else {
            Path preview = chooseDir("Izaberite PREVIEW folder");
            if (preview == null) return null;
            Path originals = chooseDir("Izaberite ORIGINALS folder");
            if (originals == null) return null;
            Path orders = chooseDir("Izaberite ORDERS folder (mora biti upisiv)");
            if (orders == null) return null;
            orders = ensureWritableDir(orders);
            GalleryRoots r = new GalleryRoots(preview.toAbsolutePath().normalize(),
                    originals.toAbsolutePath().normalize(),
                    orders.toAbsolutePath().normalize(),
                    Mode.MULTI);
            validate(r);
            return r;
        }
    }

    private Mode askMode() {
        Object[] options = {"Jedan glavni folder (preporučeno)", "Tri odvojena foldera (napredno)", "Otkaži"};
        int res = JOptionPane.showOptionDialog(
                null,
                "Kako želiš da podesiš skladište za ovaj event?",
                "Podešavanje galerije",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]
        );
        if (res == 0) return Mode.SINGLE;
        if (res == 1) return Mode.MULTI;
        return null;
    }

    private Path chooseDir(String title) {
        try {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle(title);
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            int result = fc.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                return fc.getSelectedFile().toPath().toAbsolutePath().normalize();
            }
        } catch (Exception e) {
            log.error("Greška u chooseru: {}", e.toString());
        }
        return null;
    }

    // === Validacija ===
    private void validate(GalleryRoots r) {
        // preview & originals: moraju postojati i biti čitljivi
        if (!Files.isDirectory(r.previewRoot()))
            throw new IllegalStateException("Preview root ne postoji: " + r.previewRoot());
        if (!Files.isDirectory(r.originalRoot()))
            throw new IllegalStateException("Originals root ne postoji: " + r.originalRoot());

        // orders: mora biti upisiv
        if (!canWrite(r.ordersRoot()))
            throw new IllegalStateException("Orders root nije upisiv: " + r.ordersRoot());
    }

    // === Helpers ===
    private static String env(String key) { return System.getenv(key); }
    private static boolean blank(String s) { return s == null || s.trim().isEmpty(); }
    private static Mode parseMode(String s, Mode deflt) {
        if (s == null) return deflt;
        return switch (s.trim().toLowerCase()) {
            case "single" -> Mode.SINGLE;
            case "multi"  -> Mode.MULTI;
            default       -> deflt;
        };
    }
    private static Path normalizeAndEnsure(String s) {
        Path p = Paths.get(s).toAbsolutePath().normalize();
        try { Files.createDirectories(p); } catch (IOException ignore) {}
        return p;
    }
    private static Path ensureDir(Path p) {
        try { Files.createDirectories(p); } catch (IOException ignore) {}
        return p.toAbsolutePath().normalize();
    }
    private static Path ensureWritableDir(Path p) {
        p = ensureDir(p);
        if (!canWrite(p)) throw new IllegalStateException("Nije moguće pisati u " + p);
        return p;
    }
    private static boolean canWrite(Path dir) {
        try {
            Path tmp = dir.resolve(".tmp_write_test");
            Files.writeString(tmp, "x", CREATE, TRUNCATE_EXISTING);
            Files.deleteIfExists(tmp);
            return true;
        } catch (IOException e) { return false; }
    }

    @Bean
    public GalleryRoots galleryRoots() {
        // Napravi i keširanu instancu preko postojećeg mehanizma
        return getRoots();
    }




}
