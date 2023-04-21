package com.octavemc.visualise;

import com.octavemc.Apollo;
import com.octavemc.faction.struct.Relation;
import com.octavemc.faction.type.Faction;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Optional;

public enum VisualType {

    /**
     * Represents the wall approaching claims when Spawn Tagged.
     */
    SPAWN_BORDER() {
        private final BlockFiller blockFiller = new BlockFiller() {
            @Override
            VisualBlockData generate(Player player, Location location) {
                return new VisualBlockData(Material.STAINED_GLASS, DyeColor.RED.getData());
            }
        };

        @Override
        BlockFiller blockFiller() {
            return blockFiller;
        }
    },
    /**
     * Represents the wall approaching claims when PVP Protected.
     */
    CLAIM_BORDER() {
        private final BlockFiller blockFiller = new BlockFiller() {
            @Override
            VisualBlockData generate(Player player, Location location) {
                return new VisualBlockData(Material.STAINED_GLASS, DyeColor.GREEN.getData());
            }
        };

        @Override
        BlockFiller blockFiller() {
            return blockFiller;
        }
    },
    /**
     * Represents claims shown using /faction map.
     */
    SUBCLAIM_MAP() {
        private final BlockFiller blockFiller = new BlockFiller() {
            @Override
            VisualBlockData generate(Player player, Location location) {
                return new VisualBlockData(Material.LOG, (byte) 1);
            }
        };

        @Override
        BlockFiller blockFiller() {
            return blockFiller;
        }
    },
    /**
     * Represents claims shown using /faction map.
     */
    CLAIM_MAP() {
        private final BlockFiller blockFiller = new BlockFiller() {
            private final Material[] types = new Material[] {
                    Material.COAL_BLOCK,
                    Material.IRON_BLOCK,
                    Material.LAPIS_BLOCK,
                    Material.REDSTONE_BLOCK,
                    Material.DIAMOND_BLOCK,
                    Material.EMERALD_BLOCK
            };

            private int materialCounter = 0;

            @Override
            VisualBlockData generate(Player player, Location location) {
                int y = location.getBlockY();
                if (y == 0 || y % 4 == 0) {
                    return new VisualBlockData(types[materialCounter]);
                }

                Optional<Faction> faction = Apollo.getInstance().getFactionDao().getFactionAt(location);
                return new VisualBlockData(Material.STAINED_GLASS, (faction.isPresent() ? faction.get().getRelation(player) : Relation.ENEMY).toDyeColour().getData());
            }

            @Override
            ArrayList<VisualBlockData> bulkGenerate(Player player, Iterable<Location> locations) {
                ArrayList<VisualBlockData> result = super.bulkGenerate(player, locations);
                if (++materialCounter == types.length) materialCounter = 0;
                return result;
            }
        };

        @Override
        BlockFiller blockFiller() {
            return blockFiller;
        }
    },

    CREATE_CLAIM_SELECTION() {
        private final BlockFiller blockFiller = new BlockFiller() {
            @Override
            VisualBlockData generate(Player player, Location location) {
                return new VisualBlockData(location.getBlockY() % 4 != 0 ? Material.GLASS : Material.GOLD_BLOCK);
            }
        };

        @Override
        BlockFiller blockFiller() {
            return blockFiller;
        }
    },;

    /**
     * Gets the {@link BlockFiller} instance.
     *
     * @return the filler
     */
    abstract BlockFiller blockFiller();
}
