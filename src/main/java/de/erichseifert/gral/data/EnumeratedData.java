/*
 * GRAL: GRAphing Library for Java(R)
 *
 * (C) Copyright 2009-2010 Erich Seifert <info[at]erichseifert.de>, Michael Seifert <michael.seifert[at]gmx.net>
 *
 * This file is part of GRAL.
 *
 * GRAL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GRAL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GRAL.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.erichseifert.gral.data;

/**
 * Class that creates a new data source which adds a leading column containing
 * the row number.  
 */
public class EnumeratedData extends AbstractDataSource {
	private DataSource original;
	
	/**
	 * Initializes a new data source with an original data source.
	 * @param original Original data source.
	 */
	public EnumeratedData(DataSource original) {
		this.original = original;
	}

	@Override
	public Number get(int col, int row) {
		if (col < 1) {
			return row;
		}
		return original.get(col - 1, row);
	}

	@Override
	public int getColumnCount() {
		return original.getColumnCount() + 1;
	}

	@Override
	public int getRowCount() {
		return original.getRowCount();
	}

}
