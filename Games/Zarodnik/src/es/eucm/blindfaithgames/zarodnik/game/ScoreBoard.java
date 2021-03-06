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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import es.eucm.blindfaithgames.bfgtoolkit.general.Entity;
import es.eucm.blindfaithgames.bfgtoolkit.general.GameState;
import es.eucm.blindfaithgames.bfgtoolkit.general.Mask;
import es.eucm.blindfaithgames.bfgtoolkit.graphics.SpriteMap;
import es.eucm.blindfaithgames.bfgtoolkit.input.Input;
import es.eucm.blindfaithgames.bfgtoolkit.input.Input.EventType;
import es.eucm.blindfaithgames.bfgtoolkit.others.RuntimeConfig;
import es.eucm.blindfaithgames.zarodnik.R;
import es.eucm.blindfaithgames.zarodnik.activities.MainActivity;

public class ScoreBoard extends Entity {

	private ZarodnikGameplay game;
	
	private int counter;
	private int record;
	
	private float fontSize;
	private Typeface font;
	private float scale;
	private Paint brush;
	
	public ScoreBoard(int x, int y, int record,Bitmap img, GameState game, List<Mask> mask,
			SpriteMap animations, String soundName, Point soundOffset) {
		super(x, y, img, game, mask, animations, null, null, false);
		counter = 0;
		this.game = (ZarodnikGameplay) game;
		this.record = record;
		
		scale = this.game.getContext().getResources().getDisplayMetrics().density;
		fontSize = ((this.game.getContext().getResources().getDimensionPixelSize(R.dimen.font_size_menu))/scale);
		font = Typeface.createFromAsset(this.game.getContext().getAssets(),RuntimeConfig.FONT_PATH);
		
		brush = new Paint();
		brush.setTextSize(fontSize);
		if(font != null)
			brush.setTypeface(font);
	}

	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);
		
		brush.setARGB(255, 51, 51, 51);
		canvas.drawRect(new Rect(this.x - (ZarodnikGameplay.SCREEN_WIDTH - 200), this.y - 30, GameState.SCREEN_WIDTH, (int) (fontSize + 10)), brush);
		
		if(record >= 0){
			brush.setARGB(255, 51, 153, 255);
			canvas.drawText(Integer.toString(record), this.x, this.y, brush);
		}
		
		brush.setARGB(255, 0, 51, 101);
		canvas.drawText(Integer.toString(counter), this.x + fontSize*2, this.y, brush);
	}
	
	private void onTouch() {
		EventType lp = Input.getInput().getEvent("onLongPress");
		if(lp != null){
			double ex = lp.getMotionEventE1().getX();
			double ey = lp.getMotionEventE1().getY();
			if(this.x - (ZarodnikGameplay.SCREEN_WIDTH - 200) < ex && 
					this.y - 30 < ey && 
					ex < GameState.SCREEN_WIDTH && 
					ey < fontSize + 10){
				this.game.getTTS().speak(this.game.getContext().getResources().getString(R.string.recordSpeech)
						+ " " + record + ", " + this.game.getContext().getResources().getString(R.string.counterSpeech)
						+ " " + counter);
			}
		}
	}
	
	@Override
	public void onUpdate(){
		super.onUpdate();
		EventType e = Input.getInput().removeEvent("speakRecord");
		if(e != null){
			this.game.getTTS().speak(this.game.getContext().getResources().getString(R.string.recordSpeech)
					+ " " + record + ", " + this.game.getContext().getResources().getString(R.string.counterSpeech)
					+ " " + counter);
			
		}
		onTouch();
	}
	
	public void incrementCounter(){
		counter++;
		if(counter > record){
			record = counter;
			save();
			this.game.getTTS().speak(this.game.getContext().getResources().getString(R.string.newRecordSpeech) + " "+ counter);
		}
	}
	
	private void save() {
		FileOutputStream fos;
		try { 
			fos = this.game.getContext().openFileOutput(MainActivity.FILENAMEFREEMODE, Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(record); 
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void resetCounter(){
		game.getTTS().speak(game.getContext().getString(R.string.scoreboard_reset));
		counter = 0;
	}
	
	@Override
	public void onCollision(Entity e) {}

	@Override
	public void onTimer(int timer) {}

	@Override
	public void onInit() {}

	public int getRecord() {
		return record;
	}

	public int getCounter() {
		return counter;
	}
	
	public void incrementCounter(int i) {
		counter += i;
		game.getTTS().speak(game.getContext().getString(R.string.scoreboard_success) + " " + counter);
	}

	public void decrementCounter(int i) {
		counter -= i;
		game.getTTS().speak(game.getContext().getString(R.string.scoreboard_fail) + " " + counter);
	}

	@Override
	public void onRemove() {}

	public float getHeight() {
		float scale = this.gameState.getContext().getResources().getDisplayMetrics().density;
		float fontSize = ((this.gameState.getContext().getResources().getDimensionPixelSize(R.dimen.font_size_menu))/scale);
		return fontSize + 10;
	}
	
	@Override
	public void onSavedInstance(Bundle outState, int i, int j) {
		outState.putInt("scoreBoard record", record);
		outState.putInt("scoreBoard counter", counter);
		super.onSavedInstance(outState, i, j);
	}
	
	@Override
	public void onRestoreInstance(Bundle savedInstanceState, int i, int j) {
		record = savedInstanceState.getInt("scoreBoard record", 0);
		counter = savedInstanceState.getInt("scoreBoard counter", 0);
		super.onRestoreInstance(savedInstanceState, i, j);
	}

}
