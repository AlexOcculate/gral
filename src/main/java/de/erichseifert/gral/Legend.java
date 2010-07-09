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

package de.erichseifert.gral;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Dimension2D;
import java.util.HashMap;
import java.util.Map;

import de.erichseifert.gral.DrawableConstants.Location;
import de.erichseifert.gral.DrawableConstants.Orientation;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.plots.Label;
import de.erichseifert.gral.util.GraphicsUtils;
import de.erichseifert.gral.util.Insets2D;
import de.erichseifert.gral.util.SettingChangeEvent;
import de.erichseifert.gral.util.Settings;
import de.erichseifert.gral.util.SettingsListener;
import de.erichseifert.gral.util.SettingsStorage;
import de.erichseifert.gral.util.Settings.Key;


/**
 * <p>Abstract class that serves as a basic for any legend in a plot.
 * It provides an inner Item class which is responsible for
 * displaying a specific DataSource.</p>
 * <p>The functionality includes:</p>
 * <ul>
 *   <li>Storing and retrieving settings</li>
 *   <li>Adding and removing DataSources</li>
 * </ul>
 */
public abstract class Legend extends DrawableContainer
		implements SettingsStorage, SettingsListener {
	/** Key for specifying the {@link java.awt.Paint} instance to be used to
	 paint the background. */
	public static final Key BACKGROUND = new Key("legend.background");
	/** Key for specifying the {@link java.awt.Stroke} instance to be used to
	 paint the border of the legend. */
	public static final Key BORDER = new Key("legend.border");
	/** Key for specifying the {@link java.awt.Paint} instance to be used to
	 fill the border of the legend. */
	public static final Key COLOR = new Key("legend.color");
	/** Key for specifying the orientation of the legend using a
	 {@link de.erichseifert.gral.DrawableConstants.Orientation} value. */
	public static final Key ORIENTATION = new Key("legend.orientation");
	/** Key for specifying the gap between items. */
	public static final Key GAP = new Key("legend.gap");
	/** Key for specifying the gap between items. */
	public static final Key SYMBOL_SIZE = new Key("legend.symbol.size");

	private final Settings settings;

	private final Map<DataSource, Drawable> components;

	/**
	 * Class that displays a specific DataSource as an item of a Legend.
	 */
	protected class Item extends DrawableContainer {
		private final DataSource data;
		private final Drawable symbol;
		private final Label label;

		/**
		 * Creates a new Item object with the specified DataSource and text.
		 * @param data DataSource to be displayed.
		 * @param labelText Description text.
		 */
		public Item(final DataSource data, final String labelText) {
			super(new EdgeLayout(10.0, 0.0));
			this.data = data;

			symbol = new AbstractDrawable() {
				@Override
				public void draw(DrawingContext context) {
					drawSymbol(context, this, Item.this.data);
				}

				@Override
				public Dimension2D getPreferredSize() {
					final double fontSize = 10.0;  // TODO: Use real font size
					Dimension2D symbolSize = Legend.this.getSetting(SYMBOL_SIZE);
					Dimension2D size = super.getPreferredSize();
					size.setSize(symbolSize.getWidth()*fontSize, symbolSize.getHeight()*fontSize);
					return size;
				}
			};
			label = new Label(labelText);
			label.setSetting(Label.ALIGNMENT_X, 0.0);
			label.setSetting(Label.ALIGNMENT_Y, 0.5);

			add(symbol, Location.WEST);
			add(label, Location.CENTER);
		}

		@Override
		public Dimension2D getPreferredSize() {
			return getLayout().getPreferredSize(this);
		}

		/**
		 * Returns the displayed DataSource.
		 * @return Displayed DataSource
		 */
		public DataSource getData() {
			return data;
		}
	}

	/**
	 * Creates a new Legend object with default background color, border,
	 * orientation and gap between the Items.
	 */
	public Legend() {
		components = new HashMap<DataSource, Drawable>();
		setInsets(new Insets2D.Double(10.0));

		settings = new Settings(this);
		setSettingDefault(BACKGROUND, Color.WHITE);
		setSettingDefault(BORDER, new BasicStroke(1f));
		setSettingDefault(COLOR, Color.BLACK);
		setSettingDefault(ORIENTATION, Orientation.VERTICAL);
		setSettingDefault(GAP, new de.erichseifert.gral.util.Dimension2D.Double(20.0, 5.0));
		setSettingDefault(SYMBOL_SIZE, new de.erichseifert.gral.util.Dimension2D.Double(2.0, 2.0));
	}

	@Override
	public void draw(DrawingContext context) {
		drawBackground(context);
		drawBorder(context);
		drawComponents(context);
	}

	/**
	 * Draws the background of this Legend with the specified Graphics2D
	 * object.
	 * @param context Environment used for drawing.
	 */
	protected void drawBackground(DrawingContext context) {
		Paint bg = getSetting(BACKGROUND);
		if (bg != null) {
			GraphicsUtils.fillPaintedShape(context.getGraphics(), getBounds(), bg, null);
		}
	}

	/**
	 * Draws the border of this Legend with the specified Graphics2D
	 * object.
	 * @param context Environment used for drawing.
	 */
	protected void drawBorder(DrawingContext context) {
		Stroke stroke = getSetting(BORDER);
		if (stroke != null) {
			Paint fg = getSetting(COLOR);
			GraphicsUtils.drawPaintedShape(context.getGraphics(), getBounds(), fg, null, stroke);
		}
	}

	/**
	 * Draws the symbol of a certain data source.
	 * @param context Settings for drawing.
	 * @param symbol symbol to draw.
	 * @param data Data source.
	 */
	protected abstract void drawSymbol(
			DrawingContext context,
			Drawable symbol, DataSource data);

	/**
	 * Adds the specified DataSource in order to display it.
	 * @param source DataSource to be added.
	 */
	public void add(DataSource source) {
		Item item = new Item(source, source.toString());
		add(item);
		components.put(source, item);
	}

	/**
	 * Returns whether the specified DataSource was added to the legend.
	 * @param source Data source
	 * @return <code>true</code> if legend contains the data source, otherwise <code>false</code>
	 */
	public boolean contains(DataSource source) {
		return components.containsKey(source);
	}

	/**
	 * Removes the specified DataSource.
	 * @param source DataSource to be removed.
	 */
	public void remove(DataSource source) {
		Drawable removeItem = components.get(source);
		if (removeItem != null) {
			remove(removeItem);
		}
		components.remove(source);
	}

	/**
	 * Invoked if data has changed.
	 */
	protected void notifyDataChanged() {
		// FIXME: is this function needed?
		layout();
	}

	@Override
	public <T> T getSetting(Key key) {
		return (T) settings.get(key);
	}

	@Override
	public <T> void removeSetting(Key key) {
		settings.remove(key);
	}

	@Override
	public <T> void removeSettingDefault(Key key) {
		settings.removeDefault(key);
	}

	@Override
	public <T> void setSetting(Key key, T value) {
		settings.set(key, value);
	}

	@Override
	public <T> void setSettingDefault(Key key, T value) {
		settings.setDefault(key, value);
	}

	@Override
	public void settingChanged(SettingChangeEvent event) {
		Key key = event.getKey();
		if (ORIENTATION.equals(key) || GAP.equals(key)) {
			Orientation orientation = getSetting(ORIENTATION);
			Dimension2D gap = getSetting(GAP);
			Layout layout = new StackedLayout(orientation, gap);
			setLayout(layout);
		}
	}

}
