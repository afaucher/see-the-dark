package com.beanfarmergames.seethedark.mode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.beanfarmergames.seethedark.entities.NavPoint;
import com.beanfarmergames.seethedark.game.Player;
import com.beanfarmergames.seethedark.game.RenderLayer;
import com.beanfarmergames.seethedark.game.SeeTheDark;
import com.beanfarmergames.seethedark.game.field.Field;
import com.beanfarmergames.seethedark.ship.Ship;
import com.beanfarmergames.seethedark.style.ColorPalate;
import com.beanfarmergames.seethedark.style.FontPalate;

public class RaceGameMode extends AbstractGameMode {

    private List<NavPoint> track;
    private int laps = 2;
    private SeeTheDark game;

    // TODO: Will leak when destroyed
    private SpriteBatch spriteBatch = new SpriteBatch();
    private Map<Player, NextPoint> racePositions = new HashMap<Player, NextPoint>();

    class NextPoint {
        private int nextPointIndex = 0;
        // Complete when equal to the race laps
        private int lap = 0;

        public int getNextPoint() {
            return nextPointIndex;
        }

        public int getLap() {
            return lap;
        }

        public void increment(int pointsPerLap) {
            nextPointIndex++;
            if (nextPointIndex == pointsPerLap) {
                nextPointIndex = 0;
                lap++;
            }
        }
    }

    @Override
    public void setGameState(State newState) {
        super.setGameState(newState);

        if (State.Playing.equals(newState)) {
            restartRace();
        }
    }

    public RaceGameMode(SeeTheDark game, Field field, List<NavPoint> track) {
        super(field);
        this.game = game;
        this.track = track;
    }

    @Override
    public void updateCallback(float seconds) {
        State state = getGameState();
        if (State.Playing.equals(state)) {
            boolean gameOver = false;
            for (Ship s : getField().getShips()) {
                Player player = game.getPlayerForShip(s);
                if (player == null) {
                    continue;
                }

                NextPoint nextPoint = racePositions.get(player);

                if (nextPoint.getLap() == laps) {
                    // Ship is already done
                    continue;
                }

                Vector2 shipLocation = s.getPosition();
                NavPoint nextNavPoint = track.get(nextPoint.getNextPoint());
                Vector2 nextNavLocation = nextNavPoint.getLocation();

                float distance = shipLocation.dst(nextNavLocation);
                if (distance > nextNavPoint.getRadius()) {
                    continue;
                }

                nextPoint.increment(track.size());

                if (nextPoint.getLap() == laps) {
                    gameOver = true;
                }
            }
            if (gameOver) {
                setGameState(State.GameOver);
            }
        }
    }

    public void restartRace() {
        racePositions.clear();

        for (Player player : game.getPlayers()) {
            racePositions.put(player, new NextPoint());
        }
    }

    @Override
    public Mode getGameMode() {
        return Mode.Race;
    }

    private int getPlayerScore(Player player) {
        NextPoint nextPoint = racePositions.get(player);

        return track.size() * nextPoint.lap + nextPoint.nextPointIndex;
    }

    @Override
    public void render(ShapeRenderer renderer, RenderLayer layer, Player player) {
        if (!RenderLayer.SCORES.equals(layer)) {
            return;
        }

        State state = getGameState();
        if (State.GameOver.equals(state)) {
            spriteBatch.begin();
            int x = 100;
            int y = 500;
            final float lineMargin = 1.2f;
            TextBounds lastLine = FontPalate.HUD_FONT.draw(spriteBatch, "Game Over, F2 to start over", x, y);
            y -= lastLine.height * lineMargin;

            for (Player p : game.getPlayers()) {
                boolean drawingForPlayer = p == player;
                String prefix = drawingForPlayer ? " === " : "";
                String suffix = prefix;
                String score = Integer.toString(getPlayerScore(player));

                lastLine = FontPalate.HUD_FONT.draw(spriteBatch, prefix + p.getName() + " " + score + suffix, x, y);
                y -= lastLine.height * lineMargin;
            }

            spriteBatch.end();
        } else if (State.Playing.equals(state)) {
            NextPoint nextPoint = racePositions.get(player);
            if (nextPoint == null) {
                return;
            }

            spriteBatch.begin();

            int x = 100;
            int y = 150;

            String status = String.format("Point %d/%d, Lap %d/%d", nextPoint.getNextPoint() + 1, track.size(),
                    nextPoint.getLap() + 1, laps);
            spriteBatch.setColor(ColorPalate.HUD_TEXT);
            FontPalate.HUD_FONT.draw(spriteBatch, status, x, y);

            spriteBatch.end();
        }
    }

}
