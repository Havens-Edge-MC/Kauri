package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.api.events.ListenerPriority;
import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutRelativePosition;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import lombok.val;

@Init
public class PacketListener implements AtlasListener {

    @Listen(ignoreCancelled = true, priority = ListenerPriority.LOW)
    public void onEvent(PacketReceiveEvent event) {
        if(event.getPlayer() == null || event.isCancelled()) return;
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data == null || data.checkManager == null) return;

        Kauri.INSTANCE.packetProcessor.processClient(event,
                data, event.getPacket(), event.getType(), event.getTimeStamp());

        switch(event.getType()) {
            case Packet.Client.USE_ENTITY: {
                val packet = new WrappedInUseEntityPacket(event.getPacket(), event.getPlayer());

                if(data.checkManager.runPacketCancellable(packet, event.getTimeStamp())) {
                    event.setCancelled(true);
                }
                break;
            }
            case Packet.Client.FLYING:
            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK: {
                val packet = new WrappedInFlyingPacket(event.getPacket(), event.getPlayer());

                if(data.checkManager.runPacketCancellable(packet, event.getTimeStamp())) {
                    event.setCancelled(true);
                }
                break;
            }
        }
    }

    @Listen(ignoreCancelled = true,priority = ListenerPriority.LOW)
    public void onEvent(PacketSendEvent event) {
        if(event.isCancelled() || event.getPlayer() == null
                || !Kauri.INSTANCE.enabled || event.getPacket() == null) return;

        if(!Kauri.INSTANCE.dataManager.dataMap.containsKey(event.getPlayer().getUniqueId()))
            return;

        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data == null || data.checkManager == null) return;

        Kauri.INSTANCE.packetProcessor.processServer(event,
                data, event.getPacket(), event.getType(), event.getTimeStamp());

        switch(event.getType()) {
            case Packet.Server.REL_LOOK:
            case Packet.Server.REL_POSITION:
            case Packet.Server.REL_POSITION_LOOK:
            case Packet.Server.ENTITY:
            case Packet.Server.LEGACY_REL_LOOK:
            case Packet.Server.LEGACY_REL_POSITION:
            case Packet.Server.LEGACY_REL_POSITION_LOOK: {
                val packet = new WrappedOutRelativePosition(event.getPacket(), event.getPlayer());

                if(data.checkManager.runPacketCancellable(packet, event.getTimeStamp())) {
                    event.setCancelled(true);
                }
                break;
            }
            case Packet.Server.TRANSACTION: {
                val packet = new WrappedOutTransaction(event.getPacket(), event.getPlayer());

                if(data.checkManager.runPacketCancellable(packet, event.getTimeStamp())) {
                    event.setCancelled(true);
                }
                break;
            }
            case Packet.Server.ENTITY_VELOCITY: {
                val packet = new WrappedOutVelocityPacket(event.getPacket(), event.getPlayer());

                if(data.checkManager.runPacketCancellable(packet, event.getTimeStamp())) {
                    event.setCancelled(true);
                }
                break;
            }
        }
    }
}
