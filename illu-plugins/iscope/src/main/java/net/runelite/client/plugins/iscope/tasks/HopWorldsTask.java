package net.runelite.client.plugins.iscope.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.iscope.Task;
import net.runelite.client.plugins.iutils.ObjectUtils;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.http.api.worlds.WorldType;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.Set;

import static net.runelite.client.plugins.iutils.iUtils.sleep;

@Slf4j
public class HopWorldsTask extends Task {

    @Inject
    public iUtils utils;

    @Inject
    ObjectUtils objectUtils;

    @Inject
    Client client;


    @Inject
    private WorldService worldService;

    private net.runelite.api.World quickHopTargetWorld;
    private int displaySwitcherAttempts = 0;

    boolean clicked = false;
    int tickClicked = 0;

    int[] worldList = { 318, 319 };
    int index = 0;



    @Override
    public boolean validate() {
        return client.getWidget(229, 2) != null;
    }


    private void hop(int worldId)
    {
        assert client.isClientThread();

        WorldResult worldResult = worldService.getWorlds();
        // Don't try to hop if the world doesn't exist
        World world = worldResult.findWorld(worldId);
        if (world == null)
        {
            return;
        }

        final net.runelite.api.World rsWorld = client.createWorld();
        rsWorld.setActivity(world.getActivity());
        rsWorld.setAddress(world.getAddress());
        rsWorld.setId(world.getId());
        rsWorld.setPlayerCount(world.getPlayers());
        rsWorld.setLocation(world.getLocation());
        rsWorld.setTypes(WorldUtil.toWorldTypes(world.getTypes()));

        if (client.getGameState() == GameState.LOGIN_SCREEN)
        {
            // on the login screen we can just change the world by ourselves
            client.changeWorld(rsWorld);
            return;
        }

        quickHopTargetWorld = rsWorld;
        displaySwitcherAttempts = 0;
    }

    public void IpressKey(char key)
    {
        IkeyEvent(401, key);
        IkeyEvent(402, key);
        IkeyEvent(400, key);
    }

    private void IkeyEvent(int id, char key)
    {
        KeyEvent e = new KeyEvent(
                client.getCanvas(), id, System.currentTimeMillis(),
                0, KeyEvent.VK_UNDEFINED, key
        );

        client.getCanvas().dispatchEvent(e);
    }

    @Override
    public void onGameTick(GameTick event) {
        if (quickHopTargetWorld != null) {
            Widget dialog = client.getWidget(229, 2);
            if (dialog !=null) {
                Runnable runnable =
                        () -> {
                            KeyEvent keyPress = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, ' ');
                            this.client.getCanvas().dispatchEvent(keyPress);
                            KeyEvent keyRelease = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, ' ');
                            this.client.getCanvas().dispatchEvent(keyRelease);


                        };
                new Thread(runnable).start();
            }
            client.hopToWorld(quickHopTargetWorld);
            quickHopTargetWorld = null;
        }
        System.out.println("hello from hop task..");
        if (clicked && tickClicked >0) {
            clicked = false;
            tickClicked = 0;
        }
        if (clicked) {
            tickClicked += 1;
            return;
        }

        Widget dialog = client.getWidget(229, 2);
        if (dialog == null ) {
            System.out.println("checked and dialog is null");
            return;
        }
        if (dialog != null) {
            if(dialog.getText().equals("Click here to continue")) {
                if (index > worldList.length) {
//                    index = 0;
                    return;
                }
                hop(worldList[index]);
                index +=1;
                clicked = true;
            }
        }

    }

}