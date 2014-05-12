/*
 * GRAL: GRAphing Library for Java(R)
 *
 * (C) Copyright 2009-2014 Erich Seifert <dev[at]erichseifert.de>,
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
package de.erichseifert.gral.graphics;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import de.erichseifert.gral.navigation.DrawableContainerNavigator;
import de.erichseifert.gral.navigation.Navigable;
import de.erichseifert.gral.util.PointND;

public class NavigableDrawableContainer extends DrawableContainer implements Navigable<DrawableContainerNavigator> {
	private final DrawableContainerNavigator navigator;

	public NavigableDrawableContainer() {
		navigator = new DrawableContainerNavigator(this);
	}

	@Override
	protected void drawComponents(DrawingContext context) {
		Graphics2D g2d = context.getGraphics();
		AffineTransform txOld = g2d.getTransform();
		double zoom = navigator.getZoom();

		Point2D origin = navigator.getCenter().getPoint2D();
		g2d.scale(zoom, zoom);
		g2d.translate(-origin.getX(), -origin.getY());
		super.drawComponents(context);
		g2d.setTransform(txOld);
	}

	@Override
	public DrawableContainerNavigator getNavigator() {
		return navigator;
	}

	@Override
	public List<Drawable> getDrawablesAt(Point2D point) {
		DrawableContainerNavigator navigator = getNavigator();
		PointND<? extends Number> pointZoomed = navigator.toWorldCoordinates(point, navigator.getZoom());
		List<Drawable> drawablesAtPoint = getDrawablesAt(this, pointZoomed.getPoint2D(), new LinkedList<Drawable>());
		/*
		 * After the query was performed with the transformed point coordinates and
		 * the container is not part of the result list, try to add the container using
		 * untransformed coordinates.
		 * // TODO: This could possibly be fixed by adapting the static method getDrawablesAt(Container, Point2D, LinkedList)
		 */
		if (!drawablesAtPoint.contains(this) && getBounds().contains(point)) {
			drawablesAtPoint.add(this);
		}
		return drawablesAtPoint;
	}

	public Point2D getPositionOf(Drawable drawable) {
		DrawableContainerNavigator navigator = getNavigator();
		PointND<? extends Number> positionZoomed = new PointND<Number>(drawable.getX(), drawable.getY());
		Point2D positionScreen = navigator.toViewCoordinates(positionZoomed, navigator.getZoom());
		return positionScreen;
	}
}
