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
package de.erichseifert.gral.plots.lines;

import static de.erichseifert.gral.TestUtils.assertEmpty;
import static de.erichseifert.gral.TestUtils.assertNotEmpty;
import static de.erichseifert.gral.TestUtils.createTestImage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.erichseifert.gral.TestUtils;
import de.erichseifert.gral.graphics.Drawable;
import de.erichseifert.gral.graphics.DrawingContext;
import de.erichseifert.gral.plots.DataPoint;
import de.erichseifert.gral.util.PointND;

public class SmoothLineRendererTest {

	@Test
	public void testLine() {
		// Get line
		LineRenderer r = new SmoothLineRenderer2D();
		List<DataPoint> points = Arrays.asList(
			new DataPoint(new PointND<Double>(0.0, 0.0), null, null),
			new DataPoint(new PointND<Double>(1.0, 1.0), null, null)
		);
		r.setSetting(SmoothLineRenderer2D.SMOOTHNESS, 0.5);
		Drawable line = r.getLine(points);
		assertNotNull(line);

		// Draw line
		BufferedImage image = createTestImage();
		DrawingContext context = new DrawingContext((Graphics2D) image.getGraphics());
		line.draw(context);
		assertNotEmpty(image);
	}

	@Test
	public void testNullPoint() {
		// Get line
		LineRenderer r = new SmoothLineRenderer2D();
		List<DataPoint> points = Arrays.asList((DataPoint) null);
		Drawable line = r.getLine(points);
		assertNotNull(line);

		// Draw line
		BufferedImage image = createTestImage();
		DrawingContext context = new DrawingContext((Graphics2D) image.getGraphics());
		line.draw(context);
		assertEmpty(image);
	}

	@Test
	public void testEmptyShape() {
		// Get line
		LineRenderer r = new SmoothLineRenderer2D();
		List<DataPoint> points = Arrays.asList();
		Drawable line = r.getLine(points);
		assertNotNull(line);

		// Draw line
		BufferedImage image = createTestImage();
		DrawingContext context = new DrawingContext((Graphics2D) image.getGraphics());
		line.draw(context);
		assertEmpty(image);
	}

	@Test
	public void testSettings() {
		// Get
		LineRenderer r = new SmoothLineRenderer2D();
		assertEquals(Color.BLACK, r.getSetting(LineRenderer.COLOR));
		// Set
		r.setSetting(LineRenderer.COLOR, Color.RED);
		assertEquals(Color.RED, r.getSetting(LineRenderer.COLOR));
		// Remove
		r.removeSetting(LineRenderer.COLOR);
		assertEquals(Color.BLACK, r.getSetting(LineRenderer.COLOR));
	}

	@Test
	public void testSerialization() throws IOException, ClassNotFoundException {
		LineRenderer original = new SmoothLineRenderer2D();
		LineRenderer deserialized = TestUtils.serializeAndDeserialize(original);

		TestUtils.assertSettings(original, deserialized);
    }
}