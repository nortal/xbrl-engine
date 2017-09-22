package com.nortal.xbrl.metamodel.meta;

import java.util.List;

/**
 * Linked entry interface is used to link presentation, calculation and dimension entries for the same field.
 */
public interface LinkedEntry {

	/**
	 * Get linked entry namespace.
	 * 
	 * @return linked entry namespace
	 */
	String getNamespace();

	/**
	 * Get linked entry namespace prefix.
	 * 
	 * @return linked entry namespace prefix
	 */
	String getNamespacePrefix();

	/**
	 * Get linked entry name.
	 * 
	 * @return linked entry name
	 */
	String getName();

	/**
	 * Get child list.
	 * 
	 * @return child list
	 */
	List<? extends LinkedEntry> getChildren();

	/**
	 * Get linked entry from current entry child list.
	 * 
	 * @param linkedEntry linked entry to search for
	 * @return child linked entry
	 */
	LinkedEntry getChild(LinkedEntry linkedEntry);

	/**
	 * Check if two entries are actually linked, refer to the same field.
	 * 
	 * @param linkedEntry linked entry to check against
	 * @return true if two entries are linked, have the same namespace and name.
	 */
	boolean isLinked(LinkedEntry linkedEntry);

}
