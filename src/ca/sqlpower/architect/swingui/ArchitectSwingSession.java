/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.swingui;

import java.awt.Window;
import java.util.List;

import javax.swing.JDialog;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.CoreUserSettings;
import ca.sqlpower.architect.etl.kettle.KettleJob;
import ca.sqlpower.architect.olap.OLAPRootObject;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.swingui.olap.OLAPEditSession;
import ca.sqlpower.architect.undo.UndoManager;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.swingui.event.SessionLifecycleListener;

/**
 * The ArchitectSwingSession interface provides methods that are applicable
 * to a Swing UI invocation of the Architect.  It extends the ArchitectSession
 * interface, which provides information about a session with the core (non-UI specific)
 * objects.
 */
public interface ArchitectSwingSession extends ArchitectSession, SwingWorkerRegistry {
   
    /**
     * Returns the context that created this session.
     */
    public ArchitectSwingSessionContext getContext();
    
    /**
     * Narrows the return type for the project: Swing Sessions
     * have SwingUI projects, which are a subclass of CoreProject.
     */
    public SwingUIProject getProject();
    
    /**
     * Gets the recent menu list
     * 
     * @return the recent menu
     */
    public RecentMenu getRecentMenu();

    /**
     * Returns the ArchitectFrame created in this session. 
     */
    public ArchitectFrame getArchitectFrame();
    
    /**
     * Gets the value of sourceDatabases
     *
     * @return the value of sourceDatabases
     */
    public DBTree getSourceDatabases();
    
    /**
     * Gets the value of playPen
     *
     * @return the value of playPen
     */
    public PlayPen getPlayPen();
    
    /**
     * Gets the UndoManager keeping track of changes in this session
     */
    public UndoManager getUndoManager();
    
    public CompareDMSettings getCompareDMSettings();
      
    /**
     * Returns the JDialog containing the ProfileManagerView
     */
    public JDialog getProfileDialog();
    
    /**
     * This is a common handler for all actions that must
     * occur when switching projects, e.g., dispose dialogs, 
     * shut down running threads, etc. 
     */
    public void close();
    
    /**
     * See {@link #userSettings}.
     *
     * @return the value of userSettings
     */
    public CoreUserSettings getUserSettings();
    
    /**
     * Saves the project associated with this session, optionally showing a file
     * chooser, and optionally doing the work in a separate worker thread.
     * 
     * @param showChooser If true, a file chooser will always be presented.  If
     * false, a file chooser will only be presented if the project save location
     * has not yet been set.
     * @param separateThread If true, the work will be done in a separate thread
     * and this method will return in a shorter amount of time.
     * @return True if the save operation was not cancelled by the user.
     */
    public boolean saveOrSaveAs(boolean showChooser, boolean separateThread);

    /**
     * See {@link #savingEntireSource}.
     *
     * @return the value of savingEntireSource
     */
    public boolean isSavingEntireSource();

    /**
     * See {@link #savingEntireSource}.
     *
     * @param argSavingEntireSource Value to assign to this.savingEntireSource
     */
    public void setSavingEntireSource(boolean argSavingEntireSource);
    
    public KettleJob getKettleJob();

    public void setKettleJob(KettleJob kettleJob);

    /**
     * Initializes the GUI components for this session. Call this only if you need a GUI.
     * This method must be called on the Swing Event Dispatch Thread.
     * 
     * @throws ArchitectException
     * @throws IllegalStateException if showGUI==true and this method was
     * not called on the Event Dispatch Thread.
     */
    public void initGUI() throws ArchitectException;

    /**
     * Like initGUI(), this method initializes the GUI components for this
     * session, with the exception that the GUI components will get positioned
     * relative to the GUI component of the given ArchitectSwingSession. As with
     * initGUI(), call this only if you need a GUI. This method must be called
     * on the Swing Event Dispatch Thread.
     * 
     * @param openingSession
     *            The ArchitectSwingSession to which this session's GUI
     *            components will be positioned relative to
     * @throws ArchitectException
     * @throws IllegalStateException
     *             if showGUI==true and this method was not called on the Event
     *             Dispatch Thread.
     */
    public void initGUI(ArchitectSwingSession openingSession) throws ArchitectException;
    
    /**
     * Returns true if the session contains a completely new and unmodified project.
     * Otherwise, it returns false.
     * <p>
     * Note: that this property is different from the {@link SwingUIProject#isModified()}
     * property in that the isNew property is persisted when the project is saved, and
     * refers to whether the project was ever modified since it was first created, 
     * whereas, the isModified property refers to whether the project was modified ever
     * since the project was last loaded.  
     */
    public boolean isNew();

    /**
     * Relationship line style: True means direct lines; false means only horizontal
     * and vertical lines.
     */
    public boolean getRelationshipLinesDirect();

    /**
     * Relationship line style: True means direct lines; false means only horizontal
     * and vertical lines.  Updating this preference will cause all of the relationships
     * in this session's play pen to have their line style updated.
     */
    public void setRelationshipLinesDirect(boolean direct);
    
    /**
     * Returns whether the PK Tags will be shown
     */
    public boolean isShowPkTag();
    
    /**
     * Sets whether the PK Tags will be shown
     */
    public void setShowPkTag(boolean showPkTag);
    
    /**
     * Returns whether the FK Tags will be shown
     */
    public boolean isShowFkTag();
    
    /**
     * Sets whether the FK Tags will be shown
     */
    public void setShowFkTag(boolean showFkTag);
    
    /**
     * Returns whether the AK Tags will be shown
     */
    public boolean isShowAkTag();
    
    /**
     * Sets whether the AK Tags will be shown
     */
    public void setShowAkTag(boolean showAkTag);
   
    /**
     * Indicates whether PK Columns will be shown. 
     */
    public boolean isShowPrimary();
    
    /**
     * Sets whether PK Columns should be shown.
     */
    public void setShowPrimary(boolean showPrimary);
    
    /**
     * Indicates whether FK Columns will be shown. 
     */
    public boolean isShowForeign();
    
    /**
     * Sets whether FK Columns should be shown.
     */
    public void setShowForeign(boolean showForeign);
    
    /**
     * Indicates whether Unique Columns will be shown. 
     */
    public boolean isShowUnique();
    
    /**
     * Sets whether Unique Columns should be shown.
     */
    public void setShowUnique(boolean showUnique);
    
    /**
     * Indicates whether Indexed Columns will be shown. 
     */
    public boolean isShowIndexed();
    
    /**
     * Sets whether Indexed Columns should be shown.
     */
    public void setShowIndexed(boolean showIndexed);
    
    /**
     * Indicates whether Columns that are not PK, FK, Unique or Indexed will be shown. 
     */
    public boolean isShowTheRest();
    
    /**
     * Sets whether Columns that are not PK, FK, Unique or Indexed should be shown.
     */
    public void setShowTheRest(boolean showTheRest);
    
    /**
     * Shows the schema manager dialog for this session's OLAP Schemas. 
     * 
     * @param owner The owner of the dialog.
     */
    public void showOLAPSchemaManager(Window owner);    
    
    /**
     * Returns a list of the OLAPEditSessions associated with this session's OLAP Schemas.
     * 
     * @return the list of edit sessions.
     */
    public List<OLAPEditSession> getOLAPEditSessions();
    
    
    /**
     * Returns the OLAPEditSession that is associated with editing the given
     * OLAPSession.
     * 
     * @param olapSession
     *            the OLAPSession in question, must not be null.
     * @return the associated OLAPEditSession, creates a new one if none found.
     */
    public OLAPEditSession getOLAPEditSession(OLAPSession olapSession);
    
    /**
     * Returns the OLAP root object, which contains all the OLAP sessions that are part of this
     * Architect session.
     * <p>
     * Note: We would prefer not to let ArchitectSession reference anything in the OLAP editor,
     * since we do not want the core Architect API to include OLAP support.  We are currently
     * trying to come up with a way to put this somewhere else.
     */
    public OLAPRootObject getOLAPRootObject();

    /**
     * Adds the given listener to the list of interested parties who want to be
     * notified when this session is about to close. Being a lifecycle listener
     * is an excellent way to make a session shutdown hook for your subsystem.
     */
    public void addSessionLifecycleListener(SessionLifecycleListener<ArchitectSwingSession> listener);

    /**
     * Removes the given listener from the lifecycle event list. Once removed,
     * the listener will not receive a sessionClosing notification when this
     * session closes.
     */
    public void removeSessionLifecycleListener(SessionLifecycleListener<ArchitectSwingSession> listener);
}
