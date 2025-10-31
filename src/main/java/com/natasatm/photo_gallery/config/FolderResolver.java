package com.natasatm.photo_gallery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author Natasa Todorov Markovic
 */
@Component
public class FolderResolver {

    @Value("${gallery.folder:}")
    private String propFolder;

    @Value("${gallery.choose-on-start:false}")
    private boolean chooseOnStart;

    @Value("${gallery.remember-choice:true}")
    private boolean rememberChoice;

    @Value("${gallery.remember-file:.gallery-folder}")
    private String rememberFileName;

    private Path resolved;  // cache

    public synchronized Path getRoot() {
        if (resolved != null) return resolved;

        // 1) ENV var ima najviši prioritet
        String env = System.getenv("GALLERY_FOLDER");
        if (notBlank(env)) {
            resolved = normalizeAndEnsure(env);
            return resolved;
        }

        // 2) Snimljeni izbor iz fajla (ako postoji)
        Path remembered = readRemembered();
        if (remembered != null && Files.isDirectory(remembered)) {
            resolved = remembered;
            return resolved;
        }

        // 3) Spring property (application*.properties)
        if (notBlank(propFolder)) {
            resolved = normalizeAndEnsure(propFolder);
            maybeRemember(resolved);
            return resolved;
        }

        // 4) Ako je tražen izbor (ili ništa nije zadato) → prikaži dijalog
        if (chooseOnStart) {
            Path chosen = showFolderChooser();
            if (chosen != null) {
                resolved = chosen;
                maybeRemember(resolved);
                return resolved;
            }
        }

        // 5) Fallback: ./gallery
        resolved = normalizeAndEnsure("./gallery");
        maybeRemember(resolved);
        return resolved;
    }

    private Path showFolderChooser() {
        // Swing dijalog (zahteva spring.main.headless=false)
        try {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Izaberite folder sa slikama");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                return fc.getSelectedFile().toPath().toAbsolutePath().normalize();
            }
        } catch (Exception ignore) {}
        return null;
    }

    private Path readRemembered() {
        Path f = rememberFilePath();
        if (Files.isRegularFile(f)) {
            try {
                String s = Files.readString(f).trim();
                if (notBlank(s)) return Paths.get(s).toAbsolutePath().normalize();
            } catch (IOException ignore) {}
        }
        return null;
    }

    private void maybeRemember(Path p) {
        if (!rememberChoice || p == null) return;
        try {
            Files.writeString(rememberFilePath(), p.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ignore) {}
    }

    private Path rememberFilePath() {
        // pokušaj pored exe/jar (radni dir); ako ne može, idi u user.home
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
}
