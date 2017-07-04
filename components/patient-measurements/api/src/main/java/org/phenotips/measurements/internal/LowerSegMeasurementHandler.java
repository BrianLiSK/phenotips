/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */
package org.phenotips.measurements.internal;

import org.xwiki.component.annotation.Component;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Lower-body-Segment length, in centimeters.
 *
 * @version $Id$
 * @since 1.4
 */
@Component
@Named("lowerSeg")
@Singleton
public class LowerSegMeasurementHandler extends AbstractMeasurementHandler
{
    @Override
    public String getName()
    {
        return "lowerSeg";
    }

    @Override
    public String getUnit()
    {
        return "cm";
    }
}
