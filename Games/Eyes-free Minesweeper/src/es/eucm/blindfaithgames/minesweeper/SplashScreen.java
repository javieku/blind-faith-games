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
 * 	  This file is part of EYES-FREE MINESWEEPER, developed in the Blind Faith Games project.
 *  
 *        EYES-FREE MINESWEEPER, is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU Lesser General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *   
 *       EYES-FREE MINESWEEPER is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Lesser General Public License for more details.
 *   
 *       You should have received a copy of the GNU Lesser General Public License
 *       along with Adventure.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package es.eucm.blindfaithgames.minesweeper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.Toast;
import es.eucm.blindfaithgames.bfgtoolkit.sound.SubtitleInfo;
import es.eucm.blindfaithgames.bfgtoolkit.sound.TTS;

public class SplashScreen extends Activity {
    protected boolean _active = true;
    protected int _splashTime = 5000;
	private TTS textToSpeech;
    
	private Runnable lastRunnable;
	private Handler handler;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
      
		SubtitleInfo s = new SubtitleInfo(R.layout.toast_custom, R.id.toast_layout_root,
				R.id.toast_text, 0, 0, Toast.LENGTH_SHORT, Gravity.BOTTOM, null);
       
		// Checking if TTS is installed on device
		textToSpeech = new TTS(this, getString(R.string.mode) + " "  + getString(R.string.group_name), TTS.QUEUE_FLUSH, s);
        
        handler = new Handler();

	    lastRunnable = new Runnable() {
	            public void run() {
	            	finish();
	                Intent i = new Intent("es.eucm.blindfaithgames.minesweeper.MinesweeperActivity");
	                startActivity(i);
	            }
	    };
        handler.postDelayed(lastRunnable, 15000);
    }
    
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            handler.removeCallbacks(lastRunnable);
            handler.postDelayed(lastRunnable, 1);
        }
        return true;
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(KeyEvent.KEYCODE_BACK == keyCode)
			return true;
		return super.onKeyDown(keyCode, event);
	}
	
}
