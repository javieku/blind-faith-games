/*******************************************************************************
 * Blind Faith Games is a research project of the e-UCM
 *           research group, developed by Gloria Pozuelo and Javier Álvarez, 
 *           under supervision by Baltasar Fernández-Manjón and Javier Torrente.
 *    
 *     Copyright 2011-2012 e-UCM research group.
 *   
 *      e-UCM is a research group of the Department of Software Engineering
 *           and Artificial Intelligence at the Complutense University of Madrid
 *           (School of Computer Science).
 *   
 *           C Profesor Jose Garcia Santesmases sn,
 *           28040 Madrid (Madrid), Spain.
 *   
 *           For more info please visit:  <http://blind-faith-games.e-ucm.es> or
 *           <http://www.e-ucm.es>
 *   
 *   ****************************************************************************
 * 	  This file is part of ZARODNIK GAME, developed in the Blind Faith Games project.
 *  
 *       ZARODNIK GAME, is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU Lesser General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *   
 *       ZARODNIK GAME is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Lesser General Public License for more details.
 *   
 *       You should have received a copy of the GNU Lesser General Public License
 *       along with Adventure.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package es.eucm.blindfaithgames.zarodnik.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import es.eucm.blindfaithgames.bfgtoolkit.general.Entity;
import es.eucm.blindfaithgames.bfgtoolkit.general.GameState;
import es.eucm.blindfaithgames.bfgtoolkit.general.Mask;
import es.eucm.blindfaithgames.bfgtoolkit.general.MaskCircle;
import es.eucm.blindfaithgames.bfgtoolkit.graphics.BitmapScaler;
import es.eucm.blindfaithgames.bfgtoolkit.graphics.SpriteMap;
import es.eucm.blindfaithgames.bfgtoolkit.input.Input;
import es.eucm.blindfaithgames.bfgtoolkit.input.Input.EventType;
import es.eucm.blindfaithgames.bfgtoolkit.others.RuntimeConfig;
import es.eucm.blindfaithgames.bfgtoolkit.sound.Music;
import es.eucm.blindfaithgames.bfgtoolkit.sound.Sound3DManager;
import es.eucm.blindfaithgames.zarodnik.R;
import es.eucm.blindfaithgames.zarodnik.game.ZarodnikGameplay.Sense;

/**
 * It represents the player
 * 
 * */

public class Player extends Entity{
	
	public static int PIXEL_PLAYER_RESIZE = 20;
	
	
	private enum State { EAT, MOVE, DIE, INVULNERABLE };
	private boolean inMovement;
	private boolean invulnerable;
	private State state;

	private static final int die_sound = R.raw.die;
	private static final int move_sound = R.raw.bubble;
	
	private static final float EPSILON = 8;

	private float destX;
	private float destY;
	private float speed;
	private float vx, vy;
	
	private float dotCenterX, initialX;
	private float dotCenterY, initialY;
	
	private ScoreBoard scoreBoard;
	
	private float incr;
	private Sense direction;
	private int incNo;
	
	// The player size express in dps
	private static float SIZE_DP;
	

	/**
	 * It creates the entity scoreboard to refresh its content and uses the vibrator service.
	 * 
	 * */
	public Player(int x, int y, int record, Bitmap img, GameState game, List<Mask> mask,
			SpriteMap animations, String soundName, Point soundOffset) {
		super(x, y, img, game, mask, animations, soundName, soundOffset, true);

		this.gameState = (ZarodnikGameplay) game;
		
		initMovementParameters();
		inMovement = false;
		state = State.MOVE;
		
		if(animations != null)
			animations.playAnim("up", RuntimeConfig.FRAMES_PER_STEP, true);
		
		SIZE_DP = 200;
		
		incNo = 1;
		
		scoreBoard = new ScoreBoard(ZarodnikGameplay.SCREEN_WIDTH - 200, 20, record, null, game, null, null, null, null);
		this.gameState.addEntity(scoreBoard);
	}
	
	public int getIncNo(){
		return incNo;
	}
	
	public void setInvulnerable(boolean invulnerable){
		this.invulnerable = invulnerable;
		if(invulnerable){
			this.state = State.INVULNERABLE;
		}
		else{
			this.state = State.MOVE;
		}
	}
	
	public void initMovementParameters(){
		speed = 0.05f;
		incr = 0.05f;
		vx = 0;
		vy = 0;
		initialX = this.x;
		initialY = this.y;
		dotCenterX = this.x + this.getImgWidth()/2;
		dotCenterY = this.y + this.getImgHeight()/2;
	}
	
	public boolean isInMovement() {
		return inMovement;
	}

	public void setInMovement(boolean inMovement) {
		this.inMovement = inMovement;
	}
	
	/**
	 * Use a parametric equation to generate the points of the dot's trajectory
	 * */
	@Override
	public void onUpdate() {	
		onMoveManagement();
		
		onEat();
		
		Sound3DManager.getSoundManager(this.gameState.getContext()).setListenerPosition(x, y, 0f);
		
		super.onUpdate();
	}
	
	private void onEat() {
		if(state == State.EAT){
			switch (direction) {
				case UP: this.playAnim("eatU", 5, false);
					break;
				case DOWN: this.playAnim("eatD", 5, false);
					break;
				case RIGHT: this.playAnim("eatR", 5, false);
					break;
				case LEFT: this.playAnim("eatL", 5, false);
					break;
			}
		}
	}

	private void onMoveManagement() {
		double auxX,auxY;
		EventType e  = Input.getInput().removeEvent("onDrag"); 
		
		if (e != null && state != State.DIE){
			if(e.getMotionEventE1().getY() > scoreBoard.getHeight()){
				initMovementParameters();
				
				vx = e.getMotionEventE1().getX() - dotCenterX;
				vy = e.getMotionEventE1().getY() - dotCenterY;
				
				destX = e.getMotionEventE1().getX();
				
				destY = e.getMotionEventE1().getY();
				
				inMovement = true;
				if(!Music.getInstanceMusic().isPlaying(move_sound)){
					Music.getInstanceMusic().play(this.gameState.getContext(), move_sound, true);
				}
			}
		}
		
		if (inMovement){
     		auxX = (initialX + vx * speed);
     		auxY = (initialY + vy * speed);
     		
			// We calculate the player direction
     		calculateDirection();
     		
     		speed += incr;
     		
			if (!inDestination(dotCenterX,dotCenterY)){
				this.x = (int) auxX;
				this.y = (int) auxY;
				dotCenterX = this.x + this.getImgWidth()/2;
				dotCenterY = this.y + this.getImgHeight()/2;
				inMovement = true;
			}
			else{
				inMovement = false;
				Music.getInstanceMusic().stop(move_sound);
			}
		} 
	}
	
	private void calculateDirection() {
		if (Math.abs(initialX - destX) > Math.abs(initialY - destY)){
			if (destX < initialX){
				direction = Sense.LEFT;
				this.playAnim("left", RuntimeConfig.FRAMES_PER_STEP, true);
			}else{
				direction = Sense.RIGHT;
				this.playAnim("right", RuntimeConfig.FRAMES_PER_STEP, true);
				}
		}
		else{
			if (destY < initialY){
				direction = Sense.UP;
				this.playAnim("up", RuntimeConfig.FRAMES_PER_STEP, true);
			}else{
				direction = Sense.DOWN;	
				this.playAnim("down", RuntimeConfig.FRAMES_PER_STEP, true);
			}
		}
	}

	private boolean inDestination(double auxX, double auxY) {
		if(auxX < (destX + EPSILON) && auxX >= (destX - EPSILON) && auxY < (destY + EPSILON) && auxY >= (destY - EPSILON))
			return true;
		else
			return false;
	}

	@SuppressWarnings("unused")
	private boolean inStage(double d, double e) {
		if(d < ZarodnikGameplay.SCREEN_WIDTH && d >= 0 && e < ZarodnikGameplay.SCREEN_HEIGHT && e >= 0)
			return true;
		else
			return false;
	}

	/**
	 * It calls father's onDraw and 
	 * 
	 * @param canvas surface which will be drawn
	 * 
	 * */
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);
		if(RuntimeConfig.IS_DEBUG_MODE){
			canvas.drawRect(destX-EPSILON, destY-EPSILON, destX+EPSILON, destY+EPSILON, new Paint());
			canvas.drawLine(dotCenterX, dotCenterY,destX, destY, new Paint());
		}
	}

	@Override
	public void onCollision(Entity e) {
		// Predator and prey collides
		if (e instanceof Predator && this.state != State.INVULNERABLE){
			onDie();
		}
		else if (e instanceof SmartPrey || e instanceof SillyPrey){
			inMovement = false;
			destX = x;
			destY = y;
			this.resize(PIXEL_PLAYER_RESIZE, true);
			
			state = State.EAT;
			Music.getInstanceMusic().playWithBlock(this.gameState.getContext(), R.raw.apple_bite, false);
			this.gameState.getTTS().speak(gameState.getContext().getString(R.string.size_inc));
			onEat();
			this.setTimer(1, RuntimeConfig.FRAMES_PER_STEP);
		
			((ZarodnikGameplay)(this.gameState)).decrementPrey();
			 
			((Creature) e).onDie();
	
			scoreBoard.incrementCounter();
		}else if (e instanceof Capsule || e instanceof ChainFish || e instanceof Radio){
			inMovement = false;
			destX = x;
			destY = y;
			
			e.setCollidable(false);
			
			state = State.EAT;
			Music.getInstanceMusic().playWithBlock(this.gameState.getContext(), R.raw.apple_bite, false);
			
			onEat();
			this.setTimer(1, RuntimeConfig.FRAMES_PER_STEP);
		}
	}
	

	public void onDie() {
		inMovement = false;
		destX = x;
		destY = y;
		
		this.playAnim("die", RuntimeConfig.FRAMES_PER_STEP, false);
		state = State.DIE;
		this.setTimer(0,RuntimeConfig.FRAMES_PER_STEP*4);
		this.setCollidable(false);
	}

	public void resize(int sizeInc, boolean music) {
		Bitmap img;
		List<Mask> maskList;
		int imgW, imgH, frameW, frameH;
		ArrayList<Integer> aux;
		
		if(music)
			Music.getInstanceMusic().play(this.gameState.getContext(),R.raw.appear, false);
		
		img = this.getImg();
		img.recycle();
	
		BitmapScaler scaler;
		
		// We increments 50 pixels
		SIZE_DP += sizeInc;
		
		incNo++;
		
		// Convert the dps to pixels, based on density scale
		int size = (int) (SIZE_DP * GameState.scale);
        try {
            scaler = new BitmapScaler(this.gameState.getContext().getResources(), R.drawable.playersheetx, size);
            img = scaler.getScaled();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

		this.setImg(img);
		
		/*-------- Animations --------------------------------------*/
		SpriteMap animations = new SpriteMap(8, 3, img, 0);
		aux = new ArrayList<Integer>();
		aux.add(0);
		aux.add(1);
		aux.add(2);
		aux.add(1);
		aux.add(0);
		animations.addAnim("left", aux, RuntimeConfig.FRAMES_PER_STEP, true);
		
		aux = new ArrayList<Integer>();
		aux.add(0);
		aux.add(3);
		aux.add(4);
		aux.add(5);
		aux.add(4);
		aux.add(3);
		aux.add(0);
		animations.addAnim("eatL", aux, RuntimeConfig.FRAMES_PER_STEP, false);

		aux = new ArrayList<Integer>();
		aux.add(18);
		aux.add(19);
		aux.add(20);
		aux.add(19);
		aux.add(18);
		animations.addAnim("right", aux, RuntimeConfig.FRAMES_PER_STEP, true);
		
		aux = new ArrayList<Integer>();
		aux.add(18);
		aux.add(21);
		aux.add(22);
		aux.add(23);
		aux.add(22);
		aux.add(21);
		aux.add(18);
		animations.addAnim("eatR", aux, RuntimeConfig.FRAMES_PER_STEP, false);
		
		aux = new ArrayList<Integer>();
		aux.add(6);
		aux.add(7);
		animations.addAnim("up", aux, RuntimeConfig.FRAMES_PER_STEP, true);
		
		aux = new ArrayList<Integer>();
		aux.add(6);
		aux.add(8);
		aux.add(7);
		aux.add(6);
		animations.addAnim("eatU", aux, RuntimeConfig.FRAMES_PER_STEP, false);
		
		aux = new ArrayList<Integer>();
		aux.add(9);
		aux.add(10);
		aux.add(11);
		aux.add(10);
		aux.add(9);
		animations.addAnim("down", aux, RuntimeConfig.FRAMES_PER_STEP, false);
		
		aux = new ArrayList<Integer>();
		aux.add(9);
		aux.add(12);
		aux.add(13);
		aux.add(14);
		aux.add(13);
		aux.add(12);
		aux.add(9);
		animations.addAnim("eatD", aux, RuntimeConfig.FRAMES_PER_STEP, false);
		
		aux = new ArrayList<Integer>();
		aux.add(15);
		aux.add(16);
		aux.add(17);
		animations.addAnim("die", aux, RuntimeConfig.FRAMES_PER_STEP, false);
		
		/*--------------------------------------------------*/
		
		this.setSpriteMap(animations);
		
		imgW = img.getWidth();
		imgH = img.getHeight();
		frameW = imgW / 3;
		frameH = imgH / 8;
		maskList = new ArrayList<Mask>();
		maskList.add(new MaskCircle(frameW/2,frameH/2,frameW/3));
		this.setMask(maskList);
	}

	@Override
	public void onTimer(int timer) {
		if(timer == 0)
			this.remove();
		if(timer == 1){
			if(invulnerable)
				state = State.INVULNERABLE;
			else
				state = State.MOVE;
		}
	}

	@Override
	public void onInit() {}

	@Override
	public void onRemove() {
		Music.getInstanceMusic().playWithBlock(this.gameState.getContext(), die_sound, false);
		Music.getInstanceMusic().stop(move_sound);
		this.gameState.stop();
	}
	
	@Override
	public void onSavedInstance(Bundle outState, int i, int j) {
		outState.putFloat("playerSize", SIZE_DP);
		super.onSavedInstance(outState, i, j);
	}
	
	@Override
	public void onRestoreInstance(Bundle savedInstanceState, int i, int j) {
		SIZE_DP = savedInstanceState.getFloat("playerSize", 200);
		resize(0, false);
		super.onRestoreInstance(savedInstanceState, i, j);
	}
}
