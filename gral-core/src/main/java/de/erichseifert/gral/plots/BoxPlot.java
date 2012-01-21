/*
 * GRAL: GRAphing Library for Java(R)
 *
 * (C) Copyright 2009-2012 Erich Seifert <dev[at]erichseifert.de>,
 * Michael Seifert <michael[at]erichseifert.de>
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
package de.erichseifert.gral.plots;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import de.erichseifert.gral.data.Column;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.data.Row;
import de.erichseifert.gral.data.statistics.Statistics;
import de.erichseifert.gral.graphics.AbstractDrawable;
import de.erichseifert.gral.graphics.Drawable;
import de.erichseifert.gral.graphics.DrawingContext;
import de.erichseifert.gral.plots.axes.Axis;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.colors.ColorMapper;
import de.erichseifert.gral.plots.colors.ContinuousColorMapper;
import de.erichseifert.gral.plots.colors.SingleColor;
import de.erichseifert.gral.plots.points.AbstractPointRenderer;
import de.erichseifert.gral.plots.points.PointData;
import de.erichseifert.gral.plots.settings.Key;
import de.erichseifert.gral.util.GraphicsUtils;
import de.erichseifert.gral.util.PointND;


/**
 * <p>Class that displays data as a box-and-whisker plot showing summaries of
 * important statistical values. The data source must provide six columns to
 * the {@code BoxPlot}:<p>
 * <ul>
 *   <li>Box position (for multiple boxes)</li>
 *   <li>Position of the center bar (e.g. median)</li>
 *   <li>Length of the lower whisker and position of the bottom bar
 *   (e.g. minimum)</li>
 *   <li>Position of the bottom edge of the box (e.g. first quartile)</li>
 *   <li>Position of the top edge of the box (e.g. third quartile)</li>
 *   <li>Length of the upper whisker and position of the top bar
 *   (e.g. maximum)</li>
 * </li>
 * <p>The utility method {@link #createBoxData(DataSource)} can be used to
 * obtain common statistics for these properties from the each column of an
 * existing data source.</p>
 *
 * <p>To create a new {@code BoxPlot} simply create a new instance using
 * a data source. Example:</p>
 * <pre>
 * DataTable data = new DataTable(Double.class, Double.class);
 * data.add(10.98, -12.34);
 * data.add( 7.65,  45.67);
 * data.add(43.21,  89.01);
 * DataSource boxData = BoxPlot.createBoxData(data);
 * BoxPlot plot = new BoxPlot(boxData);
 * </pre>
 */
public class BoxPlot extends XYPlot {
	/** Version id for serialization. */
	private static final long serialVersionUID = -3069831535208696337L;

	/**
	 * Class that renders a box and its whiskers in a box-and-whisker plot.
	 */
	public static class BoxWhiskerRenderer extends AbstractPointRenderer {
		/** Version id for serialization. */
		private static final long serialVersionUID = 2944482729753981341L;

		/** Key for specifying the {@link Integer} value which specifies the
		index of the column that is used for the horizontal position of a box. */
		public static final Key COLUMN_POSITION =
			new Key("boxplot.position.column"); //$NON-NLS-1$
		/** Key for specifying the {@link Integer} value which specifies the
		index of the column that is used for the center bar. */
		public static final Key COLUMN_BAR_CENTER =
			new Key("boxplot.bar.center.column"); //$NON-NLS-1$
		/** Key for specifying the {@link Integer} value which specifies the
		index of the column that is used for the bottom bar. */
		public static final Key COLUMN_BAR_BOTTOM =
			new Key("boxplot.bar.bottom.column"); //$NON-NLS-1$
		/** Key for specifying the {@link Integer} value which specifies the
		index of the column that is used for the bottom edge of the box. */
		public static final Key COLUMN_BOX_BOTTOM =
			new Key("boxplot.box.bottom.column"); //$NON-NLS-1$
		/** Key for specifying the {@link Integer} value which specifies the
		index of the column that is used for the bottom edge of the box. */
		public static final Key COLUMN_BOX_TOP =
			new Key("boxplot.box.top.column"); //$NON-NLS-1$
		/** Key for specifying the {@link Integer} value which specifies the
		index of the column that is used for the top bar. */
		public static final Key COLUMN_BAR_TOP =
			new Key("boxplot.bar.top.column"); //$NON-NLS-1$
		/** Key for specifying a {@link Number} value for the relative width
		of the box. */
		public static final Key BOX_WIDTH =
			new Key("boxplot.box.width"); //$NON-NLS-1$
		/** Key for specifying an instance of
		{@link de.erichseifert.gral.plots.colors.ColorMapper} that will be used
		to paint the background of the box. */
		public static final Key BOX_BACKGROUND =
			new Key("boxplot.box.background"); //$NON-NLS-1$
		/** Key for specifying the {@link java.awt.Paint} instance to be used
		to paint the border of the box and the lines of bars. */
		public static final Key BOX_COLOR =
			new Key("boxplot.box.color"); //$NON-NLS-1$
		/** Key for specifying the {@link java.awt.Stroke} instance to be used
		to paint the border of the box and the lines of the bars. */
		public static final Key BOX_BORDER =
			new Key("boxplot.box.border"); //$NON-NLS-1$
		/** Key for specifying the {@link java.awt.Paint} instance to be used
		to paint the lines of the whiskers. */
		public static final Key WHISKER_COLOR =
			new Key("boxplot.whisker.color"); //$NON-NLS-1$
		/** Key for specifying the {@link java.awt.Stroke} instance to be used
		to paint the lines of the whiskers. */
		public static final Key WHISKER_STROKE =
			new Key("boxplot.whisker.stroke"); //$NON-NLS-1$
		/** Key for specifying a {@link Number} value for the relative width of
		the bottom and top bars. */
		public static final Key BAR_WIDTH =
			new Key("boxplot.bar.width"); //$NON-NLS-1$
		/** Key for specifying the {@link java.awt.Paint} instance to be used
		to paint the lines of the center bar. */
		public static final Key BAR_CENTER_COLOR =
			new Key("boxplot.bar.center.color"); //$NON-NLS-1$
		/** Key for specifying the {@link java.awt.Stroke} instance to be used
		to paint the lines of the center bar. */
		public static final Key BAR_CENTER_STROKE =
			new Key("boxplot.bar.center.stroke"); //$NON-NLS-1$

		/**
		 * Constructor that creates a new instance and initializes it with a
		 * plot as data provider.
		 */
		public BoxWhiskerRenderer() {
			setSettingDefault(COLUMN_POSITION, 0);
			setSettingDefault(COLUMN_BAR_CENTER, 1);
			setSettingDefault(COLUMN_BAR_BOTTOM, 2);
			setSettingDefault(COLUMN_BOX_BOTTOM, 3);
			setSettingDefault(COLUMN_BOX_TOP, 4);
			setSettingDefault(COLUMN_BAR_TOP, 5);
			setSettingDefault(BOX_WIDTH, 0.75);
			setSettingDefault(BOX_BACKGROUND, Color.WHITE);
			setSettingDefault(BOX_COLOR, Color.BLACK);
			setSettingDefault(BOX_BORDER, new BasicStroke(1f));
			setSettingDefault(WHISKER_COLOR, Color.BLACK);
			setSettingDefault(WHISKER_STROKE, new BasicStroke(1f));
			setSettingDefault(BAR_WIDTH, 0.75);
			setSettingDefault(BAR_CENTER_COLOR, Color.BLACK);
			setSettingDefault(BAR_CENTER_STROKE, new BasicStroke(
				2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
		}

		/**
		 * Returns the graphical representation to be drawn for the specified
		 * data value.
		 * @param data Information on axes, renderers, and values.
		 * @param shape Outline that describes the point's shape.
		 * @return Component that can be used to draw the point
		 */
		public Drawable getPoint(final PointData data, final Shape shape) {
			return new AbstractDrawable() {
				/** Version id for serialization. */
				private static final long serialVersionUID = 2765031432328349977L;

				public void draw(DrawingContext context) {
					Axis axisX = data.axes.get(0);
					Axis axisY = data.axes.get(1);
					AxisRenderer axisXRenderer = data.axisRenderers.get(0);
					AxisRenderer axisYRenderer = data.axisRenderers.get(1);
					Row row = data.row;

					// Get the values from data columns
					BoxWhiskerRenderer renderer =  BoxWhiskerRenderer.this;
					int colPos = renderer.<Integer>getSetting(COLUMN_POSITION);
					int colBarCenter = renderer.<Integer>getSetting(COLUMN_BAR_CENTER);
					int colBarBottom = renderer.<Integer>getSetting(COLUMN_BAR_BOTTOM);
					int colBoxBottom = renderer.<Integer>getSetting(COLUMN_BOX_BOTTOM);
					int colBoxTop = renderer.<Integer>getSetting(COLUMN_BOX_TOP);
					int colBarTop = renderer.<Integer>getSetting(COLUMN_BAR_TOP);

					if (!row.isColumnNumeric(colPos) ||
							!row.isColumnNumeric(colBarCenter) ||
							!row.isColumnNumeric(colBarBottom) ||
							!row.isColumnNumeric(colBoxBottom) ||
							!row.isColumnNumeric(colBoxTop) ||
							!row.isColumnNumeric(colBarTop)) {
						return;
					}

					double valueX = ((Number) row.get(colPos)).doubleValue();
					double valueYBarBottom = ((Number) row.get(colBarBottom)).doubleValue();
					double valueYBoxBottom = ((Number) row.get(colBoxBottom)).doubleValue();
					double valueYBarCenter = ((Number) row.get(colBarCenter)).doubleValue();
					double valueYBoxTop = ((Number) row.get(colBoxTop)).doubleValue();
					double valueYBarTop = ((Number) row.get(colBarTop)).doubleValue();

					// Calculate positions in screen units
					double boxWidthRel = BoxWhiskerRenderer.this.
						<Number>getSetting(BOX_WIDTH).doubleValue();
					double boxAlign = 0.5;
					// Box X
					double boxXMin = axisXRenderer
						.getPosition(axisX, valueX - boxWidthRel*boxAlign, true, false)
						.get(PointND.X);
					double boxX = axisXRenderer.getPosition(
						axisX, valueX, true, false).get(PointND.X);
					double boxXMax = axisXRenderer
						.getPosition(axisX, valueX + boxWidthRel*boxAlign, true, false)
						.get(PointND.X);
					// Box Y
					double barYbottom = axisYRenderer.getPosition(
						axisY, valueYBarBottom, true, false).get(PointND.Y);
					double boxYBottom = axisYRenderer.getPosition(
						axisY, valueYBoxBottom, true, false).get(PointND.Y);
					double barYCenter = axisYRenderer.getPosition(
						axisY, valueYBarCenter, true, false).get(PointND.Y);
					double boxYTop = axisYRenderer.getPosition(
						axisY, valueYBoxTop, true, false).get(PointND.Y);
					double barYTop = axisYRenderer.getPosition(
						axisY, valueYBarTop, true, false).get(PointND.Y);
					double boxWidth = Math.abs(boxXMax - boxXMin);
					// Bars
					double barWidthRel = BoxWhiskerRenderer.this.
						<Number>getSetting(BAR_WIDTH).doubleValue();
					double barXMin = boxXMin + (1.0 - barWidthRel)*boxWidth/2.0;
					double barXMax = boxXMax - (1.0 - barWidthRel)*boxWidth/2.0;

					// Create shapes
					// The origin of all shapes is (boxX, boxY)
					Rectangle2D boxBounds = new Rectangle2D.Double(
						boxXMin - boxX, boxYTop - barYCenter,
						boxWidth, Math.abs(boxYTop - boxYBottom));
					Rectangle2D shapeBounds = shape.getBounds2D();
					AffineTransform tx = new AffineTransform();
					tx.translate(boxBounds.getX(), boxBounds.getY());
					tx.scale(boxBounds.getWidth()/shapeBounds.getWidth(),
						boxBounds.getHeight()/shapeBounds.getHeight());
					tx.translate(-shapeBounds.getMinX(), -shapeBounds.getMinY());
					Shape box = tx.createTransformedShape(shape);

					Line2D whiskerMax = new Line2D.Double(
						0.0, boxYTop - barYCenter,
						0.0, barYTop - barYCenter
					);
					Line2D whiskerMin = new Line2D.Double(
						0.0, boxYBottom - barYCenter,
						0.0, barYbottom - barYCenter
					);
					Line2D barMax = new Line2D.Double(
						barXMin - boxX, barYTop - barYCenter,
						barXMax - boxX, barYTop - barYCenter
					);
					Line2D barMin = new Line2D.Double(
						barXMin - boxX, barYbottom - barYCenter,
						barXMax - boxX, barYbottom - barYCenter
					);
					Line2D barCenter = new Line2D.Double(
						boxXMin - boxX, 0.0,
						boxXMax - boxX, 0.0
					);

					// Paint shapes
					Graphics2D graphics = context.getGraphics();
					ColorMapper paintBoxMapper =
						BoxWhiskerRenderer.this.getSetting(BOX_BACKGROUND);
					Paint paintBox;
					if (paintBoxMapper instanceof ContinuousColorMapper) {
						paintBox = ((ContinuousColorMapper) paintBoxMapper)
							.get(valueX);
					} else {
						Integer index = Integer.valueOf(row.getIndex());
						paintBox = paintBoxMapper.get(index);
					}
					Paint paintStrokeBox =
						BoxWhiskerRenderer.this.getSetting(BOX_COLOR);
					Stroke strokeBox =
						BoxWhiskerRenderer.this.getSetting(BOX_BORDER);
					Paint paintWhisker =
						BoxWhiskerRenderer.this.getSetting(WHISKER_COLOR);
					Stroke strokeWhisker =
						BoxWhiskerRenderer.this.getSetting(WHISKER_STROKE);
					Paint paintBarCenter =
						BoxWhiskerRenderer.this.getSetting(BAR_CENTER_COLOR);
					Stroke strokeBarCenter =
						BoxWhiskerRenderer.this.getSetting(BAR_CENTER_STROKE);
					// Fill box
					GraphicsUtils.fillPaintedShape(
						graphics, box, paintBox, box.getBounds2D());
					// Save current graphics state
					Paint paintOld = graphics.getPaint();
					Stroke strokeOld = graphics.getStroke();
					// Draw whiskers
					graphics.setPaint(paintWhisker);
					graphics.setStroke(strokeWhisker);
					graphics.draw(whiskerMax);
					graphics.draw(whiskerMin);
					// Draw box and bars
					graphics.setPaint(paintStrokeBox);
					graphics.setStroke(strokeBox);
					graphics.draw(box);
					graphics.draw(barMax);
					graphics.draw(barMin);
					graphics.setPaint(paintBarCenter);
					graphics.setStroke(strokeBarCenter);
					graphics.draw(barCenter);
					// Restore previous graphics state
					graphics.setStroke(strokeOld);
					graphics.setPaint(paintOld);
				}
			};
		}

		/**
		 * Returns a {@code Shape} instance that can be used for further
		 * calculations.
		 * @param data Information on axes, renderers, and values.
		 * @return Outline that describes the point's shape.
		 */
		public Shape getPointShape(PointData data) {
			return getSetting(SHAPE);
		}

		@Override
		protected <T> void setSetting(Key key, T value, boolean isDefault) {
			// Be nice and automatically convert colors to color mappers
			if (value instanceof Paint && BOX_BACKGROUND.equals(key)) {
				super.setSetting(key, new SingleColor((Paint) value), isDefault);
			} else {
				super.setSetting(key, value, isDefault);
			}
		};
	}

	/**
	 * Initializes a new box-and-whisker plot with the specified data source.
	 * @param data Data to be displayed.
	 */
	public BoxPlot(DataSource data) {
		getPlotArea().setSettingDefault(XYPlotArea2D.GRID_MAJOR_X, false);
		getAxisRenderer(AXIS_X).setSetting(AxisRenderer.TICKS_SPACING, 1.0);
		getAxisRenderer(AXIS_X).setSetting(AxisRenderer.TICKS_MINOR, false);
		getAxisRenderer(AXIS_X).setSetting(AxisRenderer.INTERSECTION,
			-Double.MAX_VALUE);
		getAxisRenderer(AXIS_Y).setSetting(AxisRenderer.INTERSECTION,
			-Double.MAX_VALUE);

		add(data);
		autoScaleAxes();
	}

	/**
	 * Extracts statistics from the columns of an data source that are commonly
	 * used for box-and-whisker plots. The result is a new data source
	 * containing <i>column index</i>, <i>median</i>, <i>mininum</i>, <i>first
	 * quartile</i>, <i>third quartile</i>, and <i>maximum</i> for each column.
	 * @param data Original data source
	 * @return New data source with (columnIndex, median, min, quartile1,
	 *         quartile3, max)
	 */
	@SuppressWarnings("unchecked")
	public static DataSource createBoxData(DataSource data) {
		if (data == null) {
			throw new NullPointerException(
				"Cannot extract statistics from null data source.");
		}

		DataTable stats = new DataTable(Integer.class, Double.class,
			Double.class, Double.class, Double.class, Double.class);

		// Generate statistical values for each column
		for (int c = 0; c < data.getColumnCount(); c++) {
			Column col = data.getColumn(c);
			stats.add(
				c + 1,
				col.getStatistics(Statistics.MEDIAN),
				col.getStatistics(Statistics.MIN),
				col.getStatistics(Statistics.QUARTILE_1),
				col.getStatistics(Statistics.QUARTILE_3),
				col.getStatistics(Statistics.MAX)
			);
		}
		return stats;
	}

	@Override
	public void add(int index, DataSource source, boolean visible) {
		if (getData().size() > 0) {
			throw new IllegalArgumentException(
				"This plot type only supports a single data source."); //$NON-NLS-1$
		}
		super.add(index, source, visible);
		setLineRenderer(source, null);
		setPointRenderer(source, new BoxWhiskerRenderer());
	}

	@Override
	protected void autoScaleAxes() {
		for (DataSource data : getData()) {
			// Adjust axes to generated data series
			Column col0 = data.getColumn(0);
			getAxis(AXIS_X).setRange(
				col0.getStatistics(Statistics.MIN) - 0.5,
				col0.getStatistics(Statistics.MAX) + 0.5);
			double yMin = data.getColumn(2).getStatistics(Statistics.MIN);
			double yMax = data.getColumn(5).getStatistics(Statistics.MAX);
			double ySpacing = 0.05*(yMax - yMin);
			getAxis(AXIS_Y).setRange(yMin - ySpacing, yMax + ySpacing);
		}
	}
}