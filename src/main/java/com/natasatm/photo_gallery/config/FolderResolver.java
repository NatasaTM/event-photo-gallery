package com.natasatm.photo_gallery.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Configuration
public class FolderResolver {

    private static final Logger log = LoggerFactory.getLogger(FolderResolver.class);

    @Value("${gallery.folder:}")
    private String propFolder;

    @Value("${gallery.choose-on-start:false}")
    private boolean chooseOnStart;

    @Value("${gallery.remember-choice:true}")
    private boolean rememberChoice;

    @Value("${gallery.remember-file:.gallery-folder}")
    private String rememberFileName;

    @Value("${gallery.force-choose:false}")
    private boolean forceChoose;

    private Path resolved;

    public synchronized Path getRoot() {
        if (resolved != null) {
            log.debug("Vraćam keširanu putanju: {}", resolved);
            return resolved;
        }

        // 1) ENV var ima najviši prioritet
        String env = System.getenv("GALLERY_FOLDER");
        if (notBlank(env)) {
            log.info("Koristim ENV varijablu GALLERY_FOLDER: {}", env);
            resolved = normalizeAndEnsure(env);
            return resolved;
        }

        // 2) Zapamćeni izbor (samo ako nije forceChoose)
        if (!forceChoose) {
            Path remembered = readRemembered();
            if (remembered != null && Files.isDirectory(remembered)) {
                log.info("Koristim zapamćeni folder iz {}: {}", rememberFilePath(), remembered);
                resolved = remembered;
                return resolved;
            } else if (remembered != null) {
                log.warn("Zapamćeni folder ne postoji ili nije direktorijum: {}", remembered);
            }
        } else {
            log.info("force-choose=true, ignoriše zapamćeni folder");
        }

        // 3) Spring property
        if (notBlank(propFolder)) {
            log.info("Koristim gallery.folder property: {}", propFolder);
            resolved = normalizeAndEnsure(propFolder);
            maybeRemember(resolved);
            return resolved;
        }

        // 4) Dijalog
        if (chooseOnStart) {
            log.info("Prikazujem dijalog za izbor foldera (choose-on-start=true)...");
            Path chosen = showFolderChooser();
            if (chosen != null) {
                log.info("Korisnik je izabrao: {}", chosen);
                resolved = chosen;
                maybeRemember(resolved);
                return resolved;
            } else {
                log.warn("Korisnik je otkazao izbor foldera");
            }
        } else {
            log.debug("choose-on-start=false, preskačem dijalog");
        }

        // 5) Fallback - NE PAMTIMO GA!
        log.info("Koristim fallback folder: ./gallery");
        resolved = normalizeAndEnsure("./gallery");
        // Намерно НЕ зовемо maybeRemember() овде!
        return resolved;
    }

    private Path showFolderChooser() {
        try {
            log.debug("Kreiram JFileChooser...");
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Izaberite folder sa slikama");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);

            int result = fc.showOpenDialog(null);
            log.debug("JFileChooser rezultat: {}", result);

            if (result == JFileChooser.APPROVE_OPTION) {
                return fc.getSelectedFile().toPath().toAbsolutePath().normalize();
            }
        } catch (Exception e) {
            log.error("Greška pri prikazu dijaloga", e);
        }
        return null;
    }

    private Path readRemembered() {
        Path f = rememberFilePath();
        log.debug("Proveravam zapamćeni fajl: {}", f);
        if (Files.isRegularFile(f)) {
            try {
                String s = Files.readString(f).trim();
                log.debug("Sadržaj fajla: '{}'", s);
                if (notBlank(s)) return Paths.get(s).toAbsolutePath().normalize();
            } catch (IOException e) {
                log.warn("Greška pri čitanju fajla: {}", f, e);
            }
        } else {
            log.debug("Fajl ne postoji: {}", f);
        }
        return null;
    }

    private void maybeRemember(Path p) {
        if (!rememberChoice || p == null) {
            log.debug("Ne pamtim izbor (rememberChoice={}, path={})", rememberChoice, p);
            return;
        }
        try {
            Path file = rememberFilePath();
            Files.writeString(file, p.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Zapamćen folder u {}: {}", file, p);
        } catch (IOException e) {
            log.warn("Ne mogu da zapamtim izbor", e);
        }
    }

    private Path rememberFilePath() {
        Path wd = Paths.get("").toAbsolutePath();
        Path inWd = wd.resolve(rememberFileName);
        if (canWrite(wd)) return inWd;
        return Paths.get(System.getProperty("user.home")).resolve(rememberFileName);
    }

    private static boolean canWrite(Path dir) {
        try {
            Path tmp = dir.resolve(".tmp_write_test");
            Files.writeString(tmp, "x", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.deleteIfExists(tmp);
            return true;
        } catch (IOException e) { return false; }
    }

    private static Path normalizeAndEnsure(String s) {
        Path p = Paths.get(s).toAbsolutePath().normalize();
        try { Files.createDirectories(p); } catch (IOException ignore) {}
        return p;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    // Dodaj Bean metod da Spring može da injektuje Path galleryRoot
    @Bean
    public Path galleryRoot() {
        return getRoot();
    }
}