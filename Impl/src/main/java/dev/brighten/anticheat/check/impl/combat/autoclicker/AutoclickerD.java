package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.Interval;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Autoclicker (D)", description = "Checks if the autoclicker clicks within a specific deviation.",
        checkType = CheckType.AUTOCLICKER, developer = true, punishVL = 20)
public class AutoclickerD extends Check {

    private Interval interval = new Interval(22);

    private long lTimestamp;
    private double lavg, lstd;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delta = timeStamp - lTimestamp;

        if(delta > 2000 || delta < 3 || data.playerInfo.lastBrokenBlock.hasNotPassed(5)) {
            lTimestamp = timeStamp;
            return;
        }

        if(interval.size() >= 20) {
            val stats = interval.getSummary();

            double avg = stats.getAverage();
            double std = interval.std();

            double deltaStd = MathUtils.getDelta(std, lstd), deltaAvg = MathUtils.getDelta(avg, lavg);
            if(deltaStd < 3 && avg < 125 && deltaAvg > 8) {
                vl++;
                if(vl > 4) {
                    flag("avg=%1 std=%2 deltaAvg=%3", MathUtils.round(avg, 4),
                            MathUtils.round(deltaStd, 4), MathUtils.round(deltaAvg, 4));
                }
            } else vl-= vl > 0 ? 0.25 : 0;
            debug("vl=" + vl + " avg=" + MathUtils.round(avg, 3)
                    + " std=" + MathUtils.round(std, 3));


            interval.clear();
            lstd = std;
            lavg = avg;
        } else interval.add(delta);
        lTimestamp = timeStamp;
    }
}
