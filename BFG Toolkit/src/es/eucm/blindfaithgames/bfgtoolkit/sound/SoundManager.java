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
 * 	  This file is part of BFG TOOLKIT, developed in the Blind Faith Games project.
 *  
 *       BFG TOOLKIT, is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU Lesser General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *   
 *       BFG TOOLKIT is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Lesser General Public License for more details.
 *   
 *       You should have received a copy of the GNU Lesser General Public License
 *       along with Adventure.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package es.eucm.blindfaithgames.bfgtoolkit.sound;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

/**
 *  This class provides methods to load sounds based in a SoundPool object.
 *  
 *  @author Gloria Pozuelo & Javier Álvarez.
 * 
 * */

public class SoundManager {
	private SoundPool soundPool; // the object that manages 
	private int soundID; // sound id that will be played
	private static HashMap<Integer, Integer> soundMap; // links a id sound with a pool id
	boolean loaded = false; // is a sound loaded in the pool?
	private static Context c; // Activity where the class is working
	private AudioManager audioManager; // 
	private float volume;

	/**
	 * Unique constructor of the class.
	 * 
	 * @param context Activity where the class is working.
	 * */
	public SoundManager(Context context){
		// Load the sound
		c = context;
		if (soundPool !=null) soundPool.release();
		soundPool = new SoundPool(9, AudioManager.STREAM_MUSIC, 0);
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
						int status) {
					loaded = true;
				}
			});
		// Getting the user sound settings
		audioManager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
		float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		volume = actualVolume / maxVolume;
		
		soundMap = new HashMap<Integer,Integer>();
	}
// ----------------------------------------------------------- Others -----------------------------------------------------------	
	/**
	 *  Adds a new sound to the pool.
	 *  
	 *  @param index position in the map 
	 *  @param soundID sound id in the application environment.
	 *  @param mCon Activity where the sound is played.
	 * */
	public void addSound(int index, int SoundID, Context mCon)
	{
	    int id = soundPool.load(mCon, SoundID, index);
		soundMap.put(index, id);
	}
	
	/**
	 * Changes a sound configuration.
	 * 
	 * @param sound sound id in the application environment.
	 * @param loop if true, the sound will loop.
	 * @param rate to change the pitch of sound.
	 * @param side 
	 * 
	 * */
	public void setPlay(int sound, int loop, float rate, int side){
		soundPool.setLoop(sound, loop);
		soundPool.setPriority(sound, 1);
		soundPool.setRate(sound, rate);
		if (side==1)soundPool.setVolume(sound, (float)0.0, volume);
		else if(side==2)soundPool.setVolume(sound, (float)0.0, volume);
		else soundPool.setVolume(sound, volume, volume);
	}
	
	/**
	 * Plays a sound previously loaded.
	 * 
	 * @param sound the sound that will be played
	 * */
	public void play(int sound){
		stopSound(0);stopSound(1);
		// Is the sound loaded already?
		if (loaded) {
			soundID=soundMap.get(sound);
			soundPool.play(soundID, volume, volume, 1, 0, 1f);
			}
	}
	
	/**
	 * 
	 * Plays a sound previously loaded.
	 * 
	 * @param sound sound id in the application environment.
	 * @param loop if true, the sound will loop.
	 * @param rate the playing pitch.
	 * @param side position[Left or right or both] where the sound will be played.
	 * 
	 * */
	public void playLooped(int sound, int loop, float rate, int side){
		stopSound(0);stopSound(1);
		if (loaded){
			soundID=soundMap.get(sound);
			//id, left volume, right volume, priority of sound, loop, rate of play
			if (side==0) soundPool.play(soundID, 0.0f, volume, 1, loop, rate);
			else soundPool.play(soundID, volume, 0.0f, 1, loop, rate);
		}
		stopSound(sound);
	}
	
	/**
	 * Stops a sound with id sound.
	 * 
	 * @param sound sound id.
	 * */
	public void stopSound(int sound){
		soundID=soundMap.get(sound);
		soundPool.stop(soundID);
	}

	/**
	 * Checks if headSet is connected to the device.
	 * 
	 * @return the checking results.
	 * */
	public boolean isWiredHeadsetOn() {
		return  audioManager.isWiredHeadsetOn();
	}
}
