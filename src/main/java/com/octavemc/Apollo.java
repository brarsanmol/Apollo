package com.octavemc;

import co.aikar.commands.MessageType;
import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.inject.Guice;
import com.google.inject.Singleton;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.octavemc.broadcaster.BroadcastManager;
import com.octavemc.combatlogging.CombatLogListener;
import com.octavemc.combatlogging.LoggerEntityVillager;
import com.octavemc.command.*;
import com.octavemc.crates.Crate;
import com.octavemc.crates.CrateDao;
import com.octavemc.crates.CratesCommand;
import com.octavemc.crates.CratesListener;
import com.octavemc.deathban.DeathbanListener;
import com.octavemc.deathban.DeathbanManager;
import com.octavemc.deathban.DeathbansCommand;
import com.octavemc.deathban.lives.LivesCommand;
import com.octavemc.economy.BalanceCommand;
import com.octavemc.economy.EconomyCommand;
import com.octavemc.economy.PayCommand;
import com.octavemc.economy.merchants.MerchantDao;
import com.octavemc.economy.merchants.MerchantEntityVillager;
import com.octavemc.economy.merchants.MerchantListener;
import com.octavemc.economy.merchants.MerchantsCommand;
import com.octavemc.eventgame.EventClaimWandListener;
import com.octavemc.eventgame.EventCommand;
import com.octavemc.eventgame.EventScheduler;
import com.octavemc.eventgame.conquest.ConquestCommand;
import com.octavemc.eventgame.eotw.EotwCommand;
import com.octavemc.eventgame.eotw.EotwHandler;
import com.octavemc.eventgame.eotw.EotwListener;
import com.octavemc.eventgame.koth.KothCommand;
import com.octavemc.faction.FactionDao;
import com.octavemc.faction.FactionsCommand;
import com.octavemc.faction.claim.*;
import com.octavemc.faction.type.*;
import com.octavemc.guice.DaoModule;
import com.octavemc.guice.PluginModule;
import com.octavemc.listener.*;
import com.octavemc.listener.fixes.*;
import com.octavemc.pvpclass.PvpClassManager;
import com.octavemc.pvpclass.bard.EffectRestorer;
import com.octavemc.pvpclass.event.ArmorListener;
import com.octavemc.sidebar.SidebarAdapterImpl;
import com.octavemc.sidebar.Zeus;
import com.octavemc.sotw.SotwCommand;
import com.octavemc.sotw.SotwListener;
import com.octavemc.sotw.SotwTimer;
import com.octavemc.stacking.EntityStackingListener;
import com.octavemc.stacking.EntityStackingTask;
import com.octavemc.tablist.*;
import com.octavemc.timer.TimerCommand;
import com.octavemc.timer.TimerManager;
import com.octavemc.user.User;
import com.octavemc.user.UserDao;
import com.octavemc.util.PersistableLocation;
import com.octavemc.visualise.VisualiseHandler;
import com.octavemc.visualise.VisualisePacketListeners;
import com.octavemc.visualise.WallBorderListener;
import com.octavemc.voting.VoteListener;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import org.bson.UuidRepresentation;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.TimeZone;

@Getter
@Singleton
public final class Apollo extends JavaPlugin {

    @Getter
    private static Apollo instance;
    private ClaimHandler claimHandler;
    private CombatLogListener combatLogListener;
    private DeathbanManager deathbanManager;
    private EffectRestorer effectRestorer;
    private EotwHandler eotwHandler;
    private EventScheduler eventScheduler;
    private ImageFolder imageFolder;
    private PvpClassManager pvpClassManager;
    private SotwTimer sotwTimer;
    private TimerManager timerManager;
    private VisualiseHandler visualiseHandler;
    private PaperCommandManager commandManager;
    private ProtocolManager protocolManager;
    private Zeus zeus;
    private TablistManager tablistManager;
    private TablistKeyGenerator tablistKeyGenerator;
    private Datastore datastore;
    private UserDao userDao;
    private FactionDao factionDao;
    private CrateDao crateDao;
    private MerchantDao merchantDao;
    private BroadcastManager broadcastManager;
    private LuckPerms luckPerms;

    static {
        ConfigurationSerialization.registerClass(PersistableLocation.class);
        ConfigurationSerialization.registerClass(Crate.class);
        ConfigurationSerialization.registerClass(MerchantEntityVillager.class);
    }

    public Object getPrivateField(String fieldName, Class clazz, Object object)
    {
        Field field;
        Object o = null;
        try
        {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            o = field.get(object);
        }
        catch(NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return o;
    }

    private void addToMaps(Class clazz, String name, int id)
    {
        //getPrivateField is the method from above.
        //Remove the lines with // in front of them if you want to override default entities (You'd have to remove the default entity from the map first though).
        ((Map)getPrivateField("c", net.minecraft.server.v1_8_R3.EntityTypes.class, null)).put(name, clazz);
        ((Map)getPrivateField("d", net.minecraft.server.v1_8_R3.EntityTypes.class, null)).put(clazz, name);
        //((Map)getPrivateField("e", net.minecraft.server.v1_7_R4.EntityTypes.class, null)).put(Integer.valueOf(id), clazz);
        ((Map)getPrivateField("f", net.minecraft.server.v1_8_R3.EntityTypes.class, null)).put(clazz, Integer.valueOf(id));
        //((Map)getPrivateField("g", net.minecraft.server.v1_7_R4.EntityTypes.class, null)).put(name, Integer.valueOf(id));
    }

    @Override
    public void onEnable() {
        instance = this;

        var injector = Guice.createInjector(
                new PluginModule(this),
                new DaoModule()
        );

        var provider = this.getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) this.luckPerms = provider.getProvider();

        this.getServer().getWorld("world").getWorldBorder().setCenter(0, 0);
        this.getServer().getWorld("world").getWorldBorder().setSize(Configuration.WORLD_BORDER);
        this.getServer().getWorld("world").getWorldBorder().setWarningDistance(Configuration.WORLD_BORDER_WARNING_DISTANCE);
        this.getServer().getWorld("world").getWorldBorder().setWarningTime(Configuration.WORLD_BORDER_WARNING_TIME);

        DateTimeFormats.reload(TimeZone.getTimeZone("America/Toronto"));
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        addToMaps(LoggerEntityVillager.class, "Villager", 120);
        addToMaps(MerchantEntityVillager.class, "Villager", 120);

        this.commandManager = new PaperCommandManager(this);
        this.commandManager.enableUnstableAPI("help");
        this.commandManager.setFormat(MessageType.SYNTAX, ChatColor.AQUA, ChatColor.WHITE);
        this.commandManager.setFormat(MessageType.HELP, ChatColor.AQUA, ChatColor.WHITE);
        this.commandManager.setFormat(MessageType.ERROR, ChatColor.RED);
        this.zeus = new Zeus(this, new SidebarAdapterImpl());
        this.tablistManager = new TablistManager();

        registerCommands();
        registerManagers();
        registerListeners();

        new TablistTask();
        new TablistUpdateTask();
        new EntityStackingTask();
        this.tablistKeyGenerator = new TablistKeyGenerator();
        this.factionDao.loadAll();
        this.crateDao.loadAll();
        this.merchantDao.loadAll();

        this.broadcastManager = new BroadcastManager();
    }

    @Override
    public void onDisable() {
        this.merchantDao.saveAll();
        this.crateDao.saveAll();
        this.factionDao.saveAll();
        this.userDao.updateAll();
        this.merchantDao.getCache().clear();
        this.crateDao.getCache().clear();
        this.factionDao.getCache().clear();
        this.userDao.getCache().clear();
        combatLogListener.removeCombatLoggers();
        pvpClassManager.onDisable();

        instance = null;
    }

    private void registerListeners() {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new BlockHitFixListener(), this);
        new BlockJumpGlitchFixListener();
        new BoatGlitchFixListener();
        new BookDisenchantListener();
        new BottledExpListener();
        new ChatListener();
        manager.registerEvents(new ClaimWandListener(), this);
        manager.registerEvents(new EventClaimWandListener(), this);
        manager.registerEvents(new EventClaimWandListener(), this);
        manager.registerEvents(combatLogListener = new CombatLogListener(), this);
        new CoreListener();
        manager.registerEvents(new CrowbarListener(this), this);
        new DeathListener();
        manager.registerEvents(new DeathbanListener(), this);
        manager.registerEvents(new EnchantLimitListener(this), this);
        new EnderchestRemovalListener();
        new EntityLimitListener();
        manager.registerEvents(new EotwListener(), this);
        new EventSignListener();
        manager.registerEvents(new ExpMultiplierListener(this), this);
        manager.registerEvents(new FactionListener(), this);
        new FurnaceSmeltSpeedListener();
        new InfinityArrowFixListener();
        new EnderpearlGlitchListener();
        new PortalListener();
        new PotionLimitListener();
        manager.registerEvents(new ProtectionListener(this), this);
        manager.registerEvents(new SubclaimWandListener(this), this);
        manager.registerEvents(new SignSubclaimListener(this), this);
        new SkullListener();
        new SotwListener();
        manager.registerEvents(new BeaconStrengthFixListener(this), this);
        new VoidGlitchFixListener();
        manager.registerEvents(new WallBorderListener(this), this);
        new WorldListener();

        // Used for visualising player walls.
        new VisualisePacketListeners();

        new TablistListener();
        new ArmorListener();
        new CratesListener();
        new SwordLoreListener();
        new PickaxeLoreListener();
        new CustomArrowListener();
        new AutoSmeltListener();
        new EntityStackingListener();
        new DeathMessageListener();
        new NametagManager();
        new MerchantListener();
        new VoteListener();
        //TODO: Should remove Lunar Client.
        new LunarClientListener();
    }

    private void registerCommands() {
        new CratesCommand();
        new EconomyCommand();
        new EotwCommand();
        new EventCommand();
        new FactionsCommand();
        new GoppleCommand();
        new KothCommand();
        new LivesCommand();
        new LogoutCommand();
        new MapKitCommand();
        new PayCommand();
        new BalanceCommand();
        new InvincibilityCommand();
        //TODO: Fix this command.
        // getCommand("regen").setExecutor(new RegenCommand());
        new SotwCommand();
        new TimerCommand();
        new DeathbansCommand();
        new KitCommand();
        new ClearChatCommand();
        new SpeedCommand();
        new SpawnCommand();
        new StressTestCommand();
        new RenameCommand();
        new CraftCommand();
        new ConquestCommand();
        new MerchantsCommand();
    }

    private void registerManagers() {
        claimHandler = new ClaimHandler();
        deathbanManager = new DeathbanManager();
        effectRestorer = new EffectRestorer(this);
        eotwHandler = new EotwHandler();
        eventScheduler = new EventScheduler();
        imageFolder = new ImageFolder();
        pvpClassManager = new PvpClassManager(this);
        sotwTimer = new SotwTimer();
        timerManager = new TimerManager();
        visualiseHandler = new VisualiseHandler();
    }
}
