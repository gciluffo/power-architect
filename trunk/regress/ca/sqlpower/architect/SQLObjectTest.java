/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SQLObjectTest extends SQLTestCase {

	public SQLObjectTest(String name) throws Exception {
        super(name);
    }

    SQLObject target;
	
	private static class SQLObjectImpl extends SQLObject {
	    protected boolean allowsChildren;
		SQLObjectImpl() {
			children = new ArrayList();
		}
		SQLObject parent = null;

		@Override
		public SQLObject getParent() {
			return parent;
		}
		@Override
		protected void setParent(SQLObject parent) {
			this.parent = parent;
		}
		@Override
		protected void populate() throws ArchitectException {
			// System.err.println("Abstract test stub populate() invoked");
		}
		@Override
		public String getShortDisplayName() {
            return "short display name";
		}
		@Override
		public boolean allowsChildren() {
			//throw new RuntimeException("test abstract stub");
			return allowsChildren;	 // Used by setChildren().
		}
		
		// manually call fireDbObjecChanged, so it can be tested.
		public void fakeObjectChanged(String string,Object oldValue, Object newValue) {
			
			fireDbObjectChanged(string,oldValue,newValue);
		}
		
		// manually call fireDbStructureChanged, so it can be tested.
		public void fakeStructureChanged() {
			fireDbStructureChanged();
		}
		@Override
		public Class<? extends SQLObject> getChildType() {
			return SQLObject.class;
		}
	}
	
	public void setUp() throws Exception {
        super.setUp();
		target = new SQLObjectImpl();
	}
    
    @Override
    protected SQLObject getSQLObjectUnderTest() throws ArchitectException {
        return target;
    }

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.isPopulated()'
	 */
	public final void testIsPopulated() {
		assertFalse(target.isPopulated());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.setPopulated(boolean)'
	 */
	public final void testSetPopulated() {
		target.setPopulated(true);
		assertTrue(target.isPopulated());
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.setChildren(List)'
	 * Note that setChildren copies elements, does not assign the list, and
	 * getChildren returns an unmodifiable copy of the current list.
	 */
	public final void testAllChildHandlingMethods() throws ArchitectException {
		assertEquals(0, target.getChildCount());

		SQLObject x = new SQLObjectImpl();
		target.addChild(x);
		assertEquals(1, target.getChildCount());
		assertEquals(x, target.getChild(0));
		
		SQLObject y = new SQLObjectImpl();
		
		// Test addChild(int, SQLObject)
		target.addChild(0, y);
		assertEquals(y, target.getChild(0));
		assertEquals(x, target.getChild(1));
		
		target.removeChild(1);
		List<SQLObject> list2 = new LinkedList<SQLObject>();
		list2.add(y);
		assertEquals(list2, target.getChildren());
		
		target.removeChild(y);
		assertEquals(Collections.EMPTY_LIST, target.getChildren());
	}

	public final void testFiresAddEvent() throws ArchitectException {
		CountingSQLObjectListener l = new CountingSQLObjectListener();
		target.addSQLObjectListener(l);
		((SQLObjectImpl) target).allowsChildren = true;
		
		final SQLObjectImpl objectImpl = new SQLObjectImpl();
		target.addChild(objectImpl);
		assertEquals(1, l.getInsertedCount());
        assertEquals(0, l.getRemovedCount());
        assertEquals(0, l.getChangedCount());
        assertEquals(0, l.getStructureChangedCount());
    }

    public void testFireChangeEvent() throws Exception {
        CountingSQLObjectListener l = new CountingSQLObjectListener();
        target.addSQLObjectListener(l);

        ((SQLObjectImpl)target).fakeObjectChanged("fred","old value","new value");
        assertEquals(0, l.getInsertedCount());
        assertEquals(0, l.getRemovedCount());
        assertEquals(1, l.getChangedCount());
        assertEquals(0, l.getStructureChangedCount());
    }
    
    /** make sure "change" to same value doesn't fire useless event */
    public void testDontFireChangeEvent() throws Exception {
        CountingSQLObjectListener l = new CountingSQLObjectListener();
        target.addSQLObjectListener(l);

        ((SQLObjectImpl)target).fakeObjectChanged("fred","old value","old value");
        assertEquals(0, l.getInsertedCount());
        assertEquals(0, l.getRemovedCount());
        assertEquals(0, l.getChangedCount());
        assertEquals(0, l.getStructureChangedCount());
    }

    public void testFireStructureChangeEvent() throws Exception {
        CountingSQLObjectListener l = new CountingSQLObjectListener();
        target.addSQLObjectListener(l);
		((SQLObjectImpl)target).fakeStructureChanged();
        assertEquals(0, l.getInsertedCount());
        assertEquals(0, l.getRemovedCount());
        assertEquals(0, l.getChangedCount());
        assertEquals(1, l.getStructureChangedCount());
    }
    
    public void testAddRemoveListener() {
        CountingSQLObjectListener l = new CountingSQLObjectListener();
        
        target.addSQLObjectListener(l);
        assertEquals(1, target.getSQLObjectListeners().size());
		
        target.removeSQLObjectListener(l);
		assertEquals(0, target.getSQLObjectListeners().size());
	}
	
	public void testNoMixChildTypes() throws ArchitectException {
		target.addChild(new SQLExceptionNode(null, "everything is ok. don't panic."));
		try {
			target.addChild(new SQLObjectImpl());
			fail("Target didn't throw exception for mixing child types!");
		} catch (ArchitectException e) {
			// this is expected
		}
	}
	
	public void testAllowMixedChildrenThatAreSubclassesOfEachOther() throws ArchitectException {
		SQLObject subImpl = new SQLObjectImpl() {};
		target.addChild(new SQLObjectImpl());
		target.addChild(subImpl);
		
		// now test the other direction
		target.removeChild(0);
		target.addChild(new SQLObjectImpl());
        
        // test passes if no exceptions were thrown
	}
	
    public void testPreRemoveEventNoVeto() throws Exception {
        target.addChild(new SQLObjectImpl());

        CountingSQLObjectPreEventListener l = new CountingSQLObjectPreEventListener();
        target.addSQLObjectPreEventListener(l);
        
        l.setVetoing(false);
        
        target.removeChild(0);
        
        assertEquals("Event fired", 1, l.getPreRemoveCount());
        assertEquals("Child removed", 0, target.getChildren().size());
    }
    
    public void testPreRemoveEventVeto() throws Exception {
        target.addChild(new SQLObjectImpl());

        CountingSQLObjectPreEventListener l = new CountingSQLObjectPreEventListener();
        target.addSQLObjectPreEventListener(l);
        
        l.setVetoing(true);
        
        target.removeChild(0);
        
        assertEquals("Event fired", 1, l.getPreRemoveCount());
        assertEquals("Child not removed", 1, target.getChildren().size());
    }

}