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
package es.eucm.blindfaithgames.minesweeper.game;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import es.eucm.blindfaithgames.bfgtoolkit.feedback.AnalyticsManager;
import es.eucm.blindfaithgames.bfgtoolkit.feedback.Log;
import es.eucm.blindfaithgames.bfgtoolkit.input.Input;
import es.eucm.blindfaithgames.bfgtoolkit.input.XMLKeyboard;
import es.eucm.blindfaithgames.minesweeper.KeyConfActivity;
import es.eucm.blindfaithgames.minesweeper.Minesweeper;
import es.eucm.blindfaithgames.minesweeper.MinesweeperTutorialActivity;
import es.eucm.blindfaithgames.minesweeper.PrefsActivity;
import es.eucm.blindfaithgames.minesweeper.R;
import es.eucm.blindfaithgames.minesweeper.game.Cell.CellStates;

public class MinesweeperTutorialView extends View {
	private static final String TAG = "MinesweeperTutorialGame";

	private enum TutorialState { TUT_TAP, TUT_DTAP, TUT_TRTAP, TUT_DRAG, TUT_KEYBOARD, 
								 TUT_ZOOM_MODE,  TUT_EXPLORATION_MODE, TUT_READ_CONTEXT, TUT_END };
	
	private static final int ARROW_SIZE = 100; // Arrows zoom mode size in pixels
	private static final int CELL_SIZE = 200; // Cell size in pixels

	private static final int TIME_TO_DO_TAP = 750; // ms
	
	private static final int START_DRAGGING = 0;
	private static final int STOP_DRAGGING = 1;
	
	// Dragging && double/triple tap attributes
	private int dragging;
	private Long timeTaps[]; // time (system time) of each tap
	private int tapsN; // number of taps
	private MotionEvent eventTaps[]; // tap events  
	
	private final static int cellSeparation = 5; // Separation between cells
	private final static int iniPosY = 100; // minefield offset with Y 
	private static final float TRANSITION_LIMIT = 100;
	private int toast_long;
	private int rowN, colN; // row and column of the focused cell
	
	private final MinesweeperTutorialActivity game;
	
	private float width; // width of one cell
	private float height; // height of one cell
	
	private int selCol; // row index of selection
	private int selRow; // col index of selection
	private final Rect selRect = new Rect();
	
	private boolean drawTransitionMode;
	
	private boolean zoomMode; // Enable/Disable zoom mode
	
	private Paint brush; // Used to manage colors, fonts...
	
	private TutorialState state;// step in the tutorial
	
	private boolean blindMode; // Enable/Disable blind mode
	
	private boolean pushed; 
	
	// Cargamos la conf desde un .xml
	private XMLKeyboard keyboard;
	
	private Runnable RunnableEvent= new Runnable() {

		public void run() {
		
			// triple tap
			if(timeTaps[1] != 0  && timeTaps[0] != 0 && timeTaps[2] != 0 && (state == TutorialState.TUT_TRTAP || state == TutorialState.TUT_ZOOM_MODE || state == TutorialState.TUT_EXPLORATION_MODE || state == TutorialState.TUT_READ_CONTEXT)){
				onTripleTapAction(eventTaps[2]);
				if(zoomMode){
				Log.getLog().addEntry(MinesweeperTutorialView.TAG,
						PrefsActivity.configurationToString(game),
						Log.TRIPLE_TAP_EVENT,Thread.currentThread().getStackTrace()[2].getMethodName(),"Zoom mode " + zoomMode + " " + eventTaps[2].getX() + " " + eventTaps[2].getY());
				
				AnalyticsManager.getAnalyticsManager().registerAction(MinesweeperAnalytics.TUTORIAL_EVENTS, MinesweeperAnalytics.TRIPLE_TAP, 
						"Zoom mode " + zoomMode + " " + eventTaps[2].getX() + " " + eventTaps[2].getY(), 0);
				}
			}else// double tap
				if(timeTaps[1] != 0  && timeTaps[0] != 0 && (state == TutorialState.TUT_DTAP|| state == TutorialState.TUT_ZOOM_MODE || state == TutorialState.TUT_EXPLORATION_MODE || state == TutorialState.TUT_READ_CONTEXT)){
					onDoubleTapAction(eventTaps[1]);
					Log.getLog().addEntry(MinesweeperTutorialView.TAG,
							PrefsActivity.configurationToString(game),
							Log.DOUBLE_TAP_EVENT,Thread.currentThread().getStackTrace()[2].getMethodName(),"Zoom mode " + zoomMode + " " + eventTaps[1].getX() + " " + eventTaps[1].getY());
					
					AnalyticsManager.getAnalyticsManager().registerAction(MinesweeperAnalytics.TUTORIAL_EVENTS, MinesweeperAnalytics.DOUBLE_TAP, 
							"Zoom mode " + zoomMode + " " + eventTaps[1].getX() + " " + eventTaps[1].getY(), 0);
				}
				else{ // one tap
					if(timeTaps[0] != 0){
						float y = eventTaps[0].getRawY();
						if(y < TRANSITION_LIMIT){
							changeViewState();
						}else{
							if( (state == TutorialState.TUT_TAP || state == TutorialState.TUT_DRAG  || state == TutorialState.TUT_ZOOM_MODE || state == TutorialState.TUT_EXPLORATION_MODE || state == TutorialState.TUT_READ_CONTEXT)){
								onTapAction(eventTaps[0]);
								Log.getLog().addEntry(MinesweeperTutorialView.TAG,
									PrefsActivity.configurationToString(game),
									Log.TAP_EVENT,Thread.currentThread().getStackTrace()[2].getMethodName(),"Zoom mode " + zoomMode + " " + eventTaps[0].getX() + " " + eventTaps[0].getY());
							
								AnalyticsManager.getAnalyticsManager().registerAction(MinesweeperAnalytics.TUTORIAL_EVENTS, MinesweeperAnalytics.SIMPLE_TAP, 
									"Zoom mode " + zoomMode + " " + eventTaps[0].getX() + " " + eventTaps[0].getY(), 3);
							}
						}
					}
				}
			// Consumed the event reset all
			for(int i = 0; i < 3; i++){
				timeTaps[i] = (long) 0;
				eventTaps[i] = null;
				tapsN = 0;
			}
		}
	};
	
	public MinesweeperTutorialView(Context context, int rowN, int colN) {
		super(context);
		
		state = TutorialState.TUT_DRAG;
		String msg = this.getContext().getString(R.string.tut_intro_text) + " "  + 
					 this.getContext().getString(R.string.tut_drag_text) + " " + 
					 this.getContext().getString(R.string.tut_step);
		toast = Toast.makeText(this.getContext(), msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.BOTTOM, 0, 0);
		displayMyToast(msg, true);
		
		setLongClickable(true);
		
		this.rowN = rowN;
		this.colN = colN;
		selCol = 0;
		selRow = 0;
		
		game = (MinesweeperTutorialActivity) context;
		requestFocus();
		setFocusableInTouchMode(true);
		
		brush = new Paint();
		
		tapsN = 0;
		timeTaps = new Long[3];
		for(int i = 0; i < 3; i++)
			timeTaps[i] = (long) 0;
		eventTaps = new MotionEvent[3];
		
		zoomMode = false;
		
		blindMode = PrefsActivity.getBlindMode(this.getContext());
		
		this.keyboard = Input.getKeyboard();
		
		Log.getLog().addEntry(MinesweeperTutorialView.TAG,
					PrefsActivity.configurationToString(game),
					Log.NONE,Thread.currentThread().getStackTrace()[2].getMethodName(),
					width + " " + height + " " + cellSeparation + " " + CELL_SIZE + " " + ARROW_SIZE);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// Set cell dimensions
		width = (w / (float)colN) - cellSeparation; 
		height = width;// iniPosY=0 ---> (h / (float)rowN) - cellSeparation; 
		setSelectedRect(selCol, selRow, selRect);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	/**
	 * Set the focus rectangle dimensions
	 * 
	 * */
	private void setSelectedRect(int col, int row, Rect rect) {
		rect.set((int) (col * width + (cellSeparation * (col+1))), (int) (row * height + (cellSeparation * (row+1))) + iniPosY,
				(int) (col * width + width + (cellSeparation * (col+1))), (int) (row * height + height + (cellSeparation * (row+1)))+ iniPosY) ;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// Draw the background...
		canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), 
							R.drawable.minefield_background),
							null, 
							new Rect(0,0,getWidth(),getHeight()), 
							null);
		
		if (zoomMode){
			
			if(drawTransitionMode){
				drawTransitionMode = false;
				
				drawMinefieldZoom(selRow,selCol,canvas);
				
				invalidate();
			}
			else{
				// Draw the center cell
				drawMinefieldZoom(selRow,selCol,canvas);
				// Draw arrow buttons
				drawArrows(canvas);
			}
			
		}else{
			Paint brush = new Paint();
			if(pushed)
				brush.setColor(getContext().getResources().getColor(R.color.transparent_orange1));
			else
				brush.setARGB(200, 255, 255, 255);
			
			canvas.drawRect(0, 0, getWidth(), TRANSITION_LIMIT, brush);
			brush.setARGB(255, 0, 0, 0);
			brush.setTextSize(20);
			canvas.drawText(getContext().getResources().getString(R.string.next),  
					getWidth()/2 + brush.getFontSpacing()/2, TRANSITION_LIMIT/2 + brush.getFontSpacing()/2, brush);
			
			
			// Draw no pushed cells and mines
			drawMinefield(canvas);
			
			// Draw the selection
			drawSelection(canvas);
		}

		if(blindMode){
			canvas.drawColor(Color.BLACK);
			invalidate();
		}
	}

	/**
	 * Used in zoomMode draw the arrows if the position exist
	 * */
	private void drawArrows(Canvas canvas) {
		if(selCol - 1 >= 0)
			canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.left_arrow), 
							null, 
							new Rect(0, 
									(int)getHeight()/2 - ARROW_SIZE/2, 
									ARROW_SIZE, 
									(int)(getHeight()/2)- ARROW_SIZE/2 + ARROW_SIZE ),
									null);
		if(selCol + 1 < colN)
			canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.right_arrow), 
							null, 
							new Rect(getWidth() - ARROW_SIZE, 
									(int)getHeight()/2 - ARROW_SIZE/2, 
									getWidth(), 
									(int)(getHeight()/2)- ARROW_SIZE/2 + ARROW_SIZE ),
									null);
		if(selRow - 1 >= 0)
			canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.up_arrow), 
							null, 
							new Rect(getWidth()/2 - ARROW_SIZE/2,
									0,
									(int)(getWidth()/2) - ARROW_SIZE/2 + ARROW_SIZE,
									ARROW_SIZE),
									null);
		if(selRow + 1 < colN)
			canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.down_arrow), 
							null, 
							new Rect(getWidth()/2 - ARROW_SIZE/2,
									getHeight() - ARROW_SIZE,
									(int)(getWidth()/2) - ARROW_SIZE/2 + ARROW_SIZE,
									getHeight()),
									null);
	}

	/**
	 * Used in Zoom Mode. Only draws the cell indicated by selX&selY.
	 * in a similar way of drawMineField
	 * 
	 * @param row row in the matrix
	 * @param col col in the matrix
	 * @param canvas 
	 * 
	 * */
	private void drawMinefieldZoom(int row, int col, Canvas canvas) {
		// Draw the numbers...
		// Define color and style for numbers
		Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
		foreground.setColor(Color.BLACK);
		foreground.setStyle(Style.FILL);
		foreground.setTextSize(height * 0.75f);
		foreground.setTextScaleX(width / height);
		foreground.setTextAlign(Paint.Align.CENTER);
		
		// Centering in X: use alignment (and X at midpoint)
		float x = 100;
		// Centering in Y: measure ascent/descent first
		float y = 125;
		
		Rect positionDrawable = new Rect((int)getWidth()/2 - CELL_SIZE/2,
											(int)getHeight()/2 - CELL_SIZE/2,
											((int)getWidth()/2) - CELL_SIZE/2 + CELL_SIZE,
											((int)getHeight())/2 - CELL_SIZE/2 + CELL_SIZE);
		
		switch(this.game.getCell(row, col).getState()){
			case PUSHED:
				if(this.game.getTileString(row, col).equals("0")){
					canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.square_grey),
							null,positionDrawable, null);
				}
				else{
					canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.square_grey),
							null,
							positionDrawable, null);
					canvas.drawText(this.game.getTileString(row, col), 
							(int)getWidth()/2 - CELL_SIZE/2 + x,
							(int)getHeight()/2 - CELL_SIZE/2 + y, 
							foreground);
				}
			break;
			case NOTPUSHED:
				canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.square_blue),
						null,
						positionDrawable, null);
			break;
			case MINE:
				
				if(this.game.getCell(row,col).isVisible()){ // if the mine has been pushed
					canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.square_grey),
							null,
							positionDrawable, null);
					canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mine),
							null,
							positionDrawable, null);
				}else
					canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.square_blue),
							null,
							positionDrawable, null);
			break;
			case FLAGGED:
				canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.square_blue),
						null,
						positionDrawable, null);
				canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.flag),
						null,
						positionDrawable, null);
				break;

		}
	}
	
	/**
	 * Draws all states that a cell can have.
	 * 
	 * */
	private void drawMinefield(Canvas canvas) {
		// Draw the numbers...
		// Define color and style for numbers
		Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
		foreground.setColor(Color.BLACK);
		foreground.setStyle(Style.FILL);
		foreground.setTextSize(height * 0.75f);
		foreground.setTextScaleX(width / height);
		foreground.setTextAlign(Paint.Align.CENTER);
		
		// Draw the number in the center of the tile
		FontMetrics fm = foreground.getFontMetrics();
		// Centering in X: use alignment (and X at midpoint)
		float x = width / 2;
		// Centering in Y: measure ascent/descent first
		float y = height / 2 - (fm.ascent + fm.descent) / 2;
	
		for (int row = 0; row < rowN; row++) {
			for (int col = 0; col < colN; col++) {
				switch(this.game.getCell(row, col).getState()){
					case PUSHED:
						if(this.game.getTileString(row, col).equals("0")){
							drawResource(row,col,R.drawable.square_grey,canvas);
						}
						else{
							drawResource(row,col,R.drawable.square_grey,canvas);
							canvas.drawText(this.game.getTileString(row, col), 
									(col * width + x) + (cellSeparation * (col + 1)), 
									(row * height + y) + (cellSeparation * (row + 1))+ iniPosY, 
									foreground);
						}
					break;
					case NOTPUSHED:
						drawResource(row,col,R.drawable.square_blue,canvas);
					break;
					case MINE:
						if(this.game.getCell(row,col).isVisible()){
							drawResource(row,col,R.drawable.square_grey,canvas);
							drawResource(row,col,R.drawable.mine,canvas);
						}else
							drawResource(row,col,R.drawable.square_blue,canvas);
					break;
					case FLAGGED:
						drawResource(row,col,R.drawable.square_blue,canvas);
						// centering in X,Y
						canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.flag),
										null, 
										new Rect((int)(col * width + width/4  + (cellSeparation * (col + 1))), 
										(int)(row * height + height/4  + (cellSeparation * (row + 1)))+ iniPosY, 
										(int)(col * width + width/4 + width/2 + (cellSeparation * (col + 1))), 
										(int)(row * height + height/4 + height/2 + (cellSeparation * (row + 1)))+ iniPosY),
										null);
						break;
			}
		}
	}
		
	}

	private void drawResource(int row , int col, int id, Canvas canvas) {
		canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), id),
						null, 
						new Rect((int)(col * width + (cellSeparation * (col + 1))), 
						(int)(row * height + (cellSeparation * (row + 1))) + iniPosY, 
						(int)(col * width + width + (cellSeparation * (col + 1))), 
						(int)(row * height + height + (cellSeparation * (row + 1)))+ iniPosY),
						null);
	}

	/**
	 * Draw the focus
	 * 
	 * */
	private void drawSelection(Canvas canvas) {
		brush.setColor(getResources().getColor(R.color.transparent_orange1));
		canvas.drawRect(selRect, brush);
	}

	
	/**
	 * Pushed a cell in the matrix and updates rectangle focus
	 * 
	 * */
	private void selectCell(int col, int row) {
		// pinta la sección que ha sido deseleccionada
		invalidate(selRect);
		selCol = Math.min(Math.max(col, 0), colN);
		selRow = Math.min(Math.max(row, 0), rowN);
		boolean speech = this.game.pushCell(selRow,selCol);
		if(speech){
			if(game.getCounter() > 5)
				this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE,this.getContext().getString(R.string.pushCellSuccess) 
								    + " " + this.getContext().getString(R.string.state)
									+ " " + game.getCell(selRow, selCol).cellToString(this.getContext()));
			else{
				if(!game.getCell(selRow, selCol).getState().equals(CellStates.MINE))
					this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE, game.getCell(selRow, selCol).cellToString(this.getContext()));
			}
		}

		setSelectedRect(selCol, selRow, selRect);
	}
	
	/**
	 * Manages touch events
	 * 
	 * */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		if(!zoomMode){
			onDragAction(event);
			return true;
		}else{
	    	timeTaps[tapsN] = System.currentTimeMillis();
	    	eventTaps[tapsN] = event;
	    	if(tapsN < 2)
	    		tapsN++;
	    	Handler myHandler = new Handler();
	    	myHandler.postDelayed(RunnableEvent, TIME_TO_DO_TAP);
	    	return true;
		}
	}
	
	/**
	 * Manages drag event. Reads only on cell change in the drag event
	 * Begins with read if the cell is new in MotionEvent.ACTION_DOWN
	 * Continues with MotionEvent.ACTION_MOVE if is new cell again reads and marks the event like consumed (readEvent) 
	 * if not read then the event only is a tap.
	 * @param event The event that it will be threated
	 * */
	private void onDragAction(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		int col = Math.min(Math.max((int) (x / (width + cellSeparation)), 0), colN-1);
		int row = Math.min(Math.max((int) ((y / (height + cellSeparation)) - iniPosY / height), 0), rowN-1);
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			dragging = START_DRAGGING;
			if((selCol != col || selRow != row) && event.getRawY() > TRANSITION_LIMIT){
	        	this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE,this.game.getCell(row, col).cellToString(this.getContext()));
	        		selCol = col;
	        		selRow = row;
	    			invalidate(selRect);
	    			setSelectedRect(selCol, selRow, selRect);
	    			invalidate(selRect);
			}
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			dragging = STOP_DRAGGING;
		    timeTaps[tapsN] = System.currentTimeMillis();
		    eventTaps[tapsN] = event;
			if(tapsN == 0){
				float auxy = eventTaps[0].getRawY();
				if(auxy < TRANSITION_LIMIT)
					pushed = true;
				invalidate();
			}
		    if(tapsN < 2)
		    	tapsN++;
		    Handler myHandler = new Handler();
		    myHandler.postDelayed(RunnableEvent, TIME_TO_DO_TAP);
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (dragging == START_DRAGGING) {
				if((selCol != col || selRow != row) && event.getRawY() > TRANSITION_LIMIT){
		        		this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE,this.game.getCell(row, col).cellToString(this.getContext()));
		        		selCol = col;
		        		selRow = row;
		    			invalidate(selRect);
		    			setSelectedRect(selCol, selRow, selRect);
		    			invalidate(selRect);
				}
			}
		}	
	}

	/**
	 * ZoomMode action or in normal mode changes the focused position
	 * 
	 * */
	public void onTapAction(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		if(!zoomMode){
			invalidate(selRect);
			selCol = Math.min(Math.max((int) (x / (width + cellSeparation)), 0), colN-1);
			selRow = Math.min(Math.max((int) ((y / (height + cellSeparation)) - iniPosY / height), 0), rowN-1);
			setSelectedRect(selCol, selRow, selRect);
			invalidate(selRect);
		}else{
			onZoomMode(x,y);
			invalidate();
		}
	}
	
	/**
	 * zoomMode action or in normal mode push a cell
	 * 
	 * */
	public void onDoubleTapAction(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		if(!zoomMode)
			selectCell(selCol,selRow);
		else
			onZoomMode(x,y);
		invalidate();
	}
	
	/**
	 * zoomMode action or in normal mode flag a cell
	 * 
	 * */
	public void onTripleTapAction(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		if(!zoomMode){
			if(this.game.isFlagMode()){
				selectCell(selCol,selRow);
			}
			else{
				this.game.switchFlag();
				selectCell(selCol,selRow);
				this.game.switchFlag();
			}
		}
		else
			onZoomMode(x,y);

		invalidate();
	}
	
	/**
	 * if the tap is in the center of the screen push the cell
	 * if arrow has pushed change the focused cell.
	 * */
	private void onZoomMode(float x, float y) {
		if(x >= ARROW_SIZE && x <= getWidth()-ARROW_SIZE && y >= ARROW_SIZE && y <= getHeight() - ARROW_SIZE)
			selectCell(selCol,selRow);
		else{
			// Left
			if(x < ARROW_SIZE && y > ARROW_SIZE && y < getHeight() - ARROW_SIZE && selCol-1 >= 0)
				selCol -= 1;
			// Right
			if(x > getWidth() - ARROW_SIZE && y > ARROW_SIZE && y < getHeight() - ARROW_SIZE && selCol+1 < rowN)
				selCol += 1;
			// Up
			if(x > ARROW_SIZE && x < getWidth() - ARROW_SIZE && y < ARROW_SIZE && selRow-1 >= 0)
				selRow -= 1;
			// Down
			if(x > ARROW_SIZE && x < getWidth() - ARROW_SIZE && y > getHeight() - ARROW_SIZE && selRow+1 < colN)
				selRow += 1;
			drawTransitionMode = true;
		}
		this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE,this.game.getCell(selRow, selCol).cellToString(this.getContext()));
        setSelectedRect(selCol, selRow, selRect);
	}
	/**
	 * Manages key events
	 * 
	 * if keyCode is DPAD_UP, DPAD_DOWN, DPAD_LEFT or DPAD_RIGHT then
	 * if keyCode is SEARCH enables exploration mode
	 * if keyCode is VOLUME_UP changes interface visualization (zoom mode)
	 * if keyCode is VOLUME_DOWN changes interface visualization (zoom mode)
	 * if keyCode is MENU then plays a voice synthesizer
	 * if keyCode is BACK then the activity finish
	 * */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
	    super.onKeyDown(keyCode, event);
	    
	    invalidate(selRect);
	    int i = 0; 
	    boolean found = false;
		if(event.getAction() == KeyEvent.ACTION_DOWN){
		    while (!found && i < keyboard.getNum()){
		    	// Si encontramos la key significa que tiene una acción en nuestro teclado
		    	found = keyboard.getAction(keyCode) != null;   
		    	i++;
		    }
		    if (found){
		    	if (keyboard.getAction(keyCode).equals("zoom") && (state == TutorialState.TUT_ZOOM_MODE)){
		        	zoomMode = !zoomMode;
		        	this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE, this.game.getString(R.string.zoom_mode_TTStext) + (zoomMode ? " On" : " Off"));
		        	invalidate();
		    	}
		    	else if (keyboard.getAction(keyCode).equals("exploration") && state == TutorialState.TUT_EXPLORATION_MODE){
		        	this.game.switchFlag();
		        	this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE,this.game.getString(R.string.exploration_mode_TTStext) + (this.game.isFlagMode() ? " On" : " Off"));
		        	invalidate();
		    	}
		    	else if (keyboard.getAction(keyCode).equals("coordinates") && state == TutorialState.TUT_KEYBOARD){
		        	this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE, this.game.getString(R.string.coordinates_information_button_TTStext) + " " 
							+ selCol + " " + selRow + " State " 
						+ this.game.getCell(selRow, selCol).cellToString(this.getContext()));
		    	}
		    	else if (keyboard.getAction(keyCode).equals("instructions") && state == TutorialState.TUT_KEYBOARD){
		        	this.game.mTtsActionControls();
		    	}
		    	else if (keyboard.getAction(keyCode).equals("context") && state == TutorialState.TUT_READ_CONTEXT){
		        	this.game.speakContextFocusedCell(selRow, selCol);
		    	}
		    	else if (keyboard.getAction(keyCode).equals("blind_mode")){
		        	blindMode = !blindMode;
		        	invalidate();
		    	}
		    	else if (keyboard.getAction(keyCode).equals(KeyConfActivity.ACTION_REPEAT)){
		        	this.game.getTTS().repeatSpeak();
		    	}
		    }
		    
		    
		    // Teclas siempre fijas
		    else{
		        switch(keyCode){
			        case KeyEvent.KEYCODE_DPAD_UP:
			        	selRow = MinesweeperMath.mod(selRow - 1, rowN);
			        	this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE,this.game.getCell(selRow, selCol).cellToString(this.getContext()));
			        	setSelectedRect(selCol, selRow, selRect);
			        	if(zoomMode)
			        		drawTransitionMode = true;
			        	break;
			        case KeyEvent.KEYCODE_DPAD_DOWN:
			        	selRow = MinesweeperMath.mod(selRow + 1, rowN);
			        	this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE,this.game.getCell(selRow, selCol).cellToString(this.getContext()));
			        	setSelectedRect(selCol, selRow, selRect);
			        	if(zoomMode)
			        		drawTransitionMode = true;
			            break;
			        case KeyEvent.KEYCODE_DPAD_LEFT:
			        	selCol = MinesweeperMath.mod(selCol - 1, colN);
			        	this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE,this.game.getCell(selRow, selCol).cellToString(this.getContext()));
			        	setSelectedRect(selCol, selRow, selRect);
			        	if(zoomMode)
			        		drawTransitionMode = true;
			        	break;
			        case KeyEvent.KEYCODE_DPAD_RIGHT:
			        	selCol = MinesweeperMath.mod(selCol + 1, colN);
			        	this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE,this.game.getCell(selRow, selCol).cellToString(this.getContext()));
			        	setSelectedRect(selCol, selRow, selRect);
			        	if(zoomMode)
			        		drawTransitionMode = true;
			            break;
			        case KeyEvent.KEYCODE_DPAD_CENTER:
			        	selectCell(selCol,selRow);
			        	invalidate();
			            break;
			        case KeyEvent.KEYCODE_BACK:
			        	state = TutorialState.TUT_END;
			        	this.game.finish();
			            break;
		        }
		    }
			if(zoomMode)
				invalidate();
			else
				invalidate(selRect);
		}
		
		Log.getLog().addEntry(MinesweeperTutorialView.TAG,
				PrefsActivity.configurationToString(game),
				Log.KEY_EVENT,Thread.currentThread().getStackTrace()[2].getMethodName(),
				keyboard.toString(keyCode) + " " +keyboard.getAction(keyCode));
		
		AnalyticsManager.getAnalyticsManager().registerAction(MinesweeperAnalytics.TUTORIAL_EVENTS, MinesweeperAnalytics.KEY_PUSHED, 
				keyboard.toString(keyCode) + " " + keyboard.getAction(keyCode), 0);
		return true;
	}
	
	private Toast toast;

	private boolean changeViewState() {
		String msg;
		this.game.resetBoard();
	    pushed = false;
		invalidate();
		switch(state){
		case TUT_DRAG:
			state = TutorialState.TUT_TAP;
			msg = this.getContext().getString(R.string.tut_tap_text) + " "  +  
					this.getContext().getString(R.string.tut_step);
			toast = Toast.makeText(this.getContext(), msg, msg.length());
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			displayMyToast(msg, true);
			this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE, msg);
			break;
		case TUT_TAP:
			state = TutorialState.TUT_DTAP;
			msg = this.getContext().getString(R.string.tut_dtap_text) + " "  +   
					  this.getContext().getString(R.string.tut_step);
			toast = Toast.makeText(this.getContext(), msg, msg.length());
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			displayMyToast(msg, true);
			this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE, msg);
			break;
		case TUT_DTAP:
			state = TutorialState.TUT_TRTAP;
			msg = this.getContext().getString(R.string.tut_trtap_text) + " "  +  
				      this.getContext().getString(R.string.tut_step);
			toast = Toast.makeText(this.getContext(), msg, msg.length());
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			displayMyToast(msg, true);
			this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE, msg);
			break;
		case TUT_TRTAP:
			state = TutorialState.TUT_KEYBOARD;
			msg = this.getContext().getString(R.string.tut_keyboard_text) + " "  +   
			          this.getContext().getString(R.string.tut_step);
			toast = Toast.makeText(this.getContext(), msg, msg.length());
			displayMyToast(msg, true);
			this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE, msg);
			break;
		case TUT_KEYBOARD: // It tells about every option that the user can use in the keyboard 
			state = TutorialState.TUT_ZOOM_MODE;
			msg = this.getContext().getString(R.string.tut_zoom_mode_text) + " "  +  
                    this.getContext().getString(R.string.tut_step);
			toast = Toast.makeText(this.getContext(), msg, msg.length());
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			displayMyToast(msg, true);
			this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE, msg);
			break;
		case TUT_ZOOM_MODE:
			state = TutorialState.TUT_EXPLORATION_MODE;
			msg = this.getContext().getString(R.string.tut_exploration_mode_text) + " "  +  
                    this.getContext().getString(R.string.tut_step);
			toast = Toast.makeText(this.getContext(), msg, msg.length());
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			displayMyToast(msg, true);
			this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE, msg);
			break;
		case TUT_EXPLORATION_MODE:
			state = TutorialState.TUT_READ_CONTEXT;
			msg = this.getContext().getString(R.string.tut_read_context_text) + " "  +  
					  this.getContext().getString(R.string.tut_finish);
			toast = Toast.makeText(this.getContext(), msg, msg.length());
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			displayMyToast(msg, true);
			this.game.mTtsAction(Minesweeper.SPEECH_READ_CODE, msg);
			break;
		case TUT_READ_CONTEXT:
			state = TutorialState.TUT_END;
			this.game.finish();
			break;
		default:
			break;
		}
		return true;
	}
	
	private Runnable extendStatusMessageLengthRunnable = new Runnable() {
		@Override
	    public void run() {
	    //Show the toast for another interval.
	    toast.show();
	    toast_long++;
	    if(state != TutorialState.TUT_END && toast_long <= 4)
	    	postDelayed(extendStatusMessageLengthRunnable, 2000L);
	   }
	}; 

	public void displayMyToast(final String statusMessage, boolean extraLongDuration) {
	  removeCallbacks(extendStatusMessageLengthRunnable);

	  toast_long = 0;
	  
	  toast.setText(statusMessage);
	  toast.show();

	  if(extraLongDuration) {
	    postDelayed(extendStatusMessageLengthRunnable, 2000L);
	  }
	}
}
