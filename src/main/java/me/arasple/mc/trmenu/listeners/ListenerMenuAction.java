package me.arasple.mc.trmenu.listeners;

import io.izzel.taboolib.module.inject.TListener;
import me.arasple.mc.trmenu.TrMenu;
import me.arasple.mc.trmenu.action.TrAction;
import me.arasple.mc.trmenu.action.base.AbstractAction;
import me.arasple.mc.trmenu.data.ArgsCache;
import me.arasple.mc.trmenu.display.Button;
import me.arasple.mc.trmenu.menu.Menu;
import me.arasple.mc.trmenu.menu.MenuHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author Arasple
 * @date 2019/10/4 14:02
 */
@TListener
public class ListenerMenuAction implements Listener {

    private HashMap<UUID, Long> clickTimes = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        long start = System.currentTimeMillis();

        if (!(e.getInventory().getHolder() instanceof MenuHolder)) {
            return;
        }

        Player p = (Player) e.getWhoClicked();
        Menu menu = ((MenuHolder) e.getInventory().getHolder()).getMenu();
        Button button = menu.getButton(p, e.getRawSlot());

        // 防刷屏点击
        clickTimes.putIfAbsent(p.getUniqueId(), 0L);
        if (System.currentTimeMillis() - clickTimes.get(p.getUniqueId()) < TrMenu.getSettings().getLong("OPTIONS.ANTI-CLICK-SPAM")) {
            e.setCancelled(true);
            return;
        } else {
            clickTimes.put(p.getUniqueId(), System.currentTimeMillis());
        }
        // 锁定玩家背包
        if (button == null) {
            if (e.getClickedInventory() == p.getInventory() && menu.isLockPlayerInv()) {
                e.setCancelled(true);
            }
            return;
        } else {
            e.setCancelled(true);
        }

        // 读取动作
        List<AbstractAction> actions = button.getIcon(p).getActions().getOrDefault(e.getClick(), new ArrayList<>());
        if (button.getIcon(p).getActions().get(null) != null) {
            actions.addAll(button.getIcon(p).getActions().get(null));
        }
        // 执行动作
        ArgsCache.getEvent().put(p.getUniqueId(), e);
        TrAction.runActions(actions.listIterator(), p);
        ArgsCache.getEvent().remove(p.getUniqueId());
        // 刷新图标优先级
        button.refreshConditionalIcon(p, e);

        if (p.hasMetadata("TrMenu-Debug")) {
            p.sendMessage("§8[§3Tr§bMenu§8]§8[§7DEBUG§8] §6InventoryClickEvent Took §e" + (System.currentTimeMillis() - start) + "ms§6.");
        }
    }

}
