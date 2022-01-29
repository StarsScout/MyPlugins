package net.runelite.client.plugins.iscope.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.plugins.iscope.Task;
import net.runelite.client.plugins.iutils.ObjectUtils;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.client.game.WorldService;

import javax.inject.Inject;
import java.util.Set;

@Slf4j
public class ClickTelescopeTask extends Task {

    @Inject
    public iUtils utils;

    @Inject
    ObjectUtils objectUtils;

    @Inject
    Client client;

    boolean clicked = false;
    int tickClicked = 0;

    private static Set<Integer> portalIDs = Set.of(13658, 12344565);

    private GameObject portal= null;

    @Override
    public boolean validate() {
        return (objectUtils.findNearestGameObjectWithin(client.getLocalPlayer().getLocalLocation(), 30, portalIDs) != null) && (client.getWidget(229, 2) == null);
    }

    @Override
    public void onGameTick(GameTick event) {
        System.out.println("hello from telescope task..");

        if (clicked && tickClicked > 3) {
            clicked = false;
            tickClicked = 0;
        }
        if (clicked) {
            tickClicked += 1;
            return;
        }
        portal = objectUtils.findNearestGameObjectWithin(client.getLocalPlayer().getLocalLocation(), 30, portalIDs);
        if (portal != null)
        {
            tickClicked = 0;
            utils.doGameObjectActionGameTick(portal, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),3);
            clicked = true;
        }

    }

}