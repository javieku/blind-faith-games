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
package es.eucm.blindfaithgames.bfgtoolkit.feedback;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey = "dE54QkpHMDBFNmRqVmJ5UnlBcjZlWFE6MQ",
customReportContent = { ReportField.PACKAGE_NAME, ReportField.APP_VERSION_CODE, ReportField.ANDROID_VERSION, ReportField.BRAND, ReportField.PHONE_MODEL, 
		ReportField.INSTALLATION_ID, ReportField.CUSTOM_DATA, ReportField.SHARED_PREFERENCES, ReportField.IS_SILENT,
		ReportField.STACK_TRACE })
public class AcraSending extends Application {
	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		super.onCreate();
	}
}