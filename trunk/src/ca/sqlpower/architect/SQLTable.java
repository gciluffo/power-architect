package ca.sqlpower.architect;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class SQLTable extends SQLObject {

	protected SQLDatabase parentDatabase;
	protected SQLObject parent;
	protected SQLCatalog catalog;
	protected SQLSchema schema;
	protected String tableName;
	protected String remarks;
	protected String objectType;

	/**
	 * A List of SQLRelationship objects describing keys that this
	 * table imports.
	 */
	protected List importedKeys;
	protected boolean columnsPopulated;
	protected String primaryKeyName;

	public SQLTable(SQLDatabase parentDb, SQLObject parent, SQLCatalog catalog, SQLSchema schema, String name, String remarks, String objectType) {
		this.parentDatabase = parentDb;
		this.parent = parent;
		this.catalog = catalog;
		this.schema = schema;
		this.tableName = name;
		this.remarks = remarks;
		this.columnsPopulated = false;
		this.objectType = objectType;

		this.children = new ArrayList();
	}
	
	/**
	 * Creates a new SQLTable with parent as its parent and a null
	 * schema and catalog.
	 */
	public SQLTable(SQLDatabase parent) {
		this(parent, parent, null, null, "", "", "TABLE");
	}

	protected static void addTablesToDatabase(SQLDatabase addTo) 
		throws SQLException, ArchitectException {
		HashMap catalogs = new HashMap();
		HashMap schemas = new HashMap();
		synchronized (addTo) {
			Connection con = addTo.getConnection();
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet mdTables = null;
			try {
				mdTables = dbmd.getTables(null,
										  null,
										  "%",
										  new String[] {"SYSTEM TABLE", "TABLE", "VIEW"});
				while (mdTables.next()) {
					SQLObject tableParent = addTo;

					String catName = mdTables.getString(1);
					SQLCatalog cat = null;

					if (catName != null) {
						cat = (SQLCatalog) catalogs.get(catName);
						if (cat == null) {
							cat = new SQLCatalog(addTo, catName);
							addTo.children.add(cat);
							catalogs.put(catName, cat);
						}
						tableParent = cat;
					}

					String schName = mdTables.getString(2);
					SQLSchema schema = null;
					if (schName != null) {
						schema = (SQLSchema) schemas.get(catName+"."+schName);
						if (schema == null) {
							if (cat == null) {
								schema = new SQLSchema(addTo, schName);
								addTo.children.add(schema);
							} else {
								schema = new SQLSchema(cat, schName);
								cat.children.add(schema);
							}
							schemas.put(catName+"."+schName, schema);
						}
						tableParent = schema;
					}

					tableParent.children.add(new SQLTable(addTo,
														  tableParent,
														  cat,
														  schema,
														  mdTables.getString(3),
														  mdTables.getString(5),
														  mdTables.getString(4)
														  ));
				}
			} finally {
				if (mdTables != null) mdTables.close();
			}
		}
	}

	/**
	 * Calls to this method should be synchronized, and should check
	 * that columnsPopulated == false.
	 */
	protected synchronized void populateColumns() throws ArchitectException {
		if (columnsPopulated) return;
		int oldSize = children.size();
		try {
			SQLColumn.addColumnsToTable(this,
										catalog == null ? null : catalog.getCatalogName(),
										schema == null ? null : schema.getSchemaName(),
										tableName);
			columnsPopulated = true;
		} catch (SQLException e) {
			throw new ArchitectException("table.populate", e);
		} finally {
			columnsPopulated = true;
			int newSize = children.size();
			if (newSize > oldSize) {
				int[] changedIndices = new int[newSize - oldSize];
				for (int i = 0, n = newSize - oldSize; i < n; i++) {
					changedIndices[i] = oldSize + i;
				}
				fireDbChildrenInserted(changedIndices, children.subList(oldSize, newSize));
			}
		}
	}

	public static SQLTable getDerivedInstance(SQLTable source, SQLDatabase parent)
		throws ArchitectException {
		SQLTable t = new SQLTable(parent);
		t.columnsPopulated = true;
		t.tableName = source.tableName;
		t.remarks = source.remarks;
		t.inherit(source);
		return t;
	}

	/**
	 * Adds all the columns of the given source table to this table.
	 */
	public void inherit(SQLTable source) throws ArchitectException {
		SQLColumn c;
		Iterator it = source.getChildren().iterator();
		while (it.hasNext()) {
			c = SQLColumn.getDerivedInstance((SQLColumn) it.next(), this);
			addChild(c);
		}
	}

	/**
	 * XXX: This could be speeded up with a hashset if needed.
	 */
	public SQLColumn getColumnByName(String colName) {
		Iterator it = children.iterator();
		while (it.hasNext()) {
			SQLColumn col = (SQLColumn) it.next();
			if (col.getColumnName().equals(colName)) {
				return col;
			}
		}
		return null;
	}

	public void addColumn(SQLColumn col) {
		addChild(col);
	}
	
	public String toString() {
		return getShortDisplayName();
	}

	// ---------------------- SQLObject support ------------------------

	public SQLObject getParent() {
		return parent;
	}

	/**
	 * The table's name.
	 */
	public String getShortDisplayName() {
		if (schema != null) {
			return schema.getSchemaName()+"."+tableName+" ("+objectType+")";
		} else {
			return tableName+" ("+objectType+")";
		}
	}

	/**
	 * Just calls populateColumns.
	 */
	public void populate() throws ArchitectException {
		populateColumns();
	}
	
	public boolean isPopulated() {
		return columnsPopulated;
	}

	/**
	 * Returns true (tables have columns as children).
	 */
	public boolean allowsChildren() {
		return true;
	}


	// ------------------ Accessors and mutators below this line ------------------------

	/**
	 * Gets the value of parentDatabase
	 *
	 * @return the value of parentDatabase
	 */
	public SQLDatabase getParentDatabase()  {
		return this.parentDatabase;
	}

	/**
	 * Sets the value of parentDatabase
	 *
	 * @param argParentDatabase Value to assign to this.parentDatabase
	 */
	public void setParentDatabase(SQLDatabase argParentDatabase) {
		this.parentDatabase = argParentDatabase;
		// XXX: fire event?
	}

	public SQLCatalog getCatalog()  {
		return this.catalog;
	}

	protected void setCatalog(SQLCatalog argCatalog) {
		this.catalog = argCatalog;
		// XXX: fire event?
	}

	public SQLSchema getSchema()  {
		return this.schema;
	}

	protected void setSchema(SQLSchema argSchema) {
		this.schema = argSchema;
		// XXX: fire event?
	}

	/**
	 * Gets the value of tableName
	 *
	 * @return the value of tableName
	 */
	public String getTableName()  {
		return this.tableName;
	}

	/**
	 * Sets the value of tableName
	 *
	 * @param argTableName Value to assign to this.tableName
	 */
	public void setTableName(String argTableName) {
		this.tableName = argTableName;
		fireDbObjectChanged("tableName");
	}

	/**
	 * Gets the value of remarks
	 *
	 * @return the value of remarks
	 */
	public String getRemarks()  {
		return this.remarks;
	}

	/**
	 * Sets the value of remarks
	 *
	 * @param argRemarks Value to assign to this.remarks
	 */
	public void setRemarks(String argRemarks) {
		this.remarks = argRemarks;
		fireDbObjectChanged("remarks");
	}

	/**
	 * Gets the value of columns
	 *
	 * @return the value of columns
	 */
	public synchronized List getColumns() throws ArchitectException {
		return getChildren();
	}

	// 	/**
	// 	 * Sets the value of columns
	// 	 *
	// 	 * @param argColumns Value to assign to this.columns
	// 	 */
	// 	public synchronized void setColumns(List argColumns) {
	// 		List old = columns;
	// 		this.columns = argColumns;
	// 		columnsPopulated = true;
	// 		firePropertyChange("children", old, columns);
	// 	}

	/**
	 * Gets the value of importedKeys
	 *
	 * @return the value of importedKeys
	 */
	public List getImportedKeys()  {
		return this.importedKeys;
	}

	/**
	 * Sets the value of importedKeys
	 *
	 * @param argImportedKeys Value to assign to this.importedKeys
	 */
	public void setImportedKeys(List argImportedKeys) {
		this.importedKeys = argImportedKeys;
		fireDbObjectChanged("importedKeys");
	}

	/**
	 * Gets the value of columnsPopulated
	 *
	 * @return the value of columnsPopulated
	 */
	public boolean isColumnsPopulated()  {
		return this.columnsPopulated;
	}

	/**
	 * Sets the value of columnsPopulated
	 *
	 * @param argColumnsPopulated Value to assign to this.columnsPopulated
	 */
	protected void setColumnsPopulated(boolean argColumnsPopulated) {
		this.columnsPopulated = argColumnsPopulated;
	}

	/**
	 * Gets the value of primaryKeyName
	 *
	 * @return the value of primaryKeyName
	 */
	public String getPrimaryKeyName()  {
		return this.primaryKeyName;
	}

	/**
	 * Sets the value of primaryKeyName
	 *
	 * @param argPrimaryKeyName Value to assign to this.primaryKeyName
	 */
	public void setPrimaryKeyName(String argPrimaryKeyName) {
		this.primaryKeyName = argPrimaryKeyName;
		fireDbObjectChanged("primaryKeyName");
	}
	
	
	/**
	 * Gets the value of objectType
	 *
	 * @return the value of objectType
	 */
	public String getObjectType()  {
		return this.objectType;
	}

	/**
	 * Sets the value of objectType
	 *
	 * @param argObjectType Value to assign to this.objectType
	 */
	public void setObjectType(String argObjectType) {
		this.objectType = argObjectType;
		fireDbObjectChanged("objectType");
	}

}
