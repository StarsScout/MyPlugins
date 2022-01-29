package net.runelite.client.plugins.iscope.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.iscope.Task;
import net.runelite.client.plugins.iutils.ActionQueue;
import net.runelite.client.plugins.iutils.LegacyMenuEntry;
import net.runelite.client.plugins.iutils.ObjectUtils;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.plugins.iutils.scene.ObjectCategory;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Set;

@Slf4j
public class EnterHouseTask extends Task {

    @Inject
    public iUtils utils;

    @Inject
    ObjectUtils objectUtils;

    @Inject
    Client client;

    boolean clicked = false;
    int tickClicked = 0;

    private static Set<Integer> portalIDs = Set.of(15478, 15482);

    private GameObject portal= null;


    @Override
    public boolean validate() {
        return objectUtils.findNearestGameObjectWithin(client.getLocalPlayer().getLocalLocation(), 12, portalIDs) != null;
    }

    @Override
    public void onGameTick(GameTick event) {
        System.out.println("hello from house task..");
        if (clicked && tickClicked > 1) {
            clicked = false;
            tickClicked = 0;
        }
        if (clicked) {
            tickClicked += 1;
            return;
        }
        portal = objectUtils.findNearestGameObjectWithin(client.getLocalPlayer().getLocalLocation(), 12, portalIDs);
        if (portal != null)
        {
            tickClicked = 0;
            utils.doGameObjectActionGameTick(portal, MenuAction.GAME_OBJECT_SECOND_OPTION.getId(),0);
            clicked = true;
        }

    }

}