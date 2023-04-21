package com.octavemc.command;

public class RegenCommand {

    /*
    private final Apollo plugin;

    public RegenCommand(Apollo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }

        Player player = (Player) sender;
        Optional<PlayerFaction> playerFaction = plugin.getFactionDao().getByPlayer(player.getUniqueId());
        if (!playerFaction.isPresent()) {
            sender.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        RegenStatus regenStatus = playerFaction.get().getRegenStatus();
        switch (regenStatus) {
            case FULL:
                sender.sendMessage(ChatColor.RED + "Your faction currently has full DTR.");
                return true;
            case PAUSED:
                sender.sendMessage(ChatColor.BLUE + "Your faction is currently on DTR freeze for another " + ChatColor.WHITE +
                        DurationFormatUtils.formatDurationWords(playerFaction.get().getRemainingRegenerationTime(), true, true) + ChatColor.BLUE + '.');

                return true;
            case REGENERATING:
                sender.sendMessage(ChatColor.BLUE + "Your faction currently has " + ChatColor.YELLOW + regenStatus.getSymbol() + ' ' +
                        playerFaction.get().getDeathsUntilRaidable() + ChatColor.BLUE + " DTR and is regenerating at a rate of " + ChatColor.GOLD +
                        Configuration.FACTION_DTR_UPDATE_INCREMENT + ChatColor.BLUE + " every " + ChatColor.GOLD +
                        Configuration.FACTION_DTR_UPDATE_TIME_WORDS + ChatColor.BLUE + ". Your ETA for maximum DTR is " + ChatColor.LIGHT_PURPLE +
                        DurationFormatUtils.formatDurationWords(getRemainingRegenMillis(playerFaction.get()), true, true) + ChatColor.BLUE + '.');

                return true;
        }

        sender.sendMessage(ChatColor.RED + "Unrecognised regen status, please inform an Administrator.");
        return true;
    }

    public long getRemainingRegenMillis(PlayerFaction faction) {
        long millisPassedSinceLastUpdate = System.currentTimeMillis() - faction.getLastDtrUpdateTimestamp();
        double dtrRequired = faction.getMaximumDeathsUntilRaidable() - faction.getDeathsUntilRaidable();
        return (long) ((Configuration.FACTION_DTR_UPDATE_MILLIS / Configuration.FACTION_DTR_UPDATE_INCREMENT) * dtrRequired) - millisPassedSinceLastUpdate;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return Collections.emptyList();
    }*/
}
