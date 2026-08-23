package dev.elysium.item.mechanic;

import dev.elysium.item.ElysiumItem;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MarkManager {

    private final ElysiumItem plugin;
    
    public static class MarkData {
        public int stacks;
        public long expireTime;
        public MarkData(int stacks, long expireTime) {
            this.stacks = stacks;
            this.expireTime = expireTime;
        }
    }

    // Dùng ConcurrentHashMap để Thread-safe 100%
    private final Map<UUID, Map<String, MarkData>> entityMarks = new ConcurrentHashMap<>();

    public MarkManager(ElysiumItem plugin) {
        this.plugin = plugin;
        startTask();
    }

    public void addMark(LivingEntity target, String markType, int amount, int maxStacks, long durationMs, Player attacker) {
        if (target == null || target.isDead()) return;
        
        UUID id = target.getUniqueId();
        entityMarks.putIfAbsent(id, new HashMap<>());
        Map<String, MarkData> marks = entityMarks.get(id);
        
        MarkData data = marks.getOrDefault(markType, new MarkData(0, 0));
        data.stacks = Math.min(data.stacks + amount, maxStacks);
        data.expireTime = System.currentTimeMillis() + durationMs;
        marks.put(markType, data);
        
        if (attacker != null && markType.equals("BLOOD_MARK")) {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < maxStacks; i++) {
                if (i < data.stacks) sb.append("§c♦");
                else sb.append("§7♢");
            }
            attacker.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                new TextComponent("§c🩸 Huyết Ấn trên mục tiêu: " + sb.toString()));
        }
    }
    
    public int getStacks(LivingEntity target, String markType) {
        Map<String, MarkData> marks = entityMarks.get(target.getUniqueId());
        if (marks == null) return 0;
        MarkData data = marks.get(markType);
        if (data == null || System.currentTimeMillis() > data.expireTime) return 0;
        return data.stacks;
    }

    public void consumeMarks(LivingEntity target, String markType) {
        Map<String, MarkData> marks = entityMarks.get(target.getUniqueId());
        if (marks != null) {
            marks.remove(markType);
        }
    }

    private void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Iterator<Map.Entry<UUID, Map<String, MarkData>>> it = entityMarks.entrySet().iterator();
                
                while (it.hasNext()) {
                    Map.Entry<UUID, Map<String, MarkData>> entry = it.next();
                    UUID uuid = entry.getKey();
                    Map<String, MarkData> marks = entry.getValue();
                    
                    // 1. Tự động xóa ấn đã hết hạn (Tối ưu RAM)
                    marks.entrySet().removeIf(m -> now > m.getValue().expireTime);
                    
                    // 2. Nếu quái vật không còn ấn nào -> Dọn dẹp sạch sẽ khỏi RAM
                    if (marks.isEmpty()) {
                        it.remove();
                        continue;
                    }
                    
                    Entity entity = plugin.getServer().getEntity(uuid);
                    // 3. Nếu quái vật đã chết hoặc despawn -> Xóa khỏi RAM
                    if (!(entity instanceof LivingEntity) || entity.isDead()) {
                        it.remove();
                        continue; 
                    }
                    
                    if (marks.containsKey("BLOOD_MARK")) {
                        spawnBloodParticles((LivingEntity) entity, marks.get("BLOOD_MARK").stacks);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // 10 tick = 0.5s -> Cực kỳ nhẹ, không chạy mỗi tick
    }

    private void spawnBloodParticles(LivingEntity entity, int stacks) {
        Location loc = entity.getLocation().add(0, entity.getHeight() + 0.3, 0);
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.2F);
        
        double radius = 0.5 + (stacks * 0.1);
        for (int i = 0; i < stacks; i++) {
            double angle = (2 * Math.PI / stacks) * i;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            entity.getWorld().spawnParticle(Particle.DUST, loc.clone().add(x, 0, z), 1, dustOptions);
        }
    }
}