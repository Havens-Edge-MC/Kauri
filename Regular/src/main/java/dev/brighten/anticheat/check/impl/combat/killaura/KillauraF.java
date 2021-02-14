package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import org.bukkit.entity.Player;

@CheckInfo(name = "Killaura (F)", description = "Checks for proper sprint motion mechanics.",
        checkType = CheckType.KILLAURA)
@Cancellable(cancelType = CancelType.ATTACK)
public class KillauraF extends Check {

    private int buffer;
    private boolean attack;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(attack && data.playerInfo.sprinting) {
            double px = data.playerInfo.lDeltaX, pz = data.playerInfo.lDeltaZ;

            px*= 0.6;
            pz*= 0.6;

            double pxz = Math.hypot(px, pz), noxz = data.playerInfo.lDeltaXZ;

            double deltaYes = Math.abs(data.playerInfo.deltaXZ - pxz),
                    deltaNo = Math.abs(data.playerInfo.deltaXZ - noxz);

            if(deltaYes > 0.07 && deltaNo < 0.01) {
                if(++buffer > 5) {
                     vl++;
                     flag("dy=%.3f dn=%.3f dxz=%.2f noxz=%.2f",
                             deltaYes, deltaNo, data.playerInfo.deltaXZ, noxz);
                }
            } else if(buffer > 0) buffer--;

            debug("(%s) dxz=%.3f pxz=%.3f noxz=%.3f dYes=%.3f dNo=%.3f",
                    buffer, data.playerInfo.deltaXZ, pxz, noxz, deltaYes, deltaNo);
        }
        attack = false;
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(packet.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK
                && packet.getEntity() instanceof Player) {
            attack = true;
        }
    }
}
