package com.octavemc.listener;

import com.octavemc.Apollo;
import com.octavemc.util.NameUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathMessageListener implements Listener {

    public DeathMessageListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        var entity = event.getEntity();
            switch (entity.getLastDamageCause().getCause()) {
                case FALL -> {
                    if (entity.getKiller() != null)
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] hit the ground too hard whilst trying to escape "
                                + ChatColor.AQUA + event.getEntity().getKiller().getName()
                                + ChatColor.GRAY + "[" + ChatColor.AQUA + entity.getKiller().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "].");
                    else
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + ']' + ChatColor.GRAY + " hit the ground too hard.");
                }
                case FIRE -> {
                    if (entity.getKiller() != null)
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] walked into fire whilst fighting  "
                                + ChatColor.AQUA + event.getEntity().getKiller().getName()
                                + ChatColor.GRAY + "[" + ChatColor.AQUA + entity.getKiller().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "].");
                    else
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + ']' + ChatColor.GRAY + " went up in flames.");
                }
                case LAVA -> {
                    event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                            + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + ']' + ChatColor.GRAY + " tried to swim in lava.");
                }
                case VOID ->  {
                    if (entity.getKiller() != null)
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] didn't want to live in the same world as "
                                + ChatColor.AQUA + event.getEntity().getKiller().getName()
                                + ChatColor.GRAY + "[" + ChatColor.AQUA + entity.getKiller().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "].");
                    else
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + ']' + ChatColor.GRAY + " fell out of the world.");
                }
                case MAGIC -> {
                    if (entity.getKiller() != null)
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] was killed by "
                                + ChatColor.AQUA + event.getEntity().getKiller().getName()
                                + ChatColor.GRAY + "[" + ChatColor.AQUA + entity.getKiller().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] using magic.");
                    else
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + ']' + ChatColor.GRAY + " was killed by magic.");
                }
                case THORNS -> {
                    if (entity.getKiller() != null)
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] was killed trying to hurt "
                                + ChatColor.AQUA + event.getEntity().getKiller().getName()
                                + ChatColor.GRAY + "[" + ChatColor.AQUA + entity.getKiller().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "].");
                    else
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] was killed trying to hurt a "
                                + ChatColor.AQUA + entity.getLastDamageCause().getEntity().getType().getName() + ChatColor.GRAY + ".");
                }
                case WITHER -> {
                    if (entity.getKiller() != null)
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] withered away whilst fighting "
                                + ChatColor.AQUA + event.getEntity().getKiller().getName()
                                + ChatColor.GRAY + "[" + ChatColor.AQUA + entity.getKiller().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "].");
                    else
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + ']' + ChatColor.GRAY + " withered away.");
                }
                case CONTACT -> {
                    if (entity.getKiller() != null)
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] walked into a cactus whilst trying to escape "
                                + ChatColor.AQUA + event.getEntity().getKiller().getName()
                                + ChatColor.GRAY + "[" + ChatColor.AQUA + entity.getKiller().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "].");
                    else
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + ']' + ChatColor.GRAY + " was pricked to death.");
                }
                case SUICIDE -> {
                    event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                            + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + ']' + ChatColor.GRAY + " didn't want to live in the world.");
                }
                case DROWNING -> {
                    if (entity.getKiller() != null)
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] drowned whilst trying to escape "
                                + ChatColor.AQUA + event.getEntity().getKiller().getName()
                                + ChatColor.GRAY + "[" + ChatColor.AQUA + entity.getKiller().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "].");
                    else
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + ']' + ChatColor.GRAY + " drowned.");
                }
                case FIRE_TICK -> {
                    if (entity.getKiller() != null)
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] was burnt to a crisp whilst fighting "
                                + ChatColor.AQUA + event.getEntity().getKiller().getName()
                                + ChatColor.GRAY + "[" + ChatColor.AQUA + entity.getKiller().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "].");
                    else
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + ']' + ChatColor.GRAY + " burned to death.");
                }
                case LIGHTNING -> {
                    if (entity.getKiller() != null)
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] was struck by lightning whilst fighting "
                                + ChatColor.AQUA + event.getEntity().getKiller().getName()
                                + ChatColor.GRAY + "[" + ChatColor.AQUA + entity.getKiller().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "].");
                    else
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + ']' + ChatColor.GRAY + " was struck by lightning.");
                }
                case PROJECTILE -> {
                    if (entity.getKiller() != null)
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] was shot by "
                                + ChatColor.AQUA + event.getEntity().getKiller().getName()
                                + ChatColor.GRAY + "[" + ChatColor.AQUA + entity.getKiller().getStatistic(Statistic.PLAYER_KILLS)+ ChatColor.GRAY + "] using "
                                + ChatColor.AQUA + (entity.getKiller().getItemInHand().getItemMeta().hasDisplayName() ? entity.getKiller().getItemInHand().getItemMeta().getDisplayName() : NameUtils.getPrettyName(entity.getKiller().getItemInHand().getType().name()))
                                + ChatColor.GRAY + '.');
                    else {
                        if (entity.getLastDamageCause() instanceof EntityDamageByEntityEvent damage)
                            event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                    + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] was shot by a "
                                    + ChatColor.AQUA + NameUtils.getPrettyName(damage.getDamager().getType().name()) + ChatColor.GRAY + '.');
                    }
                }
                case STARVATION -> {
                    if (entity.getKiller() != null)
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] starved to death whilst fighting "
                                + ChatColor.AQUA + event.getEntity().getKiller().getName()
                                + ChatColor.GRAY + "[" + ChatColor.AQUA + entity.getKiller().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "].");
                    else
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + ']' + ChatColor.GRAY + " starved to death.");
                }
                case SUFFOCATION -> {
                    if (entity.getKiller() != null)
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] suffocated in a wall whilst fighting  "
                                + ChatColor.AQUA + event.getEntity().getKiller().getName()
                                + ChatColor.GRAY + "[" + ChatColor.AQUA + entity.getKiller().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "].");
                    else
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + ']' + ChatColor.GRAY + " suffocated in a wall.");
                }
                case ENTITY_ATTACK -> {
                    if (entity.getKiller() != null)
                        //TODO: Clean up this code.
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] was slain by "
                                + ChatColor.AQUA + event.getEntity().getKiller().getName()
                                + ChatColor.GRAY + "[" + ChatColor.AQUA + entity.getKiller().getStatistic(Statistic.PLAYER_KILLS)+ ChatColor.GRAY + "] using "
                                + ChatColor.AQUA + (entity.getKiller().getItemInHand().getType() == Material.AIR
                                ? "their fists"
                                : (entity.getKiller().getItemInHand().getItemMeta().hasDisplayName()
                                ? entity.getKiller().getItemInHand().getItemMeta().getDisplayName()
                                : NameUtils.getPrettyName(entity.getKiller().getItemInHand().getType().name())))
                                + ChatColor.GRAY + '.');
                    else
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] was slain by a "
                                + ChatColor.AQUA + NameUtils.getPrettyName(event.getEntity().getLastDamageCause().getEntity().getType().name()) + ChatColor.GRAY + '.');
                }
                case FALLING_BLOCK -> {
                    if (entity.getKiller() != null)
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] was squashed by a falling block whilst fighting  "
                                + ChatColor.AQUA + event.getEntity().getKiller().getName()
                                + ChatColor.GRAY + "[" + ChatColor.AQUA + entity.getKiller().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "].");
                    else
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] was squashed by a falling block.");
                }
                case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> {
                    if (entity.getKiller() != null)
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] was blown up by  "
                                + ChatColor.AQUA + event.getEntity().getKiller().getName()
                                + ChatColor.GRAY + "[" + ChatColor.AQUA + entity.getKiller().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "].");
                    else
                        event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                                + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] blew up.");
                }
                case CUSTOM -> {
                    event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                            + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] died for unknown reasons.");
                }
                default -> {
                    event.setDeathMessage(ChatColor.AQUA + entity.getName() + ChatColor.GRAY + "[" + ChatColor.AQUA
                            + event.getEntity().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.GRAY + "] died.");
                }
            }
    }

}
