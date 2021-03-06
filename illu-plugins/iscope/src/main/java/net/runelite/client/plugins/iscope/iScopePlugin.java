/*
 * Copyright (c) 2018, SomeoneWithAnInternetConnection
 * Copyright (c) 2018, oplosthee <https://github.com/oplosthee>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.iscope;

import com.google.inject.Injector;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iscope.tasks.ClickTelescopeTask;
import net.runelite.client.plugins.iscope.tasks.EnterHouseTask;
import net.runelite.client.plugins.iscope.tasks.HopWorldsTask;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.plugins.iutils.scripts.ReflectBreakHandler;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "iScope",
        enabledByDefault = false,
        description = "Auto Telescope plugin",
        tags = {"Telescope"}
)
@Slf4j
public class iScopePlugin extends Plugin {
    @Inject
    private Injector injector;

    @Inject
    private Client client;

    @Inject
    private iScopeConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private iUtils utils;

    @Inject
    public ReflectBreakHandler chinBreakHandler;

    @Inject
    private ConfigManager configManager;

    private TaskSet tasks = new TaskSet();
    public static LocalPoint beforeLoc = new LocalPoint(0, 0);

    MenuEntry targetMenu;
    Instant botTimer;
    Player player;

    public static boolean startBot;
    public static long sleepLength;
    public static int tickLength;
    public static int timeout;
    public static String status = "starting...";

    @Provides
    iScopeConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(iScopeConfig.class);
    }

    @Override
    protected void startUp() {
//        chinBreakHandler.registerPlugin(this);
    }

    @Override
    protected void shutDown() {
        resetVals();
//        chinBreakHandler.unregisterPlugin(this);
    }


    private void loadTasks() {
        tasks.clear();
        tasks.addAll(
                injector.getInstance(EnterHouseTask.class),
//                injector.getInstance(ClickTelescopeTask.class),
                injector.getInstance(HopWorldsTask.class)
//                injector.getInstance(MenuTask.class)
//                injector.getInstance(MovingTask.class),
//                injector.getInstance(CleanHerbTask.class),

        );
    }

    public void resetVals() {
        log.debug("stopping iScope plugin");
        startBot = false;
        botTimer = null;
        tasks.clear();
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("iScope")) {
            return;
        }
        log.debug("button {} pressed!", configButtonClicked.getKey());
        if (configButtonClicked.getKey().equals("startButton")) {
            if (!startBot) {
                Player player = client.getLocalPlayer();
                if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
                    log.info("starting iScope plugin");
                    loadTasks();
                    startBot = true;
                    timeout = 0;
                    targetMenu = null;
                    botTimer = Instant.now();
                    beforeLoc = client.getLocalPlayer().getLocalLocation();
                } else {
                    log.info("Start logged in");
                }
            } else {
                resetVals();
            }
        }
    }

    public void updateStats() {
        //templatePH = (int) getPerHour(totalBraceletCount);
        //coinsPH = (int) getPerHour(totalCoins - ((totalCoins / BRACELET_HA_VAL) * (unchargedBraceletCost + revEtherCost + natureRuneCost)));
    }

    public long getPerHour(int quantity) {
        Duration timeSinceStart = Duration.between(botTimer, Instant.now());
        if (!timeSinceStart.isZero()) {
            return (int) ((double) quantity * (double) Duration.ofHours(1).toMillis() / (double) timeSinceStart.toMillis());
        }
        return 0;
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!startBot) {
            return;
        }
        player = client.getLocalPlayer();
        if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
            Task task = tasks.getValidTask();

            if (task != null) {
                status = task.getTaskDescription();
                task.onGameTick(event);
            } else {
                status = "Task not found";
                log.debug(status);
            }
            beforeLoc = player.getLocalLocation();
        }
    }
}