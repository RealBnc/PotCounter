package com.potcounter.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class NametagHelper {

    public static Text extractEntityText(Entity entity) {
        if (entity == null) return null;
        if (entity instanceof DisplayEntity.TextDisplayEntity td) {
            DisplayEntity.TextDisplayEntity.Data data = td.getData();
            if (data != null && data.text() != null) {
                return data.text();
            }
        } else if (entity instanceof ArmorStandEntity as) {
            if (as.hasCustomName()) {
                return as.getCustomName();
            }
        }
        return null;
    }

    public static boolean containsPlayerName(Entity entity, String playerName) {
        if (entity == null || playerName == null || playerName.isEmpty()) return false;
        Text text = extractEntityText(entity);
        if (text == null) return false;
        String raw = text.getString().toLowerCase();
        return raw.contains(playerName.toLowerCase());
    }

    public static Entity findServerNametagEntity(PlayerEntity player) {
        if (player == null) return null;
        String name = player.getName().getString();

        for (Entity passenger : player.getPassengersDeep()) {
            if (containsPlayerName(passenger, name)) {
                return passenger;
            }
        }

        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (client != null && client.world != null) {
            List<? extends Entity> nearby = client.world.getOtherEntities(player, player.getBoundingBox().expand(2.5, 4.0, 2.5));
            for (Entity e : nearby) {
                if (containsPlayerName(e, name)) {
                    return e;
                }
            }
        }

        return null;
    }

    public static PlayerEntity findAssociatedPlayer(Entity entity, PlayerEntity localPlayer) {
        if (entity == null) return null;

        Entity root = entity.getRootVehicle();
        if (root instanceof PlayerEntity p) {
            if (localPlayer != null && p.getUuid().equals(localPlayer.getUuid())) return null;
            return p;
        }

        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (client != null && client.world != null) {
            List<? extends PlayerEntity> players = client.world.getPlayers();
            for (PlayerEntity p : players) {
                if (localPlayer != null && p.getUuid().equals(localPlayer.getUuid())) continue;

                double dx = Math.abs(p.getX() - entity.getX());
                double dz = Math.abs(p.getZ() - entity.getZ());
                double dy = Math.abs(p.getY() - entity.getY());

                if (dx <= 2.5 && dz <= 2.5 && dy <= 4.5) {
                    if (containsPlayerName(entity, p.getName().getString())) {
                        return p;
                    }
                }
            }
        }

        return null;
    }
}
