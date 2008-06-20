package ca.sqlpower.architect.swingui.action;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.layout.ArchitectLayoutInterface;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.LayoutAnimator;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.Relationship;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.TablePane;

public class AutoLayoutAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(AutoLayoutAction.class);

	/**
	 * The PlayPen instance that owns this Action.
	 */
	private PlayPen pp;

	private boolean animationEnabled = true;

	private ArchitectLayoutInterface layout;

	private int framesPerSecond = 25;

	public AutoLayoutAction() {
		super("Auto Layout",
				  ASUtils.createIcon("AutoLayout",
									"Automatic Table Layout",
									ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Automatic Layout");
	}

	public void actionPerformed(ActionEvent evt) {
        try {
            layout = layout.getClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

		if (layout != null)
		{

			List<TablePane> tablePanes = new ArrayList(pp.getSelectedTables());
            List<TablePane> notLaidOut = new ArrayList<TablePane>(pp.getTablePanes());
            notLaidOut.removeAll(tablePanes);
 			Point layoutAreaOffset = new Point();
			if (tablePanes.size() == 0 || tablePanes.size() == 1) {
				tablePanes = pp.getTablePanes();
			} else if (tablePanes.size() != pp.getTablePanes().size()){
				int maxWidth =0;
				for (TablePane tp : notLaidOut){
					int width = tp.getWidth()+tp.getX();
					if (width > maxWidth) {
						maxWidth = width;
					}
				}
				layoutAreaOffset = new Point(maxWidth,0);
			}

			List<Relationship> relationships = pp.getRelationships();
			logger.debug("About to do layout. tablePanes="+tablePanes);
			logger.debug("About to do layout. relationships="+relationships);


			Rectangle layoutArea = new Rectangle(layoutAreaOffset,layout.getNewArea(tablePanes));
			layout.setup(tablePanes,relationships,layoutArea);
            LayoutAnimator anim = new LayoutAnimator(pp,layout);
            anim.setAnimationEnabled(animationEnabled);
            anim.setFramesPerSecond(framesPerSecond);
			anim.startAnimation();
		}

	}


	public void setPlayPen(PlayPen pp) {
		this.pp = pp;
	}

	public PlayPen getPlayPen() {
		return pp;
	}

	public boolean isAnimationEnabled() {
		return animationEnabled;
	}

	public void setAnimationEnabled(boolean animationEnabled) {
		this.animationEnabled = animationEnabled;
	}

	public ArchitectLayoutInterface getLayout() {
		return layout;
	}

	public void setLayout(ArchitectLayoutInterface layout) {
		this.layout = layout;
	}
}