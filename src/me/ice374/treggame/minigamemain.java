package me.ice374.treggame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import static org.bukkit.ChatColor.*;

public class minigamemain extends JavaPlugin implements Listener
{
    Location spectate = null;
    Location quit = null;

    ArrayList<Player> gamePlayers = new ArrayList<Player>();
    ArrayList<Player> redTeam = new ArrayList<Player>();
    ArrayList<Player> blueTeam = new ArrayList<Player>();

    public static HashSet<String> cooldowns = new HashSet<String>();

    int blueScore = 0;
    int redScore = 0;

    public ItemStack Launcher = new ItemStack(Material.STONE_HOE, 1);
    {
        List<String> lore = new ArrayList<String>();
        ItemMeta im = Launcher.getItemMeta();
        im.setDisplayName(GOLD + "Rocket Launcher");
        lore.add(BLUE + "Type: " + GOLD + "Weapon");
        lore.add(RED + "Limited Ammo");
        im.setLore(lore);
        Launcher.setItemMeta(im);
    }

    public ItemStack Shotgun = new ItemStack(Material.WOOD_HOE, 1);
    {
        List<String> lore = new ArrayList<String>();
        ItemMeta im = Shotgun.getItemMeta();
        im.setDisplayName(GOLD + "Shotgun");
        lore.add(BLUE + "Type: " + GOLD + "Weapon");
        lore.add(GREEN + "Unlimited Ammo");
        im.setLore(lore);
        Shotgun.setItemMeta(im);
    }

    public ItemStack Laser = new ItemStack(Material.GOLD_HOE, 1);
    {
        List<String> lore = new ArrayList<String>();
        ItemMeta im = Laser.getItemMeta();
        im.setDisplayName(GOLD + "Laser");
        lore.add(BLUE + "Type: " + GOLD + "Weapon");
        lore.add(GREEN + "Unlimited Battery");
        im.setLore(lore);
        Laser.setItemMeta(im);
    }

    public ItemStack Rocket = new ItemStack(Material.REDSTONE, 1);
    {
        List<String> lore = new ArrayList<String>();
        ItemMeta im = Rocket.getItemMeta();
        im.setDisplayName(GRAY + "Rocket");
        lore.add(BLUE + "Type: " + DARK_GRAY + "Ammo");
        lore.add(BLUE + "For: " + GOLD + "Rocket Launcher");
        im.setLore(lore);
        Rocket.setItemMeta(im);
    }


    public final void onEnable() 
    {
        getServer().getPluginManager().registerEvents(this, this);

    }

    public final void minigameScoreboard(Player player) {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            public void run() 
            {
                ScoreboardManager manager = Bukkit.getScoreboardManager();
                Scoreboard board = manager.getNewScoreboard();

                Objective obj = board.registerNewObjective("Kills", "Kill Enemy Players");

                Team red = board.registerNewTeam("Red");
                Team blue = board.registerNewTeam("Blue");

                red.setPrefix(RED + "");
                blue.setPrefix(BLUE + "");

                red.setAllowFriendlyFire(false);
                blue.setAllowFriendlyFire(false);  

                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                obj.setDisplayName(GOLD + " ---"+ "" + DARK_GRAY + BOLD + " Kills " + GOLD + "---");

                Score redscore = obj.getScore(Bukkit.getOfflinePlayer(RED + "Red:"));
                Score bluescore = obj.getScore(Bukkit.getOfflinePlayer(BLUE + "Blue:"));

                redscore.setScore(redScore);
                bluescore.setScore(blueScore);

                for(Player p : Bukkit.getWorld("Dev").getPlayers()){
                    p.setScoreboard(board);
                }
            }

        }, 100L, 10L);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent e)
    {
        if (e.getBlock().getTypeId() == 55) 
        {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItems(PlayerDropItemEvent e) 
    {
        Player p = e.getPlayer();
        if ((!p.getWorld().getName().equals("Dev"))){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) 
    {
        if (e.getEntity() instanceof Arrow)
        {
            Player shooter = (Player)e.getEntity().getShooter();

            getServer().getWorld("dev").createExplosion(e.getEntity().getLocation(), 0, false);
            getServer().getWorld("dev").playEffect(e.getEntity().getLocation(), Effect.MOBSPAWNER_FLAMES, 5);
            List<Entity> eList = e.getEntity().getNearbyEntities(2, 2, 2);

            for (Entity pl : eList)
            {
                System.out.println(pl);

                if (pl instanceof Player) 
                {
                    Player target = (Player)pl;

                    if ( (( redTeam.contains(shooter) && blueTeam.contains(target) ) || ( blueTeam.contains(shooter) && redTeam.contains(target) )) && !target.isDead())
                    {
                        target.setHealth(0);

                        //                     addFrag(shooter, target.getName(), "Rocket Launcher");
                    }
                }
            }

            e.getEntity().remove();
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) 
    {
        if(e.getEntity().getWorld().equals("Dev")){


            if (e.getDamager() instanceof Arrow && e.getEntity() instanceof Player)
            {
                Player shooter = (Player) ((Snowball) e.getDamager()).getShooter();
                Player target = (Player) e.getEntity();

                e.setCancelled(true);

                target.setHealth(0);

                addKill(shooter, target.getName(), "Rocket Launcher");
            }


            if (e.getDamager() instanceof Snowball && e.getEntity() instanceof Player)
            {
                Player shooter = (Player) ((Snowball) e.getDamager()).getShooter();
                Player target = (Player) e.getEntity();

                e.setCancelled(true);

                if ( ( redTeam.contains(shooter) && blueTeam.contains(target) ) || ( blueTeam.contains(shooter) && redTeam.contains(target) ) )
                {
                    if (target.getHealth() - 6 >= 0)
                    {
                        target.setHealth(target.getHealth()-6);
                    }
                    else
                    {
                        target.setHealth(0);
                        addKill(shooter, target.getName(), "Shotgun");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) 
    {
        Player p = e.getPlayer();
        if (p.getWorld().getName().equals("Dev")){
            p.teleport(spectate);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) 
    {
        Player p = e.getPlayer();
        if ((!p.getWorld().getName().equals("Dev")) && gamePlayers.contains(p)){
            p.sendMessage(RED + "You have been removed from the game!");
            gamePlayers.remove(p);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e)
    {
        Player p = e.getPlayer();

        e.getPlayer().teleport(quit);

        if (gamePlayers.contains(p)) 
        {
            p.getInventory().clear();
            p.getInventory().getHelmet().setType(Material.AIR);
            gamePlayers.remove(p);
        }
        if (redTeam.contains(p))
        {
            redTeam.remove(p);
        }			
        if (blueTeam.contains(p))
        {
            blueTeam.remove(p);
        }

        for (PotionEffect effect : p.getActivePotionEffects()) 
        {
            p.removePotionEffect(effect.getType());
        }

    }

    public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args)
    {
        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("game"))
        {
            if (args.length == 0)
            {
                p.sendMessage("Correct Usage: " + GREEN + "/game join " + RESET + "or " + RED + "/game exit");
            }else{
                if (args[0].equalsIgnoreCase("join")){

                    if (args.length < 2){
                        p.sendMessage("Correct Usage: " + RED + "/game join red " + RESET + "or " + BLUE + "/game join blue");
                        return false;
                    }

                    if (args[1].equalsIgnoreCase("red"))
                    {
                        if (redTeam.contains(p)) {
                            redTeam.remove(p);
                        } if (blueTeam.contains(p)) {
                            blueTeam.remove(p);
                        }

                        p.getInventory().clear();
                        p.sendMessage(RED + "You have joined Team Red!");
                        gamePlayers.add(p);
                        redTeam.add(p);
                        p.teleport(redSpawn());

                        p.getInventory().addItem(Shotgun);
                        p.getInventory().addItem(Laser);

                        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 12000, 1));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 3));
                        p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 14));

                        minigameScoreboard(p);
                    }

                    if (args[1].equalsIgnoreCase("blue"))
                    {
                        if (redTeam.contains(p)) {
                            redTeam.remove(p);
                        } if (blueTeam.contains(p)) {
                            blueTeam.remove(p);
                        }

                        p.getInventory().clear();
                        p.sendMessage(BLUE + "You have joined Team Blue!");
                        gamePlayers.add(p);
                        blueTeam.add(p);
                        p.teleport(blueSpawn());

                        p.getInventory().addItem(Shotgun);
                        p.getInventory().addItem(Laser);

                        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 12000, 1));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 3));
                        p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 9));

                        minigameScoreboard(p);
                    }
                }
                if (args[0].equalsIgnoreCase("exit")){
                    p.sendMessage("Add Exit");
                }
                if (args[0].equalsIgnoreCase("Start")){
                    spectate = new Location(getServer().getWorld("dev"), 315.5, 63, 425.5);
                    spectate.setPitch(6);
                    spectate.setYaw(0);

                    quit = new Location(getServer().getWorld("dev"), 0.5, 63, 0.5);
                    quit.setPitch(6);
                    quit.setYaw(0);

                    spawnBonusWeapon();
                    
                    for(Player online : Bukkit.getOnlinePlayers()){
                        online.sendMessage(DARK_PURPLE + p.getName() + DARK_GRAY + " has just started a game of " + GOLD + "ThinkOfGameName" + DARK_GRAY + ".");
                        online.sendMessage(DARK_GRAY + "To join in type " + GREEN + "/game join" + DARK_GRAY + " !");
                    }
                }
            }
        }

        return false;
    }

    @EventHandler
    public void onArmorSlot(InventoryClickEvent event) {
        HumanEntity p = event.getWhoClicked();
        if (p.getWorld().getName().equals("Dev")){
            if (event.getSlotType().equals(SlotType.ARMOR)){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) 
    {

        if (e.getEntity() instanceof Player) 
        {
            Player p = (Player)e.getEntity();

            if (!p.isDead())
            {
                if (e.getCause() == DamageCause.VOID)
                {
                    p.setHealth(0);
                }

                if (e.getCause() == DamageCause.FALL) 
                {
                    e.setCancelled(true);
                }
            }


        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) 
    {
        //   final Player p = e.getEntity();

        e.getDrops().clear();
        e.setDeathMessage("");
    }

    /**
     * Add to make it so donators instantly respawn
     */

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e)
    {
        final Player p = e.getPlayer();

        if (redTeam.contains(p))
        {
            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() 
            {
                public void run()
                {
                    p.teleport(redReSpawn());
                }
            }, 2);
        }

        if (blueTeam.contains(p))
        {
            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() 
            {
                public void run()
                {
                    p.teleport(blueReSpawn());
                }
            }, 2);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        Player p = e.getPlayer();
        
        if (e.getAction() == Action.PHYSICAL && e.getClickedBlock().getType() == Material.WOOD_PLATE) {
            Location bLoc = e.getClickedBlock().getLocation();
            
            if ((bLoc.getX() == 323 && bLoc.getY() == 66) && (bLoc.getZ() == 425)) {
                p.teleport(blueSpawn());;
                p.sendMessage(AQUA + "You have rejoined the game!");
                
                p.getInventory().addItem(Shotgun);
                p.getInventory().addItem(Laser);
                p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 12000, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 3));
                p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 9));
            }

            if ((bLoc.getX() == 307 && bLoc.getY() == 66) && (bLoc.getZ() == 425)) {
                p.teleport(redSpawn());
                p.sendMessage(AQUA + "You have rejoined the game!");
                
                p.getInventory().addItem(Shotgun);
                p.getInventory().addItem(Laser);
                p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 12000, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 3));
                p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 14));
            }
        }
         

        //  =====Launcher=====

        if (gamePlayers.contains(e.getPlayer()) && e.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals(GOLD + "Rocket Launcher") && (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)) 
        {

            if(p.getExp() < 1F) {
                if (cooldowns.contains(p.getDisplayName())) {
                    e.getPlayer().sendMessage(RED + "You can't shoot while you are reloading!");
                    return;
                }

                e.setCancelled(true);
                BukkitTask task = new Cooldown(p).runTaskTimer(this, 2, 1);
                cooldowns.add(p.getDisplayName());

            }else{
                if (p.getInventory().contains(Material.REDSTONE))
                {
                    Arrow arrow = p.getWorld().spawn(p.getEyeLocation(), Arrow.class);
                    arrow.setShooter(p);
                    arrow.setVelocity(p.getLocation().getDirection().multiply(4));

                    p.getInventory().remove(Rocket);

                    p.getWorld().playSound(p.getLocation(), Sound.CREEPER_HISS, 1, 1);
                }
                else
                {
                    p.sendMessage(RED + "You pull the trigger, but have no ammo!");
                }

                p.setExp(0F);
            }
        }

        //       =====Shotgun=====


        if (gamePlayers.contains(e.getPlayer()) && e.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals(GOLD + "Shotgun") && (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)) 
        {

            if(p.getExp() < 1F) {
                if (cooldowns.contains(p.getDisplayName())) {
                    e.getPlayer().sendMessage(RED + "You can't shoot while you are reloading!");
                    return;
                }

                e.setCancelled(true);
                BukkitTask task = new Cooldown(p).runTaskTimer(this, 2, 1);
                cooldowns.add(p.getDisplayName());

            }else{

                Vector vec = p.getLocation().getDirection().multiply(4);
                double yRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
                double xRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
                double zRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
                vec.setY(vec.getY()+yRand);
                vec.setX(vec.getX()+xRand);
                vec.setZ(vec.getZ()+zRand);

                Snowball ball = p.getWorld().spawn(p.getEyeLocation(), Snowball.class);
                ball.setShooter(p);
                ball.setVelocity(vec);

                yRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
                xRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
                zRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
                vec = p.getLocation().getDirection().multiply(4);
                vec.setY(vec.getY()+yRand);
                vec.setX(vec.getX()+xRand);
                vec.setZ(vec.getZ()+zRand);

                Snowball ball2 = p.getWorld().spawn(p.getEyeLocation(), Snowball.class);
                ball2.setShooter(p);
                ball2.setVelocity(vec.setY(vec.getY()-0.3));

                yRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
                xRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
                zRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
                vec = p.getLocation().getDirection().multiply(4);
                vec.setY(vec.getY()+yRand);
                vec.setX(vec.getX()+xRand);
                vec.setZ(vec.getZ()+zRand);

                Snowball ball3 = p.getWorld().spawn(p.getEyeLocation(), Snowball.class);
                ball3.setShooter(p);
                ball3.setVelocity(vec.setY(vec.getY()-0.3));

                yRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
                xRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
                zRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
                vec = p.getLocation().getDirection().multiply(4);
                vec.setY(vec.getY()+yRand);
                vec.setX(vec.getX()+xRand);
                vec.setZ(vec.getZ()+zRand);

                Snowball ball4 = p.getWorld().spawn(p.getEyeLocation(), Snowball.class);
                ball4.setShooter(p);
                ball4.setVelocity(vec.setY(vec.getY()-0.3));

                yRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
                xRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
                zRand = -0.1 + (0.1 - (-0.1)) * new Random().nextDouble();
                vec = p.getLocation().getDirection().multiply(4);
                vec.setY(vec.getY()+yRand);
                vec.setX(vec.getX()+xRand);
                vec.setZ(vec.getZ()+zRand);

                Snowball ball5 = p.getWorld().spawn(p.getEyeLocation(), Snowball.class);
                ball5.setShooter(p);
                ball5.setVelocity(vec.setY(vec.getY()-0.3));
                getServer().getWorld("dev").playSound(p.getLocation(), Sound.ENDERDRAGON_HIT, 1, 1);

                p.setExp(0F);
            }
        }




        //       =====Laser=====

        if (gamePlayers.contains(e.getPlayer()) && e.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals(GOLD + "Laser") && (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)) 
        {

            if(p.getExp() < 1F) {
                if (cooldowns.contains(p.getDisplayName())) {
                    e.getPlayer().sendMessage(RED + "You can't shoot while changing batteries!");
                    return;
                }

                e.setCancelled(true);
                BukkitTask task = new Cooldown(p).runTaskTimer(this, 2, 1);
                cooldowns.add(p.getDisplayName());

            }else{
                p.setExp(0F);

                if (p.getLocation().getY() < -2) { return; }

                final List<Block> blocks = p.getLineOfSight(null, 75);
                blocks.remove(0);
                int LaserLength = blocks.size();

                HashMap<Location, String> pLoc = new HashMap<Location, String>();

                for (Player ply : gamePlayers) 
                {

                    if (ply != p) 
                    {
                        pLoc.put(new Location(ply.getWorld(), ply.getLocation().getBlockX(), ply.getLocation().getBlockY(), ply.getLocation().getBlockZ()), ply.getName());
                    }

                }

                for (int i = LaserLength; i > 0 ; i--)
                {
                    if (blocks.get((i-1)).getTypeId() == 0)
                    {
                        blocks.get((i-1)).setTypeId(55);

                        if (pLoc.containsKey(blocks.get((i-1)).getLocation()))
                        {
                            String PlayerToKill = pLoc.get(blocks.get((i-1)).getLocation());

                            if ( (redTeam.contains(p) && blueTeam.contains(getServer().getPlayer(PlayerToKill))) || (blueTeam.contains(p) && redTeam.contains(getServer().getPlayer(PlayerToKill))) ) 
                            {
                                getServer().getPlayer(PlayerToKill).setHealth(0);

                                addKill(p, PlayerToKill, "Laser");

                            }

                        }
                        else if (pLoc.containsKey(  new Location(blocks.get((i-1)).getWorld(), blocks.get((i-1)).getX(), blocks.get((i-1)).getY() - 1, blocks.get((i-1)).getZ())  ))
                        {
                            String PlayerToKill = pLoc.get(new Location(blocks.get((i-1)).getWorld(), blocks.get((i-1)).getX(), blocks.get((i-1)).getY() - 1, blocks.get((i-1)).getZ()));

                            if ( ((redTeam.contains(p) && blueTeam.contains(getServer().getPlayer(PlayerToKill))) || (blueTeam.contains(p) && redTeam.contains(getServer().getPlayer(PlayerToKill)))) && !getServer().getPlayer(PlayerToKill).isDead() ) 
                            {
                                getServer().getPlayer(PlayerToKill).setHealth(0);

                                addKill(p, PlayerToKill, "Laser");
                            }

                        }
                    }
                    else 
                    {
                        blocks.remove(i-1);
                    }

                }

                getServer().getWorld("dev").playSound(p.getLocation(), Sound.WITHER_HURT, 1, 1);


                getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
                {
                    public void run()
                    {
                        for (Block b : blocks)
                        {
                            b.setTypeId(0);
                        }
                    }
                }, 10L);
            }
        }

    }


    public Location redSpawn()
    {
        Random random = new Random();
        int zmin = 416;
        int zmax = 434;
        int xmin = 305;
        int xmax = 314;

        double finalZ = (random.nextInt(zmax - zmin) + zmin) + 0.50;
        double finalX = (random.nextInt(xmax - xmin) + xmin) + 0.50;

        Location redSpawn = new Location(getServer().getWorld("dev"), finalX, 64, finalZ);
        redSpawn.setPitch(12);
        redSpawn.setYaw(-90);

        return redSpawn;
    }

    public Location blueSpawn()
    {
        Random random = new Random();
        int zmin = 416;
        int zmax = 434;
        int xmin = 315;
        int xmax = 325;

        double finalZ = (random.nextInt(zmax - zmin) + zmin) + 0.50;
        double finalX = (random.nextInt(xmax - xmin) + xmin) + 0.50;

        Location blueSpawn = new Location(getServer().getWorld("dev"), finalX, 64, finalZ);

        blueSpawn.setPitch(9);
        blueSpawn.setYaw(90);

        return blueSpawn;
    }

    public Location blueReSpawn()
    {
        Location blueReSpawn = new Location(getServer().getWorld("dev"), 325, 66, 425);
        blueReSpawn.setPitch(10);
        blueReSpawn.setYaw(90);

        return blueReSpawn;
    }
      
    public Location redReSpawn()
    {
        Location redReSpawn = new Location(getServer().getWorld("dev"), 305, 66, 425);
        redReSpawn.setPitch(10);
        redReSpawn.setYaw(-90);

        return redReSpawn;
    }

    public void spawnBonusWeapon() 
    {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            public void run() 
            {
                World world = getServer().getWorld("dev");

                world.dropItem(new Location(getServer().getWorld("dev"), 315, 64, 425), Rocket);
                world.dropItem(new Location(getServer().getWorld("dev"), 315, 64, 425), Launcher);
            }

        }, 600L, 1200L);
    }



    public void addKill(Player p, String target, String arms) 
    {
        if (redTeam.contains(p))
        {
            getServer().broadcastMessage(DARK_RED + p.getName() + DARK_GRAY + " has shot " + AQUA + target + DARK_GRAY + " with a " + GOLD + arms);
            redScore++; 
        }
        if (blueTeam.contains(p)) 
        {
            getServer().broadcastMessage(AQUA + p.getName() + DARK_GRAY + " has shot " + DARK_RED + target + DARK_GRAY + " with a " + GOLD + arms);
            blueScore++; 
        }

        for (Player pl : getServer().getOnlinePlayers())
        {
            pl.playSound(pl.getLocation(), Sound.GHAST_DEATH, 1, 1);
        }
    }
}