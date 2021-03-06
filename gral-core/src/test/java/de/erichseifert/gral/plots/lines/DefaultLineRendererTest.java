/*
 * GRAL: GRAphing Library for Java(R)
 *
 * (C) Copyright 2009-2016 Erich Seifert <dev[at]erichseifert.de>,
 * Michael Seifert <mseifert[at]error-reports.org>
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
package de.erichseifert.gral.plots.lines;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static de.erichseifert.gral.TestUtils.assertNotEmpty;
import static de.erichseifert.gral.TestUtils.createTestImage;
import static org.junit.Assert.assertNotNull;

import de.erichseifert.gral.TestUtils;
import de.erichseifert.gral.graphics.Drawable;
import de.erichseifert.gral.graphics.DrawingContext;
import de.erichseifert.gral.plots.DataPoint;
import de.erichseifert.gral.plots.axes.Axis;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.axes.LinearRenderer2D;
import de.erichseifert.gral.plots.points.PointData;
import de.erichseifert.gral.util.PointND;
import org.junit.Before;
import org.junit.Test;

public class DefaultLineRendererTest {
	private PointData data;

	@Before
	public void setUp() {
		Axis axisX = new Axis(-5.0, 5.0);
		Axis axisY = new Axis(-5.0, 5.0);
		AxisRenderer axisRendererX = new LinearRenderer2D();
		AxisRenderer axisRendererY = new LinearRenderer2D();
		data = new PointData(
			Arrays.asList(axisX, axisY),
			Arrays.asList(axisRendererX, axisRendererY),
			null, 0);
	}

	@Test
	public void testLine() {
		// Get line
		LineRenderer r = new DefaultLineRenderer2D();
		List<DataPoint> points = Arrays.asList(
			new DataPoint(data, new PointND<Double>(0.0, 0.0)),
			new DataPoint(data, new PointND<Double>(1.0, 1.0))
		);
		Shape shape = r.getLineShape(points);
		Drawable line = r.getLine(points, shape);
		assertNotNull(line);

		// Draw line
		BufferedImage image = createTestImage();
		DrawingContext context = new DrawingContext((Graphics2D) image.getGraphics());
		line.draw(context);
		assertNotEmpty(image);
	}

	@Test
	public void testGap() {
		LineRenderer r = new DefaultLineRenderer2D();
		List<DataPoint> points = Arrays.asList(
			new DataPoint(data, new PointND<Double>(0.0, 0.0)),
			new DataPoint(data, new PointND<Double>(1.0, 1.0))
		);

		List<Double> gaps = Arrays.asList(Double.NaN, 0.0, 1.0);
		List<Boolean> roundeds = Arrays.asList(false, true);

		// Test different gap sizes
		for (Double gap : gaps) {
			r.setGap(gap);

			// Draw non-rounded and non rounded gaps
			for (Boolean rounded : roundeds) {
				r.setGapRounded(rounded);

				Shape shape = r.getLineShape(points);
				Drawable line = r.getLine(points, shape);
				assertNotNull(line);

				BufferedImage image = createTestImage();
				DrawingContext context = new DrawingContext((Graphics2D) image.getGraphics());
				line.draw(context);
				assertNotEmpty(image);
			}
		}
	}

	@Test
	public void testSerialization() throws IOException, ClassNotFoundException {
		LineRenderer original = new DefaultLineRenderer2D();
		@SuppressWarnings("unused")
		LineRenderer deserialized = TestUtils.serializeAndDeserialize(original);
    }
}
