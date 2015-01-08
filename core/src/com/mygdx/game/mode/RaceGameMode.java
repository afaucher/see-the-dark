package com.mygdx.game.mode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.RenderLayer;
import com.mygdx.game.entities.NavPoint;
import com.mygdx.game.field.Field;
import com.mygdx.game.ship.Ship;
import com.mygdx.game.style.ColorPalate;
import com.mygdx.game.style.FontPalate;

public class RaceGameMode extends AbstractGameMode {

    private List<NavPoint> track;
    private int laps = 1;
    
    class NextPoint {
        private int nextPointIndex = 0;
        //Complete when equal to the race laps
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
    
    private Map<Ship, NextPoint> racePositions = new HashMap<Ship, NextPoint>();

    public RaceGameMode(Field field, List<NavPoint> track) {
        super(field);
        this.track = track;
        
        restartRace();
    }

    @Override
    public void updateCallback(float seconds) {
        State state = getGameState();
        if (State.Playing.equals(state)) {
            boolean gameOver = false;
            for (Ship s : getField().getShips()) {
                NextPoint nextPoint = racePositions.get(s);
                
                if (nextPoint.getLap() == laps) {
                    //Ship is already done
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
        
        for (Ship s : getField().getShips()) {
            racePositions.put(s, new NextPoint());
            //TODO: Move ships
        }
    }
    
    @Override
    public Mode getGameMode() {
        return Mode.Race;
    }

    @Override
    public boolean isShipWinner(Ship s) {
        NextPoint nextPoint = racePositions.get(s);
        
        return (nextPoint.getLap() == laps);
    }

    private int getShipScore(Ship s) {
        NextPoint nextPoint = racePositions.get(s);
        
        return track.size() * nextPoint.lap + nextPoint.nextPointIndex;
    }

    @Override
    public void render(ShapeRenderer renderer, RenderLayer layer) {
        if (!RenderLayer.SCORES.equals(layer)) {
            return;
        }

        State state = getGameState();
        if (State.GameOver.equals(state)) {
            SpriteBatch spriteBatch = null;
            spriteBatch = new SpriteBatch();

            spriteBatch.begin();
            int x = 100;
            int y = 500;
            final float lineMargin = 1.2f;
            TextBounds lastLine = FontPalate.HUD_FONT.draw(spriteBatch, "Game Over, F2 to start over", x, y);
            y -= lastLine.height * lineMargin;
            
            for (Ship s : getField().getShips()) {
                lastLine = FontPalate.HUD_FONT.draw(spriteBatch, "Ship " + getShipScore(s), x, y);
                y -= lastLine.height * lineMargin;
            }
            
            spriteBatch.end();
        } else if (State.Playing.equals(state)) {
        }
    }

}
