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

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

/**
 * 
 * This class manages the device volume.
 * 
 *  @author Javier Álvarez & Gloria Pozuelo.
 * 
 * */

public class VolumeManager {
	
	private static AudioManager amanager; // Singleton
	
	/**
	 * Given a context, returns a instance of AudioManager Class.
	 *
	 * @param c context where the manager will be used.
	 * 
	 * @return instance of Audio Class.
	 * */
	public static AudioManager getAudioManager(Context c) {
		if (amanager == null) {
			amanager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
		}
		return amanager;
	}
	
	/**
	 * Sets the manager volume in the context c, with an increment incr.
	 * 
	 *  @param c context where the manager will be used.
	 *  @param incr volume change.
	 * 
	 * */
	public static void setVolume(Context c,int incr){
		if (amanager == null) {
			amanager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
		}
		amanager.setStreamVolume(AudioManager.STREAM_MUSIC,incr , AudioManager.FLAG_SHOW_UI);
	}
	
	/**
	 * Gets the manager in the context c.
	 *
	 * @param c context where the manager will be used.
	 * 
	 * @return stream volume.
	 * 
	 * */
	public static float getVolume(Context c){
		if (amanager == null) {
			amanager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
		}
		return amanager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}
	
	/**
	 * Adjusts the volume of a particular stream by one step in a direction.
	 * 
	 *  @param c context where the manager will be used.
	 *  @param direction The direction to adjust the volume. One of ADJUST_LOWER, ADJUST_RAISE, or ADJUST_SAME.
	 *  
	 * */
	public static void adjustStreamVolume(Context c, int direction){
		if (amanager == null) {
			amanager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
		}
		amanager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
				direction, AudioManager.FLAG_SHOW_UI);
	}

	/**
	 * Returns the maximum volume index for a particular stream.
	 * 
	 * @param c context where the manager will be used.
	 * 
	 * @return the maximum valid volume index for the stream.
	 * 
	 * */
	public static float getVolumeMax(Activity c) {
		if (amanager == null) {
			amanager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
		}
		return amanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}
	
}
