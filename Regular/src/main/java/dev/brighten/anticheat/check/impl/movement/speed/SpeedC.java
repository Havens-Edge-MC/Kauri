package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;
import lombok.RequiredArgsConstructor;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Speed (C)", description = "Checks if a player moves past the absolute maximum speed they can possible do.",
        punishVL = 34, developer = true)
@Cancellable
public class SpeedC extends Check {

    private float verbose;
    private boolean sprinting;

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(!packet.isPos() || data.playerInfo.generalCancel) {
            if(data.playerInfo.generalCancel && verbose > 0) verbose--;
            return;
        }

        final MaxThreshold threshold = MaxThreshold.getThreshold(data.playerInfo.clientGround, sprinting);
        final int speed = data.potionProcessor.getEffectByType(PotionEffectType.SPEED)
                .map(ef -> ef.getAmplifier() + 1)
                .orElse(0);

        double calc = threshold.getCalculatedValue(data.getPlayer().getWalkSpeed(),
                speed, data.playerInfo.jumped);

        if(data.playerInfo.lastVelocity.isNotPassed(25)) {
            double velocityXZ = Math.hypot(data.playerInfo.velocityX, data.playerInfo.velocityZ);

            if(velocityXZ > data.playerInfo.deltaXZ) calc = velocityXZ;
        }

        if(data.playerInfo.deltaXZ > calc) {
            flag("t=[%s] %.3f>-%.3f jumped=%s speed=%s", threshold.display,
                    data.playerInfo.deltaXZ, calc, data.playerInfo.jumped, speed);
            vl++;
        }

        debug("threshold=%s deltaXZ=%.4f calc=%.4f jumped=%s speed=%s",
                threshold.display, data.playerInfo.deltaXZ, calc, data.playerInfo.jumped, speed);

        if(data.playerInfo.clientGround) sprinting = data.playerInfo.sprinting;
    }

    @RequiredArgsConstructor
    public enum MaxThreshold {

        GROUND_SPEED("Ground", 0.284, 1.3, 1.6, 1),
        GROUND_SPRINT_SPEED("Ground + Sprint", 0.43, 1.3, 1.6, 1),
        AIR_SPEED("Air", 0.28, 1.3, 1.6, 1),
        AIR_SPRINT_SPEED("Air + Sprint", 0.45, 1.3, 1.6, 2);

        public final String display;
        public final double threshold,  speedMultiplier, walkSpeedMultiplier, jumpMultiplier;

        public static MaxThreshold getThreshold(boolean ground, boolean sprint) {
            if(ground) {
                return sprint ? GROUND_SPRINT_SPEED : GROUND_SPEED;
            } else return sprint ? AIR_SPRINT_SPEED : AIR_SPEED;
        }

        public double getCalculatedValue(double walkSpeed, int speedAmplifier, boolean jumped) {
            double base = threshold;

            if(walkSpeed > 0.2) {
                base*= (1 + (walkSpeed - 0.2)) * walkSpeedMultiplier;
            }

            if(speedAmplifier > 0)
            base*= speedAmplifier * speedMultiplier;

            if(jumped) base*= jumpMultiplier;

            return base;
        }
    }
}