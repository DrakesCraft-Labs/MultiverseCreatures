package com.Chagui68.stand;

import com.Chagui68.stand.StandRig.Frame;
import com.Chagui68.stand.StandRig.Part;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Writes every Stand, in every pose, for the model viewer in {@code tools/stand-viewer}: the same
 * transformations the plugin sends to the game, so the viewer shows what a player would see.
 *
 * <p>Run it with {@code mvn test -Dtest=StandViewerExportTest}; it writes
 * {@code target/stand-viewer/models.json}.</p>
 */
public final class StandViewerExport {

    private static final Pattern SKIN_URL = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");

    private StandViewerExport() {
    }

    public static Path write(Path out) throws IOException {
        StringBuilder json = new StringBuilder("{\"stands\":[");
        boolean first = true;
        for (StandType type : StandType.values()) {
            if (!type.hasBody()) {
                continue;
            }
            if (!first) {
                json.append(',');
            }
            first = false;
            HeadModel heads = builtIn(type);
            json.append("{\"key\":\"").append(type.key()).append("\",\"name\":\"").append(type.displayName())
                    .append("\",\"kind\":\"").append(heads == null ? "blocks" : "heads").append("\",\"poses\":{");
            Map<String, Map<Part, Quaternionf>> poses = poses(type);
            boolean firstPose = true;
            for (Map.Entry<String, Map<Part, Quaternionf>> pose : poses.entrySet()) {
                if (!firstPose) {
                    json.append(',');
                }
                firstPose = false;
                json.append('"').append(pose.getKey()).append("\":[");
                json.append(heads == null ? blocks(type, pose.getValue()) : heads(heads, pose.getValue()));
                json.append(']');
            }
            json.append("}}");
        }
        // DIO himself, in the poses his boss fight uses.
        HeadModel dio = builtIn("dio-brando");
        if (dio != null) {
            json.append(",{\"key\":\"dio-brando\",\"name\":\"DIO (boss)\",\"kind\":\"heads\",\"poses\":{");
            Map<String, com.Chagui68.entities.boss.fx.Pose> poses = new LinkedHashMap<>();
            poses.put("idle", com.Chagui68.entities.boss.DioMoves.DIO_IDLE);
            poses.put("arms folded", com.Chagui68.entities.boss.DioMoves.DIO_ARMS_FOLDED);
            poses.put("knives thrown", com.Chagui68.entities.boss.DioMoves.DIO_KNIVES_THROWN);
            poses.put("leap", com.Chagui68.entities.boss.DioMoves.DIO_LEAP);
            poses.put("on the road roller", com.Chagui68.entities.boss.DioMoves.DIO_ON_ROLLER);
            poses.put("walking", com.Chagui68.entities.boss.DioMoves.walk(1.2));
            boolean firstPose = true;
            for (Map.Entry<String, com.Chagui68.entities.boss.fx.Pose> pose : poses.entrySet()) {
                com.Chagui68.entities.boss.fx.Pose p = pose.getValue();
                if (!firstPose) {
                    json.append(',');
                }
                firstPose = false;
                json.append('"').append(pose.getKey()).append("\":[")
                        .append(heads(dio, StandRig.fromArmorStand(p.head(), p.body(), p.leftArm(), p.rightArm(),
                                p.leftLeg(), p.rightLeg())))
                        .append(']');
            }
            json.append("}}");
        }
        json.append("]}");
        Files.createDirectories(out.getParent());
        Files.writeString(out, json, StandardCharsets.UTF_8);
        return out;
    }

    private static Map<String, Map<Part, Quaternionf>> poses(StandType type) {
        Map<String, Map<Part, Quaternionf>> poses = new LinkedHashMap<>();
        poses.put("idle", StandRig.idle(type, 0, 0f));
        poses.put("look down", StandRig.idle(type, 40, 45f));
        poses.put("barrage right", StandRig.barrage(0));
        poses.put("barrage left", StandRig.barrage(1));
        if (type == StandType.THE_WORLD) {
            double[] zero = {0, 0, 0};
            poses.put("boss: arms folded", StandRig.fromArmorStand(new double[]{-5, 0, 0}, zero,
                    new double[]{-80, -48, 0}, new double[]{-80, 48, 0}, new double[]{8, 0, 0}, new double[]{-12, 0, 0}));
            poses.put("boss: time stop", StandRig.fromArmorStand(new double[]{-20, 0, 0}, new double[]{-8, 0, 0},
                    new double[]{-150, 0, -55}, new double[]{-150, 0, 55}, zero, zero));
        }
        return poses;
    }

    private static String blocks(StandType type, Map<Part, Quaternionf> pose) {
        Map<Part, Frame> frames = StandRig.solve(pose, new Vector3f());
        StringBuilder out = new StringBuilder();
        for (StandDesign.Piece piece : StandDesign.of(type)) {
            Transformation t = StandRig.place(piece.center(), piece.size(), piece.spin(), frames.get(piece.part()), 0.9f);
            if (out.length() > 0) {
                out.append(',');
            }
            out.append("{\"m\":\"").append(piece.material().name()).append("\",\"glow\":").append(piece.bright())
                    .append(',').append(transform(t)).append('}');
        }
        return out.toString();
    }

    private static String heads(HeadModel model, Map<Part, Quaternionf> pose) {
        Map<Part, Frame> frames = StandRig.solve(pose, new Vector3f(), model::pivot);
        float scale = (float) (1.8 / model.height());
        StringBuilder out = new StringBuilder();
        for (HeadModel.Piece piece : model.pieces()) {
            Transformation t = HeadModel.place(piece, frames.get(piece.part()), scale);
            if (out.length() > 0) {
                out.append(',');
            }
            out.append("{\"skin\":\"").append(skinUrl(piece.texture())).append("\",\"part\":\"")
                    .append(piece.part().name()).append("\",").append(transform(t)).append('}');
        }
        return out.toString();
    }

    private static String transform(Transformation t) {
        Vector3f p = t.getTranslation();
        Quaternionf q = t.getLeftRotation();
        Vector3f s = t.getScale();
        return String.format(Locale.ROOT, "\"t\":[%.5f,%.5f,%.5f],\"q\":[%.6f,%.6f,%.6f,%.6f],\"s\":[%.5f,%.5f,%.5f]",
                p.x, p.y, p.z, q.x, q.y, q.z, q.w, s.x, s.y, s.z);
    }

    private static String skinUrl(String texture) {
        Matcher url = SKIN_URL.matcher(new String(Base64.getDecoder().decode(texture), StandardCharsets.UTF_8));
        return url.find() ? url.group(1).replace("http://", "https://") : "";
    }

    private static HeadModel builtIn(StandType type) throws IOException {
        return builtIn(type.key());
    }

    private static HeadModel builtIn(String key) throws IOException {
        try (InputStream in = StandViewerExport.class.getResourceAsStream("/stands/" + key + ".txt")) {
            return in == null ? null : HeadModel.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
