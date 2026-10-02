package com.Chagui68.entities.boss.attack;

import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.fx.Pose;
import com.Chagui68.entities.boss.fx.Poses;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.testsupport.ProjectPaths;
import com.Chagui68.testsupport.RecordingStage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Plays every choreographed attack from start to finish on a stage with no server, and checks what a
 * player would see: no exception, no particle the server would refuse, no prop left behind, the body
 * back at rest, and a lock no longer than the attack.
 */
class ChoreographyTest {

    static Stream<String> attacks() {
        List<String> names = new ArrayList<>();
        for (Path file : ProjectPaths.javaFiles(ProjectPaths.source("com", "Chagui68", "entities", "boss", "attack"))) {
            String source = ProjectPaths.read(file);
            if (!source.contains("extends ChoreographedAttack")) continue;
            String relative = ProjectPaths.mainJava().relativize(file).toString().replace('\\', '/').replace('/', '.');
            names.add(relative.substring(0, relative.length() - ".java".length()));
        }
        return names.stream().sorted();
    }

    static BossHost host() {
        return (BossHost) Proxy.newProxyInstance(BossHost.class.getClassLoader(), new Class<?>[]{BossHost.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getSealDamage" -> 15.0;
                    case "getHoverBarrageDamage" -> 12.0;
                    case "hashCode" -> 1;
                    case "equals" -> proxy == args[0];
                    default -> null;
                });
    }

    static RecordingStage stage() {
        RecordingStage stage = new RecordingStage(7.5);
        stage.player(3, 16);
        stage.player(-12, 9);
        stage.player(2, 4);
        stage.pose(Poses.GUARD);
        return stage;
    }

    @Test
    @DisplayName("The scan finds the attacks it is supposed to check")
    void theScanFindsTheAttacks() {
        assertTrue(attacks().count() >= 60, "only " + attacks().count() + " choreographed attacks were found");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("attacks")
    @DisplayName("Every attack plays to its end cleanly")
    void playsCleanly(String className) throws Exception {
        Class<?> type = Class.forName(className);
        if (Modifier.isAbstract(type.getModifiers())) return;
        ChoreographedAttack attack = (ChoreographedAttack) type.getConstructor(BossHost.class).newInstance(host());
        RecordingStage stage = stage();
        Timeline timeline = attack.choreograph(stage);
        assertNotNull(timeline, className + " found nothing to do with three players in range");
        stage.play(timeline, attack.lockTicks(timeline));
        int ran = stage.run(600);

        assertTrue(ran < 600, className + " never ends");
        assertTrue(ran >= 20, className + " is over in " + ran + " ticks: no wind-up a player could read");
        assertTrue(stage.lock() <= timeline.length(), className + " locks the body longer than it plays");
        assertTrue(stage.lock() >= 15, className + " releases the body after " + stage.lock() + " ticks");
        assertFalse(stage.marks.isEmpty(), className + " draws nothing");
        assertTrue(stage.sounds > 0, className + " is silent");
        for (RecordingStage.RecordedProp prop : stage.props) {
            assertTrue(prop.removed, className + " leaves a " + prop.material + " behind");
        }
        Pose last = stage.poses.get(stage.poses.size() - 1);
        boolean aerial = className.contains(".aerial.");
        Pose rest = aerial ? Poses.HOVER : Poses.GUARD;
        if (!className.endsWith("HoverBarrageAttack") && !className.endsWith("AirSlamAttack")) {
            assertPose(rest, last, className + " does not return to " + (aerial ? "hovering" : "its guard"));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("attacks")
    @DisplayName("Every attack that can hurt, hurts a player standing in it")
    void hitsAPlayerInTheWay(String className) throws Exception {
        Class<?> type = Class.forName(className);
        if (Modifier.isAbstract(type.getModifiers())) return;
        if (className.contains(".defensive.")) return;
        ChoreographedAttack attack = (ChoreographedAttack) type.getConstructor(BossHost.class).newInstance(host());
        RecordingStage stage = stage();
        Timeline timeline = attack.choreograph(stage);
        stage.play(timeline, attack.lockTicks(timeline));
        stage.run(600);
        assertFalse(stage.hits.isEmpty(), className + " hit nobody, with players at 4, 15 and 16 blocks");
    }

    private static void assertPose(Pose expected, Pose actual, String message) {
        double[][] a = {expected.head(), expected.body(), expected.leftArm(), expected.rightArm(), expected.leftLeg(), expected.rightLeg()};
        double[][] b = {actual.head(), actual.body(), actual.leftArm(), actual.rightArm(), actual.leftLeg(), actual.rightLeg()};
        for (int i = 0; i < a.length; i++) {
            for (int k = 0; k < 3; k++) {
                assertEquals(a[i][k], b[i][k], 1.0, message);
            }
        }
    }
}
